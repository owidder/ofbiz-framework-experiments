/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 */
package org.apache.ofbiz.party.microservice.domain.repository;

import org.apache.ofbiz.party.microservice.domain.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartyRepository extends JpaRepository<Party, String> {

    Optional<Party> findByExternalId(String externalId);

    List<Party> findByStatusId(String statusId);

    @Query("SELECT p FROM Party p WHERE p.partyType.partyTypeId = :partyTypeId")
    List<Party> findByPartyTypeId(@Param("partyTypeId") String partyTypeId);

    @Query("SELECT p FROM Party p JOIN p.partyRoles pr WHERE pr.roleType.roleTypeId = :roleTypeId")
    List<Party> findByRoleTypeId(@Param("roleTypeId") String roleTypeId);
}
