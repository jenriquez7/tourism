# Stage 1: Build both modules
FROM maven:3.9.6-eclipse-temurin-22 AS build

WORKDIR /app

# Copy only the POM files first to cache dependencies
COPY pom.xml .
COPY checkstyle.xml .
COPY common-model/pom.xml common-model/
COPY services/pom.xml services/

# Download dependencies and cache them
RUN mvn dependency:go-offline -B

# Copy source files
COPY common-model/src common-model/src/
COPY services/src services/src/

# Run checkstyle and build
RUN mvn checkstyle:check
RUN mvn clean package -DskipTests

# Stage 2: Run services
FROM eclipse-temurin:22-jre-jammy

WORKDIR /app

# Copy the jar from build stage
COPY --from=build /app/services/target/*.jar app.jar

# Add health check
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose the application port
EXPOSE 8080

# Set Java options for container environment
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]