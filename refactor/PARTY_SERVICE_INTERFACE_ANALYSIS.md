# Party Service - Schnittstellenanalyse und Extraktionsplan

## Executive Summary

Diese Analyse untersucht die Schnittstellen des Party-Service in OFBiz und entwickelt einen detaillierten Plan für die Extraktion als eigenständigen Microservice. Der Party-Service ist mit **83 Klassen** und **257 Service-Definitionen** der ideale Kandidat für einen Proof-of-Concept der Microservices-Migration.

**Erstellungsdatum:** 17. Januar 2026  
**Datenquelle:** Neo4j-Datenbank mit OFBiz-Code-Analyse via jqAssistant  
**Basis-Analyse:** [`SERVICE_DECOMPOSITION_ANALYSIS.md`](./SERVICE_DECOMPOSITION_ANALYSIS.md)

---

## 1. Schnittstellen-Übersicht

### 1.1 Service-Definitionen (257 Services)

Der Party-Service definiert 257 Services, aufgeteilt in folgende Kategorien:

| Service-Datei | Anzahl Services | Verantwortungsbereich |
|---------------|-----------------|------------------------|
| **services_party.xml** | 24 | Party-Kernfunktionen (CRUD für Party, Person, PartyGroup) |
| **services_contact.xml** | 22 | Kontaktmechanismen (Adressen, Telefon, E-Mail) |
| **services_agreement.xml** | 18 | Verträge und Vereinbarungen |
| **services_view.xml** | 15 | View-Services für UI |
| **services_communication.xml** | 3 | Kommunikationsereignisse |
| **services.xml** | 175+ | Weitere CRUD-Services (entity-auto) |

**Gesamt:** 257 Service-Definitionen

### 1.2 Kern-API-Klassen

Die wichtigsten öffentlichen Schnittstellen des Party-Service:

#### **PartyServices** (Kern-CRUD-Operationen)
```java
// Party-Verwaltung
Map createPerson(DispatchContext, Map)
Map createPartyGroup(DispatchContext, Map)
Map updatePerson(DispatchContext, Map)
Map updatePartyGroup(DispatchContext, Map)
Map setPartyStatus(DispatchContext, Map)

// Party-Suche
Map findParty(DispatchContext, Map)
Map findPartyById(DispatchContext, Map)
Map performFindParty(DispatchContext, Map)
Map getPartiesFromPerson(DispatchContext, Map)
Map getPartiesFromPartyGroup(DispatchContext, Map)
Map getPartiesFromExactEmail(DispatchContext, Map)
Map getPartiesFromPartOfEmail(DispatchContext, Map)

// Sonstige
Map createPartyNote(DispatchContext, Map)
Map createAffiliate(DispatchContext, Map)
Map linkParty(DispatchContext, Map)
```

#### **ContactMechServices** (Kontaktmechanismen)
```java
// Kontaktmechanismen
Map createContactMech(DispatchContext, Map)
Map updateContactMech(DispatchContext, Map)
Map deleteContactMech(DispatchContext, Map)

// Adressen
Map createPostalAddress(DispatchContext, Map)
Map updatePostalAddress(DispatchContext, Map)

// Telefonnummern
Map createTelecomNumber(DispatchContext, Map)
Map updateTelecomNumber(DispatchContext, Map)

// E-Mail
Map createEmailAddress(DispatchContext, Map)
Map updateEmailAddress(DispatchContext, Map)
Map createEmailAddressVerification(DispatchContext, Map)

// Party-Kontakt-Zuordnung
Map createPartyContactMechPurpose(DispatchContext, Map)
Map getPartyContactMechValueMaps(DispatchContext, Map)
Map copyPartyContactMechs(DispatchContext, Map)
```

#### **PartyWorker** (Helper-Methoden)
```java
// Party-Suche
GenericValue findParty(Delegator, String partyId)
String findPartyId(Delegator, String idValue)
List<GenericValue> findParties(Delegator, String roleTypeId)
List<GenericValue> findPartiesById(Delegator, String idValue, String partyIdentificationTypeId)

// Kontaktinformationen
GenericValue findPartyLatestContactMech(String partyId, String contactMechTypeId, Delegator)
GenericValue findPartyLatestPostalAddress(String partyId, Delegator)
GenericValue findPartyLatestTelecomNumber(String partyId, Delegator)
GenericValue findPartyLatestUserLogin(String partyId, Delegator)

// Locale und Zeitzone
Locale findPartyLastLocale(String partyId, Delegator)
Timestamp findPartyLastLoginTime(String partyId, Delegator)

// Adress-Matching
List<GenericValue> findMatchingPartyPostalAddress(Delegator, String address1, String address2, String city, ...)
String findFirstMatchingPartyId(Delegator, String address1, String address2, ...)
String[] findFirstMatchingPartyAndContactMechId(Delegator, String address1, ...)

// Beziehungen
List<String> getAssociatedPartyIdsByRelationshipType(Delegator, String partyIdFrom, String partyRelationshipTypeId)
```

#### **ContactMechWorker** (Kontakt-Helper)
```java
// Party-Kontakte
List<Map> getPartyContactMechValueMaps(Delegator, String partyId, boolean showOld)
List<Map> getPartyContactMechValueMaps(Delegator, String partyId, Timestamp asOfDate, String contactMechTypeId)
List<GenericValue> getPartyPostalAddresses(ServletRequest, String partyId, String curContactMechId)

// Facility-Kontakte
List<Map> getFacilityContactMechValueMaps(Delegator, String facilityId, boolean showOld)
GenericValue getFacilityContactMechByPurpose(Delegator, String facilityId, List<String> purposeTypes)

// Order-Kontakte
List<Map> getOrderContactMechValueMaps(Delegator, String orderId)

// WorkEffort-Kontakte
Collection<Map> getWorkEffortContactMechValueMaps(Delegator, String workEffortId)

// Adress-Utilities
String getContactMechAttribute(Delegator, String contactMechId, String attrName)
Map<String, Object> getCurrentPostalAddress(ServletRequest, String partyId, String curContactMechId)
String getPostalAddressPostalCodeGeoId(GenericValue postalAddress, Delegator)
boolean isUspsAddress(GenericValue postalAddress)
boolean isCompanyAddress(GenericValue postalAddress, String companyPartyId)
String urlEncodePostalAddress(GenericValue postalAddress)
```

