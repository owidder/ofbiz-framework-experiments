# Party Service PoC - Monorepo Setup Guide

## Übersicht

Dieser Guide beschreibt, wie der Party Service als Proof-of-Concept im selben OFBiz-Repository entwickelt wird, aber klar getrennt und unabhängig startbar ist.

**Aktualisiert:** 18. Januar 2026
**Basis:** [PARTY_SERVICE_INTERFACE_ANALYSIS.md](../microservices/party-service/PARTY_SERVICE_INTERFACE_ANALYSIS.md)
**Status:** ✅ Aktualisiert mit neuen Zahlen und Erkenntnissen

### Wichtige Erkenntnisse aus der Interface-Analyse

- **257 Service-Definitionen** im Party-Modul
- **42 Aufrufe** aus anderen Modulen (Order: 23, Product: 6, Accounting: 6, weitere: 7)
- **Hybrid-Ansatz:** REST für synchrone Calls + Kafka Events für asynchrone Benachrichtigungen
- **12 Wochen Gesamtdauer** für vollständige Extraktion
- **~229.000 € Gesamtkosten** (Personal + Infrastruktur)

## Empfohlene Struktur: Gradle Multi-Project Build

### Vorteile

✅ **Klare Trennung:** Eigenes Modul mit eigenen Dependencies
✅ **Unabhängig startbar:** Eigene `main()` Methode, eigener Port
✅ **Shared Code möglich:** Gemeinsame Utilities bei Bedarf
✅ **Einfache Migration:** Später leicht in eigenes Repo verschiebbar
✅ **Gradle-Integration:** Nutzt bestehendes Build-System
✅ **IDE-Support:** IntelliJ/Eclipse erkennen Multi-Project automatisch

## 1. Verzeichnisstruktur

```
ofbiz/
├── framework/                    # Bestehender OFBiz-Code
├── applications/                 # Bestehende OFBiz-Anwendungen
├── microservices/               # NEUES Verzeichnis für Microservices
│   ├── party-service/           # Party Service PoC
│   │   ├── build.gradle         # Eigenes Build-File
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/
│   │   │   │   │   └── org/apache/ofbiz/party/microservice/
│   │   │   │   │       ├── PartyServiceApplication.java
│   │   │   │   │       ├── config/
│   │   │   │   │       │   ├── DatabaseConfig.java
│   │   │   │   │       │   ├── SecurityConfig.java
│   │   │   │   │       │   └── CacheConfig.java
│   │   │   │   │       ├── domain/
│   │   │   │   │       │   ├── model/
│   │   │   │   │       │   │   ├── Party.java
│   │   │   │   │       │   │   ├── Person.java
│   │   │   │   │       │   │   ├── PartyGroup.java
│   │   │   │   │       │   │   └── ContactMech.java
│   │   │   │   │       │   ├── repository/
│   │   │   │   │       │   │   ├── PartyRepository.java
│   │   │   │   │       │   │   └── ContactMechRepository.java
│   │   │   │   │       │   └── service/
│   │   │   │   │       │       ├── PartyService.java
│   │   │   │   │       │       └── ContactMechService.java
│   │   │   │   │       ├── application/
│   │   │   │   │       │   ├── dto/
│   │   │   │   │       │   │   ├── PartyDTO.java
│   │   │   │   │       │   │   └── ContactMechDTO.java
│   │   │   │   │       │   └── mapper/
│   │   │   │   │       │       └── PartyMapper.java
│   │   │   │   │       ├── infrastructure/
│   │   │   │   │       │   ├── rest/
│   │   │   │   │       │   │   ├── PartyController.java
│   │   │   │   │       │   │   └── ContactMechController.java
│   │   │   │   │       │   └── messaging/
│   │   │   │   │       │       └── PartyEventPublisher.java
│   │   │   │   │       └── adapter/
│   │   │   │   │           └── ofbiz/
│   │   │   │   │               └── OFBizPartyAdapter.java
│   │   │   │   └── resources/
│   │   │   │       ├── application.yml
│   │   │   │       ├── application-dev.yml
│   │   │   │       ├── application-prod.yml
│   │   │   │       └── db/migration/
│   │   │   │           ├── V1__create_party_tables.sql
│   │   │   │           └── V2__create_contact_tables.sql
│   │   │   └── test/
│   │   │       ├── java/
│   │   │       │   └── org/apache/ofbiz/party/microservice/
│   │   │       │       ├── integration/
│   │   │       │       ├── unit/
│   │   │       │       └── e2e/
│   │   │       └── resources/
│   │   │           └── application-test.yml
│   │   ├── Dockerfile
│   │   ├── docker-compose.yml
│   │   └── README.md
│   │
│   └── shared/                  # Optional: Gemeinsame Utilities
│       ├── build.gradle
│       └── src/main/java/
│           └── org/apache/ofbiz/microservices/shared/
│               ├── dto/
│               └── util/
│
├── settings.gradle              # ANPASSEN: Microservices einbinden
├── build.gradle                 # Root Build-File
└── README.md
```

