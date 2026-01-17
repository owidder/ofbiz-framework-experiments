# Party Service - Detaillierte Kommunikationsanalyse

## Executive Summary

Diese Analyse listet alle Klassen des neuen Party Service, alle aufrufenden Klassen und gibt konkrete Empfehlungen für synchrone (REST) vs. asynchrone (Kafka) Kommunikation.

### Schnellübersicht

| Kategorie | Anzahl | Empfehlung |
|-----------|--------|------------|
| **Party Service Klassen** | 11 | Im neuen Microservice |
| **Aufrufende Klassen** | 43 | Benötigen Anpassung |
| **Synchrone Aufrufe (REST)** | 40 | 93% der Fälle |
| **Asynchrone Aufrufe (Kafka)** | 3 | 7% der Fälle |

## 1. Party Service - Interne Klassen

Diese Klassen werden Teil des neuen Party Microservice:

### 1.1 Core Party Management

| # | Klasse | Package | Verantwortung | Neue API |
|---|--------|---------|---------------|----------|
| 1 | **PartyServices** | `org.apache.ofbiz.party.party` | CRUD für Parties | `POST/GET/PUT/DELETE /api/v1/parties` |
| 2 | **PartyWorker** | `org.apache.ofbiz.party.party` | Party-Utilities, Namen | `GET /api/v1/parties/{id}` |
| 3 | **PartyHelper** | `org.apache.ofbiz.party.party` | Party-Helper-Methoden | `GET /api/v1/parties/{id}/name` |
| 4 | **PartyTypeHelper** | `org.apache.ofbiz.party.party` | Party-Typ-Logik | Intern (kein API) |

### 1.2 Contact Management

| # | Klasse | Package | Verantwortung | Neue API |
|---|--------|---------|---------------|----------|
| 5 | **ContactMechServices** | `org.apache.ofbiz.party.contact` | CRUD für Kontakte | `POST/GET/PUT/DELETE /api/v1/parties/{id}/contacts` |
| 6 | **ContactMechWorker** | `org.apache.ofbiz.party.contact` | Kontakt-Utilities | `GET /api/v1/parties/{id}/contacts/{contactId}` |
| 7 | **ContactHelper** | `org.apache.ofbiz.party.contact` | Kontakt-Helper | `GET /api/v1/parties/{id}/contacts?type=EMAIL` |

### 1.3 Relationships

| # | Klasse | Package | Verantwortung | Neue API |
|---|--------|---------|---------------|----------|
| 8 | **PartyRelationshipServices** | `org.apache.ofbiz.party.party` | CRUD für Beziehungen | `POST/GET/PUT/DELETE /api/v1/parties/{id}/relationships` |
| 9 | **PartyRelationshipHelper** | `org.apache.ofbiz.party.party` | Beziehungs-Utilities | `GET /api/v1/parties/{id}/relationships?type=CUSTOMER` |

### 1.4 Communication & Content

| # | Klasse | Package | Verantwortung | Neue API |
|---|--------|---------|---------------|----------|
| 10 | **CommunicationEventServices** | `org.apache.ofbiz.party.communication` | Kommunikationshistorie | `GET /api/v1/parties/{id}/communications` |
| 11 | **PartyContentWrapper** | `org.apache.ofbiz.party.content` | Party-Content-Wrapper | Intern (nutzt Content Service) |

## 2. Aufrufende Klassen - Detaillierte Analyse

### 2.1 Order Module (21 Klassen)

#### 2.1.1 ShoppingCart
**FQN:** `org.apache.ofbiz.order.shoppingcart.ShoppingCart`

**Verwendete Party-Klassen:**
- `ContactMechWorker` - Lieferadressen abrufen
- `ContactHelper` - Kontaktdaten validieren

**Aktueller Code-Pattern:**
```java
// Lieferadresse abrufen
GenericValue address = ContactMechWorker.getPostalAddress(
    delegator, partyId, contactMechId);
```

**Neuer Code-Pattern:**
```java
// REST API Call
ContactMechDTO address = partyServiceClient.getContactMech(
    partyId, contactMechId);
```

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Shopping Cart benötigt sofortige Antwort
- ✅ Benutzer wartet auf Validierung
- ✅ Transaktionaler Kontext (Checkout-Prozess)
- ✅ Fehler müssen sofort angezeigt werden

**Performance-Optimierung:**
- Cache für häufig abgefragte Adressen (Redis, 1h TTL)
- Batch-API für mehrere Kontakte auf einmal

**API-Endpoint:**
```
GET /api/v1/parties/{partyId}/contacts/{contactMechId}
Response Time SLA: < 100ms (p95)
```

---

#### 2.1.2 OrderServices
**FQN:** `org.apache.ofbiz.order.order.OrderServices`

**Verwendete Party-Klassen:**
- `ContactHelper` - Kontaktdaten
- `PartyWorker` - Party-Namen
- `ContactMechWorker` - Adressen

**Aktueller Code-Pattern:**
```java
// Party-Name abrufen
String partyName = PartyWorker.getPartyName(delegator, partyId);

// Kontaktdaten abrufen
GenericValue contact = ContactHelper.getContactMech(
    party, "BILLING_LOCATION", "POSTAL_ADDRESS", false);
```

