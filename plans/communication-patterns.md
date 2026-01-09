# Microservices-Kommunikationsmuster und Szenarien

## 1. Szenario: Bestellprozess (End-to-End)

```
Kunde platziert Bestellung im Frontend
         ↓
    [API Gateway]
         ↓
    [Order Service]
         ├─→ REST: Party Service → Kundeninfo abrufen
         ├─→ REST: Product Service → Verfügbarkeit prüfen
         ├─→ Bestellung erstellen
         └─→ Kafka: order.created Event publizieren
              ├─→ [Accounting Service] hört zu
              │   └─→ Rechnung generieren
              │   └─→ Kafka: invoice.created
              │
              ├─→ [Manufacturing Service] hört zu
              │   └─→ Produktion planen (falls nötig)
              │   └─→ Kafka: production-run.started
              │
              ├─→ [Marketing Service] hört zu
              │   └─→ Kundenhistorie aktualisieren
              │
              └─→ [Product Service] hört zu
                  └─→ Bestand reservieren
                  └─→ Kafka: inventory.reserved
```

### Zeitliche Abfolge:
1. **T+0ms:** Order Service empfängt Bestellung
2. **T+50ms:** Party & Product Service abgefragt (synchron)
3. **T+100ms:** Bestellung in DB gespeichert
4. **T+110ms:** order.created Event in Kafka publiziert
5. **T+150ms:** Accounting Service verarbeitet Event
6. **T+200ms:** Manufacturing Service verarbeitet Event
7. **T+250ms:** Marketing Service verarbeitet Event

---

## 2. Szenario: Zahlungsabwicklung

```
Order Service
    ↓
REST: Accounting Service → createInvoice()
    ↓
Accounting Service
    ├─→ Rechnung erstellen
    ├─→ REST: Payment Gateway (PayPal/Stripe)
    ├─→ Zahlungsanforderung senden
    └─→ Warten auf Callback
         ↓
    [Webhook/Callback]
         ↓
    Accounting Service
    ├─→ Zahlungsstatus aktualisieren
    ├─→ Kafka: payment.processed Event
    │   └─→ Order Service: Bestellung bestätigen
    │   └─→ Product Service: Bestand reduzieren
    │   └─→ Manufacturing Service: Produktion starten
    └─→ Kafka: payment.failed Event (bei Fehler)
        └─→ Order Service: Bestellung stornieren
```

---

## 3. Szenario: Bestandsverwaltung

```
Warehouse Worker aktualisiert Bestand
         ↓
    [Facility App]
         ↓
    [Product Service]
    ├─→ Bestand in DB aktualisieren
    ├─→ Kafka: inventory.updated Event
    │   ├─→ Order Service
    │   │   └─→ Verfügbare Bestellungen freigeben
    │   │   └─→ Kafka: order.ready-to-ship
    │   │
    │   ├─→ Manufacturing Service
    │   │   └─→ Rohstoffbestand prüfen
    │   │   └─→ Nachbestellungen auslösen
    │   │
    │   └─→ Marketing Service
    │       └─→ Verfügbarkeitsstatus aktualisieren
    │
    └─→ Alert: Bestand unter Minimum
        └─→ Kafka: stock.low Event
            └─→ Procurement System: Nachbestellung
```

---

## 4. Szenario: Fehlerbehandlung - Zahlungsausfall

```
Accounting Service
    ├─→ REST: Payment Gateway
    ├─→ Zahlungsanforderung fehlgeschlagen
    ├─→ Kafka: payment.failed Event
    │
    Order Service empfängt payment.failed
    ├─→ Bestellstatus: PAYMENT_FAILED
    ├─→ Kafka: order.payment-failed Event
    │
    Product Service empfängt order.payment-failed
    ├─→ Reservierte Bestandsmenge freigeben
    ├─→ Kafka: inventory.released Event
    │
    Manufacturing Service empfängt order.payment-failed
    ├─→ Geplante Produktion stornieren
    ├─→ Kafka: production-run.cancelled Event
    │
    Notification Service (optional)
    └─→ Kunde benachrichtigen: Zahlungsversuch fehlgeschlagen
```

---

## 5. Szenario: Verteilte Transaktion - Saga Pattern

### Bestellbestätigung mit Saga:

