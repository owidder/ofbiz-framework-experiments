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
@Table(name = "contact_mech_type")
@Getter
@Setter
@NoArgsConstructor
public class ContactMechType {

    @Id
    @Column(name = "contact_mech_type_id", length = 20)
    private String contactMechTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_type_id")
    private ContactMechType parentType;

    @Column(name = "has_table", length = 1)
    private String hasTable;

    @Column(name = "description")
    private String description;
}
