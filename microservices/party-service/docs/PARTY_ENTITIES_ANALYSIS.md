# OFBiz Party Entities - Analyse

Automatisch generiert aus `party-entitymodel.xml`

**Anzahl Entities:** 82

## Statistiken

- **Kern-Entities:** 12
- **Erweiterte Entities:** 70

### Entities nach Kategorie

- **Agreement:** 24
- **Communication:** 8
- **Contact:** 15
- **Party:** 35

## Übersicht aller Entities

| Entity | Kategorie | Priorität | Primärschlüssel | Felder | Beziehungen |
|--------|-----------|-----------|-----------------|--------|-------------|
| ContactMech | Contact | CORE | contactMechId | 3 | 2 |
| ContactMechType | Contact | CORE | contactMechTypeId | 4 | 1 |
| EmailAddressVerification | Contact | CORE | emailAddress | 3 | 0 |
| PartyContactMech | Contact | CORE | partyId, contactMechId, fromDate | 11 | 9 |
| PostalAddress | Contact | CORE | contactMechId | 18 | 8 |
| TelecomNumber | Contact | CORE | contactMechId | 5 | 1 |
| Party | Party | CORE | partyId | 12 | 7 |
| PartyGroup | Party | CORE | partyId | 9 | 1 |
| PartyRelationship | Party | CORE | partyIdFrom, partyIdTo, roleTypeIdFrom, roleTypeIdTo, fromDate | 14 | 10 |
| PartyRole | Party | CORE | partyId, roleTypeId | 2 | 4 |
| Person | Party | CORE | partyId | 32 | 4 |
| RoleType | Party | CORE | roleTypeId | 4 | 1 |
| Addendum | Agreement | EXTENDED | addendumId | 6 | 2 |
| Agreement | Agreement | EXTENDED | agreementId | 13 | 10 |
| AgreementAttribute | Agreement | EXTENDED | agreementId, attrName | 4 | 2 |
| AgreementContent | Agreement | EXTENDED | contentId, agreementId, agreementItemSeqId, agreementContentTypeId, fromDate | 6 | 3 |
| AgreementContentType | Agreement | EXTENDED | agreementContentTypeId | 4 | 1 |
| AgreementEmploymentAppl | Agreement | EXTENDED | agreementId, agreementItemSeqId, partyIdTo, partyIdFrom, roleTypeIdTo, roleTypeIdFrom, fromDate | 9 | 3 |
| AgreementFacilityAppl | Agreement | EXTENDED | agreementId, agreementItemSeqId, facilityId | 3 | 3 |
| AgreementGeographicalApplic | Agreement | EXTENDED | agreementId, agreementItemSeqId, geoId | 3 | 3 |
| AgreementItem | Agreement | EXTENDED | agreementId, agreementItemSeqId | 6 | 3 |
| AgreementItemAttribute | Agreement | EXTENDED | agreementId, agreementItemSeqId, attrName | 5 | 2 |
| AgreementItemType | Agreement | EXTENDED | agreementItemTypeId | 4 | 1 |
| AgreementItemTypeAttr | Agreement | EXTENDED | agreementItemTypeId, attrName | 3 | 3 |
| AgreementPartyApplic | Agreement | EXTENDED | agreementId, agreementItemSeqId, partyId | 3 | 3 |
| AgreementProductAppl | Agreement | EXTENDED | agreementId, agreementItemSeqId, productId | 4 | 3 |
| AgreementPromoAppl | Agreement | EXTENDED | agreementId, agreementItemSeqId, productPromoId, fromDate | 6 | 3 |
| AgreementRole | Agreement | EXTENDED | agreementId, partyId, roleTypeId | 3 | 4 |
| AgreementStatus | Agreement | EXTENDED | agreementId, statusId, statusDate | 5 | 3 |
| AgreementTerm | Agreement | EXTENDED | agreementTermId | 13 | 4 |
| AgreementTermAttribute | Agreement | EXTENDED | agreementTermId, attrName | 4 | 1 |
| AgreementType | Agreement | EXTENDED | agreementTypeId | 4 | 1 |
| AgreementTypeAttr | Agreement | EXTENDED | agreementTypeId, attrName | 3 | 3 |
| AgreementWorkEffortApplic | Agreement | EXTENDED | agreementId, agreementItemSeqId, workEffortId | 3 | 3 |
| TermType | Agreement | EXTENDED | termTypeId | 4 | 1 |
| TermTypeAttr | Agreement | EXTENDED | termTypeId, attrName | 3 | 9 |
| CommContentAssocType | Communication | EXTENDED | commContentAssocTypeId | 2 | 0 |
| CommEventContentAssoc | Communication | EXTENDED | contentId, communicationEventId, fromDate | 6 | 3 |
| CommunicationEvent | Communication | EXTENDED | communicationEventId | 27 | 14 |
| CommunicationEventProduct | Communication | EXTENDED | productId, communicationEventId | 2 | 2 |
| CommunicationEventPrpTyp | Communication | EXTENDED | communicationEventPrpTypId | 4 | 1 |
| CommunicationEventPurpose | Communication | EXTENDED | communicationEventPrpTypId, communicationEventId | 3 | 2 |
| CommunicationEventRole | Communication | EXTENDED | communicationEventId, partyId, roleTypeId | 5 | 6 |
| CommunicationEventType | Communication | EXTENDED | communicationEventTypeId | 5 | 2 |
| ContactMechAttribute | Contact | EXTENDED | contactMechId, attrName | 4 | 2 |
| ContactMechLink | Contact | EXTENDED | contactMechIdFrom, contactMechIdTo | 2 | 2 |
| ContactMechPurposeType | Contact | EXTENDED | contactMechPurposeTypeId | 4 | 0 |
| ContactMechTypeAttr | Contact | EXTENDED | contactMechTypeId, attrName | 3 | 3 |
| ContactMechTypePurpose | Contact | EXTENDED | contactMechTypeId, contactMechPurposeTypeId | 2 | 2 |
| FtpAddress | Contact | EXTENDED | contactMechId | 10 | 1 |
| PartyContactMechPurpose | Contact | EXTENDED | partyId, contactMechId, contactMechPurposeTypeId, fromDate | 5 | 8 |
| PostalAddressBoundary | Contact | EXTENDED | contactMechId, geoId | 2 | 2 |
| ValidContactMechRole | Contact | EXTENDED | roleTypeId, contactMechTypeId | 2 | 2 |
| AddressMatchMap | Party | EXTENDED | mapKey, mapValue | 3 | 0 |
| Affiliate | Party | EXTENDED | partyId | 9 | 2 |
| NeedType | Party | EXTENDED | needTypeId | 2 | 0 |
| PartyAttribute | Party | EXTENDED | partyId, attrName | 4 | 2 |
| PartyCarrierAccount | Party | EXTENDED | partyId, carrierPartyId, fromDate | 5 | 2 |
| PartyClassification | Party | EXTENDED | partyId, partyClassificationGroupId, fromDate | 4 | 2 |
| PartyClassificationGroup | Party | EXTENDED | partyClassificationGroupId | 4 | 2 |
| PartyClassificationType | Party | EXTENDED | partyClassificationTypeId | 4 | 1 |
| PartyContent | Party | EXTENDED | partyId, contentId, partyContentTypeId, fromDate | 5 | 3 |
| PartyContentType | Party | EXTENDED | partyContentTypeId | 3 | 1 |
| PartyDataSource | Party | EXTENDED | partyId, dataSourceId, fromDate | 6 | 2 |
| PartyGeoPoint | Party | EXTENDED | partyId, geoPointId, fromDate | 4 | 2 |
| PartyIcsAvsOverride | Party | EXTENDED | partyId | 2 | 1 |
| PartyIdentification | Party | EXTENDED | partyId, partyIdentificationTypeId | 3 | 2 |
| PartyIdentificationType | Party | EXTENDED | partyIdentificationTypeId | 4 | 1 |
| PartyInvitation | Party | EXTENDED | partyInvitationId | 7 | 2 |
| PartyInvitationGroupAssoc | Party | EXTENDED | partyInvitationId, partyIdTo | 2 | 3 |
| PartyInvitationRoleAssoc | Party | EXTENDED | partyInvitationId, roleTypeId | 2 | 2 |
| PartyNameHistory | Party | EXTENDED | partyId, changeDate | 8 | 1 |
| PartyNeed | Party | EXTENDED | partyNeedId, partyId, roleTypeId | 11 | 8 |
| PartyNote | Party | EXTENDED | partyId, noteId | 2 | 2 |
| PartyProfileDefault | Party | EXTENDED | partyId, productStoreId | 6 | 2 |
| PartyRelationshipType | Party | EXTENDED | partyRelationshipTypeId | 7 | 3 |
| PartyStatus | Party | EXTENDED | statusId, partyId, statusDate | 4 | 3 |
| PartyType | Party | EXTENDED | partyTypeId | 4 | 2 |
| PartyTypeAttr | Party | EXTENDED | partyTypeId, attrName | 3 | 3 |
| PriorityType | Party | EXTENDED | priorityTypeId | 2 | 0 |
| RoleTypeAttr | Party | EXTENDED | roleTypeId, attrName | 3 | 5 |
| Vendor | Party | EXTENDED | partyId | 5 | 1 |

