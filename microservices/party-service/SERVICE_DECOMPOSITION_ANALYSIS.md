# OFBiz Service-Dekompositionsanalyse

## Executive Summary

Diese Analyse untersucht die OFBiz-Codebasis mit dem Ziel, eine Strategie zur Aufteilung in unabhängige fachliche Microservices zu entwickeln. Die Analyse basiert auf der Neo4j-Datenbank mit **2.818 OFBiz-Klassen**, **33.267 Methoden** und **194.243 effektiven Code-Zeilen**.

## 1. Überblick über die Hauptmodule

### 1.1 Framework-Komponenten (Infrastruktur)
Diese bilden die technische Basis und werden von allen Business-Modulen genutzt:

| Modul | Klassen | Beschreibung |
|-------|---------|--------------|
| **base** | 364 | Kern-Utilities, Konfiguration, Logging |
| **entity** | 232 | ORM-Framework, Datenbankzugriff |
| **widget** | 219 | UI-Framework, Formular-/Screen-Rendering |
| **service** | 147 | Service-Engine, Transaktionsmanagement |
| **minilang** | 241 | DSL für Business-Logik |
| **webapp** | 86 | Web-Framework, Controller |
| **security** | 12 | Authentifizierung, Autorisierung |

**Gesamt Framework:** ~1.301 Klassen (46% der Codebasis)

### 1.2 Business-Module (Fachliche Domänen)
Diese enthalten die eigentliche Geschäftslogik:

| Modul | Klassen | Fachliche Domäne | Anteil |
|-------|---------|------------------|--------|
| **product** | 334 | Produktkatalog, Preise, Kategorien | 11,9% |
| **order** | 281 | Bestellverwaltung, Shopping Cart | 10,0% |
| **accounting** | 277 | Buchhaltung, Rechnungen, Zahlungen | 9,8% |
| **content** | 133 | Content Management, Dokumente | 4,7% |
| **manufacturing** | 86 | Produktion, Stücklisten | 3,1% |
| **party** | 83 | Kunden, Lieferanten, Kontakte | 2,9% |
| **workeffort** | 43 | Projekte, Aufgaben, Kalender | 1,5% |
| **shipment** | 25 | Versand, Logistik | 0,9% |
| **marketing** | 14 | Kampagnen, Tracking | 0,5% |
| **humanres** | 3 | Personalwesen | 0,1% |
| **sfa** | 1 | Sales Force Automation | 0,04% |

**Gesamt Business:** ~1.280 Klassen (45% der Codebasis)

### 1.3 Weitere Module
- **common** (80 Klassen): Gemeinsame Services
- **webtools** (96 Klassen): Admin-Tools
- **entityext** (26 Klassen): Entity-Erweiterungen
- **testtools** (9 Klassen): Test-Framework

**Gesamt Sonstige:** ~237 Klassen (9% der Codebasis)

## 2. Abhängigkeitsanalyse

### 2.1 Abhängigkeiten zwischen Business-Modulen

Die stärksten Abhängigkeiten zwischen fachlichen Modulen:

| Von | Nach | Abhängigkeiten | Bedeutung |
|-----|------|----------------|-----------|
| **order** | **product** | 108 | Bestellungen benötigen Produktinformationen |
| **order** | **party** | 28 | Kundeninformationen bei Bestellungen |
| **product** | **order** | 19 | Rückverweise (z.B. Bestellhistorie) |
| **accounting** | **order** | 18 | Rechnungsstellung für Bestellungen |
| **accounting** | **product** | 10 | Preise, Kosten |
| **product** | **content** | 8 | Produktbeschreibungen, Bilder |
| **party** | **accounting** | 7 | Kundenkonto-Informationen |
| **manufacturing** | **order** | 6 | Produktionsaufträge |
| **product** | **party** | 6 | Lieferanten, Hersteller |
| **accounting** | **party** | 6 | Rechnungsempfänger |

**Wichtige Erkenntnisse:**
- ✅ **Keine zyklischen Abhängigkeiten** zwischen Business-Modulen gefunden
- ⚠️ **Order-Modul** ist stark mit Product (108) und Party (28) verknüpft
- ⚠️ **Product-Modul** ist ein zentraler Hub (viele Module hängen davon ab)
- ✅ Module wie **marketing**, **humanres**, **sfa** sind sehr isoliert
- ✅ **Party-Modul** hat nur 7 ausgehende Abhängigkeiten → idealer Startpunkt!

### 2.2 Abhängigkeiten zu Framework-Komponenten

