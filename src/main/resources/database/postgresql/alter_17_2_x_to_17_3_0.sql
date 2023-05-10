
-- Document editor
alter table o_de_access add o_fire_saved_event bool default false;

-- Tags
create table o_tag_tag (
   id bigserial,
   creationdate timestamp not null,
   t_display_name varchar(256) not null,
   primary key (id)
);
create unique index idx_tag_name_idx on o_tag_tag (t_display_name);


-- ToDo
create table o_todo_task (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   t_content_modified_date timestamp not null,
   t_title varchar(128),
   t_description text,
   t_status varchar(16),
   t_priority varchar(16),
   t_expenditure_of_work int8,
   t_start_date timestamp,
   t_due_date timestamp,
   t_done_date timestamp,
   t_type varchar(50),
   t_origin_id int8,
   t_origin_subpath varchar(100),
   t_origin_title varchar(500),
   t_origin_deleted bool default false not null,
   fk_group int8 not null,
   primary key (id)
);
create table o_todo_task_tag (
   id bigserial,
   creationdate timestamp not null,
   fk_todo_task int8 not null,
   fk_tag int8,
   primary key (id)
);

alter table o_todo_task add constraint todo_to_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_todo_group_idx on o_todo_task (fk_group);
create index idx_todo_origin_id_idx on o_todo_task (t_origin_id);
alter table o_todo_task_tag add constraint todo_task_tag_todo_idx foreign key (fk_todo_task) references o_todo_task(id);
create index idx_todo_task_tag_todo_idx on o_todo_task_tag (fk_todo_task);
alter table o_todo_task_tag add constraint tag_tag_idx foreign key (fk_tag) references o_tag_tag(id);
create index idx_todo_task_tag_tag_idx on o_todo_task_tag (fk_tag);


-- Projects
create table o_proj_project (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_external_ref varchar(128),
   p_status varchar(32),
   p_title varchar(128),
   p_teaser varchar(150),
   p_description text,
   p_avatar_css_class varchar(32),
   fk_creator int8 not null,
   fk_group int8 not null,
   primary key (id)
);
create table o_proj_project_to_org (
   id bigserial,
   creationdate timestamp not null,
   fk_project int8 not null,
   fk_organisation int8 not null,
   primary key (id)
);
create table o_proj_project_user_info (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_last_visit_date timestamp,
   fk_project int8 not null,
   fk_identity int8 not null,
   primary key (id)
);
create table o_proj_artefact (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_type varchar(32),
   p_content_modified_date timestamp not null,
   fk_content_modified_by int8 not null,
   p_status varchar(32),
   fk_project int8 not null,
   fk_creator int8 not null,
   fk_group int8 not null,
   primary key (id)
);
create table o_proj_artefact_to_artefact (
   id bigserial,
   creationdate timestamp not null,
   fk_artefact1 int8 not null,
   fk_artefact2 int8 not null,
   fk_project int8 not null,
   fk_creator int8 not null,
   primary key (id)
);
create table o_proj_tag (
   id bigserial,
   creationdate timestamp not null,
   fk_project int8 not null,
   fk_artefact int8,
   fk_tag int8,
   primary key (id)
);
create table o_proj_file (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   fk_metadata int8 not null,
   fk_artefact int8 not null,
   primary key (id)
);
create table o_proj_todo (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_identifier varchar(64) not null,
   fk_todo_task int8 not null,
   fk_artefact int8 not null,
   primary key (id)
);
create table o_proj_note (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_title varchar(128),
   p_text text,
   fk_artefact int8 not null,
   primary key (id)
);
create table o_proj_appointment (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_identifier varchar(64) not null,
   p_event_id varchar(64) not null,
   p_recurrence_id varchar(500),
   p_start_date timestamp,
   p_end_date timestamp,
   p_subject varchar(256),
   p_description text,
   p_location varchar(1024),
   p_color varchar(50),
   p_all_day bool default false not null,
   p_recurrence_rule varchar(100),
   p_recurrence_exclusion varchar(4000),
   fk_artefact int8 not null,
   primary key (id)
);
create table o_proj_milestone (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_identifier varchar(64) not null,
   p_status varchar(32),
   p_due_date timestamp,
   p_subject varchar(256),
   p_description text,
   p_color varchar(50),
   fk_artefact int8 not null,
   primary key (id)
);
create table o_proj_activity (
   id bigserial,
   creationdate timestamp not null,
   p_action varchar(32) not null,
   p_action_target varchar(32) not null,
   p_before text,
   p_after text,
   p_temp_identifier varchar(100),
   fk_doer int8 not null,
   fk_project int8 not null,
   fk_artefact int8,
   fk_artefact_reference int8,
   fk_member int8,
   fk_organisation int8,
   primary key (id)
);

