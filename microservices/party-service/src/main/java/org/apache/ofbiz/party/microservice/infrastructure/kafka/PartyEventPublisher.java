package org.apache.ofbiz.party.microservice.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ofbiz.party.microservice.infrastructure.kafka.event.PartyEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class PartyEventPublisher {

    private final KafkaTemplate<String, PartyEvent> kafkaTemplate;

    @Value("${party-service.kafka.topics.party-events:party.events}")
    private String partyEventsTopic;

    public void publish(PartyEvent event) {
        log.info("Publishing event: {} for party: {}", event.getEventType(), event.getPartyId());

        CompletableFuture<SendResult<String, PartyEvent>> future =
                kafkaTemplate.send(partyEventsTopic, event.getPartyId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Event sent successfully: {} to partition: {} with offset: {}",
                        event.getEventType(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send event: {}", event.getEventType(), ex);
            }
        });
    }
}
