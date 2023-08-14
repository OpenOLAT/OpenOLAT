-- Passkey
alter table o_bs_authentication add column w_counter int8 default 0;
alter table o_bs_authentication add column w_aaguid bytea;
alter table o_bs_authentication add column w_credential_id bytea;
alter table o_bs_authentication add column w_user_handle bytea;
alter table o_bs_authentication add column w_cose_key bytea;
alter table o_bs_authentication add column w_attestation_object text;
alter table o_bs_authentication add column w_client_extensions text;
alter table o_bs_authentication add column w_authenticator_extensions text;


create table o_bs_recovery_key (
   id bigserial,
   creationdate timestamp not null,
   r_recovery_key_hash varchar(128),
   r_recovery_salt varchar(64),
   r_recovery_algorithm varchar(32),
   r_use_date timestamp,
   fk_identity int8 not null,
   primary key (id)
);

alter table o_bs_recovery_key add constraint rec_key_to_ident_idx foreign key (fk_identity) references o_bs_identity(id);
create index idx_rec_key_to_ident_idx on o_bs_recovery_key (fk_identity);
