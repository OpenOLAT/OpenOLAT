-- Credit point module
create table o_cp_system (
   id bigserial,
   creationdate timestamp,
   lastmodified timestamp,
   c_name varchar(255) not null,
   c_label varchar(16) not null,
   c_description varchar(4000),
   c_def_expiration int8,
   c_def_expiration_unit varchar(16),
   c_status varchar(16) not null,
   primary key (id)
);

create table o_cp_wallet (
   id bigserial,
   creationdate timestamp,
   lastmodified timestamp,
   c_balance decimal default null,
   c_balance_recalc timestamp,
   fk_identity int8 not null,
   fk_system int8 not null,
   primary key (id)
);

alter table o_cp_wallet add constraint unique_wallet_sys_ident unique (fk_identity, fk_system);

alter table o_cp_wallet add constraint cp_wallet_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_cp_wallet_to_identity_idx on o_cp_wallet (fk_identity);
alter table o_cp_wallet add constraint cp_wallet_to_system_idx foreign key (fk_system) references o_cp_system (id);
create index idx_cp_wallet_to_system_idx on o_cp_wallet (fk_system);

create table o_cp_transaction (
   id bigserial,
   creationdate timestamp,
   c_type varchar(16) not null,
   c_amount decimal not null,
   c_remaining_amount decimal,
   c_expiration_date timestamp,
   c_note varchar(4000),
   c_origin_run int8,
   c_destination_run int8,
   fk_wallet int8 not null,
   fk_creator int8,
   fk_transfert_origin int8,
   fk_transfert_destination int8,
   fk_trx_reference int8,
   primary key (id)
);

alter table o_cp_transaction add constraint cp_trx_to_wallet_idx foreign key (fk_wallet) references o_cp_wallet (id);
create index idx_cp_trx_to_wallet_idx on o_cp_transaction (fk_wallet);
alter table o_cp_transaction add constraint cp_trx_to_creator_idx foreign key (fk_creator) references o_bs_identity (id);
create index idx_cp_trx_to_creator_idx on o_cp_transaction (fk_creator);

alter table o_cp_transaction add constraint cp_transfert_to_origin_idx foreign key (fk_transfert_origin) references o_olatresource (resource_id);
create index idx_cp_transfert_to_origin_idx on o_cp_transaction (fk_transfert_origin);
alter table o_cp_transaction add constraint cp_transfert_to_dest_idx foreign key (fk_transfert_destination) references o_olatresource (resource_id);
create index idx_cp_transfert_to_dest_idx on o_cp_transaction (fk_transfert_destination);

alter table o_cp_transaction add constraint cp_trx_to_reference_idx foreign key (fk_trx_reference) references o_cp_transaction (id);
create index idx_cp_trx_to_reference_idx on o_cp_transaction (fk_trx_reference);

create table o_cp_transaction_details (
   id bigserial,
   creationdate timestamp,
   c_amount decimal not null,
   fk_source int8 not null,
   fk_target int8 not null,
   primary key (id)
);

alter table o_cp_transaction_details add constraint cp_details_to_source_idx foreign key (fk_source) references o_cp_transaction (id);
create index idx_cp_details_to_source_idx on o_cp_transaction_details (fk_source);
alter table o_cp_transaction_details add constraint cp_details_to_target_idx foreign key (fk_target) references o_cp_transaction (id);
create index idx_cp_details_to_target_idx on o_cp_transaction_details (fk_target);

create table o_cp_repositoryentry_config (
   id bigserial,
   creationdate timestamp,
   lastmodified timestamp,
   c_enabled bool default false,
   c_creditpoints decimal,
   c_expiration int8,
   c_expiration_unit varchar(16),
   fk_entry int8 not null,
   fk_system int8,
   primary key (id)
);

alter table o_cp_repositoryentry_config add constraint unique_creditpoint_re_config unique (fk_entry);

alter table o_cp_repositoryentry_config add constraint re_config_to_system_idx foreign key (fk_system) references o_cp_system (id);
create index idx_re_config_to_system_idx on o_cp_repositoryentry_config (fk_system);

create table o_cp_cur_element_config (
   id bigserial,
   creationdate timestamp,
   lastmodified timestamp,
   c_enabled bool default false,
   c_creditpoints decimal,
   fk_element int8 not null,
   fk_system int8,
   primary key (id)
);

alter table o_cp_cur_element_config add constraint unique_cp_cur_el_config unique (fk_element);

alter table o_cp_cur_element_config add constraint cur_el_conf_to_system_idx foreign key (fk_element) references o_cur_curriculum_element (id);
create index idx_cur_el_conf_to_system_idx on o_cp_cur_element_config (fk_element);


alter table o_cur_curriculum_element add column c_show_certificate bool default false;
alter table o_cur_curriculum_element add column c_show_creditpoints bool default false;

