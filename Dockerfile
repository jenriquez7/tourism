# Stage 1: Build both modules
FROM maven:3.9.6-eclipse-temurin-22 AS build
WORKDIR /app
COPY pom.xml .
COPY checkstyle.xml .
COPY common-model/pom.xml common-model/
COPY services/pom.xml services/
RUN mvn dependency:go-offline -B

COPY common-model/src common-model/src
COPY services/src services/src

RUN mvn checkstyle:check

RUN mvn package -DskipTests

# Stage 2: Run services
FROM eclipse-temurin:22-jre-jammy
WORKDIR /app
COPY --from=build /app/services/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]