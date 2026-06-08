# ─── Stage 1: Build ───────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copia solo pom.xml prima per cachare le dipendenze Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Scarica tutte le dipendenze (layer che cambia raramente)
RUN ./mvnw dependency:go-offline -B

# Ora copia il sorgente e builda
COPY src ./src
RUN ./mvnw package -DskipTests

# Estrae il JAR in layer separati
RUN java -Djarmode=layertools -jar target/*.jar extract

# ─── Stage 2: Runtime ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Ogni COPY è un layer Docker separato:
# - dependencies: librerie esterne (cambia raramente)
# - spring-boot-loader: il loader di Spring Boot (cambia raramente)
# - snapshot-dependencies: dipendenze SNAPSHOT (cambia a volte)
# - application: solo il tuo codice (cambia spesso)
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]