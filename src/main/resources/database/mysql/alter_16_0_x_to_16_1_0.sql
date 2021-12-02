-- Business group 
alter table o_gp_business add column status varchar(32) default 'active';

alter table o_gp_business add column inactivationdate datetime;
alter table o_gp_business add column inactivationemaildate datetime;
alter table o_gp_business add column reactivationdate datetime;

alter table o_gp_business add column softdeleteemaildate datetime;
alter table o_gp_business add column softdeletedate datetime;

alter table o_gp_business add column data_for_restore mediumtext;

alter table o_gp_business add column fk_inactivatedby_id bigint;
alter table o_gp_business add column fk_softdeletedby_id bigint;

alter table o_gp_business add constraint gb_bus_inactivateby_idx foreign key (fk_inactivatedby_id) references o_bs_identity (id);

alter table o_gp_business add constraint gb_bus_softdeletedby_idx foreign key (fk_softdeletedby_id) references o_bs_identity (id);


-- Assessment
alter table o_as_entry add a_obligation_inherited varchar(50);
alter table o_as_entry add a_obligation_evaluated varchar(50);
alter table o_as_entry add a_obligation_config varchar(50);
alter table o_as_entry add a_max_score float(65,30);

create table o_as_score_accounting_trigger (
   id bigint not null auto_increment,
   creationdate datetime not null,
   e_identifier varchar(64) not null,
   e_business_group_key bigint,
   e_organisation_key bigint,
   e_curriculum_element_key bigint,
   e_user_property_name varchar(64),
   e_user_property_value varchar(128),
   fk_entry bigint not null not null,
   e_subident varchar(64) not null,
   primary key (id)
);
alter table o_as_score_accounting_trigger ENGINE = InnoDB;

alter table o_as_score_accounting_trigger add constraint satrigger_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_satrigger_bs_group_idx on o_as_score_accounting_trigger (e_business_group_key);
create index idx_satrigger_org_idx on o_as_score_accounting_trigger (e_organisation_key);
create index idx_satrigger_curele_idx on o_as_score_accounting_trigger (e_curriculum_element_key);
create index idx_satrigger_userprop_idx on o_as_score_accounting_trigger (e_user_property_value, e_user_property_name);


-- Taxonomy Types --
alter table o_tax_taxonomy_level_type add column t_allow_as_subject boolean default false;
