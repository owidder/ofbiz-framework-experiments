# Party Service Extraktion - Impact Analysis

## Executive Summary

Diese Analyse zeigt detailliert, wie viele Klassen im OFBiz-Code angepasst werden müssen, wenn der Party Service als eigenständiger Microservice extrahiert wird.

### Gesamtübersicht

| Metrik | Anzahl |
|--------|--------|
| **Betroffene Module** | 7 |
| **Betroffene Klassen gesamt** | 64 |
| **Verwendete Party-Klassen** | 5 |
| **Abhängigkeiten gesamt** | 180 |

### Kritische Erkenntnis

Die Extraktion des Party Service ist **überschaubar**, da:
- ✅ Nur 5 zentrale Party-Klassen werden extern verwendet
- ✅ Die meisten Abhängigkeiten sind auf Helper/Worker-Klassen konzentriert
- ✅ Keine tiefen Verschachtelungen in die Party-Domäne
- ✅ Klare API-Grenzen erkennbar

## 1. Betroffene Module im Detail

### 1.1 Order Module (Höchste Priorität)

**Impact:** 🔴 Hoch

| Metrik | Wert |
|--------|------|
| Betroffene Klassen | 20 |
| Abhängigkeiten | 36 |
| Verwendete Party-Klassen | 3 |

**Betroffene Klassen:**

1. **CheckOutEvents** (`org.apache.ofbiz.order.shoppingcart.CheckOutEvents`)
   - Verwendet: `PartyWorker`
   - Zweck: Party-Informationen beim Checkout abrufen
   - Anpassung: API-Call zu Party Service

2. **CheckOutHelper** (`org.apache.ofbiz.order.shoppingcart.CheckOutHelper`)
   - Verwendet: `ContactMechWorker`, `ContactHelper`
   - Zweck: Kontaktdaten und Adressen beim Checkout
   - Anpassung: REST API für Kontaktdaten

3. **OrderServices** (`org.apache.ofbiz.order.order.OrderServices`)
   - Verwendet: `ContactHelper`, `PartyWorker`, `ContactMechWorker`
   - Zweck: Party-Daten bei Bestellerstellung
   - Anpassung: Mehrere API-Calls zu Party Service

4. **ShippingEvents** (`org.apache.ofbiz.order.shoppingcart.shipping.ShippingEvents`)
   - Verwendet: `ContactMechWorker`
   - Zweck: Lieferadressen abrufen
   - Anpassung: API-Call für Adressen

5. **ShoppingCart** (`org.apache.ofbiz.order.shoppingcart.ShoppingCart`)
   - Verwendet: `ContactMechWorker`, `ContactHelper`
   - Zweck: Kundendaten im Warenkorb
   - Anpassung: Caching + API-Calls

**Weitere betroffene Klassen:** 15 weitere Klassen mit ähnlichen Mustern

**Refactoring-Aufwand:**
- Geschätzte Personentage: **8-12 Tage**
- Komplexität: Mittel bis Hoch
- Risiko: Mittel (Shopping Cart ist kritisch)

### 1.2 Shipment Module

**Impact:** 🟡 Mittel

| Metrik | Wert |
|--------|------|
| Betroffene Klassen | 16 |
| Abhängigkeiten | 16 |
| Verwendete Party-Klassen | 3 |

**Betroffene Klassen:**

1. **UpsServices** (`org.apache.ofbiz.shipment.thirdparty.ups.UpsServices`)
   - Verwendet: `ContactMechWorker`
   - Zweck: Lieferadressen für UPS-Integration
   - Anpassung: API-Call für Adressen

2. **UspsServices** (`org.apache.ofbiz.shipment.thirdparty.usps.UspsServices`)
   - Verwendet: `ContactMechWorker`
   - Zweck: Lieferadressen für USPS-Integration
   - Anpassung: API-Call für Adressen

3. **FedexServices** (`org.apache.ofbiz.shipment.thirdparty.fedex.FedexServices`)
   - Verwendet: `PartyHelper`
   - Zweck: Party-Informationen für FedEx
   - Anpassung: API-Call zu Party Service

4. **ShipmentServices** (`org.apache.ofbiz.shipment.shipment.ShipmentServices`)
   - Verwendet: `PartyWorker`
   - Zweck: Party-Daten bei Versanderstellung
   - Anpassung: API-Call zu Party Service