Alle Business-Module haben Abhängigkeiten zu den Framework-Komponenten. Die Analyse zeigt, dass die Kopplung an das OFBiz-Framework die größte Herausforderung für die Dekomposition darstellt.

**Kritische Erkenntnis:** Die starke Kopplung an das OFBiz-Framework (insbesondere Entity-Engine und Service-Engine) erfordert einen Anti-Corruption Layer für jeden neuen Service.

## 3. Service-Kandidaten und Bounded Contexts

### 3.1 Empfohlene Service-Aufteilung

Basierend auf der Analyse empfehle ich folgende Microservices:

#### 🟢 **Tier 1: Unabhängige Core-Services (Sofort extrahierbar)**

**1. Party Service** (Kunden- und Kontaktverwaltung)
- **Größe:** 83 Klassen (~3% der Codebasis)
- **Verantwortung:** Verwaltung von Kunden, Lieferanten, Kontakten, Adressen
- **Ausgehende Abhängigkeiten:** 
  - accounting: 7 (Kundenkonto-Info)
  - content: 2 (Dokumente)
- **Eingehende Abhängigkeiten:** 
  - order: 28
  - product: 6
  - accounting: 6
  - shipment: 4
  - marketing: 2
- **API:** REST/GraphQL für CRUD-Operationen
- **Priorität:** ⭐⭐⭐⭐⭐ (Fundament für andere Services, geringe Komplexität)
- **Geschätzte Dauer:** 4-6 Wochen

**2. Content Service** (Content Management)
- **Größe:** 133 Klassen (~5% der Codebasis)
- **Verantwortung:** Dokumente, Bilder, Texte, Medien
- **Ausgehende Abhängigkeiten:** Minimal (keine zu anderen Business-Modulen)
- **Eingehende Abhängigkeiten:**
  - product: 8
  - order: 3
  - party: 2
  - workeffort: 2
  - manufacturing: 1
- **API:** REST für Upload/Download, Metadaten-Verwaltung
- **Storage:** S3/MinIO für Dateien
- **Priorität:** ⭐⭐⭐⭐
- **Geschätzte Dauer:** 3-4 Wochen

**3. Marketing Service**
- **Größe:** 14 Klassen (~0,5% der Codebasis)
- **Verantwortung:** Kampagnen, Tracking Codes
- **Ausgehende Abhängigkeiten:** 
  - party: 2 (Zielgruppen)
- **API:** REST für Kampagnen-Management
- **Priorität:** ⭐⭐⭐
- **Geschätzte Dauer:** 2-3 Wochen

#### 🟡 **Tier 2: Mittelgroße Services (Nach Tier 1)**

**4. Product Service** (Produktkatalog)
- **Größe:** 334 Klassen (~12% der Codebasis)
- **Verantwortung:** Produkte, Kategorien, Preise, Lagerbestände
- **Ausgehende Abhängigkeiten:** 
  - order: 19 (Bestellhistorie)
  - content: 8 (Produktbilder/-beschreibungen)
  - party: 6 (Lieferanten)
  - shipment: 4 (Versandinfo)
- **Eingehende Abhängigkeiten:**
  - order: 108 (sehr hoch!)
  - accounting: 10
  - manufacturing: 4
  - shipment: 4
- **API:** REST/GraphQL für Produktsuche, Katalog-Browsing
- **Herausforderung:** Zentrale Rolle - viele andere Services benötigen Produktdaten
- **Pattern:** CQRS für Lese-/Schreiboperationen, Event Sourcing für Preisänderungen
- **Priorität:** ⭐⭐⭐⭐⭐
- **Geschätzte Dauer:** 6-8 Wochen

**5. WorkEffort Service** (Projekt- und Aufgabenverwaltung)
- **Größe:** 43 Klassen (~1,5% der Codebasis)
- **Verantwortung:** Projekte, Aufgaben, Kalender, Zeiterfassung
- **Ausgehende Abhängigkeiten:** 
  - content: 2 (Dokumente)
- **API:** REST für Aufgabenverwaltung, iCal-Integration
- **Priorität:** ⭐⭐⭐
- **Geschätzte Dauer:** 3-4 Wochen

**6. Shipment Service** (Versandverwaltung)
- **Größe:** 25 Klassen (~0,9% der Codebasis)
- **Verantwortung:** Versand, Carrier-Integration (UPS, USPS)
- **Ausgehende Abhängigkeiten:** 
  - product: 4 (Versandgewicht)
  - party: 4 (Adressen)
- **API:** REST für Versandverwaltung, Tracking
- **Priorität:** ⭐⭐⭐
- **Geschätzte Dauer:** 3-4 Wochen

