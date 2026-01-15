# Neo4j Setup für jqAssistant

## Voraussetzungen

Sie benötigen einen laufenden Neo4j-Server (Version 5.x), um jqAssistant zu verwenden.

## Neo4j Installation (via Homebrew)

Falls noch nicht installiert:

```bash
brew install neo4j
```

## Neo4j starten

### Option 1: Als Service (empfohlen)

```bash
# Neo4j als Service starten (läuft im Hintergrund)
brew services start neo4j

# Status prüfen
brew services list | grep neo4j
```

### Option 2: Manuell

```bash
# Neo4j manuell starten
neo4j start

# Status prüfen
neo4j status

# Stoppen
neo4j stop
```

## Neo4j Verbindungsinformationen

- **Bolt-Protokoll:** `bolt://localhost:7687`
- **HTTP-Browser:** `http://localhost:7474`
- **Standard-Benutzername:** `neo4j`
- **Standard-Passwort:** `neo4j` (beim ersten Login ändern)

## Erstes Login

1. Öffnen Sie den Browser: `http://localhost:7474`
2. Melden Sie sich mit `neo4j` / `neo4j` an
3. Sie werden aufgefordert, das Passwort zu ändern
4. Merken Sie sich das neue Passwort!

## jqAssistant mit Neo4j verwenden

Nachdem Neo4j läuft:

```bash
cd /Users/oliverwidder/dev/ofbiz/tools/jqassistant-commandline-neo4jv5-2.8.0/macos

# Mit Standard-Passwort (neo4j)
./start_jqa_db.sh /Users/oliverwidder/dev/ofbiz

# Mit eigenem Passwort (als Parameter)
./start_jqa_db.sh /Users/oliverwidder/dev/ofbiz IhrPasswort

# Mit eigenem Passwort (als Umgebungsvariable)
NEO4J_PASSWORD=IhrPasswort ./start_jqa_db.sh /Users/oliverwidder/dev/ofbiz
```

Das Skript prüft automatisch, ob Neo4j läuft, und startet dann den jqAssistant-Server mit den angegebenen Zugangsdaten.

## Fehlerbehebung

### "Connection refused: localhost/127.0.0.1:7687"

**Problem:** Neo4j läuft nicht.

**Lösung:**
```bash
# Prüfen, ob Neo4j läuft
lsof -i :7687

# Falls nicht, starten Sie Neo4j
brew services start neo4j

# Warten Sie ~10 Sekunden, dann erneut prüfen
lsof -i :7687
```

### Neo4j läuft, aber jqAssistant kann sich nicht verbinden

**Problem:** Falsches Passwort oder Authentifizierung fehlgeschlagen.

**Lösung:** Übergeben Sie das Passwort beim Start:

```bash
# Als Parameter
./start_jqa_db.sh /Users/oliverwidder/dev/ofbiz IhrPasswort

# Als Umgebungsvariable
NEO4J_PASSWORD=IhrPasswort ./start_jqa_db.sh /Users/oliverwidder/dev/ofbiz
```

**Hinweis:** Das Skript erstellt automatisch eine temporäre Konfigurationsdatei mit den Zugangsdaten. Diese wird beim Stoppen des Servers automatisch gelöscht.

### Port 7687 bereits belegt

**Problem:** Ein anderer Prozess verwendet Port 7687.

**Lösung:**
```bash
# Prozess finden
lsof -i :7687

# Neo4j-Prozess beenden
brew services stop neo4j

# Oder manuell
neo4j stop

# Neu starten
brew services start neo4j
```

## Neo4j stoppen

```bash
# Als Service
brew services stop neo4j

# Oder manuell
neo4j stop
```

## Nützliche Befehle

```bash
# Neo4j-Version prüfen
neo4j version

# Neo4j-Konfiguration anzeigen
neo4j console

# Neo4j-Logs anzeigen
tail -f /opt/homebrew/var/log/neo4j/neo4j.log
```

## Weitere Informationen

- [Neo4j Documentation](https://neo4j.com/docs/)
- [jqAssistant Documentation](https://jqassistant.org/get-started/)
