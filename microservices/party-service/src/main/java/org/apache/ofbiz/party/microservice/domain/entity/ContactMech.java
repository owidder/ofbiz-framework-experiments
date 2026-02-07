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
@Table(name = "contact_mech", indexes = {
    @Index(name = "idx_contact_mech_type", columnList = "contact_mech_type_id")
})
@Getter
@Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class ContactMech {

    @Id
    @Column(name = "contact_mech_id", length = 20)
    private String contactMechId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_mech_type_id")
    private ContactMechType contactMechType;

    @Column(name = "info_string", columnDefinition = "TEXT")
    private String infoString;
}
