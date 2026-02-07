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
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "party", indexes = {
    @Index(name = "idx_party_external_id", columnList = "external_id"),
    @Index(name = "idx_party_status", columnList = "status_id"),
    @Index(name = "idx_party_type", columnList = "party_type_id")
})
@Getter
@Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class Party {

    @Id
    @Column(name = "party_id", length = 20)
    private String partyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_type_id")
    private PartyType partyType;

    @Column(name = "external_id", length = 20)
    private String externalId;

    @Column(name = "preferred_currency_uom_id", length = 20)
    private String preferredCurrencyUomId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status_id", length = 20)
    private String statusId;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by_user_login")
    private String createdByUserLogin;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column(name = "last_modified_by_user_login")
    private String lastModifiedByUserLogin;

    @Column(name = "data_source_id", length = 20)
    private String dataSourceId;

    @Column(name = "is_unread", length = 1)
    private String isUnread;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PartyRole> partyRoles = new HashSet<>();

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PartyContactMech> partyContactMechs = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        lastModifiedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
}