**7. Manufacturing Service** (Produktion)
- **Größe:** 86 Klassen (~3% der Codebasis)
- **Verantwortung:** Produktionsplanung, Stücklisten
- **Ausgehende Abhängigkeiten:** 
  - order: 6 (Produktionsaufträge)
  - product: 4 (Stücklisten)
  - content: 1 (Dokumente)
- **API:** REST für Produktionsplanung
- **Priorität:** ⭐⭐
- **Geschätzte Dauer:** 4-5 Wochen

#### 🔴 **Tier 3: Komplexe Services (Zuletzt)**

**8. Order Service** (Bestellverwaltung)
- **Größe:** 281 Klassen (~10% der Codebasis)
- **Verantwortung:** Bestellungen, Shopping Cart, Promotions
- **Ausgehende Abhängigkeiten:** 
  - product: 108 (sehr hoch!)
  - party: 28 (hoch)
  - accounting: 4
  - content: 3
- **Eingehende Abhängigkeiten:**
  - product: 19
  - accounting: 18
  - manufacturing: 6
- **Herausforderung:** Hohe Kopplung, Orchestrierung mehrerer Services
- **Pattern:** Saga-Pattern für verteilte Transaktionen, Event Sourcing
- **Priorität:** ⭐⭐⭐⭐
- **Geschätzte Dauer:** 8-12 Wochen

**9. Accounting Service** (Buchhaltung)
- **Größe:** 277 Klassen (~10% der Codebasis)
- **Verantwortung:** Rechnungen, Zahlungen, Buchhaltung
- **Ausgehende Abhängigkeiten:** 
  - order: 18 (Rechnungsstellung)
  - product: 10 (Preise)
  - party: 6 (Rechnungsempfänger)
- **Eingehende Abhängigkeiten:**
  - party: 7
  - order: 4
- **Herausforderung:** Transaktionale Konsistenz, Compliance
- **Pattern:** Event Sourcing für Audit-Trail
- **Priorität:** ⭐⭐⭐⭐
- **Geschätzte Dauer:** 8-10 Wochen

### 3.2 Bounded Contexts nach Domain-Driven Design

```
┌─────────────────────────────────────────────────────────────┐
│                    CUSTOMER DOMAIN                          │
│  ┌──────────────┐         ┌───────────────┐                 │
│  │ Party Service│◄────────┤Content Service│                 │
│  │  (83 Klassen)│         │ (133 Klassen) │                 │
│  └──────────────┘         └───────────────┘                 │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    CATALOG DOMAIN                           │
│  ┌───────────────┐         ┌────────────────┐               │
│  │Product Service│────────►│Content Service │               │
│  │ (334 Klassen) │         │                │               │
│  └───────────────┘         └────────────────┘               │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    ORDER DOMAIN                             │
│  ┌──────────────┐                                           │
│  │ Order Service│                                           │
│  │ (281 Klassen)│                                           │
│  └──────┬───────┘                                           │
│         │ depends on (via API):                             │
│         ├──► Product Service (108 deps)                     │
│         ├──► Party Service (28 deps)                        │
│         ├──► Accounting Service (4 deps)                    │
│         └──► Content Service (3 deps)                       │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    FULFILLMENT DOMAIN                       │
│  ┌──────────────┐         ┌──────────────┐                  │
│  │Shipment Svc  │────────►│ Party Service│                  │
│  │ (25 Klassen) │         │ Product Svc  │                  │
│  └──────────────┘         └──────────────┘                  │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    FINANCE DOMAIN                           │
│  ┌──────────────┐                                           │
│  │Accounting Svc│                                           │
│  │ (277 Klassen)│                                           │
│  └──────┬───────┘                                           │
│         │ depends on (via API):                             │
│         ├──► Order Service (18 deps)                        │
│         ├──► Product Service (10 deps)                      │
│         └──► Party Service (6 deps)                         │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    PRODUCTION DOMAIN                        │
│  ┌──────────────┐                                           │
│  │Manufacturing │                                           │
│  │  (86 Klassen)│                                           │
│  └──────┬───────┘                                           │
│         │ depends on (via API):                             │
│         ├──► Order Service (6 deps)                         │
│         ├──► Product Service (4 deps)                       │
│         └──► Content Service (1 dep)                        │
└─────────────────────────────────────────────────────────────┘
```

## 4. Refactoring-Strategie: Strangler Fig Pattern

### 4.1 Phasenplan

