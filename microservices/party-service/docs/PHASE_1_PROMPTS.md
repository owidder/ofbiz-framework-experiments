# Phase 1 Prompts - Party-Service PoC Setup

Diese Datei enthält detaillierte Prompts für jeden Schritt von Phase 1 des Party-Service PoC.

## Übersicht Phase 1: Setup (30 Min)

**Ziel:** Grundlegende Infrastruktur für den Zugriff auf die OFBiz Derby-Datenbank einrichten.

**Schritte:**
1. ✅ Spring Boot Projekt ist bereits vorhanden
2. Derby JDBC Driver zu `build.gradle` hinzufügen
3. `application-dev.yml` mit Derby-Konfiguration erstellen
4. Verbindung testen mit einfachem Query

---

## Schritt 1: Spring Boot Projekt prüfen ✅

**Status:** Bereits vorhanden

**Prompt:**
```
Bitte prüfe die bestehende Spring Boot Projektstruktur im Verzeichnis 
/Users/oliverwidder/dev/ofbiz/microservices/party-service

Analysiere:
1. Ist die Gradle-Konfiguration vollständig?
2. Welche Dependencies sind bereits vorhanden?
3. Ist die Clean Architecture Struktur korrekt aufgesetzt?
4. Gibt es bereits Konfigurationsdateien?

Erstelle einen kurzen Bericht über den aktuellen Stand.
```

**Erwartetes Ergebnis:**
- Übersicht über vorhandene Dateien und Struktur
- Liste der bereits konfigurierten Dependencies
- Identifikation fehlender Komponenten

---

## Schritt 2: Derby JDBC Driver hinzufügen

**Kontext:**
OFBiz nutzt Apache Derby als embedded Datenbank. Um darauf zuzugreifen, benötigen wir den Derby JDBC Driver.

**Prompt:**
```
Füge den Apache Derby JDBC Driver zur build.gradle des Party-Service hinzu.

Anforderungen:
1. Verwende die neueste stabile Derby-Version (10.17.x)
2. Füge die Dependency im 'implementation' Scope hinzu
3. Stelle sicher, dass auch der Derby Client Driver verfügbar ist
4. Kommentiere die Dependency mit dem Verwendungszweck

Datei: /Users/oliverwidder/dev/ofbiz/microservices/party-service/build.gradle

Zusätzlich benötigte Dependencies:
- Spring Boot Starter Data JPA (falls noch nicht vorhanden)
- Spring Boot Starter Web (falls noch nicht vorhanden)
- Spring Boot Starter Validation (falls noch nicht vorhanden)

Nach dem Hinzufügen: Führe './gradlew build' aus, um die Dependencies zu laden.
```

**Erwartetes Ergebnis:**
```gradle
dependencies {
    // ... existing dependencies ...
    
    // Apache Derby - OFBiz Database Access
    implementation 'org.apache.derby:derby:10.17.1.0'
    implementation 'org.apache.derby:derbytools:10.17.1.0'
    
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
}
```

**Validierung:**
```bash
cd /Users/oliverwidder/dev/ofbiz/microservices/party-service
./gradlew dependencies | grep derby
```

---

## Schritt 3: application-dev.yml mit Derby-Konfiguration erstellen

**Kontext:**
Wir benötigen eine Konfiguration, die auf die bestehende OFBiz Derby-Datenbank zeigt, ohne das Schema zu verändern.

**Prompt:**
```
Erstelle eine application-dev.yml Konfigurationsdatei für den Party-Service mit folgenden Anforderungen:

Datei: /Users/oliverwidder/dev/ofbiz/microservices/party-service/src/main/resources/application-dev.yml

Konfiguration:
1. Spring Datasource:
   - URL: jdbc:derby:/Users/oliverwidder/dev/ofbiz/runtime/data/derby/ofbiz;create=false
   - Username: ofbiz
   - Password: ofbiz (Standard OFBiz Derby Passwort)
   - Driver: org.apache.derby.jdbc.EmbeddedDriver

2. JPA/Hibernate:
   - Dialect: org.hibernate.dialect.DerbyDialect
   - DDL-Auto: none (WICHTIG: Keine Schema-Änderungen!)
   - Show-SQL: true (für Debugging)
   - Format-SQL: true
   - Naming Strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
     (Derby verwendet UPPERCASE Tabellennamen)

3. Logging:
   - org.hibernate.SQL: DEBUG
   - org.hibernate.type.descriptor.sql.BasicBinder: TRACE
   - org.apache.ofbiz.party: DEBUG

4. Server:
   - Port: 8081 (um Konflikt mit OFBiz auf 8080/8443 zu vermeiden)

5. Spring Profile:
   - Aktives Profil: dev

Zusätzlich: Erstelle auch eine application.yml mit Basis-Konfiguration und 
Verweis auf das dev-Profil als Standard.
```

