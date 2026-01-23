# Woche 2: Datenmodell & API-Design - Implementierungsguide

**Zeitraum:** Woche 2 (5 Arbeitstage)  
**Ziel:** Datenmodell definieren, PostgreSQL-Schema erstellen, API-Design abschließen  
**Status:** 🔄 Bereit zur Implementierung

---

## Übersicht

Woche 2 legt das Fundament für die gesamte Party-Service-Implementierung:
- **Datenmodell:** Identifikation und Modellierung der ~30 Party-Tabellen aus OFBiz
- **Datenbankschema:** PostgreSQL-Schema mit Flyway Migrations
- **API-Design:** OpenAPI 3.0 Spezifikation mit 30+ REST Endpoints
- **Event-Schema:** Kafka Event-Definitionen
- **DTOs:** Data Transfer Objects für API-Kommunikation

---

## Tag 1-2: Datenmodell-Analyse (2 Tage)

### Aufgabe 1.1: Party-Tabellen aus OFBiz identifizieren

**Ziel:** Alle relevanten Party-Tabellen aus der OFBiz-Datenbank identifizieren und dokumentieren.

**Vorgehen:**
1. Neo4j-Analyse nutzen, um Entity-Definitionen zu finden
2. OFBiz Entity-XML-Dateien analysieren
3. Tabellenstruktur dokumentieren
4. Beziehungen zwischen Tabellen kartieren

**Erwartetes Ergebnis:**
- Dokumentation aller ~30 Party-Tabellen
- Klare Priorisierung (Kern-Entities vs. erweiterte Entities)
- Verständnis der Datenstruktur

**Prompt:** Siehe [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md) - Prompt 1

---

### Aufgabe 1.2: ER-Diagramm erstellen

**Ziel:** Visuelles Datenmodell für besseres Verständnis und Kommunikation.

**Vorgehen:**
1. Nutze Mermaid-Syntax für das Diagramm
2. Zeige die wichtigsten Entities (Party, Person, PartyGroup, ContactMech, etc.)
3. Zeige Beziehungen mit Kardinalitäten (1:1, 1:N, N:M)
4. Zeige Vererbung (IS-A)

**Erwartetes Ergebnis:**
- Visuelles ER-Diagramm (Mermaid)
- Dokumentation des Datenmodells
- Klares Verständnis der Beziehungen

**Beispiel Mermaid-Syntax:**
```mermaid
erDiagram
    PARTY ||--o{ PERSON : "is-a"
    PARTY ||--o{ PARTY_GROUP : "is-a"
    PARTY ||--o{ PARTY_ROLE : "has"
    PARTY ||--o{ PARTY_CONTACT_MECH : "has"
```

**Prompt:** Siehe [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md) - Prompt 2

---

## Tag 2-3: PostgreSQL-Schema & Migrations (1.5 Tage)

### Aufgabe 2.1: Flyway Migration V1 - Basis-Tabellen

**Ziel:** PostgreSQL-Schema für Party-Basis-Tabellen erstellen.

**Tabellen:**
1. **party** (Basis-Tabelle)
   - party_id VARCHAR(20) PRIMARY KEY
   - party_type_id VARCHAR(20) NOT NULL
   - status_id VARCHAR(20)
   - created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   - created_by VARCHAR(20)
   - last_modified_date TIMESTAMP
   - last_modified_by VARCHAR(20)

2. **person** (extends party)
   - party_id VARCHAR(20) PRIMARY KEY REFERENCES party(party_id)
   - salutation VARCHAR(20)
   - first_name VARCHAR(100)
   - middle_name VARCHAR(100)
   - last_name VARCHAR(100)
   - personal_title VARCHAR(100)
   - suffix VARCHAR(20)
   - nickname VARCHAR(100)
   - gender VARCHAR(1)
   - birth_date DATE
   - marital_status VARCHAR(1)

