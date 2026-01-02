# MCP Integration für RooCode

## 📋 Überblick

Die embedded Neo4j-Datenbank von jqAssistant ist über einen MCP-Server (Model Context Protocol) direkt in RooCode integriert. Dies ermöglicht es, Cypher-Queries und Code-Analysen direkt aus RooCode auszuführen.

## ✅ Status

- **MCP-Server:** ✓ Konfiguriert
- **Neo4j-Verbindung:** ✓ Funktionsfähig (Port 7687)
- **Datenbank:** ✓ 796.749 Nodes, 15.804 Klassen
- **RooCode-Integration:** ✓ Bereit

## 🚀 Setup

### 1. MCP-Server konfigurieren

```bash
./tools/jqassistant-commandline-neo4jv5-2.8.0/macos/setup_mcp.sh
```

Dies erstellt die Konfigurationsdatei:
```
~/.config/roocode/mcp.json
```

### 2. jqAssistant-Server starten

```bash
./tools/jqassistant-commandline-neo4jv5-2.8.0/macos/start_jqa_db.sh
```

### 3. RooCode neu starten

Nach dem Neustart ist der MCP-Server verfügbar.

## 🔌 Verwendung in RooCode

### Syntax

```
@jqassistant-neo4j <method> <parameters>
```

### Verfügbare Methoden

#### 1. `execute_cypher` - Beliebige Cypher-Queries

```
@jqassistant-neo4j execute_cypher "MATCH (c:Class) RETURN c.fqn LIMIT 10"
```

#### 2. `get_statistics` - Datenbank-Statistiken

```
@jqassistant-neo4j get_statistics
```

**Ergebnis:**
```json
{
  "totalNodes": 796749,
  "classes": 15804,
  "methods": 197676,
  "fields": 57294,
  "packages": 1971
}
```

#### 3. `search_classes` - Klassen suchen

```
@jqassistant-neo4j search_classes "ofbiz"
```

#### 4. `get_class_info` - Klassen-Details

```
@jqassistant-neo4j get_class_info "org.apache.ofbiz.accounting.AccountingException"
```

**Ergebnis:**
```json
{
  "class": { "fqn": "...", "name": "...", ... },
  "methods": [ ... ],
  "fields": [ ... ],
  "parent": { ... },
  "interfaces": [ ... ]
}
```

#### 5. `find_dependencies` - Abhängigkeiten finden

```
@jqassistant-neo4j find_dependencies "org.apache.ofbiz.SomeClass"
```

#### 6. `find_usages` - Verwendungen finden

```
@jqassistant-neo4j find_usages "org.apache.ofbiz.SomeClass"
```

#### 7. `find_circular_dependencies` - Zirkuläre Abhängigkeiten

```
@jqassistant-neo4j find_circular_dependencies
```

#### 8. `get_package_structure` - Paketstruktur

```
@jqassistant-neo4j get_package_structure "org.apache"
```

#### 9. `get_method_calls` - Methoden-Aufrufe

```
@jqassistant-neo4j get_method_calls "org.apache.ofbiz.SomeClass.someMethod()"
```

#### 10. `get_schema` - Datenbank-Schema

```
@jqassistant-neo4j get_schema
```

#### 11. `list_labels` - Verfügbare Labels

```
@jqassistant-neo4j list_labels
```

#### 12. `health_check` - Verbindungs-Status

```
@jqassistant-neo4j health_check
```

## 📊 Beispiel-Queries

### Top 10 Klassen mit den meisten Methoden

```
@jqassistant-neo4j execute_cypher "MATCH (c:Class)-[:DECLARES]->(m:Method) RETURN c.fqn, COUNT(m) as MethodCount ORDER BY MethodCount DESC LIMIT 10"
```

### Abhängigkeiten zwischen Packages

```
@jqassistant-neo4j execute_cypher "MATCH (p1:Package)-[:CONTAINS]->(c1:Class)-[:DEPENDS_ON]->(c2:Class)<-[:CONTAINS]-(p2:Package) WHERE p1 <> p2 RETURN DISTINCT p1.fqn, p2.fqn LIMIT 20"
```

### Klassen ohne Abhängigkeiten

```
@jqassistant-neo4j execute_cypher "MATCH (c:Class) WHERE NOT (c)-[:DEPENDS_ON]->() RETURN c.fqn LIMIT 20"
```

### Größte Klassen (nach Methoden)

```
@jqassistant-neo4j execute_cypher "MATCH (c:Class)-[:DECLARES]->(m:Method) WITH c, COUNT(m) as mc WHERE mc > 50 RETURN c.fqn, mc ORDER BY mc DESC"
```

## 🔧 Konfiguration

Die MCP-Konfiguration befindet sich in:
```
~/.config/roocode/mcp.json
```

**Inhalt:**
```json
{
  "mcpServers": {
    "jqassistant-neo4j": {
      "command": "python3",
      "args": [
        "/Users/oliverwidder/dev/ofbiz-framework/neo4j_mcp_server.py"
      ],
      "env": {
        "NEO4J_URI": "bolt://localhost:7687",
        "NEO4J_USER": "neo4j",
        "NEO4J_PASSWORD": "neo4j",
        "NEO4J_DATABASE": "neo4j"
      }
    }
  }
}
```

## 🐛 Troubleshooting

### Problem: "Connection refused"

**Lösung:** Stelle sicher, dass der jqAssistant-Server läuft:
```bash
./tools/jqassistant-commandline-neo4jv5-2.8.0/macos/status_jqa_db.sh
./tools/jqassistant-commandline-neo4jv5-2.8.0/macos/start_jqa_db.sh
```

### Problem: "MCP Server not found in RooCode"

**Lösung:** 
1. Überprüfe die Konfigurationsdatei: `~/.config/roocode/mcp.json`
2. Starte RooCode neu
3. Überprüfe die RooCode-Logs

### Problem: "Python module not found"

**Lösung:** Installiere die neo4j Python-Bibliothek:
```bash
pip3 install neo4j
```

## 📚 Weitere Ressourcen

- [`neo4j_mcp_server.py`](../../neo4j_mcp_server.py) - MCP-Server-Implementierung
- [`README.md`](../../README.md) - Hauptdokumentation
- [`HOWTO_ACCESS_DATABASE.md`](../../HOWTO_ACCESS_DATABASE.md) - Cypher-Query Beispiele
- [Neo4j Cypher Manual](https://neo4j.com/docs/cypher-manual/)

## ✨ Zusammenfassung

Die MCP-Integration ermöglicht:
- ✅ Direkte Cypher-Queries aus RooCode
- ✅ Code-Analyse und Abhängigkeitsprüfung
- ✅ Architektur-Visualisierung
- ✅ Qualitätsmetriken
- ✅ Schnelle Prototypisierung von Analysen
