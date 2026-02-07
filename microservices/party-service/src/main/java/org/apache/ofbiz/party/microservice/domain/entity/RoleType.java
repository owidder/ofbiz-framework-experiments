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
@Table(name = "role_type")
@Getter
@Setter
@NoArgsConstructor
public class RoleType {

    @Id
    @Column(name = "role_type_id", length = 20)
    private String roleTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_type_id")
    private RoleType parentType;

    @Column(name = "has_table", length = 1)
    private String hasTable;

    @Column(name = "description")
    private String description;
}
