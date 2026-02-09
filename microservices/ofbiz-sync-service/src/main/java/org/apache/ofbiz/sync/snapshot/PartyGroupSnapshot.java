package org.apache.ofbiz.sync.snapshot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartyGroupSnapshot extends PartySnapshot {
    private String groupName;
    private String groupNameLocal;
    private String officeSiteName;
    private BigDecimal annualRevenue;
    private Integer numEmployees;
    private String tickerSymbol;
    private String comments;
    private String logoImageUrl;
}
