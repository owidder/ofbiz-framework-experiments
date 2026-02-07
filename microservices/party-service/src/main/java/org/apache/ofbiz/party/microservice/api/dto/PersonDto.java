package org.apache.ofbiz.party.microservice.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class PersonDto extends PartyDto {
    private String salutation;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String nickname;
    private String gender;
    private LocalDate birthDate;
    private String occupation;
    private String comments;
}
