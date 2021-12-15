-- Business group 
alter table o_gp_business add status varchar(32) default 'active';

alter table o_gp_business add inactivationdate date;
alter table o_gp_business add inactivationemaildate date;
alter table o_gp_business add reactivationdate date;

alter table o_gp_business add softdeleteemaildate date;
alter table o_gp_business add softdeletedate date;

alter table o_gp_business add data_for_restore CLOB;

alter table o_gp_business add fk_inactivatedby_id number(20);
alter table o_gp_business add fk_softdeletedby_id number(20);

alter table o_gp_business add constraint gb_bus_inactivateby_idx foreign key (fk_inactivatedby_id) references o_bs_identity (id);
create index idx_gb_bus_inactivateby_idx on o_gp_business (fk_inactivatedby_id);

alter table o_gp_business add constraint gb_bus_softdeletedby_idx foreign key (fk_softdeletedby_id) references o_bs_identity (id);
create index idx_gb_bus_softdeletedby_idx on o_gp_business (fk_softdeletedby_id);


-- Course
create table o_course_element (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   c_type varchar(32) not null,
   c_short_title varchar(32) not null,
   c_long_title varchar(1024) not null,
   c_assesseable number(20) not null,
   c_score_mode varchar(16) not null,
   c_passed_mode varchar(16) not null,
   c_cut_value decimal,
   fk_entry number(20) not null,
   c_subident varchar(64) not null,
   primary key (id)
);

alter table o_course_element add constraint courseele_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_courseele_entry_idx on o_course_element (fk_entry);
create unique index idx_courseele_subident_idx on o_course_element (c_subident, fk_entry);


-- Assessment
alter table o_as_entry add a_obligation_inherited varchar2(50);
alter table o_as_entry add a_obligation_evaluated varchar2(50);
alter table o_as_entry add a_obligation_config varchar2(50);
alter table o_as_entry add a_max_score decimal;
alter table o_as_entry add fk_identity_status_done number(20);

create table o_as_score_accounting_trigger (
   id number(20) generated always as identity,
   creationdate date not null,
   e_identifier varchar2(64) not null,
   e_business_group_key number(20),
   e_organisation_key number(20),
   e_curriculum_element_key number(20),
   e_user_property_name varchar2(64),
   e_user_property_value varchar2(128),
   fk_entry number(20) not null,
   e_subident varchar2(64) not null,
   primary key (id)
);

alter table o_as_score_accounting_trigger add constraint satrigger_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_satrigger_re_idx on o_as_score_accounting_trigger (fk_entry);
create index idx_satrigger_bs_group_idx on o_as_score_accounting_trigger (e_business_group_key);
create index idx_satrigger_org_idx on o_as_score_accounting_trigger (e_organisation_key);
create index idx_satrigger_curle_idx on o_as_score_accounting_trigger (e_curriculum_element_key);
create index idx_satrigger_userprop_idx on o_as_score_accounting_trigger (e_user_property_value, e_user_property_name);


-- Taxonomy Types --
alter table o_tax_taxonomy_level_type add t_allow_as_subject number(1) default 0;