## 2. Gradle-Konfiguration

### 2.1 Root `settings.gradle` anpassen

```groovy
// Bestehende OFBiz-Module
include 'framework:base'
include 'framework:entity'
include 'framework:service'
// ... weitere bestehende Module

// NEUE Microservices
include 'microservices:party-service'
include 'microservices:shared'  // Optional

// Projekt-Namen setzen
rootProject.name = 'ofbiz'
```

### 2.2 `microservices/party-service/build.gradle`

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.1'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'org.apache.ofbiz.microservices'
version = '1.0.0-SNAPSHOT'
sourceCompatibility = '17'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-cache'
    
    // Database
    implementation 'org.postgresql:postgresql'
    implementation 'org.flywaydb:flyway-core'
    
    // Redis Cache
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    
    // Kafka (optional)
    implementation 'org.springframework.kafka:spring-kafka'
    
    // OpenAPI Documentation
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
    
    // MapStruct für DTO-Mapping
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // Optional: Shared Module
    // implementation project(':microservices:shared')
    
    // Optional: OFBiz-Adapter (für Dual-Write-Phase)
    // compileOnly project(':framework:entity')
    // compileOnly project(':framework:service')
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.testcontainers:testcontainers:1.19.3'
    testImplementation 'org.testcontainers:postgresql:1.19.3'
    testImplementation 'org.testcontainers:junit-jupiter:1.19.3'
}

tasks.named('test') {
    useJUnitPlatform()
}

// Eigene Main-Class definieren
springBoot {
    mainClass = 'org.apache.ofbiz.party.microservice.PartyServiceApplication'
}

// Separate JAR-Datei
bootJar {
    archiveBaseName = 'party-service'
    archiveVersion = version
}
```

### 2.3 Optional: `microservices/shared/build.gradle`

```groovy
plugins {
    id 'java-library'
}

group = 'org.apache.ofbiz.microservices'
version = '1.0.0-SNAPSHOT'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    // Gemeinsame Dependencies
    api 'org.slf4j:slf4j-api:2.0.9'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}
```

## 3. Party Service Application

### 3.1 `PartyServiceApplication.java`

```java
package org.apache.ofbiz.party.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PartyServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PartyServiceApplication.class, args);
    }
}
```

### 3.2 `application.yml`

```yaml
spring:
  application:
    name: party-service
  
  # Eigener Port (nicht 8080 wie OFBiz)
  server:
    port: 8081
  
  # Datasource
  datasource:
    url: jdbc:postgresql://localhost:5432/party_service
    username: party_user
    password: party_pass
    driver-class-name: org.postgresql.Driver
  
  # JPA
  jpa:
    hibernate:
      ddl-auto: validate  # Flyway managed
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  # Flyway
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
  
  # Redis Cache
  cache:
    type: redis
  redis:
    host: localhost
    port: 6379
  
  # Kafka (optional)
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

