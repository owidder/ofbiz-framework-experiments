# Semantische Code-Suche mit Qdrant, Ollama und Claude Code MCP

Eine Anleitung, um einen Code-Index in Qdrant zu erstellen und über MCP (Model Context Protocol) an Claude Code anzubinden.

## Überblick

Mit dieser Lösung können Sie natürlichsprachliche Fragen an Ihren Code stellen:

- "Wie wird Authentifizierung implementiert?"
- "Wo werden Bestellungen verarbeitet?"
- "Finde Code ähnlich zu dieser Methode"

**Architektur:**

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│ Claude Code │────▶│  MCP Server │────▶│   Qdrant    │
└─────────────┘     └─────────────┘     └─────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │   Ollama    │
                    │ (Embeddings)│
                    └─────────────┘
```

## Voraussetzungen

- Docker
- Node.js 18+
- Homebrew (macOS)

## Schritt 1: Qdrant starten

```bash
docker run -d --name qdrant \
  -p 6333:6333 -p 6334:6334 \
  -v qdrant_storage:/qdrant/storage \
  qdrant/qdrant
```

Verifizieren:
```bash
curl http://localhost:6333/collections
# {"result":{"collections":[]},"status":"ok"}
```

## Schritt 2: Ollama installieren und Embedding-Modell laden

```bash
# Installation (macOS)
brew install ollama

# Service starten
brew services start ollama

# Embedding-Modell laden (768 Dimensionen)
ollama pull nomic-embed-text
```

Test:
```bash
curl http://localhost:11434/api/embeddings \
  -d '{"model": "nomic-embed-text", "prompt": "Hello World"}'
```

## Schritt 3: MCP Server erstellen

Verzeichnis anlegen:
```bash
mkdir -p ~/mcp-servers/qdrant-server
cd ~/mcp-servers/qdrant-server
```

`package.json`:
```json
{
  "name": "qdrant-mcp-server",
  "version": "1.0.0",
  "type": "module",
  "dependencies": {
    "@modelcontextprotocol/sdk": "^1.0.0",
    "@qdrant/js-client-rest": "^1.12.0"
  }
}
```

`index.js`:
```javascript
import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { CallToolRequestSchema, ListToolsRequestSchema } from "@modelcontextprotocol/sdk/types.js";
import { QdrantClient } from "@qdrant/js-client-rest";

const QDRANT_URL = process.env.QDRANT_URL || "http://localhost:6333";
const OLLAMA_URL = process.env.OLLAMA_URL || "http://localhost:11434";
const COLLECTION_NAME = process.env.COLLECTION_NAME || "code-index";

const client = new QdrantClient({ url: QDRANT_URL });

async function getEmbedding(text) {
  const response = await fetch(`${OLLAMA_URL}/api/embeddings`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ model: "nomic-embed-text", prompt: text.substring(0, 8000) })
  });
  return (await response.json()).embedding;
}

const server = new Server(
  { name: "qdrant-mcp-server", version: "1.0.0" },
  { capabilities: { tools: {} } }
);

server.setRequestHandler(ListToolsRequestSchema, async () => ({
  tools: [
    {
      name: "qdrant_search",
      description: "Semantische Suche im Code",
      inputSchema: {
        type: "object",
        properties: {
          query: { type: "string", description: "Suchanfrage" },
          limit: { type: "number", description: "Anzahl Ergebnisse (default: 5)" }
        },
        required: ["query"]
      }
    },
    {
      name: "qdrant_list_collections",
      description: "Alle Collections auflisten",
      inputSchema: { type: "object", properties: {} }
    }
  ]
}));

server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  if (name === "qdrant_search") {
    const embedding = await getEmbedding(args.query);
    const results = await client.search(args.collection || COLLECTION_NAME, {
      vector: embedding,
      limit: args.limit || 5,
      with_payload: true
    });

    const formatted = results.map((r, i) => ({
      rank: i + 1,
      score: r.score.toFixed(3),
      file: r.payload?.file,
      snippet: r.payload?.text?.substring(0, 200)
    }));

    return { content: [{ type: "text", text: JSON.stringify(formatted, null, 2) }] };
  }

  if (name === "qdrant_list_collections") {
    const collections = await client.getCollections();
    return { content: [{ type: "text", text: JSON.stringify(collections, null, 2) }] };
  }
});

