# Neo4j Query-Sammlung für OFBiz-Analyse

## Übersicht

Diese Sammlung enthält praktische Cypher-Queries zur Analyse der OFBiz-Codebase in Neo4j. Sie können diese Queries verwenden, um:
- Abhängigkeiten zu verstehen
- Refaktorierungskandidaten zu identifizieren
- Fortschritt zu verfolgen
- Neue Probleme zu entdecken

---

## 1. Service-Übersicht

### 1.1 Alle Services auflisten
```cypher
MATCH (s:Type) 
WHERE s.name CONTAINS 'Services'
RETURN s.name as serviceName, s.fqn as fullyQualifiedName
ORDER BY s.name
```

### 1.2 Services nach Abhängigkeitsanzahl sortieren
```cypher
MATCH (s:Type) 
WHERE s.name CONTAINS 'Services'
WITH s
OPTIONAL MATCH (s)-[:DEPENDS_ON]->(dep)
WITH s, COUNT(dep) as outgoing
OPTIONAL MATCH (s)<-[:DEPENDS_ON]-(dependent)
WITH s, outgoing, COUNT(dependent) as incoming
RETURN s.name as serviceName, 
       outgoing as outgoingDependencies, 
       incoming as incomingDependencies,
       (outgoing + incoming) as totalDependencies
ORDER BY totalDependencies DESC
```

### 1.3 Top 10 am stärksten gekoppelte Services
```cypher
MATCH (s:Type) 
WHERE s.name CONTAINS 'Services'
WITH s
OPTIONAL MATCH (s)-[:DEPENDS_ON]->(dep)
WITH s, COUNT(dep) as outgoing
OPTIONAL MATCH (s)<-[:DEPENDS_ON]-(dependent)
WITH s, outgoing, COUNT(dependent) as incoming
RETURN s.name as serviceName, 
       outgoing, 
       incoming,
       (outgoing + incoming) as total
ORDER BY total DESC
LIMIT 10
```

---

## 2. Abhängigkeitsanalyse

### 2.1 Alle Abhängigkeiten eines spezifischen Services
```cypher
MATCH (s:Type {name: 'OrderServices'})-[:DEPENDS_ON]->(dep)
RETURN DISTINCT dep.name as dependency
ORDER BY dep.name
```

### 2.2 Abhängigkeitstiefe (wie viele Ebenen)
```cypher
MATCH (s:Type {name: 'OrderServices'})-[:DEPENDS_ON*1..3]->(dep)
RETURN DISTINCT dep.name as dependency, 
       LENGTH(shortestPath((s)-[:DEPENDS_ON*]->(dep))) as depth
ORDER BY depth, dep.name
```

### 2.3 Abhängigkeitspfade zwischen zwei Services
```cypher
MATCH path = (s1:Type {name: 'OrderServices'})-[:DEPENDS_ON*..5]->(s2:Type {name: 'PaymentGatewayServices'})
RETURN [node IN nodes(path) | node.name] as path,
       LENGTH(path) - 1 as hops
LIMIT 10
```

### 2.4 Gemeinsame Abhängigkeiten zwischen zwei Services
```cypher
MATCH (s1:Type {name: 'OrderServices'})-[:DEPENDS_ON]->(common)<-[:DEPENDS_ON]-(s2:Type {name: 'PaymentGatewayServices'})
RETURN DISTINCT common.name as sharedDependency
ORDER BY common.name
```

### 2.5 Services, die von einem bestimmten Service abhängen
```cypher
MATCH (dependent)-[:DEPENDS_ON]->(s:Type {name: 'PaymentGatewayServices'})
WHERE dependent.name CONTAINS 'Services'
RETURN DISTINCT dependent.name as dependentService
ORDER BY dependent.name
```

---

## 3. Zirkuläre Abhängigkeiten

### 3.1 Alle zirkulären Abhängigkeiten finden
```cypher
MATCH (s1:Type)-[:DEPENDS_ON]->(s2:Type)-[:DEPENDS_ON]->(s1)
WHERE s1.name CONTAINS 'Services' AND s2.name CONTAINS 'Services'
RETURN DISTINCT s1.name as service1, s2.name as service2
ORDER BY s1.name, s2.name
```

