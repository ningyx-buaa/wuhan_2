package act.ring.cncert.task.process;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import act.ring.cncert.data.bean.Event;
import act.ring.cncert.data.common.base.Maybe;
import act.ring.cncert.data.es.Elasticsearch;
import act.ring.cncert.data.es.Logstash;
import act.ring.cncert.task.kafka.KafkaRawReader;
import kafka.consumer.ConsumerIterator;
import kafka.message.MessageAndMetadata;

/**
 * Processor for data from events from ring-event.
 */
public class WeiboEventProcessor extends Processor<Event> {

    private final static Logger LOG = LoggerFactory.getLogger(WeiboEventProcessor.class);

    private static final String KAFKA_EVENTS_TOPIC = "weibo_events";

    private static final String LOGSTASH_ENDPOINT = "http://10.1.1.11:31312";
    private static final String ES_INDEX_PREFIX = "weibo_events-";
    private static final String REMOVE_DUP_ENDPOINT = "http://10.1.1.11:30001/dupWeiboEvents";

    private ObjectMapper objectMapper = new ObjectMapper();

    private KafkaRawReader reader;
    private Logstash<Event> logstash;
    private Elasticsearch<Event> elasticsearch;

    public WeiboEventProcessor() {
        super(REMOVE_DUP_ENDPOINT);
        reader = new KafkaRawReader(KAFKA_ADDR, KAFKA_GROUP_ID, ZOOKEEPER_ADDR);
        logstash = new Logstash<>(LOGSTASH_ENDPOINT);
        elasticsearch = new Elasticsearch<>(Event.class, ES_INDEX_PREFIX);
    }

    public static void main(String[] args) throws Exception {
        WeiboEventProcessor processor = new WeiboEventProcessor();
        processor.run();
    }

    public void run() {
        ConsumerIterator<byte[], byte[]>
            consumerIterator =
            reader.subscribeTopic(KAFKA_EVENTS_TOPIC);
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

            Event event = new Event(key);
            try {
                String user = "", userid = "";
                if (!value.get("weiboIds").asText().isEmpty()) {
                    user = "微博";
                    userid = value.get("weiboIds").asText();
                } else if (!value.get("webNewsIds").asText().isEmpty()) {
                    user = "新闻";
                    userid = value.get("webNewsIds").asText();
                } else if (!value.get("wechatIds").asText().isEmpty()) {
                    user = "微信";
                    userid = value.get("wechatIds").asText();
                } else if (!value.get("tiebaIds").asText().isEmpty()) {
                    user = "贴吧";
                    userid = value.get("tiebaIds").asText();
                } else if (!value.get("terrensIds").asText().isEmpty()) {
                    user = "其他";
                    userid = value.get("terrensIds").asText();
                }
                event.setUser(user);
                event.setUserid(userid);
                event.setTitle(value.get("description").asText().trim());
                event.setText(value.get("description").asText());
                event.setLocation(refineLoc(value.get("eventLoc").asText()));
                event.setEmotion(value.get("emotion").asInt());
                this.refillSentiment(event, event.getTitle(), "");
                event.setType0(value.get("e_type").asInt());
                event.setType1(value.get("eventType").asInt());
                event.setType2(value.get("eventType2").asInt());
                event.setTime(new Date(value.get("time").asLong()));
                event.setCorewords(value.get("detailwords").asText());
                event.setHot(value.get("hot").asInt());
                event.setImportance((int) (value.get("importance").asDouble(0.0) * 100f));
                event.setRecommend((int) (value.get("recommend").asDouble(0.0) * 100f));
                event.setRisk((int) (value.get("risk").asDouble(0.0) * 100f));
                event.setGfw(value.get("gfw").asInt());
                event.setSensitivity((int) (value.get("sensitivity").asDouble(0.0) * 100f));
                this.fillCncertSecu("event", event, event.getTitle());
            } catch (Exception e) {
                LOG.error("get invalid weibo_events, id is {}, invalid json value is {}", key,
                          value);
                e.printStackTrace();
                continue;
            }

            String title = event.getTitle();
            if (title.isEmpty() || title.length() < 5) {
                continue;
            }

            try {
                Maybe<String> dup = duplicateWith(event.getId(), event.getTitle());
                if (dup.isJust() && !event.getId().equals(dup.fromJust())) {
                    if (dup.fromJust().equals(event.getId())) {
                        continue;
                    }
                    Event parent = elasticsearch.fetchItem(dup.fromJust());
                    int retry = 0;
                    while (parent == null && retry < 1) {
                        retry += 1;
                        LOG.warn("retry {}: fetch events {} from es...", retry, dup.fromJust());
                        Thread.sleep(5000);
                        parent = elasticsearch.fetchItem(dup.fromJust());
                    }
                    if (parent != null) {
                        event.setParentid(dup.fromJust());
                        List<String> simids = parent.getSimids();
                        if (simids == null) {
                            simids = new LinkedList<>();
                        }
                        if (!simids.contains(event.getId())) {
                            simids.add(event.getId());
                            elasticsearch
                                .updateItemWithListValue(parent.getEsIndex(), parent.getId(),
                                                         "simids",
                                                         event.getTime(),
                                                         simids);
                            LOG.info("duplicate weibo_events found: {} duplicated with {}",
                                     event.getId(), parent.getId());
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                LOG.error("duplicate removal request fail.");
            }

            try {
                logstash.store(event);
            } catch (IOException e) {
                LOG.error("fail to store logstash: {}: {}", key, value);
            }
        }
    }
}