#### **PartyHelper** (Name-Formatierung)
```java
String getPartyName(Delegator, String partyId, boolean lastNameFirst)
String getPartyName(GenericValue party, boolean lastNameFirst)
String getPartyName(GenericValue party)
String formatPartyNameObject(GenericValue person, boolean lastNameFirst)
```

#### **ContactHelper** (Kontakt-Utilities)
```java
Collection<GenericValue> getContactMech(GenericValue party, boolean includeOld)
Collection<GenericValue> getContactMech(GenericValue party, String contactMechTypeId, String contactMechPurposeTypeId, boolean includeOld)
Collection<GenericValue> getContactMechByType(GenericValue party, String contactMechTypeId, boolean includeOld)
Collection<GenericValue> getContactMechByPurpose(GenericValue party, String contactMechPurposeTypeId, boolean includeOld)
String formatCreditCard(GenericValue creditCard)
```

---

## 2. Abhängigkeitsanalyse

### 2.1 Eingehende Abhängigkeiten (Wer ruft Party-Service auf?)

Basierend auf der Neo4j-Analyse rufen folgende Module den Party-Service auf:

| Modul | Aufrufe | Hauptsächlich genutzte Klassen | Verwendungszweck |
|-------|---------|--------------------------------|------------------|
| **order** | 23 | ContactMechWorker (17), PartyWorker (5) | Kundenadresse, Kontaktdaten bei Bestellungen |
| **product** | 6 | ContactMechWorker (4), PartyHelper (2) | Lieferantenadressen, Adressvalidierung |
| **accounting** | 6 | PartyHelper (4), ContactMechWorker (2) | Rechnungsempfänger, Zahlungsinformationen |
| **shipment** | 4 | ContactMechWorker (2), PartyWorker (2) | Versandadressen |
| **marketing** | 2 | ContactHelper (2) | Zielgruppen-Kontaktdaten |
| **humanres** | 2 | PartyHelper (2) | Mitarbeiterinformationen |
| **sfa** | 1 | PartyHelper, PartyWorker | CRM-Funktionen |
| **securityext** | 1 | ContactHelper | Sicherheitsrelevante Kontakte |

**Wichtigste aufgerufene Methoden:**
1. `ContactMechWorker.getPartyContactMechValueMaps()` - Kontaktdaten abrufen
2. `PartyWorker.findPartyLatestUserLogin()` - Letzter Login
3. `PartyWorker.findPartyLatestContactMech()` - Aktuellste Kontaktdaten
4. `PartyHelper.getPartyName()` - Name formatieren
5. `ContactMechWorker.getFacilityContactMechByPurpose()` - Facility-Adressen

### 2.2 Ausgehende Abhängigkeiten (Was nutzt Party-Service?)

Der Party-Service hat Abhängigkeiten zu folgenden Modulen:

#### **Framework-Abhängigkeiten (kritisch)**
| Modul | Abhängigkeiten | Verwendung |
|-------|----------------|------------|
| **entity** | 14 (PartyServices), 8 (ContactMechWorker) | Datenbankzugriff via Entity Engine |
| **base** | 9 (CommunicationEventServices), 8 (PartyContentWrapper) | Utilities, Logging, Konfiguration |
| **service** | 4 (CommunicationEventServices), 3 (PartyServices) | Service-Engine für Transaktionen |

#### **Business-Modul-Abhängigkeiten (zu entkoppeln)**
| Modul | Klassen | Verwendung | Entkopplungsstrategie |
|-------|---------|------------|------------------------|
| **accounting** | BillingAccountWorker, PaymentWorker, InvoiceWorker | Zahlungsmethoden, Rechnungen | → REST API Calls |
| **content** | ContentWorker, DataResourceWorker | Party-Content, Dokumente | → REST API Calls |
| **common** | NotificationServices, GeoWorker | Benachrichtigungen, Geo-Daten | → Event Bus / REST API |
| **product** | CatalogWorker | Shopping Lists | → REST API Calls |

**Kritische Erkenntnis:** Die Abhängigkeiten zu anderen Business-Modulen sind gering (9 Klassen) und können durch API-Calls ersetzt werden.

---

## 3. Kommunikationsmuster

### 3.1 Aktuelles Muster (Monolith)

**Synchrone Aufrufe:**
- Alle Service-Aufrufe erfolgen synchron über die OFBiz Service Engine
- Direkte Java-Methodenaufrufe zwischen Klassen
- Shared Database: Alle Module greifen auf dieselbe Datenbank zu

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│ Order       │────────►│ Party       │────────►│ Shared DB   │
│ Service     │ sync    │ Service     │ JDBC    │             │
└─────────────┘         └─────────────┘         └─────────────┘
```

**Probleme:**
- ❌ Tight Coupling zwischen Modulen
- ❌ Keine Skalierbarkeit einzelner Services
- ❌ Keine unabhängigen Deployments
- ❌ Transaktionen über Modulgrenzen hinweg

### 3.2 Ziel-Architektur (Microservices)

#### **Synchrone Schnittstellen (REST API)**

Für Lese-Operationen und einfache CRUD-Operationen:

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│ Order       │────────►│ Party       │────────►│ Party DB    │
│ Service     │ REST    │ Service     │ JPA     │ (PostgreSQL)│
│             │ HTTP    │ (Spring)    │         │             │
└─────────────┘         └─────────────┘         └─────────────┘
```

**REST API Endpoints:**