**Neuer Code-Pattern:**
```java
// REST API Call
PartyDTO party = partyServiceClient.getParty(partyId);
String partyName = party.getDisplayName();

// Kontaktdaten mit Filter
List<ContactMechDTO> contacts = partyServiceClient.getContacts(
    partyId, "BILLING_LOCATION", "POSTAL_ADDRESS");
```

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Bestellerstellung ist transaktional
- ✅ Fehler bei Party-Daten müssen sofort behandelt werden
- ✅ Benutzer wartet auf Bestätigung

**Performance-Optimierung:**
- GraphQL für flexible Datenabfrage (nur benötigte Felder)
- Batch-API für Party + Kontakte in einem Call

**API-Endpoint:**
```
GET /api/v1/parties/{partyId}?include=contacts,roles
Response Time SLA: < 150ms (p95)
```

---

#### 2.1.3 CheckOutHelper
**FQN:** `org.apache.ofbiz.order.shoppingcart.CheckOutHelper`

**Verwendete Party-Klassen:**
- `ContactMechWorker` - Adressen validieren
- `ContactHelper` - Kontaktdaten

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Checkout ist kritischer Pfad
- ✅ Sofortige Validierung erforderlich
- ✅ Benutzer-Interaktion

**API-Endpoint:**
```
POST /api/v1/parties/{partyId}/contacts/validate
Body: { "contactMechId": "10001", "purpose": "SHIPPING_LOCATION" }
Response Time SLA: < 100ms (p95)
```

---

#### 2.1.4 CheckOutEvents
**FQN:** `org.apache.ofbiz.order.shoppingcart.CheckOutEvents`

**Verwendete Party-Klassen:**
- `PartyWorker` - Party-Informationen

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Event-Handler benötigt sofortige Daten
- ✅ Teil des Checkout-Flows

---

#### 2.1.5 ShippingEvents
**FQN:** `org.apache.ofbiz.order.shoppingcart.shipping.ShippingEvents`

**Verwendete Party-Klassen:**
- `ContactMechWorker` - Lieferadressen

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Versandkosten-Berechnung benötigt Adresse sofort
- ✅ Benutzer wartet auf Ergebnis

---

#### 2.1.6 CheckInits
**FQN:** `org.apache.ofbiz.order.entry.CheckInits`

**Verwendete Party-Klassen:**
- `ContactHelper` - Kontaktdaten beim Checkout-Init

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Checkout-Initialisierung benötigt sofortige Daten
- ✅ Benutzer wartet auf Checkout-Start

---

#### 2.1.7 CheckoutOptions
**FQN:** `org.apache.ofbiz.order.entry.CheckoutOptions`

**Verwendete Party-Klassen:**
- `ContactHelper` - Kontaktdaten für Checkout-Optionen

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Teil des Checkout-Flows
- ✅ Benutzer-Interaktion erforderlich

---

#### 2.1.8 CheckoutReview
**FQN:** `org.apache.ofbiz.order.entry.CheckoutReview`

**Verwendete Party-Klassen:**
- `ContactHelper` - Kontaktdaten
- `PartyWorker` - Party-Namen

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Checkout-Review ist kritischer Pfad
- ✅ Benutzer prüft Bestelldaten vor Abschluss

---

#### 2.1.9 CheckoutShippingAddress
**FQN:** `org.apache.ofbiz.order.entry.CheckoutShippingAddress`

**Verwendete Party-Klassen:**
- `ContactHelper` - Lieferadressen

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Adressauswahl im Checkout
- ✅ Sofortige Validierung erforderlich

---

#### 2.1.10 OptionSettings
**FQN:** `org.apache.ofbiz.order.entry.OptionSettings`

**Verwendete Party-Klassen:**
- `ContactHelper` - Kontaktdaten für Optionen

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Benutzer konfiguriert Bestelloptionen
- ✅ Interaktiver Prozess

---

#### 2.1.11 ShipSettings
**FQN:** `org.apache.ofbiz.order.entry.ShipSettings`

**Verwendete Party-Klassen:**
- `ContactHelper` - Versandeinstellungen
- `ContactMechWorker` - Versandadressen (via Closure)

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Versandeinstellungen im Checkout
- ✅ Benutzer wartet auf Optionen

---

#### 2.1.12 SplitShip
**FQN:** `org.apache.ofbiz.order.entry.SplitShip`

**Verwendete Party-Klassen:**
- `ContactHelper` - Mehrere Lieferadressen

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Split-Shipment-Konfiguration
- ✅ Interaktiver Prozess

---

#### 2.1.13 CompanyHeader
**FQN:** `org.apache.ofbiz.order.order.CompanyHeader`

**Verwendete Party-Klassen:**
- `PartyContentWrapper` - Firmen-Logo/Content

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Header-Rendering benötigt Firmendaten
- ✅ Teil der Seitenansicht

---

#### 2.1.14 OrderView
**FQN:** `org.apache.ofbiz.order.order.OrderView`

