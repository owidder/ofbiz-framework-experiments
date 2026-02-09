package org.apache.ofbiz.party.microservice.infrastructure.kafka.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PartyGroupCreatedEvent extends PartyEvent {
    private String groupName;
    private String groupNameLocal;
    private BigDecimal annualRevenue;
    private Integer numEmployees;
    private String tickerSymbol;
    private String statusId;

    public PartyGroupCreatedEvent(String partyId, String groupName, String groupNameLocal,
                                   BigDecimal annualRevenue, Integer numEmployees,
                                   String tickerSymbol, String statusId) {
        super(UUID.randomUUID().toString(), "PARTY_GROUP_CREATED", LocalDateTime.now(), partyId, "PARTY_GROUP");
        this.groupName = groupName;
        this.groupNameLocal = groupNameLocal;
        this.annualRevenue = annualRevenue;
        this.numEmployees = numEmployees;
        this.tickerSymbol = tickerSymbol;
        this.statusId = statusId;
    }
}
