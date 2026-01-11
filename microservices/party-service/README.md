# Party Service - Microservice PoC

## Übersicht

Dies ist ein Proof-of-Concept für den Party Service als eigenständiger Microservice, der aus dem OFBiz-Monolithen extrahiert wurde.

## Verantwortung

Der Party Service verwaltet:
- **Parties:** Kunden, Lieferanten, Organisationen
- **Personen:** Natürliche Personen mit Namen, Geburtsdatum
- **Organisationen:** Firmen, Gruppen
- **Kontaktmechanismen:** Adressen, Telefonnummern, E-Mails
- **Beziehungen:** Beziehungen zwischen Parties
- **Rollen:** Kunde, Lieferant, Mitarbeiter, etc.

## Technologie-Stack

- **Framework:** Spring Boot 3.2.x
- **Language:** Java 17
- **Build:** Gradle 8.x
- **Database:** PostgreSQL 15
- **Cache:** Redis 7
- **API:** REST + OpenAPI 3.0
- **Testing:** JUnit 5, Testcontainers

## Starten

### Voraussetzungen

- Java 17+
- PostgreSQL 15+ (oder Docker)
- Redis 7+ (oder Docker)

### Via Gradle (aus OFBiz-Root)

```bash
./gradlew :microservices:party-service:bootRun
```

### Via Docker Compose

```bash
cd microservices/party-service
docker-compose up -d
```

### Via IDE

1. Öffne `PartyServiceApplication.java`
2. Run → Run 'PartyServiceApplication'

## Endpoints

- **API:** http://localhost:8081
- **API Docs:** http://localhost:8081/swagger-ui.html
- **Health:** http://localhost:8081/actuator/health

## Entwicklung

### Struktur

```
src/main/java/org/apache/ofbiz/party/microservice/
├── PartyServiceApplication.java    # Main-Class
├── config/                         # Konfiguration
├── domain/                         # Domain Model
│   ├── model/                      # JPA Entities
│   ├── repository/                 # Repositories
│   └── service/                    # Domain Services
├── application/                    # Application Layer
│   ├── dto/                        # DTOs
│   └── mapper/                     # Mapper
├── infrastructure/                 # Infrastructure
│   ├── rest/                       # REST Controllers
│   └── messaging/                  # Kafka Events
└── adapter/                        # Adapter
    └── ofbiz/                      # OFBiz Integration
```

### Tests ausführen

```bash
./gradlew :microservices:party-service:test
```

## Status

🚧 **In Entwicklung** - Proof of Concept Phase

## Dokumentation

Siehe `/refactor` Verzeichnis für detaillierte Dokumentation:
- [Monorepo Setup Guide](../../refactor/PARTY_SERVICE_MONOREPO_SETUP.md)
- [Extraction Guide](../../refactor/PARTY_SERVICE_EXTRACTION_GUIDE.md)
- [Communication Analysis](../../refactor/PARTY_SERVICE_COMMUNICATION_ANALYSIS.md)
