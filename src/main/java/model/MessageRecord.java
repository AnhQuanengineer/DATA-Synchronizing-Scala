package model;

import java.util.Map;
import java.util.Objects;

public class MessageRecord {
    private final long count;
    private final Map<String, Object> data;

    public MessageRecord(long count, Map<String, Object> data) {
        this.count = count;
        this.data = data;
    }

    public long getCount() {
        return count;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
