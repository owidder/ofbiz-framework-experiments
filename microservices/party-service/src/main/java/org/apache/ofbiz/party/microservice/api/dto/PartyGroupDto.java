package org.apache.ofbiz.party.microservice.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class PartyGroupDto extends PartyDto {
    private String groupName;
    private String groupNameLocal;
    private String officeSiteName;
    private BigDecimal annualRevenue;
    private Integer numEmployees;
    private String tickerSymbol;
    private String comments;
    private String logoImageUrl;
}
