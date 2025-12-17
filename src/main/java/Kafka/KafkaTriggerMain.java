package Kafka;

import config.ConfigLoader;
import config.database.MySQLConfig;
import config.transfer.KafkaConfig;
import config.transfer.TransferConfig;
import connector.MySQLConnect;
import model.MessageRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class KafkaTriggerMain {

    private static final Logger LOG = LogManager.getLogger(KafkaTriggerMain.class);

    public static void main(String[] args) throws IOException {
        ConfigLoader loader = ConfigLoader.getInstance();

        TransferConfig transferConfig = loader.getTransferConfig();

        KafkaConfig kafkaConfig = transferConfig.getKafkaConfig();

        Map<String, MySQLConfig> dbConfig = loader.getDatabaseConfig();

        MySQLConfig mysqlConfig = dbConfig.get("mysql");

        if (mysqlConfig == null) {
            LOG.error("Cannot load Database → Stop process");
            return;
        }

        if (kafkaConfig == null) {
            LOG.error("Cannot load Kafka → Stop process");
            return;
        }

        MySQLConnect mySQLConnect = createMySQLConnect(mysqlConfig);

        KafkaProducerHandler kafkaProducerHandler = new KafkaProducerHandler(kafkaConfig);
        KafkaConsumerHandler kafkaConsumerHandler = new KafkaConsumerHandler(kafkaConfig);

        TimestampManager manager = new TimestampManager(mysqlConfig.getLogTimestamps());
        String lastTimestamp = manager.loadLastTimestamp();

        LOG.info("=== KAFKA TRIGGER (FULL - JAVA 8) IS RUNNER ===");

        try {
            while (true) {
                List<MessageRecord> sent = kafkaProducerHandler.produce(lastTimestamp, mySQLConnect, kafkaConfig);
                if (!sent.isEmpty()) {
                    lastTimestamp = getLastTimestamp(sent);
                }

                List<MessageRecord> received = kafkaConsumerHandler.consume();

                if (!received.isEmpty() || !sent.isEmpty()) {
                    DataValidator.validateAndResend(
                            sent
                            , received
                            , kafkaProducerHandler.getProducer()
                            , kafkaConfig
                    );
                }

                manager.saveLastTimestamp(lastTimestamp);

                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Thread is interrupted → out");
        } catch (Exception e) {
            LOG.error("ERROR in main loop", e);
        } finally {
            // 4. DỌN DẸP TÀI NGUYÊN
            LOG.info("Stopping process...");
            kafkaConsumerHandler.close();
            kafkaProducerHandler.close();

            // ĐÓNG CONNECTION POOL (mySQLConnect.close() sẽ đóng Pool HikariCP)
            if (mySQLConnect != null) {
                try { mySQLConnect.close(); } catch (Exception ignored) {}
            }
            LOG.info("Process is off");
        }
    }

    public static MySQLConnect createMySQLConnect(MySQLConfig mySQLConfig) {
        if (mySQLConfig == null) {
            LOG.error("Not found configuration of MYSQL");
            return null;
        }

        return MySQLConnect.builder()
                .host(mySQLConfig.getHost())
                .port(mySQLConfig.getPort())
                .user(mySQLConfig.getUser())
                .password(mySQLConfig.getPassword())
                .build();
    }

    public static String getLastTimestamp(List<MessageRecord> records) {
        if (records == null || records.isEmpty()) return null;
        String max = null;

        for (MessageRecord record : records) {
            String ts = (String) record.getData().get("log_timestamp");
            if (max == null || (ts != null && ts.compareTo(max) > 0)) {
                max = ts;
            }
        }
        return max;
    }
}
