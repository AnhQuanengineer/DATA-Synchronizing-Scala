package config.database;

import java.util.ArrayList;
import java.util.List;

public class MySQLConfig implements ValidateConfig {

    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String database;

    private final String tableUsers;
    private final String tableRepositories;
    private final String logTimestamps;

    private MySQLConfig(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.user = builder.user;
        this.password = builder.password;
        this.database = builder.database;
        this.tableUsers = builder.tableUsers;
        this.tableRepositories = builder.tableRepositories;
        this.logTimestamps = builder.logTimestamps;
        validate();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String host;
        private Integer port; // Dùng Integer để có thể kiểm tra null
        private String user;
        private String password;
        private String database;
        private String tableUsers = "Users";
        private String tableRepositories = "Repositories";
        private String logTimestamps;

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        // Port được nhận là int, nhưng lưu vào Integer để kiểm tra null trong build()
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder database(String database) {
            this.database = database;
            return this;
        }

        public Builder tableUsers(String tableUsers) {
            this.tableUsers = tableUsers;
            return this;
        }

        public Builder tableRepositories(String tableRepositories) {
            this.tableRepositories = tableRepositories;
            return this;
        }

        public Builder logTimestamps(String logTimestamps) {
            this.logTimestamps = logTimestamps;
            return this;
        }

        public MySQLConfig build() {
            if (host == null || port == null || user == null || password == null || database == null || logTimestamps == null) {
                throw new IllegalStateException("MySQL config requires host, port, user, password, and database.");
            }
            return new MySQLConfig(this);
        }
    }

    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getUser() { return user; }
    public String getDatabase() { return database; }
    public String getPassword() { return password; }
    public String getTableUsers() { return tableUsers; }
    public String getTableRepositories() { return tableRepositories; }
    public String getLogTimestamps() { return logTimestamps; }

    @Override
    public void validate() {
        List<String> requiredFields = new ArrayList<>();

        if (host == null || host.isEmpty()) {
            requiredFields.add("host");
        }

        if ( port == 0) {
            requiredFields.add("port");
        }

        if (user == null || user.isEmpty()) {
            requiredFields.add("user");
        }

        if (database == null || database.isEmpty()) {
            requiredFields.add("database");
        }

        if (password == null || password.isEmpty()) {
            requiredFields.add("password");
        }

        if (logTimestamps == null || logTimestamps.isEmpty()) {
            requiredFields.add("logTimestamps");

        }

        if (!requiredFields.isEmpty()) {
            String missingKeys = String.join(", ", requiredFields);
            throw new IllegalArgumentException("----------Missing config for MongoDBConfig: " + missingKeys + "-------------");
        }
    }

}