### 3.2 Längere Zyklen (3+ Services)
```cypher
MATCH path = (s:Type)-[:DEPENDS_ON*3..5]->(s)
WHERE s.name CONTAINS 'Services'
RETURN [node IN nodes(path)[0..-1] | node.name] as cycle,
       LENGTH(path) as cycleLength
LIMIT 20
```

### 3.3 Zyklen visualisieren (für spezifischen Service)
```cypher
MATCH (s:Type {name: 'LoginServices'})-[:DEPENDS_ON*1..3]->(s)
RETURN [node IN nodes(path) | node.name] as cycle
```

---

## 4. Package-Analyse

### 4.1 Packages nach Klassenzahl
```cypher
MATCH (p:Package)<-[:BELONGS_TO]-(c:Type)
RETURN p.name as package, COUNT(c) as classCount
ORDER BY classCount DESC
LIMIT 30
```

### 4.2 Abhängigkeiten zwischen Packages
```cypher
MATCH (c1:Type)-[:DEPENDS_ON]->(c2:Type)
WHERE c1.package <> c2.package
RETURN c1.package as fromPackage, 
       c2.package as toPackage, 
       COUNT(*) as dependencyCount
ORDER BY dependencyCount DESC
LIMIT 30
```

### 4.3 Package-Zyklen
```cypher
MATCH (p1:Package)<-[:BELONGS_TO]-(c1:Type)-[:DEPENDS_ON]->(c2:Type)-[:BELONGS_TO]->(p2:Package)-[:DEPENDS_ON*]->(p1)
RETURN DISTINCT p1.name as package1, p2.name as package2
LIMIT 20
```

---

## 5. Refaktorierungs-Kandidaten

### 5.1 Services ohne eingehende Abhängigkeiten (Kandidaten für Microservices)
```cypher
MATCH (s:Type) 
WHERE s.name CONTAINS 'Services'
WITH s
OPTIONAL MATCH (s)<-[:DEPENDS_ON]-(dependent)
WITH s, COUNT(dependent) as incomingDeps
WHERE incomingDeps = 0
RETURN s.name as serviceName, incomingDeps
ORDER BY s.name
```

### 5.2 Services mit nur wenigen Abhängigkeiten (einfach zu isolieren)
```cypher
MATCH (s:Type) 
WHERE s.name CONTAINS 'Services'
WITH s
OPTIONAL MATCH (s)-[:DEPENDS_ON]->(dep)
WITH s, COUNT(dep) as outgoing
WHERE outgoing <= 10
RETURN s.name as serviceName, outgoing as dependencies
ORDER BY outgoing
```

### 5.3 Hub-Services (viele eingehende Abhängigkeiten)
```cypher
MATCH (s:Type) 
WHERE s.name CONTAINS 'Services'
WITH s
OPTIONAL MATCH (s)<-[:DEPENDS_ON]-(dependent)
WITH s, COUNT(dependent) as incoming
WHERE incoming >= 5
RETURN s.name as serviceName, incoming as dependents
ORDER BY incoming DESC
```

### 5.4 Spezialisierte Services (nach Naming-Pattern)
```cypher
MATCH (s:Type) 
WHERE s.name CONTAINS 'Services' 
  AND (s.name CONTAINS 'Payment' 
    OR s.name CONTAINS 'Shipping' 
    OR s.name CONTAINS 'Email'
    OR s.name CONTAINS 'Product')
RETURN s.name as serviceName, s.fqn as fullyQualifiedName
ORDER BY s.name
```

---

## 6. Metriken & Qualität

### 6.1 Instabilität berechnen (Martin Metrics)
```cypher
MATCH (s:Type) 
WHERE s.name CONTAINS 'Services'
WITH s
OPTIONAL MATCH (s)-[:DEPENDS_ON]->(dep)
WITH s, COUNT(dep) as efferent
OPTIONAL MATCH (s)<-[:DEPENDS_ON]-(dependent)
WITH s, efferent, COUNT(dependent) as afferent
RETURN s.name as serviceName, 
       efferent as outgoing, 
       afferent as incoming,
       ROUND(TOFLOAT(efferent) / (efferent + afferent), 2) as instability
ORDER BY instability DESC
LIMIT 20
```

