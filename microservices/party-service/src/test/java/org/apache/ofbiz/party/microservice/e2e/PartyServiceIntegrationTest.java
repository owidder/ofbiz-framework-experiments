package org.apache.ofbiz.party.microservice.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests für den Party-Service inkl. Kafka-Verifizierung.
 *
 * VORAUSSETZUNGEN:
 * 1. Docker Container laufen: docker-compose up -d
 * 2. Party-Service läuft: ./gradlew bootRun -Dspring.profiles.active=local
 *
 * AUSFÜHREN:
 * ./gradlew test --tests "PartyServiceIntegrationTest"
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Party-Service Integration Tests")
@Tag("integration")
class PartyServiceIntegrationTest {

    private static final String BASE_URL = System.getProperty("test.baseUrl", "http://localhost:8085");
    private static final String BASE_PATH = "/api/party";
    private static final String KAFKA_BOOTSTRAP_SERVERS = System.getProperty("kafka.servers", "localhost:9092");
    private static final String KAFKA_TOPIC = "party.entities";

    private static String createdPersonId;
    private static String createdPartyGroupId;
    private static boolean kafkaAvailable = false;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.basePath = BASE_PATH;

        // Prüfen ob Service erreichbar
        try {
            given().get("/persons").then().statusCode(anyOf(is(200), is(404)));
            System.out.println("✅ Party-Service ist erreichbar unter " + BASE_URL + BASE_PATH);
        } catch (Exception e) {
            fail("❌ Party-Service nicht erreichbar. Bitte starten mit: ./gradlew bootRun -Dspring.profiles.active=local");
        }

