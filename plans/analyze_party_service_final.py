#!/usr/bin/env python3
"""
Neo4j Analyse: Party Service Abhängigkeiten - FINAL VERSION
Basierend auf echten Daten aus der jQAssistant-Datenbank
"""

from neo4j import GraphDatabase
import os
from typing import Dict, List, Any

class Neo4jAnalyzer:
    def __init__(self):
        self.uri = os.getenv("NEO4J_URI", "bolt://localhost:7687")
        self.user = os.getenv("NEO4J_USER", "neo4j")
        self.password = os.getenv("NEO4J_PASSWORD", "neo4j")
        self.database = os.getenv("NEO4J_DATABASE", "neo4j")
        
        self.driver = None
        self.results = {}
        
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
    
    def execute_query(self, query: str, description: str) -> List[Dict]:
        """Führe eine Cypher Query aus"""
        print(f"\n{'='*80}")
        print(f"Query: {description}")
        print(f"{'='*80}")
        
        try:
            with self.driver.session(database=self.database) as session:
                result = session.run(query)
                data = [dict(record) for record in result]
                print(f"✓ {len(data)} Ergebnisse gefunden")
                return data
        except Exception as e:
            print(f"✗ Fehler: {str(e)}")
            return []
    
    def analyze_party_dependencies(self):
        """Analysiere Party Service Abhängigkeiten nach Modul"""
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
            WHEN caller.fqn CONTAINS '.party.' THEN 'Party'
            ELSE 'Other'
          END as Module,
          count(*) as CallCount
        RETURN Module, CallCount
        ORDER BY CallCount DESC
        """
        
        results = self.execute_query(query, "Party Service Abhängigkeiten nach Modul")
        self.results['module_overview'] = results
        
        # Tabelle ausgeben
        if results:
            print("\n" + "="*50)
            print(f"{'Module':<20} | {'CallCount':>10}")
            print("="*50)
            total = 0
            for row in results:
                module = row.get('Module', 'Unknown')
                count = row.get('CallCount', 0)
                print(f"{module:<20} | {count:>10}")
                total += count
            print("="*50)
            print(f"{'GESAMT':<20} | {total:>10}")
            print("="*50)
        
        return results
    
    def analyze_complexity(self):
        """Analysiere Komplexität pro Modul"""
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
            WHEN caller.fqn CONTAINS '.party.' THEN 'Party'
            ELSE 'Other'
          END as Module,
          count(DISTINCT caller.name) as UniqueCallers,
          count(*) as TotalCalls
        RETURN Module, UniqueCallers, TotalCalls, 
               ROUND(TOFLOAT(TotalCalls) / UniqueCallers, 2) as AvgCallsPerMethod
        ORDER BY TotalCalls DESC
        """
        
        results = self.execute_query(query, "Komplexität pro Modul")
        self.results['complexity'] = results
        
        # Tabelle ausgeben
        if results:
            print("\n" + "="*90)
            print(f"{'Module':<20} | {'UniqueCallers':>15} | {'TotalCalls':>12} | {'AvgCalls':>10}")
            print("="*90)
            for row in results:
                module = row.get('Module', 'Unknown')
                unique = row.get('UniqueCallers', 0)
                total = row.get('TotalCalls', 0)
                avg = row.get('AvgCallsPerMethod', 0)
                print(f"{module:<20} | {unique:>15} | {total:>12} | {avg:>10}")
            print("="*90)
        
        return results
    
    def analyze_critical_methods(self):
        """Analysiere Top 20 kritische Party Service Methoden"""
        query = """
        MATCH (caller:Method)-[:INVOKES]->(target:Method)
        MATCH (target_class:Class)-[:DECLARES]->(target)
        WHERE target_class.fqn CONTAINS 'org.apache.ofbiz.party'
        WITH target.name as TargetMethod, target_class.fqn as TargetClass, count(*) as IncomingCalls
        RETURN TargetClass + '.' + TargetMethod as FullMethod, IncomingCalls
        ORDER BY IncomingCalls DESC
        LIMIT 20
        """
        
        results = self.execute_query(query, "Top 20 kritische Party Service Methoden")
        self.results['critical_methods'] = results
        
        # Tabelle ausgeben
        if results:
            print("\n" + "="*90)
            print(f"{'Rank':<5} | {'TargetMethod':<70} | {'IncomingCalls':>10}")
            print("="*90)
            for idx, row in enumerate(results, 1):
                method = row.get('FullMethod', 'Unknown')
                if method is None:
                    method = 'Unknown'
                method = method[:70]
                calls = row.get('IncomingCalls', 0)
                print(f"{idx:<5} | {method:<70} | {calls:>10}")
            print("="*90)
        
        return results
    
    def analyze_caller_classes(self):
        """Analysiere Top 30 Klassen, die Party Service aufrufen"""
        query = """
        MATCH (caller:Method)-[:INVOKES]->(target:Method)
        MATCH (target_class:Class)-[:DECLARES]->(target)
        MATCH (caller_class:Class)-[:DECLARES]->(caller)
        WHERE target_class.fqn CONTAINS 'org.apache.ofbiz.party'
        WITH caller_class.fqn as CallerClass, count(*) as CallCount
        WHERE CallerClass IS NOT NULL
        RETURN CallerClass, CallCount
        ORDER BY CallCount DESC
        LIMIT 30
        """
        
        results = self.execute_query(query, "Top 30 Klassen mit Party Service Aufrufen")
        self.results['caller_classes'] = results
        
        # Tabelle ausgeben
        if results:
            print("\n" + "="*90)
            print(f"{'Rank':<5} | {'CallerClass':<70} | {'CallCount':>10}")
            print("="*90)
            for idx, row in enumerate(results, 1):
                cls = row.get('CallerClass', 'Unknown')[:70]
                count = row.get('CallCount', 0)
                print(f"{idx:<5} | {cls:<70} | {count:>10}")
            print("="*90)
        
        return results
    
    def analyze_party_classes(self):
        """Analysiere Party Service Klassen"""
        query = """
        MATCH (c:Class)
        WHERE c.fqn CONTAINS 'org.apache.ofbiz.party'
        RETURN c.fqn as ClassFQN
        ORDER BY c.fqn
        """
        
        results = self.execute_query(query, "Party Service Klassen")
        self.results['party_classes'] = results
        
        # Tabelle ausgeben
        if results:
            print("\n" + "="*90)
            print(f"{'Rank':<5} | {'ClassFQN':<80}")
            print("="*90)
            for idx, row in enumerate(results, 1):
                cls = row.get('ClassFQN', 'Unknown')[:80]
                print(f"{idx:<5} | {cls:<80}")
            print("="*90)
        
        return results
    
    def analyze_party_packages(self):
        """Analysiere Party Service Packages"""
        query = """
        MATCH (p:Package)
        WHERE p.fqn CONTAINS 'org.apache.ofbiz.party'
        RETURN p.fqn as PackageFQN
        ORDER BY p.fqn
        """
        
        results = self.execute_query(query, "Party Service Packages")
        self.results['party_packages'] = results
        
        # Tabelle ausgeben
        if results:
            print("\n" + "="*90)
            print(f"{'Rank':<5} | {'PackageFQN':<80}")
            print("="*90)
            for idx, row in enumerate(results, 1):
                pkg = row.get('PackageFQN', 'Unknown')[:80]
                print(f"{idx:<5} | {pkg:<80}")
            print("="*90)
        
        return results
    
    def analyze_caller_packages(self):
        """Analysiere Packages, die Party Service aufrufen"""
        query = """
        MATCH (caller:Method)-[:INVOKES]->(target:Method)
        MATCH (target_class:Class)-[:DECLARES]->(target)
        WHERE target_class.fqn CONTAINS 'org.apache.ofbiz.party'
        WITH 
          CASE 
            WHEN caller.fqn CONTAINS '.order.' THEN 'org.apache.ofbiz.order'
            WHEN caller.fqn CONTAINS '.product.' THEN 'org.apache.ofbiz.product'
            WHEN caller.fqn CONTAINS '.accounting.' THEN 'org.apache.ofbiz.accounting'
            WHEN caller.fqn CONTAINS '.humanres.' THEN 'org.apache.ofbiz.humanres'
            WHEN caller.fqn CONTAINS '.marketing.' THEN 'org.apache.ofbiz.marketing'
            WHEN caller.fqn CONTAINS '.workeffort.' THEN 'org.apache.ofbiz.workeffort'
            WHEN caller.fqn CONTAINS '.content.' THEN 'org.apache.ofbiz.content'
            WHEN caller.fqn CONTAINS '.manufacturing.' THEN 'org.apache.ofbiz.manufacturing'
            WHEN caller.fqn CONTAINS '.shipment.' THEN 'org.apache.ofbiz.shipment'
            WHEN caller.fqn CONTAINS '.catalog.' THEN 'org.apache.ofbiz.catalog'
            WHEN caller.fqn CONTAINS '.ecommerce.' THEN 'org.apache.ofbiz.ecommerce'
            WHEN caller.fqn CONTAINS '.pos.' THEN 'org.apache.ofbiz.pos'
            WHEN caller.fqn CONTAINS '.party.' THEN 'org.apache.ofbiz.party'
            ELSE 'Other'
          END as Package,
          count(DISTINCT caller.name) as UniqueCallers,
          count(*) as TotalCalls
        RETURN Package, UniqueCallers, TotalCalls
        ORDER BY TotalCalls DESC
        """
        
        results = self.execute_query(query, "Packages mit Party Service Aufrufen")
        self.results['caller_packages'] = results
        
        # Tabelle ausgeben
        if results:
            print("\n" + "="*90)
            print(f"{'Package':<40} | {'UniqueCallers':>15} | {'TotalCalls':>15}")
            print("="*90)
            for row in results:
                pkg = row.get('Package', 'Unknown')[:40]
                unique = row.get('UniqueCallers', 0)
                total = row.get('TotalCalls', 0)
                print(f"{pkg:<40} | {unique:>15} | {total:>15}")
            print("="*90)
        
        return results
    
    def calculate_effort(self):
        """Berechne Aufwandsschätzung basierend auf Komplexität"""
        print("\n" + "="*80)
        print("AUFWANDSSCHÄTZUNG PRO MODUL")
        print("="*80)
        
        if not self.results.get('complexity'):
            print("Keine Komplexitätsdaten verfügbar")
            return
        
        print(f"\n{'Module':<20} | {'Aufwand (Wochen)':>20} | {'Aufwand (Monate)':>20}")
        print("="*80)
        
        total_effort = 0
        for row in self.results['complexity']:
            module = row.get('Module', 'Unknown')
            unique_callers = row.get('UniqueCallers', 0)
            total_calls = row.get('TotalCalls', 0)
            
            # Formel: Aufwand = (UniqueCallers × 0.5) + (TotalCalls × 0.1) + 2
            effort_weeks = (unique_callers * 0.5) + (total_calls * 0.1) + 2
            effort_months = effort_weeks / 4.33
            total_effort += effort_weeks
            
            print(f"{module:<20} | {effort_weeks:>20.1f} | {effort_months:>20.1f}")
        
        print("="*80)
        print(f"{'GESAMT':<20} | {total_effort:>20.1f} | {total_effort/4.33:>20.1f}")
        print("="*80)
        
        self.results['total_effort'] = total_effort
    
    def generate_report(self):
        """Generiere einen Bericht mit allen Ergebnissen"""
        report = []
        report.append("# Neo4j Analyse: Party Service Abhängigkeiten - ECHTE DATEN\n\n")
        report.append("## Zusammenfassung\n\n")
        
        if self.results.get('party_classes'):
            total_classes = len(self.results['party_classes'])
            report.append(f"**Gesamt Party Service Klassen:** {total_classes}\n\n")
        
        if self.results.get('party_packages'):
            total_packages = len(self.results['party_packages'])
            report.append(f"**Gesamt Party Service Packages:** {total_packages}\n\n")
        
        if self.results.get('module_overview'):
            report.append("### Modul-Übersicht (Aufrufe zu Party Service)\n\n")
            report.append("| Module | CallCount |\n")
            report.append("|--------|----------|\n")
            total = 0
            for row in self.results['module_overview']:
                module = row.get('Module', 'Unknown')
                count = row.get('CallCount', 0)
                report.append(f"| {module} | {count} |\n")
                total += count
            report.append(f"| **GESAMT** | **{total}** |\n\n")
        
        if self.results.get('complexity'):
            report.append("### Komplexität pro Modul\n\n")
            report.append("| Module | UniqueCallers | TotalCalls | AvgCallsPerMethod |\n")
            report.append("|--------|---------------|------------|-------------------|\n")
            for row in self.results['complexity']:
                module = row.get('Module', 'Unknown')
                unique = row.get('UniqueCallers', 0)
                total = row.get('TotalCalls', 0)
                avg = row.get('AvgCallsPerMethod', 0)
                report.append(f"| {module} | {unique} | {total} | {avg} |\n")
            report.append("\n")
        
        if self.results.get('critical_methods'):
            report.append("### Top 20 kritische Party Service Methoden\n\n")
            report.append("| Rank | TargetMethod | IncomingCalls |\n")
            report.append("|------|--------------|---------------|\n")
            for idx, row in enumerate(self.results['critical_methods'], 1):
                method = row.get('FullMethod', 'Unknown')
                calls = row.get('IncomingCalls', 0)
                report.append(f"| {idx} | `{method}` | {calls} |\n")
            report.append("\n")
        
        if self.results.get('caller_classes'):
            report.append("### Top 30 Klassen mit Party Service Aufrufen\n\n")
            report.append("| Rank | Class | CallCount |\n")
            report.append("|------|-------|----------|\n")
            for idx, row in enumerate(self.results['caller_classes'], 1):
                cls = row.get('CallerClass', 'Unknown')
                count = row.get('CallCount', 0)
                report.append(f"| {idx} | `{cls}` | {count} |\n")
            report.append("\n")
        
        if self.results.get('party_classes'):
            report.append("### Party Service Klassen\n\n")
            report.append("| Rank | Class |\n")
            report.append("|------|-------|\n")
            for idx, row in enumerate(self.results['party_classes'], 1):
                cls = row.get('ClassFQN', 'Unknown')
                report.append(f"| {idx} | `{cls}` |\n")
            report.append("\n")
        
        if self.results.get('caller_packages'):
            report.append("### Packages mit Party Service Aufrufen\n\n")
            report.append("| Package | UniqueCallers | TotalCalls |\n")
            report.append("|---------|---------------|----------|\n")
            for row in self.results['caller_packages']:
                pkg = row.get('Package', 'Unknown')
                unique = row.get('UniqueCallers', 0)
                total = row.get('TotalCalls', 0)
                report.append(f"| {pkg} | {unique} | {total} |\n")
            report.append("\n")
        
        return "".join(report)
    
    def run_analysis(self):
        """Führe vollständige Analyse durch"""
        print("\n" + "="*80)
        print("NEO4J ANALYSE: PARTY SERVICE ABHÄNGIGKEITEN")
        print("="*80)
        
        self.analyze_party_packages()
        self.analyze_party_classes()
        self.analyze_party_dependencies()
        self.analyze_complexity()
        self.analyze_critical_methods()
        self.analyze_caller_classes()
        self.analyze_caller_packages()
        self.calculate_effort()
        
        # Generiere Bericht
        report = self.generate_report()
        
        # Speichere Bericht
        with open("plans/neo4j-analysis-results.md", "w") as f:
            f.write(report)
        
        print("\n✓ Bericht gespeichert in: plans/neo4j-analysis-results.md")
        
        return self.results

if __name__ == "__main__":
    analyzer = None
    try:
        analyzer = Neo4jAnalyzer()
        results = analyzer.run_analysis()
        
        print("\n" + "="*80)
        print("ANALYSE ABGESCHLOSSEN")
        print("="*80)
        print(f"\nErgebnisse:")
        print(f"- Module analysiert: {len(results.get('module_overview', []))}")
        print(f"- Kritische Methoden: {len(results.get('critical_methods', []))}")
        print(f"- Caller-Klassen analysiert: {len(results.get('caller_classes', []))}")
        print(f"- Party-Klassen: {len(results.get('party_classes', []))}")
        print(f"- Party-Packages: {len(results.get('party_packages', []))}")
        print(f"- Caller-Packages: {len(results.get('caller_packages', []))}")
        print(f"- Gesamtaufwand: {results.get('total_effort', 0):.1f} Wochen")
    finally:
        if analyzer:
            analyzer.close()