**Verwendete Party-Klassen:**
- `ContactHelper` - Kontaktdaten
- `ContactMechWorker` - Adressen

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Bestellansicht benötigt Party-Daten sofort
- ✅ Benutzer-Interaktion

---

#### 2.1.15 QuickReturn
**FQN:** `org.apache.ofbiz.order.orderReturn.QuickReturn`

**Verwendete Party-Klassen:**
- `ContactHelper` - Kontaktdaten für Retoure

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Retouren-Prozess ist interaktiv
- ✅ Benutzer wartet auf Bestätigung

---

#### 2.1.16 ReturnHeader
**FQN:** `org.apache.ofbiz.order.orderReturn.ReturnHeader`

**Verwendete Party-Klassen:**
- `ContactHelper` - Kontaktdaten
- `ContactMechWorker` - Rücksendeadressen

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Retouren-Header benötigt Party-Daten
- ✅ Teil der Retouren-Ansicht

---

#### 2.1.17 GetPartyAddress
**FQN:** `org.apache.ofbiz.order.quote.GetPartyAddress`

**Verwendete Party-Klassen:**
- `ContactHelper` - Adressen für Angebote

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Angebotserstellung benötigt Adresse sofort
- ✅ Geschäftsprozess

---

#### 2.1.18 GetPartyEmailAddress
**FQN:** `org.apache.ofbiz.order.quote.GetPartyEmailAddress`

**Verwendete Party-Klassen:**
- `ContactHelper` - E-Mail-Adressen für Angebote

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Angebotserstellung benötigt E-Mail sofort
- ✅ Geschäftsprozess

---

#### 2.1.19 CreateAllocationPlan
**FQN:** `org.apache.ofbiz.order.allocationplan.CreateAllocationPlan$_run_closure1`

**Verwendete Party-Klassen:**
- `PartyHelper` - Party-Informationen für Allokationsplan

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Allokationsplan-Erstellung ist Geschäftsprozess
- ✅ Benötigt Party-Daten für Planung

---

#### 2.1.20 ViewAllocationPlan
**FQN:** `org.apache.ofbiz.order.allocationplan.ViewAllocationPlan$_run_closure1`

**Verwendete Party-Klassen:**
- `PartyHelper` - Party-Informationen für Ansicht

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Ansicht benötigt Party-Daten sofort
- ✅ Benutzer-Interaktion

---

### 2.2 Shipment Module (4 Klassen)

#### 2.2.1 UpsServices
**FQN:** `org.apache.ofbiz.shipment.thirdparty.ups.UpsServices`

**Verwendete Party-Klassen:**
- `ContactMechWorker` - Lieferadressen für UPS-API

**Aktueller Code-Pattern:**
```java
// Adresse für UPS-Integration
GenericValue address = ContactMechWorker.getPostalAddress(
    delegator, partyId, contactMechId);
String city = address.getString("city");
String postalCode = address.getString("postalCode");
```

**Neuer Code-Pattern:**
```java
// REST API Call
ContactMechDTO address = partyServiceClient.getContactMech(
    partyId, contactMechId);
String city = address.getCity();
String postalCode = address.getPostalCode();
```

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ UPS-API-Call benötigt Adresse sofort
- ✅ Versandlabel-Erstellung ist synchron
- ✅ Fehler müssen sofort behandelt werden

**Performance-Optimierung:**
- Cache für Versandadressen (Redis, 24h TTL)
- Adressen ändern sich selten

**API-Endpoint:**
```
GET /api/v1/parties/{partyId}/contacts/{contactMechId}?type=POSTAL_ADDRESS
Response Time SLA: < 100ms (p95)
```

---

#### 2.2.2 UspsServices
**FQN:** `org.apache.ofbiz.shipment.thirdparty.usps.UspsServices`

**Verwendete Party-Klassen:**
- `ContactMechWorker` - Lieferadressen für USPS-API

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:** Identisch zu UpsServices

---

#### 2.2.3 FedexServices
**FQN:** `org.apache.ofbiz.shipment.thirdparty.fedex.FedexServices`

**Verwendete Party-Klassen:**
- `PartyHelper` - Party-Informationen für FedEx

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:** Identisch zu UpsServices

---

#### 2.2.4 ShipmentServices
**FQN:** `org.apache.ofbiz.shipment.shipment.ShipmentServices`

**Verwendete Party-Klassen:**
- `PartyWorker` - Party-Daten bei Versanderstellung

**Aktueller Code-Pattern:**
```java
// Party-Name für Versandlabel
String partyName = PartyWorker.getPartyName(delegator, partyId);
```

**Neuer Code-Pattern (Option 1 - Synchron):**
```java
// REST API Call
PartyDTO party = partyServiceClient.getParty(partyId);
String partyName = party.getDisplayName();
```

