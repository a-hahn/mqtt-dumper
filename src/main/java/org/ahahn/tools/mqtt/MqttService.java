package org.ahahn.tools.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class MqttService implements MqttCallback {
    private static final Logger logger = LoggerFactory.getLogger(MqttService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final MqttConfig mqttConfig;
    private final FileWriterService fileWriterService;
    private MqttClient client;
    private long totalMessageCount = 0;

    public MqttService(MqttConfig mqttConfig, FileWriterService fileWriterService) {
        this.mqttConfig = mqttConfig;
        this.fileWriterService = fileWriterService;
        logger.info("MqttService created with config: broker={}, topic={}",
                mqttConfig.getBroker(), mqttConfig.getTopic());
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing MQTT connection...");
        try {
            // Use config values directly
            String clientId = mqttConfig.getClientId() + "-" + System.currentTimeMillis();
            client = new MqttClient(mqttConfig.getBroker(), clientId, new MemoryPersistence());
            client.setCallback(this);

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(mqttConfig.getCleanSession());
            connOpts.setConnectionTimeout(mqttConfig.getConnectionTimeout());
            connOpts.setKeepAliveInterval(mqttConfig.getKeepAliveInterval());

            // Set credentials from config
            if (mqttConfig.getUsername() != null && !mqttConfig.getUsername().isEmpty()) {
                connOpts.setUserName(mqttConfig.getUsername());
                if (mqttConfig.getPassword() != null && !mqttConfig.getPassword().isEmpty()) {
                    connOpts.setPassword(mqttConfig.getPassword().toCharArray());
                }
            }

            logger.info("Connecting to MQTT broker: {}", mqttConfig.getBroker());
            client.connect(connOpts);

            logger.info("Subscribing to topic: {} with QoS: {}", mqttConfig.getTopic(), mqttConfig.getQos());
            client.subscribe(mqttConfig.getTopic(), mqttConfig.getQos());

            logger.info("MQTT client successfully connected and subscribed");

        } catch (MqttException e) {
            logger.error("Failed to initialize MQTT client: {}", e.getMessage(), e);
            throw new RuntimeException("MQTT initialization failed", e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        logger.warn("MQTT connection lost: {}", cause.getMessage());
        // Auto-reconnect is handled by Paho client when cleanSession=false
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        try {
            String payload = new String(mqttMessage.getPayload());
            totalMessageCount++;

            logger.debug("Message #{} from topic: {} ({} bytes)",
                    totalMessageCount, topic, payload.length());

            if (isValidJson(payload)) {
                fileWriterService.writeMessage(topic, payload);
            } else {
                logger.warn("Invalid JSON from topic {}, skipping", topic);
            }

        } catch (Exception e) {
            logger.error("Error processing message from topic {}: {}", topic, e.getMessage());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Not used for subscription-only client
    }

    private boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down MQTT service...");
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                logger.info("Disconnected from MQTT broker");
            }
        } catch (MqttException e) {
            logger.error("Error during MQTT disconnect: {}", e.getMessage());
        }
        logger.info("MQTT service shutdown completed. Total messages: {}", totalMessageCount);
    }
}