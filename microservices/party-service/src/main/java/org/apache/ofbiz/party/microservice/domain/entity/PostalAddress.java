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
@Table(name = "postal_address", indexes = {
    @Index(name = "idx_postal_city", columnList = "city"),
    @Index(name = "idx_postal_code", columnList = "postal_code")
})
@Getter
@Setter
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "contact_mech_id")
public class PostalAddress extends ContactMech {

    @Column(name = "to_name", length = 100)
    private String toName;

    @Column(name = "attn_name", length = 100)
    private String attnName;

    @Column(name = "address1", columnDefinition = "TEXT")
    private String address1;

    @Column(name = "address2", columnDefinition = "TEXT")
    private String address2;

    @Column(name = "house_number")
    private Integer houseNumber;

    @Column(name = "house_number_ext", length = 60)
    private String houseNumberExt;

    @Column(name = "directions", columnDefinition = "TEXT")
    private String directions;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "city_geo_id", length = 20)
    private String cityGeoId;

    @Column(name = "postal_code", length = 60)
    private String postalCode;

    @Column(name = "postal_code_ext", length = 60)
    private String postalCodeExt;

    @Column(name = "country_geo_id", length = 20)
    private String countryGeoId;

    @Column(name = "state_province_geo_id", length = 20)
    private String stateProvinceGeoId;

    @Column(name = "county_geo_id", length = 20)
    private String countyGeoId;

    @Column(name = "municipality_geo_id", length = 20)
    private String municipalityGeoId;

    @Column(name = "postal_code_geo_id", length = 20)
    private String postalCodeGeoId;

    @Column(name = "geo_point_id", length = 20)
    private String geoPointId;

    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (address1 != null) {
            sb.append(address1);
        }
        if (address2 != null) {
            sb.append("\n").append(address2);
        }
        if (postalCode != null || city != null) {
            sb.append("\n");
            if (postalCode != null) sb.append(postalCode).append(" ");
            if (city != null) sb.append(city);
        }
        return sb.toString();
    }
}
