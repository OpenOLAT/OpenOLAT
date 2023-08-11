-- Passkey
alter table o_bs_authentication add column w_counter int8 default 0;
alter table o_bs_authentication add column w_aaguid bytea;
alter table o_bs_authentication add column w_credential_id bytea;
alter table o_bs_authentication add column w_user_handle bytea;
alter table o_bs_authentication add column w_cose_key bytea;
alter table o_bs_authentication add column w_attestation_object text;
alter table o_bs_authentication add column w_client_extensions text;
alter table o_bs_authentication add column w_authenticator_extensions text;

