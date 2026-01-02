#!/bin/bash
# Check status of jqAssistant Neo4j Database Server
# Usage: ./status_jqa_db.sh

PID_FILE="/tmp/jqassistant-server.pid"
LOG_FILE="/tmp/jqassistant-server.log"

echo "=========================================="
echo "jqAssistant Neo4j Database Status"
echo "=========================================="
echo ""

# Check if PID file exists
if [ ! -f "$PID_FILE" ]; then
    echo "Status: ✗ STOPPED (no PID file)"
    exit 1
fi

# Get PID
SERVER_PID=$(cat "$PID_FILE")

# Check if process is running
if ps -p "$SERVER_PID" > /dev/null 2>&1; then
    echo "Status: ✓ RUNNING"
    echo "PID: $SERVER_PID"
    echo ""
    echo "Database Connection:"
    echo "  Bolt: bolt://localhost:7687"
    echo "  Browser: http://localhost:7474"
    echo "  Username: neo4j"
    echo "  Password: neo4j"
    echo ""
    
    # Try to connect and get statistics
    echo "Attempting to connect..."
    if command -v cypher-shell &> /dev/null; then
        RESULT=$(cypher-shell -a bolt://localhost:7687 -u neo4j -p neo4j "MATCH (n) RETURN COUNT(n) as NodeCount" 2>/dev/null || echo "")
        if [ -n "$RESULT" ]; then
            echo "✓ Database connection successful"
            echo "$RESULT"
        else
            echo "⚠ Database connection failed (server may still be initializing)"
        fi
    else
        echo "⚠ cypher-shell not found (cannot test connection)"
    fi
    
    echo ""
    echo "Log file: $LOG_FILE"
    echo ""
    echo "To stop the server, run: ./stop_jqa_db.sh"
    exit 0
else
    echo "Status: ✗ STOPPED (PID $SERVER_PID not found)"
    rm -f "$PID_FILE"
    exit 1
fi
