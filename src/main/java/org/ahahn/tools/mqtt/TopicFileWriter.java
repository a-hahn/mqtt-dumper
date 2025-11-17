package org.ahahn.tools.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TopicFileWriter {

    private static final Logger logger = LoggerFactory.getLogger(TopicFileWriter.class);

    private final String topic;
    private final String safeTopicName;
    private final FileOutputConfig config;
    private BufferedWriter writer;
    private String currentDate;
    private long messageCount = 0;
    private boolean firstMessage = true;
    private DateTimeFormatter dateFormatter;

    public TopicFileWriter(String topic, FileOutputConfig config) {
        this.topic = topic;
        this.safeTopicName = topic.replaceAll("[^a-zA-Z0-9.-]", "_");
        this.config = config;
        initializeWriter();
    }

    private void initializeWriter() {
        try {
            this.dateFormatter = DateTimeFormatter.ofPattern(config.getDateFormat());
            this.currentDate = getCurrentDate();

            String dateDir = currentDate;
            Path dirPath = Paths.get(config.getBaseDir(), dateDir);
            Files.createDirectories(dirPath);

            String filename = safeTopicName + ".json";
            Path filePath = dirPath.resolve(filename);

            boolean fileExists = Files.exists(filePath);

            writer = new BufferedWriter(new FileWriter(filePath.toFile(), config.getAppendMode()));

            if (!fileExists || !config.getAppendMode()) {
                writer.write("["); // Start JSON array
                firstMessage = true;
            } else {
                // If appending to existing file, we need to check if we need to add a comma
                // This is simplified - in production you might want to read the last character
                firstMessage = false;
                writer.write(",");
                writer.newLine();
            }

            logger.info("Initialized writer for topic: {} -> {}", topic, filePath);

        } catch (IOException e) {
            logger.error("Failed to initialize writer for topic {}: {}", topic, e.getMessage(), e);
            throw new RuntimeException("Failed to initialize file writer", e);
        }
    }

    public void writeMessage(String payload) {
        try {
            checkDateChange();

            String jsonMessage = createJsonMessage(payload);
            writer.write(jsonMessage);
            writer.newLine();
            writer.flush();

            messageCount++;

            // Rotate file if message count exceeds limit
            if (config.getMaxMessagesPerFile() > 0 && messageCount >= config.getMaxMessagesPerFile()) {
                rotateFile();
            }

        } catch (IOException e) {
            logger.error("Error writing message for topic {}: {}", topic, e.getMessage(), e);
        }
    }

    private void checkDateChange() throws IOException {
        String today = getCurrentDate();
        if (!today.equals(currentDate)) {
            currentDate = today;
            rotateFile();
        }
    }

    private void rotateFile() throws IOException {
        close();
        messageCount = 0;
        initializeWriter();
        logger.info("Rotated file for topic: {}", topic);
    }

    private String createJsonMessage(String payload) {
        String message = String.format(
                "{\"timestamp\": \"%s\", \"topic\": \"%s\", \"message\": %s}",
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                topic,
                payload
        );

        if (firstMessage) {
            firstMessage = false;
            return message;
        } else {
            return "," + message;
        }
    }

    public void close() {
        if (writer != null) {
            try {
                writer.write("]"); // Close JSON array
                writer.newLine();
                writer.close();
                logger.info("Closed writer for topic: {}", topic);
            } catch (IOException e) {
                logger.error("Error closing writer for topic {}: {}", topic, e.getMessage(), e);
            }
            writer = null;
        }
    }

    private String getCurrentDate() {
        return LocalDate.now().format(dateFormatter);
    }
}
