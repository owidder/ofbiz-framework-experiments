package org.apache.ofbiz.sync.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ofbiz.sync.event.PartyEvent;
import org.apache.ofbiz.sync.service.OFBizSyncService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "ofbiz-sync.enabled", havingValue = "true", matchIfMissing = true)
public class PartyEventConsumer {

    private final OFBizSyncService syncService;

    @KafkaListener(
            topics = "${ofbiz-sync.kafka.topics.party-events:party.events}",
            groupId = "${spring.kafka.consumer.group-id:ofbiz-sync-group}"
    )
    public void consume(PartyEvent event) {
        log.info("Received event: {} for partyId: {}", event.getEventType(), event.getPartyId());

        try {
            if (event.isPerson()) {
                handlePersonEvent(event);
            } else if (event.isPartyGroup()) {
                handlePartyGroupEvent(event);
            } else {
                log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing event: {} for partyId: {}", event.getEventType(), event.getPartyId(), e);
            // In production, you would implement retry logic or dead-letter queue
        }
    }

    private void handlePersonEvent(PartyEvent event) {
        if (event.isCreateEvent()) {
            syncService.createPerson(event);
        } else if (event.isUpdateEvent()) {
            syncService.updatePerson(event);
        } else if (event.isDeleteEvent()) {
            syncService.deletePerson(event.getPartyId());
        }
    }

    private void handlePartyGroupEvent(PartyEvent event) {
        if (event.isCreateEvent()) {
            syncService.createPartyGroup(event);
        } else if (event.isUpdateEvent()) {
            syncService.updatePartyGroup(event);
        } else if (event.isDeleteEvent()) {
            syncService.deletePartyGroup(event.getPartyId());
        }
    }
}