alter table o_proj_project add constraint project_creator_idx foreign key (fk_creator) references o_bs_identity(id);
create index idx_proj_project_creator_idx on o_proj_project (fk_creator);
alter table o_proj_project add constraint project_to_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_proj_project_group_idx on o_proj_project (fk_group);
alter table o_proj_project_to_org add constraint rel_pto_project_idx foreign key (fk_project) references o_proj_project(id);
create index idx_rel_pto_project_idx on o_proj_project_to_org (fk_project);
alter table o_proj_project_to_org add constraint rel_pto_org_idx foreign key (fk_organisation) references o_org_organisation(id);
create index idx_rel_pto_org_idx on o_proj_project_to_org (fk_organisation);
alter table o_proj_project_user_info add constraint rel_pui_project_idx foreign key (fk_project) references o_proj_project(id);
create index idx_rel_pui_project_idx on o_proj_project_user_info (fk_project);
alter table o_proj_project_user_info add constraint rel_pui_idenity_idx foreign key (fk_identity) references o_bs_identity(id);
create index idx_rel_pui_identity_idx on o_proj_project_user_info (fk_identity);

alter table o_proj_artefact add constraint artefact_modby_idx foreign key (fk_content_modified_by) references o_bs_identity(id);
create index idx_artefact_modby_idx on o_proj_artefact (fk_content_modified_by);
alter table o_proj_artefact add constraint artefact_project_idx foreign key (fk_project) references o_proj_project(id);
create index idx_artefact_project_idx on o_proj_artefact (fk_project);
alter table o_proj_artefact add constraint artefact_creator_idx foreign key (fk_creator) references o_bs_identity(id);
create index idx_artefact_creator_idx on o_proj_artefact (fk_creator);
alter table o_proj_artefact add constraint artefact_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_artefact_group_idx on o_proj_artefact (fk_group);

alter table o_proj_artefact_to_artefact add constraint projata_artefact1_idx foreign key (fk_artefact1) references o_proj_artefact(id);
create index idx_projata_artefact1_idx on o_proj_artefact_to_artefact (fk_artefact1);
alter table o_proj_artefact_to_artefact add constraint projata_artefact2_idx foreign key (fk_artefact2) references o_proj_artefact(id);
create index idx_projata_artefact2_idx on o_proj_artefact_to_artefact (fk_artefact2);
alter table o_proj_artefact_to_artefact add constraint projata_project_idx foreign key (fk_project) references o_proj_project(id);
create index idx_projata_project_idx on o_proj_artefact_to_artefact (fk_project);
alter table o_proj_artefact_to_artefact add constraint projata_creator_idx foreign key (fk_creator) references o_bs_identity(id);
create index idx_projata_creator_idx on o_proj_artefact_to_artefact (fk_creator);

alter table o_proj_tag add constraint tag_project_idx foreign key (fk_project) references o_proj_project(id);
create index idx_tag_project_idx on o_proj_tag (fk_project);
alter table o_proj_tag add constraint tag_artefact_idx foreign key (fk_artefact) references o_proj_artefact(id);
create index idx_tag_artefact_idx on o_proj_tag (fk_artefact);
alter table o_proj_tag add constraint tag_tag_idx foreign key (fk_tag) references o_tag_tag(id);
create index idx_tag_tag_idx on o_proj_tag (fk_tag);

alter table o_proj_file add constraint file_artefact_idx foreign key (fk_artefact) references o_proj_artefact(id);
create index idx_file_artefact_idx on o_proj_file (fk_artefact);
alter table o_proj_file add constraint file_metadata_idx foreign key (fk_metadata) references o_vfs_metadata(id);
create index idx_file_metadata_idx on o_proj_file (fk_metadata);

alter table o_proj_todo add constraint todo_artefact_idx foreign key (fk_artefact) references o_proj_artefact(id);
create index idx_todo_artefact_idx on o_proj_todo (fk_artefact);
alter table o_proj_todo add constraint todo_todo_idx foreign key (fk_todo_task) references o_todo_task(id);
create index idx_todo_todo_idx on o_proj_todo (fk_todo_task);
create unique index idx_todo_ident_idx on o_proj_todo (p_identifier);

alter table o_proj_note add constraint note_artefact_idx foreign key (fk_artefact) references o_proj_artefact(id);
create index idx_note_artefact_idx on o_proj_file (fk_artefact);

alter table o_proj_appointment add constraint appointment_artefact_idx foreign key (fk_artefact) references o_proj_artefact(id);
create index idx_appointment_artefact_idx on o_proj_file (fk_artefact);
create unique index idx_appointment_ident_idx on o_proj_appointment (p_identifier);

