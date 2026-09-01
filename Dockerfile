# ==========================================
# Stage 1: Build Application with Maven
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy POM and download dependencies for faster cached builds
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# Stage 2: Minimal Production JRE Image
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add non-root user for enhanced container security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser:appgroup

# Copy built JAR from builder stage
COPY --from=builder /app/target/product-service-1.0.0.jar app.jar

# Expose service port
EXPOSE 8080

# Configure JVM flags optimized for container environments
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
