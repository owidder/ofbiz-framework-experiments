# Party Service Extraktion - Abhängigkeitsanalyse

## 🔍 Übersicht der Party-Service Abhängigkeiten

Basierend auf der Analyse der OFBiz-Codebase gibt es **MASSIVE Abhängigkeiten** vom Party-Service in allen anderen Modulen. Dies ist ein kritischer Punkt für die Migration.

---

## 📊 Quantitative Analyse

### Party-Referenzen nach Modul

| Modul | Anzahl Referenzen | Kritikalität | Abhängigkeitstyp |
|-------|------------------|--------------|-----------------|
| **Order** | 50+ | KRITISCH | Foreign Keys, Service Calls |
| **Product** | 45+ | KRITISCH | Foreign Keys, Pricing |
| **Accounting** | 35+ | KRITISCH | Tax Authority, Payments |
| **Manufacturing** | 25+ | HOCH | Production Planning |
| **Marketing** | 40+ | HOCH | Campaigns, Opportunities |
| **Content** | 30+ | MITTEL | Approvals, Roles |
| **WorkEffort** | 35+ | HOCH | Assignments, Timesheets |
| **HumanRes** | 60+ | KRITISCH | Employees, Employment |
| **Shipment** | 30+ | HOCH | Carriers, Receivers |
| **GESAMT** | **350+** | - | - |

---

## 🔗 Detaillierte Abhängigkeitsanalyse

### 1. Order Service (50+ Referenzen)

#### Foreign Key Beziehungen
```xml
<!-- OrderHeader -->
<field name="billFromPartyId" type="id"/>
<field name="billToPartyId" type="id"/>
<field name="shipFromPartyId" type="id"/>
<field name="shipToPartyId" type="id"/>

<!-- OrderItemShipGroup -->
<field name="supplierPartyId" type="id"/>
<field name="vendorPartyId" type="id"/>
<field name="carrierPartyId" type="id"/>

<!-- OrderRole -->
<field name="partyId" type="id"/>
<field name="roleTypeId" type="id"/>
```

#### Service Calls
```
- getOrderHeader() → benötigt Party-Daten
- createOrder() → validiert Party-IDs
- updateOrderStatus() → aktualisiert Party-Rollen
- getOrderParties() → holt alle Parteien einer Bestellung
```

#### Anpassungen erforderlich
- [ ] OrderHeader: Foreign Keys zu Party Service
- [ ] OrderItemShipGroup: Supplier/Vendor/Carrier Validierung
- [ ] OrderRole: Party-Validierung
- [ ] Alle Order-Services: REST Calls zu Party Service
- [ ] Geschätzte Anzahl Anpassungen: **15-20 Dateien**

---

### 2. HumanRes Service (60+ Referenzen) - KRITISCH!

#### Entity-Beziehungen
```xml
<!-- Employment -->
<field name="partyIdFrom" type="id"/>  <!-- Employer -->
<field name="partyIdTo" type="id"/>    <!-- Employee -->

<!-- EmplPosition -->
<field name="partyId" type="id"/>      <!-- Organization -->

<!-- PartyQual, PartySkill, PartyResume -->
<field name="partyId" type="id"/>      <!-- Employee -->

<!-- PerfReview -->
<field name="employeePartyId" type="id"/>
<field name="managerPartyId" type="id"/>

<!-- EmployeeLeave -->
<field name="partyId" type="id"/>
<field name="approverPartyId" type="id"/>

<!-- JobInterview -->
<field name="jobIntervieweePartyId" type="id"/>
<field name="jobInterviewerPartyId" type="id"/>
```

#### Service Calls
```
- getEmployee() → benötigt Party-Daten
- createEmployment() → validiert Party-IDs
- getEmployeeSkills() → holt Party-Qualifikationen
- getManagerInfo() → holt Manager-Party-Daten
```

#### Anpassungen erforderlich
- [ ] Employment: Party Service Calls
- [ ] EmplPosition: Party Service Calls
- [ ] PartyQual/PartySkill/PartyResume: Party Service Calls
- [ ] PerfReview: Party Service Calls
- [ ] EmployeeLeave: Party Service Calls
- [ ] JobInterview: Party Service Calls
- [ ] Geschätzte Anzahl Anpassungen: **25-30 Dateien**

