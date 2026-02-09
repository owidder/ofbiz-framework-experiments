package org.apache.ofbiz.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ofbiz.sync.event.PartyEvent;
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
}
