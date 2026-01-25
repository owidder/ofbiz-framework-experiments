package org.apache.ofbiz.party.microservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * Party Service - Microservice Application
 *
 * <p>Dieser Microservice verwaltet alle Party-bezogenen Entitäten und Operationen,
 * die aus dem OFBiz-Monolithen extrahiert wurden.</p>
 *
 * <h2>Hauptfunktionen:</h2>
 * <ul>
 *   <li>Verwaltung von Parties (Personen, Organisationen, etc.)</li>
 *   <li>Party-Rollen und Beziehungen</li>
 *   <li>Kontaktinformationen (Adressen, Telefonnummern, E-Mails)</li>
 *   <li>Party-Klassifikationen und Attribute</li>
 * </ul>
 *
 * <h2>Technologie-Stack:</h2>
 * <ul>
 *   <li>Spring Boot 3.2.x</li>
 *   <li>Derby (PoC) / PostgreSQL (Production)</li>
 *   <li>Redis (Caching)</li>
 *   <li>Kafka (Event-Streaming)</li>
 *   <li>Flyway (Database Migrations)</li>
 * </ul>
 *
 * <h2>PoC Phase 1:</h2>
 * <p>In der ersten Phase nutzen wir die OFBiz Derby-Datenbank direkt
 * für Read-Only Operationen. Dies ermöglicht einen schnellen PoC ohne
 * Datenmigration.</p>
 *
 * <h3>Wichtige Hinweise für Derby:</h3>
 * <ul>
 *   <li>Derby Embedded Mode erlaubt nur EINE Verbindung</li>
 *   <li>OFBiz muss gestoppt sein, bevor dieser Service startet</li>
 *   <li>Tabellennamen sind UPPERCASE (PARTY, PERSON, etc.)</li>
 * </ul>
 *
 * @author OFBiz Migration Team
 * @version 1.0.0-SNAPSHOT
 * @since 2026-01-11
 */
@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
public class PartyServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(PartyServiceApplication.class);

    /**
     * Main-Methode zum Starten der Spring Boot Application.
     *
     * @param args Kommandozeilen-Argumente
     */
    public static void main(String[] args) {
        SpringApplication.run(PartyServiceApplication.class, args);
    }

    /**
     * CommandLineRunner zum Testen der Datenbankverbindung beim Start.
     *
     * <p>Dieser Runner wird automatisch beim Start der Anwendung ausgeführt
     * und testet die Verbindung zur Derby-Datenbank. Er gibt nützliche
     * Informationen über die verfügbaren Daten aus.</p>
     *
     * @param jdbcTemplate Spring JDBC Template für Datenbankzugriff
     * @return CommandLineRunner Bean
     */
    @Bean
    CommandLineRunner testDatabaseConnection(JdbcTemplate jdbcTemplate) {
        return args -> {
            log.info("\n" + "=".repeat(80));
            log.info("🚀 Party Service Starting - PoC Phase 1");
            log.info("=".repeat(80));
            
            try {
                // Test 1: Verbindung und Anzahl Parties
                log.info("📊 Testing database connection...");
                Integer partyCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM PARTY",
                    Integer.class
                );
                
                log.info("✅ Database connection successful!");
                log.info("📈 Total parties in database: {}", partyCount);
                
                // Test 2: Sample Parties
                log.info("\n📋 Sample Parties:");
                List<Map<String, Object>> parties = jdbcTemplate.queryForList(
                    "SELECT PARTY_ID, PARTY_TYPE_ID, STATUS_ID FROM PARTY FETCH FIRST 5 ROWS ONLY"
                );
                
                parties.forEach(party ->
                    log.info("  - {} (Type: {}, Status: {})",
                        party.get("PARTY_ID"),
                        party.get("PARTY_TYPE_ID"),
                        party.get("STATUS_ID")
                    )
                );
                
                // Test 3: Personen
                log.info("\n👤 Sample Persons:");
                List<Map<String, Object>> persons = jdbcTemplate.queryForList(
                    "SELECT p.PARTY_ID, pe.FIRST_NAME, pe.LAST_NAME " +
                    "FROM PARTY p " +
                    "INNER JOIN PERSON pe ON p.PARTY_ID = pe.PARTY_ID " +
                    "FETCH FIRST 3 ROWS ONLY"
                );
                
                persons.forEach(person ->
                    log.info("  - {} {} (ID: {})",
                        person.get("FIRST_NAME"),
                        person.get("LAST_NAME"),
                        person.get("PARTY_ID")
                    )
                );
                
                // Test 4: Organisationen
                log.info("\n🏢 Sample Organizations:");
                List<Map<String, Object>> groups = jdbcTemplate.queryForList(
                    "SELECT p.PARTY_ID, pg.GROUP_NAME " +
                    "FROM PARTY p " +
                    "INNER JOIN PARTY_GROUP pg ON p.PARTY_ID = pg.PARTY_ID " +
                    "FETCH FIRST 3 ROWS ONLY"
                );
                
                groups.forEach(group ->
                    log.info("  - {} (ID: {})",
                        group.get("GROUP_NAME"),
                        group.get("PARTY_ID")
                    )
                );
                
                // Test 5: Verfügbare Party-Tabellen
                log.info("\n📊 Available Party Tables:");
                List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                    "SELECT TABLENAME FROM SYS.SYSTABLES " +
                    "WHERE TABLETYPE='T' AND TABLENAME LIKE 'PARTY%' " +
                    "ORDER BY TABLENAME FETCH FIRST 10 ROWS ONLY"
                );
                
                tables.forEach(table ->
                    log.info("  - {}", table.get("TABLENAME"))
                );
                
                log.info("\n" + "=".repeat(80));
                log.info("✅ Party Service Ready!");
                log.info("📍 API available at: http://localhost:8081/api/party");
                log.info("📖 Swagger UI: http://localhost:8081/api/party/swagger-ui.html");
                log.info("🔍 Actuator: http://localhost:8081/api/party/actuator");
                log.info("=".repeat(80) + "\n");
                
            } catch (Exception e) {
                log.error("\n" + "=".repeat(80));
                log.error("❌ Database connection failed!");
                log.error("=".repeat(80));
                log.error("Error: {}", e.getMessage());
                log.error("\n⚠️  Troubleshooting:");
                log.error("  1. Is OFBiz stopped? (Derby allows only ONE connection)");
                log.error("  2. Does the Derby database exist?");
                log.error("     Path: /Users/oliverwidder/dev/ofbiz/runtime/data/derby/ofbiz/");
                log.error("  3. Has OFBiz been started at least once to create demo data?");
                log.error("=".repeat(80) + "\n");
                throw e;
            }
        };
    }
}
