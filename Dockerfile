# Usiamo solo l'immagine leggera per eseguire Java (niente Maven!)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamo il truststore direttamente dal codice sorgente
COPY src/main/resources/client.truststore.jks /app/client.truststore.jks

# Copiamo il file JAR che GitHub Actions ha GIA' compilato per noi!
# Nota: rimosso "--from=build" e corretto il percorso in "target/*.jar"
COPY target/*.jar app.jar

EXPOSE 8084

# Comando di avvio di Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]