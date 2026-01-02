#!/bin/bash
# Refill jqAssistant Neo4j Database with Class Files
# Usage: ./refill_jqa_db.sh

set -e

PROJECT_DIR="/Users/oliverwidder/dev/ofbiz"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JQA_BIN="$PROJECT_DIR/tools/jqassistant-commandline-neo4jv5-2.8.0/bin/jqassistant"
CLASS_DIR="$PROJECT_DIR/build/classes/java/main"
PID_FILE="/tmp/jqassistant-server.pid"
LOG_FILE="/tmp/jqassistant-refill.log"

echo "=========================================="
echo "Refilling jqAssistant Neo4j Database"
echo "=========================================="
echo ""

# Check if class directory exists
if [ ! -d "$CLASS_DIR" ]; then
    echo "✗ Class directory not found: $CLASS_DIR"
    echo "Please build the project first: gradle build"
    exit 1
fi

# Count class files
CLASS_COUNT=$(find "$CLASS_DIR" -name "*.class" | wc -l)
echo "Found $CLASS_COUNT class files in $CLASS_DIR"
echo ""

# Check if server is running and stop it
if [ -f "$PID_FILE" ]; then
    SERVER_PID=$(cat "$PID_FILE")
    if ps -p "$SERVER_PID" > /dev/null 2>&1; then
        echo "Stopping server (PID: $SERVER_PID)..."
        "$SCRIPT_DIR/stop_jqa_db.sh" > /dev/null 2>&1 || true
        sleep 3
        echo "✓ Server stopped"
        echo ""
    fi
fi

# Run jqAssistant scan
echo "Starting jqAssistant scan..."
echo "This may take a few minutes..."
echo ""

cd "$PROJECT_DIR"

# Export environment variables for remote Neo4j server
export JQASSISTANT_STORE_PROVIDER=neo4jv5
export JQASSISTANT_STORE_URI=bolt://localhost:7687
export JQASSISTANT_STORE_USERNAME=neo4j
export JQASSISTANT_STORE_PASSWORD=neo4j12345

# Run scan with output to log file using remote Neo4j server
"$JQA_BIN" scan \
    -f "java:classpath::$CLASS_DIR" \
    > "$LOG_FILE" 2>&1

SCAN_EXIT_CODE=$?

if [ $SCAN_EXIT_CODE -eq 0 ]; then
    echo "✓ Scan completed successfully"
    echo ""
    
    # Start server again
    echo "Starting server..."
    "$SCRIPT_DIR/start_jqa_db.sh" > /dev/null 2>&1 || true
    sleep 5
    echo "✓ Server started"
    echo ""
    
    # Verify database content using Python
    echo "Verifying database content..."
    echo ""
    
    python3 << 'PYTHON_EOF'
from neo4j import GraphDatabase

try:
    driver = GraphDatabase.driver("bolt://localhost:7687", auth=("neo4j", "neo4j12345"))
    
    with driver.session(database="neo4j") as session:
        result = session.run("MATCH (n) RETURN COUNT(n) as count")
        for record in result:
            print(f"  Total Nodes: {record['count']}")
        
        result = session.run("MATCH (c:Class) RETURN COUNT(c) as count")
        for record in result:
            print(f"  Classes: {record['count']}")
        
        result = session.run("MATCH (m:Method) RETURN COUNT(m) as count")
        for record in result:
            print(f"  Methods: {record['count']}")
    
    driver.close()
except Exception as e:
    print(f"  Error querying database: {e}")
PYTHON_EOF
    
    echo ""
    echo "✓ Database refilled successfully"
    echo ""
    echo "Log file: $LOG_FILE"
    echo ""
    echo "To view the database, open: http://localhost:7474"
else
    echo "✗ Scan failed (exit code: $SCAN_EXIT_CODE)"
    echo ""
    echo "Last 50 lines of log:"
    tail -50 "$LOG_FILE"
    exit 1
fi