**Neuer Code-Pattern (Option 2 - Asynchron):**
```java
// Event-Driven
// 1. Shipment Service published ShipmentCreated Event
shipmentEventPublisher.publishShipmentCreated(shipmentId, partyId);

// 2. Party Service hört Event und enriched Daten
@KafkaListener(topics = "shipment-events")
public void onShipmentCreated(ShipmentCreatedEvent event) {
    PartyDTO party = partyService.getParty(event.getPartyId());
    // Sende enriched Event zurück
    partyEventPublisher.publishPartyDataForShipment(
        event.getShipmentId(), party);
}
```

**Kommunikationstyp:** 🟡 **HYBRID (Synchron bevorzugt)**

**Begründung:**
- ✅ **Synchron:** Einfacher, direkter Zugriff
- ⚠️ **Asynchron:** Nur wenn Versanderstellung im Hintergrund läuft

**Empfehlung:** **SYNCHRON (REST)** - Versandlabel werden meist sofort erstellt

---

### 2.3 Accounting Module (6 Klassen)

#### 2.3.1 PaymentGatewayServices
**FQN:** `org.apache.ofbiz.accounting.payment.PaymentGatewayServices`

**Verwendete Party-Klassen:**
- `ContactHelper` - Rechnungsadressen für Zahlungen

**Aktueller Code-Pattern:**
```java
// Rechnungsadresse für Payment Gateway
GenericValue billingAddress = ContactHelper.getContactMech(
    party, "BILLING_LOCATION", "POSTAL_ADDRESS", false);
```

**Neuer Code-Pattern:**
```java
// REST API Call
List<ContactMechDTO> contacts = partyServiceClient.getContacts(
    partyId, "BILLING_LOCATION", "POSTAL_ADDRESS");
ContactMechDTO billingAddress = contacts.get(0);
```

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Zahlungsprozess ist transaktional
- ✅ Payment Gateway benötigt Adresse sofort
- ✅ Fehler müssen sofort behandelt werden
- ✅ Benutzer wartet auf Zahlungsbestätigung

**Kritisch:** Zahlungen sind hochkritisch, keine Asynchronität!

**API-Endpoint:**
```
GET /api/v1/parties/{partyId}/contacts?purpose=BILLING_LOCATION&type=POSTAL_ADDRESS
Response Time SLA: < 50ms (p95) - KRITISCH!
```

---

#### 2.3.2 TaxAuthorityServices
**FQN:** `org.apache.ofbiz.accounting.tax.TaxAuthorityServices`

**Verwendete Party-Klassen:**
- `ContactMechWorker` - Adressen für Steuerberechnung

**Aktueller Code-Pattern:**
```java
// Adresse für Steuerberechnung
GenericValue address = ContactMechWorker.getPostalAddress(
    delegator, partyId, contactMechId);
String stateGeoId = address.getString("stateProvinceGeoId");
```

**Neuer Code-Pattern:**
```java
// REST API Call
ContactMechDTO address = partyServiceClient.getContactMech(
    partyId, contactMechId);
String stateGeoId = address.getStateProvinceGeoId();
```

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Steuerberechnung ist Teil des Checkout-Prozesses
- ✅ Benutzer wartet auf Gesamtpreis
- ✅ Transaktionaler Kontext

**Performance-Optimierung:**
- Cache für Adressen (Redis, 1h TTL)
- Steuerberechnung wird oft wiederholt

---

#### 2.3.3 BalanceSheet
**FQN:** `org.apache.ofbiz.accounting.reports.BalanceSheet`

**Verwendete Party-Klassen:**
- `PartyWorker` - Party-Namen für Bilanz-Report

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Report-Generierung benötigt Party-Daten
- ✅ Benutzer wartet auf Report

---

#### 2.3.4 CashFlowStatement
**FQN:** `org.apache.ofbiz.accounting.reports.CashFlowStatement`

**Verwendete Party-Klassen:**
- `PartyWorker` - Party-Namen für Cashflow-Report

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Report-Generierung benötigt Party-Daten
- ✅ Benutzer wartet auf Report

---

#### 2.3.5 IncomeStatement
**FQN:** `org.apache.ofbiz.accounting.reports.IncomeStatement`

**Verwendete Party-Klassen:**
- `PartyWorker` - Party-Namen für GuV-Report

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Report-Generierung benötigt Party-Daten
- ✅ Benutzer wartet auf Report

---

#### 2.3.6 TrialBalance
**FQN:** `org.apache.ofbiz.accounting.reports.TrialBalance$_run_closure1`

**Verwendete Party-Klassen:**
- `PartyHelper` - Party-Informationen für Saldenliste

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Report-Generierung benötigt Party-Daten
- ✅ Benutzer wartet auf Report

---

### 2.4 Product Module (6 Klassen)

#### 2.4.1 ProductStoreWorker
**FQN:** `org.apache.ofbiz.product.store.ProductStoreWorker`

**Verwendete Party-Klassen:**
- `ContactMechWorker` - Store-Kontaktdaten

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Store-Konfiguration wird beim Seitenaufruf geladen
- ✅ Selten geändert, gut cachebar

**Performance-Optimierung:**
- Aggressives Caching (Redis, 24h TTL)
- Store-Daten ändern sich sehr selten

---

#### 2.4.2 ProductSearch$SupplierConstraint
**FQN:** `org.apache.ofbiz.product.product.ProductSearch$SupplierConstraint`

