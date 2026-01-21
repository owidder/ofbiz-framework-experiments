# Woche 2: Quick Start Guide

**Ziel:** Datenmodell & API-Design für den Party Service implementieren  
**Dauer:** 5 Tage (40 Stunden)  
**Status:** 🚀 Bereit zum Start

---

## 📋 Was wird in Woche 2 gemacht?

Woche 2 legt das **Fundament** für die gesamte Party-Service-Implementierung:

1. **Datenmodell analysieren** - Welche Tabellen brauchen wir aus OFBiz?
2. **Datenbankschema erstellen** - PostgreSQL-Schema mit Flyway Migrations
3. **API designen** - REST API mit 30+ Endpoints (OpenAPI 3.0)
4. **DTOs definieren** - Data Transfer Objects für API-Kommunikation
5. **Events definieren** - Kafka Event-Schema für asynchrone Kommunikation
6. **Test-Daten erstellen** - Realistische Test-Daten für Entwicklung

---

## 🎯 Deliverables (Was entsteht?)

Nach Woche 2 hast du:

### Dokumentation (4 Dateien)
- ✅ `PARTY_ENTITIES_ANALYSIS.md` - Alle ~30 Party-Tabellen dokumentiert
- ✅ `PARTY_ER_DIAGRAM.md` - ER-Diagramm (Mermaid)
- ✅ `PARTY_DATA_MODEL.md` - Datenmodell-Beschreibung
- ✅ `KAFKA_EVENTS.md` - Event-Dokumentation

### Datenbank (5 SQL-Dateien)
- ✅ `V1__create_party_base_tables.sql` - Party, Person, PartyGroup
- ✅ `V2__create_contact_mech_tables.sql` - ContactMech, PostalAddress, TelecomNumber
- ✅ `V3__create_role_relationship_tables.sql` - PartyRole, PartyRelationship
- ✅ `V4__create_indexes.sql` - Performance-Indizes
- ✅ `V99__test_data.sql` - Test-Daten (nur dev)

### API-Design (1 YAML + ~20 Java-Klassen)
- ✅ `party-service-api.yaml` - OpenAPI 3.0 Spezifikation (30+ Endpoints)
- ✅ ~12 DTO-Klassen (PartyDTO, PersonDTO, ContactMechDTO, etc.)
- ✅ 8 Event-DTO-Klassen (PartyCreatedEvent, etc.)

**Gesamt:** ~30 Dateien, ~2000 Zeilen Code/SQL/YAML

---

## 🚀 Wie starte ich?

### Option 1: Schritt für Schritt (Empfohlen für Lernen)

Öffne [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md) und arbeite die **15 Prompts** nacheinander ab:

```
Tag 1-2: Datenmodell-Analyse
  → Prompt 1: Neo4j-Analyse
  → Prompt 2: ER-Diagramm

Tag 2-3: PostgreSQL-Schema
  → Prompt 3: Flyway V1 (Basis-Tabellen)
  → Prompt 4: Flyway V2 (ContactMech)
  → Prompt 5: Flyway V3 (Rollen & Beziehungen)
  → Prompt 6: Flyway V4 (Indizes)

Tag 3-4: API-Design
  → Prompt 7: OpenAPI Party Management
  → Prompt 8: OpenAPI Contact Management
  → Prompt 9: OpenAPI Relationships & Batch
  → Prompt 10: Basis-DTOs
  → Prompt 11: Weitere DTOs

Tag 4-5: Events & Test-Daten
  → Prompt 12: Kafka Event-DTOs
  → Prompt 13: Kafka Topics Konfiguration
  → Prompt 14: Test-Daten

Validierung:
  → Prompt 15: Validierung durchführen
```

**Vorgehen:**
1. Öffne [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md)
2. Kopiere Prompt 1
3. Gib ihn an Roo (Party-PoC Mode)
4. Warte auf Ergebnis
5. Prüfe das Ergebnis
6. Weiter mit Prompt 2
7. Wiederhole bis Prompt 15

---

### Option 2: Batch-Modus (Schneller, aber weniger Kontrolle)

Gib Roo alle Prompts auf einmal:

```
Implementiere Woche 2 des Party Service PoC:

1. Führe alle 15 Prompts aus WOCHE_2_PROMPTS.md aus
2. Erstelle alle Deliverables
3. Validiere am Ende

Arbeite die Prompts sequenziell ab und dokumentiere jeden Schritt.
```

**Vorteil:** Schneller  
**Nachteil:** Weniger Kontrolle, schwerer zu debuggen

---

## 📚 Detaillierte Dokumentation

