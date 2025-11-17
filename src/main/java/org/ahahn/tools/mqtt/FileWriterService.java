package org.ahahn.tools.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileWriterService {
    private static final Logger logger = LoggerFactory.getLogger(FileWriterService.class);

    private final FileOutputConfig config;
    private final Map<String, TopicFileWriter> topicWriters = new ConcurrentHashMap<>();

    public FileWriterService(FileOutputConfig config) {
        this.config = config;
    }

    public void writeMessage(String topic, String payload) {
        try {
            TopicFileWriter writer = topicWriters.computeIfAbsent(topic, this::createTopicWriter);
            writer.writeMessage(payload);
        } catch (Exception e) {
            logger.error("Error writing message for topic {}: {}", topic, e.getMessage(), e);
        }
    }

    private TopicFileWriter createTopicWriter(String topic) {
        return new TopicFileWriter(topic, config);
    }

    public void shutdown() {
        topicWriters.values().forEach(TopicFileWriter::close);
        topicWriters.clear();
        logger.info("All file writers closed");
    }

}