```
Order Service (Saga Orchestrator)
    ├─→ Step 1: Order erstellen
    │   └─→ Status: PENDING
    │
    ├─→ Step 2: REST → Product Service
    │   └─→ Bestand reservieren
    │   └─→ Erfolg? → Weiter zu Step 3
    │   └─→ Fehler? → Kompensation: Bestellung stornieren
    │
    ├─→ Step 3: REST → Accounting Service
    │   └─→ Rechnung erstellen
    │   └─→ Erfolg? → Weiter zu Step 4
    │   └─→ Fehler? → Kompensation: Bestandsreservierung aufheben
    │
    ├─→ Step 4: REST → Manufacturing Service
    │   └─→ Produktion planen
    │   └─→ Erfolg? → Bestellung bestätigen
    │   └─→ Fehler? → Kompensation: Rechnung stornieren
    │
    └─→ Finale Status: CONFIRMED oder CANCELLED
```

---

## 6. Datenfluss: Bestandsabstimmung

```
Täglich um 02:00 Uhr:

Inventory Reconciliation Job (Product Service)
    ├─→ Physischer Bestand aus Warehouse-System abrufen
    ├─→ Mit DB-Bestand vergleichen
    ├─→ Diskrepanzen identifizieren
    │
    ├─→ Für jede Diskrepanz:
    │   ├─→ Bestand korrigieren
    │   ├─→ Kafka: inventory.corrected Event
    │   │   ├─→ Order Service: Verfügbarkeit neu berechnen
    │   │   ├─→ Manufacturing Service: Planung anpassen
    │   │   └─→ Audit Log: Änderung dokumentieren
    │   │
    │   └─→ Alert: Große Diskrepanzen (>5%)
    │       └─→ Warehouse Manager benachrichtigen
    │
    └─→ Report: Tägliche Bestandsabstimmung
```

---

## 7. Szenario: Rückgabe/Retoure

```
Customer initiates Return
         ↓
    [Order Service]
    ├─→ Return erstellen
    ├─→ Status: INITIATED
    ├─→ Kafka: return.initiated Event
    │
    Product Service empfängt return.initiated
    ├─→ Bestand als "in return" markieren
    ├─→ Kafka: inventory.in-return Event
    │
    Warehouse empfängt Rückgabe
    ├─→ Physische Inspektion
    ├─→ REST: Product Service → updateReturnStatus()
    │   ├─→ Status: RECEIVED
    │   ├─→ Kafka: return.received Event
    │
    Order Service empfängt return.received
    ├─→ Rückerstattung initiieren
    ├─→ REST: Accounting Service → createRefund()
    │
    Accounting Service
    ├─→ Rückerstattung verarbeiten
    ├─→ Kafka: refund.processed Event
    │
    Product Service empfängt refund.processed
    ├─→ Bestand endgültig aktualisieren
    ├─→ Kafka: inventory.updated Event
    │
    Order Service empfängt inventory.updated
    └─→ Return abschließen: Status COMPLETED
```

---

## 8. Szenario: Bestandswarnung und Nachbestellung

```
Täglich um 08:00 Uhr:

Product Service - Inventory Check
    ├─→ Alle Produkte durchlaufen
    ├─→ Für jedes Produkt:
    │   ├─→ Aktueller Bestand < Minimum?
    │   │   ├─→ Ja: Kafka: stock.low Event
    │   │   │   └─→ Procurement System
    │   │   │       ├─→ Lieferanten abfragen
    │   │   │       ├─→ REST: Party Service → Lieferantendaten
    │   │   │       ├─→ Bestellung erstellen
    │   │   │       └─→ Kafka: purchase-order.created
    │   │   │
    │   │   └─→ Nein: Weiter zum nächsten Produkt
    │   │
    │   └─→ Bestand < Kritisches Minimum?
    │       ├─→ Ja: Alert an Warehouse Manager
    │       └─→ Nein: Weiter
    │
    └─→ Report: Tägliche Bestandswarnung
```

---

## 9. Szenario: Produktion und Bestandsverbrauch

