package Kafka;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.transfer.KafkaConfig;
import model.MessageRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.*;

public class KafkaConsumerHandler {
    private static final Logger LOG = LogManager.getLogger(KafkaConsumerHandler.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final KafkaConsumer<String, String> consumer;
    private long count = 0;

    public KafkaConsumerHandler(KafkaConfig kafkaConfig) {
        this.consumer = createConsumer(kafkaConfig);
    }

    private KafkaConsumer<String, String> createConsumer(KafkaConfig kafkaConfig) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaConfig.getBootstrapServers());
        props.put("group.id", kafkaConfig.getGroup_id());
        props.put("auto.offset.reset", "earliest");
        props.put("enable.auto.commit", "false");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(kafkaConfig.getTopic()));
        return consumer;
    }

    public List<MessageRecord> consume() {
        List<MessageRecord> received = new ArrayList<>();
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));

        for (ConsumerRecord<String, String> record : records) {
            try {
                Map<String, Object> data = MAPPER.readValue(record.value(), Map.class);
                count++;
                MessageRecord messageRecord = new MessageRecord(count, data);
                received.add(messageRecord);
                System.out.printf("CONSUMER [%d]: %s%n", count, data);
                LOG.info("CONSUMER [{}]: {}", count, data);
            } catch (Exception e){
                LOG.warn("Error parse JSON", e);
            }
        }

        if (!received.isEmpty()) {
            consumer.commitSync();
            System.out.printf("CONSUMER → Have received %d records%n", received.size());
            LOG.info("CONSUMER → Have received {} records", received.size());
        }

        return received;
    }

    public void close() {
        if (consumer != null) {
            consumer.close();
        }
    }
}
