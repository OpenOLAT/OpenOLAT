-- Passkey
alter table o_bs_authentication add column w_counter bigint default 0;
alter table o_bs_authentication add column w_aaguid varbinary(16);
alter table o_bs_authentication add column w_credential_id varbinary(1024);
alter table o_bs_authentication add column w_user_handle varbinary(64);
alter table o_bs_authentication add column w_cose_key varbinary(1024);
alter table o_bs_authentication add column w_attestation_object mediumtext;
alter table o_bs_authentication add column w_client_extensions mediumtext;
alter table o_bs_authentication add column w_authenticator_extensions mediumtext;
alter table o_bs_authentication add column w_transports varchar(255);


create table o_bs_recovery_key (
   id bigint not null auto_increment,
   creationdate datetime not null,
   r_recovery_key_hash varchar(128),
   r_recovery_salt varchar(64),
   r_recovery_algorithm varchar(32),
   r_use_date datetime,
   fk_identity bigint not null,
   primary key (id)
);
alter table o_bs_recovery_key ENGINE = InnoDB;

alter table o_bs_recovery_key add constraint rec_key_to_ident_idx foreign key (fk_identity) references o_bs_identity(id);

