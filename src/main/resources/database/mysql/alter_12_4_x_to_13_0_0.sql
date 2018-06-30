-- organisation
create table o_org_organisation_type (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  o_identifier varchar(64),
  o_displayname varchar(255) not null,
  o_description mediumtext,
  o_external_id varchar(64),
  o_managed_flags varchar(255),
  o_css_class varchar(64),
  primary key (id)
);

alter table o_org_organisation_type ENGINE = InnoDB;

create table o_org_organisation (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  o_identifier varchar(64),
  o_displayname varchar(255) not null,
  o_description mediumtext,
  o_m_path_keys varchar(255),
  o_external_id varchar(64),
  o_managed_flags varchar(255),
  o_status varchar(32),
  o_css_class varchar(64),
  fk_group bigint not null,
  fk_root bigint,
  fk_parent bigint,
  fk_type bigint,
  primary key (id)
);

alter table o_org_organisation ENGINE = InnoDB;

alter table o_org_organisation add constraint org_to_group_idx foreign key (fk_group) references o_bs_group (id);
alter table o_org_organisation add constraint org_to_root_org_idx foreign key (fk_root) references o_org_organisation (id);
alter table o_org_organisation add constraint org_to_parent_org_idx foreign key (fk_parent) references o_org_organisation (id);
alter table o_org_organisation add constraint org_to_org_type_idx foreign key (fk_type) references o_org_organisation_type (id);

create table o_org_type_to_type (
  id bigint not null auto_increment,
  fk_type bigint not null,
  fk_allowed_sub_type bigint not null,
  primary key (id)
);
alter table o_org_type_to_type ENGINE = InnoDB;

alter table o_org_type_to_type add constraint org_type_to_type_idx foreign key (fk_type) references o_org_organisation_type (id);
alter table o_org_type_to_type add constraint org_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_org_organisation_type (id);


create table o_re_to_organisation (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  r_master bit default 0,
  fk_entry bigint not null,
  fk_organisation bigint not null,
  primary key (id)
);
alter table o_re_to_organisation ENGINE = InnoDB;

alter table o_re_to_organisation add constraint rel_org_to_re_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_re_to_organisation add constraint rel_org_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);


-- curriculum
create table o_cur_element_type (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description mediumtext,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  c_css_class varchar(64),
  primary key (id)
);
alter table o_cur_element_type ENGINE = InnoDB;

create table o_cur_curriculum (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description mediumtext,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  c_status varchar(32),
  c_degree varchar(255),
  fk_group bigint not null,
  fk_organisation bigint,
  primary key (id)
);
alter table o_cur_curriculum ENGINE = InnoDB;

alter table o_cur_curriculum add constraint cur_to_group_idx foreign key (fk_group) references o_bs_group (id);
alter table o_cur_curriculum add constraint cur_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);

create table o_cur_curriculum_element (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  pos bigint,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description mediumtext,
  c_status varchar(32),
  c_begin datetime,
  c_end datetime,
  c_external_id varchar(64),
  c_m_path_keys varchar(255),
  c_managed_flags varchar(255),
  fk_group bigint not null,
  fk_parent bigint,
  fk_curriculum bigint not null,
  fk_type bigint,
  primary key (id)
);
alter table o_cur_curriculum_element ENGINE = InnoDB;

alter table o_cur_curriculum_element add constraint cur_el_to_group_idx foreign key (fk_group) references o_bs_group (id);
alter table o_cur_curriculum_element add constraint cur_el_to_cur_el_idx foreign key (fk_parent) references o_cur_curriculum_element (id);
alter table o_cur_curriculum_element add constraint cur_el_to_cur_idx foreign key (fk_curriculum) references o_cur_curriculum (id);
alter table o_cur_curriculum_element add constraint cur_el_type_to_el_type_idx foreign key (fk_type) references o_cur_element_type (id);

