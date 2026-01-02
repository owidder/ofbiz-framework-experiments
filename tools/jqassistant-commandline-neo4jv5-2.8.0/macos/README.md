# jqAssistant macOS Management Scripts

Management-Skripte für die jqAssistant Neo4j-Datenbank auf macOS mit MCP-Integration für RooCode.

## 🚀 Verfügbare Skripte

### `start_jqa_db.sh`
Startet den jqAssistant Server mit eingebetteter Neo4j-Datenbank im Hintergrund.

```bash
./start_jqa_db.sh
```

**Funktionsweise:**
- Erstellt Named Pipe für stdin-Verwaltung
- Startet Server mit nohup im Hintergrund
- Speichert PID in `/tmp/jqassistant-server.pid`
- Server läuft zuverlässig im Hintergrund

### `stop_jqa_db.sh`
Stoppt den laufenden jqAssistant Server.

```bash
./stop_jqa_db.sh
```

### `status_jqa_db.sh`
Prüft den Status des Servers und die Datenbankverbindung.

```bash
./status_jqa_db.sh
```

### `clear_jqa_db.sh`
Löscht alle Daten aus der Datenbank (Server muss laufen).

```bash
./clear_jqa_db.sh
```

### `refill_jqa_db.sh`
Füllt die Datenbank neu mit Class-Files aus `build/classes/java/main`.

```bash
./refill_jqa_db.sh
```

### `list_jqa_data.sh`
Listet die Inhalte der Datenbank auf.

```bash
./list_jqa_data.sh
```

## 🔌 Verbindungsinformationen

- **Browser:** http://localhost:7474
- **Bolt:** bolt://localhost:7687
- **Username:** neo4j
- **Password:** neo4j

## 📊 Datenbank-Inhalt

- **Klassen:** ~2.000+
- **Methoden:** ~50.000+
- **Nodes:** ~796.000
- **Größe:** 359 MB

## 📝 Verwendungsbeispiel

```bash
# Server starten
./start_jqa_db.sh

# Status prüfen
./status_jqa_db.sh

# Browser öffnen
open http://localhost:7474

# Server stoppen
./stop_jqa_db.sh
```

## 🛠️ Technische Details

- **jqAssistant:** 2.8.0
- **Neo4j:** 5.26.5 (embedded)
- **Java:** Erforderlich
- **Betriebssystem:** macOS

## 🔌 MCP-Integration für RooCode

### Setup

```bash
./setup_mcp.sh
```

Dies konfiguriert den MCP-Server für RooCode-Integration.

### Verwendung in RooCode

Nach dem Setup und Neustart von RooCode:

```
@jqassistant-neo4j execute_cypher "MATCH (c:Class) RETURN c.fqn LIMIT 10"
@jqassistant-neo4j get_statistics
@jqassistant-neo4j search_classes "ofbiz"
```

Siehe auch: [`MCP_INTEGRATION.md`](MCP_INTEGRATION.md) - Detaillierte MCP-Dokumentation

## 📚 Weitere Dokumentation

- [`MCP_INTEGRATION.md`](MCP_INTEGRATION.md) - RooCode MCP-Integration
- [`DATABASE_MANAGEMENT.md`](../../DATABASE_MANAGEMENT.md) - Detaillierte Anleitung
- [`HOWTO_ACCESS_DATABASE.md`](../../HOWTO_ACCESS_DATABASE.md) - Cypher-Query Beispiele
