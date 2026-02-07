/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 */
package org.apache.ofbiz.party.microservice.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "person", indexes = {
    @Index(name = "idx_person_first_name", columnList = "first_name"),
    @Index(name = "idx_person_last_name", columnList = "last_name"),
    @Index(name = "idx_person_member_id", columnList = "member_id")
})
@Getter
@Setter
@NoArgsConstructor
@PrimaryKeyJoinColumn(name = "party_id")
public class Person extends Party {

    @Column(name = "salutation", length = 100)
    private String salutation;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "personal_title", length = 100)
    private String personalTitle;

    @Column(name = "suffix", length = 100)
    private String suffix;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @Column(name = "first_name_local", length = 100)
    private String firstNameLocal;

    @Column(name = "middle_name_local", length = 100)
    private String middleNameLocal;

    @Column(name = "last_name_local", length = 100)
    private String lastNameLocal;

    @Column(name = "other_local", length = 100)
    private String otherLocal;

    @Column(name = "member_id", length = 20)
    private String memberId;

    @Column(name = "gender", length = 1)
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "deceased_date")
    private LocalDate deceasedDate;

    @Column(name = "height")
    private Double height;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "mothers_maiden_name", columnDefinition = "TEXT")
    private String mothersMaidenName;

    @Column(name = "marital_status_enum_id", length = 20)
    private String maritalStatusEnumId;

    @Column(name = "social_security_number", columnDefinition = "TEXT")
    private String socialSecurityNumber;

    @Column(name = "passport_number", columnDefinition = "TEXT")
    private String passportNumber;

    @Column(name = "passport_expire_date")
    private LocalDate passportExpireDate;

    @Column(name = "total_years_work_experience")
    private Double totalYearsWorkExperience;

    @Column(name = "comments")
    private String comments;

    @Column(name = "employment_status_enum_id", length = 20)
    private String employmentStatusEnumId;

    @Column(name = "residence_status_enum_id", length = 20)
    private String residenceStatusEnumId;

    @Column(name = "occupation", length = 100)
    private String occupation;

    @Column(name = "years_with_employer")
    private Integer yearsWithEmployer;

    @Column(name = "months_with_employer")
    private Integer monthsWithEmployer;

    @Column(name = "existing_customer", length = 1)
    private String existingCustomer;

    @Column(name = "card_id", length = 60)
    private String cardId;

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null) {
            sb.append(firstName);
        }
        if (middleName != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(middleName);
        }
        if (lastName != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(lastName);
        }
        return sb.toString();
    }
}