Für mehr Details, siehe:
- [`WOCHE_2_IMPLEMENTATION_GUIDE.md`](./WOCHE_2_IMPLEMENTATION_GUIDE.md) - Ausführlicher Guide mit Erklärungen
- [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md) - Konkrete Prompts zum Kopieren
- [`MIGRATION_STATUS.md`](./MIGRATION_STATUS.md) - Gesamtfortschritt

---

## 🔍 Was passiert in jedem Schritt?

### Tag 1-2: Datenmodell-Analyse (2 Tage)

**Was wird gemacht?**
- Neo4j-Datenbank analysieren, um OFBiz Party-Tabellen zu finden
- ~30 Tabellen identifizieren und dokumentieren
- ER-Diagramm erstellen für visuelles Verständnis

**Warum wichtig?**
- Verstehen, welche Daten wir migrieren müssen
- Beziehungen zwischen Tabellen verstehen
- Basis für Datenbankschema

**Output:**
- `PARTY_ENTITIES_ANALYSIS.md` - Tabellen-Liste
- `PARTY_ER_DIAGRAM.md` - Visuelles Diagramm
- `PARTY_DATA_MODEL.md` - Beschreibung

---

### Tag 2-3: PostgreSQL-Schema (1.5 Tage)

**Was wird gemacht?**
- Flyway Migrations erstellen für PostgreSQL
- 4 Migrations: Basis-Tabellen, ContactMech, Rollen/Beziehungen, Indizes
- Test-Daten-Migration (nur für dev)

**Warum wichtig?**
- Versioniertes Datenbankschema
- Reproduzierbar und testbar
- Basis für JPA Entities (Woche 3)

**Output:**
- `V1__create_party_base_tables.sql`
- `V2__create_contact_mech_tables.sql`
- `V3__create_role_relationship_tables.sql`
- `V4__create_indexes.sql`
- `V99__test_data.sql`

---

### Tag 3-4: API-Design (1.5 Tage)

**Was wird gemacht?**
- OpenAPI 3.0 Spezifikation schreiben
- 30+ REST Endpoints definieren
- DTOs (Data Transfer Objects) erstellen
- Request/Response-Beispiele

**Warum wichtig?**
- Klare API-Spezifikation für Frontend/Clients
- Basis für Controller-Implementierung (Woche 3)
- Swagger UI für Testing

**Output:**
- `party-service-api.yaml` - OpenAPI Spec
- ~12 DTO-Klassen (Java)

---

### Tag 4-5: Events & Test-Daten (1 Tag)

**Was wird gemacht?**
- Kafka Event-Schema definieren
- 8 Event-DTOs erstellen
- Kafka Topics konfigurieren
- Test-Daten erstellen

**Warum wichtig?**
- Asynchrone Kommunikation mit anderen Services
- Event-Driven Architecture
- Test-Daten für Entwicklung

**Output:**
- 8 Event-DTO-Klassen
- `KAFKA_EVENTS.md` - Event-Dokumentation
- Kafka Topics in `application.yml`
- Test-Daten in `V99__test_data.sql`

---

## ✅ Wie validiere ich das Ergebnis?

Nach Abschluss von Woche 2:

### 1. Datenbank-Schema testen
```bash
# Spring Boot starten
./gradlew :microservices:party-service:bootRun

# H2 Console öffnen
open http://localhost:8081/api/party/h2-console

# Prüfen:
# - Alle Tabellen erstellt?
# - Test-Daten vorhanden?
```

### 2. OpenAPI-Spezifikation validieren
```bash
# Swagger UI öffnen
open http://localhost:8081/api/party/swagger-ui.html

# Prüfen:
# - Alle 30+ Endpoints sichtbar?
# - Schemas korrekt?
# - Beispiele vorhanden?
```

### 3. Build durchführen
```bash
# Kompilieren
./gradlew :microservices:party-service:build

# Sollte ohne Fehler durchlaufen
```

### 4. Test-Daten prüfen
```sql
-- In H2 Console
SELECT COUNT(*) FROM party;          -- Sollte 8 sein (5 Personen + 3 Firmen)
SELECT COUNT(*) FROM person;         -- Sollte 5 sein
SELECT COUNT(*) FROM party_group;    -- Sollte 3 sein
SELECT COUNT(*) FROM postal_address; -- Sollte 4 sein
```

---

## 🎓 Lernziele

Nach Woche 2 verstehst du:

- ✅ **Datenmodellierung** - Wie man ein komplexes Datenmodell analysiert und dokumentiert
- ✅ **Flyway Migrations** - Wie man versionierte Datenbankschemas erstellt
- ✅ **API-Design** - Wie man REST APIs mit OpenAPI 3.0 designt
- ✅ **DTOs** - Wie man Data Transfer Objects für API-Kommunikation erstellt
- ✅ **Event-Driven Architecture** - Wie man Kafka Events definiert
- ✅ **Neo4j-Analyse** - Wie man Neo4j nutzt, um Legacy-Code zu analysieren

