package org.apache.ofbiz.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ofbiz.sync.event.PartyEvent;
import org.apache.ofbiz.sync.snapshot.PartyGroupSnapshot;
import org.apache.ofbiz.sync.snapshot.PersonSnapshot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class OFBizSyncService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void createPerson(PartyEvent event) {
        log.info("Creating person in OFBiz: {} {} ({})",
                event.getFirstName(), event.getLastName(), event.getPartyId());

        // First, create the Party record
        String insertParty = """
            INSERT INTO PARTY (PARTY_ID, PARTY_TYPE_ID, STATUS_ID, CREATED_STAMP, CREATED_TX_STAMP, LAST_UPDATED_STAMP, LAST_UPDATED_TX_STAMP)
            VALUES (?, 'PERSON', ?, ?, ?, ?, ?)
            """;

        Timestamp now = Timestamp.from(Instant.now());
        jdbcTemplate.update(insertParty,
                event.getPartyId(),
                event.getStatusId() != null ? event.getStatusId() : "PARTY_ENABLED",
                now, now, now, now);

        // Then, create the Person record
        String insertPerson = """
            INSERT INTO PERSON (PARTY_ID, FIRST_NAME, LAST_NAME, MIDDLE_NAME, GENDER, BIRTH_DATE,
                               CREATED_STAMP, CREATED_TX_STAMP, LAST_UPDATED_STAMP, LAST_UPDATED_TX_STAMP)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(insertPerson,
                event.getPartyId(),
                event.getFirstName(),
                event.getLastName(),
                event.getMiddleName(),
                event.getGender(),
                event.getBirthDate() != null ? Date.valueOf(event.getBirthDate()) : null,
                now, now, now, now);

        log.info("Successfully created person in OFBiz: {}", event.getPartyId());
    }

    @Transactional
    public void updatePerson(PartyEvent event) {
        log.info("Updating person in OFBiz: {} {} ({})",
                event.getFirstName(), event.getLastName(), event.getPartyId());

        String updatePerson = """
            UPDATE PERSON SET
                FIRST_NAME = ?,
                LAST_NAME = ?,
                MIDDLE_NAME = ?,
                GENDER = ?,
                BIRTH_DATE = ?,
                LAST_UPDATED_STAMP = ?,
                LAST_UPDATED_TX_STAMP = ?
            WHERE PARTY_ID = ?
            """;

        Timestamp now = Timestamp.from(Instant.now());
        int updated = jdbcTemplate.update(updatePerson,
                event.getFirstName(),
                event.getLastName(),
                event.getMiddleName(),
                event.getGender(),
                event.getBirthDate() != null ? Date.valueOf(event.getBirthDate()) : null,
                now, now,
                event.getPartyId());

        if (updated == 0) {
            log.warn("Person not found in OFBiz for update: {}, creating instead", event.getPartyId());
            createPerson(event);
        } else {
            log.info("Successfully updated person in OFBiz: {}", event.getPartyId());
        }
    }

    @Transactional
    public void deletePerson(String partyId) {
        log.info("Deleting person in OFBiz: {}", partyId);

        // Delete Person first (child table)
        jdbcTemplate.update("DELETE FROM PERSON WHERE PARTY_ID = ?", partyId);

        // Delete Party (parent table)
        jdbcTemplate.update("DELETE FROM PARTY WHERE PARTY_ID = ?", partyId);

        log.info("Successfully deleted person in OFBiz: {}", partyId);
    }

    @Transactional
    public void createPartyGroup(PartyEvent event) {
        log.info("Creating party group in OFBiz: {} ({})",
                event.getGroupName(), event.getPartyId());

        // First, create the Party record
        String insertParty = """
            INSERT INTO PARTY (PARTY_ID, PARTY_TYPE_ID, STATUS_ID, CREATED_STAMP, CREATED_TX_STAMP, LAST_UPDATED_STAMP, LAST_UPDATED_TX_STAMP)
            VALUES (?, 'PARTY_GROUP', ?, ?, ?, ?, ?)
            """;

        Timestamp now = Timestamp.from(Instant.now());
        jdbcTemplate.update(insertParty,
                event.getPartyId(),
                event.getStatusId() != null ? event.getStatusId() : "PARTY_ENABLED",
                now, now, now, now);

        // Then, create the PartyGroup record
        String insertPartyGroup = """
            INSERT INTO PARTY_GROUP (PARTY_ID, GROUP_NAME, GROUP_NAME_LOCAL, TICKER_SYMBOL,
                                    CREATED_STAMP, CREATED_TX_STAMP, LAST_UPDATED_STAMP, LAST_UPDATED_TX_STAMP)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(insertPartyGroup,
                event.getPartyId(),
                event.getGroupName(),
                event.getGroupNameLocal(),
                event.getTickerSymbol(),
                now, now, now, now);

        log.info("Successfully created party group in OFBiz: {}", event.getPartyId());
    }

    @Transactional
    public void updatePartyGroup(PartyEvent event) {
        log.info("Updating party group in OFBiz: {} ({})",
                event.getGroupName(), event.getPartyId());

        String updatePartyGroup = """
            UPDATE PARTY_GROUP SET
                GROUP_NAME = ?,
                GROUP_NAME_LOCAL = ?,
                TICKER_SYMBOL = ?,
                LAST_UPDATED_STAMP = ?,
                LAST_UPDATED_TX_STAMP = ?
            WHERE PARTY_ID = ?
            """;

        Timestamp now = Timestamp.from(Instant.now());
        int updated = jdbcTemplate.update(updatePartyGroup,
                event.getGroupName(),
                event.getGroupNameLocal(),
                event.getTickerSymbol(),
                now, now,
                event.getPartyId());

        if (updated == 0) {
            log.warn("PartyGroup not found in OFBiz for update: {}, creating instead", event.getPartyId());
            createPartyGroup(event);
        } else {
            log.info("Successfully updated party group in OFBiz: {}", event.getPartyId());
        }
    }

    @Transactional
    public void deletePartyGroup(String partyId) {
        log.info("Deleting party group in OFBiz: {}", partyId);

        // Delete PartyGroup first (child table)
        jdbcTemplate.update("DELETE FROM PARTY_GROUP WHERE PARTY_ID = ?", partyId);

        // Delete Party (parent table)
        jdbcTemplate.update("DELETE FROM PARTY WHERE PARTY_ID = ?", partyId);

        log.info("Successfully deleted party group in OFBiz: {}", partyId);
    }

    // ========== Snapshot-based methods for compacted topic ==========

    @Transactional
    public void upsertPerson(PersonSnapshot snapshot) {
        log.info("Upserting person in OFBiz: {} {} ({})",
                snapshot.getFirstName(), snapshot.getLastName(), snapshot.getPartyId());

        Timestamp now = Timestamp.from(Instant.now());

        // Check if party exists
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM PARTY WHERE PARTY_ID = ?",
                Integer.class, snapshot.getPartyId());

        if (count != null && count > 0) {
            // Update existing
            updatePersonFromSnapshot(snapshot, now);
        } else {
            // Insert new
            insertPersonFromSnapshot(snapshot, now);
        }
    }

    private void insertPersonFromSnapshot(PersonSnapshot snapshot, Timestamp now) {
        // Insert Party
        String insertParty = """
            INSERT INTO PARTY (PARTY_ID, PARTY_TYPE_ID, STATUS_ID, DESCRIPTION,
                              CREATED_STAMP, CREATED_TX_STAMP, LAST_UPDATED_STAMP, LAST_UPDATED_TX_STAMP)
            VALUES (?, 'PERSON', ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(insertParty,
                snapshot.getPartyId(),
                snapshot.getStatusId() != null ? snapshot.getStatusId() : "PARTY_ENABLED",
                snapshot.getDescription(),
                now, now, now, now);

        // Insert Person
        String insertPerson = """
            INSERT INTO PERSON (PARTY_ID, FIRST_NAME, LAST_NAME, MIDDLE_NAME, PERSONAL_TITLE,
                               SUFFIX, NICKNAME, GENDER, BIRTH_DATE, DECEASED_DATE, MARITAL_STATUS,
                               SOCIAL_SECURITY_NUMBER, PASSPORT_NUMBER, PASSPORT_EXPIRE_DATE,
                               EMPLOYMENT_STATUS_ENUM_ID, RESIDENCE_STATUS_ENUM_ID, OCCUPATION,
                               YEARS_WITH_EMPLOYER, MONTHS_WITH_EMPLOYER, MEMBER_ID,
                               CREATED_STAMP, CREATED_TX_STAMP, LAST_UPDATED_STAMP, LAST_UPDATED_TX_STAMP)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(insertPerson,
                snapshot.getPartyId(),
                snapshot.getFirstName(),
                snapshot.getLastName(),
                snapshot.getMiddleName(),
                snapshot.getPersonalTitle(),
                snapshot.getSuffix(),
                snapshot.getNickname(),
                snapshot.getGender(),
                snapshot.getBirthDate() != null ? Date.valueOf(snapshot.getBirthDate()) : null,
                snapshot.getDeceasedDate() != null ? Date.valueOf(snapshot.getDeceasedDate()) : null,
                snapshot.getMaritalStatusEnumId(),
                snapshot.getSocialSecurityNumber(),
                snapshot.getPassportNumber(),
                snapshot.getPassportExpireDate() != null ? Date.valueOf(snapshot.getPassportExpireDate()) : null,
                snapshot.getEmploymentStatusEnumId(),
                snapshot.getResidenceStatusEnumId(),
                snapshot.getOccupation(),
                snapshot.getYearsWithEmployer(),
                snapshot.getMonthsWithEmployer(),
                snapshot.getMemberId(),
                now, now, now, now);

        log.info("Successfully inserted person in OFBiz: {}", snapshot.getPartyId());
    }

    private void updatePersonFromSnapshot(PersonSnapshot snapshot, Timestamp now) {
        // Update Party
        String updateParty = """
            UPDATE PARTY SET
                STATUS_ID = ?,
                DESCRIPTION = ?,
                LAST_UPDATED_STAMP = ?,
                LAST_UPDATED_TX_STAMP = ?
            WHERE PARTY_ID = ?
            """;

        jdbcTemplate.update(updateParty,
                snapshot.getStatusId(),
                snapshot.getDescription(),
                now, now,
                snapshot.getPartyId());

        // Update Person
        String updatePerson = """
            UPDATE PERSON SET
                FIRST_NAME = ?,
                LAST_NAME = ?,
                MIDDLE_NAME = ?,
                PERSONAL_TITLE = ?,
                SUFFIX = ?,
                NICKNAME = ?,
                GENDER = ?,
                BIRTH_DATE = ?,
                DECEASED_DATE = ?,
                MARITAL_STATUS = ?,
                SOCIAL_SECURITY_NUMBER = ?,
                PASSPORT_NUMBER = ?,
                PASSPORT_EXPIRE_DATE = ?,
                EMPLOYMENT_STATUS_ENUM_ID = ?,
                RESIDENCE_STATUS_ENUM_ID = ?,
                OCCUPATION = ?,
                YEARS_WITH_EMPLOYER = ?,
                MONTHS_WITH_EMPLOYER = ?,
                MEMBER_ID = ?,
                LAST_UPDATED_STAMP = ?,
                LAST_UPDATED_TX_STAMP = ?
            WHERE PARTY_ID = ?
            """;

        jdbcTemplate.update(updatePerson,
                snapshot.getFirstName(),
                snapshot.getLastName(),
                snapshot.getMiddleName(),
                snapshot.getPersonalTitle(),
                snapshot.getSuffix(),
                snapshot.getNickname(),
                snapshot.getGender(),
                snapshot.getBirthDate() != null ? Date.valueOf(snapshot.getBirthDate()) : null,
                snapshot.getDeceasedDate() != null ? Date.valueOf(snapshot.getDeceasedDate()) : null,
                snapshot.getMaritalStatusEnumId(),
                snapshot.getSocialSecurityNumber(),
                snapshot.getPassportNumber(),
                snapshot.getPassportExpireDate() != null ? Date.valueOf(snapshot.getPassportExpireDate()) : null,
                snapshot.getEmploymentStatusEnumId(),
                snapshot.getResidenceStatusEnumId(),
                snapshot.getOccupation(),
                snapshot.getYearsWithEmployer(),
                snapshot.getMonthsWithEmployer(),
                snapshot.getMemberId(),
                now, now,
                snapshot.getPartyId());

        log.info("Successfully updated person in OFBiz: {}", snapshot.getPartyId());
    }

    @Transactional
    public void upsertPartyGroup(PartyGroupSnapshot snapshot) {
        log.info("Upserting party group in OFBiz: {} ({})",
                snapshot.getGroupName(), snapshot.getPartyId());

        Timestamp now = Timestamp.from(Instant.now());

        // Check if party exists
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM PARTY WHERE PARTY_ID = ?",
                Integer.class, snapshot.getPartyId());

        if (count != null && count > 0) {
            // Update existing
            updatePartyGroupFromSnapshot(snapshot, now);
        } else {
            // Insert new
            insertPartyGroupFromSnapshot(snapshot, now);
        }
    }

    private void insertPartyGroupFromSnapshot(PartyGroupSnapshot snapshot, Timestamp now) {
        // Insert Party
        String insertParty = """
            INSERT INTO PARTY (PARTY_ID, PARTY_TYPE_ID, STATUS_ID, DESCRIPTION,
                              CREATED_STAMP, CREATED_TX_STAMP, LAST_UPDATED_STAMP, LAST_UPDATED_TX_STAMP)
            VALUES (?, 'PARTY_GROUP', ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(insertParty,
                snapshot.getPartyId(),
                snapshot.getStatusId() != null ? snapshot.getStatusId() : "PARTY_ENABLED",
                snapshot.getDescription(),
                now, now, now, now);

        // Insert PartyGroup
        String insertPartyGroup = """
            INSERT INTO PARTY_GROUP (PARTY_ID, GROUP_NAME, GROUP_NAME_LOCAL, OFFICE_SITE_NAME,
                                    ANNUAL_REVENUE, NUM_EMPLOYEES, TICKER_SYMBOL, COMMENTS, LOGO_IMAGE_URL,
                                    CREATED_STAMP, CREATED_TX_STAMP, LAST_UPDATED_STAMP, LAST_UPDATED_TX_STAMP)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(insertPartyGroup,
                snapshot.getPartyId(),
                snapshot.getGroupName(),
                snapshot.getGroupNameLocal(),
                snapshot.getOfficeSiteName(),
                snapshot.getAnnualRevenue(),
                snapshot.getNumEmployees(),
                snapshot.getTickerSymbol(),
                snapshot.getComments(),
                snapshot.getLogoImageUrl(),
                now, now, now, now);

        log.info("Successfully inserted party group in OFBiz: {}", snapshot.getPartyId());
    }

    private void updatePartyGroupFromSnapshot(PartyGroupSnapshot snapshot, Timestamp now) {
        // Update Party
        String updateParty = """
            UPDATE PARTY SET
                STATUS_ID = ?,
                DESCRIPTION = ?,
                LAST_UPDATED_STAMP = ?,
                LAST_UPDATED_TX_STAMP = ?
            WHERE PARTY_ID = ?
            """;

        jdbcTemplate.update(updateParty,
                snapshot.getStatusId(),
                snapshot.getDescription(),
                now, now,
                snapshot.getPartyId());

        // Update PartyGroup
        String updatePartyGroup = """
            UPDATE PARTY_GROUP SET
                GROUP_NAME = ?,
                GROUP_NAME_LOCAL = ?,
                OFFICE_SITE_NAME = ?,
                ANNUAL_REVENUE = ?,
                NUM_EMPLOYEES = ?,
                TICKER_SYMBOL = ?,
                COMMENTS = ?,
                LOGO_IMAGE_URL = ?,
                LAST_UPDATED_STAMP = ?,
                LAST_UPDATED_TX_STAMP = ?
            WHERE PARTY_ID = ?
            """;

        jdbcTemplate.update(updatePartyGroup,
                snapshot.getGroupName(),
                snapshot.getGroupNameLocal(),
                snapshot.getOfficeSiteName(),
                snapshot.getAnnualRevenue(),
                snapshot.getNumEmployees(),
                snapshot.getTickerSymbol(),
                snapshot.getComments(),
                snapshot.getLogoImageUrl(),
                now, now,
                snapshot.getPartyId());

        log.info("Successfully updated party group in OFBiz: {}", snapshot.getPartyId());
    }

    @Transactional
    public void deleteParty(String partyId) {
        log.info("Deleting party in OFBiz: {}", partyId);

        // Try to delete from both child tables (one will succeed, one will be no-op)
        jdbcTemplate.update("DELETE FROM PERSON WHERE PARTY_ID = ?", partyId);
        jdbcTemplate.update("DELETE FROM PARTY_GROUP WHERE PARTY_ID = ?", partyId);

        // Delete from parent table
        jdbcTemplate.update("DELETE FROM PARTY WHERE PARTY_ID = ?", partyId);

        log.info("Successfully deleted party in OFBiz: {}", partyId);
    }
}
