package act.ring.cncert.task.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.Future;

public class KafkaJsonWriter<T> {

    private ObjectMapper objectMapper = new ObjectMapper();
    private Producer<String, JsonNode> producer;

    public KafkaJsonWriter(String kafkaAddr, String zkAddr) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaAddr);
        props.put("zookeeper.connect", zkAddr);
        props.put("acks", "all");
        props.put("retries", 3);
        props.put("batch.size", 1);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.connect.json.JsonSerializer");

        this.producer = new KafkaProducer<String, JsonNode>(props);
    }

    public Future<RecordMetadata> sendMessage(String topic, String key, T value) {
        return this.producer
            .send(new ProducerRecord<>(topic, key, objectMapper.valueToTree(value)));
    }
}