3. **party_group** (extends party)
   - party_id VARCHAR(20) PRIMARY KEY REFERENCES party(party_id)
   - group_name VARCHAR(100) NOT NULL
   - group_name_local VARCHAR(100)
   - office_site_name VARCHAR(100)
   - annual_revenue DECIMAL(18,2)
   - num_employees BIGINT
   - ticker_symbol VARCHAR(10)
   - comments TEXT

**Best Practices:**
- Nutze VARCHAR statt CHAR für Flexibilität
- Füge created_date/last_modified_date für Auditing hinzu
- Nutze REFERENCES für Foreign Keys
- Füge SQL-Kommentare für komplexe Felder hinzu

**Datei:** `src/main/resources/db/migration/V1__create_party_base_tables.sql`

**Prompt:** Siehe [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md) - Prompt 3

---

### Aufgabe 2.2: Flyway Migration V2 - ContactMech-Tabellen

**Ziel:** PostgreSQL-Schema für Kontaktmechanismen erstellen.

**Tabellen:**
1. **contact_mech** (Basis-Tabelle)
   - contact_mech_id VARCHAR(20) PRIMARY KEY
   - contact_mech_type_id VARCHAR(20) NOT NULL
   - info_string VARCHAR(255)

2. **postal_address** (extends contact_mech)
   - contact_mech_id VARCHAR(20) PRIMARY KEY REFERENCES contact_mech(contact_mech_id)
   - to_name VARCHAR(100)
   - attn_name VARCHAR(100)
   - address1 VARCHAR(255) NOT NULL
   - address2 VARCHAR(255)
   - city VARCHAR(100)
   - state_province_geo_id VARCHAR(20)
   - postal_code VARCHAR(20)
   - country_geo_id VARCHAR(20)
   - latitude DECIMAL(10,6)
   - longitude DECIMAL(10,6)

3. **telecom_number** (extends contact_mech)
   - contact_mech_id VARCHAR(20) PRIMARY KEY REFERENCES contact_mech(contact_mech_id)
   - country_code VARCHAR(3)
   - area_code VARCHAR(3)
   - contact_number VARCHAR(15) NOT NULL
   - extension VARCHAR(10)

4. **party_contact_mech** (Junction Table)
   - party_id VARCHAR(20) REFERENCES party(party_id)
   - contact_mech_id VARCHAR(20) REFERENCES contact_mech(contact_mech_id)
   - from_date TIMESTAMP NOT NULL
   - thru_date TIMESTAMP
   - role_type_id VARCHAR(20)
   - allow_solicitation VARCHAR(1)
   - extension VARCHAR(10)
   - comments TEXT
   - PRIMARY KEY (party_id, contact_mech_id, from_date)

**Datei:** `src/main/resources/db/migration/V2__create_contact_mech_tables.sql`

**Prompt:** Siehe [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md) - Prompt 4

---

### Aufgabe 2.3: Flyway Migration V3 - Rollen & Beziehungen

**Ziel:** PostgreSQL-Schema für Party-Rollen und Beziehungen erstellen.

**Tabellen:**
1. **party_role**
   - party_id VARCHAR(20) REFERENCES party(party_id)
   - role_type_id VARCHAR(20) NOT NULL
   - from_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   - thru_date TIMESTAMP
   - PRIMARY KEY (party_id, role_type_id)

2. **party_relationship**
   - party_id_from VARCHAR(20) REFERENCES party(party_id)
   - party_id_to VARCHAR(20) REFERENCES party(party_id)
   - role_type_id_from VARCHAR(20)
   - role_type_id_to VARCHAR(20)
   - party_relationship_type_id VARCHAR(20) NOT NULL
   - from_date TIMESTAMP NOT NULL
   - thru_date TIMESTAMP
   - status_id VARCHAR(20)
   - relationship_name VARCHAR(100)
   - security_group_id VARCHAR(20)
   - priority_type_id VARCHAR(20)
   - comments TEXT
   - PRIMARY KEY (party_id_from, party_id_to, role_type_id_from, role_type_id_to, from_date)

