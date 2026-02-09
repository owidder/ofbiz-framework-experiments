package org.apache.ofbiz.party.microservice.infrastructure.kafka.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PersonUpdatedEvent extends PartyEvent {
    private String firstName;
    private String lastName;
    private String middleName;
    private String gender;
    private LocalDate birthDate;

    public PersonUpdatedEvent(String partyId, String firstName, String lastName,
                               String middleName, String gender, LocalDate birthDate) {
        super(UUID.randomUUID().toString(), "PERSON_UPDATED", LocalDateTime.now(), partyId, "PERSON");
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.gender = gender;
        this.birthDate = birthDate;
    }
}
