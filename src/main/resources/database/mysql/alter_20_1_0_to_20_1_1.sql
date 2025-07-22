-- Credit point module
create table o_cp_system (
   id bigint not null auto_increment,
   creationdate datetime,
   lastmodified datetime,
   c_name varchar(255) not null,
   c_label varchar(16) not null,
   c_description varchar(4000),
   c_def_expiration bigint,
   c_def_expiration_unit varchar(16),
   c_status varchar(16) not null,
   primary key (id)
);
alter table o_cp_system ENGINE = InnoDB;

create table o_cp_wallet (
   id bigint not null auto_increment,
   creationdate datetime,
   lastmodified datetime,
   c_balance decimal(12,4) default null,
   c_balance_recalc datetime,
   fk_identity bigint not null,
   fk_system bigint not null,
   primary key (id)
);
alter table o_cp_wallet ENGINE = InnoDB;

alter table o_cp_wallet add constraint unique_wallet_sys_ident unique (fk_identity, fk_system);

alter table o_cp_wallet add constraint cp_wallet_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_cp_wallet add constraint cp_wallet_to_system_idx foreign key (fk_system) references o_cp_system (id);

create table o_cp_transaction (
   id bigint not null auto_increment,
   creationdate datetime,
   c_type varchar(16) not null,
   c_amount decimal(12,4) not null,
   c_remaining_amount decimal(12,4),
   c_expiration_date datetime,
   c_note varchar(4000),
   c_origin_run bigint,
   c_destination_run bigint,
   fk_wallet bigint not null,
   fk_creator bigint,
   fk_transfert_origin bigint,
   fk_transfert_destination bigint,
   fk_trx_reference bigint,
   primary key (id)
);
alter table o_cp_transaction ENGINE = InnoDB;

alter table o_cp_transaction add constraint cp_trx_to_wallet_idx foreign key (fk_wallet) references o_cp_wallet (id);
alter table o_cp_transaction add constraint cp_trx_to_creator_idx foreign key (fk_creator) references o_bs_identity (id);

alter table o_cp_transaction add constraint cp_transfert_to_origin_idx foreign key (fk_transfert_origin) references o_olatresource (resource_id);
alter table o_cp_transaction add constraint cp_transfert_to_dest_idx foreign key (fk_transfert_destination) references o_olatresource (resource_id);

alter table o_cp_transaction add constraint cp_trx_to_reference_idx foreign key (fk_trx_reference) references o_cp_transaction (id);

create table o_cp_transaction_details (
   id bigint not null auto_increment,
   creationdate datetime,
   c_amount decimal(12,4) not null,
   fk_source bigint not null,
   fk_target bigint not null,
   primary key (id)
);
alter table o_cp_transaction_details ENGINE = InnoDB;

alter table o_cp_transaction_details add constraint cp_details_to_source_idx foreign key (fk_source) references o_cp_transaction (id);
alter table o_cp_transaction_details add constraint cp_details_to_target_idx foreign key (fk_target) references o_cp_transaction (id);

create table o_cp_repositoryentry_config (
   id bigint not null auto_increment,
   creationdate datetime,
   lastmodified datetime,
   c_enabled boolean default false,
   c_creditpoints decimal(12,4),
   c_expiration bigint,
   c_expiration_unit varchar(16),
   fk_entry bigint not null,
   fk_system bigint,
   primary key (id)
);
alter table o_cp_repositoryentry_config ENGINE = InnoDB;

alter table o_cp_repositoryentry_config add constraint unique_creditpoint_re_config unique (fk_entry);

alter table o_cp_repositoryentry_config add constraint re_config_to_system_idx foreign key (fk_system) references o_cp_system (id);

create table o_cp_cur_element_config (
   id bigint not null auto_increment,
   creationdate datetime,
   lastmodified datetime,
   c_enabled boolean default false,
   c_creditpoints decimal(12,4),
   fk_element bigint not null,
   fk_system bigint,
   primary key (id)
);
alter table o_cp_cur_element_config ENGINE = InnoDB;

alter table o_cp_cur_element_config add constraint unique_cp_cur_el_config unique (fk_element);

alter table o_cp_cur_element_config add constraint cur_el_conf_to_system_idx foreign key (fk_element) references o_cur_curriculum_element (id);

alter table o_cur_curriculum_element add column c_show_certificate boolean default false;
alter table o_cur_curriculum_element add column c_show_creditpoints boolean default false;

