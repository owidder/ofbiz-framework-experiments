-- Party Service Schema
-- Based on Apache OFBiz Party Entity Model
-- Version 1: Core Party Entities

-- =====================================================
-- PARTY TYPE (Reference Table)
-- =====================================================
CREATE TABLE party_type (
    party_type_id VARCHAR(20) PRIMARY KEY,
    parent_type_id VARCHAR(20),
    has_table VARCHAR(1),
    description VARCHAR(255),
    CONSTRAINT fk_party_type_parent FOREIGN KEY (parent_type_id) REFERENCES party_type(party_type_id)
);

-- =====================================================
-- PARTY (Core Entity)
-- =====================================================
CREATE TABLE party (
    party_id VARCHAR(20) PRIMARY KEY,
    party_type_id VARCHAR(20),
    external_id VARCHAR(20),
    preferred_currency_uom_id VARCHAR(20),
    description TEXT,
    status_id VARCHAR(20),
    created_date TIMESTAMP,
    created_by_user_login VARCHAR(255),
    last_modified_date TIMESTAMP,
    last_modified_by_user_login VARCHAR(255),
    data_source_id VARCHAR(20),
    is_unread VARCHAR(1),
    CONSTRAINT fk_party_type FOREIGN KEY (party_type_id) REFERENCES party_type(party_type_id)
);

CREATE INDEX idx_party_external_id ON party(external_id);
CREATE INDEX idx_party_status ON party(status_id);
CREATE INDEX idx_party_type ON party(party_type_id);

-- =====================================================
-- PERSON (Extends Party)
-- =====================================================
CREATE TABLE person (
    party_id VARCHAR(20) PRIMARY KEY,
    salutation VARCHAR(100),
    first_name VARCHAR(100),
    middle_name VARCHAR(100),
    last_name VARCHAR(100),
    personal_title VARCHAR(100),
    suffix VARCHAR(100),
    nickname VARCHAR(100),
    first_name_local VARCHAR(100),
    middle_name_local VARCHAR(100),
    last_name_local VARCHAR(100),
    other_local VARCHAR(100),
    member_id VARCHAR(20),
    gender VARCHAR(1),
    birth_date DATE,
    deceased_date DATE,
    height DOUBLE PRECISION,
    weight DOUBLE PRECISION,
    mothers_maiden_name TEXT,
    marital_status_enum_id VARCHAR(20),
    social_security_number TEXT,
    passport_number TEXT,
    passport_expire_date DATE,
    total_years_work_experience DOUBLE PRECISION,
    comments VARCHAR(255),
    employment_status_enum_id VARCHAR(20),
    residence_status_enum_id VARCHAR(20),
    occupation VARCHAR(100),
    years_with_employer INTEGER,
    months_with_employer INTEGER,
    existing_customer VARCHAR(1),
    card_id VARCHAR(60),
    CONSTRAINT fk_person_party FOREIGN KEY (party_id) REFERENCES party(party_id)
);

CREATE INDEX idx_person_first_name ON person(first_name);
CREATE INDEX idx_person_last_name ON person(last_name);
CREATE INDEX idx_person_member_id ON person(member_id);

-- =====================================================
-- PARTY GROUP (Extends Party)
-- =====================================================
CREATE TABLE party_group (
    party_id VARCHAR(20) PRIMARY KEY,
    group_name VARCHAR(100),
    group_name_local VARCHAR(100),
    office_site_name VARCHAR(100),
    annual_revenue NUMERIC(18,2),
    num_employees INTEGER,
    ticker_symbol VARCHAR(10),
    comments VARCHAR(255),
    logo_image_url VARCHAR(2000),
    CONSTRAINT fk_party_group_party FOREIGN KEY (party_id) REFERENCES party(party_id)
);

CREATE INDEX idx_party_group_name ON party_group(group_name);

-- =====================================================
-- CONTACT MECH TYPE (Reference Table)
-- =====================================================
CREATE TABLE contact_mech_type (
    contact_mech_type_id VARCHAR(20) PRIMARY KEY,
    parent_type_id VARCHAR(20),
    has_table VARCHAR(1),
    description VARCHAR(255),
    CONSTRAINT fk_contact_mech_type_parent FOREIGN KEY (parent_type_id) REFERENCES contact_mech_type(contact_mech_type_id)
);

-- =====================================================
-- CONTACT MECH (Contact Information)
-- =====================================================
CREATE TABLE contact_mech (
    contact_mech_id VARCHAR(20) PRIMARY KEY,
    contact_mech_type_id VARCHAR(20),
    info_string TEXT,
    CONSTRAINT fk_contact_mech_type FOREIGN KEY (contact_mech_type_id) REFERENCES contact_mech_type(contact_mech_type_id)
);

CREATE INDEX idx_contact_mech_info ON contact_mech(info_string);
CREATE INDEX idx_contact_mech_type ON contact_mech(contact_mech_type_id);