```yaml
# Party Management
GET    /api/v1/parties/{partyId}
GET    /api/v1/parties?search={query}
POST   /api/v1/parties
PUT    /api/v1/parties/{partyId}
DELETE /api/v1/parties/{partyId}

# Person
POST   /api/v1/parties/persons
PUT    /api/v1/parties/persons/{partyId}
GET    /api/v1/parties/persons/{partyId}

# Party Group
POST   /api/v1/parties/groups
PUT    /api/v1/parties/groups/{partyId}
GET    /api/v1/parties/groups/{partyId}

# Contact Mechanisms
GET    /api/v1/parties/{partyId}/contacts
POST   /api/v1/parties/{partyId}/contacts
PUT    /api/v1/parties/{partyId}/contacts/{contactMechId}
DELETE /api/v1/parties/{partyId}/contacts/{contactMechId}

# Postal Addresses
GET    /api/v1/parties/{partyId}/addresses
POST   /api/v1/parties/{partyId}/addresses
PUT    /api/v1/parties/{partyId}/addresses/{contactMechId}
GET    /api/v1/parties/{partyId}/addresses/latest

# Telecom Numbers
GET    /api/v1/parties/{partyId}/phones
POST   /api/v1/parties/{partyId}/phones
PUT    /api/v1/parties/{partyId}/phones/{contactMechId}

# Email Addresses
GET    /api/v1/parties/{partyId}/emails
POST   /api/v1/parties/{partyId}/emails
PUT    /api/v1/parties/{partyId}/emails/{contactMechId}

# Search & Lookup
GET    /api/v1/parties/search?firstName={}&lastName={}
GET    /api/v1/parties/search?email={}
GET    /api/v1/parties/search?address={}
GET    /api/v1/parties/lookup/by-external-id/{externalId}

# Relationships
GET    /api/v1/parties/{partyId}/relationships
POST   /api/v1/parties/{partyId}/relationships
```

**Verwendung:** 
- ✅ Lese-Operationen (GET)
- ✅ Einfache CRUD-Operationen
- ✅ Synchrone Anfragen mit sofortiger Antwort
- ✅ Request-Response-Pattern

**Vorteile:**
- ✅ Einfach zu implementieren
- ✅ Gut für Lese-Operationen
- ✅ Standardisiert (OpenAPI/Swagger)
- ✅ Einfaches Caching möglich

**Nachteile:**
- ⚠️ Synchrone Kopplung
- ⚠️ Latenz bei Netzwerk-Calls
- ⚠️ Fehlerbehandlung komplex

#### **Asynchrone Schnittstellen (Event Bus / Kafka)**

Für Änderungsereignisse und lose Kopplung:

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│ Party       │────────►│   Kafka     │────────►│ Order       │
│ Service     │ publish │   Topic     │subscribe│ Service     │
│             │         │             │         │             │
└─────────────┘         └─────────────┘         └─────────────┘
```

**Domain Events:**

```yaml
# Party Events
PartyCreated:
  eventId: uuid
  eventType: PartyCreated
  timestamp: ISO-8601
  aggregateId: partyId
  payload:
    partyId: string
    partyTypeId: PERSON | PARTY_GROUP
    statusId: string
    createdDate: ISO-8601

PartyUpdated:
  eventId: uuid
  eventType: PartyUpdated
  timestamp: ISO-8601
  aggregateId: partyId
  payload:
    partyId: string
    changes: object
    updatedDate: ISO-8601

PartyStatusChanged:
  eventId: uuid
  eventType: PartyStatusChanged
  timestamp: ISO-8601
  aggregateId: partyId
  payload:
    partyId: string
    oldStatusId: string
    newStatusId: string
    changeDate: ISO-8601

# Contact Mechanism Events
ContactMechCreated:
  eventId: uuid
  eventType: ContactMechCreated
  timestamp: ISO-8601
  aggregateId: contactMechId
  payload:
    partyId: string
    contactMechId: string
    contactMechTypeId: POSTAL_ADDRESS | TELECOM_NUMBER | EMAIL_ADDRESS
    contactMechPurposeTypeId: string
    data: object

ContactMechUpdated:
  eventId: uuid
  eventType: ContactMechUpdated
  timestamp: ISO-8601
  aggregateId: contactMechId
  payload:
    partyId: string
    contactMechId: string
    changes: object

PostalAddressChanged:
  eventId: uuid
  eventType: PostalAddressChanged
  timestamp: ISO-8601
  aggregateId: partyId
  payload:
    partyId: string
    oldContactMechId: string
    newContactMechId: string
    address: object

EmailAddressVerified:
  eventId: uuid
  eventType: EmailAddressVerified
  timestamp: ISO-8601
  aggregateId: contactMechId
  payload:
    partyId: string
    contactMechId: string
    emailAddress: string
    verifiedDate: ISO-8601
```

**Kafka Topics:**
- `party.events` - Alle Party-Ereignisse
- `party.contact-mech.events` - Kontaktmechanismus-Ereignisse
- `party.relationship.events` - Beziehungs-Ereignisse

**Verwendung:**
- ✅ Änderungsbenachrichtigungen
- ✅ Eventual Consistency
- ✅ Lose Kopplung zwischen Services
- ✅ Event Sourcing möglich

**Vorteile:**
- ✅ Lose Kopplung
- ✅ Skalierbarkeit
- ✅ Resilience (Retry, Dead Letter Queue)
- ✅ Audit Trail

**Nachteile:**
- ⚠️ Eventual Consistency
- ⚠️ Komplexere Fehlerbehandlung
- ⚠️ Debugging schwieriger

### 3.3 Empfohlene Strategie

**Hybrid-Ansatz:**

| Use Case | Pattern | Begründung |
|----------|---------|------------|
| **Party-Daten lesen** | REST (synchron) | Sofortige Antwort benötigt |
| **Party erstellen/ändern** | REST + Event | REST für Response, Event für Notification |
| **Adresse ändern** | REST + Event | REST für Response, Event für andere Services |
| **Party-Suche** | REST (synchron) | Sofortige Antwort benötigt |
| **Bulk-Import** | Async (Kafka) | Lange Laufzeit, keine sofortige Antwort nötig |
| **Statusänderungen** | Event (asynchron) | Andere Services müssen reagieren |

**Beispiel: Party erstellen**
```
1. Client → POST /api/v1/parties → Party Service (REST)
2. Party Service → Speichert in DB
3. Party Service → Publiziert PartyCreated Event → Kafka
4. Party Service → Gibt Response zurück (synchron)
5. Order Service ← Empfängt PartyCreated Event (asynchron)
6. Accounting Service ← Empfängt PartyCreated Event (asynchron)
```

---

## 4. Code-Anpassungen im restlichen System

### 4.1 Betroffene Module und Anpassungen

#### **Order-Modul (23 Aufrufe)**

**Aktueller Code:**
```java
// Direkter Aufruf
List<Map<String, Object>> contactMechs = 
    ContactMechWorker.getPartyContactMechValueMaps(delegator, partyId, false);

