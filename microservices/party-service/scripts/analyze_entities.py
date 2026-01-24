#!/usr/bin/env python3
"""
Analysiert die OFBiz Party Entity-Definitionen aus der XML-Datei.
Extrahiert Tabellennamen, Felder, Primärschlüssel und Beziehungen.
"""

import xml.etree.ElementTree as ET
import sys
from collections import defaultdict

def analyze_entity_xml(xml_file):
    """Parst die XML-Datei und extrahiert Entity-Informationen."""
    tree = ET.parse(xml_file)
    root = tree.getroot()
    
    entities = []
    
    # Kern-Entities für Priorisierung
    core_entities = {
        'Party', 'Person', 'PartyGroup', 
        'ContactMech', 'PostalAddress', 'TelecomNumber', 'EmailAddressVerification',
        'PartyRole', 'PartyRelationship', 'PartyContactMech',
        'RoleType', 'ContactMechType'
    }
    
    for entity in root.findall('.//entity'):
        entity_name = entity.get('entity-name')
        package = entity.get('package-name', '')
        
        # Nur Party-relevante Entities
        if 'party' not in package.lower():
            continue
            
        # Primärschlüssel sammeln
        prim_keys = [pk.get('field') for pk in entity.findall('prim-key')]
        
        # Felder sammeln
        fields = []
        for field in entity.findall('field'):
            field_name = field.get('name')
            field_type = field.get('type')
            fields.append({
                'name': field_name,
                'type': field_type
            })
        
        # Beziehungen sammeln
        relations = []
        for relation in entity.findall('relation'):
            rel_type = relation.get('type')
            rel_entity = relation.get('rel-entity-name')
            fk_name = relation.get('fk-name', '')
            
            # Key-Maps für die Beziehung
            key_maps = []
            for key_map in relation.findall('key-map'):
                field_name = key_map.get('field-name')
                rel_field = key_map.get('rel-field-name', field_name)
                key_maps.append(f"{field_name}->{rel_field}")
            
            relations.append({
                'type': rel_type,
                'entity': rel_entity,
                'fk_name': fk_name,
                'keys': ', '.join(key_maps)
            })
        
        # Priorität bestimmen
        priority = 'CORE' if entity_name in core_entities else 'EXTENDED'
        
        # Kategorie bestimmen
        category = 'Unknown'
        if 'agreement' in package:
            category = 'Agreement'
        elif 'communication' in package:
            category = 'Communication'
        elif 'contact' in package:
            category = 'Contact'
        elif 'party' in package:
            category = 'Party'
        elif 'need' in package:
            category = 'Need'
        
        entities.append({
            'name': entity_name,
            'package': package,
            'category': category,
            'priority': priority,
            'prim_keys': prim_keys,
            'fields': fields,
            'relations': relations
        })
    
    return sorted(entities, key=lambda x: (x['priority'], x['category'], x['name']))

def generate_markdown_report(entities, output_file):
    """Generiert einen Markdown-Report."""
    
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("# OFBiz Party Entities - Analyse\n\n")
        f.write("Automatisch generiert aus `party-entitymodel.xml`\n\n")
        f.write(f"**Anzahl Entities:** {len(entities)}\n\n")
        
        # Statistiken
        core_count = sum(1 for e in entities if e['priority'] == 'CORE')
        extended_count = len(entities) - core_count
        
        f.write("## Statistiken\n\n")
        f.write(f"- **Kern-Entities:** {core_count}\n")
        f.write(f"- **Erweiterte Entities:** {extended_count}\n\n")
        
        # Kategorien
        categories = defaultdict(int)
        for e in entities:
            categories[e['category']] += 1
        
        f.write("### Entities nach Kategorie\n\n")
        for cat, count in sorted(categories.items()):
            f.write(f"- **{cat}:** {count}\n")
        f.write("\n")
        
        # Übersichtstabelle
        f.write("## Übersicht aller Entities\n\n")
        f.write("| Entity | Kategorie | Priorität | Primärschlüssel | Felder | Beziehungen |\n")
        f.write("|--------|-----------|-----------|-----------------|--------|-------------|\n")
        
        for entity in entities:
            pk_str = ', '.join(entity['prim_keys']) if entity['prim_keys'] else '-'
            field_count = len(entity['fields'])
            rel_count = len(entity['relations'])
            
            f.write(f"| {entity['name']} | {entity['category']} | {entity['priority']} | "
                   f"{pk_str} | {field_count} | {rel_count} |\n")
        
        f.write("\n")
        
        # Detaillierte Beschreibung der Kern-Entities
        f.write("## Kern-Entities (Detailliert)\n\n")
        
        for entity in entities:
            if entity['priority'] != 'CORE':
                continue
                
            f.write(f"### {entity['name']}\n\n")
            f.write(f"**Package:** `{entity['package']}`\n\n")
            f.write(f"**Primärschlüssel:** {', '.join(entity['prim_keys'])}\n\n")
            
            # Felder
            f.write("**Felder:**\n\n")
            f.write("| Feldname | Typ |\n")
            f.write("|----------|-----|\n")
            for field in entity['fields']:
                f.write(f"| {field['name']} | {field['type']} |\n")
            f.write("\n")
            
            # Beziehungen
            if entity['relations']:
                f.write("**Beziehungen:**\n\n")
                f.write("| Typ | Ziel-Entity | FK-Name | Key-Mapping |\n")
                f.write("|-----|-------------|---------|-------------|\n")
                for rel in entity['relations']:
                    fk = rel['fk_name'] or '-'
                    keys = rel['keys'] or '-'
                    f.write(f"| {rel['type']} | {rel['entity']} | {fk} | {keys} |\n")
                f.write("\n")
            
            f.write("---\n\n")
        
        # Erweiterte Entities (kompakt)
        f.write("## Erweiterte Entities (Kompakt)\n\n")
        
        current_category = None
        for entity in entities:
            if entity['priority'] != 'EXTENDED':
                continue
            
            if entity['category'] != current_category:
                current_category = entity['category']
                f.write(f"### Kategorie: {current_category}\n\n")
            
            pk_str = ', '.join(entity['prim_keys']) if entity['prim_keys'] else 'keine'
            rel_entities = set(r['entity'] for r in entity['relations'])
            rel_str = ', '.join(sorted(rel_entities)[:5])
            if len(rel_entities) > 5:
                rel_str += f" (+{len(rel_entities)-5} weitere)"
            
            f.write(f"**{entity['name']}**\n")
            f.write(f"- PK: {pk_str}\n")
            f.write(f"- Felder: {len(entity['fields'])}\n")
            f.write(f"- Beziehungen zu: {rel_str if rel_str else 'keine'}\n\n")

def main():
    xml_file = 'applications/datamodel/entitydef/party-entitymodel.xml'
    output_file = 'microservices/party-service/docs/PARTY_ENTITIES_ANALYSIS.md'
    
    print(f"Analysiere {xml_file}...")
    entities = analyze_entity_xml(xml_file)
    
    print(f"Gefunden: {len(entities)} Entities")
    print(f"Generiere Report: {output_file}...")
    
    generate_markdown_report(entities, output_file)
    
    print("✅ Analyse abgeschlossen!")
    print(f"\nKern-Entities: {sum(1 for e in entities if e['priority'] == 'CORE')}")
    print(f"Erweiterte Entities: {sum(1 for e in entities if e['priority'] == 'EXTENDED')}")

if __name__ == '__main__':
    main()
