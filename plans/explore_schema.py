#!/usr/bin/env python3
"""
Neo4j Schema Explorer - Erkunde die Datenbankstruktur
"""

from neo4j import GraphDatabase
import os
import json

class SchemaExplorer:
    def __init__(self):
        self.uri = os.getenv("NEO4J_URI", "bolt://localhost:7687")
        self.user = os.getenv("NEO4J_USER", "neo4j")
        self.password = os.getenv("NEO4J_PASSWORD", "neo4j")
        self.database = os.getenv("NEO4J_DATABASE", "neo4j")
        
        self.driver = None
        
        try:
            self.driver = GraphDatabase.driver(self.uri, auth=(self.user, self.password))
            self.driver.verify_connectivity()
            print(f"✓ Verbunden mit Neo4j: {self.uri}")
        except Exception as e:
            print(f"✗ Fehler beim Verbinden: {str(e)}")
            raise
    
    def close(self):
        if self.driver:
            self.driver.close()
    
    def execute_query(self, query: str) -> list:
        """Führe eine Query aus"""
        try:
            with self.driver.session(database=self.database) as session:
                result = session.run(query)
                return [dict(record) for record in result]
        except Exception as e:
            print(f"✗ Fehler: {str(e)}")
            return []
    
    def explore_labels(self):
        """Erkunde verfügbare Labels"""
        print("\n" + "="*80)
        print("VERFÜGBARE LABELS")
        print("="*80)
        
        query = "CALL db.labels() YIELD label RETURN label ORDER BY label"
        results = self.execute_query(query)
        
        for row in results:
            label = row.get('label', 'Unknown')
            print(f"  - {label}")
        
        return results
    
    def explore_relationships(self):
        """Erkunde verfügbare Relationship-Typen"""
        print("\n" + "="*80)
        print("VERFÜGBARE RELATIONSHIP-TYPEN")
        print("="*80)
        
        query = "CALL db.relationshipTypes() YIELD relationshipType RETURN relationshipType ORDER BY relationshipType"
        results = self.execute_query(query)
        
        for row in results:
            rel_type = row.get('relationshipType', 'Unknown')
            print(f"  - {rel_type}")
        
        return results
    
    def explore_properties(self):
        """Erkunde verfügbare Properties"""
        print("\n" + "="*80)
        print("VERFÜGBARE PROPERTIES")
        print("="*80)
        
        query = "CALL db.propertyKeys() YIELD propertyKey RETURN propertyKey ORDER BY propertyKey"
        results = self.execute_query(query)
        
        for row in results:
            prop = row.get('propertyKey', 'Unknown')
            print(f"  - {prop}")
        
        return results
    
    def explore_method_nodes(self):
        """Erkunde Method-Nodes"""
        print("\n" + "="*80)
        print("METHOD NODES - SAMPLE")
        print("="*80)
        
        query = """
        MATCH (m:Method)
        WHERE m.fqn CONTAINS '.party.'
        RETURN m.fqn as FQN, keys(m) as Properties
        LIMIT 5
        """
        results = self.execute_query(query)
        
        if results:
            for idx, row in enumerate(results, 1):
                print(f"\n{idx}. {row.get('FQN', 'Unknown')}")
                props = row.get('Properties', [])
                for prop in props:
                    print(f"   - {prop}")
        else:
            print("Keine Method-Nodes mit '.party.' gefunden")
        
        return results
    
    def explore_party_methods(self):
        """Erkunde Party Service Methoden"""
        print("\n" + "="*80)
        print("PARTY SERVICE METHODEN - STATISTIK")
        print("="*80)
        
        query = """
        MATCH (m:Method)
        WHERE m.fqn CONTAINS '.party.'
        RETURN COUNT(m) as TotalMethods
        """
        results = self.execute_query(query)
        
        if results:
            total = results[0].get('TotalMethods', 0)
            print(f"Gesamt Party Service Methoden: {total}")
        
        return results
    
    def explore_invocations(self):
        """Erkunde Invocation-Relationships"""
        print("\n" + "="*80)
        print("INVOCATION RELATIONSHIPS - SAMPLE")
        print("="*80)
        
        query = """
        MATCH (caller:Method)-[rel]->(target:Method)
        WHERE target.fqn CONTAINS '.party.'
        RETURN type(rel) as RelationType, COUNT(*) as Count
        LIMIT 10
        """
        results = self.execute_query(query)
        
        if results:
            print("\nRelationship-Typen zu Party Service:")
            for row in results:
                rel_type = row.get('RelationType', 'Unknown')
                count = row.get('Count', 0)
                print(f"  - {rel_type}: {count}")
        else:
            print("Keine Relationships zu Party Service gefunden")
        
        return results
    
    def explore_party_callers(self):
        """Erkunde Aufrufer von Party Service"""
        print("\n" + "="*80)
        print("AUFRUFER VON PARTY SERVICE - SAMPLE")
        print("="*80)
        
        query = """
        MATCH (caller:Method)-[rel]->(target:Method)
        WHERE target.fqn CONTAINS '.party.'
        RETURN 
          caller.fqn as CallerFQN,
          type(rel) as RelationType,
          target.fqn as TargetFQN
        LIMIT 10
        """
        results = self.execute_query(query)
        
        if results:
            print("\nSample Aufrufe:")
            for idx, row in enumerate(results, 1):
                caller = row.get('CallerFQN', 'Unknown')
                rel_type = row.get('RelationType', 'Unknown')
                target = row.get('TargetFQN', 'Unknown')
                print(f"\n{idx}. {caller}")
                print(f"   --[{rel_type}]--> {target}")
        else:
            print("Keine Aufrufe zu Party Service gefunden")
        
        return results
    
    def explore_all_nodes(self):
        """Erkunde alle Node-Typen und deren Anzahl"""
        print("\n" + "="*80)
        print("NODE-TYPEN UND ANZAHL")
        print("="*80)
        
        query = """
        CALL db.labels() YIELD label
        CALL apoc.cypher.run('MATCH (n:' + label + ') RETURN COUNT(n) as count', {}) YIELD value
        RETURN label, value.count as NodeCount
        ORDER BY NodeCount DESC
        """
        results = self.execute_query(query)
        
        if results:
            for row in results:
                label = row.get('label', 'Unknown')
                count = row.get('NodeCount', 0)
                print(f"  {label:<30} : {count:>10}")
        else:
            # Fallback ohne APOC
            print("APOC nicht verfügbar, nutze alternative Queries...")
            labels = self.explore_labels()
            for label_row in labels:
                label = label_row.get('label', 'Unknown')
                query = f"MATCH (n:{label}) RETURN COUNT(n) as count"
                result = self.execute_query(query)
                if result:
                    count = result[0].get('count', 0)
                    print(f"  {label:<30} : {count:>10}")
        
        return results
    
    def run_exploration(self):
        """Führe vollständige Exploration durch"""
        print("\n" + "="*80)
        print("NEO4J SCHEMA EXPLORATION")
        print("="*80)
        
        self.explore_all_nodes()
        self.explore_labels()
        self.explore_relationships()
        self.explore_properties()
        self.explore_party_methods()
        self.explore_invocations()
        self.explore_party_callers()
        self.explore_method_nodes()

if __name__ == "__main__":
    explorer = None
    try:
        explorer = SchemaExplorer()
        explorer.run_exploration()
    finally:
        if explorer:
            explorer.close()