**Datei:** `src/main/resources/db/migration/V3__create_role_relationship_tables.sql`

**Prompt:** Siehe [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md) - Prompt 5

---

### Aufgabe 2.4: Flyway Migration V4 - Indizes

**Ziel:** Performance-Indizes für häufige Queries erstellen.

**Indizes:**

**Party-Suche:**
- idx_party_type ON party(party_type_id)
- idx_party_status ON party(status_id)
- idx_person_name ON person(last_name, first_name)
- idx_party_group_name ON party_group(group_name)

**ContactMech-Suche:**
- idx_contact_mech_type ON contact_mech(contact_mech_type_id)
- idx_postal_address_city ON postal_address(city)
- idx_postal_address_postal_code ON postal_address(postal_code)
- idx_telecom_number ON telecom_number(contact_number)

**Beziehungen:**
- idx_party_contact_mech_party ON party_contact_mech(party_id)
- idx_party_contact_mech_contact ON party_contact_mech(contact_mech_id)
- idx_party_role_party ON party_role(party_id)
- idx_party_role_type ON party_role(role_type_id)
- idx_party_relationship_from ON party_relationship(party_id_from)
- idx_party_relationship_to ON party_relationship(party_id_to)

**Zeitbasierte Queries:**
- idx_party_created_date ON party(created_date)
- idx_party_contact_mech_dates ON party_contact_mech(from_date, thru_date)

**Datei:** `src/main/resources/db/migration/V4__create_indexes.sql`

**Prompt:** Siehe [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md) - Prompt 6

**Erwartetes Ergebnis:**
- 4 Flyway Migration-Dateien
- Vollständiges PostgreSQL-Schema
- Performance-Indizes für häufige Queries

---

## Tag 3-4: API-Design (1.5 Tage)

### Aufgabe 3.1: OpenAPI Spezifikation - Party Management

**Ziel:** REST API-Spezifikation für Party Management erstellen.

**Endpoints (8):**
- GET    /parties              - Liste aller Parties (mit Pagination)
- GET    /parties/{id}         - Party Details
- POST   /parties              - Neue Party erstellen
- PUT    /parties/{id}         - Party aktualisieren
- DELETE /parties/{id}         - Party löschen
- GET    /parties/search       - Party suchen (Query-Parameter)
- GET    /parties/{id}/roles   - Party Rollen
- POST   /parties/{id}/roles   - Rolle zuweisen

**Schemas:**
- PartyDTO (mit discriminator für Person/PartyGroup)
- PersonDTO (extends PartyDTO)
- PartyGroupDTO (extends PartyDTO)
- PartyRoleDTO
- PagedResponse<T>
- ErrorResponse

**Security:**
- Bearer Token (JWT)

**Best Practices:**
- Nutze $ref für wiederverwendbare Schemas
- Füge description für alle Felder hinzu
- Definiere Validierungsregeln (required, minLength, pattern)
- Füge Request/Response-Beispiele hinzu

**Datei:** `src/main/resources/openapi/party-service-api.yaml`

**Prompt:** Siehe [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md) - Prompt 7

---

### Aufgabe 3.2: OpenAPI Spezifikation - Contact Management

**Ziel:** REST API-Spezifikation für Contact Management erweitern.

**Endpoints (8):**
- GET    /parties/{id}/contacts           - Alle Kontakte
- POST   /parties/{id}/contacts           - Kontakt hinzufügen
- PUT    /parties/{id}/contacts/{cid}     - Kontakt aktualisieren
- DELETE /parties/{id}/contacts/{cid}     - Kontakt löschen
- GET    /parties/{id}/addresses          - Adressen
- GET    /parties/{id}/phones             - Telefonnummern
- GET    /parties/{id}/emails             - E-Mail-Adressen
- POST   /parties/{id}/contacts/validate  - Kontakt validieren

**Schemas:**
- ContactMechDTO (mit discriminator)
- PostalAddressDTO (extends ContactMechDTO)
- TelecomNumberDTO (extends ContactMechDTO)
- EmailAddressDTO (extends ContactMechDTO)
- PartyContactMechDTO