### 6.2 Durchschnittliche Abhängigkeitstiefe
```cypher
MATCH (s:Type) 
WHERE s.name CONTAINS 'Services'
WITH s
OPTIONAL MATCH (s)-[:DEPENDS_ON*1..10]->(dep)
WITH s, COUNT(DISTINCT dep) as reachable
RETURN s.name as serviceName, reachable as reachableDependencies
ORDER BY reachable DESC
LIMIT 20
```

### 6.3 Kohäsion-Analyse (Services in gleichen Packages)
```cypher
MATCH (p:Package)<-[:BELONGS_TO]-(s1:Type)-[:DEPENDS_ON]->(s2:Type)-[:BELONGS_TO]->(p)
WHERE s1.name CONTAINS 'Services' AND s2.name CONTAINS 'Services'
RETURN p.name as package, 
       COUNT(DISTINCT s1) as servicesInPackage,
       COUNT(DISTINCT s1)-[:DEPENDS_ON]->(s2) as internalDependencies
ORDER BY internalDependencies DESC
```

---

## 7. Spezifische Analysen

### 7.1 Payment-Services Ökosystem
```cypher
MATCH (s:Type) 
WHERE s.name CONTAINS 'Payment' OR s.name CONTAINS 'Gateway'
WITH s
OPTIONAL MATCH (s)-[:DEPENDS_ON]->(dep)
WITH s, COUNT(dep) as outgoing
OPTIONAL MATCH (s)<-[:DEPENDS_ON]-(dependent)
WITH s, outgoing, COUNT(dependent) as incoming
RETURN s.name as serviceName, outgoing, incoming
ORDER BY s.name
```

### 7.2 Order-Services Abhängigkeiten
```cypher
MATCH (s:Type {name: 'OrderServices'})-[:DEPENDS_ON]->(dep)
RETURN dep.name as dependency, 
       CASE 
           WHEN dep.name CONTAINS 'Services' THEN 'Service'
           WHEN dep.name CONTAINS 'Worker' THEN 'Worker'
           WHEN dep.name CONTAINS 'Helper' THEN 'Helper'
           WHEN dep.name CONTAINS 'Util' THEN 'Utility'
           ELSE 'Other'
       END as type
ORDER BY type, dep.name
```

### 7.3 Shipping-Provider Integration
```cypher
MATCH (s:Type) 
WHERE s.name CONTAINS 'Services' 
  AND (s.name CONTAINS 'Ups' 
    OR s.name CONTAINS 'Usps' 
    OR s.name CONTAINS 'Fedex' 
    OR s.name CONTAINS 'Dhl')
WITH s
OPTIONAL MATCH (s)-[:DEPENDS_ON]->(dep)
RETURN s.name as provider, dep.name as dependency
ORDER BY s.name, dep.name
```

### 7.4 Third-Party Integrationen
```cypher
MATCH (s:Type) 
WHERE s.fqn CONTAINS 'thirdparty'
  AND s.name CONTAINS 'Services'
RETURN s.name as serviceName, s.fqn as fullyQualifiedName
ORDER BY s.name
```

---

## 8. Refaktorierungs-Fortschritt

### 8.1 Zirkuläre Abhängigkeiten (sollte gegen 0 gehen)
```cypher
MATCH (s1:Type)-[:DEPENDS_ON]->(s2:Type)-[:DEPENDS_ON]->(s1)
WHERE s1.name CONTAINS 'Services' AND s2.name CONTAINS 'Services'
RETURN COUNT(DISTINCT s1) as circularDependencyCount
```

### 8.2 Services mit > 50 Abhängigkeiten (sollte abnehmen)
```cypher
MATCH (s:Type) 
WHERE s.name CONTAINS 'Services'
WITH s
OPTIONAL MATCH (s)-[:DEPENDS_ON]->(dep)
WITH s, COUNT(dep) as outgoing
WHERE outgoing > 50
RETURN COUNT(s) as servicesWithHighCoupling
```

