# Neo4j Analyse: Party Service Abhängigkeiten

## Übersicht

Dieses Dokument enthält Cypher-Queries zur präzisen Analyse der Party Service Abhängigkeiten in der OFBiz-Codebase mittels Neo4j.

---

## Query 1: Alle Aufrufe zum Party Service

```cypher
MATCH (caller)-[call:CALLS]->(target)
WHERE target.name CONTAINS 'Party' OR target.package CONTAINS 'party'
RETURN 
  caller.name as CallerService,
  caller.package as CallerPackage,
  target.name as TargetService,
  target.package as TargetPackage,
  call.type as CallType,
  count(*) as CallCount
ORDER BY CallCount DESC;
```

**Zweck:** Zeigt alle direkten Aufrufe zum Party Service mit Häufigkeit

---

## Query 2: Party Service Abhängigkeiten nach Modul

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

**Zweck:** Aggregiert Party Service Aufrufe nach Modul

**Erwartete Ausgabe:**
```
Module          | CallCount
----------------|----------
HumanRes        | 60+
Order           | 50+
Product         | 45+
Marketing       | 40+
Accounting      | 35+
WorkEffort      | 35+
Content         | 30+
Shipment        | 30+
Manufacturing   | 25+
```

---

## Query 3: Detaillierte Aufrufe pro Service

```cypher
MATCH (caller)-[call:CALLS]->(target)
WHERE target.package CONTAINS 'party'
RETURN 
  caller.name as CallerMethod,
  target.name as TargetMethod,
  call.type as CallType,
  call.lineNumber as LineNumber,
  call.file as File
ORDER BY caller.name, target.name;
```

**Zweck:** Zeigt detaillierte Informationen zu jedem Aufruf (Datei, Zeile, Typ)

---

## Query 4: Entity-Beziehungen zu Party

```cypher
MATCH (entity)-[rel:REFERENCES]->(party)
WHERE party.name = 'Party' OR party.package CONTAINS 'party'
RETURN 
  entity.name as Entity,
  entity.package as Package,
  rel.type as RelationType,
  count(*) as ReferenceCount
ORDER BY ReferenceCount DESC;
```

**Zweck:** Zeigt welche Entities auf Party-Entities referenzieren

---

## Query 5: Service-zu-Service Abhängigkeiten über Party

```cypher
MATCH (service1)-[call1:CALLS]->(party),
      (service2)-[call2:CALLS]->(party)
WHERE party.package CONTAINS 'party'
  AND service1 <> service2
  AND service1.package <> service2.package
WITH 
  CASE 
    WHEN service1.package CONTAINS 'order' THEN 'Order'
    WHEN service1.package CONTAINS 'product' THEN 'Product'
    WHEN service1.package CONTAINS 'accounting' THEN 'Accounting'
    WHEN service1.package CONTAINS 'humanres' THEN 'HumanRes'
    WHEN service1.package CONTAINS 'marketing' THEN 'Marketing'
    WHEN service1.package CONTAINS 'workeffort' THEN 'WorkEffort'
    WHEN service1.package CONTAINS 'content' THEN 'Content'
    WHEN service1.package CONTAINS 'manufacturing' THEN 'Manufacturing'
    WHEN service1.package CONTAINS 'shipment' THEN 'Shipment'
    ELSE 'Other'
  END as Service1,
  CASE 
    WHEN service2.package CONTAINS 'order' THEN 'Order'
    WHEN service2.package CONTAINS 'product' THEN 'Product'
    WHEN service2.package CONTAINS 'accounting' THEN 'Accounting'
    WHEN service2.package CONTAINS 'humanres' THEN 'HumanRes'
    WHEN service2.package CONTAINS 'marketing' THEN 'Marketing'
    WHEN service2.package CONTAINS 'workeffort' THEN 'WorkEffort'
    WHEN service2.package CONTAINS 'content' THEN 'Content'
    WHEN service2.package CONTAINS 'manufacturing' THEN 'Manufacturing'
    WHEN service2.package CONTAINS 'shipment' THEN 'Shipment'
    ELSE 'Other'
  END as Service2,
  count(*) as SharedPartyDependencies
RETURN Service1, Service2, SharedPartyDependencies
ORDER BY SharedPartyDependencies DESC;
```

