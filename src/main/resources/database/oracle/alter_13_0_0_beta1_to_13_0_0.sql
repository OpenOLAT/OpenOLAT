
-- Quality management
create table o_qual_data_collection_to_org (
   id number(20) generated always as identity,
   creationdate date not null,
   fk_data_collection number(20) not null,
   fk_organisation number(20) not null,
   primary key (id)
);

alter table o_qual_data_collection_to_org add constraint qual_dc_to_org_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
create unique index idx_qual_dc_to_org_idx on o_qual_data_collection_to_org (fk_data_collection, fk_organisation);

create table o_qual_generator (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   q_title varchar2(256),
   q_type varchar2(64) not null,
   q_enabled number not null,
   q_last_run date,
   fk_form_entry number(20),
   primary key (id)
);

create table o_qual_generator_config (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   q_identifier varchar2(50) not null,
   q_value varchar2(2048),
   fk_generator number(20) not null,
   primary key (id)
);

create table o_qual_generator_to_org (
   id number(20) generated always as identity,
   creationdate date not null,
   fk_generator number(20) not null,
   fk_organisation number(20) not null,
   primary key (id)
);

alter table o_qual_data_collection add fk_generator number(20);
alter table o_qual_data_collection add q_generator_provider_key number(20);

alter table o_qual_data_collection add constraint qual_dc_to_gen_idx foreign key (fk_generator) references o_qual_generator (id);

alter table o_qual_generator_to_org add constraint qual_gen_to_org_idx foreign key (fk_generator) references o_qual_generator (id);
create unique index idx_qual_gen_to_org_idx on o_qual_generator_to_org (fk_generator, fk_organisation);


-- Temporary keys
alter table o_temporarykey add valid_until date;

