# Neo4j MCP-Anbindung - Einrichtungsanleitung

## Überblick

Diese Anleitung beschreibt, wie Sie eine Neo4j-Datenbank über das Model Context Protocol (MCP) anbinden können. Der MCP-Server ermöglicht es, direkt aus der Entwicklungsumgebung auf die Neo4j-Datenbank zuzugreifen und Cypher-Queries auszuführen.

## Voraussetzungen

- **Node.js** (Version 14 oder höher)
- **Neo4j-Datenbank** (lokal oder remote)
- **VSCode** mit Cline-Extension oder ein anderer MCP-kompatibler Client
- **OFBiz-Code** bereits in Neo4j importiert

## MCP-Server Einrichtung

### 1. MCP-Server-Verzeichnis erstellen

Erstellen Sie ein Verzeichnis für den Neo4j MCP-Server:

```bash
mkdir -p ~/Documents/Cline/MCP/neo4j-server
cd ~/Documents/Cline/MCP/neo4j-server
```

### 2. Node.js-Projekt initialisieren

```bash
npm init -y
```

### 3. Abhängigkeiten installieren

```bash
npm install @modelcontextprotocol/sdk neo4j-driver
```

### 4. MCP-Server implementieren

Erstellen Sie eine Datei [`index.js`](~/Documents/Cline/MCP/neo4j-server/index.js) mit folgendem Inhalt:

```javascript
#!/usr/bin/env node

import { Server } from '@modelcontextprotocol/sdk/server/index.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
} from '@modelcontextprotocol/sdk/types.js';
import neo4j from 'neo4j-driver';

// Neo4j-Verbindungsparameter
const NEO4J_URI = process.env.NEO4J_URI || 'bolt://localhost:7687';
const NEO4J_USER = process.env.NEO4J_USER || 'neo4j';
const NEO4J_PASSWORD = process.env.NEO4J_PASSWORD || 'password';

// Neo4j-Driver initialisieren
const driver = neo4j.driver(
  NEO4J_URI,
  neo4j.auth.basic(NEO4J_USER, NEO4J_PASSWORD)
);

// MCP-Server erstellen
const server = new Server(
  {
    name: 'neo4j-server',
    version: '1.0.0',
  },
  {
    capabilities: {
      tools: {},
    },
  }
);

// Tool-Definitionen
server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: 'execute_cypher',
        description: 'Execute a Cypher query against the Neo4j database',
        inputSchema: {
          type: 'object',
          properties: {
            query: {
              type: 'string',
              description: 'Cypher query to execute',
            },
            parameters: {
              type: 'object',
              description: 'Query parameters',
              additionalProperties: true,
            },
          },
          required: ['query'],
        },
      },
      {
        name: 'get_statistics',
        description: 'Get database statistics',
        inputSchema: {
          type: 'object',
          properties: {},
        },
      },
      {
        name: 'get_classes',
        description: 'Get all classes from the database',
        inputSchema: {
          type: 'object',
          properties: {
            filter: {
              type: 'string',
              description: 'Filter by class name (contains)',
            },
            limit: {
              type: 'number',
              description: 'Maximum number of classes to return (default: 20)',
            },
          },
        },
      },
      {
        name: 'get_class_methods',
        description: 'Get methods of a specific class',
        inputSchema: {
          type: 'object',
          properties: {
            className: {
              type: 'string',
              description: 'Name of the class',
            },
          },
          required: ['className'],
        },
      },
      {
        name: 'get_class_fields',
        description: 'Get fields of a specific class',
        inputSchema: {
          type: 'object',
          properties: {
            className: {
              type: 'string',
              description: 'Name of the class',
            },
          },
          required: ['className'],
        },
      },
      {
        name: 'get_class_hierarchy',
        description: 'Get class hierarchy (parent class and interfaces)',
        inputSchema: {
          type: 'object',
          properties: {
            className: {
              type: 'string',
              description: 'Name of the class',
            },
          },
          required: ['className'],
        },
      },
      {
        name: 'find_classes',
        description: 'Find classes by pattern',
        inputSchema: {
          type: 'object',
          properties: {
            pattern: {
              type: 'string',
              description: 'Pattern to search for in class names',
            },
            limit: {
              type: 'number',
              description: 'Maximum number of results (default: 20)',
            },
          },
          required: ['pattern'],
        },
      },
    ],
  };
});

// Tool-Ausführung
server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;
  const session = driver.session();

  try {
    switch (name) {
      case 'execute_cypher': {
        const result = await session.run(args.query, args.parameters || {});
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(result.records.map(r => r.toObject()), null, 2),
            },
          ],
        };
      }

      case 'get_statistics': {
        const result = await session.run(`
          MATCH (n)
          RETURN labels(n)[0] as type, count(*) as count
          ORDER BY count DESC
        `);
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(result.records.map(r => r.toObject()), null, 2),
            },
          ],
        };
      }

      case 'get_classes': {
        const filter = args.filter || '';
        const limit = args.limit || 20;
        const query = filter
          ? `MATCH (c:Type) WHERE c.fqn CONTAINS $filter RETURN c.fqn as className LIMIT $limit`
          : `MATCH (c:Type) RETURN c.fqn as className LIMIT $limit`;
        const result = await session.run(query, { filter, limit });
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(result.records.map(r => r.toObject()), null, 2),
            },
          ],
        };
      }

      case 'get_class_methods': {
        const result = await session.run(
          `
          MATCH (c:Type {fqn: $className})-[:DECLARES]->(m:Method)
          RETURN m.signature as method, m.visibility as visibility
          ORDER BY m.signature
          `,
          { className: args.className }
        );
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(result.records.map(r => r.toObject()), null, 2),
            },
          ],
        };
      }

      case 'get_class_fields': {
        const result = await session.run(
          `
          MATCH (c:Type {fqn: $className})-[:DECLARES]->(f:Field)
          RETURN f.signature as field, f.visibility as visibility
          ORDER BY f.signature
          `,
          { className: args.className }
        );
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(result.records.map(r => r.toObject()), null, 2),
            },
          ],
        };
      }

      case 'get_class_hierarchy': {
        const result = await session.run(
          `
          MATCH (c:Type {fqn: $className})
          OPTIONAL MATCH (c)-[:EXTENDS]->(parent:Type)
          OPTIONAL MATCH (c)-[:IMPLEMENTS]->(interface:Type)
          RETURN 
            c.fqn as className,
            parent.fqn as parentClass,
            collect(DISTINCT interface.fqn) as interfaces
          `,
          { className: args.className }
        );
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(result.records.map(r => r.toObject()), null, 2),
            },
          ],
        };
      }

      case 'find_classes': {
        const limit = args.limit || 20;
        const result = await session.run(
          `
          MATCH (c:Type)
          WHERE c.fqn CONTAINS $pattern
          RETURN c.fqn as className
          LIMIT $limit
          `,
          { pattern: args.pattern, limit }
        );
        return {
          content: [
            {
              type: 'text',
              text: JSON.stringify(result.records.map(r => r.toObject()), null, 2),
            },
          ],
        };
      }

      default:
        throw new Error(`Unknown tool: ${name}`);
    }
  } finally {
    await session.close();
  }
});

// Server starten
async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error('Neo4j MCP Server running on stdio');
}

main().catch((error) => {
  console.error('Server error:', error);
  process.exit(1);
});
```

### 5. package.json anpassen

Fügen Sie in der [`package.json`](~/Documents/Cline/MCP/neo4j-server/package.json) folgendes hinzu:

```json
{
  "name": "neo4j-mcp-server",
  "version": "1.0.0",
  "type": "module",
  "main": "index.js",
  "bin": {
    "neo4j-mcp-server": "./index.js"
  },
  "dependencies": {
    "@modelcontextprotocol/sdk": "^0.5.0",
    "neo4j-driver": "^5.15.0"
  }
}
```

