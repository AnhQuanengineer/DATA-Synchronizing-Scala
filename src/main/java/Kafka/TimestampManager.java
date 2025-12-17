package Kafka;

import config.ConfigLoader;
import config.database.MySQLConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TimestampManager {

    private final String timestampFile ;
    private static final String FALLBACK_TIMESTAMP = "";

    public TimestampManager(String timestampFile) {
        this.timestampFile = timestampFile;
    }

    private void ensureDirectoryExists(String filePath) {
        Path path = Paths.get(filePath);
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RuntimeException("Không thể tạo thư mục: " + parent, e);
            }
        }
    }

    public String loadLastTimestamp() {
        Path path = Paths.get(timestampFile);

        if (Files.exists(path) && Files.isRegularFile(path)) {
            try {
                List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                if (!lines.isEmpty()) {
                    return lines.get(0).trim();
                }
            } catch (IOException e) {
                System.err.println("Lỗi khi đọc file timestamp: " + e.getMessage());
            }
        }
        return FALLBACK_TIMESTAMP;
    }

    public void saveLastTimestamp(String ts) {
        ensureDirectoryExists(timestampFile);
        Path path = Paths.get(timestampFile);

        try {
            Files.write(
                    path,
                    Collections.singletonList(ts),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException("Không thể ghi timestamp vào file: " + timestampFile, e);
        }
    }

    public static void main(String[] args) {
        TimestampManager manager = new TimestampManager("logs/last_timestamp.txt");
        System.out.println("Last timestamp: " + manager.loadLastTimestamp());

        manager.saveLastTimestamp("2025-12-02T10:00:00Z");
        System.out.println("Saved new timestamp");
    }
}
