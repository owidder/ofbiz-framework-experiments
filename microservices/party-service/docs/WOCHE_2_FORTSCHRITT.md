# Woche 2 - Fortschritt: Data Model & API Design

**Stand:** 24.01.2026

## ✅ Abgeschlossen

### 1. Party-Entities Analyse (Prompt 1)

**Ergebnis:** Vollständige Analyse aller 82 Party-Entities aus OFBiz

**Dateien:**
- [`PARTY_ENTITIES_ANALYSIS.md`](./PARTY_ENTITIES_ANALYSIS.md) - Detaillierte Analyse
- [`../scripts/analyze_entities.py`](../scripts/analyze_entities.py) - Analyse-Script

**Kern-Findings:**

#### Statistiken
- **Gesamt:** 82 Entities
- **Kern-Entities:** 12 (für MVP)
- **Erweiterte Entities:** 70 (für spätere Phasen)

#### Kategorien
- **Party:** 35 Entities (Kern-Domain)
- **Agreement:** 24 Entities (Verträge/Vereinbarungen)
- **Contact:** 15 Entities (Kontaktmechanismen)
- **Communication:** 8 Entities (Kommunikation)

#### Die 12 Kern-Entities für MVP

| Entity | Beschreibung | Felder | Beziehungen |
|--------|--------------|--------|-------------|
| **Party** | Basis-Entity für alle Parteien | 12 | 7 |
| **Person** | Natürliche Personen | 32 | 4 |
| **PartyGroup** | Organisationen/Firmen | 9 | 1 |
| **PartyRole** | Rollen einer Partei | 2 | 4 |
| **PartyRelationship** | Beziehungen zwischen Parteien | 14 | 10 |
| **RoleType** | Rollen-Typen (Kunde, Lieferant, etc.) | 4 | 1 |
| **ContactMech** | Basis für Kontaktmechanismen | 3 | 2 |
| **ContactMechType** | Typen (Email, Telefon, Adresse) | 4 | 1 |
| **PostalAddress** | Postanschriften | 18 | 8 |
| **TelecomNumber** | Telefonnummern | 5 | 1 |
| **EmailAddressVerification** | Email-Verifizierung | 3 | 0 |
| **PartyContactMech** | Zuordnung Party↔ContactMech | 11 | 9 |

#### Wichtige Erkenntnisse

1. **Hierarchie:** Party → Person/PartyGroup (Vererbung)
2. **Kontakte:** ContactMech ist abstrakt, konkrete Typen: PostalAddress, TelecomNumber
3. **Rollen:** Flexible Rollenzuordnung über PartyRole
4. **Beziehungen:** Komplexe N:M-Beziehungen mit Rollen und Zeiträumen
5. **Externe Abhängigkeiten:**
   - Geo (für Adressen)
   - StatusItem (für Status-Management)
   - UserLogin (für Audit-Felder)
   - DataSource (für Datenherkunft)

## 📋 Nächste Schritte

### Prompt 2: ER-Diagramm erstellen
**Ziel:** Visualisierung der Kern-Entities und deren Beziehungen

**Zu verwenden:**
```bash
# Mit Mermaid (empfohlen)
cd microservices/party-service
# Erstelle docs/PARTY_ER_DIAGRAM.md mit Mermaid-Syntax
```

**Inhalt:**
- Alle 12 Kern-Entities
- Primärschlüssel
- Foreign Keys
- Kardinalitäten (1:1, 1:N, N:M)
- Vererbungsbeziehungen

### Prompt 3: PostgreSQL-Schema mit Flyway
**Ziel:** Datenbankschema für den Party-Service

**Dateien zu erstellen:**
```
src/main/resources/db/migration/
├── V1__create_party_tables.sql
├── V2__create_contact_tables.sql
├── V3__create_indexes.sql
└── V4__insert_reference_data.sql
```

**Wichtig:**
- PostgreSQL-spezifische Datentypen
- Constraints (PK, FK, NOT NULL, UNIQUE)
- Indizes für Performance
- Audit-Felder (created_at, updated_at)

### Prompt 4: OpenAPI 3.0 Spezifikation
**Ziel:** REST API Definition

**Datei:** `src/main/resources/openapi/party-service-api.yaml`

**Endpoints (Beispiele):**
- `POST /api/v1/parties` - Party erstellen
- `GET /api/v1/parties/{id}` - Party abrufen
- `PUT /api/v1/parties/{id}` - Party aktualisieren
- `GET /api/v1/parties/{id}/contacts` - Kontakte abrufen
- `POST /api/v1/parties/{id}/contacts` - Kontakt hinzufügen

### Prompt 5-7: DTOs, Events, Test-Daten
Siehe [`WOCHE_2_PROMPTS.md`](../WOCHE_2_PROMPTS.md) für Details

## 🎯 Empfohlene Reihenfolge

1. ✅ **Entities analysieren** (abgeschlossen)
2. ⏭️ **ER-Diagramm** (visualisiert Struktur)
3. ⏭️ **PostgreSQL-Schema** (technische Umsetzung)
4. ⏭️ **OpenAPI-Spec** (API-Kontrakt)
5. ⏭️ **DTOs** (Java-Klassen)
6. ⏭️ **Event-Schema** (Kafka-Integration)
7. ⏭️ **Test-Daten** (für Entwicklung)

## 📊 Metriken

- **Analysierte Entities:** 82
- **Kern-Entities für MVP:** 12
- **Erweiterte Entities:** 70
- **Kategorien:** 4
- **Geschätzte Tabellen im Schema:** ~15 (inkl. Join-Tables)

## 🔗 Referenzen

- [WOCHE_2_OVERVIEW.md](../WOCHE_2_OVERVIEW.md) - Übersicht
- [WOCHE_2_IMPLEMENTATION_GUIDE.md](../WOCHE_2_IMPLEMENTATION_GUIDE.md) - Detaillierte Anleitung
- [WOCHE_2_PROMPTS.md](../WOCHE_2_PROMPTS.md) - Alle 15 Prompts
- [PARTY_ENTITIES_ANALYSIS.md](./PARTY_ENTITIES_ANALYSIS.md) - Entity-Analyse