**Validierungsregeln:**
- postalCode: pattern für PLZ (z.B. `^\d{5}(-\d{4})?$`)
- contactNumber: pattern für Telefonnummern
- email: format email

**Prompt:** Siehe [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md) - Prompt 8

---

### Aufgabe 3.3: OpenAPI Spezifikation - Relationships & Batch

**Ziel:** REST API-Spezifikation für Relationships und Batch-Operationen erweitern.

**Party Relationships Endpoints (6):**
- GET    /parties/{id}/relationships      - Beziehungen
- POST   /parties/{id}/relationships      - Beziehung erstellen
- DELETE /parties/{id}/relationships/{rid} - Beziehung löschen
- GET    /relationships/types             - Beziehungstypen
- GET    /parties/{id}/children           - Untergeordnete Parties
- GET    /parties/{id}/parents            - Übergeordnete Parties

**Person & PartyGroup Endpoints (4):**
- POST   /persons              - Person erstellen
- PUT    /persons/{id}         - Person aktualisieren
- POST   /party-groups         - PartyGroup erstellen
- PUT    /party-groups/{id}    - PartyGroup aktualisieren

**Batch Operations Endpoints (4):**
- POST   /parties/batch        - Mehrere Parties erstellen
- PUT    /parties/batch        - Mehrere Parties aktualisieren
- POST   /parties/import       - Parties importieren (CSV/JSON)
- GET    /parties/export       - Parties exportieren (CSV/JSON)

**Schemas:**
- PartyRelationshipDTO
- RelationshipTypeDTO
- BatchCreateRequest
- BatchUpdateRequest
- ImportRequest
- ExportResponse

**Gesamt:** 30+ Endpoints

**Prompt:** Siehe [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md) - Prompt 9

**Erwartetes Ergebnis:**
- Vollständige OpenAPI 3.0 Spezifikation
- 30+ REST Endpoints dokumentiert
- Alle Schemas definiert
- Request/Response-Beispiele

---

### Aufgabe 3.4: Basis-DTOs erstellen

**Ziel:** Java DTOs für API-Kommunikation erstellen.

**DTOs (7 Klassen):**
1. PartyDTO.java (Abstract Base)
2. PersonDTO.java (extends PartyDTO)
3. PartyGroupDTO.java (extends PartyDTO)
4. ContactMechDTO.java (Abstract Base)
5. PostalAddressDTO.java (extends ContactMechDTO)
6. TelecomNumberDTO.java (extends ContactMechDTO)
7. EmailAddressDTO.java (extends ContactMechDTO)

**Technologien:**
- Lombok (@Data, @SuperBuilder, @NoArgsConstructor, @AllArgsConstructor)
- Bean Validation (@NotNull, @Size, @NotBlank)
- Jackson (@JsonTypeInfo, @JsonSubTypes für Polymorphismus)
- JavaDoc für alle Klassen

**Beispiel PartyDTO:**
```java
package org.apache.ofbiz.party.microservice.application.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

/**
 * Base DTO for Party entities.
 * Uses Jackson polymorphism for Person and PartyGroup subtypes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "partyType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PersonDTO.class, name = "PERSON"),
    @JsonSubTypes.Type(value = PartyGroupDTO.class, name = "PARTY_GROUP")
})
public abstract class PartyDTO {
    
    @Size(max = 20)
    private String partyId;
    
    @NotBlank
    @Size(max = 20)
    private String partyType;
    
    @Size(max = 20)
    private String statusId;
    
    private LocalDateTime createdDate;
    
    @Size(max = 20)
    private String createdBy;
    
    private LocalDateTime lastModifiedDate;
    
    @Size(max = 20)
    private String lastModifiedBy;
}
```

**Verzeichnis:** `src/main/java/org/apache/ofbiz/party/microservice/application/dto/`

**Prompt:** Siehe [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md) - Prompt 10