# Management Endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

# OpenAPI
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

# Logging
logging:
  level:
    org.apache.ofbiz.party: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
```

### 3.3 `application-dev.yml` (Development)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/party_service_dev
  
  jpa:
    show-sql: true
  
  # H2 für lokale Entwicklung (optional)
  # datasource:
  #   url: jdbc:h2:mem:party_service
  #   driver-class-name: org.h2.Driver
  # h2:
  #   console:
  #     enabled: true

logging:
  level:
    org.apache.ofbiz.party: DEBUG
```

## 4. Starten des Party Service

### 4.1 Via Gradle

```bash
# Aus dem Root-Verzeichnis
./gradlew :microservices:party-service:bootRun

# Oder direkt im Microservices-Verzeichnis
cd microservices/party-service
../../gradlew bootRun
```

### 4.2 Via IDE (IntelliJ IDEA)

1. **Projekt importieren:** File → Open → `ofbiz/` auswählen
2. **Gradle Sync:** IntelliJ erkennt Multi-Project automatisch
3. **Run Configuration erstellen:**
   - Main class: `org.apache.ofbiz.party.microservice.PartyServiceApplication`
   - Module: `ofbiz.microservices.party-service.main`
   - Working directory: `$PROJECT_DIR$/microservices/party-service`
4. **Run:** Grüner Play-Button

### 4.3 Via JAR

```bash
# Build
./gradlew :microservices:party-service:bootJar

# Run
java -jar microservices/party-service/build/libs/party-service-1.0.0-SNAPSHOT.jar
```

### 4.4 Via Docker

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY build/libs/party-service-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  party-service:
    build: .
    ports:
      - "8081:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/party_service
      SPRING_DATASOURCE_USERNAME: party_user
      SPRING_DATASOURCE_PASSWORD: party_pass
      SPRING_REDIS_HOST: redis
    depends_on:
      - postgres
      - redis
  
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: party_service
      POSTGRES_USER: party_user
      POSTGRES_PASSWORD: party_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
  
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  postgres_data:
```

**Starten:**
```bash
cd microservices/party-service
docker-compose up -d
```

## 5. Parallelbetrieb mit OFBiz

### 5.1 Ports

| Service | Port | URL |
|---------|------|-----|
| **OFBiz** | 8080 | http://localhost:8080 |
| **Party Service** | 8081 | http://localhost:8081 |
| **Party Service API Docs** | 8081 | http://localhost:8081/swagger-ui.html |
| **Party Service Actuator** | 8081 | http://localhost:8081/actuator/health |

### 5.2 Beide Services gleichzeitig starten

**Terminal 1 - OFBiz:**
```bash
./gradlew ofbiz
```

**Terminal 2 - Party Service:**
```bash
./gradlew :microservices:party-service:bootRun
```

### 5.3 Gradle Task für beide Services

**Root `build.gradle` erweitern:**
```groovy
task startAll {
    group = 'application'
    description = 'Starts OFBiz and all microservices'
    
    dependsOn ':ofbiz'
    dependsOn ':microservices:party-service:bootRun'
}
```

**Starten:**
```bash
./gradlew startAll
```

## 6. Integration mit OFBiz (Dual-Write-Phase)

### 6.1 OFBiz-Adapter im Party Service

```java
package org.apache.ofbiz.party.microservice.adapter.ofbiz;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ofbiz.integration.enabled", havingValue = "true")
public class OFBizPartyAdapter {
    
    // Optional: Zugriff auf OFBiz-Datenbank für Dual-Write
    // Nur während der Migrationsphase
    
