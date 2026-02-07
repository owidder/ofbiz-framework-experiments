package org.apache.ofbiz.party.microservice.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreatePartyGroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(max = 100, message = "Group name must not exceed 100 characters")
    private String groupName;

    @Size(max = 100, message = "Group name local must not exceed 100 characters")
    private String groupNameLocal;

    @Size(max = 100, message = "Office site name must not exceed 100 characters")
    private String officeSiteName;

    private BigDecimal annualRevenue;

    private Integer numEmployees;

    @Size(max = 10, message = "Ticker symbol must not exceed 10 characters")
    private String tickerSymbol;

    private String externalId;
}
