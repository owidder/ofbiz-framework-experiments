# Neo4j Analyse: Party Service Abhängigkeiten - Ausführungsanleitung

## 🎯 Ziel

Präzise Aufstellung der Anzahl der Party Service Aufrufe aus der OFBiz-Codebase mittels Neo4j.

---

## 📋 Schritt-für-Schritt Anleitung

### Schritt 1: Neo4j Browser öffnen

```
http://localhost:7474/browser/
```

oder über Neo4j Desktop:
- Neo4j Desktop öffnen
- Datenbank starten
- "Open" klicken

---

### Schritt 2: Query 1 ausführen - Modul-Übersicht

Kopiere diese Query in den Neo4j Browser:

```cypher
MATCH (caller)-[call:CALLS]->(target)
WHERE target.package CONTAINS 'party'
WITH 
  CASE 
    WHEN caller.package CONTAINS 'order' THEN 'Order'
    WHEN caller.package CONTAINS 'product' THEN 'Product'
    WHEN caller.package CONTAINS 'accounting' THEN 'Accounting'
    WHEN caller.package CONTAINS 'humanres' THEN 'HumanRes'
    WHEN caller.package CONTAINS 'marketing' THEN 'Marketing'
    WHEN caller.package CONTAINS 'workeffort' THEN 'WorkEffort'
    WHEN caller.package CONTAINS 'content' THEN 'Content'
    WHEN caller.package CONTAINS 'manufacturing' THEN 'Manufacturing'
    WHEN caller.package CONTAINS 'shipment' THEN 'Shipment'
    ELSE 'Other'
  END as Module,
  count(*) as CallCount
RETURN Module, CallCount
ORDER BY CallCount DESC;
```

**Erwartete Ausgabe:**
```
Module          | CallCount
----------------|----------
HumanRes        | ???
Order           | ???
Product         | ???
Marketing       | ???
Accounting      | ???
WorkEffort      | ???
Content         | ???
Shipment        | ???
Manufacturing   | ???
```

---

### Schritt 3: Query 2 ausführen - Komplexität pro Modul

```cypher
MATCH (caller)-[call:CALLS]->(target)
WHERE target.package CONTAINS 'party'
WITH 
  CASE 
    WHEN caller.package CONTAINS 'order' THEN 'Order'
    WHEN caller.package CONTAINS 'product' THEN 'Product'
    WHEN caller.package CONTAINS 'accounting' THEN 'Accounting'
    WHEN caller.package CONTAINS 'humanres' THEN 'HumanRes'
    WHEN caller.package CONTAINS 'marketing' THEN 'Marketing'
    WHEN caller.package CONTAINS 'workeffort' THEN 'WorkEffort'
    WHEN caller.package CONTAINS 'content' THEN 'Content'
    WHEN caller.package CONTAINS 'manufacturing' THEN 'Manufacturing'
    WHEN caller.package CONTAINS 'shipment' THEN 'Shipment'
    ELSE 'Other'
  END as Module,
  count(DISTINCT caller.name) as UniqueCallers,
  count(*) as TotalCalls
RETURN Module, UniqueCallers, TotalCalls, 
       ROUND(TOFLOAT(TotalCalls) / UniqueCallers, 2) as AvgCallsPerMethod
ORDER BY TotalCalls DESC;
```

**Erwartete Ausgabe:**
```
Module          | UniqueCallers | TotalCalls | AvgCallsPerMethod
----------------|---------------|------------|------------------
HumanRes        | ???           | ???        | ???
Order           | ???           | ???        | ???
Product         | ???           | ???        | ???
Marketing       | ???           | ???        | ???
Accounting      | ???           | ???        | ???
WorkEffort      | ???           | ???        | ???
Content         | ???           | ???        | ???
Shipment        | ???           | ???        | ???
Manufacturing   | ???           | ???        | ???
```

---

### Schritt 4: Query 3 ausführen - Top 20 kritische Methoden

```cypher
MATCH (method)-[call:CALLS]->(target)
WHERE target.package CONTAINS 'party'
WITH target.name as TargetMethod, count(*) as IncomingCalls
RETURN TargetMethod, IncomingCalls
ORDER BY IncomingCalls DESC
LIMIT 20;
```

