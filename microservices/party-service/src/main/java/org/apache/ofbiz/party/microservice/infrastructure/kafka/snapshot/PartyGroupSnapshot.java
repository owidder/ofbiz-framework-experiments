package org.apache.ofbiz.party.microservice.infrastructure.kafka.snapshot;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PartyGroupSnapshot extends PartySnapshot {
    private String groupName;
    private String groupNameLocal;
    private String officeSiteName;
    private BigDecimal annualRevenue;
    private Integer numEmployees;
    private String tickerSymbol;
    private String comments;
    private String logoImageUrl;

    public PartyGroupSnapshot(String partyId, String statusId, String description,
                               LocalDateTime createdDate, LocalDateTime lastModifiedDate,
                               String groupName, String groupNameLocal, String officeSiteName,
                               BigDecimal annualRevenue, Integer numEmployees, String tickerSymbol,
                               String comments, String logoImageUrl) {
        super(partyId, "PARTY_GROUP", statusId, description, createdDate, lastModifiedDate);
        this.groupName = groupName;
        this.groupNameLocal = groupNameLocal;
        this.officeSiteName = officeSiteName;
        this.annualRevenue = annualRevenue;
        this.numEmployees = numEmployees;
        this.tickerSymbol = tickerSymbol;
        this.comments = comments;
        this.logoImageUrl = logoImageUrl;
    }
}
