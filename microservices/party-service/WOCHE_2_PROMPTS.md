# Woche 2: Konkrete Prompts für die Implementierung

**Verwendung:** Kopiere diese Prompts und gib sie nacheinander an Roo (Party-PoC Mode).

---

## 🎯 Tag 1-2: Datenmodell-Analyse

### Prompt 1: Neo4j-Analyse für Party-Entities
```
Analysiere die OFBiz Party-Entities mit Neo4j:

1. Nutze den Neo4j-MCP-Server, um alle Entity-Definitionen im Party-Modul zu finden
2. Suche nach Klassen in org.apache.ofbiz.party
3. Identifiziere alle Tabellen-Definitionen (XML-basiert)
4. Erstelle eine Liste mit:
   - Tabellenname
   - Primärschlüssel
   - Wichtige Felder
   - Beziehungen zu anderen Tabellen
   - Priorität (Kern vs. erweitert)

5. Priorisiere die Kern-Entities:
   - Party (Basis-Entity)
   - Person, PartyGroup
   - ContactMech, PostalAddress, TelecomNumber, EmailAddress
   - PartyRole, PartyRelationship
   - PartyContactMech

Dokumentiere das Ergebnis in:
microservices/party-service/docs/PARTY_ENTITIES_ANALYSIS.md

Format als Markdown-Tabelle:
| Tabelle | Typ | Primärschlüssel | Wichtige Felder | Beziehungen | Priorität |
```

---

### Prompt 2: ER-Diagramm erstellen
```
Erstelle ein ER-Diagramm für das Party-Datenmodell mit Mermaid:

1. Zeige die wichtigsten Entities:
   - Party (Abstract)
   - Person, PartyGroup (extends Party)
   - ContactMech (Abstract)
   - PostalAddress, TelecomNumber, EmailAddress (extends ContactMech)
   - PartyRole
   - PartyRelationship
   - PartyContactMech (Junction Table)

2. Zeige Beziehungen mit Kardinalitäten:
   - 1:1, 1:N, N:M
   - Vererbung (IS-A)
   - Assoziationen

3. Erstelle zwei Dateien:
   - microservices/party-service/docs/PARTY_ER_DIAGRAM.md (Mermaid-Code)
   - microservices/party-service/docs/PARTY_DATA_MODEL.md (Textuelle Beschreibung)

Nutze Mermaid erDiagram-Syntax.
```

---

## 🗄️ Tag 2-3: PostgreSQL-Schema & Migrations

### Prompt 3: Flyway Migration V1 - Basis-Tabellen
```
Erstelle die erste Flyway Migration für Party-Basis-Tabellen:

Datei: src/main/resources/db/migration/V1__create_party_base_tables.sql

Erstelle folgende Tabellen:
1. party (Basis-Tabelle)
   - party_id VARCHAR(20) PRIMARY KEY
   - party_type_id VARCHAR(20) NOT NULL
   - status_id VARCHAR(20)
   - created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   - created_by VARCHAR(20)
   - last_modified_date TIMESTAMP
   - last_modified_by VARCHAR(20)

2. person (extends party)
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

3. party_group (extends party)
   - party_id VARCHAR(20) PRIMARY KEY REFERENCES party(party_id)
   - group_name VARCHAR(100) NOT NULL
   - group_name_local VARCHAR(100)
   - office_site_name VARCHAR(100)
   - annual_revenue DECIMAL(18,2)
   - num_employees BIGINT
   - ticker_symbol VARCHAR(10)
   - comments TEXT

Best Practices:
- Nutze VARCHAR statt CHAR
- Füge Auditing-Felder hinzu
- Nutze REFERENCES für Foreign Keys
- Füge SQL-Kommentare hinzu
```

---

