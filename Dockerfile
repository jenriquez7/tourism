# Stage 1: Build common-model
FROM maven:3.9.6-eclipse-temurin-22 AS common-model-build
WORKDIR /app/common-model
COPY common-model/pom.xml .
COPY common-model/src ./src
RUN mvn clean install

# Stage 2: Build services
FROM maven:3.9.6-eclipse-temurin-22 AS services-build
WORKDIR /app/services
COPY services/pom.xml .
COPY --from=common-model-build /root/.m2 /root/.m2
RUN mvn dependency:go-offline
COPY services/src ./src
RUN mvn package -DskipTests

# Stage 3: Run services
FROM eclipse-temurin:22-jre-jammy
WORKDIR /app
COPY --from=services-build /app/services/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
