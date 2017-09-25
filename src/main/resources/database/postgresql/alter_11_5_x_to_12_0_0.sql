-- webfeed
create table o_feed (
   id bigserial not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   f_resourceable_id bigint,
   f_resourceable_type varchar(64),
   f_title varchar(1024),
   f_description varchar(1024),
   f_author varchar(255),
   f_image_name varchar(255),
   f_external boolean,
   f_external_feed_url varchar(1024),
   f_external_image_url varchar(1024),
   primary key (id)
);

create table o_feed_item (
   id bigserial not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   f_title varchar(1024),
   f_description text,
   f_content text,
   f_author varchar(255),
   f_guid varchar(255),
   f_external_link varchar(1024),
   f_draft boolean,
   f_publish_date timestamp,
   f_width int8,
   f_height int8,
   f_filename varchar(1024),
   f_type varchar(255),
   f_length bigint,
   f_external_url varchar(1024),
   fk_feed_id bigint,
   fk_identity_author_id int8,
   fk_identity_modified_id int8,
   primary key (id)
);

create index idx_feed_resourceable_idx on o_feed (f_resourceable_id, f_resourceable_type);
alter table o_feed_item add constraint item_to_feed_fk foreign key(fk_feed_id) references o_feed(id);
create index idx_item_feed_idx on o_feed_item(fk_feed_id);
alter table o_feed_item add constraint feed_item_to_ident_author_fk foreign key (fk_identity_author_id) references o_bs_identity (id);
create index idx_item_ident_author_idx on o_feed_item (fk_identity_author_id);
alter table o_feed_item add constraint feed_item_to_ident_modified_fk foreign key (fk_identity_modified_id) references o_bs_identity (id);
create index idx_item_ident_modified_idx on o_feed_item (fk_identity_modified_id);



create table o_lecture_reason (
  id bigserial not null,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  l_title varchar(255),
  l_descr varchar(2000),
  primary key (id)
);


create table o_lecture_block (
  id bigserial not null,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  l_external_id varchar(255),
  l_managed_flags varchar(255),
  l_title varchar(255),
  l_descr text,
  l_preparation text,
  l_location varchar(255),
  l_comment text,
  l_start_date timestamp not null,
  l_end_date timestamp not null,
  l_compulsory bool default true,
  l_eff_end_date timestamp,
  l_planned_lectures_num int8 not null default 0,
  l_effective_lectures_num int8 not null default 0,
  l_effective_lectures varchar(128),
  l_auto_close_date timestamp default null,
  l_status varchar(16) not null,
  l_roll_call_status varchar(16) not null,
  fk_reason int8,
  fk_entry int8 not null,
  fk_teacher_group int8 not null,
  primary key (id)
);

alter table o_lecture_block add constraint lec_block_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_lec_block_entry_idx on o_lecture_block(fk_entry);
alter table o_lecture_block add constraint lec_block_gcoach_idx foreign key (fk_teacher_group) references o_bs_group (id);
create index idx_lec_block_gcoach_idx on o_lecture_block(fk_teacher_group);
alter table o_lecture_block add constraint lec_block_reason_idx foreign key (fk_reason) references o_lecture_reason (id);
create index idx_lec_block_reason_idx on o_lecture_block(fk_reason);


create table o_lecture_block_to_group (
  id bigserial not null,
  fk_lecture_block int8 not null,
  fk_group int8 not null,
  primary key (id)
);

alter table o_lecture_block_to_group add constraint lec_block_to_block_idx foreign key (fk_group) references o_bs_group (id);
create index idx_lec_block_to_block_idx on o_lecture_block_to_group(fk_group);
alter table o_lecture_block_to_group add constraint lec_block_to_group_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_lec_block_to_group_idx on o_lecture_block_to_group(fk_lecture_block);


create table o_lecture_block_roll_call (
  id bigserial not null,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  l_comment text,
  l_lectures_attended varchar(128),
  l_lectures_absent varchar(128),
  l_lectures_attended_num int8 not null default 0,
  l_lectures_absent_num int8 not null default 0,
  l_absence_reason text,
  l_absence_authorized bool default null,
  l_absence_appeal_date timestamp,
  fk_lecture_block int8 not null,
  fk_identity int8 not null,
  primary key (id)
);

alter table o_lecture_block_roll_call add constraint lec_call_block_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_lec_call_block_idx on o_lecture_block_roll_call(fk_lecture_block);
alter table o_lecture_block_roll_call add constraint lec_call_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_lec_call_identity_idx on o_lecture_block_roll_call(fk_identity);


create table o_lecture_reminder (
  id bigserial not null,
  creationdate timestamp not null,
  l_status varchar(16) not null,
  fk_lecture_block int8 not null,
  fk_identity int8 not null,
  primary key (id)
);