## Kern-Entities (Detailliert)

### ContactMech

**Package:** `org.apache.ofbiz.party.contact`

**Primärschlüssel:** contactMechId

**Felder:**

| Feldname | Typ |
|----------|-----|
| contactMechId | id |
| contactMechTypeId | id |
| infoString | long-varchar |

**Beziehungen:**

| Typ | Ziel-Entity | FK-Name | Key-Mapping |
|-----|-------------|---------|-------------|
| one | ContactMechType | CONT_MECH_TYPE | contactMechTypeId->contactMechTypeId |
| many | ContactMechTypeAttr | - | contactMechTypeId->contactMechTypeId |

---

### ContactMechType

**Package:** `org.apache.ofbiz.party.contact`

**Primärschlüssel:** contactMechTypeId

**Felder:**

| Feldname | Typ |
|----------|-----|
| contactMechTypeId | id |
| parentTypeId | id |
| hasTable | indicator |
| description | description |

**Beziehungen:**

| Typ | Ziel-Entity | FK-Name | Key-Mapping |
|-----|-------------|---------|-------------|
| one | ContactMechType | CONT_MECH_TYP_PAR | parentTypeId->contactMechTypeId |

---

### EmailAddressVerification

**Package:** `org.apache.ofbiz.party.contact`

**Primärschlüssel:** emailAddress

