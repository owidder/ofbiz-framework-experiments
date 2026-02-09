package org.apache.ofbiz.party.microservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ofbiz.party.microservice.api.dto.CreatePersonRequest;
import org.apache.ofbiz.party.microservice.api.dto.PersonDto;
import org.apache.ofbiz.party.microservice.api.mapper.PersonMapper;
import org.apache.ofbiz.party.microservice.domain.entity.Person;
import org.apache.ofbiz.party.microservice.domain.entity.PartyType;
import org.apache.ofbiz.party.microservice.domain.repository.PersonRepository;
import org.apache.ofbiz.party.microservice.infrastructure.kafka.PartySnapshotPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PersonService {

    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final EntityManager entityManager;
    private final PartySnapshotPublisher snapshotPublisher;

    public List<PersonDto> findAll() {
        log.debug("Finding all persons");
        return personMapper.toDtoList(personRepository.findAll());
    }

    public PersonDto findById(String partyId) {
        log.debug("Finding person by id: {}", partyId);
        return personRepository.findById(partyId)
                .map(personMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Person not found: " + partyId));
    }

    public List<PersonDto> findByLastName(String lastName) {
        log.debug("Finding persons by last name: {}", lastName);
        return personMapper.toDtoList(personRepository.findByLastName(lastName));
    }

    public List<PersonDto> searchByName(String name) {
        log.debug("Searching persons by name: {}", name);
        return personMapper.toDtoList(personRepository.searchByName(name));
    }

    @Transactional
    public PersonDto create(CreatePersonRequest request) {
        log.info("Creating new person: {} {}", request.getFirstName(), request.getLastName());

        Person person = personMapper.toEntity(request);
        person.setPartyId(generatePartyId());
        person.setPartyType(entityManager.getReference(PartyType.class, "PERSON"));
        person.setStatusId("PARTY_ENABLED");

        Person saved = personRepository.save(person);
        log.info("Created person with id: {}", saved.getPartyId());

        // Publish snapshot to compacted topic
        snapshotPublisher.publishSnapshot(saved);

        return personMapper.toDto(saved);
    }

    @Transactional
    public PersonDto update(String partyId, CreatePersonRequest request) {
        log.info("Updating person: {}", partyId);

        Person person = personRepository.findById(partyId)
                .orElseThrow(() -> new EntityNotFoundException("Person not found: " + partyId));

        personMapper.updateEntity(request, person);
        Person saved = personRepository.save(person);

        log.info("Updated person: {}", partyId);

        // Publish snapshot to compacted topic
        snapshotPublisher.publishSnapshot(saved);

        return personMapper.toDto(saved);
    }

    @Transactional
    public void delete(String partyId) {
        log.info("Deleting person: {}", partyId);

        if (!personRepository.existsById(partyId)) {
            throw new EntityNotFoundException("Person not found: " + partyId);
        }

        personRepository.deleteById(partyId);
        log.info("Deleted person: {}", partyId);

        // Publish tombstone to compacted topic (signals deletion)
        snapshotPublisher.publishTombstone(partyId);
    }

    private String generatePartyId() {
        return "P" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