GenericValue userLogin = 
    PartyWorker.findPartyLatestUserLogin(partyId, delegator);
```

**Neuer Code (REST Client):**
```java
// REST API Call
@Autowired
private PartyServiceClient partyServiceClient;

List<ContactMechDTO> contactMechs = 
    partyServiceClient.getPartyContactMechs(partyId);

UserLoginDTO userLogin = 
    partyServiceClient.getPartyLatestUserLogin(partyId);
```

**Anzupassende Klassen im Order-Modul:**
- `OrderServices.java` - Kundenadresse bei Bestellung
- `ShoppingCartServices.java` - Kontaktdaten im Warenkorb
- `CheckOutHelper.java` - Checkout-Prozess
- ~17 weitere Klassen

**Geschätzter Aufwand:** 3-4 Tage

#### **Product-Modul (6 Aufrufe)**

**Aktueller Code:**
```java
String partyName = PartyHelper.getPartyName(delegator, partyId, false);
boolean isUsps = ContactMechWorker.isUspsAddress(postalAddress);
```

**Neuer Code:**
```java
String partyName = partyServiceClient.getPartyName(partyId);
boolean isUsps = partyServiceClient.isUspsAddress(contactMechId);
```

**Anzupassende Klassen:**
- `ProductServices.java` - Lieferanteninformationen
- `ShipmentServices.java` - Versandadressen
- ~4 weitere Klassen

**Geschätzter Aufwand:** 1-2 Tage

#### **Accounting-Modul (6 Aufrufe)**

**Aktueller Code:**
```java
String partyName = PartyHelper.getPartyName(party, false);
List<Map> contacts = ContactHelper.getContactMechByPurpose(party, "BILLING_LOCATION", false);
```

**Neuer Code:**
```java
String partyName = partyServiceClient.getPartyName(partyId);
List<ContactMechDTO> contacts = partyServiceClient.getContactMechsByPurpose(partyId, "BILLING_LOCATION");
```

**Anzupassende Klassen:**
- `InvoiceServices.java` - Rechnungsempfänger
- `PaymentServices.java` - Zahlungsinformationen
- ~4 weitere Klassen

**Geschätzter Aufwand:** 1-2 Tage

#### **Weitere Module**

| Modul | Aufrufe | Aufwand | Priorität |
|-------|---------|---------|-----------|
| Shipment | 4 | 1 Tag | Hoch |
| Marketing | 2 | 0.5 Tage | Mittel |
| HumanRes | 2 | 0.5 Tage | Niedrig |
| SFA | 1 | 0.5 Tage | Niedrig |
| SecurityExt | 1 | 0.5 Tage | Mittel |

**Gesamt-Aufwand für Code-Anpassungen:** ~8-12 Tage

### 4.2 Anti-Corruption Layer (ACL)

Um die Migration schrittweise durchzuführen, implementieren wir einen Anti-Corruption Layer:

```java
/**
 * Adapter zwischen OFBiz und neuem Party Service
 * Ermöglicht schrittweise Migration
 */
public class PartyServiceAdapter {
    
    private final PartyServiceClient partyServiceClient;
    private final Delegator delegator;
    private final boolean useNewService;
    
    public PartyServiceAdapter(PartyServiceClient client, Delegator delegator) {
        this.partyServiceClient = client;
        this.delegator = delegator;
        // Feature Flag: Schrittweise Umstellung
        this.useNewService = UtilProperties.getPropertyAsBoolean(
            "party", "use.new.party.service", false);
    }
    
    /**
     * Dual-Read: Versuche neuen Service, Fallback zu OFBiz
     */
    public GenericValue getParty(String partyId) {
        if (useNewService) {
            try {
                PartyDTO party = partyServiceClient.getParty(partyId);
                return convertToGenericValue(party);
            } catch (Exception e) {
                logger.warn("Party Service unavailable, falling back to OFBiz", e);
                // Fallback zu OFBiz
            }
        }
        
        // Legacy OFBiz
        return delegator.findOne("Party", 
            UtilMisc.toMap("partyId", partyId), false);
    }
    
    /**
     * Dual-Write: Schreibe in beide Systeme
     */
    public Map<String, Object> createParty(Map<String, Object> context) {
        // 1. Schreibe in OFBiz (Master während Migration)
        Map<String, Object> result = PartyServices.createPerson(
            dispatcher.getDispatchContext(), context);
        
        if (useNewService && ServiceUtil.isSuccess(result)) {
            try {
                // 2. Synchronisiere zu neuem Service
                String partyId = (String) result.get("partyId");
                PartyDTO party = convertFromContext(context, partyId);
                partyServiceClient.createParty(party);
            } catch (Exception e) {
                logger.error("Failed to sync party to new service", e);
                // Nicht kritisch während Migration
            }
        }
        
        return result;
    }
    
    /**
     * Konvertierung OFBiz → DTO
     */
    private PartyDTO convertToDTO(GenericValue partyValue) {
        PartyDTO dto = new PartyDTO();
        dto.setPartyId(partyValue.getString("partyId"));
        dto.setPartyTypeId(partyValue.getString("partyTypeId"));
        dto.setStatusId(partyValue.getString("statusId"));
        // ... weitere Felder
        return dto;
    }
    
