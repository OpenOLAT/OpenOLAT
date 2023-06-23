
-- Document editor
alter table o_de_access add o_fire_saved_event number default 0;

-- Tags
create table o_tag_tag (
   id number(20) generated always as identity,
   creationdate date not null,
   t_display_name varchar(256) not null,
   primary key (id)
);
create unique index idx_tag_name_idx on o_tag_tag (t_display_name);


-- ToDo
create table o_todo_task (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   t_content_modified_date date not null,
   t_title varchar(128),
   t_description CLOB,
   t_status varchar(16),
   t_priority varchar(16),
   t_start_date date,
   t_expenditure_of_work number(20),
   t_due_date date,
   t_done_date date,
   t_type varchar(50),
   t_deleted_date timestamp,
   fk_deleted_by number(20),
   t_origin_id number(20),
   t_origin_subpath varchar(100),
   t_origin_title varchar(500),
   t_origin_deleted number default 0 not null,
   t_origin_deleted_date timestamp,
   fk_origin_deleted_by number(20),
   fk_group  number(20) not null,
   primary key (id)
);
create table o_todo_task_tag (
   id number(20) generated always as identity,
   creationdate date not null,
   fk_todo_task number(20) not null,
   fk_tag number(20),
   primary key (id)
);

alter table o_todo_task add constraint todo_to_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_todo_group_idx on o_todo_task (fk_group);
create index idx_todo_origin_id_idx on o_todo_task (t_origin_id);
alter table o_todo_task_tag add constraint todo_task_tag_todo_idx foreign key (fk_todo_task) references o_todo_task(id);
create index idx_todo_task_tag_idx on o_todo_task_tag (fk_todo_task);
alter table o_todo_task_tag add constraint todo_task_tag_tag_idx foreign key (fk_tag) references o_tag_tag(id);
create index idx_todo_task_tag_tag_idx on o_todo_task_tag (fk_tag);


-- Projects
create table o_proj_project (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   p_external_ref varchar(128),
   p_status varchar(32),
   p_title varchar(128),
   p_teaser varchar(150),
   p_description CLOB,
   p_avatar_css_class varchar(32),
   p_template_private number default 0 not null,
   p_template_public number default 0 not null,
   p_deleted_date timestamp,
   fk_deleted_by number(20),
   fk_creator number(20) not null,
   fk_group number(20) not null,
   primary key (id)
);
create table o_proj_project_to_org (
   id number(20) generated always as identity,
   creationdate date not null,
   fk_project number(20) not null,
   fk_organisation number(20) not null,
   primary key (id)
);
create table o_proj_template_to_org (
   id number(20) generated always as identity,
   creationdate date not null,
   fk_project number(20) not null,
   fk_organisation number(20) not null,
   primary key (id)
);
create table o_proj_project_user_info (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   p_last_visit_date timestamp,
   fk_project number(20) not null,
   fk_identity number(20) not null,
   primary key (id)
);
create table o_proj_artefact (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   p_type varchar(32),
   p_content_modified_date timestamp not null,
   fk_content_modified_by number(20) not null,
   p_deleted_date timestamp,
   fk_deleted_by number(20),
   p_status varchar(32),
   fk_project number(20) not null,
   fk_creator number(20) not null,
   fk_group number(20) not null,
   primary key (id)
);
create table o_proj_artefact_to_artefact (
   id number(20) generated always as identity,
   creationdate date not null,
   fk_artefact1 number(20) not null,
   fk_artefact2 number(20) not null,
   fk_project number(20) not null,
   fk_creator number(20) not null,
   primary key (id)
);
create table o_proj_tag (
   id number(20) generated always as identity,
   creationdate date not null,
   fk_project number(20) not null,
   fk_artefact number(20),
   fk_tag number(20),
   primary key (id)
);
create table o_proj_file (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   fk_metadata number(20) not null,
   fk_artefact number(20) not null,
   primary key (id)
);
create table o_proj_todo (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   p_identifier varchar(64) not null,
   fk_todo_task number(20) not null,
   fk_artefact number(20) not null,
   primary key (id)
);
create table o_proj_note (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   p_title varchar(128),
   p_text CLOB,
   fk_artefact number(20) not null,
   primary key (id)
);
create table o_proj_appointment (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   p_identifier varchar(64) not null,
   p_event_id varchar(64) not null,
   p_recurrence_id varchar(500),
   p_start_date date,
   p_end_date date,
   p_subject varchar(256),
   p_description CLOB,
   p_location varchar(1024),
   p_color varchar(50),
   p_all_day number default 0 not null,
   p_recurrence_rule varchar(100),
   p_recurrence_exclusion varchar(4000),
   fk_artefact  number(20) not null,
   primary key (id)
);
create table o_proj_milestone (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   p_identifier varchar(64) not null,
   p_status varchar(32),
   p_due_date date,
   p_subject varchar(256),
   p_description CLOB,
   p_color varchar(50),
   fk_artefact  number(20) not null,
   primary key (id)
);
create table o_proj_decision (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   p_title varchar(2000),
   p_details CLOB,
   p_decision_date date,
   fk_artefact  number(20) not null,
   primary key (id)
);
create table o_proj_activity (
   id number(20) generated always as identity,
   creationdate date not null,
   p_action varchar(32) not null,
   p_action_target varchar(32) not null,
   p_before CLOB,
   p_after CLOB,
   p_temp_identifier varchar(100),
   fk_doer number(20) not null,
   fk_project number(20) not null,
   fk_artefact number(20),
   fk_artefact_reference number(20),
   fk_member number(20),
   fk_organisation number(20),
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
alter table o_proj_template_to_org add constraint rel_tto_project_idx foreign key (fk_project) references o_proj_project(id);
create index idx_rel_tto_project_idx on o_proj_template_to_org (fk_project);
alter table o_proj_template_to_org add constraint rel_tto_org_idx foreign key (fk_organisation) references o_org_organisation(id);
create index idx_rel_tto_org_idx on o_proj_template_to_org (fk_organisation);
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
create index idx_tag_tagt_idx on o_proj_tag (fk_tag);

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

alter table o_proj_decision add constraint decision_artefact_idx foreign key (fk_artefact) references o_proj_artefact(id);
create index idx_decision_artefact_idx on o_proj_file (fk_artefact);

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
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  c_cer_auto_enabled number default 0 not null,
  c_cer_manual_enabled number default 0 not null,
  c_cer_custom_1 varchar(4000),
  c_cer_custom_2 varchar(4000),
  c_cer_custom_3 varchar(4000),
  c_validity_enabled number default 0 not null,
  c_validity_timelapse number(20) default 0 not null,
  c_validity_timelapse_unit varchar(32),
  c_recer_enabled number default 0 not null,
  c_recer_leadtime_enabled number default 0 not null,
  c_recer_leadtime_days number(20) default 0 not null,
  fk_template number(20),
  fk_entry number(20) not null,
  unique(fk_entry),
  primary key (id)
);

