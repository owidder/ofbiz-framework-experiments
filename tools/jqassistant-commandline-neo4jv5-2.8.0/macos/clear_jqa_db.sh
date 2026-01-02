#!/bin/bash
# Clear jqAssistant Neo4j Database using Cypher queries
# Usage: ./clear_jqa_db.sh

set -e

PROJECT_DIR="/Users/oliverwidder/dev/ofbiz-framework"
DB_DIR="$PROJECT_DIR/jqassistant/store/data/databases/neo4j"
PID_FILE="/tmp/jqassistant-server.pid"
NEO4J_URI="bolt://localhost:7687"
NEO4J_USER="neo4j"
NEO4J_PASSWORD="neo4j"

echo "=========================================="
echo "Clearing jqAssistant Neo4j Database"
echo "=========================================="
echo ""

# Check if server is running
if [ ! -f "$PID_FILE" ]; then
    echo "✗ Server is not running"
    echo "Please start the server first: ./start_jqa_db.sh"
    exit 1
fi

SERVER_PID=$(cat "$PID_FILE")
if ! ps -p "$SERVER_PID" > /dev/null 2>&1; then
    echo "✗ Server is not running (PID: $SERVER_PID not found)"
    echo "Please start the server first: ./start_jqa_db.sh"
    exit 1
fi

echo "✓ Server is running (PID: $SERVER_PID)"
echo ""

# Check if cypher-shell is available
if ! command -v cypher-shell &> /dev/null; then
    echo "✗ cypher-shell not found"
    echo "Please install Neo4j tools or add cypher-shell to PATH"
    exit 1
fi

echo "Connecting to database: $NEO4J_URI"
echo ""

# Get current statistics (using Python to avoid Java 21 requirement)
echo "Current database statistics:"
python3 << 'PYTHON_STATS'
from neo4j import GraphDatabase
try:
    driver = GraphDatabase.driver("bolt://localhost:7687", auth=("neo4j", "neo4j"))
    with driver.session(database="neo4j") as session:
        result = session.run("MATCH (n) RETURN count(n) as count")
        count = result.single()['count']
        print(f"  Nodes: {count:,}")
    driver.close()
except Exception as e:
    print(f"  Error: {e}")
PYTHON_STATS
echo ""

# Confirm deletion
echo "⚠ WARNING: This will delete ALL data from the database!"
echo "Press Ctrl+C to cancel, or wait 5 seconds to continue..."
sleep 5
echo ""

# Delete all data using Batch-Delete Strategy (Python)
echo "Deleting all nodes and relationships using batch strategy..."
echo ""

python3 << 'PYTHON_EOF'
import sys
import time
from neo4j import GraphDatabase

uri = "bolt://localhost:7687"
username = "neo4j"
password = "neo4j"
database = "neo4j"
batch_size = 10000
batch_delay = 0.5

try:
    driver = GraphDatabase.driver(uri, auth=(username, password))
    
    with driver.session(database=database) as session:
        print("Step 1: Deleting Relationships")
        print("-" * 50)
        deleted_total = 0
        
        while True:
            result = session.run(f"""
                MATCH ()-[r]->()
                WITH r LIMIT {batch_size}
                DELETE r
                RETURN count(*) as deleted
            """)
            deleted_rels = result.single()['deleted']
            
            if deleted_rels == 0:
                break
            
            deleted_total += deleted_rels
            print(f"  ✓ {deleted_rels:,} relationships deleted (Total: {deleted_total:,})")
            time.sleep(batch_delay)
        
        print(f"\n✓ All {deleted_total:,} relationships deleted")
        print()
        
        # Delete nodes in batches
        print("Step 2: Deleting Nodes")
        print("-" * 50)
        deleted_total = 0
        
        while True:
            result = session.run(f"""
                MATCH (n)
                WITH n LIMIT {batch_size}
                DELETE n
                RETURN count(*) as deleted
            """)
            deleted_nodes = result.single()['deleted']
            
            if deleted_nodes == 0:
                break
            
            deleted_total += deleted_nodes
            print(f"  ✓ {deleted_nodes:,} nodes deleted (Total: {deleted_total:,})")
            time.sleep(batch_delay)
        
        print(f"\n✓ All {deleted_total:,} nodes deleted")
        print()
        
        # Verify database is empty
        print("Step 3: Verification")
        print("-" * 50)
        result = session.run("MATCH (n) RETURN count(n) as count")
        count = result.single()['count']
        
        if count == 0:
            print(f"✓ Database verified empty (0 nodes remaining)")
        else:
            print(f"✗ Error: {count:,} nodes still remaining")
            sys.exit(1)
    
    driver.close()
    
except Exception as e:
    print(f"✗ Error: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)

PYTHON_EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ All data deleted successfully"
else
    echo ""
    echo "✗ Error deleting data"
    exit 1
fi

echo ""

# Verify deletion (using Python to avoid Java 21 requirement)
echo "Final verification..."
python3 << 'PYTHON_VERIFY'
from neo4j import GraphDatabase
try:
    driver = GraphDatabase.driver("bolt://localhost:7687", auth=("neo4j", "neo4j"))
    with driver.session(database="neo4j") as session:
        result = session.run("MATCH (n) RETURN count(n) as count")
        count = result.single()['count']
        if count == 0:
            print("✓ Database is now empty")
            print("")
            print("To refill the database, run: ./refill_jqa_db.sh")
        else:
            print(f"✗ Database still contains {count:,} nodes")
            exit(1)
    driver.close()
except Exception as e:
    print(f"✗ Error: {e}")
    exit(1)
PYTHON_VERIFY

if [ $? -ne 0 ]; then
    exit 1
fi

echo ""
echo "=========================================="
echo "Database cleared successfully!"
echo "=========================================="
