
-- Tags
create table o_tag_tag (
   id bigint not null auto_increment,
   creationdate datetime not null,
   t_display_name varchar(256) not null,
   primary key (id)
);
alter table o_tag_tag ENGINE = InnoDB;

create unique index idx_tag_name_idx on o_tag_tag (t_display_name);


-- ToDo
create table o_todo_task (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   t_content_modified_date datetime not null,
   t_title varchar(128),
   t_description longtext,
   t_status varchar(16),
   t_priority varchar(16),
   t_expenditure_of_work integer,
   t_start_date datetime,
   t_due_date datetime,
   t_done_date datetime,
   t_type varchar(50),
   t_origin_id bigint,
   t_origin_subpath varchar(100),
   t_origin_title varchar(500),
   t_origin_deleted bool default false not null,
   fk_group bigint not null,
   primary key (id)
);
create table o_todo_task_tag (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_todo_task bigint not null,
   fk_tag bigint not null,
   primary key (id)
);
alter table o_todo_task ENGINE = InnoDB;
alter table o_todo_task_tag ENGINE = InnoDB;

create index idx_todo_origin_id_idx on o_todo_task (t_origin_id);
create index idx_todo_tag_todo_idx on o_todo_task_tag (fk_todo_task);
create index idx_todo_tag_tag_idx on o_todo_task_tag (fk_tag);


-- Projects
create table o_proj_project (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_external_ref varchar(128),
   p_status varchar(32),
   p_title varchar(128),
   p_teaser varchar(150),
   p_description longtext,
   fk_creator bigint not null,
   fk_group bigint not null,
   primary key (id)
);
create table o_proj_project_to_org (
  id bigint not null auto_increment,
  creationdate datetime not null,
  fk_project bigint not null,
  fk_organisation bigint not null,
  primary key (id)
);
create table o_proj_project_user_info (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_last_visit_date datetime,
   fk_project bigint not null,
   fk_identity bigint not null,
   primary key (id)
);
create table o_proj_artefact (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_type varchar(32),
   p_content_modified_date datetime not null,
   fk_content_modified_by bigint not null,
   p_status varchar(32),
   fk_project bigint not null,
   fk_creator bigint not null,
   fk_group bigint not null,
   primary key (id)
);
create table o_proj_artefact_to_artefact (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_artefact1 bigint not null,
   fk_artefact2 bigint not null,
   fk_project bigint not null,
   fk_creator bigint not null,
   primary key (id)
);
create table o_proj_tag (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_project bigint not null,
   fk_artefact bigint,
   fk_tag bigint not null,
   primary key (id)
);
create table o_proj_file (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   fk_metadata bigint not null,
   fk_artefact bigint not null,
   primary key (id)
);
create table o_proj_todo (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_identifier varchar(64) not null,
   fk_todo_task bigint not null,
   fk_artefact bigint not null,
   primary key (id)
);
create table o_proj_note (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_title varchar(128),
   p_text longtext,
   fk_artefact bigint not null,
   primary key (id)
);
create table o_proj_appointment (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_identifier varchar(64) not null,
   p_event_id varchar(64) not null,
   p_recurrence_id varchar(500),
   p_start_date datetime,
   p_end_date datetime,
   p_subject varchar(256),
   p_description longtext,
   p_location varchar(1024),
   p_color varchar(50),
   p_all_day bool default false not null,
   p_recurrence_rule varchar(100),
   p_recurrence_exclusion varchar(4000),
   fk_artefact bigint not null,
   primary key (id)
);
create table o_proj_milestone (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_identifier varchar(64) not null,
   p_status varchar(32),
   p_due_date datetime,
   p_subject varchar(256),
   p_description longtext,
   p_color varchar(50),
   fk_artefact bigint not null,
   primary key (id)
);
create table o_proj_activity (
   id bigint not null auto_increment,
   creationdate datetime not null,
   p_action varchar(32) not null,
   p_action_target varchar(32) not null,
   p_before longtext,
   p_after longtext,
   p_temp_identifier varchar(100),
   fk_doer bigint not null,
   fk_project bigint not null,
   fk_artefact bigint,
   fk_artefact_reference bigint,
   fk_member bigint,
   fk_organisation bigint,
   primary key (id)
);
alter table o_proj_project ENGINE = InnoDB;
alter table o_proj_project_to_org ENGINE = InnoDB;
alter table o_proj_project_user_info ENGINE = InnoDB;
alter table o_proj_artefact ENGINE = InnoDB;
alter table o_proj_artefact_to_artefact ENGINE = InnoDB;
alter table o_proj_tag ENGINE = InnoDB;
alter table o_proj_file ENGINE = InnoDB;
alter table o_proj_todo ENGINE = InnoDB;
alter table o_proj_note ENGINE = InnoDB;
alter table o_proj_appointment ENGINE = InnoDB;
alter table o_proj_milestone ENGINE = InnoDB;
alter table o_proj_activity ENGINE = InnoDB;

alter table o_proj_project add constraint project_creator_idx foreign key (fk_creator) references o_bs_identity(id);
alter table o_proj_project add constraint project_group_idx foreign key (fk_group) references o_bs_group (id);
alter table o_proj_project_to_org add constraint rel_pto_project_idx foreign key (fk_project) references o_proj_project (id);
alter table o_proj_project_to_org add constraint rel_pto_org_idx foreign key (fk_organisation) references o_org_organisation (id);
alter table o_proj_project_user_info add constraint rel_pui_project_idx foreign key (fk_project) references o_proj_project (id);
alter table o_proj_project_user_info add constraint rel_pui_identity_idx foreign key (fk_identity) references o_bs_identity(id);

