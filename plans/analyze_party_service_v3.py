#!/usr/bin/env python3
"""
Neo4j Analyse: Party Service Abhängigkeiten - Version 3
Korrekte Queries basierend auf der Datenbankstruktur
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
        WHERE target.fqn IS NOT NULL
        AND (target.fqn CONTAINS '.party.' OR target.fqn CONTAINS '.party.party.')
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
        WHERE target.fqn IS NOT NULL
        AND (target.fqn CONTAINS '.party.' OR target.fqn CONTAINS '.party.party.')
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
          count(DISTINCT caller.fqn) as UniqueCallers,
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
        MATCH (method:Method)-[:INVOKES]->(target:Method)
        WHERE target.fqn IS NOT NULL
        AND (target.fqn CONTAINS '.party.' OR target.fqn CONTAINS '.party.party.')
        WITH target.fqn as TargetMethod, count(*) as IncomingCalls
        RETURN TargetMethod, IncomingCalls
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
                method = row.get('TargetMethod', 'Unknown')[:70]
                calls = row.get('IncomingCalls', 0)
                print(f"{idx:<5} | {method:<70} | {calls:>10}")
            print("="*90)
        
        return results
    
    def analyze_files(self):
        """Analysiere Dateien mit Party Service Aufrufen"""
        query = """
        MATCH (caller:Method)-[:INVOKES]->(target:Method)
        WHERE target.fqn IS NOT NULL
        AND (target.fqn CONTAINS '.party.' OR target.fqn CONTAINS '.party.party.')
        WITH caller.sourceFileName as File, count(*) as CallCount
        WHERE File IS NOT NULL
        RETURN File, CallCount
        ORDER BY CallCount DESC
        LIMIT 30
        """
        
        results = self.execute_query(query, "Top 30 Dateien mit Party Service Aufrufen")
        self.results['files'] = results
        
        # Tabelle ausgeben
        if results:
            print("\n" + "="*90)
            print(f"{'Rank':<5} | {'File':<70} | {'CallCount':>10}")
            print("="*90)
            for idx, row in enumerate(results, 1):
                file = row.get('File', 'Unknown')[:70]
                count = row.get('CallCount', 0)
                print(f"{idx:<5} | {file:<70} | {count:>10}")
            print("="*90)
        
        return results
    
    def analyze_classes(self):
        """Analysiere Klassen mit Party Service Aufrufen"""
        query = """
        MATCH (caller:Class)-[:DECLARES]->(m:Method)-[:INVOKES]->(target:Method)
        WHERE target.fqn IS NOT NULL
        AND (target.fqn CONTAINS '.party.' OR target.fqn CONTAINS '.party.party.')
        WITH caller.fqn as CallerClass, count(*) as CallCount
        WHERE CallerClass IS NOT NULL
        RETURN CallerClass, CallCount
        ORDER BY CallCount DESC
        LIMIT 30
        """
        
        results = self.execute_query(query, "Top 30 Klassen mit Party Service Aufrufen")
        self.results['classes'] = results
        
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
    
    def analyze_packages(self):
        """Analysiere Packages mit Party Service Aufrufen"""
        query = """
        MATCH (caller:Method)-[:INVOKES]->(target:Method)
        WHERE target.fqn IS NOT NULL
        AND (target.fqn CONTAINS '.party.' OR target.fqn CONTAINS '.party.party.')
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
            ELSE 'Other'
          END as Package,
          count(DISTINCT caller.fqn) as UniqueCallers,
          count(*) as TotalCalls
        RETURN Package, UniqueCallers, TotalCalls
        ORDER BY TotalCalls DESC
        """
        
        results = self.execute_query(query, "Packages mit Party Service Aufrufen")
        self.results['packages'] = results
        
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
    
    def analyze_party_methods_count(self):
        """Analysiere Anzahl Party Service Methoden"""
        query = """
        MATCH (m:Method)
        WHERE m.fqn IS NOT NULL
        AND (m.fqn CONTAINS '.party.' OR m.fqn CONTAINS '.party.party.')
        RETURN COUNT(m) as TotalPartyMethods
        """
        
        results = self.execute_query(query, "Anzahl Party Service Methoden")
        self.results['party_methods_count'] = results
        
        if results:
            total = results[0].get('TotalPartyMethods', 0)
            print(f"\nGesamt Party Service Methoden: {total}")
        
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
        
        if self.results.get('party_methods_count'):
            total_methods = self.results['party_methods_count'][0].get('TotalPartyMethods', 0)
            report.append(f"**Gesamt Party Service Methoden:** {total_methods}\n\n")
        
        if self.results.get('module_overview'):
            report.append("### Modul-Übersicht\n\n")
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
                method = row.get('TargetMethod', 'Unknown')
                calls = row.get('IncomingCalls', 0)
                report.append(f"| {idx} | `{method}` | {calls} |\n")
            report.append("\n")
        
        if self.results.get('files'):
            report.append("### Top 30 Dateien mit Party Service Aufrufen\n\n")
            report.append("| Rank | File | CallCount |\n")
            report.append("|------|------|----------|\n")
            for idx, row in enumerate(self.results['files'], 1):
                file = row.get('File', 'Unknown')
                count = row.get('CallCount', 0)
                report.append(f"| {idx} | `{file}` | {count} |\n")
            report.append("\n")
        
        if self.results.get('classes'):
            report.append("### Top 30 Klassen mit Party Service Aufrufen\n\n")
            report.append("| Rank | Class | CallCount |\n")
            report.append("|------|-------|----------|\n")
            for idx, row in enumerate(self.results['classes'], 1):
                cls = row.get('CallerClass', 'Unknown')
                count = row.get('CallCount', 0)
                report.append(f"| {idx} | `{cls}` | {count} |\n")
            report.append("\n")
        
        if self.results.get('packages'):
            report.append("### Packages mit Party Service Aufrufen\n\n")
            report.append("| Package | UniqueCallers | TotalCalls |\n")
            report.append("|---------|---------------|----------|\n")
            for row in self.results['packages']:
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
        
        self.analyze_party_methods_count()
        self.analyze_party_dependencies()
        self.analyze_complexity()
        self.analyze_critical_methods()
        self.analyze_files()
        self.analyze_classes()
        self.analyze_packages()
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
        print(f"- Dateien analysiert: {len(results.get('files', []))}")
        print(f"- Klassen analysiert: {len(results.get('classes', []))}")
        print(f"- Packages analysiert: {len(results.get('packages', []))}")
        print(f"- Gesamtaufwand: {results.get('total_effort', 0):.1f} Wochen")
    finally:
        if analyzer:
            analyzer.close()
