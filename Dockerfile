# Build stage
FROM gradle:8.6-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean bootJar -x test --no-daemon

# Runtime stage - switched to non-Alpine for better multi-arch support
FROM eclipse-temurin:17-jre

# Install timezone data using apt (instead of apk)
RUN apt-get update && \
    apt-get install -y --no-install-recommends tzdata && \
    ln -fs /usr/share/zoneinfo/Europe/Berlin /etc/localtime && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user (different syntax for Debian/Ubuntu)
RUN groupadd -r app && useradd -r -g app app

WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

# Create data directory with proper permissions
RUN mkdir -p /app/mqtt-data && chown -R app:app /app

# Health check (optional - if you add Spring Boot Actuator)
# HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
#   CMD curl -f http://localhost:8080/actuator/health || exit 1

USER app

# Use shell form to allow environment variable expansion
ENTRYPOINT ["java", "-jar", "app.jar"]