**Weitere betroffene Klassen:** 12 weitere Klassen

**Refactoring-Aufwand:**
- Geschätzte Personentage: **5-7 Tage**
- Komplexität: Mittel
- Risiko: Niedrig (Carrier-Integrationen sind isoliert)

### 1.3 Accounting Module

**Impact:** 🟡 Mittel

| Metrik | Wert |
|--------|------|
| Betroffene Klassen | 8 |
| Abhängigkeiten | 8 |
| Verwendete Party-Klassen | 2 |

**Betroffene Klassen:**

1. **PaymentGatewayServices** (`org.apache.ofbiz.accounting.payment.PaymentGatewayServices`)
   - Verwendet: `ContactHelper`
   - Zweck: Rechnungsadressen für Zahlungen
   - Anpassung: API-Call für Kontaktdaten

2. **TaxAuthorityServices** (`org.apache.ofbiz.accounting.tax.TaxAuthorityServices`)
   - Verwendet: `ContactMechWorker`
   - Zweck: Adressen für Steuerberechnung
   - Anpassung: API-Call für Adressen

**Weitere betroffene Klassen:** 6 weitere Klassen

**Refactoring-Aufwand:**
- Geschätzte Personentage: **3-5 Tage**
- Komplexität: Mittel
- Risiko: Mittel (Zahlungen sind kritisch)

### 1.4 Product Module

**Impact:** 🟢 Niedrig

| Metrik | Wert |
|--------|------|
| Betroffene Klassen | 8 |
| Abhängigkeiten | 8 |
| Verwendete Party-Klassen | 2 |

**Betroffene Klassen:**

1. **ProductSearch$SupplierConstraint** (`org.apache.ofbiz.product.product.ProductSearch$SupplierConstraint`)
   - Verwendet: `PartyHelper`
   - Zweck: Lieferanten-Suche
   - Anpassung: API-Call zu Party Service

2. **ProductStoreWorker** (`org.apache.ofbiz.product.store.ProductStoreWorker`)
   - Verwendet: `ContactMechWorker`
   - Zweck: Store-Kontaktdaten
   - Anpassung: API-Call für Kontaktdaten

**Weitere betroffene Klassen:** 6 weitere Klassen

**Refactoring-Aufwand:**
- Geschätzte Personentage: **2-3 Tage**
- Komplexität: Niedrig
- Risiko: Niedrig

### 1.5 Weitere Module (Niedrige Priorität)

**SFA Module:**
- Betroffene Klassen: 4
- Abhängigkeiten: 8
- Aufwand: 1-2 Tage

**HumanRes Module:**
- Betroffene Klassen: 4
- Abhängigkeiten: 4
- Aufwand: 1-2 Tage

**SecurityExt Module:**
- Betroffene Klassen: 4
- Abhängigkeiten: 4
- Aufwand: 1 Tag

## 2. Verwendete Party-Klassen

### 2.1 ContactMechWorker (Am häufigsten verwendet)

**Verwendung:** 9 externe Klassen

**Funktionalität:**
```java
// Typische Verwendung in OFBiz
ContactMechWorker.getPostalAddress(delegator, partyId, contactMechId);
ContactMechWorker.getTelecomNumber(delegator, partyId, contactMechId);
```

**Neue API:**
```java
// REST API Call
GET /api/v1/parties/{partyId}/contacts/{contactMechId}

// Response
{
  "contactMechId": "10001",
  "contactMechType": "POSTAL_ADDRESS",
  "address1": "123 Main St",
  "city": "Springfield",
  "postalCode": "62701"
}
```

**Refactoring-Pattern:**
```java
// Alt (OFBiz)
GenericValue postalAddress = ContactMechWorker.getPostalAddress(
    delegator, partyId, contactMechId);
String address1 = postalAddress.getString("address1");

// Neu (Microservice)
ContactMechDTO contact = partyServiceClient.getContactMech(
    partyId, contactMechId);
String address1 = contact.getAddress1();
```

### 2.2 PartyWorker

**Verwendung:** 5 externe Klassen

**Funktionalität:**
```java
// Typische Verwendung
PartyWorker.getPartyName(delegator, partyId);
PartyWorker.findPartyLatestContactMech(delegator, partyId, "POSTAL_ADDRESS");
```

