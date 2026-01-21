# Party Service - Dokumentation

Dieses Verzeichnis enthält die gesamte Dokumentation für den Party Service PoC.

---

## 📚 Übersicht

| Dokument | Beschreibung | Status |
|----------|--------------|--------|
| **Woche 2 Guides** | | |
| [`../WOCHE_2_QUICKSTART.md`](../WOCHE_2_QUICKSTART.md) | Quick Start Guide für Woche 2 | ✅ Fertig |
| [`../WOCHE_2_PROMPTS.md`](../WOCHE_2_PROMPTS.md) | 15 konkrete Prompts für Woche 2 | ✅ Fertig |
| [`../WOCHE_2_IMPLEMENTATION_GUIDE.md`](../WOCHE_2_IMPLEMENTATION_GUIDE.md) | Detaillierter Implementierungsguide | ✅ Fertig |
| **Datenmodell** | | |
| `PARTY_ENTITIES_ANALYSIS.md` | Analyse aller Party-Tabellen | 🔄 Woche 2 |
| `PARTY_ER_DIAGRAM.md` | ER-Diagramm (Mermaid) | 🔄 Woche 2 |
| `PARTY_DATA_MODEL.md` | Datenmodell-Beschreibung | 🔄 Woche 2 |
| **API & Events** | | |
| `KAFKA_EVENTS.md` | Kafka Event-Dokumentation | 🔄 Woche 2 |
| **Validierung** | | |
| `WOCHE_2_VALIDATION_RESULTS.md` | Validierungsergebnisse | 🔄 Woche 2 |
| **Status** | | |
| [`../MIGRATION_STATUS.md`](../MIGRATION_STATUS.md) | Gesamtfortschritt | ✅ Aktuell |

---

## 🚀 Schnellstart

### Für Woche 2 (Datenmodell & API-Design)

1. **Start:** Lies [`../WOCHE_2_QUICKSTART.md`](../WOCHE_2_QUICKSTART.md)
2. **Prompts:** Nutze [`../WOCHE_2_PROMPTS.md`](../WOCHE_2_PROMPTS.md)
3. **Details:** Siehe [`../WOCHE_2_IMPLEMENTATION_GUIDE.md`](../WOCHE_2_IMPLEMENTATION_GUIDE.md)

### Für Entwickler

```bash
# Projekt starten
./gradlew :microservices:party-service:bootRun

# H2 Console
open http://localhost:8081/api/party/h2-console

# Swagger UI
open http://localhost:8081/api/party/swagger-ui.html

# Tests
./gradlew :microservices:party-service:test
```

---

## 📖 Dokumentationsstruktur

### Woche 2: Datenmodell & API-Design

**Ziel:** Fundament für die Implementierung legen

**Deliverables:**
- Datenmodell-Analyse (Neo4j)
- ER-Diagramm
- Flyway Migrations (5 SQL-Dateien)
- OpenAPI 3.0 Spezifikation
- DTOs (~12 Klassen)
- Event-Schema (8 Events)

**Guides:**
- [`WOCHE_2_QUICKSTART.md`](../WOCHE_2_QUICKSTART.md) - Schnelleinstieg
- [`WOCHE_2_PROMPTS.md`](../WOCHE_2_PROMPTS.md) - 15 Prompts
- [`WOCHE_2_IMPLEMENTATION_GUIDE.md`](../WOCHE_2_IMPLEMENTATION_GUIDE.md) - Detailliert

---

### Woche 3: Basis-Implementierung (Geplant)

**Ziel:** JPA Entities, Repositories, Services

**Deliverables:**
- JPA Entities (~15 Klassen)
- Repositories (~5 Interfaces)
- Core Services (~5 Klassen)
- Unit Tests (>80% Coverage)

**Status:** 🔄 Ausstehend

---

### Woche 4: REST API & Events (Geplant)

**Ziel:** REST Controller, Event Publishing

**Deliverables:**
- REST Controller (~5 Klassen)
- MapStruct Mapper (~5 Klassen)
- Kafka Event Publishing
- Integration Tests

**Status:** 🔄 Ausstehend

---

## 🔍 Wichtige Konzepte

### Datenmodell

Das Party-Datenmodell basiert auf dem OFBiz Party-Modul:

```
Party (Abstract)
├── Person
└── PartyGroup

ContactMech (Abstract)
├── PostalAddress
├── TelecomNumber
└── EmailAddress

PartyContactMech (Junction Table)
PartyRole
PartyRelationship
```

**Details:** Siehe `PARTY_DATA_MODEL.md` (nach Woche 2)

---

### API-Design

**REST API:**
- 30+ Endpoints
- OpenAPI 3.0 Spezifikation
- Pagination, Filtering, Sorting
- Bean Validation

