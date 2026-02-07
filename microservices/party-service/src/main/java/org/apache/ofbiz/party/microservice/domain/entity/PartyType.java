/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 */
package org.apache.ofbiz.party.microservice.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "party_type")
@Getter
@Setter
@NoArgsConstructor
public class PartyType {

    @Id
    @Column(name = "party_type_id", length = 20)
    private String partyTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_type_id")
    private PartyType parentType;

    @Column(name = "has_table", length = 1)
    private String hasTable;

    @Column(name = "description")
    private String description;
}
