# Neo4j Analyse: Party Service Abhängigkeiten - Echte Daten

## 🎯 Ziel

Präzise Aufstellung der Anzahl der Party Service Aufrufe aus der OFBiz-Codebase mittels Neo4j MCP Server.

---

## 📋 Queries zur Ausführung

Die folgenden Queries können über den Neo4j MCP Server ausgeführt werden:

### Query 1: Party Service Abhängigkeiten nach Modul

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

---

### Query 2: Komplexität pro Modul

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

---

### Query 3: Top 20 kritische Party Service Methoden

```cypher
MATCH (method)-[call:CALLS]->(target)
WHERE target.package CONTAINS 'party'
WITH target.name as TargetMethod, count(*) as IncomingCalls
RETURN TargetMethod, IncomingCalls
ORDER BY IncomingCalls DESC
LIMIT 20;
```

---

### Query 4: Dateien mit Party Service Aufrufen

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

---

### Query 5: Aufrufe nach Call-Typ

```cypher
MATCH (caller)-[call:CALLS]->(target)
WHERE target.package CONTAINS 'party'
RETURN 
  call.type as CallType,
  count(*) as Count
ORDER BY Count DESC;
```

---

## 🔧 Ausführung über MCP Server

### Methode 1: Direkt über Python

```python
import json
import subprocess

# Query ausführen
query = """
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
ORDER BY CallCount DESC
"""

request = {
    "jsonrpc": "2.0",
    "id": 1,
    "method": "execute_cypher",
    "params": {
        "query": query,
        "variables": {}
    }
}

# Request an MCP Server senden
print(json.dumps(request))
```

### Methode 2: Über Neo4j Browser

1. Öffne http://localhost:7474/browser/
2. Kopiere eine der Queries oben
3. Drücke Enter

---

## 📊 Erwartete Ergebnisse

### Query 1 Ergebnis (Modul-Übersicht)

```
Module          | CallCount
----------------|----------
HumanRes        | [ECHTE DATEN]
Order           | [ECHTE DATEN]
Product         | [ECHTE DATEN]
Marketing       | [ECHTE DATEN]
Accounting      | [ECHTE DATEN]
WorkEffort      | [ECHTE DATEN]
Content         | [ECHTE DATEN]
Shipment        | [ECHTE DATEN]
Manufacturing   | [ECHTE DATEN]
```

### Query 2 Ergebnis (Komplexität)

```
Module          | UniqueCallers | TotalCalls | AvgCallsPerMethod
----------------|---------------|------------|------------------
HumanRes        | [ECHTE DATEN] | [ECHTE DATEN] | [ECHTE DATEN]
Order           | [ECHTE DATEN] | [ECHTE DATEN] | [ECHTE DATEN]
Product         | [ECHTE DATEN] | [ECHTE DATEN] | [ECHTE DATEN]
Marketing       | [ECHTE DATEN] | [ECHTE DATEN] | [ECHTE DATEN]
Accounting      | [ECHTE DATEN] | [ECHTE DATEN] | [ECHTE DATEN]
WorkEffort      | [ECHTE DATEN] | [ECHTE DATEN] | [ECHTE DATEN]
Content         | [ECHTE DATEN] | [ECHTE DATEN] | [ECHTE DATEN]
Shipment        | [ECHTE DATEN] | [ECHTE DATEN] | [ECHTE DATEN]
Manufacturing   | [ECHTE DATEN] | [ECHTE DATEN] | [ECHTE DATEN]
```

---

## 🎯 Nächste Schritte

1. **Queries ausführen** über Neo4j Browser oder MCP Server
2. **Ergebnisse sammeln** und in Tabelle eintragen
3. **Aufwandsschätzung berechnen** basierend auf echten Daten
4. **Migrations-Reihenfolge anpassen** basierend auf Komplexität
5. **Bericht erstellen** mit präzisen Zahlen

---

## 📝 Vorlage für Ergebnisbericht

Nach Ausführung der Queries bitte folgende Tabelle ausfüllen:

### Modul-Übersicht (aus Query 1 & 2)

| Module | CallCount | UniqueCallers | TotalCalls | AvgCallsPerMethod | Kritikalität | Aufwand (Wochen) |
|--------|-----------|---------------|------------|-------------------|--------------|-----------------|
| HumanRes | | | | | | |
| Order | | | | | | |
| Product | | | | | | |
| Marketing | | | | | | |
| Accounting | | | | | | |
| WorkEffort | | | | | | |
| Content | | | | | | |
| Shipment | | | | | | |
| Manufacturing | | | | | | |
| **GESAMT** | | | | | | |

### Kritikalität bestimmen

- **KRITISCH:** CallCount > 40 UND UniqueCallers > 20
- **HOCH:** CallCount 30-40 UND UniqueCallers 15-20
- **MITTEL:** CallCount 20-30 UND UniqueCallers 10-15
- **NIEDRIG:** CallCount < 20 UND UniqueCallers < 10

### Aufwand berechnen

```
Aufwand (Wochen) = (UniqueCallers × 0.5) + (CallCount × 0.1) + 2
```

---

## 🔍 Zusätzliche Informationen

### Neo4j MCP Server Konfiguration

- **URI:** bolt://localhost:7687
- **Username:** neo4j
- **Password:** neo4j
- **Database:** neo4j

### Verfügbare MCP Methoden

- `execute_cypher` - Beliebige Cypher Query ausführen
- `find_circular_dependencies` - Zirkuläre Abhängigkeiten finden
- `search_classes` - Klassen suchen
- `get_statistics` - Datenbankstatistiken
- `find_dependencies` - Abhängigkeiten finden
- `find_usages` - Verwendungen finden
- `get_class_info` - Klasseninformationen
- `get_method_calls` - Methodenaufrufe
- `get_package_structure` - Paketstruktur
- `health_check` - Verbindungsprüfung

---

## ✅ Checkliste

- [ ] Neo4j MCP Server läuft
- [ ] Query 1 ausgeführt
- [ ] Query 2 ausgeführt
- [ ] Query 3 ausgeführt
- [ ] Query 4 ausgeführt
- [ ] Query 5 ausgeführt
- [ ] Ergebnisse in Tabelle eingetragen
- [ ] Kritikalität bestimmt
- [ ] Aufwand berechnet
- [ ] Migrations-Reihenfolge angepasst