    public void syncToOFBiz(PartyDTO party) {
        // Schreibe auch in OFBiz-Datenbank
        // Nutzt JDBC direkt oder OFBiz-Entity-Engine
    }
}
```

### 6.2 Feature Flag

**application.yml:**
```yaml
ofbiz:
  integration:
    enabled: false  # true während Dual-Write-Phase
    database:
      url: jdbc:postgresql://localhost:5432/ofbiz
      username: ofbiz
      password: ofbiz
```

## 7. Testing

### 7.1 Unit Tests

```bash
./gradlew :microservices:party-service:test
```

### 7.2 Integration Tests mit Testcontainers

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class PartyServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:15-alpine");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Test
    void shouldCreateParty() {
        // Test implementation
    }
}
```

## 8. Migration zu eigenem Repository (später)

Wenn der PoC erfolgreich ist, kann der Service leicht in ein eigenes Repository verschoben werden:

```bash
# 1. Neues Repository erstellen
git init party-service
cd party-service

# 2. Code kopieren
cp -r ../ofbiz/microservices/party-service/* .

# 3. Gradle-Konfiguration anpassen
# settings.gradle vereinfachen
# build.gradle: Projekt-Dependencies entfernen

# 4. CI/CD einrichten
# GitHub Actions, Jenkins, etc.

# 5. Deployment
# Kubernetes, Docker Swarm, etc.
```

## 9. Vorteile dieser Struktur

### 9.1 Für Entwicklung

✅ **Einfacher Start:** `./gradlew :microservices:party-service:bootRun`

✅ **IDE-Integration:** IntelliJ/Eclipse erkennen Multi-Project

✅ **Debugging:** Beide Services parallel debuggen

✅ **Shared Code:** Gemeinsame Utilities bei Bedarf

### 9.2 Für Testing

✅ **Isolierte Tests:** Party Service Tests unabhängig von OFBiz

✅ **Testcontainers:** Echte Datenbank für Integration Tests

✅ **Schnelle Feedback-Loop:** Nur Party Service neu starten

### 9.3 Für Deployment

✅ **Separate JAR:** `party-service-1.0.0-SNAPSHOT.jar`

✅ **Docker-Image:** Eigenes Image für Party Service

✅ **Unabhängiges Scaling:** Party Service separat skalieren

### 9.4 Für Migration

✅ **Schrittweise:** Erst PoC, dann Produktion

✅ **Rollback:** Feature Flag deaktivieren

✅ **Später trennbar:** Leicht in eigenes Repo verschiebbar

## 10. Aktualisierte Zahlen und Erkenntnisse

### 10.1 Service-Umfang

Basierend auf der detaillierten Interface-Analyse:

| Kategorie | Anzahl | Details |
|-----------|--------|---------|
| **Service-Definitionen** | 257 | Party (24), Contact (22), Agreement (18), View (15), Communication (3), Sonstige (175+) |
| **Kern-API-Klassen** | 6 | PartyServices, ContactMechServices, PartyWorker, ContactMechWorker, PartyHelper, ContactHelper |
| **Öffentliche Methoden** | 100+ | CRUD, Suche, Helper-Funktionen |
| **REST Endpoints** | 30+ | Party, Person, Group, Contacts, Addresses, Phones, Emails, Search |
| **Kafka Events** | 8+ | PartyCreated, PartyUpdated, ContactMechCreated, etc. |

### 10.2 Abhängigkeiten zu anderen Modulen

**Eingehende Aufrufe (42 gesamt):**
- Order-Modul: 23 Aufrufe (Kundenadresse, Kontaktdaten)
- Product-Modul: 6 Aufrufe (Lieferantenadressen)
- Accounting-Modul: 6 Aufrufe (Rechnungsempfänger)
- Weitere Module: 7 Aufrufe (Shipment, Marketing, HumanRes, SFA, SecurityExt)