**Verwendete Party-Klassen:**
- `PartyHelper` - Lieferanten-Informationen

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Produktsuche benötigt Lieferanten-Daten sofort
- ✅ Benutzer wartet auf Suchergebnisse

---

#### 2.4.3 EditContactMech (Facility)
**FQN:** `org.apache.ofbiz.product.facility.facility.EditContactMech`

**Verwendete Party-Klassen:**
- `ContactMechWorker` - Kontaktdaten für Facility

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Bearbeitung von Kontaktdaten ist interaktiv
- ✅ Benutzer wartet auf Validierung

---

#### 2.4.4 ViewContactMechs (Facility)
**FQN:** `org.apache.ofbiz.product.facility.facility.ViewContactMechs`

**Verwendete Party-Klassen:**
- `ContactMechWorker` - Kontaktdaten-Anzeige

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Ansicht benötigt Kontaktdaten sofort
- ✅ Benutzer-Interaktion

---

#### 2.4.5 ShipmentServices (Product)
**FQN:** `org.apache.ofbiz.product.shipment.ShipmentServices`

**Verwendete Party-Klassen:**
- `ContactMechWorker` - Versandadressen

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Versandprozess benötigt Adressen sofort
- ✅ Geschäftsprozess

---

#### 2.4.6 PriceServicesScript
**FQN:** `org.apache.ofbiz.product.product.price.PriceServicesScript$_getAssociatedPriceRulesConds_closure4`

**Verwendete Party-Klassen:**
- `PartyHelper` - Party-Informationen für Preisregeln

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Preisberechnung benötigt Party-Daten
- ✅ Teil des Bestellprozesses

---

### 2.5 Marketing Module (2 Klassen)

#### 2.5.1 CloneLead
**FQN:** `org.apache.ofbiz.marketing.sfa.CloneLead`

**Verwendete Party-Klassen:**
- `ContactHelper` - Kontaktdaten beim Lead-Klonen

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Lead-Klonen ist interaktiver Prozess
- ✅ Benutzer wartet auf Ergebnis

---

#### 2.5.2 MergeContacts
**FQN:** `org.apache.ofbiz.marketing.sfa.MergeContacts$_run_closure1`

**Verwendete Party-Klassen:**
- `ContactHelper` - Kontaktdaten beim Zusammenführen

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Kontakt-Merge ist interaktiver Prozess
- ✅ Benutzer wartet auf Ergebnis

---

### 2.6 HumanRes Module (2 Klassen)

#### 2.6.1 HumanResEvents
**FQN:** `org.apache.ofbiz.humanres.HumanResEvents`

**Verwendete Party-Klassen:**
- `PartyHelper` - Mitarbeiter-Informationen

**Kommunikationstyp:** 🔴 **ASYNCHRON (Kafka)**

**Begründung:**
- ✅ HR-Events sind oft Hintergrund-Prozesse
- ✅ Keine sofortige Benutzer-Interaktion
- ✅ Kann verzögert verarbeitet werden

**Event-Pattern:**
```java
// HR Event published
hrEventPublisher.publishEmployeeCreated(employeeId, partyId);

// Party Service enriched Event (optional)
@KafkaListener(topics = "hr-events")
public void onEmployeeCreated(EmployeeCreatedEvent event) {
    PartyDTO party = partyService.getParty(event.getPartyId());
    // Weitere Verarbeitung
}
```

**Kafka Topic:**
```
Topic: hr-events
Event: EmployeeCreated, EmployeeUpdated, EmployeeTerminated
Partition Key: partyId
```

---

#### 2.6.2 CategoryTree
**FQN:** `org.apache.ofbiz.humanres.category.CategoryTree$_run_closure1`

**Verwendete Party-Klassen:**
- `PartyHelper` - Party-Informationen für Kategoriebaum

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Kategoriebaum-Anzeige benötigt Party-Daten
- ✅ Benutzer-Interaktion

---

### 2.7 Weitere Module (2 Klassen)

#### 2.7.1 LoginEvents (SecurityExt)
**FQN:** `org.apache.ofbiz.securityext.login.LoginEvents`

**Verwendete Party-Klassen:**
- `ContactHelper` - Kontaktdaten beim Login

**Kommunikationstyp:** 🟢 **SYNCHRON (REST)**

**Begründung:**
- ✅ Login ist kritischer Pfad
- ✅ Benutzer wartet auf Anmeldung

---

#### 2.7.2 VCard (SFA)
**FQN:** `org.apache.ofbiz.sfa.vcard.VCard`

**Verwendete Party-Klassen:**
- `PartyWorker` - Party-Daten für vCard-Export
- `PartyHelper` - Party-Namen

**Aktueller Code-Pattern:**
```java
// vCard-Export
String partyName = PartyWorker.getPartyName(delegator, partyId);
// ... vCard generieren
```

**Neuer Code-Pattern (Option 1 - Synchron):**
```java
// REST API Call
PartyDTO party = partyServiceClient.getParty(partyId);
String partyName = party.getDisplayName();
// ... vCard generieren
```

