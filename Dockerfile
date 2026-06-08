# Stage 1: Estrazione dei layer del JAR compilato
FROM eclipse-temurin:21-jre-alpine AS extractor
WORKDIR /app
COPY target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 2: Costruzione dell'immagine finale minimale
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamo il truststore direttamente dal codice sorgente
COPY src/main/resources/client.truststore.jks /app/client.truststore.jks

# Copiamo i singoli layer estratti dallo stage precedente
# Docker metterà in cache le dipendenze; se cambia solo il tuo codice, scaricherà solo l'application layer!
COPY --from=extractor /app/dependencies/ ./
COPY --from=extractor /app/spring-boot-loader/ ./
COPY --from=extractor /app/snapshot-dependencies/ ./
COPY --from=extractor /app/application/ ./

EXPOSE 8084

# Comando di avvio ottimizzato per l'esecuzione tramite JarLauncher
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]