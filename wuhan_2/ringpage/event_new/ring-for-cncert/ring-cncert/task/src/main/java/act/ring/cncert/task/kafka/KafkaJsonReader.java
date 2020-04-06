package act.ring.cncert.task.kafka;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

/**
 * Created by HE, Tao on 2017/11/02.
 *
 * @Deprecated I still don't find out why the consumer using the new API (use "bootstrap.servers"
 * rather than "zookeeper.connect", especially) doesn't work. This KafkaJsonReader is marked as
 * deprecated as a reminder to myself.
 */
public class KafkaJsonReader<T> extends Thread {

    private Consumer<String, JsonNode> consumer;

    public KafkaJsonReader(String kafkaAddr, String kafkaGroupId) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaAddr);
        props.put("group.id", kafkaGroupId);
        props.put("enable.auto.commit", "false");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "300000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.connect.json.JsonDeserializer");

        this.consumer = new KafkaConsumer<String, JsonNode>(props);
    }

    public Consumer<String, JsonNode> subscribeTopic(String... topics) {
        this.consumer.subscribe(Arrays.asList(topics));
        return this.consumer;
    }
}
