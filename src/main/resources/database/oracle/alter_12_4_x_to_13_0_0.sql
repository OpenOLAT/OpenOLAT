-- organisation
create table o_org_organisation_type (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  o_identifier varchar(64),
  o_displayname varchar(255) not null,
  o_description CLOB,
  o_external_id varchar(64),
  o_managed_flags varchar(255),
  o_css_class varchar(64),
  primary key (id)
);

create table o_org_organisation (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  o_identifier varchar(64),
  o_displayname varchar(255) not null,
  o_description CLOB,
  o_m_path_keys varchar(255),
  o_external_id varchar(64),
  o_managed_flags varchar(255),
  o_status varchar(32),
  o_css_class varchar(64),
  fk_group number(20) not null,
  fk_root number(20),
  fk_parent number(20),
  fk_type number(20),
  primary key (id)
);

alter table o_org_organisation add constraint org_to_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_org_to_group_idx on o_org_organisation (fk_group);
alter table o_org_organisation add constraint org_to_root_org_idx foreign key (fk_root) references o_org_organisation (id);
create index idx_org_to_root_org_idx on o_org_organisation (fk_root);
alter table o_org_organisation add constraint org_to_parent_org_idx foreign key (fk_parent) references o_org_organisation (id);
create index idx_org_to_parent_org_idx on o_org_organisation (fk_parent);
alter table o_org_organisation add constraint org_to_org_type_idx foreign key (fk_type) references o_org_organisation_type (id);
create index idx_org_to_org_type_idx on o_org_organisation (fk_type);

create table o_org_type_to_type (
  id number(20) generated always as identity,
  fk_type number(20) not null,
  fk_allowed_sub_type number(20) not null,
  primary key (id)
);

alter table o_org_type_to_type add constraint org_type_to_type_idx foreign key (fk_type) references o_org_organisation_type (id);
create index idx_org_type_to_type_idx on o_org_type_to_type (fk_type);
alter table o_org_type_to_type add constraint org_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_org_organisation_type (id);
create index idx_org_type_to_sub_type_idx on o_org_type_to_type (fk_allowed_sub_type);


create table o_re_to_organisation (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  r_master number default 0,
  fk_entry number(20) not null,
  fk_organisation number(20) not null,
  primary key (id)
);

alter table o_re_to_organisation add constraint rel_org_to_re_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_rel_org_to_re_idx on o_re_to_organisation (fk_entry);
alter table o_re_to_organisation add constraint rel_org_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_rel_org_to_org_idx on o_re_to_organisation (fk_organisation);


-- curriculum
create table o_cur_element_type (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description CLOB,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  c_css_class varchar(64),
  primary key (id)
);

create table o_cur_curriculum (
   id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description CLOB,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  c_status varchar(32),
  c_degree varchar(255),
  fk_group number(20) not null,
  fk_organisation number(20),
  primary key (id)
);

alter table o_cur_curriculum add constraint cur_to_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_cur_to_group_idx on o_cur_curriculum (fk_group);
alter table o_cur_curriculum add constraint cur_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_cur_to_org_idx on o_cur_curriculum (fk_organisation);

create table o_cur_curriculum_element (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  pos number(20),
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description CLOB,
  c_status varchar(32),
  c_begin date,
  c_end date,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  fk_group number(20) not null,
  fk_parent number(20),
  fk_curriculum number(20) not null,
  primary key (id)
);

alter table o_cur_curriculum_element add constraint cur_el_to_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_cur_el_to_group_idx on o_cur_curriculum_element (fk_group);
alter table o_cur_curriculum_element add constraint cur_el_to_cur_el_idx foreign key (fk_parent) references o_cur_curriculum_element (id);
create index idx_cur_el_to_cur_el_idx on o_cur_curriculum_element (fk_parent);
alter table o_cur_curriculum_element add constraint cur_el_to_cur_idx foreign key (fk_curriculum) references o_cur_curriculum (id);
create index idx_cur_el_to_cur_idx on o_cur_curriculum_element (fk_curriculum);

create table o_cur_element_type_to_type (
  id number(20) generated always as identity,
  fk_type number(20) not null,
  fk_allowed_sub_type number(20) not null,
  primary key (id)
);

alter table o_cur_element_type_to_type add constraint cur_type_to_type_idx foreign key (fk_type) references o_cur_element_type (id);
create index idx_cur_type_to_type_idx on o_cur_element_type_to_type (fk_type);
alter table o_cur_element_type_to_type add constraint cur_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_cur_element_type (id);
create index idx_cur_type_to_sub_type_idx on o_cur_element_type_to_type (fk_allowed_sub_type);

create table o_re_to_curriculum_element (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  c_master number default 0,
  fk_entry number(20) not null,
  fk_curriculum_element number(20) not null,
  primary key (id)
);

alter table o_re_to_curriculum_element add constraint rel_cur_el_to_re_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_rel_cur_el_to_re_idx on o_re_to_curriculum_element (fk_entry);
alter table o_re_to_curriculum_element add constraint rel_cur_el_to_cur_el_idx foreign key (fk_curriculum_element) references o_cur_curriculum_element (id);
create index idx_rel_cur_el_to_cur_el_idx on o_re_to_curriculum_element (fk_curriculum_element);


-- evaluation forms
create table o_eva_form_survey (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   e_resname varchar2(50) not null,
   e_resid number(20) not null,
   e_sub_ident varchar2(2048),
   fk_form_entry number(20) not null,
   primary key (id)
);

create table o_eva_form_participation (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   e_identifier_type varchar2(50) not null,
   e_identifier_key varchar2(50) not null,
   e_status varchar2(20) not null,
   e_anonymous number default 0 not null,
   fk_executor number(20),
   fk_survey number(20) not null,
   primary key (id)
);

alter table o_eva_form_response add e_no_response number default 0;

alter table o_eva_form_session modify fk_form_entry null;
alter table o_eva_form_session modify fk_identity null;
alter table o_eva_form_session add fk_survey number(20);
alter table o_eva_form_session add fk_participation number(20) unique;

create unique index idx_eva_surv_ores_idx on o_eva_form_survey (e_resid, e_resname, e_sub_ident);

alter table o_eva_form_participation add constraint eva_part_to_surv_idx foreign key (fk_survey) references o_eva_form_survey (id);
create unique index idx_eva_part_ident_idx on o_eva_form_participation (e_identifier_key, e_identifier_type, fk_survey);
create unique index idx_eva_part_executor_idx on o_eva_form_participation (fk_executor, fk_survey);

alter table o_eva_form_session add constraint eva_sess_to_surv_idx foreign key (fk_survey) references o_eva_form_survey (id);
create index idx_eva_sess_to_surv_idx on o_eva_form_session (fk_survey);
alter table o_eva_form_session add constraint eva_sess_to_part_idx foreign key (fk_participation) references o_eva_form_participation (id);


-- membership
alter table o_bs_group_member add g_inheritance_mode varchar(16) default 'none' not null;