**Neue API:**
```java
// REST API Call
GET /api/v1/parties/{partyId}
GET /api/v1/parties/{partyId}/contacts?type=POSTAL_ADDRESS&latest=true

// Response
{
  "partyId": "10000",
  "partyType": "PERSON",
  "firstName": "John",
  "lastName": "Doe",
  "displayName": "John Doe"
}
```

### 2.3 ContactHelper

**Verwendung:** 5 externe Klassen

**Funktionalität:**
```java
// Typische Verwendung
ContactHelper.getContactMech(party, contactMechPurposeTypeId, 
                             contactMechTypeId, false);
```

**Neue API:**
```java
GET /api/v1/parties/{partyId}/contacts?purpose={purposeTypeId}&type={typeId}
```

### 2.4 PartyHelper

**Verwendung:** 4 externe Klassen

**Funktionalität:**
```java
// Typische Verwendung
PartyHelper.getPartyName(delegator, partyId, lastNameFirst);
```

**Neue API:**
```java
GET /api/v1/parties/{partyId}?format=lastNameFirst
```

### 2.5 PartyRelationshipHelper

**Verwendung:** 1 externe Klasse

**Funktionalität:**
```java
// Typische Verwendung
PartyRelationshipHelper.getRelatedParties(delegator, partyId, 
                                          relationshipTypeId);
```

**Neue API:**
```java
GET /api/v1/parties/{partyId}/relationships?type={relationshipTypeId}
```

## 3. Refactoring-Strategie

### 3.1 Anti-Corruption Layer (ACL)

Implementierung eines Adapters, der die alte API emuliert:

```java
@Component
public class PartyServiceAdapter {
    private final PartyServiceClient partyServiceClient;
    private final Delegator delegator; // Fallback
    
    @Value("${feature.party-service.enabled:false}")
    private boolean useNewService;
    
    public GenericValue getPostalAddress(String partyId, String contactMechId) {
        if (useNewService) {
            // Neuer Service
            ContactMechDTO contact = partyServiceClient.getContactMech(
                partyId, contactMechId);
            return convertToGenericValue(contact);
        } else {
            // Fallback zu OFBiz
            return ContactMechWorker.getPostalAddress(
                delegator, partyId, contactMechId);
        }
    }
    
    private GenericValue convertToGenericValue(ContactMechDTO dto) {
        // Konvertierung von DTO zu GenericValue
        GenericValue gv = delegator.makeValue("PostalAddress");
        gv.set("contactMechId", dto.getContactMechId());
        gv.set("address1", dto.getAddress1());
        // ... weitere Felder
        return gv;
    }
}
```

### 3.2 Schrittweise Migration pro Modul

**Phase 1: Order Module (Woche 1-3)**
1. ACL für Order-Module implementieren
2. Feature Flag aktivieren
3. 10% Traffic auf neuen Service
4. Monitoring & Validierung
5. Schrittweise auf 100% erhöhen

**Phase 2: Shipment Module (Woche 4-5)**
1. ACL für Shipment-Module implementieren
2. Feature Flag aktivieren
3. Migration wie Phase 1

**Phase 3: Accounting Module (Woche 6-7)**
1. ACL für Accounting-Module implementieren
2. Feature Flag aktivieren
3. Migration wie Phase 1

**Phase 4: Weitere Module (Woche 8-9)**
1. Product, SFA, HumanRes, SecurityExt
2. Niedrige Priorität, geringes Risiko

### 3.3 Caching-Strategie

Da Party-Daten relativ statisch sind, Caching implementieren:

```java
@Service
public class CachedPartyServiceClient {
    private final PartyServiceClient client;
    private final CacheManager cacheManager;
    
    @Cacheable(value = "parties", key = "#partyId")
    public PartyDTO getParty(String partyId) {
        return client.getParty(partyId);
    }
    
    @Cacheable(value = "contacts", key = "#partyId + '_' + #contactMechId")
    public ContactMechDTO getContactMech(String partyId, String contactMechId) {
        return client.getContactMech(partyId, contactMechId);
    }
    
    @CacheEvict(value = "parties", key = "#partyId")
    public void evictPartyCache(String partyId) {
        // Cache invalidieren bei Updates
    }
}
```