#### **Phase 0: Vorbereitung (2-4 Wochen)**
- [ ] API-Gateway aufsetzen (Kong, AWS API Gateway, oder Spring Cloud Gateway)
- [ ] Service-Mesh evaluieren (Istio, Linkerd)
- [ ] Event-Bus einrichten (Kafka, RabbitMQ)
- [ ] Monitoring/Observability (Prometheus, Grafana, Jaeger)
- [ ] CI/CD-Pipeline für Microservices
- [ ] Entwicklungsumgebung mit Docker Compose

**Deliverables:**
- Funktionierende Infrastruktur
- Deployment-Pipeline
- Monitoring-Dashboard

#### **Phase 1: Party Service extrahieren (4-6 Wochen)**
**Warum zuerst?** Fundament für andere Services, geringe Komplexität (nur 83 Klassen), wenige ausgehende Abhängigkeiten (9)

**Woche 1-2: Analyse & Design**
1. **Datenbank-Schema extrahieren**
   - Party, Person, PartyGroup, PartyRole
   - PostalAddress, TelecomNumber, ContactMech
   - PartyRelationship, PartyClassification
   - Eigene PostgreSQL/MySQL-Datenbank

2. **API-Design**
   - REST API mit OpenAPI 3.0 Spezifikation
   - Endpoints: CRUD für Parties, Suche, Beziehungen
   - Events: PartyCreated, PartyUpdated, PartyDeleted

**Woche 3-4: Implementierung**
3. **Service implementieren**
   - Spring Boot 3.x mit Java 17+
   - Spring Data JPA
   - REST Controller mit Validierung
   - Event-Publishing zu Kafka

4. **Anti-Corruption Layer in OFBiz**
   ```java
   public class PartyServiceAdapter {
       private final PartyServiceClient partyService;
       private final Delegator delegator;
       
       public GenericValue getParty(String partyId) {
           // Dual-Read: Versuche zuerst neuen Service
           try {
               PartyDTO party = partyService.getParty(partyId);
               return convertToGenericValue(party);
           } catch (Exception e) {
               // Fallback zu OFBiz
               return delegator.findOne("Party", 
                   UtilMisc.toMap("partyId", partyId), false);
           }
       }
   }
   ```

**Woche 5: Dual-Write-Phase**
5. **Synchronisation**
   - OFBiz schreibt weiterhin in alte DB
   - Zusätzlich Sync zu neuem Party Service (via Event oder direkter Call)
   - Lesezugriffe noch aus OFBiz
   - Daten-Konsistenz-Checks

**Woche 6: Migration & Cutover**
6. **Schrittweise Umstellung**
   - Neue Features nutzen Party Service API
   - Monitoring der Service-Performance
   - Schrittweise Migration bestehender Clients
   - Alte Party-Tabellen read-only

**Erfolgskriterien:**
- ✅ Party Service läuft stabil in Produktion
- ✅ Alle CRUD-Operationen funktionieren
- ✅ Events werden korrekt publiziert
- ✅ Performance ist akzeptabel (< 100ms für einfache Queries)
- ✅ Monitoring zeigt keine Fehler

#### **Phase 2: Content Service extrahieren (3-4 Wochen)**
Ähnlicher Prozess wie Phase 1, aber mit Fokus auf:

**Besonderheiten:**
- Datei-Storage (S3, MinIO) statt nur Datenbank
- CDN-Integration für schnelle Auslieferung
- Metadaten-Verwaltung in Datenbank
- Thumbnail-Generierung
- Virus-Scanning für Uploads

**API:**
- Upload/Download von Dateien
- Metadaten-CRUD
- Suche nach Content
- Versionierung

**Geschätzte Dauer:** 3-4 Wochen

#### **Phase 3: Product Service extrahieren (6-8 Wochen)**
**Herausforderung:** Zentrale Rolle (334 Klassen), viele Abhängigkeiten (108 von Order)

**Woche 1-2: Datenmodell & API-Design**
1. **Datenmodell**
   - Product, ProductCategory, ProductPrice
   - ProductFeature, ProductAssoc
   - Inventory (InventoryItem)
   - ProductContent (Referenz zu Content Service)

2. **API-Design**
   - GraphQL für flexible Produktabfragen
   - REST für CRUD
   - Event-Publishing bei Preisänderungen, Lagerbestandsänderungen

**Woche 3-5: Implementierung**
3. **Service implementieren**
   - Spring Boot mit GraphQL
   - Komplexe Queries (Kategorien, Features, Preise)
   - Inventory-Management

4. **Caching-Strategie**
   - Redis für häufig abgefragte Produkte
   - Cache-Invalidierung via Events
   - Cache-Warming für beliebte Produkte

