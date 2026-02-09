package org.apache.ofbiz.party.microservice.infrastructure.kafka.snapshot;

import org.apache.ofbiz.party.microservice.domain.entity.Party;
import org.apache.ofbiz.party.microservice.domain.entity.PartyGroup;
import org.apache.ofbiz.party.microservice.domain.entity.Person;
import org.springframework.stereotype.Component;

@Component
public class PartySnapshotMapper {

    public PartySnapshot toSnapshot(Party party) {
        if (party instanceof Person person) {
            return toPersonSnapshot(person);
        } else if (party instanceof PartyGroup partyGroup) {
            return toPartyGroupSnapshot(partyGroup);
        }
        throw new IllegalArgumentException("Unknown party type: " + party.getClass().getName());
    }

    public PersonSnapshot toPersonSnapshot(Person person) {
        return new PersonSnapshot(
            person.getPartyId(),
            person.getStatusId(),
            person.getDescription(),
            person.getCreatedDate(),
            person.getLastModifiedDate(),
            person.getFirstName(),
            person.getLastName(),
            person.getMiddleName(),
            person.getPersonalTitle(),
            person.getSuffix(),
            person.getNickname(),
            person.getGender(),
            person.getBirthDate(),
            person.getDeceasedDate(),
            person.getMaritalStatusEnumId(),
            person.getSocialSecurityNumber(),
            person.getPassportNumber(),
            person.getPassportExpireDate(),
            person.getEmploymentStatusEnumId(),
            person.getResidenceStatusEnumId(),
            person.getOccupation(),
            person.getYearsWithEmployer(),
            person.getMonthsWithEmployer(),
            person.getMemberId()
        );
    }

    public PartyGroupSnapshot toPartyGroupSnapshot(PartyGroup partyGroup) {
        return new PartyGroupSnapshot(
            partyGroup.getPartyId(),
            partyGroup.getStatusId(),
            partyGroup.getDescription(),
            partyGroup.getCreatedDate(),
            partyGroup.getLastModifiedDate(),
            partyGroup.getGroupName(),
            partyGroup.getGroupNameLocal(),
            partyGroup.getOfficeSiteName(),
            partyGroup.getAnnualRevenue(),
            partyGroup.getNumEmployees(),
            partyGroup.getTickerSymbol(),
            partyGroup.getComments(),
            partyGroup.getLogoImageUrl()
        );
    }
}