const transport = new StdioServerTransport();
await server.connect(transport);
```

Abhängigkeiten installieren:
```bash
npm install
```

## Schritt 4: Code indexieren

`index-code.js`:
```javascript
import { QdrantClient } from "@qdrant/js-client-rest";
import { readdir, readFile } from "fs/promises";
import { join, relative } from "path";

const client = new QdrantClient({ url: "http://localhost:6333" });
const OLLAMA_URL = "http://localhost:11434";

async function getEmbedding(text) {
  const response = await fetch(`${OLLAMA_URL}/api/embeddings`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ model: "nomic-embed-text", prompt: text.substring(0, 8000) })
  });
  return (await response.json()).embedding;
}

async function* walkDir(dir) {
  const entries = await readdir(dir, { withFileTypes: true });
  for (const entry of entries) {
    const fullPath = join(dir, entry.name);
    if (entry.isDirectory() && !["node_modules", ".git", "build"].includes(entry.name)) {
      yield* walkDir(fullPath);
    } else if (entry.name.endsWith(".java") || entry.name.endsWith(".ts") || entry.name.endsWith(".py")) {
      yield fullPath;
    }
  }
}

function hashString(str) {
  return Math.abs(str.split('').reduce((acc, char) => ((acc << 5) - acc) + char.charCodeAt(0), 0));
}

async function main() {
  const [directory, collection = "code-index"] = process.argv.slice(2);

  if (!directory) {
    console.log("Usage: node index-code.js <directory> [collection]");
    process.exit(1);
  }

  // Collection erstellen (768 Dimensionen für nomic-embed-text)
  try {
    await client.createCollection(collection, {
      vectors: { size: 768, distance: "Cosine" }
    });
    console.log(`Collection '${collection}' erstellt`);
  } catch (e) {
    console.log(`Collection '${collection}' existiert bereits`);
  }

  let count = 0;
  for await (const filePath of walkDir(directory)) {
    const content = await readFile(filePath, "utf-8");
    const relativePath = relative(directory, filePath);

    const embedding = await getEmbedding(`// File: ${relativePath}\n${content}`);

    await client.upsert(collection, {
      points: [{
        id: hashString(relativePath),
        vector: embedding,
        payload: { file: relativePath, text: content.substring(0, 2000) }
      }]
    });

    console.log(`✓ ${relativePath}`);
    count++;
  }

  console.log(`\n${count} Dateien indexiert.`);
}

main();
```

Indexierung starten:
```bash
node index-code.js /pfad/zu/ihrem/code mein-projekt
```

## Schritt 5: Claude Code konfigurieren

Option A: Projekt `.mcp.json` (im Projekt-Root):
```json
{
  "mcpServers": {
    "qdrant": {
      "type": "stdio",
      "command": "node",
      "args": ["~/mcp-servers/qdrant-server/index.js"],
      "env": {
        "QDRANT_URL": "http://localhost:6333",
        "COLLECTION_NAME": "mein-projekt"
      }
    }
  }
}
```

Option B: Global in `~/.claude.json`:
```json
{
  "projects": {
    "/pfad/zu/projekt": {
      "mcpServers": {
        "qdrant": {
          "type": "stdio",
          "command": "node",
          "args": ["~/mcp-servers/qdrant-server/index.js"],
          "env": {
            "QDRANT_URL": "http://localhost:6333",
            "COLLECTION_NAME": "mein-projekt"
          }
        }
      }
    }
  }
}
```

## Schritt 6: Testen

Claude Code neu starten und fragen:

```
> Wie wird Authentifizierung in diesem Projekt implementiert?

Claude nutzt automatisch qdrant_search und findet relevante Code-Stellen.
```

## Tipps

- **Chunking**: Für große Dateien empfiehlt sich das Aufteilen in Methoden/Funktionen
- **Embedding-Modell**: `nomic-embed-text` ist gut für Code; Alternativen: `mxbai-embed-large`
- **Collection löschen**: `curl -X DELETE http://localhost:6333/collections/mein-projekt`

## Ressourcen

- [Qdrant Dokumentation](https://qdrant.tech/documentation/)
- [Model Context Protocol](https://modelcontextprotocol.io/)
- [Ollama](https://ollama.ai/)
- [Claude Code](https://docs.anthropic.com/claude-code)