**Felder:**

| Feldname | Typ |
|----------|-----|
| emailAddress | id-vlong |
| verifyHash | value |
| expireDate | date-time |

---

### PartyContactMech

**Package:** `org.apache.ofbiz.party.contact`

**Primärschlüssel:** partyId, contactMechId, fromDate

**Felder:**

| Feldname | Typ |
|----------|-----|
| partyId | id |
| contactMechId | id |
| fromDate | date-time |
| thruDate | date-time |
| roleTypeId | id |
| allowSolicitation | indicator |
| extension | long-varchar |
| verified | indicator |
| comments | comment |
| yearsWithContactMech | numeric |
| monthsWithContactMech | numeric |

**Beziehungen:**

| Typ | Ziel-Entity | FK-Name | Key-Mapping |
|-----|-------------|---------|-------------|
| one | Party | PARTY_CMECH_PARTY | partyId->partyId |
| one-nofk | Person | - | partyId->partyId |
| one-nofk | PartyGroup | - | partyId->partyId |
| one | PartyRole | PARTY_CMECH_PROLE | partyId->partyId, roleTypeId->roleTypeId |
| one | RoleType | PARTY_CMECH_ROLE | roleTypeId->roleTypeId |
| one | ContactMech | PARTY_CMECH_CMECH | contactMechId->contactMechId |
| one-nofk | TelecomNumber | - | contactMechId->contactMechId |
| one-nofk | PostalAddress | - | contactMechId->contactMechId |
| many | PartyContactMechPurpose | - | partyId->partyId, contactMechId->contactMechId |

