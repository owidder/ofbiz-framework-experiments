#!/usr/bin/env python3
"""
MCP Server für Neo4j jqAssistant Integration
Implementiert das Model Context Protocol für RooCode
"""
import json
import sys
import os
import logging
from neo4j import GraphDatabase
from typing import Any, Dict, List, Optional

# Logging konfigurieren
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    stream=sys.stderr
)
logger = logging.getLogger(__name__)

class Neo4jMCPServer:
    def __init__(self, uri: str, username: str, password: str, database: str = "neo4j"):
        try:
            self.driver = GraphDatabase.driver(uri, auth=(username, password))
            self.database = database
            # Test connection
            with self.driver.session(database=self.database) as session:
                session.run("RETURN 1")
            logger.info(f"Connected to Neo4j at {uri}")
        except Exception as e:
            logger.error(f"Failed to connect to Neo4j: {e}")
            raise
    
    def execute_query(self, query: str, parameters: Optional[Dict] = None) -> List[Dict]:
        """Execute a Cypher query and return results"""
        try:
            with self.driver.session(database=self.database) as session:
                result = session.run(query, parameters or {})
                return [dict(record) for record in result]
        except Exception as e:
            logger.error(f"Query execution failed: {e}")
            raise Exception(f"Query execution failed: {str(e)}")
    
    def find_circular_dependencies(self) -> List[Dict]:
        """Find circular dependencies between classes"""
        query = """
        MATCH (c1:Class)-[:DEPENDS_ON]->(c2:Class)-[:DEPENDS_ON]->(c1)
        RETURN DISTINCT c1.fqn as className
        ORDER BY className
        LIMIT 100
        """
        return self.execute_query(query)
    
    def search_classes(self, pattern: str) -> List[Dict]:
        """Search for classes matching a pattern"""
        query = """
        MATCH (c:Class)
        WHERE c.fqn CONTAINS $pattern OR c.name CONTAINS $pattern
        RETURN c.fqn as fqn, c.name as name
        ORDER BY c.fqn
        LIMIT 50
        """
        return self.execute_query(query, {"pattern": pattern})
    
    def get_statistics(self) -> Dict[str, Any]:
        """Get database statistics"""
        query = """
        MATCH (n) 
        WITH COUNT(n) as totalNodes
        MATCH (c:Class) WITH totalNodes, COUNT(c) as classes
        MATCH (m:Method) WITH totalNodes, classes, COUNT(m) as methods
        MATCH (f:Field) WITH totalNodes, classes, methods, COUNT(f) as fields
        MATCH (p:Package) WITH totalNodes, classes, methods, fields, COUNT(p) as packages
        RETURN {
            totalNodes: totalNodes,
            classes: classes,
            methods: methods,
            fields: fields,
            packages: packages
        } as stats
        """
        result = self.execute_query(query)
        return result[0]['stats'] if result else {}
    
    def find_dependencies(self, fqn: str, depth: int = 2) -> List[Dict]:
        """Find all dependencies of a class"""
        # Build query with literal depth value (not parameter)
        query = f"""
        MATCH (c:Class {{fqn: $fqn}})-[:DEPENDS_ON*1..{depth}]->(dep:Class)
        RETURN DISTINCT dep.fqn as dependency
        ORDER BY dependency
        """
        return self.execute_query(query, {"fqn": fqn})
    
    def find_usages(self, fqn: str) -> List[Dict]:
        """Find all classes that use a given class"""
        query = """
        MATCH (user:Class)-[:DEPENDS_ON]->(c:Class {fqn: $fqn})
        RETURN DISTINCT user.fqn as usedBy
        ORDER BY usedBy
        """
        return self.execute_query(query, {"fqn": fqn})
    
    def get_class_info(self, fqn: str) -> Dict[str, Any]:
        """Get detailed information about a class"""
        query = """
        MATCH (c:Class {fqn: $fqn})
        RETURN {
            fqn: c.fqn,
            name: c.name,
            type: c.type,
            visibility: c.visibility
        } as info
        """
        result = self.execute_query(query, {"fqn": fqn})
        return result[0]['info'] if result else {}
    
    def get_method_calls(self, method_signature: str) -> List[Dict]:
        """Get all methods called by a given method"""
        query = """
        MATCH (m:Method {signature: $signature})-[:INVOKES]->(called:Method)
        RETURN called.signature as calledMethod
        ORDER BY calledMethod
        """
        return self.execute_query(query, {"signature": method_signature})
    
    def get_package_structure(self, package_prefix: str = "") -> List[Dict]:
        """Get package structure"""
        query = """
        MATCH (p:Package)
        WHERE p.fqn STARTS WITH $prefix OR $prefix = ""
        OPTIONAL MATCH (p)-[:CONTAINS]->(c:Class)
        RETURN p.fqn as package, COUNT(c) as classCount
        ORDER BY p.fqn
        LIMIT 100
        """
        return self.execute_query(query, {"prefix": package_prefix})
    
    def handle_request(self, request: Dict) -> Dict:
        """Handle MCP requests"""
        method = request.get("method")
        params = request.get("params", {})
        request_id = request.get("id")
        
        try:
            if method == "execute_cypher":
                query = params.get("query")
                if not query:
                    return {"jsonrpc": "2.0", "id": request_id, "error": {"code": -32602, "message": "Query parameter required"}}
                variables = params.get("variables", {})
                result = self.execute_query(query, variables)
                return {"jsonrpc": "2.0", "id": request_id, "result": {"result": result, "count": len(result)}}
            
            elif method == "find_circular_dependencies":
                result = self.find_circular_dependencies()
                return {"jsonrpc": "2.0", "id": request_id, "result": {"result": result, "count": len(result)}}
            
            elif method == "search_classes":
                pattern = params.get("pattern", "")
                if not pattern:
                    return {"jsonrpc": "2.0", "id": request_id, "error": {"code": -32602, "message": "Pattern parameter required"}}
                result = self.search_classes(pattern)
                return {"jsonrpc": "2.0", "id": request_id, "result": {"result": result, "count": len(result)}}
            
            elif method == "get_statistics":
                result = self.get_statistics()
                return {"jsonrpc": "2.0", "id": request_id, "result": {"result": result}}
            
            elif method == "find_dependencies":
                fqn = params.get("fqn")
                if not fqn:
                    return {"jsonrpc": "2.0", "id": request_id, "error": {"code": -32602, "message": "FQN parameter required"}}
                depth = params.get("depth", 2)
                result = self.find_dependencies(fqn, depth)
                return {"jsonrpc": "2.0", "id": request_id, "result": {"result": result, "count": len(result)}}
            
            elif method == "find_usages":
                fqn = params.get("fqn")
                if not fqn:
                    return {"jsonrpc": "2.0", "id": request_id, "error": {"code": -32602, "message": "FQN parameter required"}}
                result = self.find_usages(fqn)
                return {"jsonrpc": "2.0", "id": request_id, "result": {"result": result, "count": len(result)}}
            
            elif method == "get_class_info":
                fqn = params.get("fqn")
                if not fqn:
                    return {"jsonrpc": "2.0", "id": request_id, "error": {"code": -32602, "message": "FQN parameter required"}}
                result = self.get_class_info(fqn)
                return {"jsonrpc": "2.0", "id": request_id, "result": {"result": result}}
            
            elif method == "get_method_calls":
                signature = params.get("signature")
                if not signature:
                    return {"jsonrpc": "2.0", "id": request_id, "error": {"code": -32602, "message": "Signature parameter required"}}
                result = self.get_method_calls(signature)
                return {"jsonrpc": "2.0", "id": request_id, "result": {"result": result, "count": len(result)}}
            
            elif method == "get_package_structure":
                prefix = params.get("prefix", "")
                result = self.get_package_structure(prefix)
                return {"jsonrpc": "2.0", "id": request_id, "result": {"result": result, "count": len(result)}}
            
            elif method == "health_check":
                query = "RETURN 1"
                self.execute_query(query)
                return {"jsonrpc": "2.0", "id": request_id, "result": {"status": "healthy"}}
            
            else:
                return {"jsonrpc": "2.0", "id": request_id, "error": {"code": -32601, "message": f"Unknown method: {method}"}}
        
        except Exception as e:
            logger.error(f"Error handling request: {e}")
            return {"jsonrpc": "2.0", "id": request_id, "error": {"code": -32603, "message": str(e)}}

