# Party-Service PoC - Minimaler Ansatz

**Ziel:** Schneller PoC mit einer einzigen Funktion auf der bestehenden OFBiz-Derby-Datenbank

## Gewählte Funktion

**`GET /api/v1/parties/{id}`** - Party mit Kontakten abrufen (Read-Only)

### Warum diese Funktion?

✅ **Read-Only** - Kein Risiko für Dateninkonsistenz  
✅ **Einfach** - Keine komplexe Business-Logik  
✅ **Repräsentativ** - Zeigt Datenbankzugriff, DTOs, REST-API  
✅ **Testbar** - Mit existierenden Demo-Daten aus OFBiz  

## Technischer Ansatz

### 1. Datenbankzugriff

**OFBiz nutzt Derby (embedded):**
- JDBC URL: `jdbc:derby:ofbiz`
- Schema: `OFBIZ`
- User: `ofbiz`
- Tabellen: `PARTY`, `PERSON`, `PARTY_GROUP`, `CONTACT_MECH`, `POSTAL_ADDRESS`, etc.

**Spring Boot Konfiguration:**
```yaml
spring:
  datasource:
    url: jdbc:derby:${OFBIZ_HOME}/ofbiz;create=false
    username: ofbiz
    password: ${DERBY_PASSWORD}
    driver-class-name: org.apache.derby.jdbc.EmbeddedDriver
  jpa:
    database-platform: org.hibernate.dialect.DerbyDialect
    hibernate:
      ddl-auto: none  # WICHTIG: Keine Schema-Änderungen!
```

### 2. Minimale Implementierung

**Benötigte Komponenten:**

```
party-service/
├── src/main/java/
│   └── org/apache/ofbiz/party/
│       ├── adapter/
│       │   └── rest/
│       │       ├── PartyController.java          # REST Endpoint
│       │       └── dto/
│       │           ├── PartyResponse.java        # Response DTO
│       │           ├── PersonResponse.java
│       │           ├── PartyGroupResponse.java
│       │           └── ContactMechResponse.java
│       ├── application/
│       │   └── service/
│       │       └── PartyQueryService.java        # Business Logic
│       └── infrastructure/
│           └── persistence/
│               ├── entity/
│               │   ├── PartyEntity.java          # JPA Entity
│               │   ├── PersonEntity.java
│               │   ├── PartyGroupEntity.java
│               │   └── ContactMechEntity.java
│               └── repository/
│                   └── PartyRepository.java      # Spring Data JPA
└── src/main/resources/
    ├── application.yml                           # Konfiguration
    └── application-dev.yml                       # Dev-Profile mit Derby
```

### 3. API-Spezifikation

**Endpoint:**
```
GET /api/v1/parties/{partyId}
```

**Response (Beispiel):**
```json
{
  "partyId": "10000",
  "partyType": "PERSON",
  "statusId": "PARTY_ENABLED",
  "person": {
    "firstName": "John",
    "lastName": "Doe",
    "gender": "M"
  },
  "contacts": [
    {
      "contactMechId": "10001",
      "contactMechType": "POSTAL_ADDRESS",
      "postalAddress": {
        "address1": "123 Main St",
        "city": "Springfield",
        "postalCode": "12345",
        "countryGeoId": "USA"
      }
    },
    {
      "contactMechId": "10002",
      "contactMechType": "TELECOM_NUMBER",
      "telecomNumber": {
        "countryCode": "1",
        "areaCode": "555",
        "contactNumber": "1234567"
      }
    }
  ]
}
```

**Error Responses:**
- `404 Not Found` - Party existiert nicht
- `500 Internal Server Error` - Datenbankfehler

## Implementierungsschritte

### Phase 1: Setup (30 Min)
1. ✅ Spring Boot Projekt ist bereits vorhanden
2. Derby JDBC Driver zu `build.gradle` hinzufügen
3. `application-dev.yml` mit Derby-Konfiguration erstellen
4. Verbindung testen mit einfachem Query

### Phase 2: Entities & Repository (45 Min)
5. JPA Entities für PARTY, PERSON, PARTY_GROUP erstellen
6. JPA Entity für CONTACT_MECH, POSTAL_ADDRESS, TELECOM_NUMBER
7. PartyRepository mit Spring Data JPA
8. Integration-Test für Repository

### Phase 3: Service & Controller (30 Min)
9. PartyQueryService implementieren
10. DTOs (Response-Objekte) erstellen
11. PartyController mit GET-Endpoint
12. Unit-Tests für Service

### Phase 4: Testing (30 Min)
13. Integration-Test mit Testcontainers (Derby)
14. Manueller Test gegen OFBiz-Demo-Daten
15. Dokumentation

**Geschätzte Gesamtzeit:** ~2-3 Stunden

## Testdaten

OFBiz Demo-Daten enthalten bereits Parties:
- `DemoCustomer` - Demo-Kunde
- `DemoEmployee1` - Demo-Mitarbeiter
- `Company` - Demo-Firma

Diese können direkt für Tests verwendet werden.

## Nächste Schritte nach PoC

Nach erfolgreichem PoC:
1. Weitere Read-Operationen (Search, List)
2. Write-Operationen (Create, Update)
3. Event-Publishing (Kafka)
4. Migration zu eigener PostgreSQL-DB
5. Anti-Corruption Layer für OFBiz-Integration

## Offene Fragen

- [ ] Wo liegt die OFBiz Derby-Datenbank? (Pfad ermitteln)
- [ ] Welche Demo-Daten sind vorhanden? (Query ausführen)
- [ ] Ist OFBiz aktuell laufend? (Port-Konflikt vermeiden)

## Entscheidungen

| Entscheidung | Begründung |
|--------------|------------|
| Derby statt PostgreSQL | Schneller Start, keine zusätzliche DB-Installation |
| Read-Only Operation | Kein Risiko für Dateninkonsistenz |
| Keine Flyway Migrations | Nutzt existierendes OFBiz-Schema |
| Minimale DTOs | Nur notwendige Felder für PoC |
| Spring Data JPA | Standard, weniger Boilerplate |

## Erfolgs-Kriterien

✅ Service startet erfolgreich  
✅ Verbindung zu Derby funktioniert  
✅ GET /api/v1/parties/{id} liefert korrekte Daten  
✅ Tests sind grün  
✅ Dokumentation ist vorhanden  