**Erwartetes Ergebnis:**

`application.yml`:
```yaml
spring:
  application:
    name: party-service
  profiles:
    active: dev

server:
  port: 8081

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

`application-dev.yml`:
```yaml
spring:
  datasource:
    url: jdbc:derby:/Users/oliverwidder/dev/ofbiz/runtime/data/derby/ofbiz;create=false
    username: ofbiz
    password: ofbiz
    driver-class-name: org.apache.derby.jdbc.EmbeddedDriver
    hikari:
      maximum-pool-size: 5
      minimum-idle: 2
      connection-timeout: 30000
      
  jpa:
    database-platform: org.hibernate.dialect.DerbyDialect
    hibernate:
      ddl-auto: none
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    org.apache.ofbiz.party: DEBUG
    org.springframework.jdbc.core: DEBUG
```

**Wichtige Hinweise:**
- Der Derby-Pfad muss auf die tatsächliche OFBiz-Installation zeigen
- `create=false` verhindert, dass eine neue Datenbank erstellt wird
- `ddl-auto: none` verhindert Schema-Änderungen durch Hibernate
- Die PhysicalNamingStrategy ist wichtig, da Derby UPPERCASE Tabellennamen verwendet

---

## Schritt 4: Verbindung testen mit einfachem Query

**Kontext:**
Bevor wir komplexe Entities erstellen, testen wir die Datenbankverbindung mit einem einfachen Query.

**Prompt:**
```
Erstelle einen einfachen Connection-Test für die Derby-Datenbank.

Anforderungen:

1. Erstelle eine Test-Klasse: 
   /Users/oliverwidder/dev/ofbiz/microservices/party-service/src/test/java/org/apache/ofbiz/party/infrastructure/persistence/DerbyConnectionTest.java

2. Der Test soll:
   - Spring Boot Test sein (@SpringBootTest)
   - Das dev-Profil aktivieren (@ActiveProfiles("dev"))
   - Einen JdbcTemplate injizieren
   - Einen einfachen Query ausführen: "SELECT COUNT(*) FROM PARTY"
   - Prüfen, dass die Verbindung funktioniert und Daten vorhanden sind
   - Einen Query ausführen: "SELECT PARTY_ID, PARTY_TYPE_ID, STATUS_ID FROM PARTY FETCH FIRST 5 ROWS ONLY"
   - Die ersten 5 Parties ausgeben

3. Erstelle auch eine Main-Klasse für den Service:
   /Users/oliverwidder/dev/ofbiz/microservices/party-service/src/main/java/org/apache/ofbiz/party/PartyServiceApplication.java

4. Füge einen CommandLineRunner hinzu, der beim Start:
   - Die Datenbankverbindung testet
   - Die Anzahl der Parties ausgibt
   - Beispiel-Parties listet