---

### Aufgabe 3.5: Weitere DTOs erstellen

**Ziel:** DTOs für Rollen, Beziehungen und Responses erstellen.

**DTOs (5 Klassen):**
1. PartyRoleDTO.java
2. PartyRelationshipDTO.java
3. PartyContactMechDTO.java
4. PagedResponseDTO.java (Generic)
5. ErrorResponseDTO.java

**Beispiel PagedResponseDTO:**
```java
package org.apache.ofbiz.party.microservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Generic DTO for paginated responses.
 *
 * @param <T> Type of content
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponseDTO<T> {
    
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
}
```

**Verzeichnis:** `src/main/java/org/apache/ofbiz/party/microservice/application/dto/`

**Prompt:** Siehe [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md) - Prompt 11

**Erwartetes Ergebnis:**
- ~12 DTO-Klassen
- Vollständige Bean Validation
- Jackson-Polymorphismus für Vererbung
- Lombok für Boilerplate-Reduktion

---

## Tag 4-5: Event-Schema & Test-Daten (1 Tag)

### Aufgabe 4.1: Kafka Event-DTOs erstellen

**Ziel:** Event-DTOs für Kafka-Publishing erstellen.

**Events (8 Klassen):**
1. PartyCreatedEvent.java
2. PartyUpdatedEvent.java
3. PartyDeletedEvent.java
4. PartyRoleAssignedEvent.java
5. PartyRoleRemovedEvent.java
6. ContactMechAddedEvent.java
7. ContactMechUpdatedEvent.java
8. PartyRelationshipCreatedEvent.java

**Basis-Struktur:**
```java
package org.apache.ofbiz.party.microservice.infrastructure.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Event published when a new Party is created.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyCreatedEvent {
    
    private String eventId;           // UUID für Idempotenz
    private String partyId;
    private String partyType;
    private LocalDateTime timestamp;
    private String createdBy;
    private String payload;           // Optional: JSON string für vollständige Daten
}
```

**Best Practices:**
- Füge eventId (UUID) hinzu für Idempotenz
- Füge timestamp hinzu
- Füge correlation-ID hinzu für Tracing
- Halte Events klein (nur IDs + wichtige Felder)
- Optional: Füge payload für vollständige Daten hinzu

**Verzeichnis:** `src/main/java/org/apache/ofbiz/party/microservice/infrastructure/messaging/event/`

**Prompt:** Siehe [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md) - Prompt 12

---

### Aufgabe 4.2: Kafka Topics Konfiguration

**Ziel:** Kafka Topics in application.yml definieren.

**Topics:**
- party.created
- party.updated
- party.deleted
- party.role.assigned
- party.role.removed
- party.contact.added
- party.contact.updated
- party.relationship.created

**Konfiguration:**
```yaml
party:
  kafka:
    topics:
      party-created: "party.created"
      party-updated: "party.updated"
      party-deleted: "party.deleted"
      party-role-assigned: "party.role.assigned"
      party-role-removed: "party.role.removed"
      contact-added: "party.contact.added"
      contact-updated: "party.contact.updated"
      relationship-created: "party.relationship.created"
    
    # Topic-Konfiguration
    partitions: 3
    replication-factor: 1
    retention-ms: 604800000  # 7 Tage
```

**Dokumentation:**
Erstelle `docs/KAFKA_EVENTS.md` mit:
- Event-Name
- Topic
- Payload-Struktur
- Wann wird es publiziert?
- Wer konsumiert es?
- Beispiel-Payload (JSON)

**Datei:** `src/main/resources/application.yml`

**Prompt:** Siehe [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md) - Prompt 13

**Erwartetes Ergebnis:**
- 8 Event-DTO-Klassen
- Kafka Topics konfiguriert
- Event-Dokumentation

---

### Aufgabe 4.3: Test-Daten erstellen

**Ziel:** Test-Daten für Entwicklung und Tests erstellen.