**Ausgehende Abhängigkeiten (9 Klassen):**
- Accounting: 3 Klassen (BillingAccountWorker, PaymentWorker, InvoiceWorker)
- Content: 2 Klassen (ContentWorker, DataResourceWorker)
- Common: 2 Klassen (NotificationServices, GeoWorker)
- Product: 1 Klasse (CatalogWorker)
- Minilang: 1 Klasse (SimpleMapProcessor)

**Entkopplungsstrategie:** Alle ausgehenden Abhängigkeiten werden durch REST API Calls oder Event Bus ersetzt.

### 10.3 Implementierungs-Roadmap (12 Wochen)

#### **Phase 0: Vorbereitung (Woche 1-2)**
**Woche 1: Infrastruktur**
- [ ] Docker Compose Setup (PostgreSQL, Kafka, Redis, Jaeger)
- [ ] Monorepo-Struktur erstellen (`microservices/party-service/`)
- [ ] `settings.gradle` anpassen
- [ ] CI/CD Pipeline (GitHub Actions)

**Woche 2: Datenmodell & API-Design**
- [ ] ~30 Party-Tabellen identifizieren und extrahieren
- [ ] PostgreSQL-Schema mit Flyway Migrations
- [ ] OpenAPI 3.0 Spezifikation (30+ Endpoints)
- [ ] Kafka Event-Schema (Avro)

**Deliverables:**
- ✅ Funktionierende Entwicklungsumgebung
- ✅ Datenbank-Schema
- ✅ API-Spezifikation

#### **Phase 1: Service-Implementierung (Woche 3-5)**
**Woche 3: Basis-Implementierung**
- [ ] Spring Boot 3.x Projekt Setup
- [ ] JPA Entities (Party, Person, PartyGroup, ContactMech, etc.)
- [ ] Repositories (PartyRepository, ContactMechRepository)
- [ ] Core Services (PartyService, ContactMechService, PartySearchService)

**Woche 4: REST API & Events**
- [ ] REST Controller (PartyController, ContactMechController, SearchController)
- [ ] DTOs und MapStruct Mapper
- [ ] Kafka Event Publishing (PartyCreated, PartyUpdated, etc.)
- [ ] Request Validation & Exception Handling

**Woche 5: Caching & Performance**
- [ ] Redis Caching für häufige Abfragen
- [ ] Datenbank-Indizes optimieren
- [ ] Load Testing (JMeter/Gatling)
- [ ] Performance-Ziel: < 100ms für einfache Queries

**Deliverables:**
- ✅ Lauffähiger Party Service
- ✅ REST API vollständig
- ✅ Event Publishing funktioniert
- ✅ Unit & Integration Tests (>80% Coverage)

#### **Phase 2: Integration mit OFBiz (Woche 6-7)**
**Woche 6: Anti-Corruption Layer**
- [ ] PartyServiceAdapter in OFBiz implementieren
- [ ] PartyServiceClient (REST Client mit Feign/RestTemplate)
- [ ] Circuit Breaker (Resilience4j)
- [ ] Feature Flags für schrittweise Umstellung
- [ ] Dual-Read-Logik (neuer Service mit Fallback)

**Woche 7: Code-Migration**
- [ ] Order-Modul: 23 Aufrufe anpassen (3-4 Tage)
- [ ] Product-Modul: 6 Aufrufe anpassen (1-2 Tage)
- [ ] Accounting-Modul: 6 Aufrufe anpassen (1-2 Tage)
- [ ] Weitere Module: 7 Aufrufe anpassen (1 Tag)

**Deliverables:**
- ✅ ACL implementiert
- ✅ Alle Module nutzen ACL
- ✅ Tests grün

#### **Phase 3: Datenmigration & Cutover (Woche 8-9)**
**Woche 8: Datenmigration**
- [ ] ETL-Script für Party-Daten (~30 Tabellen)
- [ ] Test-Migration auf Staging
- [ ] Daten-Validierung & Konsistenz-Checks
- [ ] Migrations-Runbook