alter table o_cer_entry_config add constraint cer_entry_config_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_cer_entry_config add constraint template_config_entry_idx foreign key (fk_template) references o_cer_template (id);
create index idx_template_config_entry_idx on o_cer_entry_config(fk_template);

alter table o_rem_sent_reminder add r_run number(20) default 1 not null;


-- Assessment entry
alter table o_as_entry add a_passed_date date;


-- JupyterHub
create table o_jup_hub (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   j_name varchar(255) not null,
   j_status varchar(255) not null,
   j_ram varchar(255) not null,
   j_cpu number(20) not null,
   j_image_checking_service_url varchar(255),
   j_info_text varchar2(4000),
   j_lti_key varchar(255),
   j_access_token varchar(255),
   j_agreement_setting varchar(32) default 'suppressAgreement' not null,
   fk_lti_tool_id number(20) not null,
   primary key (id)
);

create table o_jup_deployment (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   j_description varchar(255),
   j_image varchar(255) not null,
   j_suppress_data_transmission_agreement number,
   fk_hub number(20) not null,
   fk_lti_tool_deployment_id number(20) not null,
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
   id number(20) generated always as identity,
   creationdate date not null,
   q_action varchar(32) not null,
   q_before CLOB,
   q_after CLOB,
   fk_doer number(20) not null,
   fk_data_collection number(20),
   fk_todo_task number(20),
   fk_identity number(20),
   primary key (id)
);
create index idx_qm_audit_doer_idx on o_qual_audit_log (fk_doer);
create index idx_qm_audit_dc_idx on o_qual_audit_log (fk_data_collection);
create index idx_qm_audit_todo_idx on o_qual_audit_log (fk_todo_task);
create index idx_qm_audit_ident_idx on o_qual_audit_log (fk_identity);


-- Assessment message
update o_as_message set a_publication_type='asap' where a_publication_type='0';
update o_as_message set a_publication_type='scheduled' where a_publication_type='1';

update o_gta_task set g_submission_drole='coach' where g_submission_drole='0';
update o_gta_task set g_submission_drole='user' where g_submission_drole='1';
update o_gta_task set g_submission_drole='auto' where g_submission_drole='2';

update o_gta_task set g_submission_revisions_drole='coach' where g_submission_revisions_drole='0';
update o_gta_task set g_submission_revisions_drole='user' where g_submission_revisions_drole='1';
update o_gta_task set g_submission_revisions_drole='auto' where g_submission_revisions_drole='2';

alter table o_info_message add publishdate timestamp default null;
alter table o_info_message add published number default 0 not null;
alter table o_info_message add sendmailto varchar(255);

-- infoMessage connection to groups
create table o_info_message_to_group (
   id number(20) generated always as identity,
   fk_info_message_id number(20) not null,
   fk_group_id number(20) not null,
   primary key (id)
);

alter table o_info_message_to_group add constraint o_info_message_to_group_msg_idx foreign key (fk_info_message_id) references o_info_message (info_id);
create index idx_o_info_message_to_group_msg_idx on o_info_message_to_group (fk_info_message_id);
alter table o_info_message_to_group add constraint o_info_message_to_group_group_idx foreign key (fk_group_id) references o_gp_business (group_id);
create index idx_o_info_message_to_group_group_idx on o_info_message_to_group (fk_group_id);