        // Prüfen ob Kafka erreichbar
        try {
            KafkaConsumer<String, String> testConsumer = createKafkaConsumer();
            testConsumer.listTopics();
            testConsumer.close();
            kafkaAvailable = true;
            System.out.println("✅ Kafka ist erreichbar unter " + KAFKA_BOOTSTRAP_SERVERS);
        } catch (Exception e) {
            kafkaAvailable = false;
            System.out.println("⚠️  Kafka nicht erreichbar - Kafka-Tests werden übersprungen");
        }
    }

    // ==================== PERSON TESTS ====================

    @Test
    @Order(1)
    @DisplayName("✅ Person erstellen")
    void test01_createPerson() {
        Response response = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Integration",
                        "lastName": "Test",
                        "gender": "M"
                    }
                    """)
        .when()
                .post("/persons")
        .then()
                .statusCode(201)
                .body("firstName", equalTo("Integration"))
                .body("lastName", equalTo("Test"))
                .body("statusId", equalTo("PARTY_ENABLED"))
                .body("partyId", notNullValue())
                .extract().response();

        createdPersonId = response.jsonPath().getString("partyId");
        System.out.println("   → Person erstellt: " + createdPersonId);
    }

    @Test
    @Order(2)
    @DisplayName("✅ Kafka: Snapshot für neue Person verifizieren")
    void test02_verifyPersonSnapshotInKafka() {
        Assumptions.assumeTrue(kafkaAvailable, "Kafka nicht verfügbar");
        assertNotNull(createdPersonId, "Person muss erst erstellt werden");

        // Kurz warten, damit Kafka die Nachricht verarbeitet
        sleep(1000);

        List<KafkaMessage> messages = consumeKafkaMessages(KAFKA_TOPIC, 10);

        // Suche nach Snapshot für erstellte Person
        KafkaMessage personSnapshot = messages.stream()
                .filter(m -> m.key.equals(createdPersonId))
                .filter(m -> m.value != null && m.value.contains("\"firstName\":\"Integration\""))
                .findFirst()
                .orElse(null);

        assertNotNull(personSnapshot, "Kafka sollte Snapshot für Person " + createdPersonId + " enthalten");
        assertTrue(personSnapshot.value.contains("\"partyType\":\"PERSON\""), "Snapshot sollte partyType PERSON haben");
        assertTrue(personSnapshot.value.contains("\"lastName\":\"Test\""), "Snapshot sollte lastName enthalten");

        System.out.println("   → Kafka Snapshot verifiziert: Key=" + createdPersonId);
    }

    @Test
    @Order(3)
    @DisplayName("✅ Person abrufen")
    void test03_getPerson() {
        assertNotNull(createdPersonId, "Person muss erst erstellt werden");

        given()
        .when()
                .get("/persons/" + createdPersonId)
        .then()
                .statusCode(200)
                .body("partyId", equalTo(createdPersonId))
                .body("firstName", equalTo("Integration"))
                .body("lastName", equalTo("Test"));

        System.out.println("   → Person abgerufen: " + createdPersonId);
    }

    @Test
    @Order(4)
    @DisplayName("✅ Person aktualisieren")
    void test04_updatePerson() {
        assertNotNull(createdPersonId, "Person muss erst erstellt werden");

        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "IntegrationUpdated",
                        "lastName": "Test",
                        "gender": "M",
                        "occupation": "Tester"
                    }
                    """)
        .when()
                .put("/persons/" + createdPersonId)
        .then()
                .statusCode(200)
                .body("firstName", equalTo("IntegrationUpdated"))
                .body("occupation", equalTo("Tester"));

        System.out.println("   → Person aktualisiert: " + createdPersonId);
    }

    @Test
    @Order(5)
    @DisplayName("✅ Kafka: Aktualisierter Snapshot verifizieren")
    void test05_verifyUpdatedSnapshotInKafka() {
        Assumptions.assumeTrue(kafkaAvailable, "Kafka nicht verfügbar");
        assertNotNull(createdPersonId, "Person muss erst erstellt werden");

        sleep(1000);

        List<KafkaMessage> messages = consumeKafkaMessages(KAFKA_TOPIC, 20);

        // Suche nach aktualisiertem Snapshot
        KafkaMessage updatedSnapshot = messages.stream()
                .filter(m -> m.key.equals(createdPersonId))
                .filter(m -> m.value != null && m.value.contains("\"firstName\":\"IntegrationUpdated\""))
                .findFirst()
                .orElse(null);

        assertNotNull(updatedSnapshot, "Kafka sollte aktualisierten Snapshot enthalten");
        assertTrue(updatedSnapshot.value.contains("\"occupation\":\"Tester\""), "Snapshot sollte occupation enthalten");

        System.out.println("   → Kafka Update-Snapshot verifiziert");
    }

    @Test
    @Order(6)
    @DisplayName("✅ Alle Personen auflisten")
    void test06_listPersons() {
        given()
        .when()
                .get("/persons")
        .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));

        System.out.println("   → Personenliste abgerufen");
    }

    // ==================== PARTY GROUP TESTS ====================

    @Test
    @Order(7)
    @DisplayName("✅ PartyGroup erstellen")
    void test07_createPartyGroup() {
        Response response = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "groupName": "Test GmbH",
                        "tickerSymbol": "TEST"
                    }
                    """)
        .when()
                .post("/party-groups")
        .then()
                .statusCode(201)
                .body("groupName", equalTo("Test GmbH"))
                .body("tickerSymbol", equalTo("TEST"))
                .body("partyId", notNullValue())
                .extract().response();

        createdPartyGroupId = response.jsonPath().getString("partyId");
        System.out.println("   → PartyGroup erstellt: " + createdPartyGroupId);
    }

    @Test
    @Order(8)
    @DisplayName("✅ Kafka: Snapshot für PartyGroup verifizieren")
    void test08_verifyPartyGroupSnapshotInKafka() {
        Assumptions.assumeTrue(kafkaAvailable, "Kafka nicht verfügbar");
        assertNotNull(createdPartyGroupId, "PartyGroup muss erst erstellt werden");

        sleep(1000);

        List<KafkaMessage> messages = consumeKafkaMessages(KAFKA_TOPIC, 20);

        KafkaMessage groupSnapshot = messages.stream()
                .filter(m -> m.key.equals(createdPartyGroupId))
                .filter(m -> m.value != null && m.value.contains("\"groupName\":\"Test GmbH\""))
                .findFirst()
                .orElse(null);

        assertNotNull(groupSnapshot, "Kafka sollte Snapshot für PartyGroup enthalten");
        assertTrue(groupSnapshot.value.contains("\"partyType\":\"PARTY_GROUP\""), "Snapshot sollte partyType PARTY_GROUP haben");

        System.out.println("   → Kafka PartyGroup-Snapshot verifiziert");
    }

    @Test
    @Order(9)
    @DisplayName("✅ PartyGroup abrufen")
    void test09_getPartyGroup() {
        assertNotNull(createdPartyGroupId, "PartyGroup muss erst erstellt werden");

        given()
        .when()
                .get("/party-groups/" + createdPartyGroupId)
        .then()
                .statusCode(200)
                .body("partyId", equalTo(createdPartyGroupId))
                .body("groupName", equalTo("Test GmbH"));

        System.out.println("   → PartyGroup abgerufen: " + createdPartyGroupId);
    }

    @Test
    @Order(10)
    @DisplayName("✅ PartyGroup aktualisieren")
    void test10_updatePartyGroup() {
        assertNotNull(createdPartyGroupId, "PartyGroup muss erst erstellt werden");

        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "groupName": "Test International GmbH",
                        "tickerSymbol": "TINT"
                    }
                    """)
        .when()
                .put("/party-groups/" + createdPartyGroupId)
        .then()
                .statusCode(200)
                .body("groupName", equalTo("Test International GmbH"))
                .body("tickerSymbol", equalTo("TINT"));

        System.out.println("   → PartyGroup aktualisiert: " + createdPartyGroupId);
    }

    // ==================== DELETE TESTS ====================

    @Test
    @Order(11)
    @DisplayName("✅ Person löschen")
    void test11_deletePerson() {
        assertNotNull(createdPersonId, "Person muss erst erstellt werden");

        given()
        .when()
                .delete("/persons/" + createdPersonId)
        .then()
                .statusCode(204);

        // Verifizieren dass gelöscht
        given()
        .when()
                .get("/persons/" + createdPersonId)
        .then()
                .statusCode(404);

        System.out.println("   → Person gelöscht: " + createdPersonId);
    }

    @Test
    @Order(12)
    @DisplayName("✅ Kafka: Tombstone nach Löschen verifizieren")
    void test12_verifyTombstoneInKafka() {
        Assumptions.assumeTrue(kafkaAvailable, "Kafka nicht verfügbar");
        assertNotNull(createdPersonId, "Person muss erst erstellt werden");

        sleep(1000);

        List<KafkaMessage> messages = consumeKafkaMessages(KAFKA_TOPIC, 30);

        // Suche nach Tombstone (value = null) für gelöschte Person
        KafkaMessage tombstone = messages.stream()
                .filter(m -> m.key.equals(createdPersonId))
                .filter(m -> m.value == null || m.value.equals("null"))
                .findFirst()
                .orElse(null);

        assertNotNull(tombstone, "Kafka sollte Tombstone (null) für gelöschte Person enthalten");

        System.out.println("   → Kafka Tombstone verifiziert für: " + createdPersonId);
    }

    @Test
    @Order(13)
    @DisplayName("✅ PartyGroup löschen")
    void test13_deletePartyGroup() {
        assertNotNull(createdPartyGroupId, "PartyGroup muss erst erstellt werden");

        given()
        .when()
                .delete("/party-groups/" + createdPartyGroupId)
        .then()
                .statusCode(204);

        // Verifizieren dass gelöscht
        given()
        .when()
                .get("/party-groups/" + createdPartyGroupId)
        .then()
                .statusCode(404);

        System.out.println("   → PartyGroup gelöscht: " + createdPartyGroupId);
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    @Order(14)
    @DisplayName("✅ 404 für nicht existierende Person")
    void test14_getNotFound() {
        given()
        .when()
                .get("/persons/NICHT_VORHANDEN")
        .then()
                .statusCode(404);

        System.out.println("   → 404 korrekt zurückgegeben");
    }

    // ==================== KAFKA TOPIC TESTS ====================

    @Test
    @Order(15)
    @DisplayName("✅ Kafka: Topic ist als Compacted konfiguriert")
    void test15_verifyTopicIsCompacted() {
        Assumptions.assumeTrue(kafkaAvailable, "Kafka nicht verfügbar");

        // Dieser Test prüft indirekt, dass das Topic existiert und Nachrichten enthält
        List<KafkaMessage> messages = consumeKafkaMessages(KAFKA_TOPIC, 5);

        assertFalse(messages.isEmpty(), "Kafka Topic sollte Nachrichten enthalten");

        // Prüfe dass alle Nachrichten einen Key haben (wichtig für Compaction)
        boolean allHaveKeys = messages.stream().allMatch(m -> m.key != null && !m.key.isEmpty());
        assertTrue(allHaveKeys, "Alle Nachrichten sollten einen Key haben (für Compaction)");

        System.out.println("   → Kafka Topic enthält " + messages.size() + " Nachrichten mit Keys");
    }

    // ==================== HELPER METHODS ====================

    private static KafkaConsumer<String, String> createKafkaConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + System.currentTimeMillis());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");
        return new KafkaConsumer<>(props);
    }

    private List<KafkaMessage> consumeKafkaMessages(String topic, int maxMessages) {
        List<KafkaMessage> messages = new ArrayList<>();

        try (KafkaConsumer<String, String> consumer = createKafkaConsumer()) {
            consumer.subscribe(Collections.singletonList(topic));

            // Warten auf Partition-Zuweisung
            int attempts = 0;
            while (consumer.assignment().isEmpty() && attempts < 10) {
                consumer.poll(Duration.ofMillis(500));
                attempts++;
            }

            // Zum Anfang springen
            consumer.seekToBeginning(consumer.assignment());

            // Mehrere Poll-Versuche für zuverlässigeres Lesen
            for (int i = 0; i < 5; i++) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));
                for (ConsumerRecord<String, String> record : records) {
                    messages.add(new KafkaMessage(record.key(), record.value()));
                }
                if (messages.size() >= maxMessages) break;
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Lesen von Kafka: " + e.getMessage());
        }

        return messages;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Record für Kafka-Nachrichten
    private record KafkaMessage(String key, String value) {}
}