**Woche 9: Cutover**
- [ ] Produktions-Migration (2-4 Stunden Downtime)
- [ ] Schrittweiser Traffic-Shift (10% → 25% → 50% → 75% → 100%)
- [ ] Monitoring & Fehleranalyse
- [ ] Rollback-Plan bereit

**Deliverables:**
- ✅ Party Service in Produktion
- ✅ 100% Traffic auf neuem Service
- ✅ Performance-Ziele erreicht

#### **Phase 4: Stabilisierung (Woche 10-12)**
- [ ] Bugfixing aus Produktion
- [ ] Performance-Optimierungen
- [ ] Dokumentation vervollständigen
- [ ] Lessons Learned
- [ ] OFBiz Party-Code als deprecated markieren

**Deliverables:**
- ✅ Stabiler Service (Uptime > 99.9%)
- ✅ Vollständige Dokumentation
- ✅ Team-Retrospektive

### 10.4 Aufwand & Kosten (aktualisiert)

**Team:**
- 2-3 Senior Backend-Entwickler (Java/Spring Boot)
- 1 DevOps-Engineer (Kubernetes, CI/CD)
- 1 Architekt (Teilzeit, Beratung)
- 1 QA-Engineer (Testing)

**Zeitplan:**
- **Gesamt:** 12 Wochen (3 Monate)
- **Personentage:** 130-190 PT

**Kosten:**
- **Personal:** ~225.000 € (3 Devs × 60 Tage × 800€ + DevOps × 40 Tage × 900€ + Architekt × 20 Tage × 1.200€ + QA × 30 Tage × 700€)
- **Infrastruktur:** ~3.900 € (3 Monate × 1.300€/Monat)
- **Gesamt:** ~229.000 €

### 10.5 Kritische Erfolgsfaktoren

✅ **Technisch:**
- Monorepo-Setup ermöglicht parallele Entwicklung
- Anti-Corruption Layer ermöglicht schrittweise Migration
- Feature Flags ermöglichen sicheren Rollout
- Dual-Write-Phase minimiert Risiko

✅ **Organisatorisch:**
- Klare Abgrenzung des Scopes (257 Services, 42 Aufrufe)
- Realistische Zeitplanung (12 Wochen)
- Dediziertes Team (2-3 Entwickler)
- Stakeholder-Buy-In

⚠️ **Risiken:**
- Framework-Kopplung (Entity/Service Engine)
- Datenmigration (~30 Tabellen)
- Performance-Degradation durch Netzwerk-Calls
- Team-Überforderung

**Mitigation:** Siehe [PARTY_SERVICE_INTERFACE_ANALYSIS.md](../microservices/party-service/PARTY_SERVICE_INTERFACE_ANALYSIS.md) Abschnitt 6

## 11. Nächste Schritte (Priorisiert)

### Sofort (Woche 1)
1. [ ] **Monorepo-Struktur erstellen**
   ```bash
   mkdir -p microservices/party-service/src/{main,test}/{java,resources}
   ```

2. [ ] **`settings.gradle` anpassen**
   ```groovy
   include 'microservices:party-service'
   ```

3. [ ] **`build.gradle` für Party Service erstellen**
   - Spring Boot 3.2.1
   - PostgreSQL, Redis, Kafka
   - OpenAPI, MapStruct, Lombok

4. [ ] **Docker Compose Setup**
   ```bash
   cd microservices/party-service
   # docker-compose.yml mit PostgreSQL, Redis, Kafka
   docker-compose up -d
   ```

5. [ ] **`PartyServiceApplication.java` implementieren**
   ```java
   @SpringBootApplication
   @EnableCaching
   public class PartyServiceApplication {
       public static void main(String[] args) {
           SpringApplication.run(PartyServiceApplication.class, args);
       }
   }
   ```

