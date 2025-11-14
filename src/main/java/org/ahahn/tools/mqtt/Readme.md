Simple MQTT Dumper

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

# Run with default variables
java -jar build/libs/mqtt-dumper.jar

# Or run with specific environment variables overriding the settings in application.yaml
MQTT_BROKER=tcp://localhost:1883 MQTT_TOPIC=sensors/# java -jar build/libs/mqtt-dumper.jar

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
docker run -it --rm \
  -e MQTT_BROKER=tcp://localhost:1883 \
  -e MQTT_TOPIC=sensors/# \
  -v $(pwd)/data:/app/data \
  mqtt-dumper

# Or use docker-compose
docker compose up
```

# Run with docker
```bash
docker run -it --rm   -e MQTT_BROKER=tcp://yourmqttbroker:1883   -e MQTT_TOPIC=sensors/#   -v $(pwd)/data:/app/data   mqtt-dumper
```
