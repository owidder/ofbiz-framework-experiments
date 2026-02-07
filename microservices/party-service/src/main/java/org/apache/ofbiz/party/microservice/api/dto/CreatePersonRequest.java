package org.apache.ofbiz.party.microservice.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CreatePersonRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Middle name must not exceed 100 characters")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Size(max = 100, message = "Salutation must not exceed 100 characters")
    private String salutation;

    @Size(max = 100, message = "Nickname must not exceed 100 characters")
    private String nickname;

    @Size(max = 1, message = "Gender must be a single character (M/F)")
    private String gender;

    private LocalDate birthDate;

    @Size(max = 100, message = "Occupation must not exceed 100 characters")
    private String occupation;

    private String externalId;
}