---

## 🚨 Häufige Probleme & Lösungen

### Problem: Neo4j-Datenbank nicht erreichbar
**Lösung:**
```bash
# Neo4j starten
cd tools/jqassistant-commandline-neo4jv5-2.8.0/macos
./start_jqa_db.sh /Users/oliverwidder/dev/ofbiz neo4j
```

### Problem: Flyway Migration schlägt fehl
**Lösung:**
- Prüfe SQL-Syntax
- Prüfe, ob Tabellen bereits existieren
- Prüfe Foreign Key Constraints

### Problem: DTOs kompilieren nicht
**Lösung:**
- Prüfe Lombok-Installation
- Prüfe Jackson-Dependencies
- Prüfe Bean Validation-Dependencies

### Problem: Swagger UI zeigt keine Endpoints
**Lösung:**
- Prüfe, ob `party-service-api.yaml` im richtigen Verzeichnis liegt
- Prüfe Springdoc-Konfiguration in `application.yml`
- Prüfe, ob Controller existieren (kommen in Woche 3)

---

## 📊 Fortschritt tracken

Nutze die Todo-Liste in [`MIGRATION_STATUS.md`](./MIGRATION_STATUS.md):

```markdown
### Woche 2: Datenmodell & API-Design
- [ ] Party-Tabellen identifizieren (Neo4j)
- [ ] ER-Diagramm erstellen
- [ ] Flyway V1-V4 Migrations
- [ ] OpenAPI 3.0 Spezifikation
- [ ] DTOs erstellen
- [ ] Event-Schema definieren
- [ ] Test-Daten erstellen
- [ ] Validierung durchführen
```

Aktualisiere nach jedem abgeschlossenen Schritt!

---

## 🎯 Erfolgskriterien

Woche 2 ist erfolgreich abgeschlossen, wenn:

- ✅ Alle ~30 Party-Tabellen dokumentiert
- ✅ ER-Diagramm erstellt
- ✅ 5 Flyway Migrations erstellt
- ✅ OpenAPI 3.0 Spezifikation mit 30+ Endpoints
- ✅ ~12 DTO-Klassen erstellt
- ✅ 8 Event-DTO-Klassen erstellt
- ✅ Test-Daten vorhanden
- ✅ Build erfolgreich
- ✅ H2-Datenbank läuft mit Test-Daten
- ✅ Swagger UI zeigt API-Spezifikation

---

## 🔜 Was kommt als Nächstes?

Nach Woche 2 folgt **Woche 3: Basis-Implementierung**:

- JPA Entities erstellen (basierend auf Flyway Migrations)
- Repositories implementieren (Spring Data JPA)
- Core Services implementieren (PartyService, ContactMechService)
- Unit Tests schreiben (>80% Coverage)

**Vorbereitung:**
- Datenmodell verstanden ✅
- API-Design abgeschlossen ✅
- Bereit für Implementierung ✅

---

## 💡 Tipps für Woche 2

1. **Arbeite sequenziell** - Jeder Schritt baut auf dem vorherigen auf
2. **Dokumentiere alles** - Du wirst es später brauchen
3. **Nutze Neo4j aktiv** - Es hilft, OFBiz zu verstehen
4. **Teste nach jedem Schritt** - Früh Fehler finden
5. **Frage bei Unklarheiten** - Besser fragen als falsch implementieren
6. **Committe regelmäßig** - Nach jedem abgeschlossenen Schritt
7. **Nutze die Prompts** - Sie sind optimiert für Roo

---

## 📞 Support

Bei Fragen oder Problemen:
1. Prüfe [`WOCHE_2_IMPLEMENTATION_GUIDE.md`](./WOCHE_2_IMPLEMENTATION_GUIDE.md)
2. Prüfe [`MIGRATION_STATUS.md`](./MIGRATION_STATUS.md)
3. Frage Roo im Party-PoC Mode

---

## 🎉 Los geht's!

**Nächster Schritt:**
1. Öffne [`WOCHE_2_PROMPTS.md`](./WOCHE_2_PROMPTS.md)
2. Kopiere **Prompt 1** (Neo4j-Analyse)
3. Gib ihn an Roo
4. Arbeite dich durch alle 15 Prompts

**Viel Erfolg! 🚀**

---

**Erstellt:** 2026-01-21  
**Version:** 1.0  
**Autor:** Roo AI (Party-PoC Mode)