**Woche 6-7: Integration & Migration**
5. **Integration mit anderen Services**
   - Content Service für Produktbilder
   - Party Service für Lieferanten
   - Event-Handling für Bestellungen

**Woche 8: Cutover**
6. **Produktiv-Schaltung**
   - Performance-Tests
   - Load-Tests
   - Monitoring

**Geschätzte Dauer:** 6-8 Wochen

#### **Phase 4: Order Service extrahieren (8-12 Wochen)**
**Herausforderung:** Komplexe Orchestrierung (281 Klassen), hohe Kopplung (108 deps zu Product)

**Woche 1-3: Saga-Pattern Design**
1. **Saga-Pattern implementieren**
   - **Order Creation Saga:**
     1. Validate Order
     2. Reserve Inventory (Product Service)
     3. Validate Customer (Party Service)
     4. Calculate Prices (Product Service)
     5. Create Order
   
   - **Payment Processing Saga:**
     1. Authorize Payment (Accounting Service)
     2. Capture Payment
     3. Update Order Status
   
   - **Fulfillment Saga:**
     1. Create Shipment (Shipment Service)
     2. Update Inventory (Product Service)
     3. Send Notification (Party Service)

2. **Compensating Transactions**
   - Rollback bei Fehlern
   - Idempotenz sicherstellen

**Woche 4-8: Event Sourcing & CQRS**
3. **Event Sourcing**
   - OrderCreated, OrderItemAdded, OrderPlaced
   - OrderPaid, OrderShipped, OrderCompleted
   - OrderCancelled, OrderRefunded
   - Event Store (EventStoreDB, Kafka)

4. **CQRS**
   - Command-Side: Order-Mutationen
   - Query-Side: Order-Abfragen (materialized views)
   - Separate Read-Models für verschiedene Use Cases

**Woche 9-11: Integration & Testing**
5. **Integration**
   - Product Service (Produktdaten, Preise)
   - Party Service (Kundendaten)
   - Accounting Service (Zahlungen)
   - Shipment Service (Versand)

6. **Testing**
   - Unit-Tests
   - Integration-Tests
   - End-to-End-Tests
   - Chaos Engineering

**Woche 12: Cutover**
7. **Produktiv-Schaltung**
   - Canary Deployment
   - Feature Flags
   - Monitoring & Alerting

**Geschätzte Dauer:** 8-12 Wochen

#### **Phase 5: Weitere Services (je 4-8 Wochen)**
- **Shipment Service** (3-4 Wochen)
- **Accounting Service** (8-10 Wochen)
- **WorkEffort Service** (3-4 Wochen)
- **Manufacturing Service** (4-5 Wochen)
- **Marketing Service** (2-3 Wochen)

### 4.2 Technische Patterns

#### **Anti-Corruption Layer (ACL)**
Für jeden neuen Service:
```java
// Adapter zwischen OFBiz und neuem Service
public class PartyServiceAdapter {
    private final PartyServiceClient partyService;
    private final Delegator delegator; // OFBiz Entity Engine
    
    public GenericValue getParty(String partyId) {
        // Ruft neuen Service auf
        PartyDTO party = partyService.getParty(partyId);
        // Konvertiert zu OFBiz GenericValue
        return convertToGenericValue(party);
    }
    
    public void createParty(GenericValue partyValue) {
        // Konvertiert von OFBiz zu DTO
        PartyDTO party = convertFromGenericValue(partyValue);
        // Ruft neuen Service auf
        partyService.createParty(party);
        // Publiziert Event
        eventPublisher.publish(new PartyCreatedEvent(party));
    }
}
```

#### **Database per Service**
Jeder Service hat seine eigene Datenbank:
- Keine Shared Database
- Datenkonsistenz via Events
- Eventual Consistency akzeptieren
- Saga-Pattern für verteilte Transaktionen

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ Party Svc   │     │ Product Svc │     │ Order Svc   │
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │
       ▼                   ▼                   ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ Party DB    │     │ Product DB  │     │ Order DB    │
│ (PostgreSQL)│     │ (PostgreSQL)│     │ (PostgreSQL)│
└─────────────┘     └─────────────┘     └─────────────┘
```

#### **API Gateway Pattern**
```
Client → API Gateway → [Party Service]
                    → [Product Service]
                    → [Order Service]
                    → [Content Service]
                    → [Legacy OFBiz]
```

**Vorteile:**
- Einheitlicher Einstiegspunkt
- Authentifizierung/Autorisierung zentral
- Rate Limiting
- Request/Response Transformation
- Routing zu alten/neuen Services

#### **Event-Driven Architecture**
```
Order Service → OrderPlaced Event → Kafka
                                   ↓
                     ┌──────────────┼──────────────┐
                     ↓              ↓              ↓
             Accounting Svc   Shipment Svc   Inventory Svc