### 8.3 Durchschnittliche Abhängigkeiten pro Service
```cypher
MATCH (s:Type) 
WHERE s.name CONTAINS 'Services'
WITH s
OPTIONAL MATCH (s)-[:DEPENDS_ON]->(dep)
WITH COUNT(dep) as deps
RETURN ROUND(AVG(deps), 2) as averageDependencies
```

### 8.4 Verteilung der Abhängigkeiten
```cypher
MATCH (s:Type) 
WHERE s.name CONTAINS 'Services'
WITH s
OPTIONAL MATCH (s)-[:DEPENDS_ON]->(dep)
WITH s, COUNT(dep) as outgoing
RETURN 
  CASE 
    WHEN outgoing <= 10 THEN '0-10'
    WHEN outgoing <= 20 THEN '11-20'
    WHEN outgoing <= 30 THEN '21-30'
    WHEN outgoing <= 50 THEN '31-50'
    ELSE '50+'
  END as dependencyRange,
  COUNT(s) as serviceCount
ORDER BY dependencyRange
```

---

## 9. Detaillierte Service-Profile

### 9.1 Komplettes Profil eines Services
```cypher
MATCH (s:Type {name: 'OrderServices'})
WITH s
OPTIONAL MATCH (s)-[:DEPENDS_ON]->(outDep)
WITH s, COUNT(DISTINCT outDep) as outgoing, COLLECT(DISTINCT outDep.name) as outgoingDeps
OPTIONAL MATCH (s)<-[:DEPENDS_ON]-(inDep)
WITH s, outgoing, outgoingDeps, COUNT(DISTINCT inDep) as incoming, COLLECT(DISTINCT inDep.name) as incomingDeps
RETURN {
  name: s.name,
  fqn: s.fqn,
  outgoingDependencies: outgoing,
  incomingDependencies: incoming,
  totalCoupling: outgoing + incoming,
  dependsOn: outgoingDeps,
  dependentServices: incomingDeps
} as serviceProfile
```

### 9.2 Abhängigkeitsbaum (bis Tiefe 3)
```cypher
MATCH (s:Type {name: 'OrderServices'})-[:DEPENDS_ON*0..3]->(dep)
RETURN DISTINCT dep.name as dependency,
       LENGTH(shortestPath((s:Type {name: 'OrderServices'})-[:DEPENDS_ON*]->(dep))) as depth
ORDER BY depth, dep.name
```

---

## 10. Vergleichende Analysen

### 10.1 Services vergleichen
```cypher
MATCH (s1:Type {name: 'OrderServices'})
MATCH (s2:Type {name: 'PaymentGatewayServices'})
WITH s1, s2
OPTIONAL MATCH (s1)-[:DEPENDS_ON]->(dep1)
WITH s1, s2, COUNT(dep1) as s1_outgoing
OPTIONAL MATCH (s2)-[:DEPENDS_ON]->(dep2)
WITH s1, s2, s1_outgoing, COUNT(dep2) as s2_outgoing
OPTIONAL MATCH (s1)<-[:DEPENDS_ON]-(in1)
WITH s1, s2, s1_outgoing, s2_outgoing, COUNT(in1) as s1_incoming
OPTIONAL MATCH (s2)<-[:DEPENDS_ON]-(in2)
WITH s1, s2, s1_outgoing, s2_outgoing, s1_incoming, COUNT(in2) as s2_incoming
RETURN {
  service1: s1.name,
  outgoing: s1_outgoing,
  incoming: s1_incoming,
  total: s1_outgoing + s1_incoming
} as service1,
{
  service2: s2.name,
  outgoing: s2_outgoing,
  incoming: s2_incoming,
  total: s2_outgoing + s2_incoming
} as service2
```

