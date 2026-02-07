package org.apache.ofbiz.party.microservice.api.mapper;

import org.apache.ofbiz.party.microservice.api.dto.CreatePartyGroupRequest;
import org.apache.ofbiz.party.microservice.api.dto.PartyGroupDto;
import org.apache.ofbiz.party.microservice.domain.entity.PartyGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PartyGroupMapper {

    @Mapping(target = "partyTypeId", source = "partyType.partyTypeId")
    PartyGroupDto toDto(PartyGroup partyGroup);

    List<PartyGroupDto> toDtoList(List<PartyGroup> partyGroups);

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
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "logoImageUrl", ignore = true)
    PartyGroup toEntity(CreatePartyGroupRequest request);

    @Mapping(target = "partyId", ignore = true)
    @Mapping(target = "partyType", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "createdByUserLogin", ignore = true)
    void updateEntity(CreatePartyGroupRequest request, @MappingTarget PartyGroup partyGroup);
}
