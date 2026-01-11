# Party Service PoC - Migration Status

**Letzte Aktualisierung:** 2026-01-11 15:54 CET  
**Status:** 🟡 In Entwicklung - Phase 1

---

## Übersicht

Dieser Dokument trackt den Fortschritt der Migration des Party Service aus dem OFBiz-Monolithen in einen eigenständigen Microservice als Proof-of-Concept.

## Gesamtfortschritt

```
Phase 1: Setup                    [████░░░░░░] 40% (2/5 Schritte)
Phase 2: Domain Model             [░░░░░░░░░░]  0% (0/4 Schritte)
Phase 3: REST API                 [░░░░░░░░░░]  0% (0/4 Schritte)
Phase 4: Integration              [░░░░░░░░░░]  0% (0/4 Schritte)

GESAMT:                           [██░░░░░░░░] 12% (2/17 Schritte)
```

---

## Phase 1: Setup (Woche 1) - 40% Complete

### ✅ Schritt 1: Verzeichnisstruktur erstellen
**Status:** ✅ Abgeschlossen  
**Datum:** 2026-01-11  
**Durchgeführt von:** Roo AI

**Ergebnis:**
- Verzeichnisstruktur nach Clean Architecture erstellt
- 14 `.gitkeep` Dateien für leere Verzeichnisse
- `README.md` mit Projekt-Dokumentation
- `.gitignore` für Build-Artefakte und IDE-Dateien

**Dateien:**
```
microservices/party-service/
├── .gitignore
├── README.md
└── src/
    ├── main/java/org/apache/ofbiz/party/microservice/
    │   ├── config/
    │   ├── domain/{model,repository,service}/
    │   ├── application/{dto,mapper}/
    │   ├── infrastructure/{rest,messaging}/
    │   └── adapter/ofbiz/
    ├── main/resources/db/migration/
    └── test/java/org/apache/ofbiz/party/microservice/{unit,integration,e2e}/
```

**Commit:** Bereit für Git Commit

---

### ✅ Schritt 2: README und .gitignore erstellen
**Status:** ✅ Abgeschlossen  
**Datum:** 2026-01-11

**Ergebnis:**
- README.md mit Projekt-Übersicht, Tech-Stack, Start-Anweisungen
- .gitignore für Gradle, IDE, Logs

---

### 🔄 Schritt 3: settings.gradle anpassen
**Status:** 🔄 Ausstehend  
**Geplant:** Nächster Schritt

**Aufgaben:**
- [ ] `settings.gradle` im Root öffnen
- [ ] Zeile hinzufügen: `include 'microservices:party-service'`
- [ ] Gradle Sync durchführen
- [ ] Verifizieren: `./gradlew projects` zeigt party-service

**Datei:** `/Users/oliverwidder/dev/ofbiz/settings.gradle`

---

### 🔄 Schritt 4: build.gradle erstellen
**Status:** 🔄 Ausstehend  
**Abhängig von:** Schritt 3

**Aufgaben:**
- [ ] `microservices/party-service/build.gradle` erstellen
- [ ] Spring Boot Plugin konfigurieren (3.2.1)
- [ ] Dependencies hinzufügen:
  - [ ] Spring Boot Starter Web
  - [ ] Spring Boot Starter Data JPA
  - [ ] PostgreSQL Driver
  - [ ] Redis
  - [ ] Kafka (optional)
  - [ ] OpenAPI/Swagger
  - [ ] MapStruct
  - [ ] Lombok
  - [ ] Testcontainers
- [ ] Main-Class definieren: `PartyServiceApplication`
- [ ] Build testen: `./gradlew :microservices:party-service:build`

---

### 🔄 Schritt 5: PartyServiceApplication.java und application.yml
**Status:** 🔄 Ausstehend  
**Abhängig von:** Schritt 4

**Aufgaben:**
- [ ] `PartyServiceApplication.java` erstellen
  - [ ] `@SpringBootApplication` Annotation
  - [ ] `@EnableCaching` Annotation
  - [ ] `main()` Methode
- [ ] `application.yml` erstellen
  - [ ] Server Port: 8081
  - [ ] PostgreSQL Datasource
  - [ ] Redis Cache
  - [ ] Flyway Migration
  - [ ] Logging
  - [ ] Management Endpoints
- [ ] `application-dev.yml` erstellen
- [ ] `application-test.yml` erstellen
- [ ] Erste Start-Test: `./gradlew :microservices:party-service:bootRun`