```
Manufacturing Service - Production Run
    ├─→ Production Run erstellen
    ├─→ Status: SCHEDULED
    ├─→ Kafka: production-run.scheduled Event
    │
    Product Service empfängt production-run.scheduled
    ├─→ Rohstoffe reservieren (BOM)
    ├─→ Kafka: inventory.reserved Event
    │
    Manufacturing Service - Production Start
    ├─→ Status: IN_PROGRESS
    ├─→ Kafka: production-run.started Event
    │
    Product Service empfängt production-run.started
    ├─→ Rohstoffe verbrauchen
    ├─→ Kafka: inventory.consumed Event
    │
    Manufacturing Service - Production Complete
    ├─→ Status: COMPLETED
    ├─→ Fertigprodukte erstellen
    ├─→ Kafka: production-run.completed Event
    │
    Product Service empfängt production-run.completed
    ├─→ Fertigprodukte zum Bestand hinzufügen
    ├─→ Kafka: inventory.updated Event
    │
    Order Service empfängt inventory.updated
    └─→ Verfügbare Bestellungen freigeben
```

---

## 10. Szenario: Benutzer-Authentifizierung und Autorisierung

```
Frontend-Request
    ↓
[API Gateway]
    ├─→ Authorization Header prüfen
    ├─→ REST: Auth Service → validateToken()
    │   ├─→ Token gültig?
    │   │   ├─→ Ja: User-Kontext extrahieren
    │   │   │   ├─→ User ID
    │   │   │   ├─→ Rollen
    │   │   │   └─→ Berechtigungen
    │   │   │
    │   │   └─→ Nein: 401 Unauthorized
    │   │
    │   └─→ Berechtigungen für Endpoint prüfen
    │       ├─→ Ja: Request weiterleiten
    │       └─→ Nein: 403 Forbidden
    │
    ├─→ Request an Service weiterleiten
    │   ├─→ User-Kontext im Header mitgeben
    │   └─→ Service kann User-Kontext nutzen
    │
    └─→ Response zurück an Frontend
```

---

## 11. Szenario: Asynchrone Berichterstellung

```
Manager fordert Bericht an
         ↓
    [Report Service]
    ├─→ Bericht-Job erstellen
    ├─→ Status: QUEUED
    ├─→ Kafka: report.requested Event
    │
    Report Worker empfängt report.requested
    ├─→ Status: PROCESSING
    ├─→ REST: Order Service → getOrders(filter)
    ├─→ REST: Accounting Service → getInvoices(filter)
    ├─→ REST: Product Service → getInventory(filter)
    ├─→ Daten aggregieren
    ├─→ PDF/Excel generieren
    ├─→ In S3/Blob Storage speichern
    ├─→ Kafka: report.completed Event
    │
    Report Service empfängt report.completed
    ├─→ Status: COMPLETED
    ├─→ Download-Link generieren
    ├─→ Manager benachrichtigen (Email)
    └─→ Bericht zum Download bereit
```

---

## 12. Szenario: Monitoring und Alerting

```
Alle Services
    ├─→ Metriken an Prometheus senden
    │   ├─→ Request Count
    │   ├─→ Response Time
    │   ├─→ Error Rate
    │   ├─→ Database Connection Pool
    │   └─→ Message Queue Lag
    │
    ├─→ Logs an ELK Stack senden
    │   ├─→ Request/Response Logs
    │   ├─→ Error Logs
    │   ├─→ Business Events
    │   └─→ Audit Logs
    │
    └─→ Traces an Jaeger senden
        ├─→ Request Flow durch Services
        ├─→ Latenz-Analyse
        └─→ Bottleneck-Identifikation

Prometheus + Alertmanager
    ├─→ Alerts definieren
    │   ├─→ Error Rate > 5%
    │   ├─→ Response Time > 1s
    │   ├─→ Service Down
    │   ├─→ Database Connection Pool > 80%
    │   └─→ Kafka Lag > 10000 Messages
    │
    └─→ Alerts senden
        ├─→ Slack/Teams Notification
        ├─→ PagerDuty (für kritische Alerts)
        └─→ Email
```

---

## Fehlerbehandlung und Resilience Patterns

### Circuit Breaker Pattern

```
Order Service → Product Service (REST)

Normal State:
    ├─→ Request erfolgreich
    └─→ Circuit: CLOSED

Fehler erkannt:
    ├─→ 5 Fehler in Folge
    ├─→ Circuit: OPEN
    └─→ Neue Requests werden sofort abgelehnt

Nach Timeout (z.B. 30s):
    ├─→ Circuit: HALF_OPEN
    ├─→ Test-Request senden
    ├─→ Erfolgreich? → Circuit: CLOSED
    └─→ Fehler? → Circuit: OPEN (Timeout zurücksetzen)
```

### Retry Pattern