---

### PostalAddress

**Package:** `org.apache.ofbiz.party.contact`

**Primärschlüssel:** contactMechId

**Felder:**

| Feldname | Typ |
|----------|-----|
| contactMechId | id |
| toName | name |
| attnName | name |
| address1 | long-varchar |
| address2 | long-varchar |
| houseNumber | numeric |
| houseNumberExt | short-varchar |
| directions | long-varchar |
| city | name |
| cityGeoId | id |
| postalCode | short-varchar |
| postalCodeExt | short-varchar |
| countryGeoId | id |
| stateProvinceGeoId | id |
| countyGeoId | id |
| municipalityGeoId | id |
| postalCodeGeoId | id |
| geoPointId | id |

**Beziehungen:**

| Typ | Ziel-Entity | FK-Name | Key-Mapping |
|-----|-------------|---------|-------------|
| one | ContactMech | POST_ADDR_CMECH | contactMechId->contactMechId |
| one | Geo | POST_ADDR_CGEO | countryGeoId->geoId |
| one | Geo | POST_ADDR_SPGEO | stateProvinceGeoId->geoId |
| one | Geo | POST_ADDR_CNTG | countyGeoId->geoId |
| one | Geo | POST_ADDR_MNCP | municipalityGeoId->geoId |
| one | Geo | POST_ADDR_CITY | cityGeoId->geoId |
| one | Geo | POST_ADDR_PCGEO | postalCodeGeoId->geoId |
| one | GeoPoint | POST_ADDR_GEOPT | geoPointId->geoPointId |

---

### TelecomNumber

**Package:** `org.apache.ofbiz.party.contact`

**Primärschlüssel:** contactMechId

**Felder:**

| Feldname | Typ |
|----------|-----|
| contactMechId | id |
| countryCode | very-short |
| areaCode | very-short |
| contactNumber | short-varchar |
| askForName | name |

**Beziehungen:**

| Typ | Ziel-Entity | FK-Name | Key-Mapping |
|-----|-------------|---------|-------------|
| one | ContactMech | TEL_NUM_CMECH | contactMechId->contactMechId |

---

### Party

**Package:** `org.apache.ofbiz.party.party`

**Primärschlüssel:** partyId

**Felder:**

| Feldname | Typ |
|----------|-----|
| partyId | id |
| partyTypeId | id |
| externalId | id |
| preferredCurrencyUomId | id |
| description | very-long |
| statusId | id |
| createdDate | date-time |
| createdByUserLogin | id-vlong |
| lastModifiedDate | date-time |
| lastModifiedByUserLogin | id-vlong |
| dataSourceId | id |
| isUnread | indicator |

**Beziehungen:**

| Typ | Ziel-Entity | FK-Name | Key-Mapping |
|-----|-------------|---------|-------------|
| one | PartyType | PARTY_PTY_TYP | partyTypeId->partyTypeId |
| one | UserLogin | PARTY_CUL | createdByUserLogin->userLoginId |
| one | UserLogin | PARTY_LMCUL | lastModifiedByUserLogin->userLoginId |
| one | Uom | PARTY_PREF_CRNCY | preferredCurrencyUomId->uomId |
| one | StatusItem | PARTY_STATUSITM | statusId->statusId |
| many | PartyTypeAttr | - | partyTypeId->partyTypeId |
| one | DataSource | PARTY_DATSRC | dataSourceId->dataSourceId |

---

### PartyGroup

**Package:** `org.apache.ofbiz.party.party`

**Primärschlüssel:** partyId

**Felder:**

| Feldname | Typ |
|----------|-----|
| partyId | id |
| groupName | name |
| groupNameLocal | name |
| officeSiteName | name |
| annualRevenue | currency-amount |
| numEmployees | numeric |
| tickerSymbol | very-short |
| comments | comment |
| logoImageUrl | url |

