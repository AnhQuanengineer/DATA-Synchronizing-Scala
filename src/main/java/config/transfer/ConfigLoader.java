package config.transfer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;

public class ConfigLoader {
    private static final ConfigLoader INSTANCE = new ConfigLoader();
    private final Config config;
    private final TransferConfig transferConfig;

    private ConfigLoader() {
        this.config = ConfigFactory.load();
        this.transferConfig = loadTransferConfig();
    }

    public static ConfigLoader getInstance() {
        return INSTANCE;
    }

    public TransferConfig getTransferConfig() {
        return transferConfig;
    }

    private KafkaConfig loadKafkaConfig() {
        String bootstrapServers;
        String groupId;
        String topic;
        try {
            bootstrapServers = config.getString("Kafka.bootstrapServers");
            groupId = config.getString("Kafka.groupId");
            topic = config.getString("Kafka.topic");

        } catch (ConfigException e) {
            System.out.println("ConfigException: " + e.getMessage());
            return null;
        }
        return new KafkaConfig(bootstrapServers, groupId, topic);
    }

    private TransferConfig loadTransferConfig() {
        System.out.println("--- Loading and validating Kafka Configs... ---");
        KafkaConfig kafkaConfig = loadKafkaConfig();
        System.out.println("--- All configurations loaded successfully. ---");
        return new TransferConfig(kafkaConfig);
    }

    public static void main(String[] args) {
        ConfigLoader loader = ConfigLoader.getInstance();

        TransferConfig transferConfig = loader.getTransferConfig();

        KafkaConfig kafkaConfig = transferConfig.getKafkaConfig();

        System.out.println(kafkaConfig.getBootstrapServers());

    }
}
