# Stage 1: Build both modules
FROM maven:3.9.6-eclipse-temurin-22 AS build

WORKDIR /app

# Copy root POM
COPY ./pom.xml .

# Copy checkstyle if exists (make this optional)
COPY ./checkstyle.xml ./checkstyle.xml

# Copy module POMs first for better caching
COPY ./common-model/pom.xml ./common-model/
COPY ./services/pom.xml ./services/

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY ./common-model/src ./common-model/src/
COPY ./services/src ./services/src/

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:22-jre-jammy

WORKDIR /app

# Copy the built artifact
COPY --from=build /app/services/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Set Java options and start the application
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]