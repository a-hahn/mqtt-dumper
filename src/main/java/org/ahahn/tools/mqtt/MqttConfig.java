package org.ahahn.tools.mqtt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties(prefix = "mqtt")
public class MqttConfig {

    @NotBlank
    private String broker = "tcp://forsy.net:1883";

    private String clientId = "spring-mqtt-dumper";

    @NotBlank
    private String topic = "owntracks/#";

    @NotNull
    private Integer qos = 1;

    private String username;
    private String password;
    private Integer connectionTimeout = 30;
    private Integer keepAliveInterval = 60;
    private Boolean cleanSession = true;

    // Getters and Setters
    public String getBroker() { return broker; }
    public void setBroker(String broker) { this.broker = broker; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public Integer getQos() { return qos; }
    public void setQos(Integer qos) { this.qos = qos; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(Integer connectionTimeout) { this.connectionTimeout = connectionTimeout; }

    public Integer getKeepAliveInterval() { return keepAliveInterval; }
    public void setKeepAliveInterval(Integer keepAliveInterval) { this.keepAliveInterval = keepAliveInterval; }

    public Boolean getCleanSession() { return cleanSession; }
    public void setCleanSession(Boolean cleanSession) { this.cleanSession = cleanSession; }

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{broker});
        options.setCleanSession(cleanSession);
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);

        if (username != null && !username.trim().isEmpty()) {
            options.setUserName(username);
            if (password != null && !password.trim().isEmpty()) {
                options.setPassword(password.toCharArray());
            }
        }

        // Set last will testament
        String willPayload = String.format(
                "{\"clientId\": \"%s\", \"status\": \"disconnected\", \"type\": \"last_will\"}",
                clientId
        );
        options.setWill("client/status", willPayload.getBytes(), 1, true);

        return options;
    }
}