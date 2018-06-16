-- organisation
create table o_org_organisation_type (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  o_identifier varchar(64),
  o_displayname varchar(255) not null,
  o_description text,
  o_external_id varchar(64),
  o_managed_flags varchar(255),
  o_css_class varchar(64),
  primary key (id)
);

create table o_org_organisation (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  o_identifier varchar(64),
  o_displayname varchar(255) not null,
  o_description text,
  o_m_path_keys varchar(255),
  o_external_id varchar(64),
  o_managed_flags varchar(255),
  o_status varchar(32),
  o_css_class varchar(64),
  fk_group int8 not null,
  fk_root int8,
  fk_parent int8,
  fk_type int8,
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
  id bigserial,
  fk_type int8 not null,
  fk_allowed_sub_type int8 not null,
  primary key (id)
);

alter table o_org_type_to_type add constraint org_type_to_type_idx foreign key (fk_type) references o_org_organisation_type (id);
create index idx_org_type_to_type_idx on o_org_type_to_type (fk_type);
alter table o_org_type_to_type add constraint org_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_org_organisation_type (id);
create index idx_org_type_to_sub_type_idx on o_org_type_to_type (fk_allowed_sub_type);


create table o_re_to_organisation (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  r_master bool default false,
  fk_entry int8 not null,
  fk_organisation int8 not null,
  primary key (id)
);

alter table o_re_to_organisation add constraint rel_org_to_re_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_rel_org_to_re_idx on o_re_to_organisation (fk_entry);
alter table o_re_to_organisation add constraint rel_org_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_rel_org_to_org_idx on o_re_to_organisation (fk_organisation);


-- curriculum
create table o_cur_element_type (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description text,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  c_css_class varchar(64),
  primary key (id)
);

create table o_cur_curriculum (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description text,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  c_status varchar(32),
  c_degree varchar(255),
  fk_group int8 not null,
  fk_organisation int8,
  primary key (id)
);

alter table o_cur_curriculum add constraint cur_to_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_cur_to_group_idx on o_cur_curriculum (fk_group);
alter table o_cur_curriculum add constraint cur_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_cur_to_org_idx on o_cur_curriculum (fk_organisation);

create table o_cur_curriculum_element (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  pos int8,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description text,
  c_status varchar(32),
  c_begin timestamp,
  c_end timestamp ,
  c_external_id varchar(64),
  c_m_path_keys varchar(255),
  c_managed_flags varchar(255),
  fk_group int8 not null,
  fk_parent int8,
  fk_curriculum int8 not null,
  fk_type int8,
  primary key (id)
);

alter table o_cur_curriculum_element add constraint cur_el_to_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_cur_el_to_group_idx on o_cur_curriculum_element (fk_group);
alter table o_cur_curriculum_element add constraint cur_el_to_cur_el_idx foreign key (fk_parent) references o_cur_curriculum_element (id);
create index idx_cur_el_to_cur_el_idx on o_cur_curriculum_element (fk_parent);
alter table o_cur_curriculum_element add constraint cur_el_to_cur_idx foreign key (fk_curriculum) references o_cur_curriculum (id);
create index idx_cur_el_to_cur_idx on o_cur_curriculum_element (fk_curriculum);
alter table o_cur_curriculum_element add constraint cur_el_type_to_el_type_idx foreign key (fk_type) references o_cur_element_type (id);
create index idx_cur_el_type_to_el_type_idx on o_cur_curriculum_element (fk_type);


create table o_cur_element_type_to_type (
  id bigserial,
  fk_type int8 not null,
  fk_allowed_sub_type int8 not null,
  primary key (id)
);

alter table o_cur_element_type_to_type add constraint cur_type_to_type_idx foreign key (fk_type) references o_cur_element_type (id);
create index idx_cur_type_to_type_idx on o_cur_element_type_to_type (fk_type);
alter table o_cur_element_type_to_type add constraint cur_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_cur_element_type (id);
create index idx_cur_type_to_sub_type_idx on o_cur_element_type_to_type (fk_allowed_sub_type);


-- drop policy
alter table o_bs_policy drop constraint FK9A1C5101E2E76DB;


-- evaluation forms
create table o_eva_form_survey (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   e_resname varchar(50) not null,
   e_resid int8 not null,
   e_sub_ident varchar(2048),
   fk_form_entry bigint not null,
   primary key (id)
);

create table o_eva_form_participation (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   e_identifier_type varchar(50) not null,
   e_identifier_key varchar(50) not null,
   e_status varchar(20) not null,
   e_anonymous bool not null,
   fk_executor bigint,
   fk_survey bigint not null,
   primary key (id)
);

alter table o_eva_form_response add column e_no_response bool default false;

alter table o_eva_form_session alter column fk_form_entry drop not null;
alter table o_eva_form_session alter column fk_identity drop not null;
alter table o_eva_form_session add column e_email varchar(1024);
alter table o_eva_form_session add column e_firstname varchar(1024);
alter table o_eva_form_session add column e_lastname varchar(1024);
alter table o_eva_form_session add column e_age varchar(1024);
alter table o_eva_form_session add column e_gender varchar(1024);
alter table o_eva_form_session add column e_org_unit varchar(1024);
alter table o_eva_form_session add column e_study_subject varchar(1024);
alter table o_eva_form_session add column fk_survey bigint;
alter table o_eva_form_session add column fk_participation bigint unique;

create unique index idx_eva_surv_ores_idx on o_eva_form_survey (e_resid, e_resname, e_sub_ident);

alter table o_eva_form_participation add constraint eva_part_to_surv_idx foreign key (fk_survey) references o_eva_form_survey (id);
create unique index idx_eva_part_ident_idx on o_eva_form_participation (e_identifier_key, e_identifier_type, fk_survey);
create unique index idx_eva_part_executor_idx on o_eva_form_participation (fk_executor, fk_survey) where fk_executor is not null;

alter table o_eva_form_session add constraint eva_sess_to_surv_idx foreign key (fk_survey) references o_eva_form_survey (id);
create index idx_eva_sess_to_surv_idx on o_eva_form_session (fk_survey);
alter table o_eva_form_session add constraint eva_sess_to_part_idx foreign key (fk_participation) references o_eva_form_participation (id);
create unique index idx_eva_sess_to_part_idx on o_eva_form_session (fk_participation);

create index idx_eva_resp_report_idx on o_eva_form_response (fk_session, e_responseidentifier, e_no_response);

-- quality management
create table o_qual_data_collection (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_status varchar(50),
   q_title varchar(200),
   q_start timestamp,
   q_deadline timestamp,
   q_topic_type varchar(50),
   q_topic_custom varchar(200),
   q_topic_fk_identity int8,
   q_topic_fk_organisation int8,
   q_topic_fk_curriculum int8,
   q_topic_fk_curriculum_element int8,
   q_topic_fk_repository int8,
   primary key (id)
);

-- membership
alter table o_bs_group_member add column g_inheritance_mode varchar(16) default 'none' not null;


-- lectures
alter table o_lecture_block_roll_call add column l_appeal_reason text;
alter table o_lecture_block_roll_call add column l_appeal_status text;
alter table o_lecture_block_roll_call add column l_appeal_status_reason text;





