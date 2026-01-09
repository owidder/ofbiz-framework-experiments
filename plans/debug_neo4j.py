#!/usr/bin/env python3
"""
Neo4j Debug - Verstehe die tatsächliche Datenbankstruktur
"""

from neo4j import GraphDatabase
import os
import json

class DebugExplorer:
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
    
    def debug_party_package(self):
        """Debug: Erkunde Party Package"""
        print("\n" + "="*80)
        print("DEBUG: PARTY PACKAGE STRUKTUR")
        print("="*80)
        
        query = """
        MATCH (p:Package {fqn: 'org.apache.ofbiz.party'})
        RETURN p.fqn as PackageFQN, keys(p) as Properties
        """
        results = self.execute_query(query)
        
        if results:
            for row in results:
                print(f"\nPackage: {row.get('PackageFQN', 'Unknown')}")
                print(f"Properties: {row.get('Properties', [])}")
        else:
            print("Party Package nicht gefunden")
        
        return results
    
    def debug_party_classes(self):
        """Debug: Erkunde Party Classes"""
        print("\n" + "="*80)
        print("DEBUG: PARTY CLASSES")
        print("="*80)
        
        query = """
        MATCH (p:Package {fqn: 'org.apache.ofbiz.party'})
        MATCH (p)-[:CONTAINS]->(c:Class)
        RETURN c.fqn as ClassFQN, keys(c) as Properties
        LIMIT 5
        """
        results = self.execute_query(query)
        
        if results:
            print(f"Gefunden: {len(results)} Klassen")
            for idx, row in enumerate(results, 1):
                print(f"\n{idx}. {row.get('ClassFQN', 'Unknown')}")
                print(f"   Properties: {row.get('Properties', [])}")
        else:
            print("Keine Party Classes gefunden")
        
        return results
    
    def debug_party_methods(self):
        """Debug: Erkunde Party Methods"""
        print("\n" + "="*80)
        print("DEBUG: PARTY METHODS")
        print("="*80)
        
        query = """
        MATCH (p:Package {fqn: 'org.apache.ofbiz.party'})
        MATCH (p)-[:CONTAINS]->(c:Class)
        MATCH (c)-[:DECLARES]->(m:Method)
        RETURN m.name as MethodName, m.signature as Signature, keys(m) as Properties
        LIMIT 5
        """
        results = self.execute_query(query)
        
        if results:
            print(f"Gefunden: {len(results)} Methoden")
            for idx, row in enumerate(results, 1):
                print(f"\n{idx}. {row.get('MethodName', 'Unknown')}")
                print(f"   Signature: {row.get('Signature', 'Unknown')}")
                print(f"   Properties: {row.get('Properties', [])}")
        else:
            print("Keine Party Methods gefunden")
        
        return results
    
    def debug_invocations(self):
        """Debug: Erkunde Invocations zu Party"""
        print("\n" + "="*80)
        print("DEBUG: INVOCATIONS ZU PARTY")
        print("="*80)
        
        query = """
        MATCH (p:Package {fqn: 'org.apache.ofbiz.party'})
        MATCH (p)-[:CONTAINS]->(c:Class)
        MATCH (c)-[:DECLARES]->(m:Method)
        MATCH (caller:Method)-[:INVOKES]->(m)
        RETURN 
          caller.name as CallerName,
          m.name as TargetName,
          COUNT(*) as InvocationCount
        LIMIT 10
        """
        results = self.execute_query(query)
        
        if results:
            print(f"Gefunden: {len(results)} Invocations")
            for idx, row in enumerate(results, 1):
                print(f"\n{idx}. {row.get('CallerName', 'Unknown')} -> {row.get('TargetName', 'Unknown')}")
                print(f"   Count: {row.get('InvocationCount', 0)}")
        else:
            print("Keine Invocations zu Party gefunden")
        
        return results
    
    def debug_all_invocations(self):
        """Debug: Alle Invocations"""
        print("\n" + "="*80)
        print("DEBUG: ALLE INVOCATIONS (SAMPLE)")
        print("="*80)
        
        query = """
        MATCH (caller:Method)-[:INVOKES]->(target:Method)
        RETURN 
          caller.name as CallerName,
          target.name as TargetName,
          COUNT(*) as Count
        LIMIT 10
        """
        results = self.execute_query(query)
        
        if results:
            print(f"Gefunden: {len(results)} Invocation-Paare")
            for idx, row in enumerate(results, 1):
                print(f"\n{idx}. {row.get('CallerName', 'Unknown')} -> {row.get('TargetName', 'Unknown')}")
                print(f"   Count: {row.get('Count', 0)}")
        else:
            print("Keine Invocations gefunden")
        
        return results
    
    def debug_party_subpackages(self):
        """Debug: Party Sub-Packages"""
        print("\n" + "="*80)
        print("DEBUG: PARTY SUB-PACKAGES")
        print("="*80)
        
        query = """
        MATCH (p:Package)
        WHERE p.fqn CONTAINS 'org.apache.ofbiz.party'
        RETURN p.fqn as PackageFQN
        ORDER BY p.fqn
        """
        results = self.execute_query(query)
        
        if results:
            print(f"Gefunden: {len(results)} Party-Packages")
            for row in results:
                print(f"  - {row.get('PackageFQN', 'Unknown')}")
        else:
            print("Keine Party-Packages gefunden")
        
        return results
    
    def debug_party_classes_all(self):
        """Debug: Alle Party Classes"""
        print("\n" + "="*80)
        print("DEBUG: ALLE PARTY CLASSES")
        print("="*80)
        
        query = """
        MATCH (c:Class)
        WHERE c.fqn CONTAINS 'org.apache.ofbiz.party'
        RETURN c.fqn as ClassFQN
        ORDER BY c.fqn
        LIMIT 20
        """
        results = self.execute_query(query)
        
        if results:
            print(f"Gefunden: {len(results)} Party-Classes")
            for idx, row in enumerate(results, 1):
                print(f"  {idx}. {row.get('ClassFQN', 'Unknown')}")
        else:
            print("Keine Party-Classes gefunden")
        
        return results
    
    def debug_invocations_to_party_classes(self):
        """Debug: Invocations zu Party Classes"""
        print("\n" + "="*80)
        print("DEBUG: INVOCATIONS ZU PARTY CLASSES")
        print("="*80)
        
        query = """
        MATCH (caller:Method)-[:INVOKES]->(target:Method)
        MATCH (target_class:Class)-[:DECLARES]->(target)
        WHERE target_class.fqn CONTAINS 'org.apache.ofbiz.party'
        WITH 
          CASE 
            WHEN caller.fqn CONTAINS '.order.' THEN 'Order'
            WHEN caller.fqn CONTAINS '.product.' THEN 'Product'
            WHEN caller.fqn CONTAINS '.accounting.' THEN 'Accounting'
            WHEN caller.fqn CONTAINS '.humanres.' THEN 'HumanRes'
            WHEN caller.fqn CONTAINS '.marketing.' THEN 'Marketing'
            WHEN caller.fqn CONTAINS '.workeffort.' THEN 'WorkEffort'
            WHEN caller.fqn CONTAINS '.content.' THEN 'Content'
            WHEN caller.fqn CONTAINS '.manufacturing.' THEN 'Manufacturing'
            WHEN caller.fqn CONTAINS '.shipment.' THEN 'Shipment'
            WHEN caller.fqn CONTAINS '.catalog.' THEN 'Catalog'
            WHEN caller.fqn CONTAINS '.ecommerce.' THEN 'Ecommerce'
            WHEN caller.fqn CONTAINS '.pos.' THEN 'POS'
            ELSE 'Other'
          END as Module,
          count(*) as CallCount
        RETURN Module, CallCount
        ORDER BY CallCount DESC
        """
        results = self.execute_query(query)
        
        if results:
            print(f"Gefunden: {len(results)} Module mit Aufrufen zu Party")
            total = 0
            for row in results:
                module = row.get('Module', 'Unknown')
                count = row.get('CallCount', 0)
                print(f"  {module:<20} : {count:>10}")
                total += count
            print(f"  {'GESAMT':<20} : {total:>10}")
        else:
            print("Keine Invocations zu Party Classes gefunden")
        
        return results
    
    def run_debug(self):
        """Führe Debug-Exploration durch"""
        print("\n" + "="*80)
        print("NEO4J DEBUG EXPLORATION")
        print("="*80)
        
        self.debug_party_subpackages()
        self.debug_party_classes_all()
        self.debug_party_package()
        self.debug_party_classes()
        self.debug_party_methods()
        self.debug_all_invocations()
        self.debug_invocations()
        self.debug_invocations_to_party_classes()

if __name__ == "__main__":
    explorer = None
    try:
        explorer = DebugExplorer()
        explorer.run_debug()
    finally:
        if explorer:
            explorer.close()