**Neuer Code-Pattern (Option 2 - Asynchron):**
```java
// Event-Driven für Batch-Export
// 1. Request vCard Export
vCardExportService.requestExport(List<String> partyIds);

// 2. Asynchrone Verarbeitung
@KafkaListener(topics = "vcard-export-requests")
public void onExportRequest(VCardExportRequest request) {
    for (String partyId : request.getPartyIds()) {
        PartyDTO party = partyService.getParty(partyId);
        VCard vcard = generateVCard(party);
        // Speichern oder versenden
    }
}
```

**Kommunikationstyp:** 🟡 **HYBRID**

**Empfehlung:**
- **Einzelner Export:** SYNCHRON (REST) - Benutzer wartet
- **Batch-Export:** ASYNCHRON (Kafka) - Hintergrund-Job

---

#### 2.5.3 HumanResEvents
**FQN:** `org.apache.ofbiz.humanres.HumanResEvents`

**Verwendete Party-Klassen:**
- `PartyHelper` - Mitarbeiter-Informationen

**Kommunikationstyp:** 🔴 **ASYNCHRON (Kafka)**

**Begründung:**
- ✅ HR-Events sind oft Hintergrund-Prozesse
- ✅ Keine sofortige Benutzer-Interaktion
- ✅ Kann verzögert verarbeitet werden

**Event-Pattern:**
```java
// HR Event published
hrEventPublisher.publishEmployeeCreated(employeeId, partyId);

// Party Service enriched Event (optional)
@KafkaListener(topics = "hr-events")
public void onEmployeeCreated(EmployeeCreatedEvent event) {
    PartyDTO party = partyService.getParty(event.getPartyId());
    // Weitere Verarbeitung
}
```

**Kafka Topic:**
```
Topic: hr-events
Event: EmployeeCreated, EmployeeUpdated, EmployeeTerminated
Partition Key: partyId
```

---

## 3. Kommunikations-Matrix

### 3.1 Übersicht nach Modul

| Modul | Klassen | Synchron (REST) | Asynchron (Kafka) | Hybrid |
|-------|---------|-----------------|-------------------|--------|
| **Order** | 21 | 21 | 0 | 0 |
| **Shipment** | 4 | 4 | 0 | 0 |
| **Accounting** | 6 | 6 | 0 | 0 |
| **Product** | 6 | 6 | 0 | 0 |
| **Marketing** | 2 | 2 | 0 | 0 |
| **HumanRes** | 2 | 1 | 1 | 0 |
| **SecurityExt** | 1 | 1 | 0 | 0 |
| **SFA** | 1 | 0 | 0 | 1 |
| **GESAMT** | **43** | **41 (95%)** | **1 (2%)** | **1 (2%)** |

### 3.2 Empfehlungen nach Use Case

#### 🟢 SYNCHRON (REST) - 41 Klassen (95%)

**Wann verwenden:**
- ✅ Benutzer wartet auf Antwort
- ✅ Transaktionaler Kontext
- ✅ Fehler müssen sofort behandelt werden
- ✅ Daten werden für sofortige Entscheidung benötigt

**Beispiele:**
- Shopping Cart (Checkout)
- Zahlungsprozess
- Bestellerstellung
- Login
- Versandlabel-Erstellung

**Performance-Optimierungen:**
```yaml
# Redis Caching
party_cache:
  ttl: 3600  # 1 Stunde
  max_size: 10000

contact_cache:
  ttl: 86400  # 24 Stunden (Adressen ändern sich selten)
  max_size: 50000

# Circuit Breaker
circuit_breaker:
  failure_threshold: 5
  timeout: 2000ms
  reset_timeout: 30000ms
```

---

#### 🔴 ASYNCHRON (Kafka) - 1 Klasse (2%)

**Wann verwenden:**
- ✅ Hintergrund-Prozesse
- ✅ Keine sofortige Benutzer-Interaktion
- ✅ Event-Driven Architecture
- ✅ Eventual Consistency akzeptabel

**Beispiele:**
- HR-Events (Mitarbeiter-Änderungen)
- Batch-Exporte
- Reporting
- Audit-Logs

**Kafka Topics:**
```yaml
topics:
  party-events:
    partitions: 10
    replication: 3
    events:
      - PartyCreated
      - PartyUpdated
      - PartyDeleted
      - ContactMechAdded
      - ContactMechUpdated
      
  hr-events:
    partitions: 5
    replication: 3
    events:
      - EmployeeCreated
      - EmployeeUpdated
      - EmployeeTerminated
```

---

#### 🟡 HYBRID - 1 Klasse (2%)

**Wann verwenden:**
- ✅ Unterschiedliche Use Cases in derselben Klasse
- ✅ Einzelne Operationen: Synchron
- ✅ Batch-Operationen: Asynchron

