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
@Table(name = "telecom_number", indexes = {
    @Index(name = "idx_telecom_contact_number", columnList = "contact_number")
})
@Getter
@Setter
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "contact_mech_id")
public class TelecomNumber extends ContactMech {

    @Column(name = "country_code", length = 10)
    private String countryCode;

    @Column(name = "area_code", length = 10)
    private String areaCode;

    @Column(name = "contact_number", length = 60)
    private String contactNumber;

    public String getFormattedNumber() {
        StringBuilder sb = new StringBuilder();
        if (countryCode != null) {
            sb.append("+").append(countryCode).append(" ");
        }
        if (areaCode != null) {
            sb.append("(").append(areaCode).append(") ");
        }
        if (contactNumber != null) {
            sb.append(contactNumber);
        }
        return sb.toString();
    }
}
