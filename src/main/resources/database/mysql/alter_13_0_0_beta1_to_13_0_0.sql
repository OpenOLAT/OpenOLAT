
-- Quality management
create table o_qual_data_collection_to_org (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_data_collection bigint not null,
   fk_organisation bigint not null,
   primary key (id)
);

alter table o_qual_data_collection_to_org ENGINE = InnoDB;

alter table o_qual_data_collection_to_org add constraint qual_dc_to_org_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
create unique index idx_qual_dc_to_org_idx on o_qual_data_collection_to_org (fk_data_collection, fk_organisation);

create table o_qual_generator (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_title varchar(256),
   q_type varchar(64) not null,
   q_enabled bit not null,
   q_last_run datetime,
   fk_form_entry bigint,
   primary key (id)
);

create table o_qual_generator_config (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_identifier varchar(50) not null,
   q_value mediumtext,
   fk_generator bigint not null,
   primary key (id)
);

create table o_qual_generator_to_org (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_generator bigint not null,
   fk_organisation bigint not null,
   primary key (id)
);

alter table o_qual_generator ENGINE = InnoDB;
alter table o_qual_generator_config ENGINE = InnoDB;
alter table o_qual_generator_to_org ENGINE = InnoDB;

alter table o_qual_data_collection add fk_generator bigint;
alter table o_qual_data_collection add q_generator_provider_key bigint;

alter table o_qual_data_collection add constraint qual_dc_to_gen_idx foreign key (fk_generator) references o_qual_generator (id);

alter table o_qual_generator_to_org add constraint qual_gen_to_org_idx foreign key (fk_generator) references o_qual_generator (id);
create unique index idx_qual_gen_to_org_idx on o_qual_generator_to_org (fk_generator, fk_organisation);

-- Temporary keys
alter table o_temporarykey add valid_until datetime;

