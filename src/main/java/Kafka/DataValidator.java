package Kafka;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.transfer.KafkaConfig;
import model.MessageRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class DataValidator {
    private static final Logger LOG = LogManager.getLogger(DataValidator.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static void resend(MessageRecord messageRecord
                                , KafkaProducer<String, String> producer
                               , KafkaConfig kafkaConfig
                               ) {
        try {
            String json = MAPPER.writeValueAsString(messageRecord.getData());
            producer.send(new ProducerRecord<>(kafkaConfig.getTopic(), json));
            System.err.printf("RESEND [%d]: %s%n", messageRecord.getCount(), messageRecord.getData());
            LOG.warn("RESEND [{}]: {}", messageRecord.getCount(), messageRecord.getData());
        } catch (Exception e){
            System.err.println("Error resend " + e.getMessage());
            LOG.error("Error resend {}", e);
        }
    }

    public static void validateAndResend(
            List<MessageRecord> producerData
            , List<MessageRecord> consumerData
            , KafkaProducer<String, String> producer
            , KafkaConfig kafkaConfig
    ) {
        if (producerData.isEmpty()) return;

        Set<Long> producerCounts = new HashSet<>();
        Set<Long> consumerCounts = new HashSet<>();
        Map<Long, MessageRecord> producerRecordMap = new HashMap<>();

        for (MessageRecord messageRecord : producerData) {
            producerCounts.add(messageRecord.getCount());
            producerRecordMap.put(messageRecord.getCount(), messageRecord);
        }

        for (MessageRecord messageRecord : consumerData) {
            consumerCounts.add(messageRecord.getCount());
        }

        producerCounts.removeAll(consumerCounts);
        boolean hasMissing;

        if (!producerCounts.isEmpty()) {
            hasMissing = true;
            for (Long missingCount: producerCounts) {
                MessageRecord messageRecordProducer = producerRecordMap.get(missingCount);
                if (messageRecordProducer != null) {
                    resend(messageRecordProducer, producer, kafkaConfig);
                }
            }
        } else {
            hasMissing = false;
        }

        if (hasMissing) {
            System.err.println("VALIDATE → Missing data");
            LOG.warn("VALIDATE → Missing data");
        } else {
            System.out.printf("-----VALIDATE PASS----- (count: %d)%n", consumerCounts.size());
            LOG.info("-----VALIDATE PASS----- (count: {})", consumerCounts.size());
        }
    }
}