**Beispiel: VCard Export**
```java
// Synchron für einzelnen Export
@GetMapping("/parties/{partyId}/vcard")
public ResponseEntity<VCard> exportVCard(@PathVariable String partyId) {
    PartyDTO party = partyServiceClient.getParty(partyId);
    VCard vcard = vCardGenerator.generate(party);
    return ResponseEntity.ok(vcard);
}

// Asynchron für Batch-Export
@PostMapping("/parties/vcard/batch")
public ResponseEntity<BatchExportResponse> exportVCardBatch(
        @RequestBody List<String> partyIds) {
    String jobId = UUID.randomUUID().toString();
    vCardExportService.exportAsync(jobId, partyIds);
    return ResponseEntity.accepted()
        .body(new BatchExportResponse(jobId, "PROCESSING"));
}
```

---

## 4. API-Design für Party Service

### 4.1 REST Endpoints

```yaml
# Party Management
GET    /api/v1/parties                          # List parties (paginated)
GET    /api/v1/parties/{partyId}                # Get party by ID
POST   /api/v1/parties                          # Create party
PUT    /api/v1/parties/{partyId}                # Update party
DELETE /api/v1/parties/{partyId}                # Delete party (soft)

# Contact Management
GET    /api/v1/parties/{partyId}/contacts                    # List contacts
GET    /api/v1/parties/{partyId}/contacts/{contactMechId}    # Get contact
POST   /api/v1/parties/{partyId}/contacts                    # Add contact
PUT    /api/v1/parties/{partyId}/contacts/{contactMechId}    # Update contact
DELETE /api/v1/parties/{partyId}/contacts/{contactMechId}    # Remove contact

# Filtered Queries
GET    /api/v1/parties/{partyId}/contacts?type=POSTAL_ADDRESS&purpose=BILLING_LOCATION

# Batch Operations
POST   /api/v1/parties/batch                    # Get multiple parties
POST   /api/v1/parties/{partyId}/contacts/batch # Get multiple contacts

# Relationships
GET    /api/v1/parties/{partyId}/relationships              # List relationships
POST   /api/v1/parties/{partyId}/relationships              # Add relationship

# Search
GET    /api/v1/parties/search?q={query}                     # Search parties
```

### 4.2 GraphQL (Optional, für komplexe Queries)

```graphql
type Party {
  partyId: ID!
  partyType: PartyType!
  firstName: String
  lastName: String
  displayName: String
  contacts(type: ContactMechType, purpose: String): [ContactMech!]!
  relationships(type: String): [PartyRelationship!]!
  roles: [PartyRole!]!
}

type ContactMech {
  contactMechId: ID!
  contactMechType: ContactMechType!
  address1: String
  city: String
  postalCode: String
  countryGeoId: String
}

type Query {
  party(partyId: ID!): Party
  parties(filter: PartyFilter, page: Int, size: Int): PartyPage!
  searchParties(query: String!): [Party!]!
}

type Mutation {
  createParty(input: CreatePartyInput!): Party!
  updateParty(partyId: ID!, input: UpdatePartyInput!): Party!
  addContactMech(partyId: ID!, input: AddContactMechInput!): ContactMech!
}
```

**Beispiel-Query:**
```graphql
query GetPartyForCheckout($partyId: ID!) {
  party(partyId: $partyId) {
    partyId
    displayName
    contacts(purpose: "BILLING_LOCATION", type: POSTAL_ADDRESS) {
      contactMechId
      address1
      city
      postalCode
      countryGeoId
    }
    contacts(purpose: "SHIPPING_LOCATION", type: POSTAL_ADDRESS) {
      contactMechId
      address1
      city
      postalCode
    }
  }
}
```

### 4.3 Kafka Events

```yaml
# Party Events
topic: party-events
events:
  PartyCreated:
    partyId: string
    partyType: string
    timestamp: datetime
    
  PartyUpdated:
    partyId: string
    changes: object
    timestamp: datetime
    
  PartyDeleted:
    partyId: string
    timestamp: datetime
    
  ContactMechAdded:
    partyId: string
    contactMechId: string
    contactMechType: string
    timestamp: datetime
    
  ContactMechUpdated:
    partyId: string
    contactMechId: string
    changes: object
    timestamp: datetime
```

## 5. Performance-Anforderungen

### 5.1 SLA nach Endpoint-Typ

| Endpoint-Typ | p50 | p95 | p99 | Timeout |
|--------------|-----|-----|-----|---------|
| **Kritisch** (Payment, Login) | < 30ms | < 50ms | < 100ms | 500ms |
| **Hoch** (Checkout, Order) | < 50ms | < 100ms | < 200ms | 1000ms |
| **Normal** (Search, List) | < 100ms | < 200ms | < 500ms | 2000ms |
| **Niedrig** (Batch, Export) | < 500ms | < 1000ms | < 2000ms | 5000ms |

### 5.2 Caching-Strategie

```yaml
# Redis Cache Configuration
caches:
  parties:
    ttl: 3600          # 1 Stunde
    max_size: 10000
    eviction: LRU
    
  contacts:
    ttl: 86400         # 24 Stunden (Adressen ändern sich selten)
    max_size: 50000
    eviction: LRU
    
  relationships:
    ttl: 7200          # 2 Stunden
    max_size: 20000
    eviction: LRU

# Cache Invalidation
invalidation:
  on_update: true
  on_delete: true
  propagation: async  # Via Kafka Event
```

