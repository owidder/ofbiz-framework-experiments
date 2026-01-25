package org.apache.ofbiz.party.microservice.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Derby Connection Test
 * 
 * <p>Testet die Verbindung zur OFBiz Derby-Datenbank.</p>
 * 
 * <h2>Voraussetzungen:</h2>
 * <ul>
 *   <li>OFBiz muss gestoppt sein (Derby Embedded Mode erlaubt nur eine Verbindung)</li>
 *   <li>Derby-Datenbank muss existieren: /Users/oliverwidder/dev/ofbiz/runtime/data/derby/ofbiz/</li>
 *   <li>OFBiz muss mindestens einmal gestartet worden sein, um Demo-Daten zu laden</li>
 * </ul>
 * 
 * <h2>Ausführung:</h2>
 * <pre>
 * # OFBiz stoppen
 * # Im OFBiz Terminal: Ctrl+C
 * 
 * # Test ausführen
 * ./gradlew :microservices:party-service:test --tests DerbyConnectionTest
 * </pre>
 * 
 * @author Party-PoC Mode
 * @version 1.0.0
 * @since 2026-01-25
 */
@SpringBootTest
@ActiveProfiles("dev")
class DerbyConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Testet die grundlegende Verbindung zur Derby-Datenbank.
     * 
     * <p>Führt einen einfachen COUNT-Query auf der PARTY-Tabelle aus
     * und prüft, dass Daten vorhanden sind.</p>
     */
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
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ Derby connection successful!");
        System.out.println("📊 Total parties in database: " + count);
        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * Testet das Abfragen von Sample-Daten aus der PARTY-Tabelle.
     * 
     * <p>Ruft die ersten 5 Parties ab und gibt deren Details aus.
     * Dies zeigt, dass nicht nur die Verbindung funktioniert, sondern
     * auch Daten korrekt gelesen werden können.</p>
     */
    @Test
    void shouldQuerySampleParties() {
        // Given & When
        List<Map<String, Object>> parties = jdbcTemplate.queryForList(
            "SELECT PARTY_ID, PARTY_TYPE_ID, STATUS_ID FROM PARTY FETCH FIRST 5 ROWS ONLY"
        );

        // Then
        assertThat(parties).isNotEmpty();
        assertThat(parties.size()).isLessThanOrEqualTo(5);
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📋 Sample Parties from OFBiz Derby Database:");
        System.out.println("=".repeat(60));
        
        parties.forEach(party -> {
            System.out.printf("  %-20s | Type: %-15s | Status: %s%n",
                party.get("PARTY_ID"),
                party.get("PARTY_TYPE_ID"),
                party.get("STATUS_ID")
            );
        });
        
        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * Testet das Abfragen von PERSON-Daten.
     * 
     * <p>Joined PARTY und PERSON Tabellen, um vollständige Personen-Informationen
     * zu erhalten. Dies testet komplexere Queries und Joins.</p>
     */
    @Test
    void shouldQueryPersonData() {
        // Given & When
        List<Map<String, Object>> persons = jdbcTemplate.queryForList(
            "SELECT p.PARTY_ID, p.PARTY_TYPE_ID, pe.FIRST_NAME, pe.LAST_NAME " +
            "FROM PARTY p " +
            "INNER JOIN PERSON pe ON p.PARTY_ID = pe.PARTY_ID " +
            "FETCH FIRST 5 ROWS ONLY"
        );

        // Then
        assertThat(persons).isNotEmpty();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("👤 Sample Persons from OFBiz Derby Database:");
        System.out.println("=".repeat(60));
        
        persons.forEach(person -> {
            System.out.printf("  %-20s | %s %s%n",
                person.get("PARTY_ID"),
                person.get("FIRST_NAME"),
                person.get("LAST_NAME")
            );
        });
        
        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * Testet das Abfragen von PARTY_GROUP-Daten.
     * 
     * <p>Joined PARTY und PARTY_GROUP Tabellen, um Organisations-Informationen
     * zu erhalten.</p>
     */
    @Test
    void shouldQueryPartyGroupData() {
        // Given & When
        List<Map<String, Object>> groups = jdbcTemplate.queryForList(
            "SELECT p.PARTY_ID, p.PARTY_TYPE_ID, pg.GROUP_NAME " +
            "FROM PARTY p " +
            "INNER JOIN PARTY_GROUP pg ON p.PARTY_ID = pg.PARTY_ID " +
            "FETCH FIRST 5 ROWS ONLY"
        );

        // Then
        assertThat(groups).isNotEmpty();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🏢 Sample Party Groups from OFBiz Derby Database:");
        System.out.println("=".repeat(60));
        
        groups.forEach(group -> {
            System.out.printf("  %-20s | %s%n",
                group.get("PARTY_ID"),
                group.get("GROUP_NAME")
            );
        });
        
        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * Testet das Abfragen von Tabellen-Metadaten.
     * 
     * <p>Listet alle Tabellen auf, die mit 'PARTY' beginnen.
     * Dies hilft beim Verständnis der Datenbankstruktur.</p>
     */
    @Test
    void shouldListPartyTables() {
        // Given & When
        List<Map<String, Object>> tables = jdbcTemplate.queryForList(
            "SELECT TABLENAME FROM SYS.SYSTABLES " +
            "WHERE TABLETYPE='T' AND TABLENAME LIKE 'PARTY%' " +
            "ORDER BY TABLENAME"
        );

        // Then
        assertThat(tables).isNotEmpty();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📊 Party-related Tables in Derby Database:");
        System.out.println("=".repeat(60));
        
        tables.forEach(table -> {
            System.out.println("  - " + table.get("TABLENAME"));
        });
        
        System.out.println("=".repeat(60));
        System.out.println("Total tables: " + tables.size());
        System.out.println("=".repeat(60) + "\n");
    }
}
