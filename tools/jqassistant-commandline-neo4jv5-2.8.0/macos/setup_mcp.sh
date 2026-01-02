#!/bin/bash
# Setup MCP Server for RooCode Integration
# This script configures the Neo4j MCP server for RooCode

set -e

PROJECT_DIR="/Users/oliverwidder/dev/ofbiz"
ROOCODE_CONFIG_DIR="$HOME/.config/roocode"
MCP_CONFIG_FILE="$ROOCODE_CONFIG_DIR/mcp.json"

echo "=========================================="
echo "Setting up MCP Server for RooCode"
echo "=========================================="
echo ""

# Create config directory if it doesn't exist
if [ ! -d "$ROOCODE_CONFIG_DIR" ]; then
    echo "Creating RooCode config directory: $ROOCODE_CONFIG_DIR"
    mkdir -p "$ROOCODE_CONFIG_DIR"
fi

# Check if mcp.json exists
if [ -f "$MCP_CONFIG_FILE" ]; then
    echo "⚠ MCP configuration already exists: $MCP_CONFIG_FILE"
    echo "Creating backup: ${MCP_CONFIG_FILE}.backup"
    cp "$MCP_CONFIG_FILE" "${MCP_CONFIG_FILE}.backup"
fi

# Create MCP configuration
echo "Creating MCP configuration..."
cat > "$MCP_CONFIG_FILE" << 'EOF'
{
  "mcpServers": {
    "jqassistant-neo4j": {
      "command": "python3",
      "args": [
        "/Users/oliverwidder/dev/ofbiz/neo4j_mcp_server.py"
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
EOF

echo "✓ MCP configuration created: $MCP_CONFIG_FILE"
echo ""

# Verify configuration
echo "Configuration content:"
cat "$MCP_CONFIG_FILE" | python3 -m json.tool
echo ""

# Check if neo4j_mcp_server.py exists
if [ ! -f "$PROJECT_DIR/neo4j_mcp_server.py" ]; then
    echo "✗ neo4j_mcp_server.py not found in $PROJECT_DIR"
    exit 1
fi

echo "✓ neo4j_mcp_server.py found"
echo ""

# Check if Python neo4j driver is installed
echo "Checking Python dependencies..."
if python3 -c "import neo4j" 2>/dev/null; then
    echo "✓ neo4j Python driver is installed"
else
    echo "⚠ neo4j Python driver not found"
    echo "Install with: pip3 install neo4j"
fi

echo ""
echo "=========================================="
echo "Setup complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Start the jqAssistant server:"
echo "   ./tools/jqassistant-commandline-neo4jv5-2.8.0/macos/start_jqa_db.sh"
echo ""
echo "2. Restart RooCode to load the MCP configuration"
echo ""
echo "3. Use the MCP server in RooCode:"
echo "   @jqassistant-neo4j execute_cypher \"MATCH (c:Class) RETURN c.fqn LIMIT 10\""
echo ""
