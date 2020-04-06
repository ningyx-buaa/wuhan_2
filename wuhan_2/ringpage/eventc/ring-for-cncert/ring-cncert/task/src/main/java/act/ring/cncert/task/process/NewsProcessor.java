package act.ring.cncert.task.process;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.bcel.internal.generic.NEW;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
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
import act.ring.cncert.task.kafka.KafkaJsonReader;
import act.ring.cncert.task.kafka.KafkaRawReader;
import kafka.consumer.ConsumerIterator;
import kafka.message.MessageAndMetadata;

/**
 * Processor for data from news from crawler.
 */
public class NewsProcessor extends Processor<News> {

    public static final ThreadLocal<SimpleDateFormat> plainTime =
        new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("yyyyMMddHHmmss");
            }
        };
    private final static Logger LOG = LoggerFactory.getLogger(NewsProcessor.class);
    private static final String KAFKA_NEWS_TOPIC = "news";
    private static final String LOGSTASH_ENDPOINT = "http://10.1.1.11:31311";
    private static final String ES_INDEX_PREFIX = "news-";
    private static final String ES_OPINION_INDEX = "opinions";
    private static final String REMOVE_DUP_ENDPOINT = "http://10.1.1.11:30001/dupNews";
    private static final String REMOVE_DUP_OPINION_ENDPOINT = "http://10.1.1.11:30001/dupOpinion";


    private KafkaRawReader reader;
    private Logstash<News> logstash;
    private Elasticsearch<News> elasticsearch;
    private Classification classify;
    private ExtractOpinion extractOpinion;

    private ObjectMapper objectMapper;

    public NewsProcessor() {
        super(REMOVE_DUP_ENDPOINT);
        reader = new KafkaRawReader(KAFKA_ADDR, KAFKA_GROUP_ID, ZOOKEEPER_ADDR);
        elasticsearch = new Elasticsearch<>(News.class, ES_INDEX_PREFIX);
        logstash = new Logstash<>(LOGSTASH_ENDPOINT);
        classify = new Classification();
        extractOpinion = new ExtractOpinion();

        this.objectMapper = new ObjectMapper();
    }

    public static void main(String[] args) throws Exception {
        NewsProcessor processor = new NewsProcessor();
        processor.run();
    }

    @Override
    public void run() {
        ConsumerIterator<byte[], byte[]> consumerIterator = reader.subscribeTopic(KAFKA_NEWS_TOPIC);

        MessageAndMetadata<byte[], byte[]> rec;
        String key;
        JsonNode value;

        while (consumerIterator.hasNext()) {
            rec = consumerIterator.next();
            key = new String(rec.key());
            try {
                value = objectMapper.readTree(new String(rec.message()).replaceAll("\\n", "\\\\n"));
            } catch (IOException e) {
                LOG.error("get invalid news, id is {}, invalid value is {}", key,
                          new String(rec.message()));
                continue;
            }

            if (!elasticsearch.fetchItems(Arrays.asList(key)).isEmpty()) {
                continue; // IGNORE duplicate items.
            }

            News news = new News(key);
            try {
                news.setUser(value.get("post_user").asText());
                news.setUserid(value.get("uid").asText());
                news.setUserno(Integer.valueOf(news.getUserid()));
                news.setTitle(value.get("title").asText().trim());
                news.setText(value.get("text").asText());
                news.setLocation(refineLoc(value.get("location").asText()));
                news.setEmotion(value.get("emotion").asInt());
                news.setUrl(value.get("url").asText());
                news.setType0(classify.getTextType(news.getTitle() + " " + news.getText()));
                if (value.has("eventtype")) {
                    news.setType1(value.get("eventtype").asInt());
                } else {
                    news.setType1(0);
                }
                if (value.has("eventtype2")) {
                    news.setType2(value.get("eventtype2").asInt());
                } else {
                    news.setType2(0);
                }
                news.setSecu(value.get("secu").asInt());
                if (value.get("time") != null && value.get("time").asText() != null) {
                    news.setTime(plainTime.get().parse(value.get("time").asText()));
                }
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
                        LOG.warn("retry {}: fetch news {} from es...", retry, dup.fromJust());
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
                            LOG.info("duplicate news found: {} duplicated with {}", news.getId(),
                                     parent.getId());
                            elasticsearch
                                .updateItemWithListValue(parent.getEsIndex(), parent.getId(),
                                                         "simids", news.getTime(),
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
                        refillSentiment(news, news.getTitle(), news.getText());
                    }
                } else {
                    fillCncertIndex(news, news.getTitle(), news.getText());
                    refillSentiment(news, news.getTitle(), news.getText());
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                LOG.error("duplicate removal request fail.");
            }

            try {
                this.processOpinion(news);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                logstash.store(news);
            } catch (IOException e) {
                LOG.error("fail to store logstash: {}: {}", key, new String(rec.message()));
            }
        }
    }

    private void processOpinion(News news) throws IOException {
        for (int tagKey : extractOpinion.getOpinion_tag().keySet()) {
            Opinion opinion = extractOpinion.ExtractOpinionBySource(news, tagKey);
            if (opinion != null) {
                Maybe<String> dup = duplicateWith(opinion.getId(), opinion.getText());
                if (dup.isJust() && !opinion.getId().equals(dup.fromJust())) {
                    continue;
                }
                elasticsearch.updateOpinion(ES_OPINION_INDEX, opinion);
            }
        }
    }
}