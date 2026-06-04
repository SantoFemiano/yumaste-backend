FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY src/main/resources/client.truststore.jks /app/client.truststore.jks
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8084

# Comando di avvio di Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]