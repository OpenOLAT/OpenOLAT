-- Lectures
alter table o_lecture_block add column l_external_ref varchar(128);

alter table o_lecture_block alter column fk_entry drop not null;
alter table o_lecture_block add column fk_curriculum_element int8;

alter table o_lecture_block add constraint lec_block_curelem_idx foreign key (fk_curriculum_element) references o_cur_curriculum_element(id);
create index idx_lec_block_curelem_idx on o_lecture_block(fk_curriculum_element);

alter table o_lecture_block_audit_log add column fk_curriculum_element int8;

-- Curriculum
alter table o_cur_element_type add column c_single_element bool default false not null;
alter table o_cur_element_type add column c_max_repo_entries int8 default -1 not null;
alter table o_cur_element_type add column c_allow_as_root bool default true not null;

-- Organisations
create table o_org_email_domain (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  o_domain varchar(255) not null,
  o_enabled bool default true not null,
  o_subdomains_allowed bool default false not null,
  fk_organisation int8 not null,
  primary key (id)
);

alter table o_org_email_domain add constraint org_email_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_org_email_to_org_idx on o_org_email_domain (fk_organisation);

-- Catalog
alter table o_ca_launcher add column c_web_enabled bool default true not null;