---

### 3. Product Service (45+ Referenzen)

#### Entity-Beziehungen
```xml
<!-- ProductCatalogRole -->
<field name="partyId" type="id"/>

<!-- ProductCategoryRole -->
<field name="partyId" type="id"/>

<!-- Facility -->
<field name="ownerPartyId" type="id"/>

<!-- FacilityParty -->
<field name="partyId" type="id"/>

<!-- InventoryItem -->
<field name="partyId" type="id"/>
<field name="ownerPartyId" type="id"/>

<!-- ProductPrice -->
<field name="taxAuthPartyId" type="id"/>

<!-- ProductPromoCode -->
<field name="partyId" type="id"/>

<!-- ProductStore -->
<field name="payToPartyId" type="id"/>
<field name="vatTaxAuthPartyId" type="id"/>

<!-- SupplierProduct -->
<field name="partyId" type="id"/>  <!-- Supplier -->
```

#### Service Calls
```
- getCatalogRoles() → holt Party-Rollen
- getFacilityOwner() → holt Owner-Party
- getSupplierInfo() → holt Supplier-Party
- validateTaxAuthority() → validiert Tax Authority Party
```

#### Anpassungen erforderlich
- [ ] ProductCatalogRole: Party Service Calls
- [ ] ProductCategoryRole: Party Service Calls
- [ ] Facility: Party Service Calls
- [ ] InventoryItem: Party Service Calls
- [ ] ProductPrice: Party Service Calls
- [ ] ProductStore: Party Service Calls
- [ ] SupplierProduct: Party Service Calls
- [ ] Geschätzte Anzahl Anpassungen: **18-22 Dateien**

---

### 4. Accounting Service (35+ Referenzen)

#### Entity-Beziehungen
```xml
<!-- Invoice -->
<field name="partyIdFrom" type="id"/>  <!-- Vendor -->
<field name="partyId" type="id"/>      <!-- Customer -->

<!-- Payment -->
<field name="partyIdFrom" type="id"/>
<field name="partyIdTo" type="id"/>

<!-- TaxAuthority -->
<field name="taxAuthPartyId" type="id"/>

<!-- FinAccount -->
<field name="organizationPartyId" type="id"/>
```

#### Service Calls
```
- createInvoice() → validiert Party-IDs
- processPayment() → validiert Party-IDs
- getTaxAuthority() → holt Tax Authority Party
- getFinAccountOwner() → holt Organization Party
```

#### Anpassungen erforderlich
- [ ] Invoice: Party Service Calls
- [ ] Payment: Party Service Calls
- [ ] TaxAuthority: Party Service Calls
- [ ] FinAccount: Party Service Calls
- [ ] Geschätzte Anzahl Anpassungen: **12-15 Dateien**

---

### 5. Marketing Service (40+ Referenzen)

#### Entity-Beziehungen
```xml
<!-- MarketingCampaignRole -->
<field name="partyId" type="id"/>

<!-- ContactList -->
<field name="ownerPartyId" type="id"/>

<!-- ContactListParty -->
<field name="partyId" type="id"/>

<!-- SalesOpportunity -->
<field name="partyId" type="id"/>

<!-- SalesOpportunityRole -->
<field name="partyId" type="id"/>

<!-- SegmentGroupRole -->
<field name="partyId" type="id"/>
```

#### Service Calls
```
- getCampaignRoles() → holt Party-Rollen
- getContactListOwner() → holt Owner-Party
- getOpportunityParties() → holt Opportunity-Parteien
- getSegmentMembers() → holt Party-Segmente
```

#### Anpassungen erforderlich
- [ ] MarketingCampaignRole: Party Service Calls
- [ ] ContactList: Party Service Calls
- [ ] ContactListParty: Party Service Calls
- [ ] SalesOpportunity: Party Service Calls
- [ ] SegmentGroupRole: Party Service Calls
- [ ] Geschätzte Anzahl Anpassungen: **16-20 Dateien**