**Test-Daten:**
1. 5 Personen (verschiedene Szenarien: aktiv, inaktiv, verschiedene Geschlechter)
2. 3 PartyGroups (Firmen mit verschiedenen Größen)
3. 10 Kontaktmechanismen:
   - 4 Adressen (verschiedene Länder)
   - 3 Telefonnummern (verschiedene Formate)
   - 3 E-Mail-Adressen
4. 5 Party-Rollen (verschiedene Rollen-Typen)
5. 3 Party-Beziehungen (verschiedene Beziehungstypen)

**Beispiel SQL:**
```sql
-- Test Persons
INSERT INTO party (party_id, party_type_id, status_id, created_date, created_by)
VALUES 
    ('PERSON_001', 'PERSON', 'PARTY_ENABLED', CURRENT_TIMESTAMP, 'SYSTEM'),
    ('PERSON_002', 'PERSON', 'PARTY_ENABLED', CURRENT_TIMESTAMP, 'SYSTEM'),
    ('PERSON_003', 'PERSON', 'PARTY_DISABLED', CURRENT_TIMESTAMP, 'SYSTEM');

INSERT INTO person (party_id, first_name, last_name, gender, birth_date)
VALUES 
    ('PERSON_001', 'John', 'Doe', 'M', '1980-01-15'),
    ('PERSON_002', 'Jane', 'Smith', 'F', '1985-05-20'),
    ('PERSON_003', 'Bob', 'Johnson', 'M', '1990-12-10');
```

**Hinweis:** Diese Migration sollte nur im dev-Profil laufen!

**Konfiguration in application-dev.yml:**
```yaml
spring:
  flyway:
    locations: classpath:db/migration
```

**Datei:** `src/main/resources/db/migration/V99__test_data.sql`

**Prompt:** Siehe [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md) - Prompt 14

**Erwartetes Ergebnis:**
- Test-Daten für Entwicklung
- Realistische Szenarien abgedeckt
- Nur im dev-Profil aktiv

---

## Zusammenfassung & Deliverables

### Deliverables Woche 2

**Dokumentation (4 Dateien):**
- ✅ PARTY_ENTITIES_ANALYSIS.md - Alle ~30 Tabellen dokumentiert
- ✅ PARTY_ER_DIAGRAM.md - ER-Diagramm (Mermaid)
- ✅ PARTY_DATA_MODEL.md - Datenmodell-Beschreibung
- ✅ KAFKA_EVENTS.md - Event-Dokumentation

**Datenbank (5 SQL-Dateien):**
- ✅ V1__create_party_base_tables.sql
- ✅ V2__create_contact_mech_tables.sql
- ✅ V3__create_role_relationship_tables.sql
- ✅ V4__create_indexes.sql
- ✅ V99__test_data.sql (dev only)

**API-Design (1 YAML + ~20 Java-Klassen):**
- ✅ party-service-api.yaml - OpenAPI 3.0 Spezifikation (30+ Endpoints)
- ✅ ~12 DTO-Klassen (PartyDTO, PersonDTO, ContactMechDTO, etc.)
- ✅ 8 Event-DTO-Klassen

**Gesamt:**
- ~30 Dateien erstellt
- ~2000 Zeilen Code/SQL/YAML
- Vollständiges Datenmodell
- Vollständige API-Spezifikation

---

## Validierung & Tests

### Validierung nach Woche 2

**1. Datenbank-Schema testen:**
```bash
# Spring Boot starten (dev-Profil)
./gradlew :microservices:party-service:bootRun

# Flyway sollte alle Migrations ausführen
# H2 Console öffnen: http://localhost:8081/api/party/h2-console
# Tabellen prüfen: SELECT * FROM party;
```

**2. OpenAPI-Spezifikation validieren:**
```bash
# Swagger UI öffnen
# http://localhost:8081/api/party/swagger-ui.html

# Prüfe:
# - Alle 30+ Endpoints sichtbar
# - Schemas korrekt
# - Beispiele vorhanden
```