### Prompt 4: Flyway Migration V2 - ContactMech-Tabellen
```
Erstelle die zweite Flyway Migration für ContactMech-Tabellen:

Datei: src/main/resources/db/migration/V2__create_contact_mech_tables.sql

Erstelle folgende Tabellen:
1. contact_mech (Basis-Tabelle)
   - contact_mech_id VARCHAR(20) PRIMARY KEY
   - contact_mech_type_id VARCHAR(20) NOT NULL
   - info_string VARCHAR(255)

2. postal_address (extends contact_mech)
   - contact_mech_id VARCHAR(20) PRIMARY KEY REFERENCES contact_mech
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

3. telecom_number (extends contact_mech)
   - contact_mech_id VARCHAR(20) PRIMARY KEY REFERENCES contact_mech
   - country_code VARCHAR(3)
   - area_code VARCHAR(3)
   - contact_number VARCHAR(15) NOT NULL
   - extension VARCHAR(10)

4. party_contact_mech (Junction Table)
   - party_id VARCHAR(20) REFERENCES party(party_id)
   - contact_mech_id VARCHAR(20) REFERENCES contact_mech(contact_mech_id)
   - from_date TIMESTAMP NOT NULL
   - thru_date TIMESTAMP
   - role_type_id VARCHAR(20)
   - allow_solicitation VARCHAR(1)
   - extension VARCHAR(10)
   - comments TEXT
   - PRIMARY KEY (party_id, contact_mech_id, from_date)
```

---

### Prompt 5: Flyway Migration V3 - Rollen & Beziehungen
```
Erstelle die dritte Flyway Migration für Rollen und Beziehungen:

Datei: src/main/resources/db/migration/V3__create_role_relationship_tables.sql

Erstelle folgende Tabellen:
1. party_role
   - party_id VARCHAR(20) REFERENCES party(party_id)
   - role_type_id VARCHAR(20) NOT NULL
   - from_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   - thru_date TIMESTAMP
   - PRIMARY KEY (party_id, role_type_id)

2. party_relationship
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
```

---

### Prompt 6: Flyway Migration V4 - Indizes
```
Erstelle die vierte Flyway Migration für Performance-Indizes:

Datei: src/main/resources/db/migration/V4__create_indexes.sql

Erstelle Indizes für häufige Queries:

1. Party-Suche:
   - idx_party_type ON party(party_type_id)
   - idx_party_status ON party(status_id)
   - idx_person_name ON person(last_name, first_name)
   - idx_party_group_name ON party_group(group_name)

2. ContactMech-Suche:
   - idx_contact_mech_type ON contact_mech(contact_mech_type_id)
   - idx_postal_address_city ON postal_address(city)
   - idx_postal_address_postal_code ON postal_address(postal_code)
   - idx_telecom_number ON telecom_number(contact_number)

3. Beziehungen:
   - idx_party_contact_mech_party ON party_contact_mech(party_id)
   - idx_party_contact_mech_contact ON party_contact_mech(contact_mech_id)
   - idx_party_role_party ON party_role(party_id)
   - idx_party_role_type ON party_role(role_type_id)
   - idx_party_relationship_from ON party_relationship(party_id_from)
   - idx_party_relationship_to ON party_relationship(party_id_to)

4. Zeitbasierte Queries:
   - idx_party_created_date ON party(created_date)
   - idx_party_contact_mech_dates ON party_contact_mech(from_date, thru_date)
```

---

## 🔌 Tag 3-4: API-Design