---

### 6. WorkEffort Service (35+ Referenzen)

#### Entity-Beziehungen
```xml
<!-- WorkEffortPartyAssignment -->
<field name="partyId" type="id"/>

<!-- TimeEntry -->
<field name="partyId" type="id"/>

<!-- Timesheet -->
<field name="partyId" type="id"/>
<field name="clientPartyId" type="id"/>

<!-- WorkEffortEventReminder -->
<field name="partyId" type="id"/>
```

#### Service Calls
```
- getPartyAssignments() → holt Party-Zuweisungen
- getTimeEntries() → holt Party-Zeiteinträge
- getTimesheetOwner() → holt Timesheet-Owner
```

#### Anpassungen erforderlich
- [ ] WorkEffortPartyAssignment: Party Service Calls
- [ ] TimeEntry: Party Service Calls
- [ ] Timesheet: Party Service Calls
- [ ] Geschätzte Anzahl Anpassungen: **12-15 Dateien**

---

### 7. Content Service (30+ Referenzen)

#### Entity-Beziehungen
```xml
<!-- ContentApproval -->
<field name="partyId" type="id"/>

<!-- ContentRevision -->
<field name="committedByPartyId" type="id"/>

<!-- ContentRole -->
<field name="partyId" type="id"/>

<!-- DataResourceRole -->
<field name="partyId" type="id"/>

<!-- WebPreference -->
<field name="partyId" type="id"/>

<!-- SurveyResponse -->
<field name="partyId" type="id"/>

<!-- WebSiteRole -->
<field name="partyId" type="id"/>
```

#### Service Calls
```
- getContentApprovers() → holt Approver-Parteien
- getContentRoles() → holt Party-Rollen
- getWebSiteRoles() → holt Website-Rollen
```

#### Anpassungen erforderlich
- [ ] ContentApproval: Party Service Calls
- [ ] ContentRole: Party Service Calls
- [ ] DataResourceRole: Party Service Calls
- [ ] WebPreference: Party Service Calls
- [ ] SurveyResponse: Party Service Calls
- [ ] WebSiteRole: Party Service Calls
- [ ] Geschätzte Anzahl Anpassungen: **14-18 Dateien**

---

### 8. Manufacturing Service (25+ Referenzen)

#### Entity-Beziehungen
```xml
<!-- ProductionRun -->
<!-- Indirekt über Product Service -->

<!-- BillOfMaterials -->
<!-- Indirekt über Product Service -->
```

#### Service Calls
```
- getProductionPlanning() → benötigt Party-Daten für Supplier
- getRoutingInfo() → benötigt Party-Daten für Operator
```

#### Anpassungen erforderlich
- [ ] Indirekte Abhängigkeiten über Product Service
- [ ] Geschätzte Anzahl Anpassungen: **8-10 Dateien**

---

### 9. Shipment Service (30+ Referenzen)

#### Entity-Beziehungen
```xml
<!-- Shipment -->
<field name="partyIdTo" type="id"/>    <!-- Receiver -->
<field name="partyIdFrom" type="id"/>  <!-- Sender -->

<!-- ShipmentRouteSegment -->
<field name="carrierPartyId" type="id"/>

<!-- CarrierShipmentMethod -->
<field name="partyId" type="id"/>      <!-- Carrier -->

<!-- PicklistRole -->
<field name="partyId" type="id"/>

<!-- ReceiptRole -->
<field name="partyId" type="id"/>
```

#### Service Calls
```
- getShipmentParties() → holt Sender/Receiver
- getCarrierInfo() → holt Carrier-Party
- getPicklistAssignees() → holt Party-Zuweisungen
```

#### Anpassungen erforderlich
- [ ] Shipment: Party Service Calls
- [ ] ShipmentRouteSegment: Party Service Calls
- [ ] CarrierShipmentMethod: Party Service Calls
- [ ] PicklistRole: Party Service Calls
- [ ] ReceiptRole: Party Service Calls
- [ ] Geschätzte Anzahl Anpassungen: **12-15 Dateien**