alter table o_proj_milestone add constraint milestone_artefact_idx foreign key (fk_artefact) references o_proj_artefact(id);
create index idx_milestone_artefact_idx on o_proj_file (fk_artefact);
create unique index idx_milestone_ident_idx on o_proj_milestone (p_identifier);

alter table o_proj_activity add constraint activity_doer_idx foreign key (fk_doer) references o_bs_identity(id);
create index idx_activity_doer_idx on o_proj_activity (fk_doer);
alter table o_proj_activity add constraint activity_project_idx foreign key (fk_project) references o_proj_project(id);
create index idx_activity_project_idx on o_proj_activity (fk_project);
alter table o_proj_activity add constraint activity_artefact_idx foreign key (fk_artefact) references o_proj_artefact(id);
create index idx_activity_artefact_idx on o_proj_activity (fk_artefact);
alter table o_proj_activity add constraint activity_artefact_reference_idx foreign key (fk_artefact_reference) references o_proj_artefact(id);
create index idx_activity_artefact_reference_idx on o_proj_activity (fk_artefact_reference);
alter table o_proj_activity add constraint activity_member_idx foreign key (fk_member) references o_bs_identity(id);
create index idx_activity_member_idx on o_proj_activity (fk_member);
alter table o_proj_activity add constraint activity_organisation_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_activity_organisation_idx on o_proj_activity (fk_organisation);
create index idx_activity_temp_ident_idx on o_proj_activity (p_temp_identifier);

-- Certificate
create table o_cer_entry_config (
  id bigserial not null,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  c_cer_auto_enabled bool default false not null,
  c_cer_manual_enabled bool default false not null,
  c_cer_custom_1 varchar(4000),
  c_cer_custom_2 varchar(4000),
  c_cer_custom_3 varchar(4000),
  c_validity_enabled bool default false not null,
  c_validity_timelapse int8 default 0 not null,
  c_validity_timelapse_unit varchar(32),
  c_recer_enabled bool default false not null,
  c_recer_leadtime_enabled bool default false not null,
  c_recer_leadtime_days int8 default 0 not null,
  fk_template int8,
  fk_entry int8 not null,
  unique(fk_entry),
  primary key (id)
);

alter table o_cer_entry_config add constraint cer_entry_config_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_cer_entry_config_entry_idx on o_cer_entry_config(fk_entry);
alter table o_cer_entry_config add constraint template_config_entry_idx foreign key (fk_template) references o_cer_template (id);
create index idx_template_config_entry_idx on o_cer_entry_config(fk_template);

alter table o_rem_sent_reminder add column r_run int8 default 1 not null;

-- JupyterHub
create table o_jup_hub (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   j_name varchar(255) not null,
   j_status varchar(255) not null,
   j_ram varchar(255) not null,
   j_cpu int8 not null,
   j_image_checking_service_url varchar(255),
   j_info_text text,
   j_lti_key varchar(255),
   j_access_token varchar(255),
   j_agreement_setting varchar(32) default 'suppressAgreement' not null,
   fk_lti_tool_id int8 not null,
   primary key (id)
);

create table o_jup_deployment (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   j_description varchar(255),
   j_image varchar(255) not null,
   j_suppress_data_transmission_agreement bool,
   fk_hub int8 not null,
   fk_lti_tool_deployment_id int8 not null,
   primary key (id)
);

alter table o_jup_hub add constraint jup_hub_tool_idx foreign key (fk_lti_tool_id) references o_lti_tool (id);
create index idx_jup_hub_tool_idx on o_jup_hub (fk_lti_tool_id);

alter table o_jup_deployment add constraint jup_deployment_hub_idx foreign key (fk_hub) references o_jup_hub (id);
create index idx_jup_deployment_hub_idx on o_jup_deployment (fk_hub);

alter table o_jup_deployment add constraint jup_deployment_tool_deployment_idx foreign key (fk_lti_tool_deployment_id) references o_lti_tool_deployment (id);
create index idx_jup_deployment_tool_deployment_idx on o_jup_deployment (fk_lti_tool_deployment_id);


-- Quality management
create table o_qual_audit_log (
   id bigserial,
   creationdate timestamp not null,
   q_action varchar(32) not null,
   q_before text,
   q_after text,
   fk_doer int8 not null,
   fk_data_collection int8,
   fk_todo_task int8,
   fk_identity int8,
   primary key (id)
);
create index idx_qm_audit_doer_idx on o_qual_audit_log (fk_doer);
create index idx_qm_audit_dc_idx on o_qual_audit_log (fk_data_collection);
create index idx_qm_audit_todo_idx on o_qual_audit_log (fk_todo_task);
create index idx_qm_audit_ident_idx on o_qual_audit_log (fk_identity);
