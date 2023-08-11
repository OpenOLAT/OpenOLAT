-- Passkey
alter table o_bs_authentication add column w_counter bigint default 0;
alter table o_bs_authentication add column w_aaguid varbinary(16);
alter table o_bs_authentication add column w_credential_id varbinary(1024);
alter table o_bs_authentication add column w_user_handle varbinary(64);
alter table o_bs_authentication add column w_cose_key varbinary(1024);
alter table o_bs_authentication add column w_attestation_object mediumtext;
alter table o_bs_authentication add column w_client_extensions mediumtext;
alter table o_bs_authentication add column w_authenticator_extensions mediumtext;