def main():
    """Main entry point for MCP server"""
    # Get configuration from environment or use defaults
    uri = os.getenv("NEO4J_URI", "bolt://localhost:7687")
    username = os.getenv("NEO4J_USER", "neo4j")
    password = os.getenv("NEO4J_PASSWORD", "neo4j")
    database = os.getenv("NEO4J_DATABASE", "neo4j")
    
    # Initialize server
    try:
        server = Neo4jMCPServer(uri, username, password, database)
    except Exception as e:
        logger.error(f"Failed to initialize server: {e}")
        sys.exit(1)
    
    # Read requests from stdin and process them
    try:
        for line in sys.stdin:
            line = line.strip()
            if not line:
                continue
            
            try:
                request = json.loads(line)
                response = server.handle_request(request)
                print(json.dumps(response))
                sys.stdout.flush()
            except json.JSONDecodeError as e:
                logger.error(f"Invalid JSON: {e}")
                print(json.dumps({"jsonrpc": "2.0", "error": {"code": -32700, "message": f"Invalid JSON: {str(e)}"}}))
                sys.stdout.flush()
            except Exception as e:
                logger.error(f"Request processing failed: {e}")
                print(json.dumps({"jsonrpc": "2.0", "error": {"code": -32603, "message": f"Request processing failed: {str(e)}"}}))
                sys.stdout.flush()
    
    except KeyboardInterrupt:
        logger.info("Server interrupted")
    finally:
        server.driver.close()
        logger.info("Server stopped")

if __name__ == "__main__":
    main()