**Zweck:** Zeigt welche Services gemeinsam auf Party Service zugreifen

---

## Query 6: Kritische Party Service Methoden

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

## Query 7: Komplexität der Party Service Migration

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

**Zweck:** Zeigt Komplexität pro Modul (Anzahl eindeutiger Aufrufer, Gesamtaufrufe, Durchschnitt)

---

## Query 8: Transitive Abhängigkeiten

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

**Zweck:** Zeigt indirekte Abhängigkeiten (über mehrere Hops)

---

## Query 9: Dateien mit Party Service Aufrufen

```cypher
MATCH (caller)-[call:CALLS]->(target)
WHERE target.package CONTAINS 'party'
RETURN 
  call.file as File,
  count(*) as CallCount,
  collect(DISTINCT caller.name) as Methods
ORDER BY CallCount DESC;
```

**Zweck:** Zeigt welche Dateien Party Service aufrufen

---

## Query 10: Aufrufe nach Call-Typ

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

## Anleitung zur Ausführung

### Schritt 1: Neo4j Browser öffnen
```
http://localhost:7474/browser/
```

### Schritt 2: Query kopieren und ausführen
- Eine der obigen Queries kopieren
- In Neo4j Browser einfügen
- Enter drücken

### Schritt 3: Ergebnisse exportieren
- Rechtsklick auf Ergebnisse
- "Export as CSV" oder "Export as JSON"

---

## Erwartete Ergebnisse

### Modul-Übersicht (Query 2)
```
Module          | CallCount | Komplexität
----------------|-----------|------------
HumanRes        | 60+       | KRITISCH
Order           | 50+       | KRITISCH
Product         | 45+       | KRITISCH
Marketing       | 40+       | HOCH
Accounting      | 35+       | KRITISCH
WorkEffort      | 35+       | HOCH
Content         | 30+       | MITTEL
Shipment        | 30+       | HOCH
Manufacturing   | 25+       | HOCH
```

### Komplexität pro Modul (Query 7)
```
Module          | UniqueCallers | TotalCalls | AvgCallsPerMethod
----------------|---------------|------------|------------------
HumanRes        | 25-30         | 60+        | 2.0-2.4
Order           | 20-25         | 50+        | 2.0-2.5
Product         | 18-22         | 45+        | 2.0-2.5
Marketing       | 16-20         | 40+        | 2.0-2.5
Accounting      | 14-18         | 35+        | 1.9-2.5
WorkEffort      | 14-18         | 35+        | 1.9-2.5
Content         | 12-16         | 30+        | 1.9-2.5
Shipment        | 12-15         | 30+        | 2.0-2.5
Manufacturing   | 10-12         | 25+        | 2.1-2.5
```

---

## Interpretation der Ergebnisse

### Kritikalität bestimmen
- **KRITISCH:** > 40 Aufrufe, > 20 eindeutige Aufrufer
- **HOCH:** 30-40 Aufrufe, 15-20 eindeutige Aufrufer
- **MITTEL:** 20-30 Aufrufe, 10-15 eindeutige Aufrufer
- **NIEDRIG:** < 20 Aufrufe, < 10 eindeutige Aufrufer

### Komplexität bestimmen
- **Hoch:** AvgCallsPerMethod > 2.3
- **Mittel:** AvgCallsPerMethod 1.9-2.3
- **Niedrig:** AvgCallsPerMethod < 1.9

---

## Nächste Schritte

1. **Queries ausführen** und Ergebnisse sammeln
2. **Ergebnisse analysieren** und mit Schätzungen vergleichen
3. **Abhängigkeitsgraph** visualisieren
4. **Migrations-Reihenfolge** basierend auf Daten anpassen
5. **Aufwandsschätzung** verfeinern

---

## Hinweise

- Diese Queries setzen voraus, dass die OFBiz-Codebase in Neo4j importiert wurde
- Die Queries können je nach Neo4j-Version angepasst werden müssen
- Für große Codebases können die Queries lange dauern
- Ergebnisse sollten in CSV exportiert und analysiert werden
