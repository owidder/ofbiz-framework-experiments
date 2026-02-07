/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 */
package org.apache.ofbiz.party.microservice.domain.repository;

import org.apache.ofbiz.party.microservice.domain.entity.TelecomNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TelecomNumberRepository extends JpaRepository<TelecomNumber, String> {

    List<TelecomNumber> findByContactNumber(String contactNumber);

    @Query("SELECT tn FROM TelecomNumber tn WHERE tn.areaCode = :areaCode")
    List<TelecomNumber> findByAreaCode(@Param("areaCode") String areaCode);

    @Query("SELECT tn FROM TelecomNumber tn WHERE tn.countryCode = :countryCode")
    List<TelecomNumber> findByCountryCode(@Param("countryCode") String countryCode);

    @Query("SELECT tn FROM TelecomNumber tn WHERE tn.contactNumber LIKE CONCAT('%', :number, '%')")
    List<TelecomNumber> searchByNumber(@Param("number") String number);
}