**Cache-Konfiguration:**
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1 Stunde
      cache-null-values: false
```

## 4. Aufwandsschätzung

### 4.1 Entwicklungsaufwand

| Aktivität | Personentage | Beschreibung |
|-----------|--------------|--------------|
| **ACL-Implementierung** | 5-7 | Adapter für alle 5 Party-Klassen |
| **Order Module** | 8-12 | 20 Klassen anpassen |
| **Shipment Module** | 5-7 | 16 Klassen anpassen |
| **Accounting Module** | 3-5 | 8 Klassen anpassen |
| **Product Module** | 2-3 | 8 Klassen anpassen |
| **Weitere Module** | 3-4 | 12 Klassen anpassen |
| **Testing** | 10-15 | Unit, Integration, E2E Tests |
| **Monitoring Setup** | 2-3 | Metriken, Dashboards, Alerts |
| **Dokumentation** | 2-3 | API-Docs, Migration Guide |
| **Puffer** | 5-10 | Unvorhergesehenes |
| **GESAMT** | **45-69 Tage** | ~2-3 Monate mit 1 Entwickler |

### 4.2 Zeitplan mit 2 Entwicklern

| Phase | Dauer | Aktivitäten |
|-------|-------|-------------|
| **Woche 1-2** | 2 Wochen | ACL-Implementierung + Order Module Start |
| **Woche 3-4** | 2 Wochen | Order Module fertig + Shipment Module |
| **Woche 5-6** | 2 Wochen | Accounting + Product Module |
| **Woche 7-8** | 2 Wochen | Weitere Module + Testing |
| **Woche 9-10** | 2 Wochen | E2E Testing + Bugfixes |
| **Woche 11-12** | 2 Wochen | Produktionsrollout + Monitoring |
| **GESAMT** | **12 Wochen** | ~3 Monate |

## 5. Risiken und Mitigationen

### 5.1 Performance-Risiken

**Risiko:** Netzwerk-Latenz durch API-Calls

**Mitigation:**
- ✅ Redis-Caching für häufig abgefragte Daten
- ✅ Batch-APIs für mehrere Parties auf einmal
- ✅ GraphQL für flexible Datenabfragen
- ✅ HTTP/2 für Connection Pooling

**Beispiel Batch-API:**
```java
POST /api/v1/parties/batch
{
  "partyIds": ["10000", "10001", "10002"]
}

// Response
{
  "parties": [
    { "partyId": "10000", ... },
    { "partyId": "10001", ... },
    { "partyId": "10002", ... }
  ]
}
```

### 5.2 Daten-Konsistenz

**Risiko:** Inkonsistente Daten während Dual-Write-Phase

**Mitigation:**
- ✅ Transaktionale Outbox Pattern
- ✅ Reconciliation Jobs (täglich)
- ✅ Monitoring von Sync-Fehlern
- ✅ Rollback-Strategie

### 5.3 Komplexität im Order Module

**Risiko:** Shopping Cart ist kritisch und komplex

**Mitigation:**
- ✅ Umfangreiche Tests vor Rollout
- ✅ Canary Deployment (1% → 10% → 50% → 100%)
- ✅ Feature Flag für schnellen Rollback
- ✅ Shadow Mode (parallel laufen lassen)

## 6. Testing-Strategie

### 6.1 Unit Tests

**Ziel:** Jede angepasste Klasse testen

```java
@Test
void shouldGetPartyViaAdapter() {
    // Given
    String partyId = "10000";
    when(partyServiceClient.getParty(partyId))
        .thenReturn(new PartyDTO("10000", "John", "Doe"));
    
    // When
    GenericValue party = partyAdapter.getParty(partyId);
    
    // Then
    assertThat(party.getString("firstName")).isEqualTo("John");
}
```

**Umfang:** ~200-300 Unit Tests

### 6.2 Integration Tests

**Ziel:** API-Calls zum Party Service testen

```java
@SpringBootTest
@Testcontainers
class PartyServiceIntegrationTest {
    @Container
    static WireMockContainer wireMock = new WireMockContainer();
    