---

## Phase 2: Domain Model (Woche 2) - 0% Complete

### 🔄 Schritt 1: JPA Entities erstellen
**Status:** 🔄 Ausstehend

**Aufgaben:**
- [ ] `Party.java` (Abstract Base Entity)
- [ ] `Person.java` (extends Party)
- [ ] `PartyGroup.java` (extends Party)
- [ ] `ContactMech.java` (Abstract Base Entity)
- [ ] `PostalAddress.java` (extends ContactMech)
- [ ] `TelecomNumber.java` (extends ContactMech)
- [ ] `EmailAddress.java` (extends ContactMech)
- [ ] `PartyRole.java`
- [ ] `PartyRelationship.java`

**Entities:** 9 Klassen

---

### 🔄 Schritt 2: Repositories implementieren
**Status:** 🔄 Ausstehend

**Aufgaben:**
- [ ] `PartyRepository.java` (extends JpaRepository)
- [ ] `ContactMechRepository.java`
- [ ] `PartyRoleRepository.java`
- [ ] `PartyRelationshipRepository.java`
- [ ] Custom Queries mit `@Query`

**Repositories:** 4 Interfaces

---

### 🔄 Schritt 3: Flyway Migrations erstellen
**Status:** 🔄 Ausstehend

**Aufgaben:**
- [ ] `V1__create_party_tables.sql`
- [ ] `V2__create_contact_tables.sql`
- [ ] `V3__create_relationship_tables.sql`
- [ ] `V4__create_indexes.sql`
- [ ] Test-Daten: `V99__test_data.sql` (nur für dev)

**Migrations:** 5 SQL-Dateien

---

### 🔄 Schritt 4: Unit Tests schreiben
**Status:** 🔄 Ausstehend

**Aufgaben:**
- [ ] `PartyRepositoryTest.java`
- [ ] `ContactMechRepositoryTest.java`
- [ ] Test mit H2 In-Memory DB
- [ ] Test ausführen: `./gradlew :microservices:party-service:test`

**Tests:** ~10-15 Unit Tests

---

## Phase 3: REST API (Woche 3) - 0% Complete

### 🔄 Schritt 1: DTOs und Mapper erstellen
**Status:** 🔄 Ausstehend

**Aufgaben:**
- [ ] `PartyDTO.java`
- [ ] `PersonDTO.java`
- [ ] `PartyGroupDTO.java`
- [ ] `ContactMechDTO.java`
- [ ] `PartyMapper.java` (MapStruct)
- [ ] `ContactMechMapper.java` (MapStruct)

**DTOs:** 6 Klassen

---

### 🔄 Schritt 2: Controllers implementieren
**Status:** 🔄 Ausstehend

**Aufgaben:**
- [ ] `PartyController.java`
  - [ ] GET /api/v1/parties
  - [ ] GET /api/v1/parties/{id}
  - [ ] POST /api/v1/parties
  - [ ] PUT /api/v1/parties/{id}
  - [ ] DELETE /api/v1/parties/{id}
- [ ] `ContactMechController.java`
  - [ ] GET /api/v1/parties/{id}/contacts
  - [ ] POST /api/v1/parties/{id}/contacts
  - [ ] PUT /api/v1/parties/{id}/contacts/{contactId}
  - [ ] DELETE /api/v1/parties/{id}/contacts/{contactId}

**Controllers:** 2 Klassen, ~10 Endpoints

---

### 🔄 Schritt 3: OpenAPI Dokumentation
**Status:** 🔄 Ausstehend

**Aufgaben:**
- [ ] Springdoc OpenAPI konfigurieren
- [ ] API-Beschreibungen hinzufügen
- [ ] Beispiele für Request/Response
- [ ] Swagger UI testen: http://localhost:8081/swagger-ui.html

---

### 🔄 Schritt 4: Integration Tests
**Status:** 🔄 Ausstehend

**Aufgaben:**
- [ ] `PartyControllerIntegrationTest.java`
- [ ] `ContactMechControllerIntegrationTest.java`
- [ ] Testcontainers für PostgreSQL
- [ ] REST API Tests mit MockMvc
- [ ] Tests ausführen

**Tests:** ~20-30 Integration Tests

---

## Phase 4: Integration mit OFBiz (Woche 4) - 0% Complete

