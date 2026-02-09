package org.apache.ofbiz.party.microservice.infrastructure.kafka.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PersonDeletedEvent extends PartyEvent {

    public PersonDeletedEvent(String partyId) {
        super(UUID.randomUUID().toString(), "PERSON_DELETED", LocalDateTime.now(), partyId, "PERSON");
    }
}
