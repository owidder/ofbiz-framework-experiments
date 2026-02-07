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
     * @param jdbcTemplate Spring JDBC Template für Datenbankzugriff
     * @return CommandLineRunner Bean
     */
    @Bean
    CommandLineRunner testDatabaseConnection(JdbcTemplate jdbcTemplate) {
        return args -> {
            log.info("=".repeat(80));
            log.info("Party Service Starting - Phase 2 (PostgreSQL)");
            log.info("=".repeat(80));

            try {
                // Test: Verbindung und Anzahl Party Types
                log.info("Testing database connection...");
                Integer partyTypeCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM party_type",
                    Integer.class
                );

                log.info("Database connection successful!");
                log.info("Party types in database: {}", partyTypeCount);

                // Sample Party Types
                log.info("Available Party Types:");
                List<Map<String, Object>> partyTypes = jdbcTemplate.queryForList(
                    "SELECT party_type_id, description FROM party_type LIMIT 5"
                );

                partyTypes.forEach(pt ->
                    log.info("  - {} ({})",
                        pt.get("party_type_id"),
                        pt.get("description")
                    )
                );

                log.info("=".repeat(80));
                log.info("Party Service Ready!");
                log.info("API available at: http://localhost:8081/api/party");
                log.info("Swagger UI: http://localhost:8081/api/party/swagger-ui.html");
                log.info("Actuator: http://localhost:8081/api/party/actuator");
                log.info("=".repeat(80));

            } catch (Exception e) {
                log.error("=".repeat(80));
                log.error("Database connection failed!");
                log.error("Error: {}", e.getMessage());
                log.error("Troubleshooting:");
                log.error("  1. Is PostgreSQL running? (docker compose up -d)");
                log.error("  2. Check connection settings in application-local.yml");
                log.error("=".repeat(80));
                throw e;
            }
        };
    }
}