-- infoMessage connection to curriculumElements
create table o_info_message_to_cur_el (
   id number(20) generated always as identity,
   fk_info_message_id number(20) not null,
   fk_cur_element_id number(20) not null,
   primary key (id)
);

alter table o_info_message_to_cur_el add constraint o_info_message_to_cur_el_msg_idx foreign key (fk_info_message_id) references o_info_message (info_id);
create index idx_o_info_message_to_cur_el_msg_idx on o_info_message_to_cur_el (fk_info_message_id);
alter table o_info_message_to_cur_el add constraint o_info_message_to_cur_el_curel_idx foreign key (fk_cur_element_id) references o_cur_curriculum_element (id);
create index idx_o_info_message_to_cur_el_curel_idx on o_info_message_to_cur_el (fk_cur_element_id);


-- Content editor
create table o_ce_audit_log (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   p_action varchar(32),
   p_before CLOB,
   p_after CLOB,
   fk_doer number(20),
   fk_page number(20),
   primary key (id)
);
alter table o_ce_audit_log add constraint ce_log_to_doer_idx foreign key (fk_doer) references o_bs_identity (id);
create index idx_ce_log_to_doer_idx on o_ce_audit_log (fk_doer);

create index idx_ce_log_to_page_idx on o_ce_audit_log (fk_page);


-- Open Badges
create table o_badge_template (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   b_image varchar(256) not null,
   b_name varchar(1024) not null,
   b_description clob,
   b_scopes varchar(128),
   b_placeholders varchar(1024),
   primary key (id)
);

create table o_badge_class (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   b_uuid varchar(36) not null,
   b_status varchar(256) not null,
   b_version varchar(32) not null,
   b_language varchar(32),
   b_image varchar(256) not null,
   b_name varchar(256) not null,
   b_description varchar(1024) not null,
   b_criteria clob,
   b_salt varchar(128) not null,
   b_issuer varchar(1024) not null,
   b_validity_enabled number default 0 not null,
   b_validity_timelapse number(20) default 0 not null,
   b_validity_timelapse_unit varchar(32),
   fk_entry number(20),
   primary key (id)
);

create table o_badge_assertion (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   b_uuid varchar(36) not null,
   b_status varchar(256) not null,
   b_recipient varchar(1024) not null,
   b_verification varchar(256) not null,
   b_issued_on date not null,
   b_baked_image varchar(256),
   b_evidence varchar(256),
   b_narrative varchar(1024),
   b_expires date,
   b_revocation_reason varchar(256),
   fk_badge_class number(20) not null,
   fk_recipient number(20) not null,
   fk_awarded_by number(20),
   primary key (id)
);

create table o_badge_category (
   id number(20) generated always as identity,
   creationdate date not null,
   fk_tag number(20) not null,
   fk_template number(20),
   fk_class number(20),
   primary key (id)
);

create table o_badge_entry_config (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   b_award_enabled number default 0 not null,
   b_owner_can_award number default 0 not null,
   b_coach_can_award number default 0 not null,
   fk_entry number(20) not null,
   unique(fk_entry),
   primary key (id)
);

create index o_badge_class_uuid_idx on o_badge_class (b_uuid);
create index o_badge_assertion_uuid_idx on o_badge_assertion (b_uuid);

alter table o_badge_class add constraint badge_class_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_badge_class_entry_idx on o_badge_class (fk_entry);

alter table o_badge_assertion add constraint badge_assertion_class_idx foreign key (fk_badge_class) references o_badge_class (id);
create index idx_badge_assertion_class_idx on o_badge_assertion (fk_badge_class);

alter table o_badge_assertion add constraint badge_assertion_recipient_idx foreign key (fk_recipient) references o_bs_identity (id);
create index idx_badge_assertion_recipient_idx on o_badge_assertion (fk_recipient);

alter table o_badge_assertion add constraint badge_assertion_awarded_by_idx foreign key (fk_awarded_by) references o_bs_identity (id);
create index idx_badge_assertion_awarded_by_idx on o_badge_assertion (fk_awarded_by);

alter table o_badge_category add constraint badge_category_tag_idx foreign key (fk_tag) references o_tag_tag (id);
create index idx_badge_category_tag_idx on o_badge_category (fk_tag);

alter table o_badge_category add constraint badge_category_template_idx foreign key (fk_template) references o_badge_template (id);
create index idx_badge_category_template_idx on o_badge_category (fk_template);

alter table o_badge_category add constraint badge_category_class_idx foreign key (fk_class) references o_badge_class (id);
create index idx_badge_category_class_idx on o_badge_category (fk_class);

alter table o_badge_entry_config add constraint badge_entry_config_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_badge_entry_config_entry_idx on o_badge_entry_config (fk_entry);

-- Dialog element
alter table o_dialog_element add d_authoredby varchar(64);