### Prompt 7: OpenAPI Spezifikation - Party Management
```
Erstelle eine OpenAPI 3.0 Spezifikation für den Party Service:

Datei: microservices/party-service/src/main/resources/openapi/party-service-api.yaml

1. Metadata:
   - Title: Party Service API
   - Version: 1.0.0
   - Description: Microservice für Party-Management
   - Base URL: /api/v1

2. Party Management Endpoints (8):
   GET    /parties              - Liste aller Parties (mit Pagination)
   GET    /parties/{id}         - Party Details
   POST   /parties              - Neue Party erstellen
   PUT    /parties/{id}         - Party aktualisieren
   DELETE /parties/{id}         - Party löschen
   GET    /parties/search       - Party suchen (Query-Parameter)
   GET    /parties/{id}/roles   - Party Rollen
   POST   /parties/{id}/roles   - Rolle zuweisen

3. Schemas definieren:
   - PartyDTO (mit discriminator für Person/PartyGroup)
   - PersonDTO (extends PartyDTO)
   - PartyGroupDTO (extends PartyDTO)
   - PartyRoleDTO
   - PagedResponse<T>
   - ErrorResponse

4. Request/Response-Beispiele hinzufügen

5. Security-Schema (Bearer Token):
   securitySchemes:
     bearerAuth:
       type: http
       scheme: bearer
       bearerFormat: JWT

Best Practices:
- Nutze $ref für wiederverwendbare Schemas
- Füge description für alle Felder hinzu
- Definiere Validierungsregeln (required, minLength, pattern)
- Füge Beispiele hinzu
```

---

### Prompt 8: OpenAPI Spezifikation - Contact Management
```
Erweitere die OpenAPI Spezifikation um Contact Management:

Füge hinzu zu: microservices/party-service/src/main/resources/openapi/party-service-api.yaml

Contact Management Endpoints (8):
   GET    /parties/{id}/contacts           - Alle Kontakte
   POST   /parties/{id}/contacts           - Kontakt hinzufügen
   PUT    /parties/{id}/contacts/{cid}     - Kontakt aktualisieren
   DELETE /parties/{id}/contacts/{cid}     - Kontakt löschen
   GET    /parties/{id}/addresses          - Adressen
   GET    /parties/{id}/phones             - Telefonnummern
   GET    /parties/{id}/emails             - E-Mail-Adressen
   POST   /parties/{id}/contacts/validate  - Kontakt validieren

Schemas:
   - ContactMechDTO (mit discriminator)
   - PostalAddressDTO (extends ContactMechDTO)
   - TelecomNumberDTO (extends ContactMechDTO)
   - EmailAddressDTO (extends ContactMechDTO)
   - PartyContactMechDTO

Füge Validierungsregeln hinzu:
- postalCode: pattern für PLZ
- contactNumber: pattern für Telefonnummern
- email: format email
```

---

### Prompt 9: OpenAPI Spezifikation - Relationships & Batch
```
Erweitere die OpenAPI Spezifikation um Relationships und Batch-Operationen:

Füge hinzu zu: microservices/party-service/src/main/resources/openapi/party-service-api.yaml

Party Relationships Endpoints (6):
   GET    /parties/{id}/relationships      - Beziehungen
   POST   /parties/{id}/relationships      - Beziehung erstellen
   DELETE /parties/{id}/relationships/{rid} - Beziehung löschen
   GET    /relationships/types             - Beziehungstypen
   GET    /parties/{id}/children           - Untergeordnete Parties
   GET    /parties/{id}/parents            - Übergeordnete Parties

Person & PartyGroup Endpoints (4):
   POST   /persons              - Person erstellen
   PUT    /persons/{id}         - Person aktualisieren
   POST   /party-groups         - PartyGroup erstellen
   PUT    /party-groups/{id}    - PartyGroup aktualisieren

Batch Operations Endpoints (4):
   POST   /parties/batch        - Mehrere Parties erstellen
   PUT    /parties/batch        - Mehrere Parties aktualisieren
   POST   /parties/import       - Parties importieren (CSV/JSON)
   GET    /parties/export       - Parties exportieren (CSV/JSON)

Schemas:
   - PartyRelationshipDTO
   - RelationshipTypeDTO
   - BatchCreateRequest
   - BatchUpdateRequest
   - ImportRequest
   - ExportResponse

Gesamt: 30+ Endpoints
```

---

