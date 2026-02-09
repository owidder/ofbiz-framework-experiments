package org.apache.ofbiz.sync.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Instant;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartyEvent {

    @JsonProperty("eventType")
    private String eventType;

    @JsonProperty("partyId")
    private String partyId;

    @JsonProperty("timestamp")
    private Instant timestamp;

    // Person fields
    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("middleName")
    private String middleName;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("birthDate")
    private LocalDate birthDate;

    @JsonProperty("statusId")
    private String statusId;

    // PartyGroup fields
    @JsonProperty("groupName")
    private String groupName;

    @JsonProperty("groupNameLocal")
    private String groupNameLocal;

    @JsonProperty("tickerSymbol")
    private String tickerSymbol;

    public boolean isPerson() {
        return eventType != null && eventType.contains("PERSON");
    }

    public boolean isPartyGroup() {
        return eventType != null && eventType.contains("PARTY_GROUP");
    }

    public boolean isCreateEvent() {
        return eventType != null && eventType.contains("CREATED");
    }

    public boolean isUpdateEvent() {
        return eventType != null && eventType.contains("UPDATED");
    }

    public boolean isDeleteEvent() {
        return eventType != null && eventType.contains("DELETED");
    }
}
