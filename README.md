# 🍽️ Yumaste Backend API

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-blue?logo=mysql)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/Auth-JWT-yellow?logo=jsonwebtokens)](https://jwt.io/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey)](LICENSE)

Backend RESTful per la piattaforma **Yumaste**, un servizio di food delivery / box alimentari con gestione completa di utenti, ordini, ingredienti, magazzino, spedizioni e sconti.

---

## 📋 Indice

- [Panoramica](#-panoramica)
- [Repository Collegate](#-repository-collegate)
- [Stack Tecnologico](#-stack-tecnologico)
- [Architettura del Progetto](#-architettura-del-progetto)
- [Modelli di Dominio](#-modelli-di-dominio)
- [Endpoint API](#-endpoint-api)
- [Sicurezza & Autenticazione](#-sicurezza--autenticazione)
- [Configurazione](#-configurazione)
- [Avvio del Progetto](#-avvio-del-progetto)
- [Documentazione API (Swagger)](#-documentazione-api-swagger)
- [Variabili d'Ambiente](#-variabili-dambiente)

---

## 📖 Panoramica

Yumaste Backend è una REST API sviluppata con **Spring Boot 4** che gestisce l'intero ciclo di vita di una piattaforma di food delivery basata su box alimentari personalizzate. Il sistema supporta:

- Registrazione e autenticazione utenti tramite JWT
- Gestione box con composizione di ingredienti
- Tracciamento ordini, spedizioni e fatture
- Gestione magazzino e fornitori
- Sistema di sconti su box e categorie
- Pannello amministrativo dedicato
- Documentazione interattiva via OpenAPI / Swagger UI

---

## 🔗 Repository Collegate

Yumaste è un progetto **multi-repo**. Di seguito le repository che compongono l'ecosistema:

| Repository | Descrizione |
|---|---|
| ⬅️ **Sei qui** — `yumaste-backend` | REST API Spring Boot |
| 🗄️ [yumaste-db](https://github.com/SantoFemiano/yumaste-db/tree/main) | Schema del database, DDL, DML e diagramma UML |
| 🗄️ [yumaste-admin](https://github.com/SantoFemiano/yumasteadminshop) | Front-end Admin in Angular |
| 🗄️ [yumaste-client](https://github.com/SantoFemiano/yumaste-shop) | Front-end Client in React  |
---

## 🛠️ Stack Tecnologico

| Tecnologia | Versione | Scopo |
|---|---|---|
| Java | 21 | Linguaggio principale |
| Spring Boot | 4.0.2 | Framework applicativo |
| Spring Web MVC | - | Layer REST |
| Spring Data JPA | - | ORM / persistenza dati |
| Spring Security | - | Autenticazione e autorizzazione |
| Spring Validation | - | Validazione input |
| Hibernate | - | Implementazione JPA |
| MySQL | 8.x | Database relazionale |
| JJWT | 0.11.5 | Generazione e verifica token JWT |
| MapStruct | 1.5.5 | Mapping Entity ↔ DTO |
| Lombok | latest | Riduzione boilerplate |
| SpringDoc OpenAPI | 2.8.5 | Documentazione Swagger UI |
| Maven | wrapper | Build tool |

---

## 🏗️ Architettura del Progetto

Il progetto segue un'architettura **a strati** (Layered Architecture) standard di Spring Boot:

```
src/
└── main/
    ├── java/com/yumaste/yumasteapi/
    │   ├── YumasteApiApplication.java      # Entry point
    │   ├── controllers/                    # Layer presentazione (REST)
    │   │   ├── AdminController.java        # Endpoint amministrativi
    │   │   ├── AuthController.java         # Registrazione e login
    │   │   ├── PublicController.java       # Endpoint pubblici
    │   │   └── UserController.java         # Operazioni utente autenticato
    │   ├── services/                       # Business logic
    │   ├── repositories/                   # Accesso dati (Spring Data JPA)
    │   ├── models/                         # Entità JPA
    │   ├── DTO/                            # Data Transfer Objects
    │   ├── mapper/                         # MapStruct mappers
    │   ├── exceptions/                     # Gestione eccezioni custom
    │   └── security/                       # Configurazione sicurezza JWT
    │       ├── ApplicationConfig.java
    │       ├── SecurityConfig.java
    │       ├── JwtService.java
    │       └── JwtAuthenticationFilter.java
    └── resources/
        └── application.properties          # Configurazione applicazione
```

---

## 🗃️ Modelli di Dominio

Il dominio è modellato con le seguenti entità JPA:

| Entità | Descrizione |
|---|---|
| `Utente` | Utente della piattaforma (implementa `UserDetails` per Spring Security) |
| `IndirizzoUtente` | Indirizzo di consegna associato all'utente |
| `Box` | Box alimentare ordinabile |
| `ComposizioneBox` | Associazione Box ↔ Ingredienti (con quantità) |
| `Ingrediente` | Ingrediente con valori nutrizionali e fornitore |
| `ValoriNutrizionali` | Macronutrienti dell'ingrediente |
| `Allergene` | Allergeni catalogati |
| `IngredienteAllergene` | Relazione N:N tra Ingrediente e Allergene |
| `Fornitore` | Fornitore degli ingredienti |
| `Magazzino` | Magazzino fisico |
| `IngredienteMagazzino` | Stock di un ingrediente in magazzino |
| `Carrello` | Carrello dell'utente |
| `Ordine` | Ordine effettuato dall'utente |
| `DettaglioOrdine` | Righe di dettaglio di un ordine |
| `Spedizione` | Spedizione collegata a un ordine |
| `Fattura` | Fattura generata all'ordine |
| `Sconto` | Codice sconto generale |
| `ScontoBox` | Sconto applicato su una specifica box |
| `ScontoCategoria` | Sconto applicato per categoria |

> 🗄️ Per lo schema completo del database (DDL, DML e diagramma UML) consulta la repository [yumaste-db](https://github.com/SantoFemiano/yumaste-db/tree/main).

---

## 🔌 Endpoint API

### 🔓 Auth — `/api/auth`
Endpoints pubblici per autenticazione:

| Metodo | Path | Descrizione |
|---|---|---|
| `POST` | `/api/auth/register` | Registrazione nuovo utente |
| `POST` | `/api/auth/login` | Login e ottenimento token JWT |

### 🌐 Public — `/api/public`
Endpoints accessibili senza autenticazione:

| Metodo | Path | Descrizione |
|---|---|---|
| `GET` | `/api/public/...` | Visualizzazione box, ingredienti, allergeni |

### 👤 User — `/api/user`
Endpoints per utenti autenticati (ruolo `USER`):

| Metodo | Path | Descrizione |
|---|---|---|
| `GET` | `/api/user/...` | Profilo, ordini, carrello |
| `POST` | `/api/user/...` | Creazione ordini, gestione carrello |
| `PUT` | `/api/user/...` | Aggiornamento dati profilo |

### 🔐 Admin — `/api/admin`
Endpoints riservati agli amministratori (ruolo `ADMIN`):

| Metodo | Path | Descrizione |
|---|---|---|
| `GET/POST/PUT/DELETE` | `/api/admin/...` | CRUD su box, ingredienti, fornitori, magazzino, sconti, utenti |

> 📖 Per la lista completa e dettagliata degli endpoint consultare la [Swagger UI](#-documentazione-api-swagger) a runtime.

---

## 🔐 Sicurezza & Autenticazione

Il sistema utilizza **JWT (JSON Web Token)** stateless:

1. L'utente si registra o effettua il login tramite `/api/auth`
2. Il server genera un token JWT firmato con una chiave segreta
3. Il client include il token nell'header `Authorization: Bearer <token>` per ogni richiesta protetta
4. `JwtAuthenticationFilter` intercetta ogni request, valida il token e imposta il contesto di sicurezza

```
Client                          Server
  |                               |
  |-- POST /api/auth/login ------->|
  |<-- { "token": "eyJ..." } ------|
  |                               |
  |-- GET /api/user/profile ------>|
  |   Authorization: Bearer eyJ...|
  |<-- 200 OK { userdata } --------|
```

### Ruoli
- **`USER`** — Utente standard, può fare ordini e gestire il proprio profilo
- **`ADMIN`** — Amministratore con accesso completo al pannello di gestione

---

## ⚙️ Configurazione

Il file `src/main/resources/application.properties` utilizza variabili d'ambiente per tutte le configurazioni sensibili:

```properties
# Database MySQL
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

# Server
server.port=8084

# JWT
application.security.jwt.secret-key=${JWT_SECRET_KEY}
application.security.jwt.expiration=${JWT_EXPIRATION}
```

> ⚠️ **Non committare mai credenziali reali nel repository.** Usa variabili d'ambiente o un file `.env` locale (già incluso in `.gitignore`).

---

## 🚀 Avvio del Progetto

### Prerequisiti

- Java 21+
- MySQL 8.x
- Maven (o usare il wrapper incluso `./mvnw`)

### 1. Clona il repository

```bash
git clone https://github.com/SantoFemiano/yumaste-backend.git
cd yumaste-backend
```

### 2. Crea e inizializza il database MySQL

Clona ed esegui i file SQL dalla repository [yumaste-db](https://github.com/SantoFemiano/yumaste-db/tree/main):

```bash
git clone https://github.com/SantoFemiano/yumaste-db.git
mysql -u <utente> -p -e "CREATE DATABASE yumaste;"
mysql -u <utente> -p yumaste < yumaste-db/DDL.sql
# Opzionale: dati di esempio
mysql -u <utente> -p yumaste < yumaste-db/DML.sql
```

### 3. Configura le variabili d'ambiente

Crea un file `.env` nella root del progetto oppure esporta le variabili nel tuo terminale:

```bash
export DB_URL=jdbc:mysql://localhost:3306/yumaste
export DB_USERNAME=root
export DB_PASSWORD=tuapassword
export JWT_SECRET_KEY=una-chiave-segreta-molto-lunga-almeno-256-bit
export JWT_EXPIRATION=86400000
```

### 4. Avvia l'applicazione

```bash
./mvnw spring-boot:run
```

Oppure compila e avvia il JAR:

```bash
./mvnw clean package
java -jar target/yumaste-api-0.0.1-SNAPSHOT.jar
```

L'API sarà disponibile su: `http://localhost:8084`

---

## 📚 Documentazione API (Swagger)

Una volta avviata l'applicazione, la documentazione interattiva OpenAPI è disponibile a:

```
http://localhost:8084/swagger-ui/index.html
```

Da qui è possibile esplorare e testare tutti gli endpoint direttamente dal browser.

---

## 🌍 Variabili d'Ambiente

| Variabile | Descrizione | Esempio |
|---|---|---|
| `DB_URL` | URL JDBC del database MySQL | `jdbc:mysql://localhost:3306/yumaste` |
| `DB_USERNAME` | Username del database | `root` |
| `DB_PASSWORD` | Password del database | `secret` |
| `JWT_SECRET_KEY` | Chiave segreta per firmare i JWT (min. 256 bit) | `mySecretKey...` |
| `JWT_EXPIRATION` | Durata del token in millisecondi | `86400000` (24h) |

---

## 👤 Autori

**Santo Femiano**
- GitHub: [@SantoFemiano](https://github.com/SantoFemiano)
  
**Salvatore Santaniello**
- GitHub: [@salvsant](https://github.com/salvsant)
---