**Beziehungen:**

| Typ | Ziel-Entity | FK-Name | Key-Mapping |
|-----|-------------|---------|-------------|
| one | Party | PARTY_GRP_PARTY | partyId->partyId |

---

### PartyRelationship

**Package:** `org.apache.ofbiz.party.party`

**Primärschlüssel:** partyIdFrom, partyIdTo, roleTypeIdFrom, roleTypeIdTo, fromDate

**Felder:**

| Feldname | Typ |
|----------|-----|
| partyIdFrom | id |
| partyIdTo | id |
| roleTypeIdFrom | id |
| roleTypeIdTo | id |
| fromDate | date-time |
| thruDate | date-time |
| statusId | id |
| relationshipName | name |
| securityGroupId | id |
| priorityTypeId | id |
| partyRelationshipTypeId | id |
| permissionsEnumId | id |
| positionTitle | name |
| comments | comment |

**Beziehungen:**

| Typ | Ziel-Entity | FK-Name | Key-Mapping |
|-----|-------------|---------|-------------|
| one-nofk | Party | - | partyIdFrom->partyId |
| one-nofk | Party | - | partyIdTo->partyId |
| one-nofk | RoleType | - | roleTypeIdFrom->roleTypeId |
| one-nofk | RoleType | - | roleTypeIdTo->roleTypeId |
| one | PartyRole | PARTY_REL_FPROLE | partyIdFrom->partyId, roleTypeIdFrom->roleTypeId |
| one | PartyRole | PARTY_REL_TPROLE | partyIdTo->partyId, roleTypeIdTo->roleTypeId |
| one | StatusItem | PARTY_REL_STTS | statusId->statusId |
| one | PriorityType | PARTY_REL_PRTYP | priorityTypeId->priorityTypeId |
| one | PartyRelationshipType | PARTY_REL_TYPE | partyRelationshipTypeId->partyRelationshipTypeId |
| one | SecurityGroup | PARTY_REL_SECGRP | securityGroupId->groupId |

---

### PartyRole

**Package:** `org.apache.ofbiz.party.party`

**Primärschlüssel:** partyId, roleTypeId

**Felder:**

| Feldname | Typ |
|----------|-----|
| partyId | id |
| roleTypeId | id |

**Beziehungen:**

| Typ | Ziel-Entity | FK-Name | Key-Mapping |
|-----|-------------|---------|-------------|
| one | Party | PARTY_RLE_PARTY | partyId->partyId |
| one | RoleType | PARTY_RLE_ROLE | roleTypeId->roleTypeId |
| many | RoleTypeAttr | - | roleTypeId->roleTypeId |
| many | PartyAttribute | - | partyId->partyId |

---

### Person

**Package:** `org.apache.ofbiz.party.party`

**Primärschlüssel:** partyId

**Felder:**

| Feldname | Typ |
|----------|-----|
| partyId | id |
| salutation | name |
| firstName | name |
| middleName | name |
| lastName | name |
| personalTitle | name |
| suffix | name |
| nickname | name |
| firstNameLocal | name |
| middleNameLocal | name |
| lastNameLocal | name |
| otherLocal | name |
| memberId | id |
| gender | indicator |
| birthDate | date |
| deceasedDate | date |
| height | floating-point |
| weight | floating-point |
| mothersMaidenName | long-varchar |
| maritalStatusEnumId | id |
| socialSecurityNumber | long-varchar |
| passportNumber | long-varchar |
| passportExpireDate | date |
| totalYearsWorkExperience | floating-point |
| comments | comment |
| employmentStatusEnumId | id |
| residenceStatusEnumId | id |
| occupation | name |
| yearsWithEmployer | numeric |
| monthsWithEmployer | numeric |
| existingCustomer | indicator |
| cardId | id-long |

**Beziehungen:**