    /**
     * Konvertierung DTO → OFBiz
     */
    private GenericValue convertToGenericValue(PartyDTO party) {
        GenericValue value = delegator.makeValue("Party");
        value.set("partyId", party.getPartyId());
        value.set("partyTypeId", party.getPartyTypeId());
        value.set("statusId", party.getStatusId());
        // ... weitere Felder
        return value;
    }
}
```

**Verwendung im Code:**
```java
// Statt direktem Aufruf:
// GenericValue party = delegator.findOne("Party", ...);

// Verwende Adapter:
PartyServiceAdapter adapter = new PartyServiceAdapter(partyServiceClient, delegator);
GenericValue party = adapter.getParty(partyId);
```

---

## 5. Extraktionsplan

### Phase 0: Vorbereitung (Woche 1-2)

#### **Woche 1: Infrastruktur & Tooling**

**Tag 1-2: Entwicklungsumgebung**
- [ ] Docker Compose Setup für lokale Entwicklung
  - PostgreSQL für Party Service
  - Kafka + Zookeeper
  - Redis für Caching
  - Jaeger für Tracing
- [ ] Git Repository für Party Service erstellen
- [ ] CI/CD Pipeline aufsetzen (GitHub Actions / GitLab CI)

**Tag 3-4: API Gateway & Service Mesh**
- [ ] Kong API Gateway installieren und konfigurieren
- [ ] Service Discovery (Consul / Eureka) aufsetzen
- [ ] Istio Service Mesh evaluieren (optional)

**Tag 5: Monitoring & Observability**
- [ ] Prometheus + Grafana Setup
- [ ] Jaeger Distributed Tracing
- [ ] ELK Stack für Logging (oder Loki)
- [ ] Alerting-Regeln definieren

**Deliverables:**
- ✅ Funktionierende lokale Entwicklungsumgebung
- ✅ CI/CD Pipeline
- ✅ Monitoring-Dashboard
- ✅ Dokumentation der Infrastruktur

#### **Woche 2: Datenmodell & API-Design**

**Tag 1-3: Datenbank-Schema extrahieren**
- [ ] Party-Tabellen identifizieren (ca. 30 Tabellen):
  - Party, Person, PartyGroup, PartyRole
  - PostalAddress, TelecomNumber, ContactMech
  - PartyRelationship, PartyClassification
  - PartyStatus, PartyType, etc.
- [ ] ER-Diagramm erstellen
- [ ] PostgreSQL-Schema generieren
- [ ] Liquibase/Flyway Migrations erstellen
- [ ] Test-Daten migrieren

**Tag 4-5: API-Design**
- [ ] OpenAPI 3.0 Spezifikation schreiben
- [ ] REST Endpoints definieren (siehe Abschnitt 3.2)
- [ ] DTOs (Data Transfer Objects) definieren
- [ ] Event-Schema für Kafka definieren (Avro)
- [ ] API-Dokumentation mit Swagger UI

**Deliverables:**
- ✅ PostgreSQL-Datenbank mit Party-Schema
- ✅ OpenAPI 3.0 Spezifikation
- ✅ Event-Schema (Avro)
- ✅ API-Dokumentation

---

### Phase 1: Service-Implementierung (Woche 3-5)

#### **Woche 3: Basis-Implementierung**

**Tag 1-2: Spring Boot Projekt Setup**
```bash
# Projekt-Struktur
party-service/
├── src/main/java/org/ofbiz/party/
│   ├── PartyServiceApplication.java
│   ├── config/
│   │   ├── DatabaseConfig.java
│   │   ├── KafkaConfig.java
│   │   ├── SecurityConfig.java
│   │   └── SwaggerConfig.java
│   ├── domain/
│   │   ├── Party.java
│   │   ├── Person.java
│   │   ├── PartyGroup.java
│   │   ├── ContactMech.java
│   │   ├── PostalAddress.java
│   │   └── TelecomNumber.java
│   ├── repository/
│   │   ├── PartyRepository.java
│   │   ├── PersonRepository.java
│   │   ├── ContactMechRepository.java
│   │   └── ...
│   ├── service/
│   │   ├── PartyService.java
│   │   ├── ContactMechService.java
│   │   └── PartySearchService.java
│   ├── controller/
│   │   ├── PartyController.java
│   │   ├── ContactMechController.java
│   │   └── PartySearchController.java
│   ├── dto/
│   │   ├── PartyDTO.java
│   │   ├── PersonDTO.java
│   │   ├── ContactMechDTO.java
│   │   └── ...
│   ├── mapper/
│   │   ├── PartyMapper.java
│   │   └── ContactMechMapper.java
│   ├── event/
│   │   ├── PartyEvent.java
│   │   ├── PartyCreatedEvent.java
│   │   ├── PartyUpdatedEvent.java
│   │   └── ContactMechChangedEvent.java
│   └── exception/
│       ├── PartyNotFoundException.java
│       └── InvalidPartyDataException.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/
│       ├── V1__create_party_tables.sql
│       └── V2__create_indexes.sql
└── src/test/java/
    ├── integration/
    └── unit/