### Prompt 10: Basis-DTOs erstellen
```
Erstelle die Basis-DTOs für den Party Service:

Erstelle folgende DTOs in:
src/main/java/org/apache/ofbiz/party/microservice/application/dto/

1. PartyDTO.java (Abstract Base)
   - Nutze Jackson für JSON-Serialisierung (@JsonTypeInfo, @JsonSubTypes)
   - Nutze Bean Validation (@NotNull, @Size, @NotBlank)
   - Nutze Lombok (@Data, @SuperBuilder, @NoArgsConstructor, @AllArgsConstructor)
   - Füge JavaDoc hinzu
   - Felder: partyId, partyType, statusId, createdDate, createdBy, lastModifiedDate, lastModifiedBy

2. PersonDTO.java (extends PartyDTO)
   - Felder: salutation, firstName, middleName, lastName, personalTitle, suffix, nickname, gender, birthDate, maritalStatus

3. PartyGroupDTO.java (extends PartyDTO)
   - Felder: groupName, groupNameLocal, officeSiteName, annualRevenue, numEmployees, tickerSymbol, comments

4. ContactMechDTO.java (Abstract Base)
   - Nutze Jackson Polymorphismus
   - Felder: contactMechId, contactMechType, infoString

5. PostalAddressDTO.java (extends ContactMechDTO)
   - Felder: toName, attnName, address1, address2, city, stateProvinceGeoId, postalCode, countryGeoId, latitude, longitude

6. TelecomNumberDTO.java (extends ContactMechDTO)
   - Felder: countryCode, areaCode, contactNumber, extension

7. EmailAddressDTO.java (extends ContactMechDTO)
   - Felder: emailAddress

Best Practices:
- Nutze Lombok für Boilerplate-Reduktion
- Nutze Bean Validation
- Nutze Jackson Annotations für Polymorphismus
- Füge JavaDoc hinzu
```

---

### Prompt 11: Weitere DTOs erstellen
```
Erstelle weitere DTOs für Rollen, Beziehungen und Responses:

Erstelle in: src/main/java/org/apache/ofbiz/party/microservice/application/dto/

1. PartyRoleDTO.java
   - Felder: partyId, roleTypeId, fromDate, thruDate

2. PartyRelationshipDTO.java
   - Felder: partyIdFrom, partyIdTo, roleTypeIdFrom, roleTypeIdTo, partyRelationshipTypeId, fromDate, thruDate, statusId, relationshipName, securityGroupId, priorityTypeId, comments

3. PartyContactMechDTO.java
   - Felder: partyId, contactMechId, fromDate, thruDate, roleTypeId, allowSolicitation, extension, comments

4. PagedResponseDTO.java (Generic)
   - Felder: content (List<T>), page, size, totalElements, totalPages, first, last

5. ErrorResponseDTO.java
   - Felder: timestamp, status, error, message, path

Nutze Lombok und Bean Validation.
```

---

## 📨 Tag 4-5: Event-Schema & Test-Daten

### Prompt 12: Kafka Event-DTOs erstellen
```
Erstelle Event-DTOs für Kafka-Publishing:

Erstelle in: src/main/java/org/apache/ofbiz/party/microservice/infrastructure/messaging/event/

1. PartyCreatedEvent.java
2. PartyUpdatedEvent.java
3. PartyDeletedEvent.java
4. PartyRoleAssignedEvent.java
5. PartyRoleRemovedEvent.java
6. ContactMechAddedEvent.java
7. ContactMechUpdatedEvent.java
8. PartyRelationshipCreatedEvent.java

Basis-Struktur für alle Events:
- eventId (UUID für Idempotenz)
- partyId (oder relevante ID)
- timestamp (LocalDateTime)
- createdBy (String)
- payload (Optional: JSON string für vollständige Daten)

Best Practices:
- Nutze Lombok (@Data, @Builder)
- Halte Events klein (nur IDs + wichtige Felder)
- Füge correlation-ID für Tracing hinzu
- Füge JavaDoc hinzu
```

---

