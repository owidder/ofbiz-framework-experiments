#!/usr/bin/env python3
"""
Neo4j Analyse: Party Service Abhängigkeiten
Nutzt den Neo4j MCP Server, um echte Daten aus der OFBiz-Codebase zu holen
"""

import json
import subprocess
import sys
from typing import Dict, List, Any

class Neo4jAnalyzer:
    def __init__(self):
        self.results = {}
    
    def execute_query(self, query: str, description: str) -> List[Dict]:
        """Führe eine Cypher Query aus und gebe Ergebnisse zurück"""
        print(f"\n{'='*80}")
        print(f"Query: {description}")
        print(f"{'='*80}")
        
        request = {
            "jsonrpc": "2.0",
            "id": 1,
            "method": "execute_cypher",
            "params": {
                "query": query,
                "variables": {}
            }
        }
        
        try:
            # Sende Request an Neo4j MCP Server
            result = subprocess.run(
                ["python3", "neo4j_mcp_server.py"],
                input=json.dumps(request),
                capture_output=True,
                text=True,
                timeout=30
            )
            
            if result.returncode == 0:
                response = json.loads(result.stdout)
                if "result" in response:
                    data = response["result"].get("result", [])
                    print(f"✓ {len(data)} Ergebnisse gefunden")
                    return data
                else:
                    print(f"✗ Fehler: {response.get('error', {}).get('message', 'Unbekannter Fehler')}")
                    return []
            else:
                print(f"✗ Fehler beim Ausführen der Query: {result.stderr}")
                return []
        except Exception as e:
            print(f"✗ Exception: {str(e)}")
            return []
    
    def analyze_party_dependencies(self):
        """Analysiere Party Service Abhängigkeiten nach Modul"""
        query = """
        MATCH (caller)-[call:CALLS]->(target)
        WHERE target.package CONTAINS 'party'
        WITH 
          CASE 
            WHEN caller.package CONTAINS 'order' THEN 'Order'
            WHEN caller.package CONTAINS 'product' THEN 'Product'
            WHEN caller.package CONTAINS 'accounting' THEN 'Accounting'
            WHEN caller.package CONTAINS 'humanres' THEN 'HumanRes'
            WHEN caller.package CONTAINS 'marketing' THEN 'Marketing'
            WHEN caller.package CONTAINS 'workeffort' THEN 'WorkEffort'
            WHEN caller.package CONTAINS 'content' THEN 'Content'
            WHEN caller.package CONTAINS 'manufacturing' THEN 'Manufacturing'
            WHEN caller.package CONTAINS 'shipment' THEN 'Shipment'
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
        MATCH (caller)-[call:CALLS]->(target)
        WHERE target.package CONTAINS 'party'
        WITH 
          CASE 
            WHEN caller.package CONTAINS 'order' THEN 'Order'
            WHEN caller.package CONTAINS 'product' THEN 'Product'
            WHEN caller.package CONTAINS 'accounting' THEN 'Accounting'
            WHEN caller.package CONTAINS 'humanres' THEN 'HumanRes'
            WHEN caller.package CONTAINS 'marketing' THEN 'Marketing'
            WHEN caller.package CONTAINS 'workeffort' THEN 'WorkEffort'
            WHEN caller.package CONTAINS 'content' THEN 'Content'
            WHEN caller.package CONTAINS 'manufacturing' THEN 'Manufacturing'
            WHEN caller.package CONTAINS 'shipment' THEN 'Shipment'
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
        MATCH (method)-[call:CALLS]->(target)
        WHERE target.package CONTAINS 'party'
        WITH target.name as TargetMethod, count(*) as IncomingCalls
        RETURN TargetMethod, IncomingCalls
        ORDER BY IncomingCalls DESC
        LIMIT 20
        """
        
        results = self.execute_query(query, "Top 20 kritische Party Service Methoden")
        self.results['critical_methods'] = results
        
        # Tabelle ausgeben
        if results:
            print("\n" + "="*70)
            print(f"{'Rank':<5} | {'TargetMethod':<40} | {'IncomingCalls':>15}")
            print("="*70)
            for idx, row in enumerate(results, 1):
                method = row.get('TargetMethod', 'Unknown')[:40]
                calls = row.get('IncomingCalls', 0)
                print(f"{idx:<5} | {method:<40} | {calls:>15}")
            print("="*70)
        
        return results
    
    def analyze_files(self):
        """Analysiere Dateien mit Party Service Aufrufen"""
        query = """
        MATCH (caller)-[call:CALLS]->(target)
        WHERE target.package CONTAINS 'party'
        RETURN 
          call.file as File,
          count(*) as CallCount
        ORDER BY CallCount DESC
        LIMIT 30
        """
        
        results = self.execute_query(query, "Top 30 Dateien mit Party Service Aufrufen")
        self.results['files'] = results
        
        # Tabelle ausgeben
        if results:
            print("\n" + "="*80)
            print(f"{'Rank':<5} | {'File':<60} | {'CallCount':>10}")
            print("="*80)
            for idx, row in enumerate(results, 1):
                file = row.get('File', 'Unknown')[:60]
                count = row.get('CallCount', 0)
                print(f"{idx:<5} | {file:<60} | {count:>10}")
            print("="*80)
        
        return results
    
    def analyze_call_types(self):
        """Analysiere Aufrufe nach Call-Typ"""
        query = """
        MATCH (caller)-[call:CALLS]->(target)
        WHERE target.package CONTAINS 'party'
        RETURN 
          call.type as CallType,
          count(*) as Count
        ORDER BY Count DESC
        """
        
        results = self.execute_query(query, "Aufrufe nach Call-Typ")
        self.results['call_types'] = results
        
        # Tabelle ausgeben
        if results:
            print("\n" + "="*50)
            print(f"{'CallType':<20} | {'Count':>20}")
            print("="*50)
            for row in results:
                call_type = row.get('CallType', 'Unknown')
                count = row.get('Count', 0)
                print(f"{call_type:<20} | {count:>20}")
            print("="*50)
        
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
        report.append("# Neo4j Analyse: Party Service Abhängigkeiten - ECHTE DATEN\n")
        report.append("## Zusammenfassung\n")
        
        if self.results.get('module_overview'):
            report.append("### Modul-Übersicht\n")
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
            report.append("### Komplexität pro Modul\n")
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
            report.append("### Top 20 kritische Party Service Methoden\n")
            report.append("| Rank | TargetMethod | IncomingCalls |\n")
            report.append("|------|--------------|---------------|\n")
            for idx, row in enumerate(self.results['critical_methods'], 1):
                method = row.get('TargetMethod', 'Unknown')
                calls = row.get('IncomingCalls', 0)
                report.append(f"| {idx} | {method} | {calls} |\n")
            report.append("\n")
        
        return "".join(report)
    
    def run_analysis(self):
        """Führe vollständige Analyse durch"""
        print("\n" + "="*80)
        print("NEO4J ANALYSE: PARTY SERVICE ABHÄNGIGKEITEN")
        print("="*80)
        
        self.analyze_party_dependencies()
        self.analyze_complexity()
        self.analyze_critical_methods()
        self.analyze_files()
        self.analyze_call_types()
        self.calculate_effort()
        
        # Generiere Bericht
        report = self.generate_report()
        
        # Speichere Bericht
        with open("plans/neo4j-analysis-results.md", "w") as f:
            f.write(report)
        
        print("\n✓ Bericht gespeichert in: plans/neo4j-analysis-results.md")
        
        return self.results

if __name__ == "__main__":
    analyzer = Neo4jAnalyzer()
    results = analyzer.run_analysis()
    
    print("\n" + "="*80)
    print("ANALYSE ABGESCHLOSSEN")
    print("="*80)
    print(f"\nErgebnisse:")
    print(f"- Module analysiert: {len(results.get('module_overview', []))}")
    print(f"- Kritische Methoden: {len(results.get('critical_methods', []))}")
    print(f"- Dateien analysiert: {len(results.get('files', []))}")
    print(f"- Gesamtaufwand: {results.get('total_effort', 0):.1f} Wochen")
