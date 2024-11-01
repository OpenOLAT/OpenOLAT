-- Lecture
alter table o_lecture_block add l_external_ref varchar(128);

alter table o_lecture_block modify (fk_entry null);
alter table o_lecture_block add fk_curriculum_element number(20);

alter table o_lecture_block add constraint lec_block_curelem_idx foreign key (fk_curriculum_element) references o_cur_curriculum_element(id);
create index idx_lec_block_curelem_idx on o_lecture_block(fk_curriculum_element);

alter table o_lecture_block_audit_log add fk_curriculum_element number(20);

-- Curriculum
alter table o_cur_element_type add c_single_element number default 0 not null;
alter table o_cur_element_type add c_max_repo_entries number(20) default -1 not null;
alter table o_cur_element_type add c_allow_as_root number default 1 not null;

-- Organisations
alter table o_org_organisation add o_location varchar(255);
create table o_org_email_domain (
  id number(20) GENERATED ALWAYS AS IDENTITY,
  creationdate date not null,
  lastmodified date not null,
  o_domain varchar(255) not null,
  o_enabled number default 1 not null,
  o_subdomains_allowed number default 0 not null,
  fk_organisation number(20) not null,
  primary key (id)
);

alter table o_org_email_domain add constraint org_email_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_org_email_to_org_idx on o_org_email_domain (fk_organisation);

-- Catalog
alter table o_ca_launcher add column c_web_enabled number default 1 not null;

-- Access control
create table o_ac_billing_address (
  id number(20) GENERATED ALWAYS AS IDENTITY,
  creationdate date not null,
  lastmodified date not null,
  a_identifier varchar(255),
  a_name_line_1 varchar(255),
  a_name_line_2 varchar(255),
  a_address_line_1 varchar(255),
  a_address_line_2 varchar(255),
  a_address_line_3 varchar(255),
  a_address_line_4 varchar(255),
  a_pobox varchar(255),
  a_region varchar(255),
  a_zip varchar(255),
  a_city varchar(255),
  a_country varchar(255),
  a_enabled number default 1 not null,
  fk_organisation number(20),
  fk_identity number(20),
  primary key (id)
);
alter table o_ac_order add column fk_billing_address number(20);

alter table o_ac_billing_address add constraint ac_billing_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_ac_billing_to_org_idx on o_ac_billing_address (fk_organisation);
alter table o_ac_billing_address add constraint ac_billing_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_ac_billing_to_ident_idx on o_ac_billing_address (fk_identity);

alter table o_ac_order add constraint ord_billing_idx foreign key (fk_billing_address) references o_ac_billing_address (id);
create index idx_ord_billing_idx on o_ac_order (fk_billing_address);