### Kurzfristig (Woche 2-3)
1. [ ] **Datenbank-Schema extrahieren**
   - Party, Person, PartyGroup, PartyRole
   - PostalAddress, TelecomNumber, ContactMech
   - Flyway Migrations erstellen

2. [ ] **OpenAPI 3.0 Spezifikation schreiben**
   - 30+ REST Endpoints definieren
   - DTOs dokumentieren

3. [ ] **JPA Entities implementieren**
   - @Entity Klassen
   - Repositories
   - Unit Tests

4. [ ] **Erste REST Endpoints**
   - GET /api/v1/parties/{partyId}
   - POST /api/v1/parties
   - Integration Tests

### Mittelfristig (Woche 4-7)
1. [ ] **Vollständige REST API**
   - Alle 30+ Endpoints
   - Validation & Exception Handling
   - Swagger UI

2. [ ] **Kafka Event Publishing**
   - PartyCreated, PartyUpdated, etc.
   - Avro Schema Registry

3. [ ] **Anti-Corruption Layer in OFBiz**
   - PartyServiceAdapter
   - PartyServiceClient
   - Feature Flags

4. [ ] **Code-Migration (42 Aufrufe)**
   - Order: 23 Aufrufe
   - Product: 6 Aufrufe
   - Accounting: 6 Aufrufe
   - Weitere: 7 Aufrufe

### Langfristig (Woche 8-12)
1. [ ] **Datenmigration**
   - ETL-Script
   - Test-Migration
   - Produktions-Migration

2. [ ] **Cutover**
   - Schrittweiser Traffic-Shift
   - Monitoring
   - Rollback-Plan

3. [ ] **Stabilisierung**
   - Bugfixing
   - Performance-Optimierung
   - Dokumentation

## 11. Troubleshooting

### Problem: Port 8081 bereits belegt

**Lösung:**
```yaml
# application.yml
server:
  port: 8082  # Anderen Port verwenden
```

### Problem: Gradle findet Microservice nicht

**Lösung:**
```bash
# Gradle Sync
./gradlew --refresh-dependencies

# IntelliJ: File → Invalidate Caches → Restart
```

### Problem: OFBiz-Dependencies nicht gefunden

**Lösung:**
```groovy
// build.gradle
dependencies {
    // Optional machen
    compileOnly project(':framework:entity')
    
    // Oder ganz entfernen für PoC
}
```

## 12. Troubleshooting

### Problem: Port 8081 bereits belegt

**Lösung:**
```yaml
# application.yml
server:
  port: 8082  # Anderen Port verwenden
```

### Problem: Gradle findet Microservice nicht

**Lösung:**
```bash
# Gradle Sync
./gradlew --refresh-dependencies

# IntelliJ: File → Invalidate Caches → Restart
```

### Problem: OFBiz-Dependencies nicht gefunden

**Lösung:**
```groovy
// build.gradle
dependencies {
    // Optional machen
    compileOnly project(':framework:entity')
    
    // Oder ganz entfernen für PoC
}
```

### Problem: 42 Aufrufe in anderen Modulen - wie priorisieren?

**Lösung:**
1. **Phase 1:** Nur Lese-Zugriffe umstellen (GET-Operationen)
2. **Phase 2:** Schreib-Zugriffe mit Dual-Write
3. **Phase 3:** Vollständiger Cutover

**Priorisierung nach Modul:**
- Order (23 Aufrufe) → Höchste Priorität, kritisch für Business
- Product (6 Aufrufe) → Mittlere Priorität
- Accounting (6 Aufrufe) → Mittlere Priorität
- Weitere (7 Aufrufe) → Niedrige Priorität

### Problem: Performance-Degradation durch REST Calls

**Lösung:**
- **Caching:** Redis für häufige Abfragen (z.B. getPartyName)
- **Batch-APIs:** Mehrere Parties in einem Call abrufen
- **Async:** Nicht-kritische Calls asynchron machen
- **Monitoring:** Response-Zeiten überwachen und optimieren