```

**Event-Typen:**
- **Domain Events:** OrderPlaced, ProductPriceChanged
- **Integration Events:** PartyCreated, InventoryReserved
- **Command Events:** CreateShipment, ProcessPayment

**Event Schema:**
```json
{
  "eventId": "uuid",
  "eventType": "OrderPlaced",
  "timestamp": "2026-01-17T12:00:00Z",
  "aggregateId": "order-123",
  "version": 1,
  "payload": {
    "orderId": "order-123",
    "customerId": "party-456",
    "items": [...],
    "totalAmount": 99.99
  }
}
```

#### **Saga Pattern für verteilte Transaktionen**
```
Order Saga Coordinator
  ├─► 1. Reserve Inventory (Product Service)
  │     └─► Success → Continue
  │     └─► Failure → Abort
  ├─► 2. Authorize Payment (Accounting Service)
  │     └─► Success → Continue
  │     └─► Failure → Compensate (Release Inventory)
  ├─► 3. Create Shipment (Shipment Service)
  │     └─► Success → Complete
  │     └─► Failure → Compensate (Refund, Release Inventory)
  └─► 4. Complete Order
```

## 5. Risiken und Mitigationsstrategien

### 5.1 Risiken

| Risiko | Wahrscheinlichkeit | Impact | Mitigation |
|--------|-------------------|--------|------------|
| **Datenkonsistenz** | Hoch | Hoch | Saga-Pattern, Event Sourcing, Compensating Transactions, Monitoring |
| **Performance-Degradation** | Mittel | Hoch | Caching (Redis), API-Optimierung, CDN, Monitoring, Load-Tests |
| **Komplexität steigt** | Hoch | Mittel | Service-Mesh, Observability, Dokumentation, Training |
| **Framework-Lock-in** | Hoch | Hoch | Schrittweise Migration, ACL-Pattern, Dual-Write-Phase |
| **Team-Überforderung** | Mittel | Hoch | Training, externe Expertise, Pilotprojekt, Pair Programming |
| **Netzwerk-Latenz** | Mittel | Mittel | Service-Mesh, Caching, Async Communication, Circuit Breaker |
| **Debugging schwieriger** | Hoch | Mittel | Distributed Tracing (Jaeger), Correlation IDs, Centralized Logging |
| **Deployment-Komplexität** | Mittel | Mittel | CI/CD-Automation, Kubernetes, Helm Charts, GitOps |

### 5.2 Erfolgskriterien

- ✅ Jeder Service ist unabhängig deploybar
- ✅ Keine direkten Datenbank-Zugriffe zwischen Services
- ✅ API-First-Ansatz mit OpenAPI/GraphQL-Spezifikation
- ✅ Automatisierte Tests (Unit, Integration, E2E) mit >80% Coverage
- ✅ Monitoring und Alerting für jeden Service (SLOs definiert)
- ✅ Rollback-Strategie für jeden Service (< 5 Minuten)
- ✅ Performance-Ziele erreicht (95th Percentile < 200ms)
- ✅ Dokumentation vollständig (API-Docs, Runbooks, Architecture Decision Records)

## 6. Tooling und Technologie-Stack

### 6.1 Empfohlener Stack

**Service-Implementierung:**
- **Framework:** Spring Boot 3.x (Java 17+)
- **Service Discovery:** Spring Cloud Netflix Eureka oder Consul
- **Configuration:** Spring Cloud Config oder Consul KV
- **Data Access:** Spring Data JPA / R2DBC für reaktive Anwendungen

**API:**
- **REST:** Spring MVC / WebFlux
- **GraphQL:** Spring for GraphQL
- **Documentation:** OpenAPI 3.0 (Springdoc)
- **Validation:** Jakarta Bean Validation

**Messaging:**
- **Event Streaming:** Apache Kafka (Event Sourcing, Domain Events)
- **Message Queue:** RabbitMQ (Command/Request-Response)
- **Schema Registry:** Confluent Schema Registry (Avro)

**Datenbanken:**
- **Relational:** PostgreSQL 15+ (JSONB für flexible Daten)
- **Document:** MongoDB (Content, Logs)
- **Cache:** Redis 7+ (Caching, Sessions, Rate Limiting)
- **Search:** Elasticsearch (Produktsuche, Logs)

**Infrastruktur:**
- **Container:** Docker
- **Orchestration:** Kubernetes (EKS, GKE, AKS)
- **Package Manager:** Helm Charts
- **Service Mesh:** Istio oder Linkerd (Traffic Management, Security)
- **API Gateway:** Kong oder AWS API Gateway

**Observability:**
- **Metrics:** Prometheus + Grafana
- **Logging:** ELK Stack (Elasticsearch, Logstash, Kibana) oder Loki
- **Tracing:** Jaeger oder Zipkin
- **APM:** New Relic oder Datadog (optional)

**CI/CD:**
- **Version Control:** Git (GitHub, GitLab)
- **CI/CD:** GitHub Actions, GitLab CI, oder Jenkins
- **Artifact Repository:** Nexus oder Artifactory
- **Container Registry:** Docker Hub, ECR, GCR

**Security:**
- **Authentication:** OAuth 2.0 / OpenID Connect
- **Authorization:** Spring Security, Keycloak
- **Secrets Management:** HashiCorp Vault oder AWS Secrets Manager
- **API Security:** Rate Limiting, WAF

## 7. Größenordnung und Aufwand

### 7.1 Übersicht

| Service | Klassen | % der Codebasis | Geschätzte Dauer | Team-Größe |
|---------|---------|-----------------|------------------|------------|
| **Party** | 83 | 2,9% | 4-6 Wochen | 2-3 Entwickler |
| **Content** | 133 | 4,7% | 3-4 Wochen | 2 Entwickler |
| **Marketing** | 14 | 0,5% | 2-3 Wochen | 1-2 Entwickler |
| **Product** | 334 | 11,9% | 6-8 Wochen | 3-4 Entwickler |
| **WorkEffort** | 43 | 1,5% | 3-4 Wochen | 2 Entwickler |
| **Shipment** | 25 | 0,9% | 3-4 Wochen | 2 Entwickler |
| **Manufacturing** | 86 | 3,1% | 4-5 Wochen | 2-3 Entwickler |
| **Order** | 281 | 10,0% | 8-12 Wochen | 3-4 Entwickler |
| **Accounting** | 277 | 9,8% | 8-10 Wochen | 3-4 Entwickler |

**Gesamt:** ~1.276 Klassen (45% der Codebasis), 45-60 Wochen Entwicklungszeit

### 7.2 Zeitplan (bei 3-4 Entwicklern)

| Phase | Dauer | Kumulativ |
|-------|-------|-----------|
| **Phase 0:** Infrastruktur | 2-4 Wochen | 4 Wochen |
| **Phase 1:** Party Service | 4-6 Wochen | 10 Wochen |
| **Phase 2:** Content Service | 3-4 Wochen | 14 Wochen |
| **Phase 3:** Product Service | 6-8 Wochen | 22 Wochen |
| **Phase 4:** Order Service | 8-12 Wochen | 34 Wochen |
| **Phase 5:** Weitere Services | 16-24 Wochen | 58 Wochen |

**Gesamtdauer:** ~12-14 Monate (bei paralleler Entwicklung einiger Services)

## 8. Nächste Schritte

### 8.1 Sofort (Woche 1-2)
1. ✅ Diese Analyse mit Stakeholdern reviewen
2. [ ] Pilotprojekt definieren (Empfehlung: **Party Service**)
3. [ ] Team zusammenstellen (2-3 Entwickler mit Microservices-Erfahrung)
4. [ ] Budget und Ressourcen freigeben
5. [ ] Infrastruktur-Setup starten (AWS/GCP/Azure Account, Kubernetes Cluster)

### 8.2 Kurzfristig (Monat 1-2)
1. [ ] **Party Service als Proof of Concept**
   - Datenmodell extrahieren
   - REST API implementieren
   - Dual-Write-Phase
   - Monitoring aufsetzen
2. [ ] API-Gateway aufsetzen (Kong oder AWS API Gateway)
3. [ ] Kafka-Cluster produktiv nehmen
4. [ ] Monitoring-Stack implementieren (Prometheus, Grafana, Jaeger)
5. [ ] CI/CD-Pipeline für Microservices

### 8.3 Mittelfristig (Monat 3-6)
1. [ ] Content Service extrahieren
2. [ ] Product Service extrahieren
3. [ ] Event-Bus produktiv nehmen
4. [ ] Erste Services in Produktion (Party, Content)
5. [ ] Lessons Learned dokumentieren

### 8.4 Langfristig (Monat 7-18)
1. [ ] Order Service extrahieren
2. [ ] Accounting Service extrahieren
3. [ ] Weitere Services nach Bedarf (Shipment, Manufacturing, WorkEffort)
4. [ ] Legacy OFBiz schrittweise abbauen
5. [ ] Vollständige Migration abschließen

## 9. Weitere Analysen

Für detailliertere Analysen können folgende Neo4j-Queries verwendet werden:

```cypher
// Alle Klassen eines Moduls mit ihren Abhängigkeiten
MATCH (c:Class)
WHERE c.fqn STARTS WITH 'org.apache.ofbiz.party'
OPTIONAL MATCH (c)-[d:DEPENDS_ON]->(target:Class)
WHERE target.fqn STARTS WITH 'org.apache.ofbiz'
RETURN c.fqn, COUNT(d) as dependencies
ORDER BY dependencies DESC

