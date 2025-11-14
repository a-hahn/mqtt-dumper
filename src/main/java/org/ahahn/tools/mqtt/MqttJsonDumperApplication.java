package org.ahahn.tools.mqtt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MqttJsonDumperApplication {

    public static void main(String[] args) {
        SpringApplication.run(MqttJsonDumperApplication.class, args);
    }
}