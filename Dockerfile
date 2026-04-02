# Fase 2: Creazione del server di produzione con Java 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copia il file .jar generato dalla fase 1
COPY --from=build /app/target/*.jar app.jar

# Render imposterà la variabile PORT, ma possiamo esporre la 8084 di default
EXPOSE 8084

# Comando di avvio di Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]