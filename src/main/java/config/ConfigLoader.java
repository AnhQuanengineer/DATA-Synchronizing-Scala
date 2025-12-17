package config;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import config.database.MySQLConfig;
import config.transfer.KafkaConfig;
import config.transfer.TransferConfig;
import config.transfer.ValidateConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        return new KafkaConfig(bootstrapServers, topic, groupId);
    }

    private TransferConfig loadTransferConfig() {
        System.out.println("--- Loading and validating Kafka Configs... ---");
        KafkaConfig kafkaConfig = loadKafkaConfig();
        System.out.println("--- All configurations loaded successfully. ---");
        return new TransferConfig(kafkaConfig);
    }

    public Map<String, MySQLConfig> getDatabaseConfig() throws IllegalArgumentException, IOException {
        Map<String, MySQLConfig> configs = new HashMap<>();

        configs.put("mysql", MySQLConfig.builder()
                .host(config.getString("Mysql.host"))
                .port(config.getInt("Mysql.port"))
                .user(config.getString("Mysql.user"))
                .password(config.getString("Mysql.password"))
                .database(config.getString("Mysql.database"))
                .tableUsers("users")
                .tableRepositories("repos")
                .logTimestamps(config.getString("Mysql.log_last_timestamp"))
                .build()
        );

        return configs;
    }

    public static void main(String[] args) throws IOException {
        ConfigLoader loader = ConfigLoader.getInstance();

        TransferConfig transferConfig = loader.getTransferConfig();

        KafkaConfig kafkaConfig = transferConfig.getKafkaConfig();

        System.out.println(kafkaConfig.getTopic());

        Map<String, MySQLConfig> dbConfig = loader.getDatabaseConfig();

        MySQLConfig mysqlConfig = dbConfig.get("mysql");

        System.out.println(mysqlConfig.getLogTimestamps());

    }
}
