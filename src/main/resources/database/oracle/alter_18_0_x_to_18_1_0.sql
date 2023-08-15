-- Passkey
alter table o_bs_authentication add w_counter number(20) default 0;
alter table o_bs_authentication add w_aaguid raw(16);
alter table o_bs_authentication add w_credential_id raw(1024);
alter table o_bs_authentication add w_user_handle raw(64);
alter table o_bs_authentication add w_cose_key raw(1024);
alter table o_bs_authentication add w_attestation_object clob;
alter table o_bs_authentication add w_client_extensions clob;
alter table o_bs_authentication add w_authenticator_extensions clob;
alter table o_bs_authentication add w_transports varchar(255);

create table o_bs_recovery_key (
   id number(20) generated always as identity,
   creationdate date not null,
   r_recovery_key_hash varchar(128),
   r_recovery_salt varchar(64),
   r_recovery_algorithm varchar(32),
   r_use_date date,
   fk_identity number(20) not null,
   primary key (id)
);

alter table o_bs_recovery_key add constraint rec_key_to_ident_idx foreign key (fk_identity) references o_bs_identity(id);
create index idx_rec_key_to_ident_idx on o_bs_recovery_key (fk_identity);