### 6. Ausführbar machen

```bash
chmod +x index.js
```

## MCP-Client Konfiguration (Cline/VSCode)

### 1. Cline-Konfiguration öffnen

In VSCode:
1. Öffnen Sie die Command Palette (`Cmd+Shift+P` auf macOS)
2. Suchen Sie nach "Cline: Open MCP Settings"

### 2. MCP-Server hinzufügen

Fügen Sie in der MCP-Konfigurationsdatei folgende Konfiguration hinzu:

```json
{
  "mcpServers": {
    "neo4j": {
      "command": "node",
      "args": ["/Users/oliverwidder/Documents/Cline/MCP/neo4j-server/index.js"],
      "env": {
        "NEO4J_URI": "bolt://localhost:7687",
        "NEO4J_USER": "neo4j",
        "NEO4J_PASSWORD": "your-password-here"
      }
    }
  }
}
```

**Wichtig:** Passen Sie die Umgebungsvariablen an Ihre Neo4j-Installation an:
- `NEO4J_URI`: Die Verbindungs-URI Ihrer Neo4j-Datenbank
- `NEO4J_USER`: Ihr Neo4j-Benutzername
- `NEO4J_PASSWORD`: Ihr Neo4j-Passwort

### 3. VSCode neu laden

Laden Sie VSCode neu, damit die Änderungen wirksam werden:
- Command Palette → "Developer: Reload Window"

## Verwendung

### Verfügbare Tools

Nach erfolgreicher Einrichtung stehen folgende Tools zur Verfügung:

1. **`mcp__neo4j__execute_cypher`**
   - Führt beliebige Cypher-Queries aus
   - Parameter: `query` (String), `parameters` (Object, optional)

2. **`mcp__neo4j__get_statistics`**
   - Liefert Datenbankstatistiken (Anzahl der Knoten pro Label)

3. **`mcp__neo4j__get_classes`**
   - Listet alle Klassen auf
   - Parameter: `filter` (String, optional), `limit` (Number, optional)

4. **`mcp__neo4j__get_class_methods`**
   - Zeigt alle Methoden einer Klasse
   - Parameter: `className` (String, erforderlich)

5. **`mcp__neo4j__get_class_fields`**
   - Zeigt alle Felder einer Klasse
   - Parameter: `className` (String, erforderlich)

6. **`mcp__neo4j__get_class_hierarchy`**
   - Zeigt die Klassenhierarchie (Parent-Klasse und Interfaces)
   - Parameter: `className` (String, erforderlich)

7. **`mcp__neo4j__find_classes`**
   - Sucht Klassen nach Pattern
   - Parameter: `pattern` (String, erforderlich), `limit` (Number, optional)

### Beispiel-Abfragen

#### Datenbankstatistiken abrufen

```
Zeige mir die Statistiken der Neo4j-Datenbank
```

Der MCP-Server wird automatisch das Tool `mcp__neo4j__get_statistics` aufrufen.

#### Klassen suchen

```
Suche alle Klassen, die "Party" im Namen enthalten
```

#### Methoden einer Klasse anzeigen

```
Zeige mir alle Methoden der Klasse org.apache.ofbiz.party.party.PartyServices
```

#### Eigene Cypher-Query ausführen

```
Führe folgende Cypher-Query aus:
MATCH (c:Type)-[:DECLARES]->(m:Method)
WHERE c.fqn CONTAINS 'PartyServices'
RETURN c.fqn, count(m) as methodCount
ORDER BY methodCount DESC
```

## Fehlerbehebung

### Verbindungsprobleme

Wenn die Verbindung zur Neo4j-Datenbank fehlschlägt:

1. **Prüfen Sie, ob Neo4j läuft:**
   ```bash
   # Für Neo4j Desktop: Starten Sie die Datenbank über die GUI
   # Für Neo4j Server:
   neo4j status
   ```

