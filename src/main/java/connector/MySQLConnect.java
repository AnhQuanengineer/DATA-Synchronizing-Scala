package connector;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLConnect implements AutoCloseable {

    private static volatile HikariDataSource dataSource = null;
    private static final Object lock = new Object();

    private final String host;
    private final int port;
    private final String user;
    private final String password;

    public static class Builder {
        private String host;
        private Integer port;
        private String user;
        private String password;

        public Builder host(String host) { this.host = host; return this; }
        public Builder port(int port) { this.port = port; return this; }
        public Builder user(String user) { this.user = user; return this; }
        public Builder password(String password) { this.password = password; return this; }

        public MySQLConnect build() {
            if (host == null || port == null || user == null || password == null) {
                throw new IllegalStateException("MySQL config requires host, port, user, and password.");
            }
            MySQLConnect instance = new MySQLConnect(this);
            instance.intializePool(); // Khởi tạo Pool khi đối tượng được xây dựng lần đầu
            return instance;
        }
    }

    public MySQLConnect(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.user = builder.user;
        this.password = builder.password;
    }

    // Phương thức tĩnh để bắt đầu Builder
    public static Builder builder() {
        return new Builder();
    }

    private void intializePool() {
        if (dataSource == null) {
            synchronized (lock) {
                if (dataSource == null) {
                    HikariConfig config = new HikariConfig();

                    String jdbcUrl = String.format("jdbc:mysql://%s:%d", host, port);

                    config.setJdbcUrl(jdbcUrl);
                    config.setUsername(user);
                    config.setPassword(password);

                    // Cấu hình Pool cơ bản để tối ưu cho ứng dụng chạy dài
                    config.setMinimumIdle(5); // Số lượng kết nối nhàn rỗi tối thiểu
                    config.setMaximumPoolSize(20); // Số lượng kết nối tối đa
                    config.setConnectionTimeout(30000); // 30 giây chờ lấy kết nối
                    config.setIdleTimeout(600000); // 10 phút timeout cho kết nối nhàn rỗi
                    config.setMaxLifetime(1800000); // 30 phút là thời gian sống tối đa của một kết nối

                    config.setAutoCommit(false);

                    dataSource = new HikariDataSource(config);
                    System.out.println("--------------------MySQL Connection Pool (HikariCP) Initialized------------------");
                }
            }
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Connection Pool has not been initialized. Call build() first in your main method.");
        }

        return dataSource.getConnection();
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
            System.out.println("--------------------MySQL Connection Pool Shut Down Successfully----------------------");
        }
    }
}
