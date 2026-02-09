package org.apache.ofbiz.party.microservice.infrastructure.kafka.snapshot;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PersonSnapshot extends PartySnapshot {
    private String firstName;
    private String lastName;
    private String middleName;
    private String personalTitle;
    private String suffix;
    private String nickname;
    private String gender;
    private LocalDate birthDate;
    private LocalDate deceasedDate;
    private String maritalStatusEnumId;
    private String socialSecurityNumber;
    private String passportNumber;
    private LocalDate passportExpireDate;
    private String employmentStatusEnumId;
    private String residenceStatusEnumId;
    private String occupation;
    private Integer yearsWithEmployer;
    private Integer monthsWithEmployer;
    private String memberId;

    public PersonSnapshot(String partyId, String statusId, String description,
                          LocalDateTime createdDate, LocalDateTime lastModifiedDate,
                          String firstName, String lastName, String middleName,
                          String personalTitle, String suffix, String nickname,
                          String gender, LocalDate birthDate, LocalDate deceasedDate,
                          String maritalStatusEnumId, String socialSecurityNumber,
                          String passportNumber, LocalDate passportExpireDate,
                          String employmentStatusEnumId, String residenceStatusEnumId,
                          String occupation, Integer yearsWithEmployer, Integer monthsWithEmployer,
                          String memberId) {
        super(partyId, "PERSON", statusId, description, createdDate, lastModifiedDate);
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleName = middleName;
        this.personalTitle = personalTitle;
        this.suffix = suffix;
        this.nickname = nickname;
        this.gender = gender;
        this.birthDate = birthDate;
        this.deceasedDate = deceasedDate;
        this.maritalStatusEnumId = maritalStatusEnumId;
        this.socialSecurityNumber = socialSecurityNumber;
        this.passportNumber = passportNumber;
        this.passportExpireDate = passportExpireDate;
        this.employmentStatusEnumId = employmentStatusEnumId;
        this.residenceStatusEnumId = residenceStatusEnumId;
        this.occupation = occupation;
        this.yearsWithEmployer = yearsWithEmployer;
        this.monthsWithEmployer = monthsWithEmployer;
        this.memberId = memberId;
    }
}
