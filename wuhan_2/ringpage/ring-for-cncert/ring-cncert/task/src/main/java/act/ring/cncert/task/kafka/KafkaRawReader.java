package act.ring.cncert.task.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

/**
 * Created by HE, Tao on 2017/11/02.
 */
public class KafkaRawReader {

    private Properties props;
    private ConsumerConnector consumerConnector;

    public KafkaRawReader(String kafkaAddr, String kafkaGroupId, String zkAddr) {
        this.props = new Properties();

//        props.put("bootstrap.servers", kafkaAddr); // MAKE NO SENSE
        props.put("zookeeper.connect", zkAddr);
        props.put("group.id", kafkaGroupId);
        props.put("auto.offset.reset", "smallest");
        props.put("auto.commit.enable", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "300000");

        this.consumerConnector = Consumer.createJavaConsumerConnector(new ConsumerConfig(props));
    }

    public ConsumerIterator<byte[], byte[]> subscribeTopic(String topic) {

        Map<String, Integer> topicCounts = new HashMap<>();
        topicCounts.put(topic, 1); // retrieve one element once.

        KafkaStream<byte[], byte[]>
            stream =
            consumerConnector.createMessageStreams(topicCounts).get(topic).get(0);
        return stream.iterator();
    }
}