| Typ | Ziel-Entity | FK-Name | Key-Mapping |
|-----|-------------|---------|-------------|
| one | Party | PERSON_PARTY | partyId->partyId |
| one | Enumeration | PERSON_EMPS_ENUM | employmentStatusEnumId->enumId |
| one | Enumeration | PERSON_RESS_ENUM | residenceStatusEnumId->enumId |
| one | Enumeration | PERSON_MARITAL | maritalStatusEnumId->enumId |

---

### RoleType

**Package:** `org.apache.ofbiz.party.party`

**Primärschlüssel:** roleTypeId

**Felder:**

| Feldname | Typ |
|----------|-----|
| roleTypeId | id |
| parentTypeId | id |
| hasTable | indicator |
| description | description |

**Beziehungen:**

| Typ | Ziel-Entity | FK-Name | Key-Mapping |
|-----|-------------|---------|-------------|
| one | RoleType | ROLE_TYPE_PAR | parentTypeId->roleTypeId |

---

## Erweiterte Entities (Kompakt)

### Kategorie: Agreement

**Addendum**
- PK: addendumId
- Felder: 6
- Beziehungen zu: Agreement, AgreementItem

**Agreement**
- PK: agreementId
- Felder: 13
- Beziehungen zu: AgreementType, AgreementTypeAttr, Party, PartyRelationship, PartyRole (+2 weitere)

**AgreementAttribute**
- PK: agreementId, attrName
- Felder: 4
- Beziehungen zu: Agreement, AgreementTypeAttr

**AgreementContent**
- PK: contentId, agreementId, agreementItemSeqId, agreementContentTypeId, fromDate
- Felder: 6
- Beziehungen zu: Agreement, AgreementContentType, Content

**AgreementContentType**
- PK: agreementContentTypeId
- Felder: 4
- Beziehungen zu: AgreementContentType

**AgreementEmploymentAppl**
- PK: agreementId, agreementItemSeqId, partyIdTo, partyIdFrom, roleTypeIdTo, roleTypeIdFrom, fromDate
- Felder: 9
- Beziehungen zu: Agreement, AgreementItem, Employment

**AgreementFacilityAppl**
- PK: agreementId, agreementItemSeqId, facilityId
- Felder: 3
- Beziehungen zu: Agreement, AgreementItem, Facility

**AgreementGeographicalApplic**
- PK: agreementId, agreementItemSeqId, geoId
- Felder: 3
- Beziehungen zu: Agreement, AgreementItem, Geo

**AgreementItem**
- PK: agreementId, agreementItemSeqId
- Felder: 6
- Beziehungen zu: Agreement, AgreementItemType, AgreementItemTypeAttr

**AgreementItemAttribute**
- PK: agreementId, agreementItemSeqId, attrName
- Felder: 5
- Beziehungen zu: AgreementItem, AgreementItemTypeAttr

**AgreementItemType**
- PK: agreementItemTypeId
- Felder: 4
- Beziehungen zu: AgreementItemType

**AgreementItemTypeAttr**
- PK: agreementItemTypeId, attrName
- Felder: 3
- Beziehungen zu: AgreementItem, AgreementItemAttribute, AgreementItemType

**AgreementPartyApplic**
- PK: agreementId, agreementItemSeqId, partyId
- Felder: 3
- Beziehungen zu: Agreement, AgreementItem, Party

**AgreementProductAppl**
- PK: agreementId, agreementItemSeqId, productId
- Felder: 4
- Beziehungen zu: Agreement, AgreementItem, Product

**AgreementPromoAppl**
- PK: agreementId, agreementItemSeqId, productPromoId, fromDate
- Felder: 6
- Beziehungen zu: Agreement, AgreementItem, ProductPromo

**AgreementRole**
- PK: agreementId, partyId, roleTypeId
- Felder: 3
- Beziehungen zu: Agreement, Party, PartyRole, RoleType

**AgreementStatus**
- PK: agreementId, statusId, statusDate
- Felder: 5
- Beziehungen zu: Agreement, StatusItem, UserLogin

