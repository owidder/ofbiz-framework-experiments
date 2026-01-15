#!/bin/bash
# Start jqAssistant server with embedded database
# Usage: ./start_embedded_server.sh <project_dir>

set -e

if [ $# -ne 1 ]; then
    echo "Usage: $0 <project_dir>"
    echo ""
    echo "Example:"
    echo "  $0 /Users/oliverwidder/dev/ofbiz"
    exit 1
fi

PROJECT_DIR="$1"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JQA_BIN="$PROJECT_DIR/tools/jqassistant-commandline-neo4jv5-2.8.0/bin/jqassistant"

echo "=========================================="
echo "Starting jqAssistant Embedded Server"
echo "=========================================="
echo ""
echo "Project Directory: $PROJECT_DIR"
echo "Store Directory: $SCRIPT_DIR/jqassistant/store"
echo ""

# Check if project directory exists
if [ ! -d "$PROJECT_DIR" ]; then
    echo "✗ Project directory not found: $PROJECT_DIR"
    exit 1
fi

# Check if store exists
if [ ! -d "$SCRIPT_DIR/jqassistant/store" ]; then
    echo "✗ No data found. Please run import_ofbiz.sh first."
    exit 1
fi

cd "$SCRIPT_DIR"

echo "Starting embedded Neo4j server..."
echo "This will start a Neo4j browser on http://localhost:7474"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

"$JQA_BIN" server

