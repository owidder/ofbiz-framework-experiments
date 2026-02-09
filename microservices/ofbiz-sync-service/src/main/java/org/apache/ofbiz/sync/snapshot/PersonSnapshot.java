package org.apache.ofbiz.sync.snapshot;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
}