**AgreementTerm**
- PK: agreementTermId
- Felder: 13
- Beziehungen zu: Agreement, AgreementItem, InvoiceItemType, TermType

**AgreementTermAttribute**
- PK: agreementTermId, attrName
- Felder: 4
- Beziehungen zu: AgreementTerm

**AgreementType**
- PK: agreementTypeId
- Felder: 4
- Beziehungen zu: AgreementType

**AgreementTypeAttr**
- PK: agreementTypeId, attrName
- Felder: 3
- Beziehungen zu: Agreement, AgreementAttribute, AgreementType

**AgreementWorkEffortApplic**
- PK: agreementId, agreementItemSeqId, workEffortId
- Felder: 3
- Beziehungen zu: Agreement, AgreementItem, WorkEffort

**TermType**
- PK: termTypeId
- Felder: 4
- Beziehungen zu: TermType

**TermTypeAttr**
- PK: termTypeId, attrName
- Felder: 3
- Beziehungen zu: AgreementTerm, AgreementTermAttribute, InvoiceTerm, InvoiceTermAttribute, OrderTerm (+4 weitere)

### Kategorie: Communication

**CommContentAssocType**
- PK: commContentAssocTypeId
- Felder: 2
- Beziehungen zu: keine

**CommEventContentAssoc**
- PK: contentId, communicationEventId, fromDate
- Felder: 6
- Beziehungen zu: CommContentAssocType, CommunicationEvent, Content

**CommunicationEvent**
- PK: communicationEventId
- Felder: 27
- Beziehungen zu: CommunicationEventType, ContactList, ContactMech, ContactMechType, Enumeration (+5 weitere)

**CommunicationEventProduct**
- PK: productId, communicationEventId
- Felder: 2
- Beziehungen zu: CommunicationEvent, Product

**CommunicationEventPrpTyp**
- PK: communicationEventPrpTypId
- Felder: 4
- Beziehungen zu: CommunicationEventPrpTyp

**CommunicationEventPurpose**
- PK: communicationEventPrpTypId, communicationEventId
- Felder: 3
- Beziehungen zu: CommunicationEvent, CommunicationEventPrpTyp

**CommunicationEventRole**
- PK: communicationEventId, partyId, roleTypeId
- Felder: 5
- Beziehungen zu: CommunicationEvent, ContactMech, Party, PartyRole, RoleType (+1 weitere)

**CommunicationEventType**
- PK: communicationEventTypeId
- Felder: 5
- Beziehungen zu: CommunicationEventType, ContactMechType

### Kategorie: Contact

**ContactMechAttribute**
- PK: contactMechId, attrName
- Felder: 4
- Beziehungen zu: ContactMech, ContactMechTypeAttr

**ContactMechLink**
- PK: contactMechIdFrom, contactMechIdTo
- Felder: 2
- Beziehungen zu: ContactMech

**ContactMechPurposeType**
- PK: contactMechPurposeTypeId
- Felder: 4
- Beziehungen zu: keine

**ContactMechTypeAttr**
- PK: contactMechTypeId, attrName
- Felder: 3
- Beziehungen zu: ContactMech, ContactMechAttribute, ContactMechType

**ContactMechTypePurpose**
- PK: contactMechTypeId, contactMechPurposeTypeId
- Felder: 2
- Beziehungen zu: ContactMechPurposeType, ContactMechType

**FtpAddress**
- PK: contactMechId
- Felder: 10
- Beziehungen zu: ContactMech

**PartyContactMechPurpose**
- PK: partyId, contactMechId, contactMechPurposeTypeId, fromDate
- Felder: 5
- Beziehungen zu: ContactMech, ContactMechPurposeType, Party, PartyContactMech, PartyGroup (+3 weitere)

**PostalAddressBoundary**
- PK: contactMechId, geoId
- Felder: 2
- Beziehungen zu: Geo, PostalAddress

**ValidContactMechRole**
- PK: roleTypeId, contactMechTypeId
- Felder: 2
- Beziehungen zu: ContactMechType, RoleType

### Kategorie: Party

