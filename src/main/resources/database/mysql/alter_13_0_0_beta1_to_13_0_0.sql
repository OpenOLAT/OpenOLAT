
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

-- Temporary keys
alter table o_temporarykey add valid_until datetime;