2. **Testen Sie die Verbindung:**
   ```bash
   cypher-shell -a bolt://localhost:7687 -u neo4j -p your-password
   ```

3. **Überprüfen Sie die Firewall-Einstellungen:**
   - Port 7687 (Bolt) muss erreichbar sein

### MCP-Server startet nicht

1. **Überprüfen Sie die Node.js-Version:**
   ```bash
   node --version  # Sollte >= 14 sein
   ```

2. **Installieren Sie die Abhängigkeiten neu:**
   ```bash
   cd ~/Documents/Cline/MCP/neo4j-server
   rm -rf node_modules package-lock.json
   npm install
   ```

3. **Testen Sie den Server manuell:**
   ```bash
   node index.js
   ```

### Logs überprüfen

Der MCP-Server schreibt Fehler nach `stderr`. In Cline können Sie die Logs einsehen:
- Command Palette → "Cline: Show MCP Logs"

## Erweiterte Konfiguration

### Remote Neo4j-Datenbank

Für eine Remote-Datenbank passen Sie die `NEO4J_URI` an:

```json
{
  "mcpServers": {
    "neo4j": {
      "command": "node",
      "args": ["/Users/oliverwidder/Documents/Cline/MCP/neo4j-server/index.js"],
      "env": {
        "NEO4J_URI": "neo4j+s://your-server.databases.neo4j.io",
        "NEO4J_USER": "neo4j",
        "NEO4J_PASSWORD": "your-password-here"
      }
    }
  }
}
```

### Mehrere Neo4j-Instanzen

Sie können mehrere Neo4j-Server konfigurieren:

```json
{
  "mcpServers": {
    "neo4j-dev": {
      "command": "node",
      "args": ["/Users/oliverwidder/Documents/Cline/MCP/neo4j-server/index.js"],
      "env": {
        "NEO4J_URI": "bolt://localhost:7687",
        "NEO4J_USER": "neo4j",
        "NEO4J_PASSWORD": "dev-password"
      }
    },
    "neo4j-prod": {
      "command": "node",
      "args": ["/Users/oliverwidder/Documents/Cline/MCP/neo4j-server/index.js"],
      "env": {
        "NEO4J_URI": "neo4j+s://prod-server.databases.neo4j.io",
        "NEO4J_USER": "neo4j",
        "NEO4J_PASSWORD": "prod-password"
      }
    }
  }
}
```

## Sicherheitshinweise

1. **Passwörter nicht im Klartext speichern:**
   - Verwenden Sie Umgebungsvariablen oder ein Secrets-Management-System
   - Fügen Sie die MCP-Konfigurationsdatei zu `.gitignore` hinzu

2. **Zugriffsbeschränkungen:**
   - Verwenden Sie einen Neo4j-Benutzer mit eingeschränkten Rechten
   - Aktivieren Sie SSL/TLS für Remote-Verbindungen

3. **Query-Limits:**
   - Implementieren Sie Timeouts für lange laufende Queries
   - Begrenzen Sie die Anzahl der zurückgegebenen Ergebnisse

## Weitere Ressourcen

- [Model Context Protocol Dokumentation](https://modelcontextprotocol.io/)
- [Neo4j Driver Dokumentation](https://neo4j.com/docs/javascript-manual/current/)
- [Cypher Query Language](https://neo4j.com/docs/cypher-manual/current/)
- [OFBiz Neo4j Queries](../refactor/NEO4J_QUERIES.md)

## Status

✅ **Aktuell verbunden:** Der Neo4j MCP-Server ist bereits konfiguriert und verbunden.

**Datenbankstatistiken:**
- Java-Knoten: 343.608
- Type-Knoten: 11.752
- File-Knoten: 4.160
- Package-Knoten: 877
- Value-Knoten: 20
- Artifact-Knoten: 4
- Task-Knoten: 1

Die Datenbank enthält den vollständigen OFBiz-Code und kann für Analysen und Refactoring-Aufgaben verwendet werden.
