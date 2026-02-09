package org.apache.ofbiz.party.microservice.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ofbiz.party.microservice.domain.entity.Party;
import org.apache.ofbiz.party.microservice.infrastructure.kafka.snapshot.PartySnapshot;
import org.apache.ofbiz.party.microservice.infrastructure.kafka.snapshot.PartySnapshotMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class PartySnapshotPublisher {

    private final KafkaTemplate<String, PartySnapshot> snapshotKafkaTemplate;
    private final PartySnapshotMapper snapshotMapper;

    @Value("${party-service.kafka.topics.party-entities:party.entities}")
    private String partyEntitiesTopic;

    /**
     * Publishes the current state of a party entity to the compacted topic.
     * The party ID is used as the message key for log compaction.
     */
    public void publishSnapshot(Party party) {
        PartySnapshot snapshot = snapshotMapper.toSnapshot(party);

        log.info("Publishing snapshot for party: {} (type: {})",
                snapshot.getPartyId(), snapshot.getPartyType());

        CompletableFuture<SendResult<String, PartySnapshot>> future =
                snapshotKafkaTemplate.send(partyEntitiesTopic, snapshot.getPartyId(), snapshot);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Snapshot published successfully: {} to partition: {} with offset: {}",
                        snapshot.getPartyId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish snapshot for party: {}", snapshot.getPartyId(), ex);
            }
        });
    }

    /**
     * Publishes a tombstone (null value) for a deleted party.
     * This signals to Kafka that the key can be removed during compaction.
     */
    public void publishTombstone(String partyId) {
        log.info("Publishing tombstone for deleted party: {}", partyId);

        CompletableFuture<SendResult<String, PartySnapshot>> future =
                snapshotKafkaTemplate.send(partyEntitiesTopic, partyId, null);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Tombstone published for party: {} to partition: {} with offset: {}",
                        partyId,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish tombstone for party: {}", partyId, ex);
            }
        });
    }
}
