/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 */
package org.apache.ofbiz.party.microservice.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "party_contact_mech", indexes = {
    @Index(name = "idx_party_contact_mech_party", columnList = "party_id"),
    @Index(name = "idx_party_contact_mech_cm", columnList = "contact_mech_id")
})
@IdClass(PartyContactMechId.class)
@Getter
@Setter
@NoArgsConstructor
public class PartyContactMech {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id")
    private Party party;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_mech_id")
    private ContactMech contactMech;

    @Id
    @Column(name = "from_date")
    private LocalDateTime fromDate;

    @Column(name = "thru_date")
    private LocalDateTime thruDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_type_id")
    private RoleType roleType;

    @Column(name = "allow_solicitation", length = 1)
    private String allowSolicitation;

    @Column(name = "extension", columnDefinition = "TEXT")
    private String extension;

    @Column(name = "verified", length = 1)
    private String verified;

    @Column(name = "comments")
    private String comments;

    @Column(name = "years_with_contact_mech")
    private Integer yearsWithContactMech;

    @Column(name = "months_with_contact_mech")
    private Integer monthsWithContactMech;

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return fromDate != null && fromDate.isBefore(now) && (thruDate == null || thruDate.isAfter(now));
    }
}
