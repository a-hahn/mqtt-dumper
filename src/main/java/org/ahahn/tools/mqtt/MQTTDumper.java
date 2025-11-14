package org.ahahn.tools.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class MQTTDumper {
    private Properties config;
    private BufferedWriter writer;
    private MqttClient client;
    private boolean firstMessage = true;

    public static void main(String[] args) {
        MQTTDumper dumper = new MQTTDumper();
        dumper.loadConfig("config.properties");
        dumper.start();
    }

    public void loadConfig(String configFile) {
        config = new Properties();
        try (InputStream input = new FileInputStream(configFile)) {
            config.load(input);
        } catch (IOException e) {
            // Use defaults if config file not found
            config.setProperty("mqtt.broker", "tcp://forsy.net:1883");
            config.setProperty("mqtt.clientId", "JsonDumperClient");
            config.setProperty("mqtt.topic", "owntracks/#");
            config.setProperty("mqtt.qos", "1");
            config.setProperty("output.file", "mqtt_messages.json");
            config.setProperty("mqtt.username", "");
            config.setProperty("mqtt.password", "");
            System.out.println("Using default configuration");
        }
    }

    public void start() {
        try {
            String outputFile = config.getProperty("output.file", "mqtt_messages.json");
            writer = new BufferedWriter(new FileWriter(outputFile, true));

            writeToFile("// MQTT Message Dump Started at: " + getCurrentTimestamp());
            writeToFile("["); // Start JSON array

            initializeMqttClient();

            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        } catch (MqttException | IOException e) {
            e.printStackTrace();
            shutdown();
        }
    }

    private void initializeMqttClient() throws MqttException {
        String broker = config.getProperty("mqtt.broker");
        String clientId = config.getProperty("mqtt.clientId");

        MemoryPersistence persistence = new MemoryPersistence();
        client = new MqttClient(broker, clientId, persistence);

        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setConnectionTimeout(60);
        connOpts.setKeepAliveInterval(60);

        // Set credentials if provided
        String username = config.getProperty("mqtt.username", "");
        String password = config.getProperty("mqtt.password", "");
        if (!username.isEmpty()) {
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connection lost: " + cause.getMessage());
                try {
                    Thread.sleep(5000);
                    reconnect();
                } catch (Exception e) {
                    System.err.println("Reconnection failed: " + e.getMessage());
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                processMessage(topic, message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Not needed for subscription
            }
        });

        System.out.println("Connecting to broker: " + broker);
        client.connect(connOpts);

        String topic = config.getProperty("mqtt.topic");
        int qos = Integer.parseInt(config.getProperty("mqtt.qos", "1"));

        System.out.println("Subscribing to topic: " + topic + " with QoS: " + qos);
        client.subscribe(topic, qos);

        System.out.println("MQTT client started successfully");
    }

    private void processMessage(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());

            if (isValidJson(payload)) {
                // Handle comma separation for JSON array
                String jsonMessage;
                if (firstMessage) {
                    jsonMessage = String.format(
                            "{\"timestamp\": \"%s\", \"topic\": \"%s\", \"qos\": %d, \"message\": %s}",
                            getCurrentTimestamp(), topic, message.getQos(), payload
                    );
                    firstMessage = false;
                } else {
                    jsonMessage = String.format(
                            ",{\"timestamp\": \"%s\", \"topic\": \"%s\", \"qos\": %d, \"message\": %s}",
                            getCurrentTimestamp(), topic, message.getQos(), payload
                    );
                }

                writeToFile(jsonMessage);
                System.out.println("Message processed and saved - Topic: " + topic);
            } else {
                System.out.println("Invalid JSON received, skipping. Payload: " + payload);
            }

        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    private void writeToFile(String content) {
        try {
            writer.write(content);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    private boolean isValidJson(String json) {
        try {
            String trimmed = json.trim();
            return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                    (trimmed.startsWith("[") && trimmed.endsWith("]"));
        } catch (Exception e) {
            return false;
        }
    }

    private void reconnect() {
        try {
            if (client != null && !client.isConnected()) {
                client.reconnect();
                String topic = config.getProperty("mqtt.topic");
                int qos = Integer.parseInt(config.getProperty("mqtt.qos", "1"));
                client.subscribe(topic, qos);
                System.out.println("Reconnected and resubscribed");
            }
        } catch (MqttException e) {
            System.err.println("Reconnection failed: " + e.getMessage());
        }
    }

    private void shutdown() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                System.out.println("Disconnected from MQTT broker");
            }

            if (writer != null) {
                writeToFile("]"); // Close JSON array
                writeToFile("// MQTT Message Dump Ended at: " + getCurrentTimestamp());
                writer.close();
                System.out.println("Output file closed");
            }
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}