### Problem: Kafka nicht verfügbar während Entwicklung

**Lösung:**
```yaml
# application-dev.yml
spring:
  kafka:
    enabled: false  # Kafka optional für lokale Entwicklung
```

Oder nutze Embedded Kafka für Tests:
```groovy
testImplementation 'org.springframework.kafka:spring-kafka-test'
```

## 13. Zusammenfassung

### Diese Monorepo-Struktur bietet:

✅ **Klare Trennung:** Eigenes Gradle-Modul mit 257 Services
✅ **Unabhängig startbar:** Eigene Main-Class, eigener Port (8081)
✅ **Einfache Entwicklung:** Im selben Repository, paralleles Debugging
✅ **Parallelbetrieb:** OFBiz (8080) und Party Service (8081) gleichzeitig
✅ **Testbar:** Isolierte Tests mit Testcontainers, >80% Coverage
✅ **Migrierbar:** Später leicht in eigenes Repo verschiebbar
✅ **Skalierbar:** Unabhängiges Deployment und Scaling
✅ **Hybrid-Kommunikation:** REST für synchrone Calls, Kafka für Events

### Wichtige Metriken:

| Metrik | Wert | Bedeutung |
|--------|------|-----------|
| **Service-Definitionen** | 257 | Umfang des Party-Service |
| **Aufrufe aus anderen Modulen** | 42 | Anzupassende Stellen |
| **REST Endpoints** | 30+ | Öffentliche API |
| **Kafka Events** | 8+ | Asynchrone Benachrichtigungen |
| **Dauer** | 12 Wochen | Vollständige Extraktion |
| **Kosten** | ~229.000 € | Personal + Infrastruktur |
| **Team** | 2-3 Entwickler | + DevOps + Architekt + QA |

### Empfehlung:

**✅ GO für Party-Service als Proof-of-Concept im Monorepo!**

**Vorteile:**
1. **Schneller Start:** Keine separate Repository-Verwaltung
2. **Einfaches Debugging:** Beide Services in einer IDE
3. **Schrittweise Migration:** Feature Flags ermöglichen sicheren Rollout
4. **Geringes Risiko:** Fallback zu OFBiz jederzeit möglich
5. **Lessons Learned:** Erkenntnisse für weitere Services (Content, Product)

**Nächster Schritt:**
```bash
# 1. Verzeichnisstruktur erstellen
mkdir -p microservices/party-service/src/{main,test}/{java,resources}

# 2. settings.gradle anpassen
echo "include 'microservices:party-service'" >> settings.gradle

# 3. build.gradle erstellen
# (siehe Abschnitt 2.2)

# 4. Docker Compose starten
cd microservices/party-service
docker-compose up -d

# 5. Party Service starten
../../gradlew :microservices:party-service:bootRun
```

**Erfolg messen:**
- ✅ Party Service läuft parallel zu OFBiz
- ✅ REST API unter http://localhost:8081/swagger-ui.html erreichbar
- ✅ Erste Endpoints funktionieren
- ✅ Tests grün (>80% Coverage)
- ✅ Performance < 100ms für einfache Queries

---

## 14. Referenzen

- [PARTY_SERVICE_INTERFACE_ANALYSIS.md](../microservices/party-service/PARTY_SERVICE_INTERFACE_ANALYSIS.md) - Detaillierte Schnittstellenanalyse
- [SERVICE_DECOMPOSITION_ANALYSIS.md](../microservices/party-service/SERVICE_DECOMPOSITION_ANALYSIS.md) - Basis-Analyse
- [MICROSERVICES_ARCHITECTURE.md](./MICROSERVICES_ARCHITECTURE.md) - Architektur-Übersicht

---

**Erstellt:** 2026-01-11
**Aktualisiert:** 2026-01-18
**Status:** ✅ Bereit für Implementierung mit aktualisierten Zahlen
**Version:** 2.0
