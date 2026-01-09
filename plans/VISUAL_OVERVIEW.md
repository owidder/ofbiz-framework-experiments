# OFBiz Microservices Migration - Visuelle Übersicht

## 🏗️ Architektur-Übersicht

### Aktuelle Monolith-Struktur
```
┌─────────────────────────────────────────────────────────────┐
│                    OFBiz Monolith                           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  Order   │  │ Product  │  │ Shipment │  │ Catalog  │   │
│  │ Service  │  │ Service  │  │ Service  │  │ Service  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │Accounting│  │Marketing │  │ Content  │  │   SFA    │   │
│  │ Service  │  │ Service  │  │ Service  │  │ Service  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│                                                             │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                 │
│  │HumanRes  │  │Manufacturing│ Party    │                 │
│  │ Service  │  │ Service  │  │ Service  │                 │
│  └──────────┘  └──────────┘  └──────────┘                 │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Shared Database                        │   │
│  │         (Entity Engine, Data Model)                 │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Zielarchitektur (Microservices)
```
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway                              │
│              (Kong, Nginx, AWS API Gateway)                 │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
    ┌────────┐          ┌────────┐         ┌────────┐
    │ Order  │          │Product │         │Shipment│
    │Service │          │Service │         │Service │
    └────────┘          └────────┘         └────────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
    ┌────────┐          ┌────────┐         ┌────────┐
    │Catalog │          │Content │         │ Party  │
    │Service │          │Service │         │Service │
    └────────┘          └────────┘         └────────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
    ┌────────┐          ┌────────┐         ┌────────┐
    │Accounting          │Marketing        │HumanRes│
    │Service │          │Service │         │Service │
    └────────┘          └────────┘         └────────┘

┌─────────────────────────────────────────────────────────────┐
│                  Message Broker (Kafka)                     │
│  party.created | party.updated | order.created | ...       │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│              Service Mesh (Istio/Linkerd)                   │
│  Circuit Breaker | Retry | Timeout | Load Balancing        │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│         Observability Stack                                 │
│  Prometheus | Grafana | ELK Stack | Jaeger Tracing         │
└─────────────────────────────────────────────────────────────┘
```

---

## 📊 Migrations-Roadmap

```
PHASE 1: FOUNDATION (Wochen 1-4)
┌─────────────────────────────────────────────────────────┐
│ ✓ Infrastruktur-Setup (Docker, Kubernetes)              │
│ ✓ Monitoring & Logging (ELK, Prometheus)                │
│ ✓ API Gateway (Kong)                                    │
│ ✓ Message Broker (Kafka)                                │
│ ✓ Service Mesh (Istio)                                  │
└─────────────────────────────────────────────────────────┘

PHASE 2: EINFACHE SERVICES (Wochen 5-16)
┌─────────────────────────────────────────────────────────┐
│ Week 5-7:   Content Service ⭐ (Pilot)                  │
│ Week 8-10:  Marketing Service                           │
│ Week 11-13: Manufacturing Service                       │
│ Week 14-16: Testing & Stabilisierung                    │
└─────────────────────────────────────────────────────────┘

PHASE 3: MITTLERE SERVICES (Wochen 17-40)
┌─────────────────────────────────────────────────────────┐
│ Week 17-21:  Catalog Service                            │
│ Week 22-27:  Product Service                            │
│ Week 28-35:  Shipment Service                           │
│ Week 36-40:  Testing & Stabilisierung                   │
└─────────────────────────────────────────────────────────┘

PHASE 4: KOMPLEXE SERVICES (Wochen 41-80)
┌─────────────────────────────────────────────────────────┐
│ Week 41-50:  Accounting Service                         │
│ Week 51-65:  Order Service                              │
│ Week 66-80:  Testing & Stabilisierung                   │
└─────────────────────────────────────────────────────────┘

PHASE 5: PARTY SERVICE (Wochen 81-120)
┌─────────────────────────────────────────────────────────┐
│ Week 81-100:  Party Service (Strangler Fig)             │
│ Week 101-110: Migration & Fallback                      │
│ Week 111-120: Monolith-Cleanup & Finalisierung          │
└─────────────────────────────────────────────────────────┘