**Zweck:** Zeigt die 20 am häufigsten aufgerufenen Party Service Methoden

---

### Schritt 5: Query 4 ausführen - Dateien mit Party Aufrufen

```cypher
MATCH (caller)-[call:CALLS]->(target)
WHERE target.package CONTAINS 'party'
RETURN 
  call.file as File,
  count(*) as CallCount,
  collect(DISTINCT caller.name) as Methods
ORDER BY CallCount DESC
LIMIT 30;
```

**Zweck:** Zeigt welche Dateien Party Service aufrufen (Top 30)

---

### Schritt 6: Query 5 ausführen - Aufrufe nach Call-Typ

```cypher
MATCH (caller)-[call:CALLS]->(target)
WHERE target.package CONTAINS 'party'
RETURN 
  call.type as CallType,
  count(*) as Count
ORDER BY Count DESC;
```

**Zweck:** Zeigt Verteilung der Call-Typen (REST, Service, Direct, etc.)

---

## 📊 Ergebnisse exportieren

### Schritt 1: Ergebnisse markieren
- Rechtsklick auf die Ergebnistabelle
- "Export" wählen

### Schritt 2: Format wählen
- CSV (für Excel/Sheets)
- JSON (für weitere Verarbeitung)

### Schritt 3: Datei speichern
- Speichern unter `/plans/neo4j-results/`

---

## 📈 Ergebnisse interpretieren

### Kritikalität bestimmen

**KRITISCH:** 
- CallCount > 40
- UniqueCallers > 20

**HOCH:**
- CallCount 30-40
- UniqueCallers 15-20

**MITTEL:**
- CallCount 20-30
- UniqueCallers 10-15

**NIEDRIG:**
- CallCount < 20
- UniqueCallers < 10

---

## 🎯 Aufwandsschätzung basierend auf Ergebnissen

### Formel:

```
Aufwand (Wochen) = (UniqueCallers × 0.5) + (CallCount × 0.1) + 2
```

### Beispiel:
- HumanRes: UniqueCallers=30, CallCount=60
- Aufwand = (30 × 0.5) + (60 × 0.1) + 2 = 15 + 6 + 2 = 23 Wochen ≈ 6 Monate

---

## 📝 Vorlage für Ergebnisbericht

```markdown
# Neo4j Analyse Ergebnisse - Party Service Abhängigkeiten

## Modul-Übersicht

| Module | CallCount | UniqueCallers | AvgCallsPerMethod | Kritikalität |
|--------|-----------|---------------|-------------------|--------------|
| HumanRes | [RESULT] | [RESULT] | [RESULT] | [BERECHNET] |
| Order | [RESULT] | [RESULT] | [RESULT] | [BERECHNET] |
| Product | [RESULT] | [RESULT] | [RESULT] | [BERECHNET] |
| Marketing | [RESULT] | [RESULT] | [RESULT] | [BERECHNET] |
| Accounting | [RESULT] | [RESULT] | [RESULT] | [BERECHNET] |
| WorkEffort | [RESULT] | [RESULT] | [RESULT] | [BERECHNET] |
| Content | [RESULT] | [RESULT] | [RESULT] | [BERECHNET] |
| Shipment | [RESULT] | [RESULT] | [RESULT] | [BERECHNET] |
| Manufacturing | [RESULT] | [RESULT] | [RESULT] | [BERECHNET] |
| **GESAMT** | **[SUMME]** | **[SUMME]** | **[DURCHSCHNITT]** | - |

## Top 20 kritische Methoden

| TargetMethod | IncomingCalls |
|--------------|---------------|
| [RESULT] | [RESULT] |
| ... | ... |

## Call-Typ Verteilung

| CallType | Count |
|----------|-------|
| [RESULT] | [RESULT] |
| ... | ... |

## Aufwandsschätzung pro Modul

| Module | Aufwand (Wochen) | Aufwand (Monate) |
|--------|-----------------|-----------------|
| HumanRes | [BERECHNET] | [BERECHNET] |
| Order | [BERECHNET] | [BERECHNET] |
| ... | ... | ... |
| **GESAMT** | **[SUMME]** | **[SUMME]** |

## Optimierte Migrations-Reihenfolge

1. Content Service - [AUFWAND] Wochen
2. Product Service - [AUFWAND] Wochen
3. Manufacturing Service - [AUFWAND] Wochen
4. Order Service - [AUFWAND] Wochen
5. Accounting Service - [AUFWAND] Wochen
6. Marketing Service - [AUFWAND] Wochen
7. WorkEffort Service - [AUFWAND] Wochen
8. HumanRes Service - [AUFWAND] Wochen
9. Party Service - [AUFWAND] Wochen

**GESAMTAUFWAND: [SUMME] Wochen = [MONATE] Monate**
```

