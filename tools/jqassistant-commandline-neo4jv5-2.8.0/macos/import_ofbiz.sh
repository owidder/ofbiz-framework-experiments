#!/bin/bash
# Import OFBiz classes into Neo4j via jqAssistant
# Usage: ./import_ofbiz.sh <project_dir> <neo4j_password>

set -e

# Check command line arguments
if [ $# -lt 1 ] || [ $# -gt 2 ]; then
    echo "Usage: $0 <project_dir> [neo4j_password]"
    echo ""
    echo "Arguments:"
    echo "  project_dir      - Path to the OFBiz project directory"
    echo "  neo4j_password   - Neo4j password (optional, default: neo4j)"
    echo ""
    echo "Environment Variables:"
    echo "  NEO4J_PASSWORD   - Alternative way to provide the password"
    echo ""
    echo "Example:"
    echo "  $0 /Users/oliverwidder/dev/ofbiz"
    echo "  $0 /Users/oliverwidder/dev/ofbiz mypassword"
    echo "  NEO4J_PASSWORD=mypassword $0 /Users/oliverwidder/dev/ofbiz"
    exit 1
fi

PROJECT_DIR="$1"
NEO4J_PASSWORD="${2:-${NEO4J_PASSWORD:-neo4j}}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JQA_BIN="$PROJECT_DIR/tools/jqassistant-commandline-neo4jv5-2.8.0/bin/jqassistant"
CONFIG_FILE="/tmp/jqassistant-import-config-$$.yml"
OFBIZ_JAR="$PROJECT_DIR/build/libs/ofbiz.jar"

echo "=========================================="
echo "Import OFBiz into Neo4j via jqAssistant"
echo "=========================================="
echo ""
echo "Project Directory: $PROJECT_DIR"
echo "OFBiz JAR: $OFBIZ_JAR"
echo ""

# Check if project directory exists
if [ ! -d "$PROJECT_DIR" ]; then
    echo "✗ Project directory not found: $PROJECT_DIR"
    exit 1
fi

# Check if JAR file exists
if [ ! -f "$OFBIZ_JAR" ]; then
    echo "✗ OFBiz JAR not found: $OFBIZ_JAR"
    echo ""
    echo "Please build the project first:"
    echo "  cd $PROJECT_DIR"
    echo "  ./gradlew build"
    exit 1
fi

# Check if Neo4j is running
echo "Checking if Neo4j is running on localhost:7687..."
if ! nc -z localhost 7687 2>/dev/null; then
    echo ""
    echo "✗ Neo4j is not running on localhost:7687"
    echo ""
    echo "Please start Neo4j first using one of these commands:"
    echo "  brew services start neo4j"
    echo "  neo4j start"
    echo ""
    exit 1
fi
echo "✓ Neo4j is running"
echo ""

# Create temporary config file with password
cat > "$CONFIG_FILE" << EOF
jqassistant:
  store:
    uri: bolt://localhost:7687
    username: neo4j
    password: $NEO4J_PASSWORD
    embedded:
      enabled: false
    remote:
      enabled: true
EOF

echo "Step 1: Resetting database..."
"$JQA_BIN" reset -C "$CONFIG_FILE"

echo ""
echo "Step 2: Scanning JAR file..."
echo "This may take several minutes..."
"$JQA_BIN" scan -f "$OFBIZ_JAR" -C "$CONFIG_FILE"

echo ""
echo "Step 3: Running analysis..."
"$JQA_BIN" analyze -C "$CONFIG_FILE"

# Clean up
rm -f "$CONFIG_FILE"

echo ""
echo "=========================================="
echo "✓ Import completed successfully!"
echo "=========================================="
echo ""
echo "You can now:"
echo "  1. Open Neo4j Browser: http://localhost:7474"
echo "  2. Run queries like:"
echo "     MATCH (c:Class) RETURN c.fqn LIMIT 10"
echo "     MATCH (c:Class)-[:DECLARES]->(m:Method) RETURN c.fqn, count(m) ORDER BY count(m) DESC LIMIT 10"
echo ""