### 10.2 Ähnliche Services finden
```cypher
MATCH (s1:Type) 
WHERE s1.name CONTAINS 'Services'
WITH s1
OPTIONAL MATCH (s1)-[:DEPENDS_ON]->(dep1)
WITH s1, COLLECT(dep1.name) as deps1
MATCH (s2:Type) 
WHERE s2.name CONTAINS 'Services' AND s2 <> s1
WITH s1, deps1, s2
OPTIONAL MATCH (s2)-[:DEPENDS_ON]->(dep2)
WITH s1, deps1, s2, COLLECT(dep2.name) as deps2
WITH s1, s2, 
     SIZE([x IN deps1 WHERE x IN deps2]) as commonDeps,
     SIZE(deps1) as s1_deps,
     SIZE(deps2) as s2_deps
WHERE commonDeps > 5
RETURN s1.name as service1, s2.name as service2, commonDeps, s1_deps, s2_deps
ORDER BY commonDeps DESC
LIMIT 20
```

---

## 11. Export & Reporting

### 11.1 Alle Services mit Metriken exportieren
```cypher
MATCH (s:Type) 
WHERE s.name CONTAINS 'Services'
WITH s
OPTIONAL MATCH (s)-[:DEPENDS_ON]->(dep)
WITH s, COUNT(dep) as outgoing
OPTIONAL MATCH (s)<-[:DEPENDS_ON]-(dependent)
WITH s, outgoing, COUNT(dependent) as incoming
RETURN s.name as serviceName,
       s.fqn as fullyQualifiedName,
       outgoing as outgoingDependencies,
       incoming as incomingDependencies,
       (outgoing + incoming) as totalCoupling,
       ROUND(TOFLOAT(outgoing) / (outgoing + incoming), 2) as instability
ORDER BY totalCoupling DESC
```

### 11.2 Abhängigkeitsmatrix (für Spreadsheet)
```cypher
MATCH (s1:Type) 
WHERE s1.name CONTAINS 'Services'
WITH s1
MATCH (s2:Type) 
WHERE s2.name CONTAINS 'Services'
WITH s1, s2
OPTIONAL MATCH (s1)-[:DEPENDS_ON]->(s2)
RETURN s1.name as fromService, 
       s2.name as toService,
       CASE WHEN s2 IS NOT NULL THEN 1 ELSE 0 END as hasDependency
ORDER BY s1.name, s2.name
```

---

## 12. Tipps & Tricks

### Performance-Optimierung
- Verwenden Sie `LIMIT` bei großen Ergebnismengen
- Nutzen Sie `DISTINCT` um Duplikate zu vermeiden
- Verwenden Sie Indizes auf häufig gefilterten Eigenschaften

### Debugging
- Verwenden Sie `EXPLAIN` um Abfragepläne zu sehen
- Nutzen Sie `PROFILE` um Performance zu messen
- Verwenden Sie `RETURN *` um alle Eigenschaften zu sehen

### Häufige Fehler
- ❌ `size()` mit Pattern-Expressions (verwenden Sie `COUNT {}` stattdessen)
- ❌ Zu tiefe Rekursion (verwenden Sie `LIMIT` auf Pfadlänge)
- ❌ Zu viele `OPTIONAL MATCH` (können Performance beeinträchtigen)

---

## 13. Automatisierte Berichte

### Wöchentlicher Bericht
```cypher
// Führen Sie diese Queries wöchentlich aus
MATCH (s:Type) WHERE s.name CONTAINS 'Services'
WITH s
OPTIONAL MATCH (s)-[:DEPENDS_ON]->(dep)
WITH s, COUNT(dep) as outgoing
OPTIONAL MATCH (s)<-[:DEPENDS_ON]-(dependent)
WITH s, outgoing, COUNT(dependent) as incoming
RETURN {
  timestamp: datetime(),
  totalServices: COUNT(s),
  avgCoupling: ROUND(AVG(outgoing + incoming), 2),
  maxCoupling: MAX(outgoing + incoming),
  minCoupling: MIN(outgoing + incoming),
  servicesWithHighCoupling: SIZE([x IN COLLECT(outgoing + incoming) WHERE x > 50])
} as weeklyMetrics
```

---

## Kontakt & Erweiterung

Diese Query-Sammlung kann erweitert werden. Für neue Queries:
1. Dokumentieren Sie den Zweck
2. Testen Sie die Query
3. Fügen Sie Beispiel-Ergebnisse hinzu
4. Aktualisieren Sie diese Datei
