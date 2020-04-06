package act.ring.cncert.task.process;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import act.ring.cncert.data.bean.News;
import act.ring.cncert.data.bean.Opinion;
import act.ring.cncert.data.common.base.Maybe;
import act.ring.cncert.data.es.Elasticsearch;
import act.ring.cncert.data.es.Logstash;
import act.ring.cncert.data.nlp.Classification;
import act.ring.cncert.data.topic.ExtractOpinion;
import act.ring.cncert.task.kafka.KafkaRawReader;
import kafka.consumer.ConsumerIterator;
import kafka.message.MessageAndMetadata;

/**
 * Processor for data from news from crawler.
 */
public class TerrenProcessor extends Processor<News> {

    public static final ThreadLocal<SimpleDateFormat> plainTime =
        new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("yyyyMMddHHmmss");
            }
        };
    private final static Logger LOG = LoggerFactory.getLogger(TerrenProcessor.class);
    private static final String KAFKA_TERREN_TOPIC = "Terren";
    private static final String LOGSTASH_ENDPOINT = "http://10.1.1.11:31311";
    private static final String ES_INDEX_PREFIX = "news-";
    private static final String ES_OPINION_INDEX = "opinions";
    private static final String REMOVE_DUP_ENDPOINT = "http://10.1.1.11:30001/dupNews";


    private KafkaRawReader reader;
    private Logstash<News> logstash;
    private Elasticsearch<News> elasticsearch;
    private Classification classify;
    private ExtractOpinion extractOpinion;

    private ObjectMapper objectMapper;

    public TerrenProcessor() {
        super(REMOVE_DUP_ENDPOINT);
        reader = new KafkaRawReader(KAFKA_ADDR, KAFKA_GROUP_ID, ZOOKEEPER_ADDR);
        elasticsearch = new Elasticsearch<>(News.class, ES_INDEX_PREFIX);
        logstash = new Logstash<>(LOGSTASH_ENDPOINT);
        classify = new Classification();
        extractOpinion = new ExtractOpinion();

        this.objectMapper = new ObjectMapper();
    }

    public static void main(String[] args) throws Exception {
        TerrenProcessor processor = new TerrenProcessor();
        processor.run();
    }

    @Override
    public void run() {
        ConsumerIterator<byte[], byte[]> consumerIterator = reader.subscribeTopic(KAFKA_TERREN_TOPIC);

        MessageAndMetadata<byte[], byte[]> rec;
        String key;
        JsonNode value;

        while (consumerIterator.hasNext()) {
            rec = consumerIterator.next();
            try {
                value = objectMapper.readTree(new String(rec.message()).replaceAll("\\n", "\\\\n"));
            } catch (IOException e) {
                LOG.error("get invalid terren, invalid value is {}",
                          new String(rec.message()));
                continue;
            }
            key = new String(value.get("id").asText());

            if (!elasticsearch.fetchItems(Arrays.asList(key)).isEmpty()) {
                continue; // IGNORE duplicate items.
            }

            News news = new News(key);
            try {
                if (value.has("customer")) {
                    news.setUser(value.get("customer").asText());
                }
                news.setTitle(value.get("title").asText().trim());
                news.setText(value.get("content").asText());
                news.setLocation(refineLoc(value.get("location").asText()));
                news.setEmotion(value.get("emotion").asInt());
                news.setUrl(value.get("url").asText());
                news.setCorewords(value.get("keyword").asText());
                news.setEmotion(classify.getEmotionTypeHttp(news.getTitle()));
                news.setType0(classify.getTextType(news.getTitle() + " " + news.getText()));
                news.setType1(classify.getTextType1(news.getTitle() + " " + news.getText()));
                news.setType2(classify.getTextType2(news.getTitle() + " " + news.getText()));
                news.setTime(isoTime.get().parse(value.get("time").asText().replaceAll("Z$", "+0000")));
            } catch (Exception e) {
                LOG.error("get invalid news, id is {}, invalid json value is {}", key,
                          new String(rec.message()));
                e.printStackTrace();
                continue;
            }

            // DONNOT store items with empty titles.
            String title = news.getTitle();
            if (title.isEmpty() || title.length() < 5) {
                continue;
            }

            try {
                Maybe<String> dup = duplicateWith(news.getId(), news.getTitle());
                if (dup.isJust() && !news.getId().equals(dup.fromJust())) {
                    if (dup.fromJust().equals(news.getId())) {
                        continue;
                    }
                    System.out.println(news.getId() + "  ===>  " + dup.fromJust());
                    News parent = elasticsearch.fetchItem(dup.fromJust());
                    int retry = 0;
                    while (parent == null && retry < 1) {
                        retry += 1;
                        LOG.warn("retry {}: fetch terren {} from es...", retry, dup.fromJust());
                        Thread.sleep(5000);
                        parent = elasticsearch.fetchItem(dup.fromJust());
                    }
                    if (parent != null) {
                        news.setParentid(dup.fromJust());
                        List<String> simids = parent.getSimids();
                        if (simids == null) {
                            simids = new LinkedList<>();
                        }
                        if (!simids.contains(news.getId())) {
                            simids.add(news.getId());
                            LOG.info("duplicate terren found: {} duplicated with {}", news.getId(),
                                     parent.getId());
                            elasticsearch
                                .updateItemWithListValue(parent.getEsIndex(), parent.getId(),
                                                         "simids",
                                                         news.getTime(),
                                                         simids);
                        }
                        news.setImportance(parent.getImportance());
                        news.setRecommend(parent.getRecommend());
                        news.setRisk(parent.getRisk());
                        news.setGfw(parent.getGfw());
                        news.setSecu(parent.getSecu());

                        if (parent.getEmotion() != 0 && news.getEmotion() == 0) {
                            news.setEmotion(parent.getEmotion());
                        } else {
                            this.refillSentiment(news, news.getTitle(), news.getText());
                        }
                    } else {
                        fillCncertIndex(news, news.getTitle(), news.getText());
                        fillCncertSecu("terren", news, news.getTitle());
                        refillSentiment(news, news.getTitle(), news.getText());
                    }
                } else {
                    fillCncertIndex(news, news.getTitle(), news.getText());
                    fillCncertSecu("terren", news, news.getTitle());
                    refillSentiment(news, news.getTitle(), news.getText());
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                LOG.error("duplicate removal request fail.");
            }

            for (int tagKey : extractOpinion.getOpinion_tag().keySet()) {
                try {
                    Opinion opinion = extractOpinion.ExtractOpinionBySource(news, tagKey);
                    if (opinion != null) {
                        elasticsearch.updateOpinion(ES_OPINION_INDEX, opinion);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                logstash.store(news);
            } catch (IOException e) {
                LOG.error("fail to store logstash: {}: {}", key, new String(rec.message()));
            }
        }
    }
}