Nach der Erstellung:
- Führe den Test aus: ./gradlew test --tests DerbyConnectionTest
- Starte die Anwendung: ./gradlew bootRun
- Prüfe die Logs auf erfolgreiche Verbindung
```

**Erwartetes Ergebnis:**

`DerbyConnectionTest.java`:
```java
package org.apache.ofbiz.party.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class DerbyConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldConnectToDerbyDatabase() {
        // Given & When
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM PARTY", 
            Integer.class
        );

        // Then
        assertThat(count).isNotNull();
        assertThat(count).isGreaterThan(0);
        
        System.out.println("✅ Derby connection successful!");
        System.out.println("📊 Total parties in database: " + count);
    }

    @Test
    void shouldQuerySampleParties() {
        // Given & When
        List<Map<String, Object>> parties = jdbcTemplate.queryForList(
            "SELECT PARTY_ID, PARTY_TYPE_ID, STATUS_ID FROM PARTY FETCH FIRST 5 ROWS ONLY"
        );

        // Then
        assertThat(parties).isNotEmpty();
        
        System.out.println("\n📋 Sample Parties:");
        parties.forEach(party -> {
            System.out.printf("  - ID: %s, Type: %s, Status: %s%n",
                party.get("PARTY_ID"),
                party.get("PARTY_TYPE_ID"),
                party.get("STATUS_ID")
            );
        });
    }
}
```

`PartyServiceApplication.java`:
```java
package org.apache.ofbiz.party;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class PartyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartyServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner testConnection(JdbcTemplate jdbcTemplate) {
        return args -> {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🚀 Party Service Starting...");
            System.out.println("=".repeat(60));
            
            try {
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM PARTY", 
                    Integer.class
                );
                
                System.out.println("✅ Database connection successful!");
                System.out.println("📊 Total parties in database: " + count);
                
                var parties = jdbcTemplate.queryForList(
                    "SELECT PARTY_ID, PARTY_TYPE_ID FROM PARTY FETCH FIRST 3 ROWS ONLY"
                );
                
                System.out.println("\n📋 Sample Parties:");
                parties.forEach(party -> 
                    System.out.printf("  - %s (%s)%n", 
                        party.get("PARTY_ID"), 
                        party.get("PARTY_TYPE_ID")
                    )
                );
                
            } catch (Exception e) {
                System.err.println("❌ Database connection failed: " + e.getMessage());
                throw e;
            }
            
            System.out.println("\n" + "=".repeat(60));
        };
    }
}
```

**Validierung:**
```bash
# Test ausführen
cd /Users/oliverwidder/dev/ofbiz/microservices/party-service
./gradlew test --tests DerbyConnectionTest

# Anwendung starten
./gradlew bootRun

# Erwartete Ausgabe:
# ✅ Database connection successful!
# 📊 Total parties in database: 150 (oder ähnlich)
# 📋 Sample Parties:
#   - DemoCustomer (PERSON)
#   - Company (PARTY_GROUP)
#   - ...
```

---

## Troubleshooting

### Problem: "Database not found"

**Lösung:**
```
Prüfe den Derby-Pfad in application-dev.yml:
1. Stelle sicher, dass OFBiz mindestens einmal gestartet wurde
2. Prüfe, ob die Datei existiert:
   ls -la /Users/oliverwidder/dev/ofbiz/runtime/data/derby/ofbiz/

3. Falls nicht vorhanden, starte OFBiz:
   cd /Users/oliverwidder/dev/ofbiz
   ./gradlew ofbiz
```

### Problem: "Table 'PARTY' not found"

**Lösung:**
```
Derby verwendet UPPERCASE Tabellennamen. Prüfe:
1. Ist die PhysicalNamingStrategy korrekt konfiguriert?
2. Verwende UPPERCASE in Queries: SELECT * FROM PARTY (nicht party)
```

### Problem: "Another instance of Derby may have already booted"

**Lösung:**
```
Derby erlaubt nur eine Verbindung im embedded Mode:
1. Stoppe OFBiz: Ctrl+C im Terminal
2. Oder verwende den Derby Network Server (komplexer)
3. Für PoC: Starte Party-Service nur, wenn OFBiz gestoppt ist
```

### Problem: Port 8081 bereits belegt

**Lösung:**
```
Ändere den Port in application.yml:
server:
  port: 8082  # oder einen anderen freien Port
