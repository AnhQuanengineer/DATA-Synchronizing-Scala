package config.transfer;

public class TransferConfig {
    private final KafkaConfig kafkaConfig;

    public TransferConfig(KafkaConfig kafkaConfig) {
        this.kafkaConfig = kafkaConfig;
    }

    public KafkaConfig getKafkaConfig() {
        return kafkaConfig;
    }
}
