package org.apache.ofbiz.party.microservice.infrastructure.kafka.snapshot;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "partyType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = PersonSnapshot.class, name = "PERSON"),
    @JsonSubTypes.Type(value = PartyGroupSnapshot.class, name = "PARTY_GROUP")
})
public abstract class PartySnapshot {
    private String partyId;
    private String partyType;
    private String statusId;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
