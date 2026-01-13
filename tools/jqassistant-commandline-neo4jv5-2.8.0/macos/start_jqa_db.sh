#!/bin/bash
# Start jqAssistant Neo4j Database Server
# Usage: ./start_jqa_db.sh <project_dir>

# Check command line arguments
if [ $# -ne 1 ]; then
    echo "Usage: $0 <project_dir>"
    echo ""
    echo "Arguments:"
    echo "  project_dir      - Path to the OFBiz project directory"
    echo ""
    echo "Example:"
    echo "  $0 /Users/oliverwidder/dev/ofbiz"
    exit 1
fi

PROJECT_DIR="$1"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JQA_BIN="$PROJECT_DIR/tools/jqassistant-commandline-neo4jv5-2.8.0/bin/jqassistant"
LOG_FILE="/tmp/jqassistant-server.log"
PID_FILE="/tmp/jqassistant-server.pid"
INPUT_FIFO="/tmp/jqassistant-input"

echo "=========================================="
echo "Starting jqAssistant Neo4j Database"
echo "=========================================="
echo ""
echo "Project Directory: $PROJECT_DIR"
echo ""

# Check if project directory exists
if [ ! -d "$PROJECT_DIR" ]; then
    echo "✗ Project directory not found: $PROJECT_DIR"
    exit 1
fi

# Kill any existing server
pkill -f "jqassistant server" 2>/dev/null || true
sleep 2
rm -f "$INPUT_FIFO" "$PID_FILE"

# Create named pipe
mkfifo "$INPUT_FIFO" 2>/dev/null || true

# Start server with nohup, using named pipe for stdin
echo "Starting server..."
nohup env JQASSISTANT_STORE_NEO4J_DATABASE=neo4j "$JQA_BIN" server \
    < "$INPUT_FIFO" > "$LOG_FILE" 2>&1 &
SERVER_PID=$!
echo $SERVER_PID > "$PID_FILE"

# Keep the named pipe open by writing to it in background
# This process will be killed when we stop the server
(while true; do sleep 1; done > "$INPUT_FIFO" 2>/dev/null) &
FIFO_PID=$!
echo $FIFO_PID >> "$PID_FILE"

# Wait for server to initialize
echo "Waiting for server to initialize..."
sleep 10

# Check if server is running
if ps -p "$SERVER_PID" > /dev/null 2>&1; then
    echo ""
    echo "✓ Server started successfully (PID: $SERVER_PID)"
    echo ""
    echo "Database available at:"
    echo "  Bolt: bolt://localhost:7687"
    echo "  Browser: http://localhost:7474"
    echo "  Username: neo4j"
    echo ""
    echo "To stop the server, run: ./stop_jqa_db.sh"
else
    echo "✗ Failed to start server"
    echo ""
    echo "Last 30 lines of log:"
    tail -30 "$LOG_FILE"
    rm -f "$PID_FILE"
    exit 1
fi
