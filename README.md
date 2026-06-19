# 🍽️ Yumaste — Backend API

<div align="center">

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-blue?logo=mysql)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/Auth-JWT-yellow?logo=jsonwebtokens)](https://jwt.io/)
[![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker)](https://hub.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey)](LICENSE)

[![CI/CD Pipeline](https://github.com/SantoFemiano/yumaste-backend/actions/workflows/deploy.yml/badge.svg)](https://github.com/SantoFemiano/yumaste-backend/actions/workflows/deploy.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=SantoFemiano_yumaste-backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=SantoFemiano_yumaste-backend) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=SantoFemiano_yumaste-backend&metric=coverage)](https://sonarcloud.io/summary/new_code?id=SantoFemiano_yumaste-backend) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=SantoFemiano_yumaste-backend&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=SantoFemiano_yumaste-backend)

**Production-ready REST API for a full-stack food delivery platform.**  
Built with Spring Boot 4 · Java 21 · MySQL · Docker · GitHub Actions · SonarCloud · AI Integration

[🌐 Live Demo](https://yumaste-shop.vercel.app/) · [📖 Swagger UI](#-api-documentation-swagger) · [🗄️ Database Schema](https://github.com/SantoFemiano/yumaste-db) · [🖥️ Frontend Client](https://github.com/SantoFemiano/yumaste-shop) · [⚙️ Admin Panel](https://github.com/SantoFemiano/yumasteadminshop)

</div>

---

## 📌 Project Overview

**Yumaste** is a full-stack e-commerce platform for **personalized food delivery boxes** (meal-kit style). This repository contains the backend REST API, which manages the complete lifecycle of the platform: user authentication, product catalog, orders, warehouse, shipping, invoicing, discounts, and AI-powered assistant features.

The project showcases a production-oriented architecture with automated CI/CD, code quality monitoring, containerization, and AI integration — all running in a cloud environment.

### Key Highlights for Recruiters

- ✅ **Full production deployment** on Oracle Cloud Infrastructure (OCI) via Docker
- ✅ **CI/CD pipeline** with GitHub Actions: build → test → SonarCloud analysis → Docker Hub push
- ✅ **AI Integration** using Google Gemini SDK and LangChain4j for intelligent features
- ✅ **Stateless JWT authentication** + OAuth2 client support
- ✅ **Redis caching** and **Apache Kafka** event-driven messaging
- ✅ **Observability** with Spring Actuator + Micrometer + Prometheus metrics
- ✅ **PDF generation** for invoices (OpenPDF)
- ✅ **Email notifications** via Spring Mail
- ✅ **Code quality** enforced by SonarCloud with JaCoCo test coverage reports
- ✅ **Multi-repo architecture**: backend, frontend (React), admin panel (Angular), database

---

## 🗂️ Ecosystem — Multi-Repo Architecture

Yumaste is organized across **4 repositories** that together form the complete platform:

| Repository | Tech Stack | Description |
|---|---|---|
| ⬅️ **`yumaste-backend`** *(you are here)* | Spring Boot 4, Java 21, MySQL | REST API — core of the platform |
| 🖥️ [`yumaste-shop`](https://github.com/SantoFemiano/yumaste-shop) | React, TypeScript, Tailwind CSS, Vite | Customer-facing storefront — deployed on Vercel |
| ⚙️ [`yumasteadminshop`](https://github.com/SantoFemiano/yumasteadminshop) | Angular | Admin panel for platform management |
| 🗄️ [`yumaste-db`](https://github.com/SantoFemiano/yumaste-db) | MySQL, SQL | Database schema (DDL/DML), UML ER diagram |

---

## 🛠️ Tech Stack

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| **Language** | Java | 21 | Core language (LTS) |
| **Framework** | Spring Boot | 4.0.2 | Application framework |
| **Web** | Spring Web MVC | — | REST layer |
| **Persistence** | Spring Data JPA + Hibernate | — | ORM & database access |
| **Security** | Spring Security + JJWT | 0.11.5 | JWT auth & authorization |
| **OAuth2** | Spring OAuth2 Client | — | Social login support |
| **Database** | MySQL | 8.x | Relational data store |
| **Caching** | Spring Data Redis | — | Cache layer (Aiven Redis) |
| **Messaging** | Apache Kafka | — | Event-driven architecture |
| **AI** | Google Gemini SDK + LangChain4j | 1.53.0 / 0.36.2 | AI assistant integration |
| **Mapping** | MapStruct | 1.5.5 | Entity ↔ DTO mapping |
| **Validation** | Spring Validation | — | Input validation |
| **Documentation** | SpringDoc OpenAPI | 2.8.5 | Swagger UI |
| **PDF** | OpenPDF | 2.0.4 | Invoice generation |
| **Email** | Spring Mail | — | Transactional email |
| **Observability** | Actuator + Micrometer + Prometheus | — | Metrics & health checks |
| **Build** | Maven Wrapper | — | Build & dependency management |
| **Containerization** | Docker (multi-stage build) | — | Production container image |
| **CI/CD** | GitHub Actions | — | Automated pipeline |
| **Code Quality** | SonarCloud + JaCoCo | — | Static analysis & coverage |

---

## 🏗️ Architecture

The API follows a classic **Layered Architecture** (Controller → Service → Repository) with clear separation of concerns:

```
src/main/java/com/yumaste/yumasteapi/
│
├── YumasteApiApplication.java          # Entry point
│
├── controllers/                        # REST layer — 4 controllers
│   ├── AuthController.java             # /api/auth — register, login
│   ├── PublicController.java           # /api/public — public endpoints
│   ├── UserController.java             # /api/user — authenticated user ops
│   └── AdminController.java            # /api/admin — admin CRUD operations
│
├── services/                           # Business logic layer
├── repositories/                       # Spring Data JPA repositories
├── models/                             # JPA entities (18 domain entities)
├── DTO/                                # Data Transfer Objects
├── mapper/                             # MapStruct entity ↔ DTO mappers
├── exceptions/                         # Custom exception handling
└── security/                           # JWT security config
    ├── SecurityConfig.java
    ├── JwtService.java
    └── JwtAuthenticationFilter.java
```

### CI/CD Flow

```
git push → main
     │
     ▼
┌─────────────────────────────────────────────────────┐
│                 GitHub Actions                        │
│                                                       │
│  1. Set up JDK 21 (Eclipse Temurin)                  │
│  2. mvn clean verify (build + JaCoCo coverage)       │
│  3. SonarCloud analysis (quality gate + coverage)    │
│  4. docker buildx build (multi-stage, linux/amd64)   │
│  5. docker push → Docker Hub                         │
└─────────────────────────────────────────────────────┘
     │
     ▼
  Docker Hub → Oracle Cloud (OCI) deployment
```

---

## 🗃️ Domain Model

18 JPA entities covering the full business domain:

| Entity | Description |
|---|---|
| `Utente` | Platform user (implements `UserDetails`) |
| `IndirizzoUtente` | Delivery address |
| `Box` | Orderable food box product |
| `ComposizioneBox` | Box ↔ Ingredient composition (with quantities) |
| `Ingrediente` | Ingredient with nutritional data and supplier |
| `ValoriNutrizionali` | Macronutrients per ingredient |
| `Allergene` | Allergen catalog |
| `IngredienteAllergene` | N:N Ingredient ↔ Allergen |
| `Fornitore` | Ingredient supplier |
| `Magazzino` | Physical warehouse |
| `IngredienteMagazzino` | Ingredient stock in warehouse |
| `Carrello` | User shopping cart |
| `Ordine` | Customer order |
| `DettaglioOrdine` | Order line items |
| `Spedizione` | Shipping record |
| `Fattura` | Generated invoice (PDF) |
| `Sconto` | General discount code |
| `ScontoBox` / `ScontoCategoria` | Box-level / category-level discounts |

> 📊 Full ER diagram and DDL/DML scripts available in [yumaste-db](https://github.com/SantoFemiano/yumaste-db).

---

## 🔌 API Endpoints

### 🔓 Auth — `/api/auth`

| Method | Path | Description | Auth |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register new user | ❌ Public |
| `POST` | `/api/auth/login` | Login & receive JWT token | ❌ Public |

### 🌐 Public — `/api/public`

| Method | Path | Description | Auth |
|---|---|---|---|
| `GET` | `/api/public/boxes` | Browse food box catalog | ❌ Public |
| `GET` | `/api/public/ingredients` | List ingredients | ❌ Public |
| `GET` | `/api/public/allergens` | List allergens | ❌ Public |

### 👤 User — `/api/user` *(requires `USER` role)*

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/user/profile` | Get user profile |
| `PUT` | `/api/user/profile` | Update profile |
| `GET` | `/api/user/orders` | List own orders |
| `POST` | `/api/user/orders` | Place new order |
| `GET/POST/DELETE` | `/api/user/cart` | Manage cart |

### 🔐 Admin — `/api/admin` *(requires `ADMIN` role)*

Full CRUD on: boxes, ingredients, suppliers, warehouse, discounts, users, orders, shipments, invoices.

> 📖 Full interactive documentation available at runtime via [Swagger UI](#-api-documentation-swagger).

---

## 🔐 Security & Authentication

JWT-based stateless security:

```
Client                              Server
  │                                    │
  ├── POST /api/auth/login ──────────▶ │
  │◀── { "token": "eyJ..." } ──────── │
  │                                    │
  ├── GET /api/user/profile ─────────▶ │
  │   Authorization: Bearer eyJ...     │
  │◀── 200 OK { userProfile } ─────── │
```

| Role | Access |
|---|---|
| `USER` | Own profile, cart, order placement, order history |
| `ADMIN` | Full platform management (boxes, inventory, users, reports) |

---

## 🐳 Docker

The application uses an optimized **multi-stage Docker build** leveraging Spring Boot layer extraction to maximize cache efficiency:

- **Stage 1 (builder):** `eclipse-temurin:21-jdk-alpine` — compiles the app and extracts layered JAR
- **Stage 2 (runtime):** `eclipse-temurin:21-jre-alpine` — minimal JRE image with layered dependencies

```bash
# Pull from Docker Hub
docker pull santofemiano/yumaste-backend:latest

# Run locally (requires external MySQL)
docker run -p 8084:8084 \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/yumaste \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=secret \
  -e JWT_SECRET_KEY=your-256bit-secret \
  -e JWT_EXPIRATION=86400000 \
  santofemiano/yumaste-backend:latest
```

---

## 🚀 Local Setup

### Prerequisites

- Java 21+
- MySQL 8.x
- Maven (or use the included `./mvnw` wrapper)

### 1. Clone the repository

```bash
git clone https://github.com/SantoFemiano/yumaste-backend.git
cd yumaste-backend
```

### 2. Initialize the database

```bash
git clone https://github.com/SantoFemiano/yumaste-db.git
mysql -u <user> -p -e "CREATE DATABASE yumaste;"
mysql -u <user> -p yumaste < yumaste-db/DDL.sql
# Optional: seed data
mysql -u <user> -p yumaste < yumaste-db/DML.sql
```

### 3. Configure environment variables

```bash
export DB_URL=jdbc:mysql://localhost:3306/yumaste
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
export JWT_SECRET_KEY=a-very-long-secret-key-at-least-256-bits
export JWT_EXPIRATION=86400000
export GEMINI_API_KEY=your-gemini-api-key
export MAIL_USERNAME=your@email.com
export MAIL_PASSWORD=yourpassword
```

### 4. Run

```bash
./mvnw spring-boot:run
```

API available at: `http://localhost:8084`

---

## 📚 API Documentation (Swagger)

Once the app is running, the interactive OpenAPI documentation is available at:

```
http://localhost:8084/swagger-ui/index.html
```

### Screenshots

#### 🔓 Auth & Public Controller
![Auth e Public Controller](Screenshot%202026-03-31%20alle%2018.17.04.png)

#### 👤 User Controller
![User Controller](Screenshot%202026-03-31%20alle%2018.16.11.png)

#### 🔐 Admin Controller (1/2)
![Admin Controller - parte 1](Screenshot%202026-03-31%20alle%2018.16.43.png)

#### 🔐 Admin Controller (2/2)
![Admin Controller - parte 2](Screenshot%202026-03-31%20alle%2018.16.58.png)

---

## 🌍 Environment Variables

| Variable | Description | Example |
|---|---|---|
| `DB_URL` | JDBC connection URL | `jdbc:mysql://localhost:3306/yumaste` |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | `secret` |
| `JWT_SECRET_KEY` | JWT signing key (min 256-bit) | `myVeryLongSecretKey...` |
| `JWT_EXPIRATION` | Token TTL in milliseconds | `86400000` (24h) |
| `GEMINI_API_KEY` | Google Gemini API key | `AIza...` |
| `MAIL_USERNAME` | SMTP email address | `noreply@yumaste.com` |
| `MAIL_PASSWORD` | SMTP password | `smtp_secret` |

> ⚠️ Never commit real credentials. Use environment variables or a `.env` file (already in `.gitignore`).

---

## 🧪 Testing & Quality

```bash
# Run tests + generate JaCoCo coverage report
./mvnw clean verify

# Coverage report available at:
# target/site/jacoco/index.html
```

Code quality is monitored continuously via **SonarCloud** on every push to `main`, checking for:
- Bugs, vulnerabilities, code smells
- Test coverage (JaCoCo XML report)
- Security hotspots

---

## 👤 Authors

**Santo Femiano**
- GitHub: [@SantoFemiano](https://github.com/SantoFemiano)

**Salvatore Santaniello**
- GitHub: [@salvsant](https://github.com/salvsant)
