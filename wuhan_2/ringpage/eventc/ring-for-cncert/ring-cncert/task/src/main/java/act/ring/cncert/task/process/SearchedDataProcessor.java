package act.ring.cncert.task.process;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import act.ring.cncert.data.bean.Foreign;
import act.ring.cncert.data.common.base.Maybe;
import act.ring.cncert.data.es.Elasticsearch;
import act.ring.cncert.data.es.Logstash;
import act.ring.cncert.data.nlp.Classification;
import act.ring.cncert.task.kafka.KafkaRawReader;
import kafka.consumer.ConsumerIterator;
import kafka.message.MessageAndMetadata;

/**
 * Processor for data from facebook, twitter and youtube.
 */
public class SearchedDataProcessor extends Processor<Foreign> {

    private final static Logger LOG = LoggerFactory.getLogger(SearchedDataProcessor.class);
    private static final String KAFKA_FM_TOPIC = "Search";
    private static final String LOGSTASH_ENDPOINT = "http://10.1.1.11:31313";
    private static final String ES_INDEX_PREFIX = "foreigns-";
    private static final String REMOVE_DUP_ENDPOINT = "http://10.1.1.11:30001/dupForeignMedia";

    private static final String KAFKA_ADDR = "10.1.1.11:9092,10.1.1.12:9092,10.1.1.12:9094";

    private KafkaRawReader reader;
    private Logstash<Foreign> logstash;
    private Elasticsearch<Foreign> elasticsearch;

    private ObjectMapper objectMapper;
    private Classification classify;

    private ThreadLocal<SimpleDateFormat> chineseDateFormat =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("h:mm - yyyy年M月d日"));

    public SearchedDataProcessor() {
        super(REMOVE_DUP_ENDPOINT);
        reader = new KafkaRawReader(KAFKA_ADDR, KAFKA_GROUP_ID, ZOOKEEPER_ADDR);
        elasticsearch = new Elasticsearch<>(Foreign.class, ES_INDEX_PREFIX);
        logstash = new Logstash<>(LOGSTASH_ENDPOINT);

        this.objectMapper = new ObjectMapper();
        this.classify = new Classification();
    }

    public static void main(String[] args) throws Exception {
        SearchedDataProcessor processor = new SearchedDataProcessor();
        processor.run();
    }

    @Override
    public void run() {
        ConsumerIterator<byte[], byte[]> consumerIterator = reader.subscribeTopic(KAFKA_FM_TOPIC);

        MessageAndMetadata<byte[], byte[]> rec;
        JsonNode value;

        while (consumerIterator.hasNext()) {
            rec = consumerIterator.next();
            try {
                value = objectMapper.readTree(new String(rec.message()).replaceAll("\\n", "\\\\n"));
            } catch (IOException e) {
                LOG.error("get invalid news, invalid value is {}",
                          new String(rec.message()));
                e.printStackTrace();
                continue;
            }

            String id = value.get("id").asText();
            Foreign item = new Foreign(id);

            try {
                String title = value.get("title").asText();
                String text = value.get("description").asText();
                item.setTitle(title);
                item.setText(text);
                item.setUrl(value.get("url").asText());
                item.setUser(value.get("author").asText());
                item.setUserid(value.get("authorid").asText());
                item.setEmotion(classify.getEmotionTypeHttp(title + text));
                item.setType0(classify.getTextType(title + text));
                item.setType1(classify.getTextType1(title + text));
                item.setType2(classify.getTextType2(title + text));
                item.setTime(this.parseHumanTime(value.get("pubtime").asText()));
                if (value.has("view_count")) {
                    item.setViewcnt(value.get("view_count").asInt());
                }
                if (value.has("like_count")) {
                    item.setLikecnt(value.get("like_count").asInt());
                }
                if (value.has("search_keywords")) {
                    item.setCorewords(value.get("search_keywords").asText());
                } else {
                    item.setCorewords(value.get("keywords").asText());
                }
                item.setDetailwords(value.get("keywords").asText());
            } catch (Exception e) {
                LOG.error("get invalid foreign searched data, id is {}, invalid json value is {}",
                          id,
                          new String(rec.message()));
                e.printStackTrace();
                continue;
            }

            // DONNOT store items with empty titles.
            if (item.getTitle().isEmpty() || item.getTitle().length() < 5) {
                continue;
            }

            try {
                Maybe<String> dup = duplicateWith(item.getId(), item.getTitle());
                if (dup.isJust() && !item.getId().equals(dup.fromJust())) {
                    System.out.println(item.getId() + "  ===>  " + dup.fromJust());
                    Foreign parent = elasticsearch.fetchItem(dup.fromJust());
                    int retry = 0;
                    while (parent == null && retry < 1) {
                        retry += 1;
                        LOG.warn("retry {}: fetch foreigns {} from es...", retry, dup.fromJust());
                        Thread.sleep(5000);
                        parent = elasticsearch.fetchItem(dup.fromJust());
                    }
                    if (parent != null) {
                        item.setParentid(dup.fromJust());
                        List<String> simids = parent.getSimids();
                        if (simids == null) {
                            simids = new LinkedList<>();
                        }
                        if (!simids.contains(item.getId())) {
                            simids.add(item.getId());
                            LOG.info("duplicate foreigns found: {} duplicated with {}",
                                     item.getId(),
                                     parent.getId());
                            elasticsearch
                                .updateItemWithListValue(parent.getEsIndex(), parent.getId(),
                                                         "simids",
                                                         item.getTime(),
                                                         simids);
                        }
                        item.setImportance(parent.getImportance());
                        item.setRecommend(parent.getRecommend());
                        item.setRisk(parent.getRisk());
                        item.setGfw(parent.getGfw());
                        item.setSecu(parent.getSecu());

                        if (parent.getEmotion() != 0 && item.getEmotion() == 0) {
                            item.setEmotion(parent.getEmotion());
                        } else {
                            this.refillSentiment(item, item.getTitle(), item.getText());
                        }
                    } else {
                        this.fillCncertIndex(item, item.getTitle(), item.getText());
                        this.fillCncertSecu("foreign", item, item.getTitle());
                        this.refillSentiment(item, item.getTitle(), item.getText());
                    }
                } else {
                    this.fillCncertIndex(item, item.getTitle(), item.getText());
                    this.fillCncertSecu("foreign", item, item.getTitle());
                    this.refillSentiment(item, item.getTitle(), item.getText());
                }
            } catch (IOException | InterruptedException e) {
                LOG.error("duplicate removal request fail.");
            }

            try {
                logstash.store(item);
            } catch (IOException e) {
                e.printStackTrace();
                LOG.error("fail to store logstash: {}: {}", id, new String(rec.message()));
            }
        }
    }

    private Date parseHumanTime(String s) throws ParseException {
        Date date;
        String apm = s.substring(0, 2);
        if (apm.equals("上午") || apm.equals("下午")) {
            date = chineseDateFormat.get().parse(s.substring(2));
            if (apm.equals("下午")) {
                date = DateUtils.addHours(date, 12);
            }
            date = DateUtils.addHours(date, 16); // 时区调整
        } else {
            date = isoTime.get().parse(s.replaceAll("Z$", "+0000"));
        }
        return date;
    }
}
