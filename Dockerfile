# Dockerfile actualizado
FROM maven:3.9.6-eclipse-temurin-22 AS build

WORKDIR /app

# Copy root POM
COPY pom.xml .

# Copy checkstyle if exists
COPY checkstyle.xml ./checkstyle.xml

# Copy module POMs
COPY common-model/pom.xml common-model/
COPY services/pom.xml services/

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY common-model/ common-model/
COPY services/ services/

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:22-jre-jammy

WORKDIR /app

# Copy the built artifact
COPY --from=build /app/services/target/*.jar app.jar

# Expose port
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]