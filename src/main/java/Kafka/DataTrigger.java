package Kafka;

import config.ConfigLoader;
import config.database.MySQLConfig;
import connector.MySQLConnect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class DataTrigger {

    private static final Logger LOG = LogManager.getLogger(DataTrigger.class);
    private static final String DEFAULT_DATABASE_NAME = "github_data";
    private static final String TOPIC = "quandz";

    public static class TriggerResult {
       public List<Map<String, Object>> data;
       public String newTimestamp;

       public TriggerResult(List<Map<String, Object>> data, String newTimestamp) {
           this.data = data;
           this.newTimestamp = newTimestamp;
       }
    }

    public TriggerResult getDataTrigger(MySQLConnect mysqlClient, String lastTimestamp) {
        List<Map<String, Object>> data = new ArrayList<>();
        String newTimestamp = lastTimestamp != null ? lastTimestamp : "";

        String sql = "SELECT user_id, login, gravatar_id, avatar_url, url, state, " +
                "DATE_FORMAT(log_timestamp, '%Y-%m-%d %H:%i:%s.%f') AS log_timestamp1 " +
                "FROM user_log_after";

        StringBuilder queryBuilder = new StringBuilder(sql);
        boolean hasWhere = false;

        if (lastTimestamp != null && !lastTimestamp.trim().isEmpty()) {
            queryBuilder.append(" WHERE DATE_FORMAT(log_timestamp, '%Y-%m-%d %H:%i:%s.%f') > ?");
            hasWhere = true;
        }

        String finalQuery = queryBuilder.toString();

        try (Connection connection = mysqlClient.getConnection()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("USE " + DEFAULT_DATABASE_NAME);
            }

            try (PreparedStatement pstmt = connection.prepareStatement(finalQuery)) {
                if (hasWhere) {
                    pstmt.setString(1, lastTimestamp);
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("user_id", rs.getObject("user_id"));
                        row.put("login", rs.getString("login"));
                        row.put("gravatar_id", rs.getString("gravatar_id"));
                        row.put("avatar_url", rs.getString("avatar_url"));
                        row.put("url", rs.getString("url"));
                        row.put("state", rs.getString("state"));
                        row.put("log_timestamp", rs.getString("log_timestamp1"));
                        data.add(row);
                    }
                }
            }

            if (!data.isEmpty()) {
                newTimestamp = data.stream()
                        .map(m -> (String) m.get("log_timestamp"))
                        .max(String::compareTo)
                        .orElse(lastTimestamp);
            }

            LOG.info("Have read {} records from user_log_after. newTimestamp = {}", data.size(), newTimestamp);
        } catch (SQLException e) {
            LOG.error("ERROR SQL WHEN READ FORM user_log_after", e);
        } catch (Exception e) {
            LOG.error("ERROR SYSTEM", e);
        }
        return new TriggerResult(data, newTimestamp);
    }

    public static void main(String[] args) throws IOException {
        ConfigLoader loader = ConfigLoader.getInstance();

        Map<String, MySQLConfig> dbConfig = loader.getDatabaseConfig();

        MySQLConfig mysqlConfig = dbConfig.get("mysql");

//        System.out.println(mysqlConfig.getPort());

        MySQLConnect mySQLConnect = MySQLConnect.builder()
                .host(mysqlConfig.getHost())
                .port(mysqlConfig.getPort())
                .user(mysqlConfig.getUser())
                .password(mysqlConfig.getPassword())
                .build();

        TimestampManager manager = new TimestampManager(mysqlConfig.getLogTimestamps());

        DataTrigger trigger = new DataTrigger();
        TriggerResult result = trigger.getDataTrigger(mySQLConnect, manager.loadLastTimestamp());

        System.out.println("Count records: " + result.data);
        System.out.println("new Timestamp: " + result.newTimestamp);

        manager.saveLastTimestamp(result.newTimestamp);
    }
}