```

**Aufgaben:**
- [ ] Spring Boot 3.x Projekt initialisieren
- [ ] Dependencies konfigurieren (Spring Data JPA, Kafka, Redis, etc.)
- [ ] Datenbank-Konfiguration
- [ ] Basis-Entities erstellen (JPA)

**Tag 3-5: Core Services implementieren**
- [ ] PartyService: CRUD-Operationen
  - createPerson()
  - createPartyGroup()
  - updatePerson()
  - updatePartyGroup()
  - getParty()
  - deleteParty()
- [ ] ContactMechService: Kontaktmechanismen
  - createPostalAddress()
  - updatePostalAddress()
  - createTelecomNumber()
  - createEmailAddress()
  - getPartyContactMechs()
- [ ] PartySearchService: Suchfunktionen
  - findPartyById()
  - searchParties()
  - findByEmail()
  - findByAddress()

**Deliverables:**
- ✅ Lauffähiger Spring Boot Service
- ✅ Basis-CRUD-Operationen funktionieren
- ✅ Unit Tests (>80% Coverage)

#### **Woche 4: REST API & Events**

**Tag 1-3: REST Controller implementieren**
- [ ] PartyController mit allen Endpoints
- [ ] ContactMechController
- [ ] PartySearchController
- [ ] Request Validation (Bean Validation)
- [ ] Exception Handling (@ControllerAdvice)
- [ ] HATEOAS Links (optional)

**Tag 4-5: Event Publishing**
- [ ] Kafka Producer konfigurieren
- [ ] Event-Publishing bei CRUD-Operationen
  - PartyCreated
  - PartyUpdated
  - PartyStatusChanged
  - ContactMechCreated
  - ContactMechUpdated
- [ ] Event-Schema-Validierung (Avro)
- [ ] Dead Letter Queue für fehlerhafte Events

**Deliverables:**
- ✅ Vollständige REST API
- ✅ Event-Publishing funktioniert
- ✅ Integration Tests
- ✅ Postman Collection / OpenAPI Spec

#### **Woche 5: Caching & Performance**

**Tag 1-2: Redis Caching**
- [ ] Redis-Konfiguration
- [ ] Cache für häufig abgefragte Parties
- [ ] Cache-Invalidierung bei Updates
- [ ] Cache-Warming für beliebte Parties

**Tag 3-4: Performance-Optimierung**
- [ ] Datenbank-Indizes optimieren
- [ ] N+1 Query Problem lösen (JPA Fetch Strategies)
- [ ] Connection Pooling (HikariCP)
- [ ] Pagination für große Resultsets

**Tag 5: Load Testing**
- [ ] JMeter / Gatling Tests
- [ ] Performance-Benchmarks
- [ ] Bottleneck-Analyse

**Deliverables:**
- ✅ Caching implementiert
- ✅ Performance-Ziele erreicht (< 100ms für einfache Queries)
- ✅ Load-Test-Ergebnisse dokumentiert

---

### Phase 2: Integration mit OFBiz (Woche 6-7)

#### **Woche 6: Anti-Corruption Layer**

**Tag 1-3: ACL in OFBiz implementieren**
- [ ] PartyServiceAdapter-Klasse erstellen (siehe Abschnitt 4.2)
- [ ] PartyServiceClient (REST Client)
  - Feign Client oder RestTemplate
  - Circuit Breaker (Resilience4j)
  - Retry-Logik
  - Timeout-Konfiguration
- [ ] Feature Flags für schrittweise Umstellung
- [ ] Dual-Read-Logik (neuer Service mit Fallback)

**Tag 4-5: Dual-Write-Phase**
- [ ] Schreibe in OFBiz (Master)
- [ ] Synchronisiere zu Party Service
- [ ] Konsistenz-Checks
- [ ] Monitoring der Sync-Fehler

**Deliverables:**
- ✅ ACL implementiert
- ✅ Dual-Write funktioniert
- ✅ Fallback-Mechanismus getestet

#### **Woche 7: Code-Migration**

**Tag 1-2: Order-Modul anpassen**
- [ ] 23 Aufrufe zu Party-Service umstellen
- [ ] Tests anpassen
- [ ] Integration Tests

**Tag 3: Product-Modul anpassen**
- [ ] 6 Aufrufe umstellen
- [ ] Tests anpassen

**Tag 4: Accounting-Modul anpassen**
- [ ] 6 Aufrufe umstellen
- [ ] Tests anpassen

**Tag 5: Weitere Module**
- [ ] Shipment, Marketing, HumanRes, SFA, SecurityExt
- [ ] Tests anpassen

**Deliverables:**
- ✅ Alle Module nutzen ACL
- ✅ Tests grün
- ✅ Integration Tests erfolgreich

---

### Phase 3: Datenmigration & Cutover (Woche 8-9)

#### **Woche 8: Datenmigration**

**Tag 1-2: Migrations-Script**
- [ ] ETL-Script für Party-Daten
  - Extrahiere aus OFBiz-DB
  - Transformiere zu neuem Schema
  - Lade in Party-Service-DB
- [ ] Daten-Validierung
- [ ] Konsistenz-Checks

**Tag 3-4: Test-Migration**
- [ ] Migration auf Test-Umgebung
- [ ] Daten-Vergleich (alt vs. neu)
- [ ] Performance-Tests mit echten Daten
- [ ] Rollback-Test

**Tag 5: Produktions-Migration vorbereiten**
- [ ] Migrations-Runbook erstellen
- [ ] Downtime-Fenster planen
- [ ] Rollback-Plan
- [ ] Kommunikation an Stakeholder

**Deliverables:**
- ✅ Migrations-Script getestet
- ✅ Daten erfolgreich migriert
- ✅ Rollback-Plan vorhanden

#### **Woche 9: Cutover & Go-Live**

**Tag 1: Produktions-Migration**
- [ ] Maintenance-Modus aktivieren
- [ ] Daten migrieren (ca. 2-4 Stunden)
- [ ] Daten-Validierung
- [ ] Party Service in Produktion deployen

**Tag 2: Schrittweise Umstellung**
- [ ] Feature Flag aktivieren (10% Traffic)
- [ ] Monitoring intensiv beobachten
- [ ] Fehler-Logs analysieren
- [ ] Bei Problemen: Rollback

**Tag 3-4: Traffic erhöhen**
- [ ] 25% Traffic → Party Service
- [ ] 50% Traffic → Party Service
- [ ] 75% Traffic → Party Service
- [ ] Monitoring & Fehleranalyse

**Tag 5: Vollständiger Cutover**
- [ ] 100% Traffic → Party Service
- [ ] OFBiz Party-Tabellen read-only
- [ ] Alte Party-Services deaktivieren
- [ ] Erfolg feiern! 🎉

**Deliverables:**
- ✅ Party Service läuft in Produktion
- ✅ Alle Clients nutzen neuen Service
- ✅ Monitoring zeigt keine kritischen Fehler
- ✅ Performance-Ziele erreicht

---

### Phase 4: Stabilisierung & Optimierung (Woche 10-12)

#### **Woche 10: Monitoring & Bugfixing**
- [ ] Fehler aus Produktion analysieren und fixen
- [ ] Performance-Optimierungen
- [ ] Monitoring-Dashboards verfeinern
- [ ] Alerting-Regeln anpassen

#### **Woche 11: Dokumentation**
- [ ] API-Dokumentation vervollständigen
- [ ] Runbooks für Operations
- [ ] Architecture Decision Records (ADRs)
- [ ] Lessons Learned dokumentieren

#### **Woche 12: Alte Systeme aufräumen**
- [ ] OFBiz Party-Code als deprecated markieren
- [ ] Alte Party-Tabellen archivieren
- [ ] Cleanup von Test-Code
- [ ] Retrospektive mit Team

**Deliverables:**
- ✅ Stabiler Party Service in Produktion
- ✅ Vollständige Dokumentation
- ✅ Team-Retrospektive durchgeführt

---

## 6. Risiken und Mitigationsstrategien

### 6.1 Technische Risiken

| Risiko | Wahrscheinlichkeit | Impact | Mitigation |
|--------|-------------------|--------|------------|
| **Datenverlust bei Migration** | Niedrig | Kritisch | • Backup vor Migration<br>• Daten-Validierung<br>• Rollback-Plan<br>• Test-Migration vorher |
| **Performance-Degradation** | Mittel | Hoch | • Load-Tests vor Go-Live<br>• Caching (Redis)<br>• Datenbank-Indizes<br>• Monitoring & Alerting |
| **Netzwerk-Latenz** | Mittel | Mittel | • Service-Mesh (Istio)<br>• Caching<br>• Circuit Breaker<br>• Async Communication wo möglich |
| **Inkonsistente Daten** | Mittel | Hoch | • Dual-Write-Phase<br>• Konsistenz-Checks<br>• Event Sourcing<br>• Compensating Transactions |
| **Service-Ausfall** | Niedrig | Hoch | • Circuit Breaker<br>• Fallback zu OFBiz<br>• Health Checks<br>• Auto-Scaling |
| **Komplexität steigt** | Hoch | Mittel | • Gute Dokumentation<br>• Service-Mesh<br>• Observability<br>• Team-Training |

### 6.2 Organisatorische Risiken

| Risiko | Wahrscheinlichkeit | Impact | Mitigation |
|--------|-------------------|--------|------------|
| **Team-Überforderung** | Mittel | Hoch | • Externe Expertise<br>• Pair Programming<br>• Training<br>• Realistische Zeitplanung |
| **Stakeholder-Widerstand** | Niedrig | Mittel | • Frühe Kommunikation<br>• Quick Wins zeigen<br>• Business Value betonen |
| **Scope Creep** | Mittel | Mittel | • Klare Abgrenzung<br>• MVP-Ansatz<br>• Change Management |
| **Zeitdruck** | Hoch | Hoch | • Puffer einplanen<br>• Priorisierung<br>• Schrittweise Umstellung |

### 6.3 Erfolgskriterien

**Technische Kriterien:**
- ✅ Party Service läuft stabil in Produktion (Uptime > 99.9%)
- ✅ Performance-Ziele erreicht (95th Percentile < 200ms)
- ✅ Alle CRUD-Operationen funktionieren
- ✅ Events werden korrekt publiziert
- ✅ Monitoring zeigt keine kritischen Fehler
- ✅ Automatisierte Tests (>80% Coverage)

**Business-Kriterien:**
- ✅ Keine Ausfallzeiten für Endbenutzer
- ✅ Keine Datenverluste
- ✅ Funktionalität bleibt gleich oder besser
- ✅ Unabhängiges Deployment möglich

---

## 7. Aufwandsschätzung

### 7.1 Team-Zusammensetzung

**Empfohlenes Team:**
- 2-3 Senior Backend-Entwickler (Java/Spring Boot)
- 1 DevOps-Engineer (Kubernetes, CI/CD)
- 1 Architekt (Teilzeit, Beratung)
- 1 QA-Engineer (Testing)

### 7.2 Zeitplan

| Phase | Dauer | Team-Größe | Aufwand (Personentage) |
|-------|-------|------------|------------------------|
| **Phase 0: Vorbereitung** | 2 Wochen | 2-3 | 20-30 PT |
| **Phase 1: Implementierung** | 3 Wochen | 2-3 | 30-45 PT |
| **Phase 2: Integration** | 2 Wochen | 2-3 | 20-30 PT |
| **Phase 3: Migration & Cutover** | 2 Wochen | 3-4 | 30-40 PT |
| **Phase 4: Stabilisierung** | 3 Wochen | 2-3 | 30-45 PT |
| **Gesamt** | **12 Wochen** | **2-3** | **130-190 PT** |

**Bei 3 Entwicklern:** ~12-14 Wochen (3 Monate)  
**Bei 2 Entwicklern:** ~16-18 Wochen (4 Monate)

### 7.3 Kosten-Schätzung

**Annahmen:**
- Senior Developer: 800 €/Tag
- DevOps Engineer: 900 €/Tag
- Architekt: 1.200 €/Tag (Teilzeit)
- QA Engineer: 700 €/Tag

**Personal-Kosten:**
- 3 Senior Developers × 60 Tage × 800 € = 144.000 €
- 1 DevOps Engineer × 40 Tage × 900 € = 36.000 €
- 1 Architekt × 20 Tage × 1.200 € = 24.000 €
- 1 QA Engineer × 30 Tage × 700 € = 21.000 €

**Infrastruktur-Kosten (pro Monat):**
- Kubernetes Cluster (AWS EKS): 300 €
- PostgreSQL (RDS): 200 €
- Kafka (MSK): 400 €
- Redis (ElastiCache): 100 €
- Monitoring (Datadog/New Relic): 300 €
- **Gesamt:** ~1.300 €/Monat × 3 Monate = 3.900 €

**Gesamt-Kosten:** ~225.000 € + 3.900 € = **~229.000 €**

---

## 8. Nächste Schritte

### 8.1 Sofort (Woche 1)
1. ✅ Diese Analyse mit Stakeholdern reviewen
2. [ ] Budget und Ressourcen freigeben
3. [ ] Team zusammenstellen (2-3 Entwickler + DevOps)
4. [ ] Kick-off Meeting planen
5. [ ] Entwicklungsumgebung aufsetzen

### 8.2 Kurzfristig (Monat 1)
1. [ ] Infrastruktur aufsetzen (Docker Compose, Kubernetes)
2. [ ] Datenbank-Schema extrahieren
3. [ ] OpenAPI-Spezifikation schreiben
4. [ ] Spring Boot Projekt initialisieren
5. [ ] Erste REST Endpoints implementieren

### 8.3 Mittelfristig (Monat 2-3)
1. [ ] Vollständige Service-Implementierung
2. [ ] Anti-Corruption Layer in OFBiz
3. [ ] Code-Migration in anderen Modulen
4. [ ] Datenmigration vorbereiten
5. [ ] Load-Tests durchführen

### 8.4 Langfristig (Monat 3-4)
1. [ ] Produktions-Migration
2. [ ] Schrittweiser Cutover
3. [ ] Stabilisierung
4. [ ] Dokumentation vervollständigen
5. [ ] Lessons Learned für nächsten Service (Content Service)

---

## 9. Anhang

### 9.1 Wichtige Dateien

**Service-Definitionen:**
- [`applications/party/servicedef/services_party.xml`](../applications/party/servicedef/services_party.xml)
- [`applications/party/servicedef/services_contact.xml`](../applications/party/servicedef/services_contact.xml)
- [`applications/party/servicedef/services_communication.xml`](../applications/party/servicedef/services_communication.xml)

**Java-Klassen:**
- [`PartyServices.java`](../applications/party/src/main/java/org/apache/ofbiz/party/party/PartyServices.java)
- [`ContactMechServices.java`](../applications/party/src/main/java/org/apache/ofbiz/party/contact/ContactMechServices.java)
- [`PartyWorker.java`](../applications/party/src/main/java/org/apache/ofbiz/party/party/PartyWorker.java)
- [`ContactMechWorker.java`](../applications/party/src/main/java/org/apache/ofbiz/party/contact/ContactMechWorker.java)

### 9.2 Referenzen

- [SERVICE_DECOMPOSITION_ANALYSIS.md](./SERVICE_DECOMPOSITION_ANALYSIS.md) - Basis-Analyse
- [MICROSERVICES_ARCHITECTURE.md](./MICROSERVICES_ARCHITECTURE.md) - Architektur-Übersicht
- [NEO4J_QUERIES.md](./NEO4J_QUERIES.md) - Nützliche Queries

### 9.3 Neo4j Queries für weitere Analysen

```cypher
// Alle Party-Services finden
MATCH (c:Class)
WHERE c.fqn STARTS WITH 'org.apache.ofbiz.party'
RETURN c.name, c.fqn
ORDER BY c.name

