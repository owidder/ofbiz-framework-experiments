package org.apache.ofbiz.sync.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.ofbiz.sync.service.OFBizSyncService;
import org.apache.ofbiz.sync.snapshot.PartyGroupSnapshot;
import org.apache.ofbiz.sync.snapshot.PartySnapshot;
import org.apache.ofbiz.sync.snapshot.PersonSnapshot;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "ofbiz-sync.enabled", havingValue = "true", matchIfMissing = true)
public class PartySnapshotConsumer {

    private final OFBizSyncService syncService;

    /**
     * Consumes party snapshots from the compacted topic.
     * Key = partyId, Value = full party state (or null for tombstone/delete)
     */
    @KafkaListener(
            topics = "${ofbiz-sync.kafka.topics.party-entities:party.entities}",
            groupId = "${spring.kafka.consumer.group-id:ofbiz-sync-group}"
    )
    public void consume(ConsumerRecord<String, PartySnapshot> record) {
        String partyId = record.key();
        PartySnapshot snapshot = record.value();

        if (snapshot == null) {
            // Tombstone message - party was deleted
            log.info("Received tombstone for partyId: {} - deleting from OFBiz", partyId);
            handleDelete(partyId);
            return;
        }

        log.info("Received snapshot for partyId: {} (type: {})", partyId, snapshot.getPartyType());

        try {
            if (snapshot instanceof PersonSnapshot personSnapshot) {
                handlePersonSnapshot(personSnapshot);
            } else if (snapshot instanceof PartyGroupSnapshot partyGroupSnapshot) {
                handlePartyGroupSnapshot(partyGroupSnapshot);
            } else {
                log.warn("Unknown snapshot type: {}", snapshot.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Error processing snapshot for partyId: {}", partyId, e);
            // In production, implement retry logic or dead-letter queue
        }
    }

    private void handlePersonSnapshot(PersonSnapshot snapshot) {
        log.debug("Upserting person: {} {} ({})",
                snapshot.getFirstName(), snapshot.getLastName(), snapshot.getPartyId());
        syncService.upsertPerson(snapshot);
    }

    private void handlePartyGroupSnapshot(PartyGroupSnapshot snapshot) {
        log.debug("Upserting party group: {} ({})",
                snapshot.getGroupName(), snapshot.getPartyId());
        syncService.upsertPartyGroup(snapshot);
    }

    private void handleDelete(String partyId) {
        log.debug("Deleting party: {}", partyId);
        syncService.deleteParty(partyId);
    }
}