```
Order Service → Accounting Service (REST)

Versuch 1: Fehler (Timeout)
    ├─→ Warten: 100ms
    └─→ Versuch 2

Versuch 2: Fehler (500 Internal Server Error)
    ├─→ Warten: 200ms
    └─→ Versuch 3

Versuch 3: Fehler (500 Internal Server Error)
    ├─→ Warten: 400ms
    └─→ Versuch 4

Versuch 4: Erfolg
    └─→ Request erfolgreich

Max Retries: 4
Backoff Strategy: Exponential (100ms, 200ms, 400ms, 800ms)
```

### Timeout Pattern

```
Order Service → Product Service (REST)

Request gesendet
    ├─→ Warten: 5 Sekunden
    ├─→ Keine Response
    ├─→ Timeout ausgelöst
    ├─→ Request abgebrochen
    ├─→ Circuit Breaker: Fehler registriert
    └─→ Fallback: Cached Data oder Default Value
```

---

## Konsistenzmodelle

### Eventual Consistency (Empfohlen)

```
Order Service erstellt Bestellung
    ├─→ Bestellung in DB gespeichert
    ├─→ order.created Event publiziert
    │
    Product Service empfängt order.created
    ├─→ Bestand reservieren (asynchron)
    ├─→ Kafka: inventory.reserved Event
    │
    Order Service empfängt inventory.reserved
    ├─→ Bestellung Status: CONFIRMED
    │
    Zeitverzögerung: ~100-500ms
    Konsistenz: Eventual (nach kurzer Zeit konsistent)
```

### Strong Consistency (für kritische Operationen)

```
Order Service erstellt Bestellung
    ├─→ REST: Product Service → reserveInventory()
    │   ├─→ Synchroner Call
    │   ├─→ Warten auf Response
    │   ├─→ Erfolg? → Weiter
    │   └─→ Fehler? → Bestellung abbrechen
    │
    ├─→ REST: Accounting Service → createInvoice()
    │   ├─→ Synchroner Call
    │   ├─→ Warten auf Response
    │   ├─→ Erfolg? → Bestellung bestätigen
    │   └─→ Fehler? → Bestandsreservierung aufheben
    │
    Zeitverzögerung: ~500-2000ms
    Konsistenz: Strong (sofort konsistent)
```

---

## Skalierungsszenarien

### Szenario: Black Friday - Hohe Last

```
Normale Last: 100 Requests/s
Black Friday: 10.000 Requests/s

Maßnahmen:
    ├─→ Order Service: 1 → 10 Instanzen
    ├─→ Product Service: 1 → 5 Instanzen
    ├─→ Accounting Service: 1 → 3 Instanzen
    ├─→ Kafka Partitionen: 3 → 20
    ├─→ Database Read Replicas: 1 → 3
    ├─→ Cache (Redis): Aktivieren
    │   ├─→ Product Catalog cachen
    │   ├─→ Pricing cachen
    │   └─→ Inventory cachen (mit TTL)
    │
    └─→ API Gateway: Rate Limiting
        ├─→ Pro User: 100 Requests/min
        ├─→ Pro IP: 1000 Requests/min
        └─→ Excess Requests: Queue oder Reject
```

---

## Deployment-Strategie

### Blue-Green Deployment

```
Alte Version (Blue)
    ├─→ 100% Traffic
    └─→ Läuft stabil

Neue Version (Green)
    ├─→ 0% Traffic
    ├─→ Parallel deployen
    ├─→ Tests durchführen
    ├─→ Smoke Tests bestätigen
    │
    └─→ Traffic umschalten
        ├─→ 10% Traffic → Green
        ├─→ Monitoring: Fehlerrate OK?
        ├─→ 50% Traffic → Green
        ├─→ Monitoring: Fehlerrate OK?
        ├─→ 100% Traffic → Green
        │
        └─→ Rollback möglich: Zurück zu Blue
```

### Canary Deployment

```
Alte Version: 95% Traffic
Neue Version: 5% Traffic
    ├─→ Monitoring: Fehlerrate, Latenz
    ├─→ Nach 1 Stunde: OK?
    │   ├─→ Ja: 50% Traffic zur neuen Version
    │   └─→ Nein: Rollback
    │
    ├─→ Nach 2 Stunden: OK?
    │   ├─→ Ja: 100% Traffic zur neuen Version
    │   └─→ Nein: Rollback
    │
    └─→ Alte Version: Abschalten
```