// Schnittstellen zwischen zwei Modulen
MATCH (source:Class)-[:DEPENDS_ON]->(target:Class)
WHERE source.fqn STARTS WITH 'org.apache.ofbiz.order'
  AND target.fqn STARTS WITH 'org.apache.ofbiz.product'
RETURN DISTINCT 
  split(source.fqn, '.')[4] as orderClass, 
  split(target.fqn, '.')[4] as productClass
LIMIT 20

// Gemeinsam genutzte Typen (potenzielle Shared Kernel)
MATCH (c:Class)<-[:DEPENDS_ON]-(dependent:Class)
WHERE c.fqn STARTS WITH 'org.apache.ofbiz'
  AND dependent.fqn STARTS WITH 'org.apache.ofbiz'
WITH c, split(c.fqn, '.')[3] as module, 
     COUNT(DISTINCT split(dependent.fqn, '.')[3]) as usageCount
WHERE usageCount > 3
RETURN module, c.fqn, usageCount
ORDER BY usageCount DESC
LIMIT 20

// Zyklische Abhängigkeiten prüfen
MATCH path = (c1:Class)-[:DEPENDS_ON*2..5]->(c1)
WHERE c1.fqn STARTS WITH 'org.apache.ofbiz'
RETURN DISTINCT 
  [node IN nodes(path) | split(node.fqn, '.')[3]] as cycle