create table o_cur_element_type_to_type (
  id bigint not null auto_increment,
  fk_type bigint not null,
  fk_allowed_sub_type bigint not null,
  primary key (id)
);
alter table o_cur_element_type_to_type ENGINE = InnoDB;

alter table o_cur_element_type_to_type add constraint cur_type_to_type_idx foreign key (fk_type) references o_cur_element_type (id);
alter table o_cur_element_type_to_type add constraint cur_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_cur_element_type (id);

create table o_cur_element_to_tax_level (
  id bigint not null auto_increment,
  creationdate datetime not null,
  fk_cur_element bigint not null,
  fk_taxonomy_level bigint not null,
  primary key (id)
);
alter table o_cur_element_to_tax_level ENGINE = InnoDB;

alter table o_cur_element_to_tax_level add constraint cur_el_rel_to_cur_el_idx foreign key (fk_cur_element) references o_cur_curriculum_element (id);
alter table o_cur_element_to_tax_level add constraint cur_el_to_tax_level_idx foreign key (fk_taxonomy_level) references o_tax_taxonomy_level (id);


-- lectures
create table o_lecture_block_to_tax_level (
  id bigint not null auto_increment,
  creationdate datetime not null,
  fk_lecture_block bigint not null,
  fk_taxonomy_level bigint not null,
  primary key (id)
);
alter table o_lecture_block_to_tax_level ENGINE = InnoDB;

alter table o_lecture_block_to_tax_level add constraint lblock_rel_to_lblock_idx foreign key (fk_lecture_block) references o_lecture_block (id);
alter table o_lecture_block_to_tax_level add constraint lblock_rel_to_tax_lev_idx foreign key (fk_taxonomy_level) references o_tax_taxonomy_level (id);


-- repository
create table o_re_to_tax_level (
  id bigint not null auto_increment,
  creationdate datetime not null,
  fk_entry bigint not null,
  fk_taxonomy_level bigint not null,
  primary key (id)
);
alter table o_re_to_tax_level ENGINE = InnoDB;

alter table o_re_to_tax_level add constraint re_to_lev_re_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_re_to_tax_level add constraint re_to_lev_tax_lev_idx foreign key (fk_taxonomy_level) references o_tax_taxonomy_level (id);


-- drop policy
alter table o_bs_policy drop foreign key FK9A1C5101E2E76DB;


-- evaluation forms
create table o_eva_form_survey (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   e_resname varchar(50) not null,
   e_resid bigint not null,
   e_sub_ident varchar(2048),
   fk_form_entry bigint not null,
   primary key (id)
);

create table o_eva_form_participation (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   e_identifier_type varchar(50) not null,
   e_identifier_key varchar(50) not null,
   e_status varchar(20) not null,
   e_anonymous bit not null,
   fk_executor bigint,
   fk_survey bigint not null,
   primary key (id)
);

alter table o_eva_form_response add column e_no_response bit default 0;

alter table o_eva_form_session modify column fk_form_entry bigint;
alter table o_eva_form_session modify column fk_identity bigint;
alter table o_eva_form_session add column e_email varchar(1024);
alter table o_eva_form_session add column e_firstname varchar(1024);
alter table o_eva_form_session add column e_lastname varchar(1024);
alter table o_eva_form_session add column e_age varchar(1024);
alter table o_eva_form_session add column e_gender varchar(1024);
alter table o_eva_form_session add column e_org_unit varchar(1024);
alter table o_eva_form_session add column e_study_subject varchar(1024);
alter table o_eva_form_session add column fk_survey bigint;
alter table o_eva_form_session add column fk_participation bigint unique;

alter table o_eva_form_survey ENGINE = InnoDB;
alter table o_eva_form_participation ENGINE = InnoDB;

create unique index idx_eva_surv_ores_idx on o_eva_form_survey (e_resid, e_resname, e_sub_ident(255));

alter table o_eva_form_participation add constraint eva_part_to_surv_idx foreign key (fk_survey) references o_eva_form_survey (id);
create unique index idx_eva_part_ident_idx on o_eva_form_participation (e_identifier_key, e_identifier_type, fk_survey);
create unique index idx_eva_part_executor_idx on o_eva_form_participation (fk_executor, fk_survey);

