/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 */
package org.apache.ofbiz.party.microservice.domain.repository;

import org.apache.ofbiz.party.microservice.domain.entity.ContactMech;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactMechRepository extends JpaRepository<ContactMech, String> {

    @Query("SELECT cm FROM ContactMech cm WHERE cm.contactMechType.contactMechTypeId = :typeId")
    List<ContactMech> findByContactMechTypeId(@Param("typeId") String typeId);

    List<ContactMech> findByInfoStringContainingIgnoreCase(String infoString);
}