**Kafka Events:**
- 8+ Event-Typen
- Asynchrone Kommunikation
- Event Sourcing
- Audit Trail

**Details:** Siehe `../src/main/resources/openapi/party-service-api.yaml` (nach Woche 2)

---

### Architektur

**Clean Architecture:**
```
domain/          - Entities, Repositories (Interfaces)
application/     - DTOs, Mapper, Use Cases
infrastructure/  - REST, Messaging, Persistence
adapter/         - OFBiz Integration (ACL)
```

**Technologien:**
- Spring Boot 3.2.1
- PostgreSQL + Flyway
- Redis (Caching)
- Kafka (Events)
- MapStruct (Mapping)
- Testcontainers (Tests)

---

## 📊 Fortschritt

Siehe [`../MIGRATION_STATUS.md`](../MIGRATION_STATUS.md) für den aktuellen Fortschritt.

**Aktueller Stand:**
- Phase 0: Vorbereitung - 50% (Woche 1 ✅, Woche 2 🔄)
- Phase 1: Implementierung - 0%
- Phase 2: Integration - 0%
- Phase 3: Migration - 0%
- Phase 4: Stabilisierung - 0%

---

## 🛠️ Tools & Ressourcen

### Neo4j-Analyse

```bash
# Neo4j starten
cd tools/jqassistant-commandline-neo4jv5-2.8.0/macos
./start_jqa_db.sh /Users/oliverwidder/dev/ofbiz neo4j

# Neo4j Browser
open http://localhost:7474
```

### Entwicklung

```bash
# Build
./gradlew :microservices:party-service:build

# Tests
./gradlew :microservices:party-service:test

# Integration Tests
./gradlew :microservices:party-service:integrationTest

# E2E Tests
./gradlew :microservices:party-service:e2eTest

# Alle Tests
./gradlew :microservices:party-service:allTests
```

### Docker Compose

```bash
# Services starten (PostgreSQL, Redis, Kafka)
docker-compose up -d

# Services stoppen
docker-compose down
```

---

## 📝 Konventionen

### Dokumentation

- **Markdown:** Alle Dokumentation in Markdown
- **Mermaid:** Diagramme mit Mermaid-Syntax
- **Sprache:** Deutsch für Dokumentation, Englisch für Code

### Code

- **Java:** Java 17, Spring Boot 3.2.x
- **Naming:** camelCase für Java, snake_case für SQL
- **JavaDoc:** Alle öffentlichen Klassen und Methoden
- **Tests:** >80% Coverage

### Git

- **Commits:** Aussagekräftige Commit-Messages
- **Branches:** feature/*, bugfix/*, release/*
- **PRs:** Code Review erforderlich

---

## 🤝 Beitragen

### Neue Dokumentation hinzufügen

1. Erstelle Markdown-Datei in `docs/`
2. Füge Link in dieser README hinzu
3. Aktualisiere [`../MIGRATION_STATUS.md`](../MIGRATION_STATUS.md)

### Dokumentation aktualisieren

1. Bearbeite entsprechende Markdown-Datei
2. Aktualisiere "Letzte Aktualisierung" Datum
3. Committe mit aussagekräftiger Message

---

## 📞 Support

Bei Fragen oder Problemen:

1. **Dokumentation prüfen:** Siehe Guides oben
2. **Status prüfen:** Siehe [`../MIGRATION_STATUS.md`](../MIGRATION_STATUS.md)
3. **Roo fragen:** Nutze Party-PoC Mode

---

## 📅 Timeline

| Woche | Phase | Status | Dokumentation |
|-------|-------|--------|---------------|
| **Woche 1** | Infrastruktur & Tooling | ✅ Abgeschlossen | [`../MIGRATION_STATUS.md`](../MIGRATION_STATUS.md) |
| **Woche 2** | Datenmodell & API-Design | 🔄 In Arbeit | Dieser Guide |
| **Woche 3** | Basis-Implementierung | 📋 Geplant | TBD |
| **Woche 4** | REST API & Events | 📋 Geplant | TBD |
| **Woche 5** | Caching & Performance | 📋 Geplant | TBD |
| **Woche 6** | Anti-Corruption Layer | 📋 Geplant | TBD |
| **Woche 7** | Code-Migration | 📋 Geplant | TBD |
| **Woche 8** | Datenmigration | 📋 Geplant | TBD |
| **Woche 9** | Cutover & Go-Live | 📋 Geplant | TBD |
| **Woche 10-12** | Stabilisierung | 📋 Geplant | TBD |

---

**Erstellt:** 2026-01-21  
**Letzte Aktualisierung:** 2026-01-21  
**Version:** 1.0
