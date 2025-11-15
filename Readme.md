Simple MQTT Dumper for logging mqtt json content to files 

# Features

- Spring Boot - Dependency injection and configuration management
- Environment Variables - External configuration support
- Eclipse Paho: MQTT client implementation
- Daily Subdirectories - Automatic directory creation by date
- Topic-based Files - Separate files for each MQTT topic
- File Rotation - Automatic rotation based on date and message count
- JSON Validation - Robust JSON parsing with Jackson
- Graceful Shutdown - Proper resource cleanup
- Connection Management - Automatic reconnection

# Run from the latest package

```bash
sudo docker run -d   \
  -e MQTT_BROKER=tcp://yourmqttbroker:1883 \
  -e MQTT_TOPIC=sensors/# 
  -e SPRING_PROFILES_ACTIVE=docker \ 
  --name mqtt-dumper \
  ghcr.io/a-hahn/mqtt-dumper:latest
```

# Run with default variables
java -jar build/libs/mqtt-dumper.jar

# Run local and override environment variables in application.yaml
MQTT_BROKER=tcp://localhost:1883 MQTT_TOPIC=sensors/# java -jar build/libs/mqtt-dumper.jar

# File structure
```text
mqtt-data/
├── 2024-01-15/
│   ├── sensors_temperature.json
│   ├── sensors_humidity.json
│   └── sensors_pressure.json
├── 2024-01-16/
│   ├── sensors_temperature.json
│   └── sensors_humidity.json
└── ...
```

# Local testing with docker

```bash
# Build and test locally
docker build -t mqtt-dumper .
sudo docker run -d   -e MQTT_BROKER=tcp://yourmqttbroker:1883   -e MQTT_TOPIC=sensors/# -e SPRING_PROFILES_ACTIVE=docker -v mqtt-data:/app/mqtt-data  --name mqtt-dumper mqtt-dumper
```


```
# Or use docker-compose
docker compose up
```

When using docker compose you might want to create a ``docker-compose.override.yml`` to override the default values