### 5.3 Circuit Breaker

```yaml
circuit_breaker:
  failure_threshold: 5        # Nach 5 Fehlern öffnen
  success_threshold: 2        # Nach 2 Erfolgen schließen
  timeout: 2000ms            # Request Timeout
  reset_timeout: 30000ms     # Nach 30s wieder versuchen
  half_open_requests: 3      # 3 Requests im Half-Open State
```

## 6. Migration-Strategie

### 6.1 Phase 1: Synchrone Aufrufe (Woche 1-8)

**Priorität 1: Kritische Pfade**
1. PaymentGatewayServices (Accounting)
2. LoginEvents (SecurityExt)
3. ShoppingCart (Order)
4. CheckOutHelper (Order)
5. OrderServices (Order)

**Priorität 2: Wichtige Pfade**
6. TaxAuthorityServices (Accounting)
7. UpsServices, UspsServices, FedexServices (Shipment)
8. ShipmentServices (Shipment)

**Priorität 3: Normale Pfade**
9. ProductStoreWorker (Product)
10. ProductSearch$SupplierConstraint (Product)
11. CheckOutEvents, ShippingEvents (Order)

### 6.2 Phase 2: Asynchrone Aufrufe (Woche 9-10)

**Niedrige Priorität:**
12. HumanResEvents (HumanRes) - Asynchron
13. VCard (SFA) - Hybrid

### 6.3 Rollout-Plan

```yaml
Week 1-2:
  - ACL-Implementierung für alle Klassen
  - Feature Flags einrichten
  - Monitoring Setup
  
Week 3-4:
  - Kritische Pfade (Payment, Login, Shopping Cart)
  - 10% Traffic → 25% → 50% → 100%
  - Intensive Überwachung
  
Week 5-6:
  - Wichtige Pfade (Tax, Shipment)
  - 25% → 50% → 100%
  
Week 7-8:
  - Normale Pfade (Product, weitere Order-Klassen)
  - 50% → 100%
  
Week 9-10:
  - Asynchrone Pfade (HR, VCard)
  - Event-Bus Setup
  - Kafka Topics erstellen
  
Week 11-12:
  - Vollständiger Cutover
  - Legacy-Code deaktivieren
  - Monitoring & Optimierung
```

## 7. Zusammenfassung

### 7.1 Kernaussagen

✅ **95% Synchron (REST):** Die meisten Aufrufe benötigen sofortige Antworten

✅ **2% Asynchron (Kafka):** Nur für Hintergrund-Prozesse (HR-Events)

✅ **2% Hybrid:** VCard-Export (einzeln synchron, batch asynchron)

✅ **Klare API-Grenzen:** 11 Party-Klassen werden zu REST/GraphQL APIs

✅ **Performance kritisch:** Caching und Circuit Breaker essentiell

### 7.2 Empfehlungen

1. **Start mit synchronen Aufrufen:** Einfacher zu implementieren und zu testen
2. **Aggressives Caching:** Besonders für Adressen (ändern sich selten)
3. **GraphQL für komplexe Queries:** Reduziert Anzahl der API-Calls
4. **Circuit Breaker:** Schutz vor Ausfällen
5. **Monitoring von Anfang an:** Response Times, Error Rates, Cache Hit Ratio

### 7.3 Risiken

⚠️ **Performance-Degradation:** Netzwerk-Latenz durch API-Calls
- **Mitigation:** Redis-Caching, Batch-APIs, GraphQL

⚠️ **Kritische Pfade:** Payment und Checkout sind hochkritisch
- **Mitigation:** Niedrige Timeouts, Circuit Breaker, umfangreiche Tests

⚠️ **Komplexität:** 43 Klassen müssen angepasst werden (+22 mehr als ursprünglich erwartet)
- **Mitigation:** ACL-Pattern, schrittweiser Rollout, Feature Flags

---

**Erstellt:** 2026-01-11
**Aktualisiert:** 2026-01-17 (Nach Fehlerkorrektur in Neo4j-Daten)
**Basierend auf:** Neo4j Code-Analyse
**Status:** ✅ Aktualisiert und bereit für Implementierung

## Änderungshistorie

### 2026-01-17: Korrektur nach Neo4j-Datenbereinigung
- **Aufrufende Klassen:** 21 → 43 (+22 Klassen)
- **Order-Modul:** 7 → 21 Klassen (+14 neue Klassen aus order.entry, order.quote, order.allocationplan)
- **Accounting-Modul:** 2 → 6 Klassen (+4 Report-Klassen)
- **Product-Modul:** 2 → 6 Klassen (+4 Klassen aus facility und price)
- **Neues Marketing-Modul:** 2 Klassen (CloneLead, MergeContacts)
- **HumanRes-Modul:** 1 → 2 Klassen (+CategoryTree)
- **Synchrone Aufrufe:** 90% → 95% (41 von 43 Klassen)
- **Asynchrone Aufrufe:** 5% → 2% (1 von 43 Klassen)
- **Hybrid:** 5% → 2% (1 von 43 Klassen)
