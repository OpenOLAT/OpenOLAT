-- Passkey
alter table o_bs_authentication add w_counter number(20) default 0;
alter table o_bs_authentication add w_aaguid raw(16);
alter table o_bs_authentication add w_credential_id raw(1024);
alter table o_bs_authentication add w_user_handle raw(64);
alter table o_bs_authentication add w_cose_key raw(1024);
alter table o_bs_authentication add w_attestation_object clob;
alter table o_bs_authentication add w_client_extensions clob;
alter table o_bs_authentication add w_authenticator_extensions clob;

