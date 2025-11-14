# Build stage
FROM gradle:8.6-jdk17-alpine AS builder
WORKDIR /app
COPY . .
RUN gradle clean bootJar -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache tzdata && \
    addgroup -S app && adduser -S app -G app

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Create data directory with proper permissions
RUN mkdir -p /app/data && chown -R app:app /app

# Health check (optional - if you add Spring Boot Actuator)
# HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
#   CMD curl -f http://localhost:8080/actuator/health || exit 1

USER app

# Use shell form to allow environment variable expansion
ENTRYPOINT ["java", "-jar", "app.jar"]

