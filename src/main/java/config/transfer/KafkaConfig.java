package config.transfer;

import java.util.ArrayList;
import java.util.List;

public class KafkaConfig implements ValidateConfig{
    private final String bootstrapServers;
    private final String topic;
    private final String group_id;

    public KafkaConfig(String bootstrapServers, String topic, String group_id) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        this.group_id = group_id;

        validate();
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getTopic() {
        return topic;
    }

    public String getGroup_id() {
        return group_id;
    }

    @Override
    public void validate() {
        List<String> requiredFields = new ArrayList<>();

        if (bootstrapServers == null || bootstrapServers.isEmpty()) {
            requiredFields.add("bootstrapServers");
        }

        if (topic == null || topic.isEmpty()) {
            requiredFields.add("topic");
        }

        if (group_id == null || group_id.isEmpty()) {
            requiredFields.add("group_id");
        }

        if (!requiredFields.isEmpty()) {
            String missingKeys = String.join(", ", requiredFields);
            throw new IllegalArgumentException("----------Missing config for MongoDBConfig: " + missingKeys + "-------------");
        }
    }
}