// Abhängigkeiten visualisieren
MATCH path = (party:Class)-[:DEPENDS_ON*1..2]->(target:Class)
WHERE party.fqn STARTS WITH 'org.apache.ofbiz.party'
  AND target.fqn STARTS WITH 'org.apache.ofbiz'
  AND NOT target.fqn STARTS WITH 'org.apache.ofbiz.party'
RETURN path
LIMIT 50

// Meistgenutzte Party-Methoden
MATCH (caller:Class)-[:DECLARES]->(m:Method)-[:INVOKES]->(partyMethod:Method)
      <-[:DECLARES]-(party:Class)
WHERE party.fqn STARTS WITH 'org.apache.ofbiz.party'
  AND NOT caller.fqn STARTS WITH 'org.apache.ofbiz.party'
RETURN party.name, partyMethod.name, COUNT(*) as callCount
ORDER BY callCount DESC
LIMIT 20
```

---

## 10. Fazit

Der Party-Service ist der **ideale Kandidat** für die erste Microservice-Extraktion:

### ✅ Vorteile
- **Überschaubare Größe:** 83 Klassen, 257 Services
- **Geringe Kopplung:** Nur 9 ausgehende Abhängigkeiten zu Business-Modulen
- **Klare Domäne:** Kunden- und Kontaktverwaltung
- **Fundament:** Wird von vielen anderen Services benötigt
- **Geringes Risiko:** Keine komplexen Transaktionen

### ⚠️ Herausforderungen
- **Framework-Kopplung:** Starke Abhängigkeit zu OFBiz Entity/Service Engine
- **Datenmigration:** ~30 Tabellen müssen migriert werden
- **Code-Anpassungen:** 42 Aufrufe in anderen Modulen
- **Dual-Write-Phase:** Synchronisation zwischen alt und neu

### 🎯 Empfehlung
**GO für Party-Service als Proof-of-Concept!**

Mit einem Team von 2-3 Entwicklern ist die Extraktion in **12-14 Wochen** realistisch machbar. Die Investition von ~230.000 € ist gerechtfertigt durch:
- Proof-of-Concept für Microservices-Architektur
- Unabhängige Skalierbarkeit
- Technologie-Freiheit für zukünftige Services
- Lessons Learned für weitere Extraktionen

Nach erfolgreichem Party-Service können Content Service (3-4 Wochen) und Product Service (6-8 Wochen) folgen.

---

**Erstellt am:** 17. Januar 2026  
**Autor:** Roo (AI Assistant)  
**Version:** 1.0  
**Status:** Ready for Review
