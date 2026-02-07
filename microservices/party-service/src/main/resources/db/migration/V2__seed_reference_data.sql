-- Seed Reference Data for Party Service
-- Based on Apache OFBiz default data

-- =====================================================
-- PARTY TYPES
-- =====================================================
INSERT INTO party_type (party_type_id, parent_type_id, has_table, description) VALUES
('AUTOMATED_AGENT', NULL, 'N', 'Automated Agent'),
('PERSON', NULL, 'Y', 'Person'),
('PARTY_GROUP', NULL, 'Y', 'Party Group'),
('INFORMAL_GROUP', 'PARTY_GROUP', 'N', 'Informal Group'),
('LEGAL_ORGANIZATION', 'PARTY_GROUP', 'N', 'Legal Organization'),
('CORPORATION', 'LEGAL_ORGANIZATION', 'N', 'Corporation'),
('GOVERNMENT_AGENCY', 'LEGAL_ORGANIZATION', 'N', 'Government Agency'),
('FAMILY', 'INFORMAL_GROUP', 'N', 'Family'),
('TEAM', 'INFORMAL_GROUP', 'N', 'Team');

-- =====================================================
-- CONTACT MECH TYPES
-- =====================================================
INSERT INTO contact_mech_type (contact_mech_type_id, parent_type_id, has_table, description) VALUES
('ELECTRONIC_ADDRESS', NULL, 'N', 'Electronic Address'),
('POSTAL_ADDRESS', NULL, 'Y', 'Postal Address'),
('TELECOM_NUMBER', NULL, 'Y', 'Telecom Number'),
('EMAIL_ADDRESS', 'ELECTRONIC_ADDRESS', 'N', 'Email Address'),
('IP_ADDRESS', 'ELECTRONIC_ADDRESS', 'N', 'IP Address'),
('DOMAIN_NAME', 'ELECTRONIC_ADDRESS', 'N', 'Domain Name'),
('WEB_ADDRESS', 'ELECTRONIC_ADDRESS', 'N', 'Web Address'),
('INTERNAL_PARTYID', 'ELECTRONIC_ADDRESS', 'N', 'Internal Party ID'),
('FTP_ADDRESS', 'ELECTRONIC_ADDRESS', 'Y', 'FTP Address'),
('LDAP_ADDRESS', 'ELECTRONIC_ADDRESS', 'N', 'LDAP Address');

-- =====================================================
-- CONTACT MECH PURPOSE TYPES
-- =====================================================
INSERT INTO contact_mech_purpose_type (contact_mech_purpose_type_id, parent_type_id, has_table, description) VALUES
('BILLING_LOCATION', NULL, 'N', 'Billing (Invoicing) Location'),
('GENERAL_LOCATION', NULL, 'N', 'General Correspondence Location'),
('PAYMENT_LOCATION', NULL, 'N', 'Payment Location'),
('PORTING_LOCATION', NULL, 'N', 'Porting Location'),
('PRIMARY_EMAIL', NULL, 'N', 'Primary Email Address'),
('PRIMARY_LOCATION', NULL, 'N', 'Primary Location'),
('PRIMARY_PHONE', NULL, 'N', 'Primary Phone Number'),
('PRIMARY_WEB_URL', NULL, 'N', 'Primary Web URL'),
('SHIP_ORIG_LOCATION', NULL, 'N', 'Shipping Origin Location'),
('SHIPPING_LOCATION', NULL, 'N', 'Shipping Destination Location'),
('PREVIOUS_LOCATION', NULL, 'N', 'Previous Location'),
('FAX_NUMBER', NULL, 'N', 'Fax Number'),
('FAX_NUMBER_SEC', NULL, 'N', 'Fax Number Secondary'),
('PHONE_ASSISTANT', NULL, 'N', 'Assistant Phone Number'),
('PHONE_DID', NULL, 'N', 'DID Phone Number'),
('PHONE_HOME', NULL, 'N', 'Home Phone Number'),
('PHONE_MOBILE', NULL, 'N', 'Mobile Phone Number'),
('PHONE_QUICK', NULL, 'N', 'Quick Calls'),
('PHONE_WORK', NULL, 'N', 'Work Phone Number'),
('PHONE_WORK_SEC', NULL, 'N', 'Work Phone Number Secondary'),
('ORDER_EMAIL', NULL, 'N', 'Order Notification Email Address'),
('OTHER_EMAIL', NULL, 'N', 'Other Email Address');

-- =====================================================
-- ROLE TYPES
-- =====================================================
INSERT INTO role_type (role_type_id, parent_type_id, has_table, description) VALUES
('MAIN_ROLE', NULL, 'N', 'Main Role'),
('ACCOUNT', 'MAIN_ROLE', 'N', 'Account'),
('CUSTOMER', 'MAIN_ROLE', 'N', 'Customer'),
('PROSPECT', 'MAIN_ROLE', 'N', 'Prospect'),
('SUPPLIER', 'MAIN_ROLE', 'N', 'Supplier'),
('VENDOR', 'MAIN_ROLE', 'N', 'Vendor'),
('PARTNER', 'MAIN_ROLE', 'N', 'Partner'),
('CONTACT', 'MAIN_ROLE', 'N', 'Contact'),
('LEAD', 'MAIN_ROLE', 'N', 'Lead'),
('EMPLOYEE', 'MAIN_ROLE', 'N', 'Employee'),
('CONTRACTOR', 'MAIN_ROLE', 'N', 'Contractor'),
('INTERNAL_ORGANIZATIO', 'MAIN_ROLE', 'N', 'Internal Organization'),
('ORGANIZATION_UNIT', 'MAIN_ROLE', 'N', 'Organization Unit'),
('PARENT_ORGANIZATION', 'MAIN_ROLE', 'N', 'Parent Organization'),
('BILL_TO_CUSTOMER', 'CUSTOMER', 'N', 'Bill-To Customer'),
('SHIP_TO_CUSTOMER', 'CUSTOMER', 'N', 'Ship-To Customer'),
('END_USER_CUSTOMER', 'CUSTOMER', 'N', 'End-User Customer'),
('PLACING_CUSTOMER', 'CUSTOMER', 'N', 'Placing Customer'),
('BILL_FROM_VENDOR', 'VENDOR', 'N', 'Bill-From Vendor'),
('SHIP_FROM_VENDOR', 'VENDOR', 'N', 'Ship-From Vendor'),
('SUPPLIER_AGENT', 'SUPPLIER', 'N', 'Supplier Agent'),
('CARRIER', 'MAIN_ROLE', 'N', 'Carrier'),
('OWNER', 'MAIN_ROLE', 'N', 'Owner'),
('MANAGER', 'MAIN_ROLE', 'N', 'Manager'),
('SALES_REP', 'MAIN_ROLE', 'N', 'Sales Representative'),
('AGENT', 'MAIN_ROLE', 'N', 'Agent'),
('CLIENT', 'MAIN_ROLE', 'N', 'Client');
