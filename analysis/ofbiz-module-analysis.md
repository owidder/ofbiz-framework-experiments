# OFBiz Module Analyse

**Datum:** 21. Januar 2026  
**Analysiert mit:** Neo4j MCP Server & jqAssistant  
**OFBiz Version:** Aktueller Stand der Codebasis

---

## Inhaltsverzeichnis

1. [Übersicht der fachlichen Module](#übersicht-der-fachlichen-module)
2. [Detaillierte Modulstatistiken](#detaillierte-modulstatistiken)
3. [Manufacturing-Modul Analyse](#manufacturing-modul-analyse)
4. [Verwendete Cypher-Queries](#verwendete-cypher-queries)

---

## Übersicht der fachlichen Module

### Gesamtstatistik

| Typ | Anzahl Klassen | Anzahl Methoden | Codezeilen |
|-----|----------------|-----------------|------------|
| **Fachliche Module** | 1.376 | 13.855 | 114.194 |
| **Framework-Module** | 1.442 | 15.089 | 79.896 |
| **GESAMT** | **2.818** | **28.944** | **194.090** |

### Key Insights

1. **Größenverhältnis**: Die fachlichen Module machen 49% der Klassen aus, enthalten aber 59% der Codezeilen - sie sind im Durchschnitt größer und komplexer als Framework-Module

2. **Code-Verteilung**: 
   - Durchschnittliche Klassengröße (Fachlich): ~83 LOC/Klasse
   - Durchschnittliche Klassengröße (Framework): ~55 LOC/Klasse

3. **Methodendichte**:
   - Fachliche Module: ~10 Methoden/Klasse
   - Framework-Module: ~10,5 Methoden/Klasse

---

## Detaillierte Modulstatistiken

### Fachliche Module (Applications)

Die fachlichen Module implementieren die Business-Logik von OFBiz:

| Rang | Modul | Klassen | Methoden | Codezeilen | Beschreibung |
|------|-------|---------|----------|------------|--------------|
| 1 | **product** | 334 | 3.086 | 23.989 | Produktverwaltung, Kataloge, Preise |
| 2 | **order** | 281 | 3.641 | 29.478 | Bestellverwaltung, Shopping Cart |
| 3 | **accounting** | 277 | 2.579 | 19.207 | Buchhaltung, Finanzen, Rechnungswesen |
| 4 | **content** | 133 | 1.201 | 12.150 | Content Management System |
| 5 | **webtools** | 96 | 850 | 4.463 | Web-Tools, Admin-Interface |
| 6 | **manufacturing** | 86 | 738 | 6.428 | Fertigung, Produktionsplanung |
| 7 | **party** | 83 | 805 | 6.777 | Personen, Organisationen, Kontakte |
| 8 | **workeffort** | 43 | 413 | 3.502 | Arbeitsaufwände, Projekte, Kalender |
| 9 | **shipment** | 25 | 408 | 7.081 | Versand, Logistik, Fulfillment |
| 10 | **marketing** | 14 | 107 | 756 | Marketing, Kampagnen |
| 11 | **humanres** | 3 | 23 | 184 | Personalwesen, HR |
| 12 | **sfa** | 1 | 4 | 179 | Sales Force Automation |

#### Top 3 Module nach Komplexität

1. **order** (29.478 LOC, 3.641 Methoden)
   - Höchste Methodenanzahl
   - Durchschnittlich 13 Methoden pro Klasse
   - Komplexe Geschäftslogik für Bestellprozesse

2. **product** (23.989 LOC, 3.086 Methoden)
   - Größtes Modul nach Klassenanzahl (334)
   - Umfangreiche Produktverwaltung

3. **accounting** (19.207 LOC, 2.579 Methoden)
   - Komplexe Finanzlogik
   - Integration mit vielen anderen Modulen

#### Kleine Module (Kandidaten für Konsolidierung)

- **humanres**: Nur 3 Klassen, 184 LOC
- **sfa**: Nur 1 Klasse, 179 LOC
- **marketing**: 14 Klassen, 756 LOC

Diese könnten in größere Module integriert werden.

---

### Framework-Module

Die technischen Framework-Module stellen die Infrastruktur bereit:

| Rang | Modul | Klassen | Methoden | Codezeilen | Beschreibung |
|------|-------|---------|----------|------------|--------------|
| 1 | **base** | 364 | 3.644 | 12.782 | Basis-Framework, Core-Funktionalität |
| 2 | **minilang** | 241 | 1.172 | 5.711 | Mini-Language Engine |
| 3 | **entity** | 232 | 2.911 | 16.305 | Entity Engine (ORM) |
| 4 | **widget** | 219 | 3.430 | 16.839 | UI-Widgets, Forms, Screens |
| 5 | **service** | 147 | 1.724 | 10.347 | Service Engine, SOA |
| 6 | **webapp** | 86 | 736 | 5.822 | Web Application Framework |
| 7 | **common** | 80 | 676 | 5.993 | Common Services |
| 8 | **entityext** | 26 | 299 | 3.068 | Entity Extensions |
| 9 | **security** | 12 | 121 | 928 | Security Framework |
| 10 | **datafile** | 9 | 158 | 881 | Datei-Verarbeitung |
| 11 | **testtools** | 9 | 60 | 331 | Test-Framework |
| 12 | **commonext** | 8 | 72 | 254 | Common Extensions |
| 13 | **catalina** | 7 | 74 | 433 | Tomcat Integration |
| 14 | **securityext** | 2 | 12 | 202 | Security Extensions |

#### Framework-Kern (Top 3)

1. **base** (364 Klassen, 12.782 LOC)
   - Größtes Framework-Modul
   - Grundlegende Utilities und Services

2. **widget** (219 Klassen, 16.839 LOC)
   - Höchste LOC im Framework
   - Komplexe UI-Rendering-Logik

3. **entity** (232 Klassen, 16.305 LOC)
   - ORM-Framework
   - Datenbankzugriff und Entity-Management

---

## Manufacturing-Modul Analyse

### Modul-Übersicht

- **Klassen:** 86
- **Methoden:** 738
- **Codezeilen:** 6.428
- **Durchschnitt:** 8,6 Methoden/Klasse, 74,7 LOC/Klasse

### Hauptkomponenten

Das Manufacturing-Modul ist in folgende Bereiche unterteilt:

1. **BOM (Bill of Materials)** - Stücklisten
2. **Routing** - Arbeitspläne
3. **Job Shop Management** - Produktionsaufträge
4. **MRP (Material Requirements Planning)** - Materialbedarfsplanung
5. **Techdata** - Technische Daten, Kalender
6. **Cost Calculation** - Kostenrechnung

### Datenbanktabellen (Entities)

Das Manufacturing-Modul greift auf **ca. 50+ verschiedene Entities** zu:

#### Manufacturing-spezifische Entities (~10)

**Kalender & Zeitplanung:**
- `TechDataCalendar` - Technische Kalender für Produktionsplanung
- `TechDataCalendarWeek` - Kalenderwochen-Definitionen
- `TechDataCalendarExcDay` - Ausnahmetage im Kalender
- `TechDataCalendarExcWeek` - Ausnahmewochen im Kalender

**Material Requirements Planning (MRP):**
- `MrpEventType` - Typen von MRP-Events

**Produktionsregeln & BOM:**
- `ProductManufacturingRule` - Fertigungsregeln für Produkte
- `ProductAssoc` - Produkt-Assoziationen (Bill of Materials)

**Kostenrechnung:**
- `CostComponent` - Kostenkomponenten
- `CostComponentCalc` - Kostenberechnungen
- `CostComponentType` - Kostenkomponenten-Typen

#### WorkEffort-Entities (~15)

**Hauptentities:**
- `WorkEffort` - Produktionsaufträge, Routings, Tasks
- `WorkEffortAssoc` - Verknüpfungen zwischen WorkEfforts
- `WorkEffortGoodStandard` - Material-/Produktzuordnungen
- `WorkEffortCostCalc` - Kostenberechnungen für Tasks
- `WorkEffortFixedAssetStd` - Standard-Anlagenzuordnungen
- `WorkEffortFixedAssetAssign` - Tatsächliche Anlagenzuweisungen
- `WorkEffortPartyAssignment` - Personenzuweisungen
- `WorkEffortSkillStandard` - Erforderliche Skills
- `WorkEffortPurposeType` - Zweck-Typen
- `WorkEffortContentAndInfo` - Content-Verknüpfungen
- `WorkEffortNoteAndData` - Notizen

**View-Entities:**
- `WorkEffortAssocView` - View für WorkEffort-Assoziationen
- `WorkEffortAndInventoryAssign` - View für Materialzuweisungen
- `WorkEffortAndInventoryProduced` - View für produzierte Materialien
- `WorkOrderItemFulfillment` - View für Auftragserfüllung

#### Produkt & Inventar (~10)

**Produkt-Entities:**
- `Product` - Produkte
- `ProductAssocType` - Produkt-Assoziations-Typen
- `ProductFeatureAndAppl` - Produktmerkmale

**Inventar-Entities:**
- `InventoryItem` - Lagerbestände
- `InventoryItemDetail` - Bestandsbewegungen
- `InventoryItemType` - Bestandsarten
- `ProductFacilityLocation` - Lagerplätze

#### Stammdaten (~10)

**Organisatorisches:**
- `Facility` - Lager/Produktionsstätten
- `FacilityGroup` - Lagergruppen
- `FixedAsset` - Anlagen/Maschinen
- `FixedAssetType` - Anlagentypen
- `Party` / `PartyNameView` - Personen/Organisationen
- `RoleType` - Rollen
- `SkillType` - Skill-Typen

**Aufträge & Versand:**
- `OrderItem` - Auftragspositionen
- `OrderItemShipGrpInvRes` - Reservierungen
- `Shipment` - Sendungen

**Buchhaltung:**
- `AcctgTrans` - Buchungssätze
- `Uom` - Maßeinheiten

#### System & Konfiguration (~10)

- `StatusItem` - Status-Werte
- `Enumeration` - Aufzählungswerte
- `CustomMethod` - Benutzerdefinierte Methoden
- `JobSandbox` - Geplante Jobs (für MRP)
- `UserLogin` - Benutzer-Logins
- `ElectronicText` - Elektronische Texte
- `ContentType` - Content-Typen
- `WorkEffortContentType` - WorkEffort-Content-Typen

### Entity-Kategorien Übersicht

| Kategorie | Anzahl Entities | Hauptzweck |
|-----------|-----------------|------------|
| **Manufacturing-spezifisch** | ~10 | Kalender, MRP, BOM, Kostenrechnung |
| **WorkEffort** | ~15 | Produktionsaufträge, Routing, Tasks |
| **Produkt & Inventar** | ~10 | Produkte, Bestände, Lagerplätze |
| **Stammdaten** | ~10 | Facilities, Anlagen, Personen |
| **Aufträge & Versand** | ~5 | Order-Integration |
| **System** | ~10 | Status, Enums, Jobs |

### Modul-Abhängigkeiten

Das Manufacturing-Modul ist stark integriert mit:

1. **WorkEffort-Framework** (Framework)
   - Kern der Produktionsplanung
   - Routing und Task-Management

2. **Product-Modul** (Fachlich)
   - BOM-Verwaltung
   - Produktdaten

3. **Facility-Modul** (Stammdaten)
   - Lager und Produktionsstätten
   - Anlagen und Maschinen

4. **Order-Modul** (Fachlich)
   - Integration mit Aufträgen
   - Produktionsauslösung durch Bestellungen

5. **Accounting-Modul** (Fachlich)
   - Kostenrechnung
   - Buchungssätze

6. **Party-Modul** (Fachlich)
   - Personen und Organisationen
   - Hersteller, Lieferanten

### Architektur-Erkenntnisse

**Monolithische Struktur:**
- Starke Kopplung zwischen Modulen
- Gemeinsame Datenbank-Entities
- Direkte Service-Aufrufe zwischen Modulen

**Herausforderungen für Microservices:**
- Viele geteilte Entities (z.B. WorkEffort, Product, Party)
- Transaktionale Abhängigkeiten
- Komplexe Geschäftsprozesse über Modulgrenzen hinweg

**Empfehlungen:**
- Domain-Driven Design für klare Bounded Contexts
- Event-Driven Architecture für lose Kopplung
- Schrittweise Migration (Strangler Pattern)

---

## Verwendete Cypher-Queries

### Query 1: Datenbankstatistiken

```cypher
// Abruf der Gesamtstatistiken
CALL apoc.meta.stats()
```

**Ergebnis:**
- 4.038 Types (Klassen/Interfaces)
- 312 Packages
- 105.569 Java-Nodes
- 553.464 XML-Nodes

### Query 2: Fachliche Module mit Metriken

```cypher
// Fachliche Module mit Klassen, Methoden und LOC
MATCH (c:Type:Class)
WHERE c.fqn STARTS WITH 'org.apache.ofbiz.'
WITH split(c.fqn, '.')[3] as module, c
WHERE module IN ['accounting', 'party', 'product', 'order', 'workeffort', 
                 'manufacturing', 'humanres', 'content', 'marketing', 'shipment',
                 'facility', 'passport', 'ebay', 'ebaystore', 'ldap', 'bi',
                 'projectmgr', 'scrum', 'oagis', 'assetmaint', 'ecommerce', 'webpos']
OPTIONAL MATCH (c)-[:DECLARES]->(m:Method)
WITH module, c, m
RETURN module as Modul,
       count(DISTINCT c) as AnzahlKlassen,
       count(DISTINCT m) as AnzahlMethoden,
       sum(COALESCE(m.effectiveLineCount, 0)) as Codezeilen
ORDER BY AnzahlKlassen DESC
```

### Query 3: Alle OFBiz-Module identifizieren

```cypher
// Alle OFBiz-Module identifizieren
MATCH (c:Type:Class)
WHERE c.fqn STARTS WITH 'org.apache.ofbiz.'
WITH split(c.fqn, '.')[3] as module
RETURN DISTINCT module
ORDER BY module
```

**Ergebnis:** 26 verschiedene Module identifiziert

### Query 4: Vollständige Modulübersicht mit Klassifizierung

```cypher
// Vollständige Modulübersicht mit Klassifizierung
MATCH (c:Type:Class)
WHERE c.fqn STARTS WITH 'org.apache.ofbiz.'
WITH split(c.fqn, '.')[3] as module, c
OPTIONAL MATCH (c)-[:DECLARES]->(m:Method)
WITH module, c, m,
     CASE module
       WHEN 'accounting' THEN 'Fachlich'
       WHEN 'party' THEN 'Fachlich'
       WHEN 'product' THEN 'Fachlich'
       WHEN 'order' THEN 'Fachlich'
       WHEN 'workeffort' THEN 'Fachlich'
       WHEN 'manufacturing' THEN 'Fachlich'
       WHEN 'humanres' THEN 'Fachlich'
       WHEN 'content' THEN 'Fachlich'
       WHEN 'marketing' THEN 'Fachlich'
       WHEN 'shipment' THEN 'Fachlich'
       WHEN 'sfa' THEN 'Fachlich'
       WHEN 'webtools' THEN 'Fachlich'
       ELSE 'Framework'
     END as typ
WITH typ, module, 
     count(DISTINCT c) as klassenAnzahl,
     count(DISTINCT m) as methodenAnzahl,
     sum(COALESCE(m.effectiveLineCount, 0)) as codezeilen
RETURN typ as Typ,
       module as Modul,
       klassenAnzahl as AnzahlKlassen,
       methodenAnzahl as AnzahlMethoden,
       codezeilen as Codezeilen
ORDER BY typ DESC, klassenAnzahl DESC
```

### Query 5: Zusammenfassung nach Typ

```cypher
// Zusammenfassung nach Typ
MATCH (c:Type:Class)
WHERE c.fqn STARTS WITH 'org.apache.ofbiz.'
WITH split(c.fqn, '.')[3] as module, c
OPTIONAL MATCH (c)-[:DECLARES]->(m:Method)
WITH module, c, m,
     CASE module
       WHEN 'accounting' THEN 'Fachlich'
       WHEN 'party' THEN 'Fachlich'
       WHEN 'product' THEN 'Fachlich'
       WHEN 'order' THEN 'Fachlich'
       WHEN 'workeffort' THEN 'Fachlich'
       WHEN 'manufacturing' THEN 'Fachlich'
       WHEN 'humanres' THEN 'Fachlich'
       WHEN 'content' THEN 'Fachlich'
       WHEN 'marketing' THEN 'Fachlich'
       WHEN 'shipment' THEN 'Fachlich'
       WHEN 'sfa' THEN 'Fachlich'
       WHEN 'webtools' THEN 'Fachlich'
       ELSE 'Framework'
     END as typ
WITH typ,
     count(DISTINCT c) as klassenAnzahl,
     count(DISTINCT m) as methodenAnzahl,
     sum(COALESCE(m.effectiveLineCount, 0)) as codezeilen
RETURN typ as Typ,
       klassenAnzahl as AnzahlKlassen,
       methodenAnzahl as AnzahlMethoden,
       codezeilen as Codezeilen
ORDER BY typ DESC
```

### Query 6: Manufacturing-Klassen

```cypher
// Finde alle Klassen im Manufacturing-Modul
MATCH (c:Type:Class)
WHERE c.fqn STARTS WITH 'org.apache.ofbiz.manufacturing'
RETURN c.fqn as Klasse, c.name as Name
ORDER BY c.fqn
LIMIT 20
```

---

## Analysemethodik

### Datenquellen

1. **Neo4j-Datenbank** mit jqAssistant-Analyse
   - Java-Code-Struktur
   - Klassen, Methoden, Abhängigkeiten
   - Code-Metriken (LOC, Komplexität)

2. **XML-Dateien** (Service-Definitionen, Entity-Modelle)
   - Entity-Namen aus `entity-name` Attributen
   - Service-Definitionen
   - Widget-Konfigurationen

3. **Minilang-Dateien**
   - Entity-Zugriffe
   - Service-Aufrufe

### Einschränkungen

- **LOC-Daten**: Basieren auf `effectiveLineCount` der Methoden, nicht der Klassen
- **Entity-Definitionen**: Nicht alle Entities sind im Manufacturing-Modul definiert, viele werden aus anderen Modulen verwendet
- **View-Entities**: Wurden teilweise manuell aus XML-Dateien extrahiert
- **Groovy-Code**: Wurde in der Analyse berücksichtigt (z.B. `ProductionRunActualComponents$_run_closure1`)

---

## Fazit

Die Analyse zeigt:

1. **Ausgewogene Architektur**: Framework und fachliche Module sind etwa gleich groß (1.442 vs. 1.376 Klassen)

2. **Komplexe fachliche Logik**: Fachliche Module haben mehr Code pro Klasse (83 vs. 55 LOC)

3. **Starke Integration**: Manufacturing-Modul nutzt ~50 Entities aus verschiedenen Modulen

4. **Monolithische Struktur**: Enge Kopplung zwischen Modulen erschwert Microservices-Migration

5. **Modernisierungspotenzial**: Kleine Module (humanres, sfa) könnten konsolidiert werden

Diese Erkenntnisse sind wertvoll für:
- Refactoring-Entscheidungen
- Microservices-Extraktion
- Architektur-Modernisierung
- Technische Schulden-Analyse

---

**Erstellt mit:** Neo4j MCP Server, jqAssistant, Cypher-Queries  
**Analysiert von:** OFBiz-Q&A Mode  
**Dokumentiert am:** 21. Januar 2026
