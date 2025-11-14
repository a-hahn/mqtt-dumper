package org.ahahn.tools.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Validated
@Configuration
@ConfigurationProperties(prefix = "output")
public class FileOutputConfig {

    @NotBlank
    private String baseDir = "./mqtt-data";

    private String dateFormat = "yyyy-MM-dd";

    private Long maxMessagesPerFile = 10000L;

    private Boolean appendMode = true;

    // Getters and Setters
    public String getBaseDir() { return baseDir; }
    public void setBaseDir(String baseDir) { this.baseDir = baseDir; }

    public String getDateFormat() { return dateFormat; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }

    public Long getMaxMessagesPerFile() { return maxMessagesPerFile; }
    public void setMaxMessagesPerFile(Long maxMessagesPerFile) { this.maxMessagesPerFile = maxMessagesPerFile; }

    public Boolean getAppendMode() { return appendMode; }
    public void setAppendMode(Boolean appendMode) { this.appendMode = appendMode; }
}