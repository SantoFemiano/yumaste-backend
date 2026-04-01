# Fase 1: Compilazione con Maven
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compila il progetto ignorando i test per velocizzare il deploy
RUN mvn clean package -DskipTests

# Fase 2: Creazione del server di produzione
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
# Copia il file .jar generato dalla fase 1
COPY --from=build /app/target/*.jar app.jar

# Esponi la porta (su Render solitamente si usa la 8080)
EXPOSE 8080

# Comando di avvio di Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]