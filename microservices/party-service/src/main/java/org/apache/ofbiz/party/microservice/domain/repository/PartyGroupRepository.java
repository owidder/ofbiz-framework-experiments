/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 */
package org.apache.ofbiz.party.microservice.domain.repository;

import org.apache.ofbiz.party.microservice.domain.entity.PartyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartyGroupRepository extends JpaRepository<PartyGroup, String> {

    List<PartyGroup> findByGroupName(String groupName);

    @Query("SELECT pg FROM PartyGroup pg WHERE LOWER(pg.groupName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<PartyGroup> searchByGroupName(@Param("name") String name);

    @Query("SELECT pg FROM PartyGroup pg WHERE pg.tickerSymbol = :tickerSymbol")
    List<PartyGroup> findByTickerSymbol(@Param("tickerSymbol") String tickerSymbol);
}
