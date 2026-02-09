package org.apache.ofbiz.party.microservice.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class PartyEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private String partyId;
    private String partyTypeId;
}
