#!/bin/bash
# Script to list all data in jqAssistant embedded database

echo "=========================================="
echo "jqAssistant Database Content Analysis"
echo "=========================================="
echo ""

# Try to connect to jqAssistant embedded DB on port 7688
# If not available, try to start the server first

echo "Checking if jqAssistant server is running on port 7688..."
if ! nc -z localhost 7688 2>/dev/null; then
    echo "Server not running. Attempting to start..."
    mkfifo /tmp/jqassistant-input 2>/dev/null || true
    nohup tools/jqassistant-commandline-neo4jv5-2.8.0/bin/jqassistant server < /tmp/jqassistant-input > /tmp/jqassistant-server.log 2>&1 &
    sleep 10
fi

echo ""
echo "Querying database..."
echo ""

# Create a temporary Cypher script
cat > /tmp/query_jqa.cypher << 'EOF'
// Count all node types
MATCH (n) RETURN labels(n)[0] as NodeType, COUNT(n) as Count ORDER BY Count DESC;

// Count all relationship types
MATCH ()-[r]->() RETURN type(r) as RelationType, COUNT(r) as Count ORDER BY Count DESC;

// List all Class nodes
MATCH (c:Class) RETURN c.fqn as FullyQualifiedName, c.name as ClassName LIMIT 50;

// List all Package nodes
MATCH (p:Package) RETURN p.fqn as PackageName, COUNT((p)-[:CONTAINS]->()) as ContainedItems;

// List all Method nodes
MATCH (m:Method) RETURN m.signature as MethodSignature LIMIT 50;

// List all Field nodes
MATCH (f:Field) RETURN f.name as FieldName, f.type as FieldType LIMIT 50;

// Show relationships between Classes
MATCH (c1:Class)-[r]->(c2:Class) RETURN type(r) as RelationType, COUNT(r) as Count;
EOF

echo "=== NODE TYPE STATISTICS ==="
cypher-shell -a bolt://localhost:7688 -u neo4j -p neo4j "MATCH (n) RETURN labels(n)[0] as NodeType, COUNT(n) as Count ORDER BY Count DESC;" 2>/dev/null || echo "Could not connect to database"

echo ""
echo "=== RELATIONSHIP TYPE STATISTICS ==="
cypher-shell -a bolt://localhost:7688 -u neo4j -p neo4j "MATCH ()-[r]->() RETURN type(r) as RelationType, COUNT(r) as Count ORDER BY Count DESC;" 2>/dev/null || echo "Could not connect to database"

echo ""
echo "=== SAMPLE CLASS NODES (first 20) ==="
cypher-shell -a bolt://localhost:7688 -u neo4j -p neo4j "MATCH (c:Class) RETURN c.fqn as FullyQualifiedName LIMIT 20;" 2>/dev/null || echo "Could not connect to database"

echo ""
echo "=== SAMPLE PACKAGE NODES ==="
cypher-shell -a bolt://localhost:7688 -u neo4j -p neo4j "MATCH (p:Package) RETURN p.fqn as PackageName LIMIT 20;" 2>/dev/null || echo "Could not connect to database"

echo ""
echo "=== TOTAL STATISTICS ==="
cypher-shell -a bolt://localhost:7688 -u neo4j -p neo4j "MATCH (n) RETURN 'Total Nodes' as Metric, COUNT(n) as Value UNION MATCH ()-[r]->() RETURN 'Total Relationships' as Metric, COUNT(r) as Value;" 2>/dev/null || echo "Could not connect to database"

echo ""
echo "=========================================="
echo "Analysis complete!"
echo "=========================================="