alter table o_eva_form_session add constraint eva_sess_to_surv_idx foreign key (fk_survey) references o_eva_form_survey (id);
alter table o_eva_form_session add constraint eva_sess_to_part_idx foreign key (fk_participation) references o_eva_form_participation (id);

create index idx_eva_resp_report_idx on o_eva_form_response (fk_session, e_responseidentifier, e_no_response);

-- quality management
create table o_qual_data_collection (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_status varchar(50),
   q_title varchar(200),
   q_start datetime,
   q_deadline datetime,
   q_topic_type varchar(50),
   q_topic_custom varchar(200),
   q_topic_fk_identity bigint,
   q_topic_fk_organisation bigint,
   q_topic_fk_curriculum bigint,
   q_topic_fk_curriculum_element bigint,
   q_topic_fk_repository bigint,
   primary key (id)
);

create table o_qual_context (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_role varchar(20),
   fk_data_collection bigint not null,
   fk_eva_participation bigint,
   fk_eva_session bigint,
   fk_audience_repository bigint,
   fk_audience_cur_element bigint,
   primary key (id)
);

create table o_qual_context_to_organisation (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_context bigint not null,
   fk_organisation bigint not null,
   primary key (id)
);

create table o_qual_context_to_curriculum (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_context bigint not null,
   fk_curriculum bigint not null,
   primary key (id)
);

create table o_qual_context_to_cur_element (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_context bigint not null,
   fk_cur_element bigint not null,
   primary key (id)
);

create table o_qual_context_to_tax_level (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_context bigint not null,
   fk_tax_leveL bigint not null,
   primary key (id)
);

alter table o_qual_data_collection ENGINE = InnoDB;
alter table o_qual_context ENGINE = InnoDB;
alter table o_qual_context_to_organisation ENGINE = InnoDB;
alter table o_qual_context_to_curriculum ENGINE = InnoDB;
alter table o_qual_context_to_cur_element ENGINE = InnoDB;
alter table o_qual_context_to_tax_level ENGINE = InnoDB;

alter table o_qual_context add constraint qual_con_to_data_collection_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
alter table o_qual_context add constraint qual_con_to_participation_idx foreign key (fk_eva_participation) references o_eva_form_participation (id);
alter table o_qual_context add constraint qual_con_to_session_idx foreign key (fk_eva_session) references o_eva_form_session (id);

alter table o_qual_context_to_organisation add constraint qual_con_to_org_con_idx foreign key (fk_context) references o_qual_context (id);
create unique index idx_con_to_org_org_idx on o_qual_context_to_organisation (fk_organisation, fk_context);

alter table o_qual_context_to_curriculum add constraint qual_con_to_cur_con_idx foreign key (fk_context) references o_qual_context (id);
create unique index idx_con_to_cur_cur_idx on o_qual_context_to_curriculum (fk_curriculum, fk_context);

alter table o_qual_context_to_cur_element add constraint qual_con_to_cur_ele_con_idx foreign key (fk_context) references o_qual_context (id);
create unique index idx_con_to_cur_ele_ele_idx on o_qual_context_to_cur_element (fk_cur_element, fk_context);

alter table o_qual_context_to_tax_level add constraint qual_con_to_tax_level_con_idx foreign key (fk_context) references o_qual_context (id);
create unique index idx_con_to_tax_level_tax_idx on o_qual_context_to_tax_level (fk_tax_leveL, fk_context);


-- membership
alter table o_bs_group_member add column g_inheritance_mode varchar(16) default 'none' not null;


-- lectures
alter table o_lecture_block_roll_call add column l_appeal_reason mediumtext;
alter table o_lecture_block_roll_call add column l_appeal_status mediumtext;
alter table o_lecture_block_roll_call add column l_appeal_status_reason mediumtext;