---

## 🎯 Migrations-Strategie für Party Service

### Option 1: Strangler Fig Pattern (EMPFOHLEN)

```
Phase 1: Adapter Layer erstellen
├─ PartyServiceAdapter in Monolith
├─ Delegiert zu Party Service REST API
├─ Fallback zu lokaler DB bei Fehler
└─ Keine Änderungen an bestehenden Code

Phase 2: Schrittweise Migration
├─ Order Service → Party Service Adapter
├─ Product Service → Party Service Adapter
├─ Accounting Service → Party Service Adapter
├─ Etc.
└─ Jeder Service einzeln migrieren

Phase 3: Monolith abschalten
├─ Alle Services verwenden Party Service
├─ Monolith nur noch für Legacy
└─ Schrittweise Abschaltung
```

### Option 2: Big Bang Migration (NICHT EMPFOHLEN)

```
Probleme:
├─ Zu viele Abhängigkeiten
├─ Hohes Risiko
├─ Lange Testphase
├─ Viele Rollbacks wahrscheinlich
└─ Nicht praktikabel
```

---

## 📋 Detaillierte Anpassungsliste

### Zu ändernde Dateitypen

```
1. Entity Model Files (.xml)
   ├─ Foreign Key Definitionen
   ├─ Relation Definitionen
   └─ View Entity Definitionen
   Geschätzt: 50-60 Dateien

2. Service Definition Files (.xml)
   ├─ Service Input/Output Parameter
   ├─ Service Calls zu Party Service
   └─ Event Handler
   Geschätzt: 40-50 Dateien

3. Java Service Implementation Files (.java)
   ├─ Service Methoden
   ├─ REST Calls zu Party Service
   ├─ Error Handling
   └─ Caching
   Geschätzt: 80-100 Dateien

4. Controller Files (.xml)
   ├─ Request Handler
   ├─ Event Handler
   └─ View Handler
   Geschätzt: 20-30 Dateien

5. Test Files (.java)
   ├─ Unit Tests
   ├─ Integration Tests
   └─ Mock Party Service
   Geschätzt: 30-40 Dateien

GESAMT: 220-280 Dateien müssen angepasst werden
```

---

## ⏱️ Geschätzter Aufwand für Party Service Extraktion

### Infrastruktur & Setup
- Party Service Projekt erstellen: 1 Woche
- REST API definieren: 1 Woche
- Datenbank-Migration: 1 Woche
- **Subtotal: 3 Wochen**

### Adapter Layer
- PartyServiceAdapter implementieren: 1 Woche
- Fallback-Logik: 1 Woche
- Caching-Strategie: 1 Woche
- **Subtotal: 3 Wochen**

### Service-by-Service Migration
- Order Service: 2 Wochen
- HumanRes Service: 2 Wochen
- Product Service: 2 Wochen
- Accounting Service: 1.5 Wochen
- Marketing Service: 1.5 Wochen
- WorkEffort Service: 1.5 Wochen
- Content Service: 1 Woche
- Manufacturing Service: 1 Woche
- Shipment Service: 1 Woche
- **Subtotal: 14 Wochen**

### Testing & QA
- Integration Tests: 2 Wochen
- Performance Tests: 1 Woche
- Security Tests: 1 Woche
- **Subtotal: 4 Wochen**

### **GESAMTAUFWAND: 24 Wochen (6 Monate)**

---

## 🚨 Kritische Erfolgsfaktoren

### 1. Adapter Layer Pattern
```java
// PartyServiceAdapter.java
@Component
public class PartyServiceAdapter {
    
    private final PartyServiceClient partyServiceClient;
    private final PartyRepository partyRepository;
    private final CacheManager cacheManager;
    
    public Party getParty(String partyId) {
        try {
            // Versuche Party Service zu erreichen
            return partyServiceClient.getParty(partyId);
        } catch (Exception e) {
            // Fallback zu lokaler DB
            log.warn("Party Service unavailable, using local DB", e);
            return partyRepository.findById(partyId).orElse(null);
        }
    }
}
```