---

## 🔍 Zusätzliche Queries (Optional)

### Query 6: Entity-Beziehungen

```cypher
MATCH (entity)-[rel:REFERENCES]->(party)
WHERE party.name = 'Party' OR party.package CONTAINS 'party'
RETURN 
  entity.name as Entity,
  entity.package as Package,
  rel.type as RelationType,
  count(*) as ReferenceCount
ORDER BY ReferenceCount DESC
LIMIT 30;
```

### Query 7: Transitive Abhängigkeiten

```cypher
MATCH path = (service)-[*1..3]->(party)
WHERE party.package CONTAINS 'party'
  AND service.package NOT CONTAINS 'party'
WITH 
  CASE 
    WHEN service.package CONTAINS 'order' THEN 'Order'
    WHEN service.package CONTAINS 'product' THEN 'Product'
    WHEN service.package CONTAINS 'accounting' THEN 'Accounting'
    WHEN service.package CONTAINS 'humanres' THEN 'HumanRes'
    WHEN service.package CONTAINS 'marketing' THEN 'Marketing'
    WHEN service.package CONTAINS 'workeffort' THEN 'WorkEffort'
    WHEN service.package CONTAINS 'content' THEN 'Content'
    WHEN service.package CONTAINS 'manufacturing' THEN 'Manufacturing'
    WHEN service.package CONTAINS 'shipment' THEN 'Shipment'
    ELSE 'Other'
  END as Module,
  length(path) as PathLength,
  count(*) as PathCount
RETURN Module, PathLength, PathCount
ORDER BY Module, PathLength;
```

---

## ✅ Checkliste

- [ ] Neo4j Browser geöffnet
- [ ] Query 1 ausgeführt und Ergebnisse notiert
- [ ] Query 2 ausgeführt und Ergebnisse notiert
- [ ] Query 3 ausgeführt und Ergebnisse notiert
- [ ] Query 4 ausgeführt und Ergebnisse notiert
- [ ] Query 5 ausgeführt und Ergebnisse notiert
- [ ] Ergebnisse als CSV exportiert
- [ ] Ergebnisbericht ausgefüllt
- [ ] Aufwandsschätzung berechnet
- [ ] Migrations-Reihenfolge angepasst

---

## 📞 Support

Falls Queries nicht funktionieren:

1. **Syntax-Fehler:** Überprüfe die Klammern und Kommas
2. **Keine Ergebnisse:** Überprüfe ob die Daten in Neo4j importiert sind
3. **Timeout:** Query ist zu komplex, versuche mit LIMIT zu begrenzen
4. **Verbindungsfehler:** Neo4j läuft nicht, starte die Datenbank neu

---

## 🎯 Nächste Schritte nach der Analyse

1. **Ergebnisse mit Schätzungen vergleichen**
   - Sind die Ergebnisse höher oder niedriger als erwartet?
   - Welche Module sind kritischer als gedacht?

2. **Migrations-Reihenfolge anpassen**
   - Basierend auf echten Daten
   - Nicht auf Schätzungen

3. **Aufwandsschätzung verfeinern**
   - Mit echten Daten
   - Für Stakeholder-Präsentation

4. **Risiko-Analyse durchführen**
   - Welche Module sind am riskantesten?
   - Welche brauchen mehr Testing?

5. **Adapter Layer planen**
   - Für Party Service
   - Für andere zentrale Services