    @Test
    void shouldCallPartyServiceAPI() {
        // Mock Party Service Response
        wireMock.stubFor(get("/api/v1/parties/10000")
            .willReturn(okJson("{\"partyId\":\"10000\"}")));
        
        // Test Order Service calling Party Service
        orderService.createOrder(orderData);
        
        // Verify API was called
        wireMock.verify(getRequestedFor(urlEqualTo("/api/v1/parties/10000")));
    }
}
```

**Umfang:** ~50-80 Integration Tests

### 6.3 E2E Tests

**Ziel:** Komplette Workflows testen

```java
@Test
void completeOrderWorkflow() {
    // 1. Create Party
    PartyDTO party = createTestParty();
    
    // 2. Add to Cart
    ShoppingCart cart = new ShoppingCart();
    cart.setParty(party.getPartyId());
    
    // 3. Checkout
    Order order = checkoutService.checkout(cart);
    
    // 4. Verify
    assertThat(order.getPartyId()).isEqualTo(party.getPartyId());
}
```

**Umfang:** ~20-30 E2E Tests

## 7. Monitoring & Metriken

### 7.1 Wichtige Metriken

```yaml
# Prometheus Metrics
party_service_api_calls_total{module="order", endpoint="/parties/{id}"}
party_service_api_latency_seconds{module="order", endpoint="/parties/{id}"}
party_service_api_errors_total{module="order", endpoint="/parties/{id}"}
party_service_cache_hit_ratio{cache="parties"}
party_service_sync_failures_total
```

### 7.2 Alerts

```yaml
# Alert Rules
- alert: PartyServiceHighLatency
  expr: party_service_api_latency_seconds > 0.5
  for: 5m
  annotations:
    summary: "Party Service API calls are slow"

- alert: PartyServiceHighErrorRate
  expr: rate(party_service_api_errors_total[5m]) > 0.01
  for: 5m
  annotations:
    summary: "Party Service API error rate is high"
```

### 7.3 Dashboards

**Grafana Dashboard:**
- API Call Volume pro Modul
- Latency Percentiles (p50, p95, p99)
- Error Rate
- Cache Hit Ratio
- Sync Status

## 8. Rollback-Plan

Falls kritische Probleme auftreten:

### 8.1 Sofortmaßnahmen (< 5 Minuten)

```bash
# Feature Flag deaktivieren
kubectl set env deployment/order-service FEATURE_PARTY_SERVICE_ENABLED=false

# Oder via Config Service
curl -X POST http://config-service/refresh \
  -d '{"feature.party-service.enabled": false}'
```

### 8.2 Mittelfristig (< 1 Stunde)

1. Traffic komplett zurück zu OFBiz
2. Party Service offline nehmen
3. Daten-Sync rückwärts (falls nötig)
4. Incident Post-Mortem

### 8.3 Langfristig

1. Root Cause Analysis
2. Fixes implementieren
3. Erneuter Rollout mit mehr Tests

## 9. Zusammenfassung

### 9.1 Kernaussagen

✅ **Überschaubarer Aufwand:** 64 Klassen in 7 Modulen betroffen

✅ **Klare API-Grenzen:** Nur 5 Party-Klassen werden extern verwendet

✅ **Geringes Risiko:** Mit ACL-Pattern und Feature Flags gut steuerbar

✅ **Realistische Timeline:** 3 Monate mit 2 Entwicklern

⚠️ **Kritische Bereiche:** Order Module (Shopping Cart) erfordert besondere Aufmerksamkeit

### 9.2 Empfehlungen

1. **Start mit Order Module:** Höchste Priorität, aber auch höchstes Risiko
2. **Umfangreiches Testing:** Besonders für Shopping Cart
3. **Caching von Anfang an:** Performance-Probleme vermeiden
4. **Feature Flags:** Für schnellen Rollback
5. **Monitoring:** Von Tag 1 an aktiv

### 9.3 Nächste Schritte

1. [ ] Stakeholder-Review dieser Analyse
2. [ ] Team-Kapazität prüfen (2 Entwickler für 3 Monate)
3. [ ] ACL-Implementierung starten
4. [ ] Test-Strategie detaillieren
5. [ ] Monitoring-Setup vorbereiten

---

**Erstellt:** 2026-01-10  
**Basierend auf:** Neo4j Code-Analyse (343.608 Java-Elemente)  
**Status:** ✅ Bereit für Implementierung