### 2. Caching-Strategie
```
Party-Daten cachen:
├─ TTL: 5-10 Minuten
├─ Cache-Invalidation bei Updates
├─ Fallback zu DB bei Cache-Miss
└─ Monitoring von Cache-Hit-Rate
```

### 3. Error Handling
```
Bei Party Service Fehler:
├─ Retry mit Exponential Backoff
├─ Circuit Breaker aktivieren
├─ Fallback zu lokaler DB
├─ Alert an Operations Team
└─ Graceful Degradation
```

### 4. Monitoring
```
Metriken zu tracken:
├─ Party Service Response Time
├─ Party Service Error Rate
├─ Adapter Fallback Rate
├─ Cache Hit Rate
└─ Database Query Performance
```

---

## 📊 Abhängigkeitsgraph

```
Party Service (Zentral)
    ↑
    ├─ Order Service (50+ Refs)
    ├─ HumanRes Service (60+ Refs)
    ├─ Product Service (45+ Refs)
    ├─ Accounting Service (35+ Refs)
    ├─ Marketing Service (40+ Refs)
    ├─ WorkEffort Service (35+ Refs)
    ├─ Content Service (30+ Refs)
    ├─ Manufacturing Service (25+ Refs)
    └─ Shipment Service (30+ Refs)

TOTAL: 350+ Abhängigkeiten
```

---

## ✅ Empfehlung

**Party Service ist NICHT der beste Startpunkt für die Microservices-Migration!**

### Bessere Reihenfolge:

1. **Content Service** (30 Refs, niedrig gekoppelt)
   - Wenige Abhängigkeiten
   - Einfach zu extrahieren
   - Gutes Lernprojekt

2. **Product Service** (45 Refs, aber relativ unabhängig)
   - Kann mit Party Service Adapter arbeiten
   - Gute Größe für Microservice

3. **Manufacturing Service** (25 Refs, indirekt)
   - Indirekte Party-Abhängigkeiten
   - Relativ unabhängig

4. **Order Service** (50 Refs, aber zentral)
   - Viele Abhängigkeiten
   - Aber kritisch für Business
   - Nach Content/Product/Manufacturing

5. **Party Service** (SPÄTER!)
   - Nach anderen Services
   - Mit etabliertem Adapter Pattern
   - Mit Erfahrung aus anderen Services

---

## 🎯 Alternative: Party Service als Shared Library

Statt Party Service zu extrahieren, könnte man:

```
Option A: Shared Library
├─ Party-Entities in gemeinsamer Library
├─ Party-Services in gemeinsamer Library
├─ Alle Services nutzen diese Library
└─ Später zu Microservice migrieren

Vorteil:
├─ Keine Abhängigkeitsänderungen
├─ Schnellere Migration anderer Services
├─ Weniger Komplexität anfangs
└─ Später zu echtem Service migrieren

Nachteil:
├─ Nicht echte Microservices
├─ Shared Library ist Bottleneck
└─ Später trotzdem große Änderungen
```

---

## 📝 Fazit

**Party Service Extraktion ist komplex und sollte NICHT der erste Service sein!**

### Empfohlene Reihenfolge:

1. **Content Service** (Einfach, unabhängig)
2. **Product Service** (Mittelschwer, relativ unabhängig)
3. **Manufacturing Service** (Mittelschwer, indirekt abhängig)
4. **Order Service** (Komplex, zentral)
5. **Accounting Service** (Komplex, zentral)
6. **Marketing Service** (Mittelschwer)
7. **WorkEffort Service** (Mittelschwer)
8. **HumanRes Service** (Komplex, viele Abhängigkeiten)
9. **Party Service** (ZULETZT! Mit Adapter Pattern)

### Gesamtaufwand für Party Service Extraktion: **6 Monate**

Dies ist deutlich länger als für andere Services, weil:
- 350+ Abhängigkeiten in der Codebase
- 220-280 Dateien müssen angepasst werden
- Adapter Layer Pattern erforderlich
- Umfangreiche Testing erforderlich
- Hohes Risiko bei Fehlern