PHASE 6: STABILISIERUNG (Wochen 121-144)
┌─────────────────────────────────────────────────────────┐
│ Week 121-144: Performance-Optimierung                   │
│               Disaster Recovery Tests                   │
│               Dokumentation & Training                  │
└─────────────────────────────────────────────────────────┘
```

---

## 🎯 Service-Komplexität Matrix

```
                    KOMPLEXITÄT
                        ▲
                        │
                  HIGH  │  ┌─────────────────────┐
                        │  │  Order Service      │
                        │  │  (120 Aufrufe)      │
                        │  │  12-15 Wochen       │
                        │  └─────────────────────┐
                        │         ▲              │
                        │         │              │
                        │  ┌──────┴──────┐       │
                        │  │ Accounting  │       │
                        │  │ (80 Aufrufe)│       │
                        │  │ 8-10 Wochen │       │
                        │  └─────────────┘       │
                        │                        │
                  MID   │  ┌──────────────────┐  │
                        │  │ Shipment Service │  │
                        │  │ (60 Aufrufe)     │  │
                        │  │ 6-8 Wochen       │  │
                        │  └──────────────────┘  │
                        │                        │
                        │  ┌──────────────────┐  │
                        │  │ Product Service  │  │
                        │  │ (50 Aufrufe)     │  │
                        │  │ 5-6 Wochen       │  │
                        │  └──────────────────┘  │
                        │                        │
                  LOW   │  ┌──────────────────┐  │
                        │  │ Content Service  │  │
                        │  │ (30 Aufrufe)     │  │
                        │  │ 2-3 Wochen ⭐    │  │
                        │  └──────────────────┘  │
                        │                        │
                        └────────────────────────┼──────► ABHÄNGIGKEITEN
                                                 
                        LOW    MID    HIGH    VERY HIGH
```

---

## 🔄 Party Service Abhängigkeits-Graph

```
                    ┌─────────────────┐
                    │  Party Service  │
                    │   (436 Aufrufe) │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
    ┌────────┐          ┌────────┐          ┌────────┐
    │ Order  │          │Shipment│          │Accounting
    │Service │          │Service │          │Service
    │(20)    │          │(12)    │          │(8)
    └────────┘          └────────┘          └────────┘
        │                    │                    │
        └────────────────────┼────────────────────┘
                             │
                    ┌────────▼────────┐
                    │  Monolith Core  │
                    │  (Shared DB)    │
                    └─────────────────┘

Legend: (X) = Anzahl der Aufrufe
```

---

## 📈 Aufwands-Schätzung pro Service

```
Service                 Aufwand (Wochen)    Aufwand (Monate)
─────────────────────────────────────────────────────────
Content Service         2-3                 0.5-0.7         ⭐
Marketing Service       2-3                 0.5-0.7
Manufacturing Service   2-3                 0.5-0.7
Catalog Service         4-5                 0.9-1.2
Product Service         5-6                 1.2-1.4
Shipment Service        6-8                 1.4-1.8
Accounting Service      8-10                1.8-2.3
Order Service           12-15               2.8-3.5
Party Service           17-18               3.9-4.2         ⚠️
─────────────────────────────────────────────────────────
GESAMT                  ~60-70              ~14-17 Monate
+ Foundation            4                   1 Monat
+ Stabilisierung        20-24               5-6 Monate
─────────────────────────────────────────────────────────
TOTAL MIGRATION         ~84-98              ~20-24 Monate
```

---

## 🔗 Party Service - Kritische Methoden

```
Methode                                    Aufrufe    Kritikalität
─────────────────────────────────────────────────────────────────
PartyHelper.getPartyName()                 30         🔴 KRITISCH
PartyContentWrapper.getPartyContentAsText()28         🔴 KRITISCH
PartyWorker.findPartyLatestContactMech()   20         🟠 HOCH
PartyServices.getPartyId()                 16         🟠 HOCH
ContactMechWorker.getPartyContactMechVMs() 16         🟠 HOCH
ContactHelper.getContactMech()             14         🟠 HOCH
ContactMechWorker.getFacilityContactMech() 14         🟠 HOCH
CommunicationEventServices.buildList...()  12         🟡 MITTEL
ContactMechWorker.insertRelatedContact()   12         🟡 MITTEL
PartyWorker.findPartyLatestUserLogin()     12         🟡 MITTEL
```

---

## 🚀 Strangler Fig Pattern für Party Service

```
PHASE 1: ADAPTER-LAYER
┌─────────────────────────────────────────────────────────┐
│ Monolith                                                │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Order Service                                       │ │
│ │ ┌──────────────────────────────────────────────┐   │ │
│ │ │ Party Service Calls                          │   │ │
│ │ │ ↓                                            │   │ │
│ │ │ [Adapter Layer]                              │   │ │
│ │ │ ├─ Local Party Service (Monolith)            │   │ │
│ │ │ └─ Remote Party Service (New)                │   │ │
│ │ └──────────────────────────────────────────────┘   │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘

PHASE 2: SCHRITTWEISE MIGRATION
┌─────────────────────────────────────────────────────────┐
│ Monolith (20% Party Calls)                              │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Order Service                                       │ │
│ │ ┌──────────────────────────────────────────────┐   │ │
│ │ │ [Adapter Layer]                              │   │ │
│ │ │ ├─ 80% → Remote Party Service (New)          │   │ │
│ │ │ └─ 20% → Local Party Service (Monolith)      │   │ │
│ │ └──────────────────────────────────────────────┘   │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
                ┌─────────────────────────┐
                │ Party Service (New)     │
                │ (Microservice)          │
                └─────────────────────────┘

PHASE 3: VOLLSTÄNDIGE MIGRATION
┌─────────────────────────────────────────────────────────┐
│ Monolith (ohne Party Service)                           │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ Order Service                                       │ │
│ │ ┌──────────────────────────────────────────────┐   │ │
│ │ │ [Adapter Layer]                              │   │ │
│ │ │ └─ 100% → Remote Party Service (New)         │   │ │
│ │ └──────────────────────────────────────────────┘   │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
                ┌─────────────────────────┐
                │ Party Service (New)     │
                │ (Microservice)          │
                │ 100% Traffic            │
                └─────────────────────────┘
```

---

## 📋 Kommunikations-Patterns

### REST-API Pattern
```
Client Request:
  GET /api/party/12345/name
  
Service Response:
  {
    "partyId": "12345",
    "name": "Acme Corp",
    "type": "COMPANY"
  }
```

### Kafka Event Pattern
```
Event Published:
  Topic: party.created
  {
    "eventId": "evt-001",
    "partyId": "12345",
    "name": "Acme Corp",
    "timestamp": "2026-01-09T18:00:00Z"
  }

Subscribers:
  - Order Service (aktualisiert Bestellungen)
  - Accounting Service (erstellt Konten)
  - Shipment Service (aktualisiert Adressen)
```

### Circuit Breaker Pattern
```
Normal State:
  Request → [Circuit Breaker] → Service → Response
                    ✓

Failure State (nach 5 Fehlern):
  Request → [Circuit Breaker] → Fallback Response
                    ✗ (Open)

Recovery State (nach 30 Sekunden):
  Request → [Circuit Breaker] → Service (Test)
                    ? (Half-Open)
```

---

## ✅ Erfolgskriterien

```
Phase 1: Foundation
  ✓ Infrastruktur läuft stabil
  ✓ Monitoring zeigt alle Metriken
  ✓ Logging funktioniert zentral

Phase 2: Content Service
  ✓ Service läuft in Production
  ✓ Keine Performance-Degradation
  ✓ Fehlerrate < 0.1%

Phase 3-4: Weitere Services
  ✓ Alle Services kommunizieren
  ✓ Saga Pattern funktioniert
  ✓ Datenkonsistenz gewährleistet

Phase 5: Party Service
  ✓ Strangler Fig Pattern erfolgreich
  ✓ Alle Abhängigkeiten migriert
  ✓ Monolith-Code entfernt

Phase 6: Finalisierung
  ✓ Alle Services stabil
  ✓ Performance optimiert
  ✓ Team geschult
```

---

## 📞 Support & Kontakt

**Fragen zur Architektur?**
- Siehe: `microservices-architecture.md`

**Fragen zur Implementierung?**
- Siehe: `implementation-guide.md`

**Fragen zur Analyse?**
- Siehe: `PARTY_SERVICE_EXTRACTION_ANALYSIS.md`

---

**Analysedatum**: 2026-01-09  
**Status**: ✅ Abgeschlossen