**3. DTOs kompilieren:**
```bash
# Build durchführen
./gradlew :microservices:party-service:build

# Sollte ohne Fehler durchlaufen
```

**4. Test-Daten prüfen:**
```sql
-- In H2 Console
SELECT COUNT(*) FROM party;          -- Sollte 8 sein (5 Personen + 3 Firmen)
SELECT COUNT(*) FROM person;         -- Sollte 5 sein
SELECT COUNT(*) FROM party_group;    -- Sollte 3 sein
SELECT COUNT(*) FROM postal_address; -- Sollte 4 sein
```

**Prompt:** Siehe [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md) - Prompt 15

---

## Nächste Schritte (Woche 3)

Nach Abschluss von Woche 2 folgt Woche 3:
- JPA Entities implementieren (basierend auf Flyway Migrations)
- Repositories erstellen (Spring Data JPA)
- Core Services implementieren (PartyService, ContactMechService)
- Unit Tests schreiben (>80% Coverage)

**Vorbereitung:**
- Datenmodell verstanden ✅
- API-Design abgeschlossen ✅
- Bereit für Implementierung ✅

---

## Tipps & Best Practices

### Datenmodell
- ✅ Halte dich an OFBiz-Namenskonventionen (party_id, nicht partyId)
- ✅ Nutze VARCHAR statt CHAR für Flexibilität
- ✅ Füge Auditing-Felder hinzu (created_date, last_modified_date)
- ✅ Nutze Foreign Keys für Referential Integrity

### API-Design
- ✅ Nutze REST-Best-Practices (GET/POST/PUT/DELETE)
- ✅ Nutze Pagination für Listen (page, size)
- ✅ Nutze HTTP-Status-Codes korrekt (200, 201, 400, 404, 500)
- ✅ Füge Validierung hinzu (Bean Validation)

### Events
- ✅ Halte Events klein (nur IDs + wichtige Felder)
- ✅ Füge eventId für Idempotenz hinzu
- ✅ Füge timestamp hinzu
- ✅ Dokumentiere Event-Schema

### Test-Daten
- ✅ Erstelle realistische Szenarien
- ✅ Decke Edge-Cases ab (null-Werte, leere Strings)
- ✅ Nur im dev-Profil aktiv

---

## Zeitplan Woche 2

| Tag | Aufgaben | Stunden | Status |
|-----|----------|---------|--------|
| **Tag 1** | Neo4j-Analyse, Entity-Dokumentation | 8h | 🔄 |
| **Tag 2** | ER-Diagramm, Flyway V1-V2 | 8h | 🔄 |
| **Tag 3** | Flyway V3-V4, OpenAPI Party/Contact | 8h | 🔄 |
| **Tag 4** | OpenAPI Relationships/Batch, DTOs | 8h | 🔄 |
| **Tag 5** | Event-Schema, Test-Daten, Validierung | 8h | 🔄 |
| **Gesamt** | | **40h** | **0%** |

---

## Fragen & Antworten

**Q: Warum Flyway statt Liquibase?**  
A: Flyway ist einfacher, SQL-basiert, und gut für PostgreSQL geeignet.

**Q: Warum OpenAPI 3.0 statt Swagger 2.0?**  
A: OpenAPI 3.0 ist der aktuelle Standard, besser strukturiert, mehr Features.

**Q: Warum DTOs statt direkt Entities?**  
A: DTOs entkoppeln API von Datenmodell, ermöglichen Versionierung, bessere Kontrolle.

**Q: Warum Kafka Events?**  
A: Asynchrone Kommunikation, Entkopplung, Event Sourcing, Audit-Trail.

**Q: Wie viele Tabellen werden migriert?**  
A: ~30 Tabellen aus OFBiz Party-Modul, priorisiert nach Wichtigkeit.

---

**Erstellt:** 2026-01-21  
**Aktualisiert:** 2026-01-23  
**Autor:** Roo AI (Party-PoC Mode)  
**Version:** 2.0  
**Status:** Bereit zur Implementierung
