package org.apache.ofbiz.party.microservice.api.mapper;

import org.apache.ofbiz.party.microservice.api.dto.CreatePersonRequest;
import org.apache.ofbiz.party.microservice.api.dto.PersonDto;
import org.apache.ofbiz.party.microservice.domain.entity.Person;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PersonMapper {

    @Mapping(target = "partyTypeId", source = "partyType.partyTypeId")
    @Mapping(target = "fullName", expression = "java(person.getFullName())")
    PersonDto toDto(Person person);

    List<PersonDto> toDtoList(List<Person> persons);

    @Mapping(target = "partyId", ignore = true)
    @Mapping(target = "partyType", ignore = true)
    @Mapping(target = "statusId", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdByUserLogin", ignore = true)
    @Mapping(target = "lastModifiedByUserLogin", ignore = true)
    @Mapping(target = "preferredCurrencyUomId", ignore = true)
    @Mapping(target = "dataSourceId", ignore = true)
    @Mapping(target = "isUnread", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "partyRoles", ignore = true)
    @Mapping(target = "partyContactMechs", ignore = true)
    @Mapping(target = "personalTitle", ignore = true)
    @Mapping(target = "suffix", ignore = true)
    @Mapping(target = "firstNameLocal", ignore = true)
    @Mapping(target = "middleNameLocal", ignore = true)
    @Mapping(target = "lastNameLocal", ignore = true)
    @Mapping(target = "otherLocal", ignore = true)
    @Mapping(target = "memberId", ignore = true)
    @Mapping(target = "deceasedDate", ignore = true)
    @Mapping(target = "height", ignore = true)
    @Mapping(target = "weight", ignore = true)
    @Mapping(target = "mothersMaidenName", ignore = true)
    @Mapping(target = "maritalStatusEnumId", ignore = true)
    @Mapping(target = "socialSecurityNumber", ignore = true)
    @Mapping(target = "passportNumber", ignore = true)
    @Mapping(target = "passportExpireDate", ignore = true)
    @Mapping(target = "totalYearsWorkExperience", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "employmentStatusEnumId", ignore = true)
    @Mapping(target = "residenceStatusEnumId", ignore = true)
    @Mapping(target = "yearsWithEmployer", ignore = true)
    @Mapping(target = "monthsWithEmployer", ignore = true)
    @Mapping(target = "existingCustomer", ignore = true)
    @Mapping(target = "cardId", ignore = true)
    Person toEntity(CreatePersonRequest request);

    @Mapping(target = "partyId", ignore = true)
    @Mapping(target = "partyType", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdByUserLogin", ignore = true)
    void updateEntity(CreatePersonRequest request, @MappingTarget Person person);
}