**AddressMatchMap**
- PK: mapKey, mapValue
- Felder: 3
- Beziehungen zu: keine

**Affiliate**
- PK: partyId
- Felder: 9
- Beziehungen zu: Party, PartyGroup

**NeedType**
- PK: needTypeId
- Felder: 2
- Beziehungen zu: keine

**PartyAttribute**
- PK: partyId, attrName
- Felder: 4
- Beziehungen zu: Party, PartyTypeAttr

**PartyCarrierAccount**
- PK: partyId, carrierPartyId, fromDate
- Felder: 5
- Beziehungen zu: Party

**PartyClassification**
- PK: partyId, partyClassificationGroupId, fromDate
- Felder: 4
- Beziehungen zu: Party, PartyClassificationGroup

**PartyClassificationGroup**
- PK: partyClassificationGroupId
- Felder: 4
- Beziehungen zu: PartyClassificationGroup, PartyClassificationType

**PartyClassificationType**
- PK: partyClassificationTypeId
- Felder: 4
- Beziehungen zu: PartyClassificationType

**PartyContent**
- PK: partyId, contentId, partyContentTypeId, fromDate
- Felder: 5
- Beziehungen zu: Content, Party, PartyContentType

**PartyContentType**
- PK: partyContentTypeId
- Felder: 3
- Beziehungen zu: PartyContentType

**PartyDataSource**
- PK: partyId, dataSourceId, fromDate
- Felder: 6
- Beziehungen zu: DataSource, Party

**PartyGeoPoint**
- PK: partyId, geoPointId, fromDate
- Felder: 4
- Beziehungen zu: GeoPoint, Party

**PartyIcsAvsOverride**
- PK: partyId
- Felder: 2
- Beziehungen zu: Party

**PartyIdentification**
- PK: partyId, partyIdentificationTypeId
- Felder: 3
- Beziehungen zu: Party, PartyIdentificationType

**PartyIdentificationType**
- PK: partyIdentificationTypeId
- Felder: 4
- Beziehungen zu: PartyIdentificationType

**PartyInvitation**
- PK: partyInvitationId
- Felder: 7
- Beziehungen zu: Party, StatusItem

**PartyInvitationGroupAssoc**
- PK: partyInvitationId, partyIdTo
- Felder: 2
- Beziehungen zu: Party, PartyGroup, PartyInvitation

**PartyInvitationRoleAssoc**
- PK: partyInvitationId, roleTypeId
- Felder: 2
- Beziehungen zu: PartyInvitation, RoleType

**PartyNameHistory**
- PK: partyId, changeDate
- Felder: 8
- Beziehungen zu: Party

**PartyNeed**
- PK: partyNeedId, partyId, roleTypeId
- Felder: 11
- Beziehungen zu: CommunicationEvent, NeedType, Party, PartyRole, PartyType (+3 weitere)

**PartyNote**
- PK: partyId, noteId
- Felder: 2
- Beziehungen zu: NoteData, Party

**PartyProfileDefault**
- PK: partyId, productStoreId
- Felder: 6
- Beziehungen zu: Party, ProductStore

**PartyRelationshipType**
- PK: partyRelationshipTypeId
- Felder: 7
- Beziehungen zu: PartyRelationshipType, RoleType

**PartyStatus**
- PK: statusId, partyId, statusDate
- Felder: 4
- Beziehungen zu: Party, StatusItem, UserLogin

**PartyType**
- PK: partyTypeId
- Felder: 4
- Beziehungen zu: PartyType

**PartyTypeAttr**
- PK: partyTypeId, attrName
- Felder: 3
- Beziehungen zu: Party, PartyAttribute, PartyType

**PriorityType**
- PK: priorityTypeId
- Felder: 2
- Beziehungen zu: keine

**RoleTypeAttr**
- PK: roleTypeId, attrName
- Felder: 3
- Beziehungen zu: PartyAttribute, PartyRelationshipType, PartyRole, RoleType

**Vendor**
- PK: partyId
- Felder: 5
- Beziehungen zu: Party