LIMIT 10
```

## 10. Fazit

Die OFBiz-Codebasis ist gut strukturiert mit klaren fachlichen Modulen, aber stark an das Framework gekoppelt. Die Dekomposition in Microservices ist machbar, erfordert aber:

### ✅ Stärken der aktuellen Architektur
- **Klare Modularisierung:** 45% Business-Logik, 46% Framework, 9% Sonstige
- **Keine zyklischen Abhängigkeiten** zwischen Business-Modulen
- **Kleine, isolierte Module** (Party: 83 Klassen, Marketing: 14 Klassen) sind leicht extrahierbar
- **Durchschnittlich 5,8 Zeilen pro Methode** deutet auf gute Code-Qualität hin

### ⚠️ Herausforderungen
- **Framework-Kopplung:** Alle Module hängen stark von Entity-Engine und Service-Engine ab
- **Zentrale Module:** Product (334 Klassen) und Order (281 Klassen) sind Hubs mit vielen Abhängigkeiten
- **Komplexe Orchestrierung:** Order Service benötigt 108 Abhängigkeiten zu Product Service

### 🎯 Empfohlenes Vorgehen
1. **Schrittweises Vorgehen** (Strangler Fig Pattern)
2. **Starkes Team** mit Microservices-Erfahrung (3-4 Entwickler)
3. **Gute Infrastruktur** (API Gateway, Event Bus, Monitoring)
4. **Zeit und Geduld** (12-14 Monate für vollständige Migration)
5. **Frühe Erfolge** durch einfache Services (Party, Content, Marketing)

### 📊 Realistische Einschätzung
- **Aufwand:** ~12-14 Monate mit 3-4 Entwicklern
- **Kosten:** Signifikante Investition in Infrastruktur und Team
- **Risiko:** Mittel bis Hoch (Datenkonsistenz, Performance, Komplexität)
- **Nutzen:** Hoch (Skalierbarkeit, Unabhängige Deployments, Technologie-Freiheit)

Die empfohlene Reihenfolge **Party → Content → Product → Order → Rest** minimiert Risiken und ermöglicht frühe Erfolge. Der Party Service ist mit nur 83 Klassen und 9 ausgehenden Abhängigkeiten der ideale Startpunkt für einen Proof of Concept.

---

**Erstellt am:** 17. Januar 2026  
**Datenquelle:** Neo4j-Datenbank mit 2.818 OFBiz-Klassen (ohne externe Bibliotheken)  
**Analysierte Version:** OFBiz aus `/Users/oliverwidder/dev/ofbiz/build/libs/ofbiz.jar`