-- =====================================================
-- POSTAL ADDRESS (Extends ContactMech)
-- =====================================================
CREATE TABLE postal_address (
    contact_mech_id VARCHAR(20) PRIMARY KEY,
    to_name VARCHAR(100),
    attn_name VARCHAR(100),
    address1 TEXT,
    address2 TEXT,
    house_number INTEGER,
    house_number_ext VARCHAR(60),
    directions TEXT,
    city VARCHAR(100),
    city_geo_id VARCHAR(20),
    postal_code VARCHAR(60),
    postal_code_ext VARCHAR(60),
    country_geo_id VARCHAR(20),
    state_province_geo_id VARCHAR(20),
    county_geo_id VARCHAR(20),
    municipality_geo_id VARCHAR(20),
    postal_code_geo_id VARCHAR(20),
    geo_point_id VARCHAR(20),
    CONSTRAINT fk_postal_address_contact_mech FOREIGN KEY (contact_mech_id) REFERENCES contact_mech(contact_mech_id)
);

CREATE INDEX idx_postal_address1 ON postal_address(address1);
CREATE INDEX idx_postal_city ON postal_address(city);
CREATE INDEX idx_postal_code ON postal_address(postal_code);

-- =====================================================
-- TELECOM NUMBER (Extends ContactMech)
-- =====================================================
CREATE TABLE telecom_number (
    contact_mech_id VARCHAR(20) PRIMARY KEY,
    country_code VARCHAR(10),
    area_code VARCHAR(10),
    contact_number VARCHAR(60),
    CONSTRAINT fk_telecom_contact_mech FOREIGN KEY (contact_mech_id) REFERENCES contact_mech(contact_mech_id)
);

CREATE INDEX idx_telecom_contact_number ON telecom_number(contact_number);

-- =====================================================
-- ROLE TYPE (Reference Table)
-- =====================================================
CREATE TABLE role_type (
    role_type_id VARCHAR(20) PRIMARY KEY,
    parent_type_id VARCHAR(20),
    has_table VARCHAR(1),
    description VARCHAR(255),
    CONSTRAINT fk_role_type_parent FOREIGN KEY (parent_type_id) REFERENCES role_type(role_type_id)
);

-- =====================================================
-- PARTY ROLE (Many-to-Many: Party <-> RoleType)
-- =====================================================
CREATE TABLE party_role (
    party_id VARCHAR(20),
    role_type_id VARCHAR(20),
    PRIMARY KEY (party_id, role_type_id),
    CONSTRAINT fk_party_role_party FOREIGN KEY (party_id) REFERENCES party(party_id),
    CONSTRAINT fk_party_role_type FOREIGN KEY (role_type_id) REFERENCES role_type(role_type_id)
);

-- =====================================================
-- CONTACT MECH PURPOSE TYPE (Reference Table)
-- =====================================================
CREATE TABLE contact_mech_purpose_type (
    contact_mech_purpose_type_id VARCHAR(20) PRIMARY KEY,
    parent_type_id VARCHAR(20),
    has_table VARCHAR(1),
    description VARCHAR(255)
);

-- =====================================================
-- PARTY CONTACT MECH (Many-to-Many: Party <-> ContactMech)
-- =====================================================
CREATE TABLE party_contact_mech (
    party_id VARCHAR(20),
    contact_mech_id VARCHAR(20),
    from_date TIMESTAMP,
    thru_date TIMESTAMP,
    role_type_id VARCHAR(20),
    allow_solicitation VARCHAR(1),
    extension TEXT,
    verified VARCHAR(1),
    comments VARCHAR(255),
    years_with_contact_mech INTEGER,
    months_with_contact_mech INTEGER,
    PRIMARY KEY (party_id, contact_mech_id, from_date),
    CONSTRAINT fk_party_contact_mech_party FOREIGN KEY (party_id) REFERENCES party(party_id),
    CONSTRAINT fk_party_contact_mech_cm FOREIGN KEY (contact_mech_id) REFERENCES contact_mech(contact_mech_id),
    CONSTRAINT fk_party_contact_mech_role FOREIGN KEY (role_type_id) REFERENCES role_type(role_type_id)
);

CREATE INDEX idx_party_contact_mech_party ON party_contact_mech(party_id);
CREATE INDEX idx_party_contact_mech_cm ON party_contact_mech(contact_mech_id);

-- =====================================================
-- PARTY CONTACT MECH PURPOSE
-- =====================================================
CREATE TABLE party_contact_mech_purpose (
    party_id VARCHAR(20),
    contact_mech_id VARCHAR(20),
    contact_mech_purpose_type_id VARCHAR(20),
    from_date TIMESTAMP,
    thru_date TIMESTAMP,
    PRIMARY KEY (party_id, contact_mech_id, contact_mech_purpose_type_id, from_date),
    CONSTRAINT fk_pcmp_party FOREIGN KEY (party_id) REFERENCES party(party_id),
    CONSTRAINT fk_pcmp_contact_mech FOREIGN KEY (contact_mech_id) REFERENCES contact_mech(contact_mech_id),
    CONSTRAINT fk_pcmp_purpose_type FOREIGN KEY (contact_mech_purpose_type_id) REFERENCES contact_mech_purpose_type(contact_mech_purpose_type_id)
);