alter table o_lecture_reminder add constraint lec_reminder_block_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_lec_reminder_block_idx on o_lecture_reminder(fk_lecture_block);
alter table o_lecture_reminder add constraint lec_reminder_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_lec_reminder_identity_idx on o_lecture_reminder(fk_identity);


create table o_lecture_participant_summary (
  id bigserial not null,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  l_required_attendance_rate float(24) default null,
  l_first_admission_date timestamp default null,
  l_attended_lectures int8 not null default 0,
  l_absent_lectures int8 not null default 0,
  l_excused_lectures int8 not null default 0,
  l_planneds_lectures int8 not null default 0,
  l_attendance_rate float(24) default null,
  l_cal_sync bool default false,
  l_cal_last_sync_date timestamp default null,
  fk_entry int8 not null,
  fk_identity int8 not null,
  primary key (id),
  unique (fk_entry, fk_identity)
);

alter table o_lecture_participant_summary add constraint lec_part_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_lec_part_entry_idx on o_lecture_participant_summary(fk_entry);
alter table o_lecture_participant_summary add constraint lec_part_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_lec_part_ident_idx on o_lecture_participant_summary(fk_identity);


create table o_lecture_entry_config (
  id bigserial not null,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  l_lecture_enabled bool default null,
  l_override_module_def bool default false,
  l_rollcall_enabled bool default null,
  l_calculate_attendance_rate bool default null,
  l_required_attendance_rate float(24) default null,
  l_sync_calendar_teacher bool default null,
  l_sync_calendar_participant bool default null,
  l_sync_calendar_course bool default null,
  fk_entry int8 not null,
  unique(fk_entry),
  primary key (id)
);

alter table o_lecture_entry_config add constraint lec_entry_config_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_lec_entry_conf_entry_idx on o_lecture_entry_config(fk_entry);


create table o_lecture_block_audit_log (
  id bigserial not null,
  creationdate timestamp not null,
  l_action varchar(32),
  l_val_before text,
  l_val_after text,
  l_message text,
  fk_lecture_block int8,
  fk_roll_call int8,
  fk_entry int8,
  fk_identity int8,
  fk_author int8,
  primary key (id)
);

create index idx_lec_audit_entry_idx on o_lecture_block_audit_log(fk_entry);
create index idx_lec_audit_ident_idx on o_lecture_block_audit_log(fk_identity);


alter table o_rem_reminder add column r_email_subject varchar(255);
update o_rem_reminder set r_email_subject=r_description;


alter table o_qti_assessment_marks add column q_hidden_rubrics text default null;


alter table o_gta_task add column g_submission_date timestamp default null;
alter table o_gta_task add column g_submission_revisions_date timestamp default null;
alter table o_gta_task add column g_collection_date timestamp default null;

alter table o_gta_task add column g_assignment_due_date timestamp default null;
alter table o_gta_task add column g_submission_due_date timestamp default null;
alter table o_gta_task add column g_revisions_due_date timestamp default null;
alter table o_gta_task add column g_solution_due_date timestamp default null;

alter table o_gta_task add column g_acceptation_date timestamp default null;
alter table o_gta_task add column g_solution_date timestamp default null;
alter table o_gta_task add column g_graduation_date timestamp default null;

alter table o_gta_task add column g_submission_ndocs int8 default null;
alter table o_gta_task add column g_submission_revisions_ndocs int8 default null;
alter table o_gta_task add column g_collection_ndocs int8 default null;

create table o_gta_task_revision_date (
  id bigserial not null,
  creationdate timestamp not null,
  g_status varchar(36) not null,
  g_rev_loop int8 not null,
  g_date timestamp not null,
  fk_task int8 not null,
  primary key (id)
);

alter table o_gta_task_revision_date add constraint gtaskrev_to_task_idx foreign key (fk_task) references o_gta_task (id);
create index idx_gtaskrev_to_task_idx on o_gta_task_revision_date (fk_task);

alter table o_gta_task add column g_allow_reset_date timestamp default null;
alter table o_gta_task add column fk_allow_reset_identity int8 default null;

alter table o_gta_task add constraint gtaskreset_to_allower_idx foreign key (fk_allow_reset_identity) references o_bs_identity (id);
create index idx_gtaskreset_to_allower_idx on o_gta_task (fk_allow_reset_identity);


alter table o_info_message add column attachmentpath varchar(1024) default null;


alter table o_as_entry add column lastcoachmodified timestamp default null;
alter table o_as_entry add column lastusermodified timestamp default null;

alter table o_as_eff_statement add column lastcoachmodified timestamp default null;
alter table o_as_eff_statement add column lastusermodified timestamp default null;



