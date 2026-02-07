package org.apache.ofbiz.party.microservice.api.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PartyDto {
    private String partyId;
    private String partyTypeId;
    private String externalId;
    private String description;
    private String statusId;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
