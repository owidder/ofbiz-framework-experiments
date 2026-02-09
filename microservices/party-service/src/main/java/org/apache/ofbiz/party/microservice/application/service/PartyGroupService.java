package org.apache.ofbiz.party.microservice.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ofbiz.party.microservice.api.dto.CreatePartyGroupRequest;
import org.apache.ofbiz.party.microservice.api.dto.PartyGroupDto;
import org.apache.ofbiz.party.microservice.api.mapper.PartyGroupMapper;
import org.apache.ofbiz.party.microservice.domain.entity.PartyGroup;
import org.apache.ofbiz.party.microservice.domain.entity.PartyType;
import org.apache.ofbiz.party.microservice.domain.repository.PartyGroupRepository;
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
public class PartyGroupService {

    private final PartyGroupRepository partyGroupRepository;
    private final PartyGroupMapper partyGroupMapper;
    private final EntityManager entityManager;
    private final PartySnapshotPublisher snapshotPublisher;

    public List<PartyGroupDto> findAll() {
        log.debug("Finding all party groups");
        return partyGroupMapper.toDtoList(partyGroupRepository.findAll());
    }

    public PartyGroupDto findById(String partyId) {
        log.debug("Finding party group by id: {}", partyId);
        return partyGroupRepository.findById(partyId)
                .map(partyGroupMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Party group not found: " + partyId));
    }

    public List<PartyGroupDto> searchByGroupName(String name) {
        log.debug("Searching party groups by name: {}", name);
        return partyGroupMapper.toDtoList(partyGroupRepository.searchByGroupName(name));
    }

    @Transactional
    public PartyGroupDto create(CreatePartyGroupRequest request) {
        log.info("Creating new party group: {}", request.getGroupName());

        PartyGroup partyGroup = partyGroupMapper.toEntity(request);
        partyGroup.setPartyId(generatePartyId());
        partyGroup.setPartyType(entityManager.getReference(PartyType.class, "PARTY_GROUP"));
        partyGroup.setStatusId("PARTY_ENABLED");

        PartyGroup saved = partyGroupRepository.save(partyGroup);
        log.info("Created party group with id: {}", saved.getPartyId());

        // Publish snapshot to compacted topic
        snapshotPublisher.publishSnapshot(saved);

        return partyGroupMapper.toDto(saved);
    }

    @Transactional
    public PartyGroupDto update(String partyId, CreatePartyGroupRequest request) {
        log.info("Updating party group: {}", partyId);

        PartyGroup partyGroup = partyGroupRepository.findById(partyId)
                .orElseThrow(() -> new EntityNotFoundException("Party group not found: " + partyId));

        partyGroupMapper.updateEntity(request, partyGroup);
        PartyGroup saved = partyGroupRepository.save(partyGroup);

        log.info("Updated party group: {}", partyId);

        // Publish snapshot to compacted topic
        snapshotPublisher.publishSnapshot(saved);

        return partyGroupMapper.toDto(saved);
    }

    @Transactional
    public void delete(String partyId) {
        log.info("Deleting party group: {}", partyId);

        if (!partyGroupRepository.existsById(partyId)) {
            throw new EntityNotFoundException("Party group not found: " + partyId);
        }

        partyGroupRepository.deleteById(partyId);
        log.info("Deleted party group: {}", partyId);

        // Publish tombstone to compacted topic (signals deletion)
        snapshotPublisher.publishTombstone(partyId);
    }

    private String generatePartyId() {
        return "G" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
