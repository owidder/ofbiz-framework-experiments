package org.apache.ofbiz.party.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

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
 *   <li>PostgreSQL (Primäre Datenbank)</li>
 *   <li>Redis (Caching)</li>
 *   <li>Kafka (Event-Streaming)</li>
 *   <li>Flyway (Database Migrations)</li>
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

    /**
     * Main-Methode zum Starten der Spring Boot Application.
     * 
     * @param args Kommandozeilen-Argumente
     */
    public static void main(String[] args) {
        SpringApplication.run(PartyServiceApplication.class, args);
    }
}
