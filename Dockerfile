# Fase 1: Compilazione con Maven e Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compila il progetto ignorando i test per velocizzare il deploy
RUN mvn clean package -DskipTests

# Fase 2: Creazione del server di produzione con Java 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copia il file .jar generato dalla fase 1
COPY --from=build /app/target/*.jar app.jar

# Esponi la porta 8080
EXPOSE 8080

# Comando di avvio di Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]