```

---

## Erfolgs-Kriterien für Phase 1

✅ **Schritt 2:** Derby JDBC Driver ist in build.gradle vorhanden  
✅ **Schritt 3:** application-dev.yml ist korrekt konfiguriert  
✅ **Schritt 4:** DerbyConnectionTest läuft erfolgreich durch  
✅ **Schritt 4:** PartyServiceApplication startet ohne Fehler  
✅ **Schritt 4:** Verbindung zur OFBiz Derby-Datenbank funktioniert  
✅ **Schritt 4:** Sample-Queries liefern Daten zurück  

---

## Nächste Schritte

Nach erfolgreichem Abschluss von Phase 1:
- **Phase 2:** JPA Entities für PARTY, PERSON, PARTY_GROUP erstellen
- **Phase 2:** Repository-Layer mit Spring Data JPA implementieren
- **Phase 2:** Integration-Tests für Repository schreiben

---

## Zusätzliche Ressourcen

### OFBiz Derby Schema

Relevante Tabellen für Party-Service:
```sql
-- Haupt-Tabelle
PARTY (PARTY_ID, PARTY_TYPE_ID, STATUS_ID, CREATED_DATE, ...)

-- Typ-spezifische Tabellen
PERSON (PARTY_ID, FIRST_NAME, LAST_NAME, GENDER, ...)
PARTY_GROUP (PARTY_ID, GROUP_NAME, ...)

-- Kontakt-Informationen
PARTY_CONTACT_MECH (PARTY_ID, CONTACT_MECH_ID, FROM_DATE, ...)
CONTACT_MECH (CONTACT_MECH_ID, CONTACT_MECH_TYPE_ID, ...)
POSTAL_ADDRESS (CONTACT_MECH_ID, ADDRESS1, CITY, POSTAL_CODE, ...)
TELECOM_NUMBER (CONTACT_MECH_ID, COUNTRY_CODE, AREA_CODE, CONTACT_NUMBER, ...)
```

### Nützliche Derby-Queries

```sql
-- Alle Tabellen anzeigen
SELECT TABLENAME FROM SYS.SYSTABLES WHERE TABLETYPE='T' ORDER BY TABLENAME;

-- Schema einer Tabelle anzeigen
DESCRIBE PARTY;

-- Anzahl Einträge pro Tabelle
SELECT COUNT(*) FROM PARTY;
SELECT COUNT(*) FROM PERSON;
SELECT COUNT(*) FROM PARTY_GROUP;

-- Demo-Daten finden
SELECT * FROM PARTY WHERE PARTY_ID LIKE 'Demo%';
```

### Neo4j Queries für Analyse

Falls du die OFBiz-Struktur analysieren möchtest:

```cypher
// Party-bezogene Klassen finden
MATCH (c:Class)
WHERE c.fqn STARTS WITH 'org.apache.ofbiz.party'
RETURN c.name, c.fqn
LIMIT 20;

// Party Entity-Klassen finden
MATCH (c:Class)
WHERE c.fqn CONTAINS 'party' AND c.name CONTAINS 'Entity'
RETURN c.name, c.fqn;

// Abhängigkeiten von Party-Modul
MATCH (party:Class)-[:DEPENDS_ON]->(dep:Class)
WHERE party.fqn STARTS WITH 'org.apache.ofbiz.party'
RETURN party.name, dep.name, dep.fqn
LIMIT 50;
```

---

## Dokumentation

Nach Abschluss von Phase 1, aktualisiere:

1. **MIGRATION_STATUS.md:**
   - Phase 1 als abgeschlossen markieren
   - Erkenntnisse dokumentieren
   - Probleme und Lösungen festhalten

2. **POC_PLAN.md:**
   - Checkboxen für Phase 1 abhaken
   - Geschätzte vs. tatsächliche Zeit notieren
   - Offene Fragen beantworten

3. **README.md:**
   - Setup-Anleitung aktualisieren
   - Voraussetzungen dokumentieren
   - Erste Schritte beschreiben

---

## Zeitplan

| Schritt | Geschätzt | Tatsächlich | Status |
|---------|-----------|-------------|--------|
| 1. Projekt prüfen | 5 Min | - | ✅ |
| 2. Derby Driver | 5 Min | - | ⏳ |
| 3. Konfiguration | 10 Min | - | ⏳ |
| 4. Connection Test | 10 Min | - | ⏳ |
| **Gesamt** | **30 Min** | **-** | **⏳** |

---

**Erstellt:** 2026-01-25  
**Autor:** Party-PoC Mode  
**Version:** 1.0
