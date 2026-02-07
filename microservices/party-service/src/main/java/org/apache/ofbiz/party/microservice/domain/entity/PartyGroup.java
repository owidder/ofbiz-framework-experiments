/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 */
package org.apache.ofbiz.party.microservice.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "party_group", indexes = {
    @Index(name = "idx_party_group_name", columnList = "group_name")
})
@Getter
@Setter
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "party_id")
public class PartyGroup extends Party {

    @Column(name = "group_name", length = 100)
    private String groupName;

    @Column(name = "group_name_local", length = 100)
    private String groupNameLocal;

    @Column(name = "office_site_name", length = 100)
    private String officeSiteName;

    @Column(name = "annual_revenue", precision = 18, scale = 2)
    private BigDecimal annualRevenue;

    @Column(name = "num_employees")
    private Integer numEmployees;

    @Column(name = "ticker_symbol", length = 10)
    private String tickerSymbol;

    @Column(name = "comments")
    private String comments;

    @Column(name = "logo_image_url", length = 2000)
    private String logoImageUrl;
}