### 🔄 Schritt 1: OFBiz-Adapter implementieren
**Status:** 🔄 Ausstehend

**Aufgaben:**
- [ ] `OFBizPartyAdapter.java`
- [ ] Dual-Write-Logik (Party Service + OFBiz DB)
- [ ] Feature Flag: `ofbiz.integration.enabled`
- [ ] Sync-Fehler-Handling

---

### 🔄 Schritt 2: Feature Flags einrichten
**Status:** 🔄 Ausstehend

**Aufgaben:**
- [ ] Feature Flag Konfiguration
- [ ] Toggle zwischen OFBiz und Party Service
- [ ] Monitoring für Feature Flag Status

---

### 🔄 Schritt 3: Dual-Write-Logik
**Status:** 🔄 Ausstehend

**Aufgaben:**
- [ ] Schreibe in beide Datenbanken
- [ ] Reconciliation Job (täglich)
- [ ] Monitoring von Sync-Fehlern

---

### 🔄 Schritt 4: E2E-Tests
**Status:** 🔄 Ausstehend

**Aufgaben:**
- [ ] Kompletter Party-Lifecycle-Test
- [ ] OFBiz + Party Service parallel
- [ ] Daten-Konsistenz-Tests
- [ ] Performance-Tests

**Tests:** ~10-15 E2E Tests

---

## Metriken

### Code-Statistiken
- **Klassen erstellt:** 2 (README, .gitignore)
- **Zeilen Code:** ~100
- **Tests geschrieben:** 0
- **Test-Coverage:** 0%

### Zeitaufwand
- **Geplant:** 4 Wochen (2 Entwickler)
- **Verbraucht:** 0.5 Tage
- **Verbleibend:** ~19.5 Tage

### Risiken
- 🟢 **Niedrig:** Verzeichnisstruktur und Setup
- 🟡 **Mittel:** Domain Model und Migrations
- 🔴 **Hoch:** Integration mit OFBiz (Dual-Write)

---

## Nächste Schritte

### Sofort (heute)
1. ✅ Verzeichnisstruktur committen
2. 🔄 `settings.gradle` anpassen
3. 🔄 `build.gradle` erstellen

### Diese Woche
4. 🔄 `PartyServiceApplication.java` implementieren
5. 🔄 `application.yml` konfigurieren
6. 🔄 Ersten Start-Test durchführen

### Nächste Woche
7. 🔄 Domain Model implementieren
8. 🔄 Repositories erstellen
9. 🔄 Flyway Migrations

---

## Entscheidungen

### 2026-01-11: Monorepo-Struktur
**Entscheidung:** Party Service im selben Repository wie OFBiz  
**Begründung:** Einfachere Entwicklung für PoC, später migrierbar  
**Alternative:** Eigenes Repository (für Produktion empfohlen)

### 2026-01-11: Gradle Multi-Project Build
**Entscheidung:** Gradle Multi-Project mit eigenem build.gradle  
**Begründung:** Klare Trennung, unabhängig startbar, IDE-Support  
**Alternative:** Separate Gradle-Installation

### 2026-01-11: Port 8081
**Entscheidung:** Party Service läuft auf Port 8081  
**Begründung:** Parallelbetrieb mit OFBiz (Port 8080)  
**Alternative:** Anderer Port bei Konflikt

---

## Probleme und Lösungen

### Problem: Leere Verzeichnisse nicht in Git
**Datum:** 2026-01-11  
**Lösung:** `.gitkeep` Dateien in allen leeren Verzeichnissen erstellt  
**Status:** ✅ Gelöst

---

## Ressourcen

### Dokumentation
- [Monorepo Setup Guide](../../refactor/PARTY_SERVICE_MONOREPO_SETUP.md)
- [Extraction Guide](../../refactor/PARTY_SERVICE_EXTRACTION_GUIDE.md)
- [Communication Analysis](../../refactor/PARTY_SERVICE_COMMUNICATION_ANALYSIS.md)
- [Impact Analysis](../../refactor/PARTY_SERVICE_IMPACT_ANALYSIS.md)

### Externe Links
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Gradle Multi-Project](https://docs.gradle.org/current/userguide/multi_project_builds.html)
- [Testcontainers](https://www.testcontainers.org/)

---

## Team

- **Entwickler:** TBD
- **Reviewer:** TBD
- **Product Owner:** TBD

---

**Hinweis:** Dieses Dokument wird bei jedem Fortschritt aktualisiert.
