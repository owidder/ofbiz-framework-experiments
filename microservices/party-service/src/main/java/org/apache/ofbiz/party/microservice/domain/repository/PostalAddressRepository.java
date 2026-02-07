/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 */
package org.apache.ofbiz.party.microservice.domain.repository;

import org.apache.ofbiz.party.microservice.domain.entity.PostalAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostalAddressRepository extends JpaRepository<PostalAddress, String> {

    List<PostalAddress> findByCity(String city);

    List<PostalAddress> findByPostalCode(String postalCode);

    @Query("SELECT pa FROM PostalAddress pa WHERE pa.countryGeoId = :countryGeoId")
    List<PostalAddress> findByCountry(@Param("countryGeoId") String countryGeoId);

    @Query("SELECT pa FROM PostalAddress pa WHERE pa.stateProvinceGeoId = :stateGeoId")
    List<PostalAddress> findByState(@Param("stateGeoId") String stateGeoId);

    @Query("SELECT pa FROM PostalAddress pa WHERE LOWER(pa.address1) LIKE LOWER(CONCAT('%', :address, '%')) OR LOWER(pa.address2) LIKE LOWER(CONCAT('%', :address, '%'))")
    List<PostalAddress> searchByAddress(@Param("address") String address);
}