alter table o_proj_artefact add constraint artefact_modby_idx foreign key (fk_content_modified_by) references o_bs_identity(id);
alter table o_proj_artefact add constraint artefact_project_idx foreign key (fk_project) references o_proj_project (id);
alter table o_proj_artefact add constraint artefact_creator_idx foreign key (fk_creator) references o_bs_identity(id);
alter table o_proj_artefact add constraint artefact_group_idx foreign key (fk_group) references o_bs_group (id);
alter table o_proj_artefact_to_artefact add constraint projata_artefact1_idx foreign key (fk_artefact1) references o_proj_artefact (id);
alter table o_proj_artefact_to_artefact add constraint projata_artefact2_idx foreign key (fk_artefact2) references o_proj_artefact (id);
alter table o_proj_artefact_to_artefact add constraint projata_project_idx foreign key (fk_project) references o_proj_project (id);
alter table o_proj_artefact_to_artefact add constraint projata_creator_idx foreign key (fk_creator) references o_bs_identity(id);

alter table o_proj_tag add constraint tag_project_idx foreign key (fk_project) references o_proj_project (id);
alter table o_proj_tag add constraint tag_artefact_idx foreign key (fk_artefact) references o_proj_artefact (id);
alter table o_proj_tag add constraint tag_tag_idx foreign key (fk_tag) references o_tag_tag (id);

alter table o_proj_file add constraint file_artefact_idx foreign key (fk_artefact) references o_proj_artefact (id);
alter table o_proj_file add constraint file_metadata_idx foreign key (fk_metadata) references o_vfs_metadata(id);
alter table o_proj_todo add constraint todo_artefact_idx foreign key (fk_artefact) references o_proj_artefact (id);
alter table o_proj_todo add constraint todo_todo_idx foreign key (fk_todo_task) references o_todo_task(id);
create unique index idx_todo_ident_idx on o_proj_todo (p_identifier);
alter table o_proj_note add constraint note_artefact_idx foreign key (fk_artefact) references o_proj_artefact (id);
alter table o_proj_appointment add constraint appointment_artefact_idx foreign key (fk_artefact) references o_proj_artefact (id);
create unique index idx_appointment_ident_idx on o_proj_appointment (p_identifier);
alter table o_proj_milestone add constraint milestone_artefact_idx foreign key (fk_artefact) references o_proj_artefact (id);
create unique index idx_milestone_ident_idx on o_proj_milestone (p_identifier);

alter table o_proj_activity add constraint activity_doer_idx foreign key (fk_doer) references o_bs_identity (id);
alter table o_proj_activity add constraint activity_project_idx foreign key (fk_project) references o_proj_project (id);
alter table o_proj_activity add constraint activity_artefact_idx foreign key (fk_artefact) references o_proj_artefact (id);
alter table o_proj_activity add constraint activity_artefact_ref_idx foreign key (fk_artefact_reference) references o_proj_artefact (id);
alter table o_proj_activity add constraint activity_member_idx foreign key (fk_member) references o_bs_identity (id);
alter table o_proj_activity add constraint activity_organisation_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_activity_temp_ident_idx on o_proj_activity (p_temp_identifier);


-- JupyterHub
create table o_jup_hub (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   j_name varchar(255) not null,
   j_status varchar(255) not null,
   j_ram varchar(255) not null,
   j_cpu bigint not null,
   j_image_checking_service_url varchar(255),
   j_info_text mediumtext,
   j_lti_key varchar(255),
   j_access_token varchar(255),
   j_agreement_setting varchar(32) default 'suppressAgreement' not null,
   fk_lti_tool_id bigint not null,
   primary key (id)
);
alter table o_jup_hub ENGINE = InnoDB;

create table o_jup_deployment (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   j_description varchar(255),
   j_image varchar(255) not null,
   j_suppress_data_transmission_agreement bit,
   fk_hub bigint not null,
   fk_lti_tool_deployment_id bigint not null,
   primary key (id)
);
alter table o_jup_deployment ENGINE = InnoDB;

alter table o_jup_hub add constraint jup_hub_tool_idx foreign key (fk_lti_tool_id) references o_lti_tool (id);
create index idx_jup_hub_tool_idx on o_jup_hub (fk_lti_tool_id);

alter table o_jup_deployment add constraint jup_deployment_hub_idx foreign key (fk_hub) references o_jup_hub (id);
create index idx_jup_deployment_hub_idx on o_jup_deployment (fk_hub);

alter table o_jup_deployment add constraint jup_deployment_tool_deployment_idx foreign key (fk_lti_tool_deployment_id) references o_lti_tool_deployment (id);
create index idx_jup_deployment_tool_deployment_idx on o_jup_deployment (fk_lti_tool_deployment_id);


-- Quality management
create table o_qual_audit_log (
   id bigint not null auto_increment,
   creationdate datetime not null,
   q_action varchar(32) not null,
   q_before longtext,
   q_after longtext,
   fk_doer bigint not null,
   fk_data_collection bigint,
   fk_todo_task bigint,
   fk_identity bigint,
   primary key (id)
);
alter table o_qual_audit_log ENGINE = InnoDB;

create index idx_qm_audit_doer_idx on o_qual_audit_log (fk_doer);
create index idx_qm_audit_dc_idx on o_qual_audit_log (fk_data_collection);
create index idx_qm_audit_todo_idx on o_qual_audit_log (fk_todo_task);
create index idx_qm_audit_ident_idx on o_qual_audit_log (fk_identity);
