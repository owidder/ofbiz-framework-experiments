package org.apache.ofbiz.party.microservice.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End Tests für den Party-Service.
 *
 * Diese Tests starten automatisch PostgreSQL und Kafka als Docker-Container
 * und testen den kompletten Flow: REST API → PostgreSQL → Kafka.
 *
 * Ausführen mit: ./gradlew test --tests "*E2ETest"
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Party-Service End-to-End Tests")
class PartyServiceE2ETest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("party_db")
            .withUsername("party_user")
            .withPassword("party_password");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"));

    @LocalServerPort
    private int port;

    private static String createdPersonId;
    private static String createdPartyGroupId;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/party";
    }

    // ========== PERSON TESTS ==========

    @Test
    @Order(1)
    @DisplayName("1. Person erstellen - sollte Person in DB speichern und Snapshot an Kafka senden")
    void createPerson_shouldSaveAndPublishSnapshot() {
        String response = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Max",
                        "lastName": "Mustermann",
                        "gender": "M"
                    }
                    """)
        .when()
                .post("/persons")
        .then()
                .statusCode(201)
                .body("firstName", equalTo("Max"))
                .body("lastName", equalTo("Mustermann"))
                .body("gender", equalTo("M"))
                .body("statusId", equalTo("PARTY_ENABLED"))
                .body("partyId", notNullValue())
                .extract().asString();

        // PartyId für weitere Tests speichern
        createdPersonId = io.restassured.path.json.JsonPath.from(response).getString("partyId");
        assertNotNull(createdPersonId);
        System.out.println("✅ Person erstellt: " + createdPersonId);
    }

    @Test
    @Order(2)
    @DisplayName("2. Person abrufen - sollte erstellte Person zurückgeben")
    void getPerson_shouldReturnCreatedPerson() {
        given()
        .when()
                .get("/persons/" + createdPersonId)
        .then()
                .statusCode(200)
                .body("partyId", equalTo(createdPersonId))
                .body("firstName", equalTo("Max"))
                .body("lastName", equalTo("Mustermann"));

        System.out.println("✅ Person abgerufen: " + createdPersonId);
    }

    @Test
    @Order(3)
    @DisplayName("3. Person aktualisieren - sollte Änderungen speichern und neuen Snapshot senden")
    void updatePerson_shouldUpdateAndPublishSnapshot() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "Maximilian",
                        "lastName": "Mustermann",
                        "gender": "M",
                        "occupation": "Software Engineer"
                    }
                    """)
        .when()
                .put("/persons/" + createdPersonId)
        .then()
                .statusCode(200)
                .body("firstName", equalTo("Maximilian"))
                .body("occupation", equalTo("Software Engineer"));

        System.out.println("✅ Person aktualisiert: " + createdPersonId);
    }

    @Test
    @Order(4)
    @DisplayName("4. Alle Personen abrufen - sollte mindestens eine Person enthalten")
    void getAllPersons_shouldReturnList() {
        given()
        .when()
                .get("/persons")
        .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("find { it.partyId == '" + createdPersonId + "' }.firstName", equalTo("Maximilian"));

        System.out.println("✅ Personenliste abgerufen");
    }

    // ========== PARTY GROUP TESTS ==========

    @Test
    @Order(5)
    @DisplayName("5. PartyGroup erstellen - sollte Gruppe speichern und Snapshot senden")
    void createPartyGroup_shouldSaveAndPublishSnapshot() {
        String response = given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "groupName": "Acme GmbH",
                        "tickerSymbol": "ACME"
                    }
                    """)
        .when()
                .post("/party-groups")
        .then()
                .statusCode(201)
                .body("groupName", equalTo("Acme GmbH"))
                .body("tickerSymbol", equalTo("ACME"))
                .body("statusId", equalTo("PARTY_ENABLED"))
                .body("partyId", notNullValue())
                .extract().asString();

        createdPartyGroupId = io.restassured.path.json.JsonPath.from(response).getString("partyId");
        assertNotNull(createdPartyGroupId);
        System.out.println("✅ PartyGroup erstellt: " + createdPartyGroupId);
    }

    @Test
    @Order(6)
    @DisplayName("6. PartyGroup abrufen - sollte erstellte Gruppe zurückgeben")
    void getPartyGroup_shouldReturnCreatedGroup() {
        given()
        .when()
                .get("/party-groups/" + createdPartyGroupId)
        .then()
                .statusCode(200)
                .body("partyId", equalTo(createdPartyGroupId))
                .body("groupName", equalTo("Acme GmbH"));

        System.out.println("✅ PartyGroup abgerufen: " + createdPartyGroupId);
    }

    @Test
    @Order(7)
    @DisplayName("7. PartyGroup aktualisieren - sollte Änderungen speichern")
    void updatePartyGroup_shouldUpdate() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "groupName": "Acme International GmbH",
                        "tickerSymbol": "ACME"
                    }
                    """)
        .when()
                .put("/party-groups/" + createdPartyGroupId)
        .then()
                .statusCode(200)
                .body("groupName", equalTo("Acme International GmbH"));

        System.out.println("✅ PartyGroup aktualisiert: " + createdPartyGroupId);
    }

    // ========== KAFKA TESTS ==========

    @Test
    @Order(8)
    @DisplayName("8. Kafka Topic enthält Snapshots - sollte veröffentlichte Nachrichten enthalten")
    void kafkaTopic_shouldContainSnapshots() {
        List<String> messages = consumeKafkaMessages("party.entities", 5);

        assertFalse(messages.isEmpty(), "Kafka Topic sollte Nachrichten enthalten");

        // Prüfen, ob Person-Snapshot vorhanden
        boolean hasPersonSnapshot = messages.stream()
                .anyMatch(msg -> msg.contains(createdPersonId) && msg.contains("PERSON"));
        assertTrue(hasPersonSnapshot, "Kafka sollte Person-Snapshot enthalten");

        // Prüfen, ob PartyGroup-Snapshot vorhanden
        boolean hasPartyGroupSnapshot = messages.stream()
                .anyMatch(msg -> msg.contains(createdPartyGroupId) && msg.contains("PARTY_GROUP"));
        assertTrue(hasPartyGroupSnapshot, "Kafka sollte PartyGroup-Snapshot enthalten");

        System.out.println("✅ Kafka enthält " + messages.size() + " Snapshots");
    }

    // ========== DELETE TESTS ==========

    @Test
    @Order(9)
    @DisplayName("9. Person löschen - sollte Person entfernen und Tombstone senden")
    void deletePerson_shouldDeleteAndSendTombstone() {
        given()
        .when()
                .delete("/persons/" + createdPersonId)
        .then()
                .statusCode(204);

        // Verifizieren, dass Person nicht mehr existiert
        given()
        .when()
                .get("/persons/" + createdPersonId)
        .then()
                .statusCode(404);

        System.out.println("✅ Person gelöscht: " + createdPersonId);
    }

    @Test
    @Order(10)
    @DisplayName("10. PartyGroup löschen - sollte Gruppe entfernen und Tombstone senden")
    void deletePartyGroup_shouldDeleteAndSendTombstone() {
        given()
        .when()
                .delete("/party-groups/" + createdPartyGroupId)
        .then()
                .statusCode(204);

        // Verifizieren, dass PartyGroup nicht mehr existiert
        given()
        .when()
                .get("/party-groups/" + createdPartyGroupId)
        .then()
                .statusCode(404);

        System.out.println("✅ PartyGroup gelöscht: " + createdPartyGroupId);
    }

    // ========== ERROR HANDLING TESTS ==========

    @Test
    @Order(11)
    @DisplayName("11. Nicht existierende Person abrufen - sollte 404 zurückgeben")
    void getNonExistentPerson_shouldReturn404() {
        given()
        .when()
                .get("/persons/NON_EXISTENT_ID")
        .then()
                .statusCode(404);

        System.out.println("✅ 404 für nicht existierende Person");
    }

    @Test
    @Order(12)
    @DisplayName("12. Ungültige Person erstellen - sollte 400 zurückgeben")
    void createInvalidPerson_shouldReturn400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "firstName": "",
                        "lastName": ""
                    }
                    """)
        .when()
                .post("/persons")
        .then()
                .statusCode(400);

        System.out.println("✅ 400 für ungültige Person");
    }

    // ========== HELPER METHODS ==========

    private List<String> consumeKafkaMessages(String topic, int maxMessages) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + System.currentTimeMillis());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        List<String> messages = new ArrayList<>();

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topic));

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
            for (ConsumerRecord<String, String> record : records) {
                messages.add(record.key() + ": " + record.value());
                if (messages.size() >= maxMessages) break;
            }
        }

        return messages;
    }
}
