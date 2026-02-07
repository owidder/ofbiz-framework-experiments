/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 */
package org.apache.ofbiz.party.microservice.integration;

import org.apache.ofbiz.party.microservice.domain.entity.Party;
import org.apache.ofbiz.party.microservice.domain.entity.PartyType;
import org.apache.ofbiz.party.microservice.domain.entity.Person;
import org.apache.ofbiz.party.microservice.domain.repository.PartyRepository;
import org.apache.ofbiz.party.microservice.domain.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@Tag("integration")
class PartyRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("party_test_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.cache.type", () -> "simple");
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
    }

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        personRepository.deleteAll();
        partyRepository.deleteAll();
    }

    @Test
    void shouldCreateAndFindPerson() {
        // Given
        PartyType personType = entityManager.find(PartyType.class, "PERSON");

        Person person = new Person();
        person.setPartyId("TEST001");
        person.setPartyType(personType);
        person.setFirstName("Max");
        person.setLastName("Mustermann");
        person.setBirthDate(LocalDate.of(1990, 1, 15));
        person.setGender("M");
        person.setStatusId("PARTY_ENABLED");

        // When
        personRepository.save(person);

        // Then
        Optional<Person> found = personRepository.findById("TEST001");
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("Max");
        assertThat(found.get().getLastName()).isEqualTo("Mustermann");
        assertThat(found.get().getFullName()).isEqualTo("Max Mustermann");
    }

    @Test
    void shouldSearchPersonByName() {
        // Given
        PartyType personType = entityManager.find(PartyType.class, "PERSON");

        Person person1 = new Person();
        person1.setPartyId("TEST002");
        person1.setPartyType(personType);
        person1.setFirstName("Anna");
        person1.setLastName("Schmidt");
        personRepository.save(person1);

        Person person2 = new Person();
        person2.setPartyId("TEST003");
        person2.setPartyType(personType);
        person2.setFirstName("Hans");
        person2.setLastName("Müller");
        personRepository.save(person2);

        // When
        List<Person> results = personRepository.searchByName("anna");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("Anna");
    }

    @Test
    void shouldFindPartyByExternalId() {
        // Given
        PartyType personType = entityManager.find(PartyType.class, "PERSON");

        Person person = new Person();
        person.setPartyId("TEST004");
        person.setExternalId("EXT-12345");
        person.setPartyType(personType);
        person.setFirstName("External");
        person.setLastName("Test");
        personRepository.save(person);

        // When
        Optional<Party> found = partyRepository.findByExternalId("EXT-12345");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getPartyId()).isEqualTo("TEST004");
    }

    @Test
    void shouldFindPersonsByLastName() {
        // Given
        PartyType personType = entityManager.find(PartyType.class, "PERSON");

        Person person1 = new Person();
        person1.setPartyId("TEST005");
        person1.setPartyType(personType);
        person1.setFirstName("Peter");
        person1.setLastName("Meier");
        personRepository.save(person1);

        Person person2 = new Person();
        person2.setPartyId("TEST006");
        person2.setPartyType(personType);
        person2.setFirstName("Klaus");
        person2.setLastName("Meier");
        personRepository.save(person2);

        // When
        List<Person> results = personRepository.findByLastName("Meier");

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("firstName")
                .containsExactlyInAnyOrder("Peter", "Klaus");
    }
}
