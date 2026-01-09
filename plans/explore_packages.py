#!/usr/bin/env python3
"""
Neo4j Package Explorer - Erkunde die Package-Struktur
"""

from neo4j import GraphDatabase
import os

class PackageExplorer:
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
    
    def explore_packages(self):
        """Erkunde alle Packages"""
        print("\n" + "="*80)
        print("ALLE PACKAGES")
        print("="*80)
        
        query = """
        MATCH (p:Package)
        RETURN p.fqn as PackageFQN
        ORDER BY p.fqn
        LIMIT 50
        """
        results = self.execute_query(query)
        
        for row in results:
            pkg = row.get('PackageFQN', 'Unknown')
            print(f"  - {pkg}")
        
        return results
    
    def explore_party_packages(self):
        """Erkunde Party-bezogene Packages"""
        print("\n" + "="*80)
        print("PARTY-BEZOGENE PACKAGES")
        print("="*80)
        
        query = """
        MATCH (p:Package)
        WHERE p.fqn CONTAINS 'party'
        RETURN p.fqn as PackageFQN
        ORDER BY p.fqn
        """
        results = self.execute_query(query)
        
        if results:
            for row in results:
                pkg = row.get('PackageFQN', 'Unknown')
                print(f"  - {pkg}")
        else:
            print("Keine Party-Packages gefunden")
        
        return results
    
    def explore_party_classes(self):
        """Erkunde Party-bezogene Klassen"""
        print("\n" + "="*80)
        print("PARTY-BEZOGENE KLASSEN")
        print("="*80)
        
        query = """
        MATCH (c:Class)
        WHERE c.fqn CONTAINS 'party'
        RETURN c.fqn as ClassFQN
        ORDER BY c.fqn
        LIMIT 30
        """
        results = self.execute_query(query)
        
        if results:
            print(f"Gefunden: {len(results)} Klassen")
            for idx, row in enumerate(results, 1):
                cls = row.get('ClassFQN', 'Unknown')
                print(f"  {idx}. {cls}")
        else:
            print("Keine Party-Klassen gefunden")
        
        return results
    
    def explore_party_methods(self):
        """Erkunde Party-bezogene Methoden"""
        print("\n" + "="*80)
        print("PARTY-BEZOGENE METHODEN")
        print("="*80)
        
        query = """
        MATCH (m:Method)
        WHERE m.fqn CONTAINS 'party'
        RETURN m.fqn as MethodFQN
        ORDER BY m.fqn
        LIMIT 30
        """
        results = self.execute_query(query)
        
        if results:
            print(f"Gefunden: {len(results)} Methoden")
            for idx, row in enumerate(results, 1):
                method = row.get('MethodFQN', 'Unknown')
                print(f"  {idx}. {method}")
        else:
            print("Keine Party-Methoden gefunden")
        
        return results
    
    def explore_ofbiz_packages(self):
        """Erkunde OFBiz Packages"""
        print("\n" + "="*80)
        print("OFBIZ PACKAGES")
        print("="*80)
        
        query = """
        MATCH (p:Package)
        WHERE p.fqn CONTAINS 'ofbiz'
        RETURN p.fqn as PackageFQN
        ORDER BY p.fqn
        LIMIT 50
        """
        results = self.execute_query(query)
        
        if results:
            print(f"Gefunden: {len(results)} Packages")
            for idx, row in enumerate(results, 1):
                pkg = row.get('PackageFQN', 'Unknown')
                print(f"  {idx}. {pkg}")
        else:
            print("Keine OFBiz-Packages gefunden")
        
        return results
    
    def explore_method_sample(self):
        """Erkunde Sample-Methoden"""
        print("\n" + "="*80)
        print("SAMPLE METHODEN MIT PROPERTIES")
        print("="*80)
        
        query = """
        MATCH (m:Method)
        RETURN m.fqn as FQN, keys(m) as Properties
        LIMIT 5
        """
        results = self.execute_query(query)
        
        if results:
            for idx, row in enumerate(results, 1):
                fqn = row.get('FQN', 'Unknown')
                props = row.get('Properties', [])
                print(f"\n{idx}. {fqn}")
                print(f"   Properties: {', '.join(props)}")
        
        return results
    
    def explore_class_sample(self):
        """Erkunde Sample-Klassen"""
        print("\n" + "="*80)
        print("SAMPLE KLASSEN MIT PROPERTIES")
        print("="*80)
        
        query = """
        MATCH (c:Class)
        RETURN c.fqn as FQN, keys(c) as Properties
        LIMIT 5
        """
        results = self.execute_query(query)
        
        if results:
            for idx, row in enumerate(results, 1):
                fqn = row.get('FQN', 'Unknown')
                props = row.get('Properties', [])
                print(f"\n{idx}. {fqn}")
                print(f"   Properties: {', '.join(props)}")
        
        return results
    
    def explore_invocations_sample(self):
        """Erkunde Sample-Invocations"""
        print("\n" + "="*80)
        print("SAMPLE INVOCATIONS")
        print("="*80)
        
        query = """
        MATCH (caller:Method)-[rel:INVOKES]->(target:Method)
        RETURN 
          caller.fqn as CallerFQN,
          target.fqn as TargetFQN,
          keys(rel) as RelProperties
        LIMIT 5
        """
        results = self.execute_query(query)
        
        if results:
            for idx, row in enumerate(results, 1):
                caller = row.get('CallerFQN', 'Unknown')
                target = row.get('TargetFQN', 'Unknown')
                props = row.get('RelProperties', [])
                print(f"\n{idx}. {caller}")
                print(f"   --[INVOKES]--> {target}")
                print(f"   Rel Properties: {', '.join(props)}")
        
        return results
    
    def run_exploration(self):
        """Führe vollständige Exploration durch"""
        print("\n" + "="*80)
        print("NEO4J PACKAGE EXPLORATION")
        print("="*80)
        
        self.explore_ofbiz_packages()
        self.explore_party_packages()
        self.explore_party_classes()
        self.explore_party_methods()
        self.explore_method_sample()
        self.explore_class_sample()
        self.explore_invocations_sample()

if __name__ == "__main__":
    explorer = None
    try:
        explorer = PackageExplorer()
        explorer.run_exploration()
    finally:
        if explorer:
            explorer.close()