### Prompt 13: Kafka Topics Konfiguration
```
Definiere Kafka Topics in application.yml:

Füge hinzu zu: src/main/resources/application.yml

Unter dem Abschnitt "party":
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
    
    partitions: 3
    replication-factor: 1
    retention-ms: 604800000  # 7 Tage

Erstelle auch eine Dokumentation:
microservices/party-service/docs/KAFKA_EVENTS.md

Dokumentiere für jedes Event:
- Event-Name
- Topic
- Payload-Struktur
- Wann wird es publiziert?
- Wer konsumiert es?
- Beispiel-Payload (JSON)
```

---

### Prompt 14: Test-Daten Migration erstellen
```
Erstelle eine Flyway Migration mit Test-Daten:

Datei: src/main/resources/db/migration/V99__test_data.sql

Füge Test-Daten hinzu:
1. 5 Personen (verschiedene Szenarien: aktiv, inaktiv, verschiedene Geschlechter)
2. 3 PartyGroups (Firmen mit verschiedenen Größen)
3. 10 Kontaktmechanismen:
   - 4 Adressen (verschiedene Länder)
   - 3 Telefonnummern (verschiedene Formate)
   - 3 E-Mail-Adressen
4. 5 Party-Rollen (verschiedene Rollen-Typen)
5. 3 Party-Beziehungen (verschiedene Beziehungstypen)

Verknüpfe die Daten sinnvoll:
- Personen haben Adressen und Telefonnummern
- PartyGroups haben Geschäftsadressen
- Personen haben Beziehungen zu PartyGroups (Mitarbeiter)

Hinweis: Diese Migration sollte nur im dev-Profil laufen!

Konfiguriere in application-dev.yml:
spring:
  flyway:
    locations: classpath:db/migration
```

---

## ✅ Validierung nach Woche 2

### Prompt 15: Validierung durchführen
```
Führe folgende Validierungen durch:

1. Datenbank-Schema testen:
   - Starte Spring Boot: ./gradlew :microservices:party-service:bootRun
   - Öffne H2 Console: http://localhost:8081/api/party/h2-console
   - Prüfe, ob alle Tabellen erstellt wurden
   - Prüfe, ob Test-Daten vorhanden sind

2. OpenAPI-Spezifikation validieren:
   - Öffne Swagger UI: http://localhost:8081/api/party/swagger-ui.html
   - Prüfe, ob alle 30+ Endpoints sichtbar sind
   - Prüfe, ob Schemas korrekt sind
   - Prüfe, ob Beispiele vorhanden sind

3. DTOs kompilieren:
   - Build durchführen: ./gradlew :microservices:party-service:build
   - Sollte ohne Fehler durchlaufen

4. Test-Daten prüfen:
   - In H2 Console SQL ausführen:
     SELECT COUNT(*) FROM party;
     SELECT COUNT(*) FROM person;
     SELECT COUNT(*) FROM party_group;
     SELECT COUNT(*) FROM postal_address;

Dokumentiere die Ergebnisse in:
microservices/party-service/docs/WOCHE_2_VALIDATION_RESULTS.md
```

---

## 📊 Zusammenfassung

**Gesamt: 15 Prompts**

**Deliverables:**
- ✅ 4 Dokumentations-Dateien (Entities, ER-Diagramm, Datenmodell, Kafka Events)
- ✅ 5 Flyway Migrations (V1-V4 + V99 Test-Daten)
- ✅ 1 OpenAPI 3.0 Spezifikation (30+ Endpoints)
- ✅ ~12 DTO-Klassen
- ✅ 8 Event-DTO-Klassen
- ✅ Kafka Topics Konfiguration

**Zeitaufwand:** 5 Tage (40 Stunden)

**Nächste Schritte:** Woche 3 - JPA Entities, Repositories, Services

---

**Tipp:** Arbeite die Prompts sequenziell ab. Jeder Prompt baut auf dem vorherigen auf.

**Erstellt:** 2026-01-21  
**Version:** 1.0
