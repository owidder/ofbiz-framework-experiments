/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 */
package org.apache.ofbiz.party.microservice.domain.repository;

import org.apache.ofbiz.party.microservice.domain.entity.PartyContactMech;
import org.apache.ofbiz.party.microservice.domain.entity.PartyContactMechId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PartyContactMechRepository extends JpaRepository<PartyContactMech, PartyContactMechId> {

    @Query("SELECT pcm FROM PartyContactMech pcm WHERE pcm.party.partyId = :partyId")
    List<PartyContactMech> findByPartyId(@Param("partyId") String partyId);

    @Query("SELECT pcm FROM PartyContactMech pcm WHERE pcm.party.partyId = :partyId AND pcm.thruDate IS NULL")
    List<PartyContactMech> findActiveByPartyId(@Param("partyId") String partyId);

    @Query("SELECT pcm FROM PartyContactMech pcm WHERE pcm.party.partyId = :partyId AND pcm.fromDate <= :now AND (pcm.thruDate IS NULL OR pcm.thruDate > :now)")
    List<PartyContactMech> findActiveByPartyIdAtDate(@Param("partyId") String partyId, @Param("now") LocalDateTime now);

    @Query("SELECT pcm FROM PartyContactMech pcm WHERE pcm.contactMech.contactMechId = :contactMechId")
    List<PartyContactMech> findByContactMechId(@Param("contactMechId") String contactMechId);
}
