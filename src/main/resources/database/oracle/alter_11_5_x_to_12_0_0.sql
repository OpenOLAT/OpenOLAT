-- webfeed
create table o_feed ( 
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   f_resourceable_id number(20),
   f_resourceable_type varchar(64),
   f_title varchar(1024), 
   f_description varchar(1024),
   f_author varchar(255),
   f_image_name varchar(255),
   f_external number(2) default 0,
   f_external_feed_url varchar(1024),
   f_external_image_url varchar(1024),
   primary key (id)
);

create table o_feed_item (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   f_title varchar(1024),
   f_description clob,
   f_content clob,
   f_author varchar(255),
   f_guid varchar(255),
   f_external_link varchar(1024),
   f_draft number(2) default 0,
   f_publish_date date,
   f_width number(20),
   f_height number(20),
   f_filename varchar(1024),
   f_type varchar(255),
   f_length number(20),
   f_external_url varchar(1024),
   fk_feed_id number(20),
   fk_identity_author_id number(20),
   fk_identity_modified_id number(20),
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
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  l_title varchar2(255 char),
  l_descr varchar2(2000 char),
  primary key (id)
);


create table o_lecture_block (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  l_external_id varchar2(255 char),
  l_managed_flags varchar2(255 char),
  l_title varchar2(255 char),
  l_descr clob,
  l_preparation clob,
  l_location varchar2(255 char),
  l_comment clob,
  l_start_date date not null,
  l_end_date date not null,
  l_compulsory number default 1 not null,
  l_eff_end_date date,
  l_planned_lectures_num number(20) default 0 not null,
  l_effective_lectures_num number(20) default 0 not null,
  l_effective_lectures varchar2(128 char),
  l_auto_close_date date default null,
  l_status varchar2(16 char) not null,
  l_roll_call_status varchar2(16 char) not null,
  fk_reason number(20),
  fk_entry number(20) not null,
  fk_teacher_group number(20) not null,
  primary key (id)
);

alter table o_lecture_block add constraint lec_block_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_lec_block_entry_idx on o_lecture_block(fk_entry);
alter table o_lecture_block add constraint lec_block_gcoach_idx foreign key (fk_teacher_group) references o_bs_group (id);
create index idx_lec_block_gcoach_idx on o_lecture_block(fk_teacher_group);
alter table o_lecture_block add constraint lec_block_reason_idx foreign key (fk_reason) references o_lecture_reason (id);
create index idx_lec_block_reason_idx on o_lecture_block(fk_reason);


create table o_lecture_block_to_group (
  id number(20) generated always as identity,
  fk_lecture_block number(20) not null,
  fk_group number(20) not null,
  primary key (id)
);

alter table o_lecture_block_to_group add constraint lec_block_to_block_idx foreign key (fk_group) references o_bs_group (id);
create index idx_lec_block_to_block_idx on o_lecture_block_to_group(fk_group);
alter table o_lecture_block_to_group add constraint lec_block_to_group_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_lec_block_to_group_idx on o_lecture_block_to_group(fk_lecture_block);


create table o_lecture_block_roll_call (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  l_comment clob,
  l_lectures_attended varchar2(128 char),
  l_lectures_absent varchar2(128 char),
  l_lectures_attended_num number(20) default 0 not null,
  l_lectures_absent_num number(20) default 0 not null,
  l_absence_reason clob,
  l_absence_authorized number default null,
  l_absence_appeal_date date,
  fk_lecture_block number(20) not null,
  fk_identity number(20) not null,
  primary key (id)
);

alter table o_lecture_block_roll_call add constraint lec_call_block_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_lec_call_block_idx on o_lecture_block_roll_call(fk_lecture_block);
alter table o_lecture_block_roll_call add constraint lec_call_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_lec_call_identity_idx on o_lecture_block_roll_call(fk_identity);


create table o_lecture_reminder (
  id number(20) generated always as identity,
  creationdate date not null,
  l_status varchar2(16 char) not null,
  fk_lecture_block number(20) not null,
  fk_identity number(20) not null,
  primary key (id)
);

alter table o_lecture_reminder add constraint lec_reminder_block_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_lec_reminder_block_idx on o_lecture_reminder(fk_lecture_block);
alter table o_lecture_reminder add constraint lec_reminder_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_lec_reminder_identity_idx on o_lecture_reminder(fk_identity);


create table o_lecture_participant_summary (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  l_required_attendance_rate float(24) default null,
  l_first_admission_date date default null,
  l_attended_lectures number(20) default 0 not null,
  l_absent_lectures number(20) default 0 not null,
  l_excused_lectures number(20) default 0 not null,
  l_planneds_lectures number(20) default 0 not null,
  l_attendance_rate float(24) default null,
  l_cal_sync number default 0 not null,
  l_cal_last_sync_date date default null,
  fk_entry number(20) not null,
  fk_identity number(20) not null,
  primary key (id),
  unique (fk_entry, fk_identity)
);

alter table o_lecture_participant_summary add constraint lec_part_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_lec_part_entry_idx on o_lecture_participant_summary(fk_entry);
alter table o_lecture_participant_summary add constraint lec_part_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_lec_part_ident_idx on o_lecture_participant_summary(fk_identity);


create table o_lecture_entry_config (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  l_lecture_enabled number default null,
  l_override_module_def number default 0 not null,
  l_rollcall_enabled number default null,
  l_calculate_attendance_rate number default null,
  l_required_attendance_rate float(24) default null,
  l_sync_calendar_teacher number default null,
  l_sync_calendar_participant number default null,
  l_sync_calendar_course number default null,
  fk_entry number(20) not null,
  unique(fk_entry),
  primary key (id)
);

alter table o_lecture_entry_config add constraint lec_entry_config_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);


create table o_lecture_block_audit_log (
  id number(20) generated always as identity,
  creationdate date not null,
  l_action varchar2(32 char),
  l_val_before CLOB,
  l_val_after CLOB,
  l_message CLOB,
  fk_lecture_block number(20),
  fk_roll_call number(20),
  fk_entry number(20),
  fk_identity number(20),
  fk_author number(20),
  primary key (id)
);

create index idx_lec_audit_entry_idx on o_lecture_block_audit_log(fk_entry);
create index idx_lec_audit_ident_idx on o_lecture_block_audit_log(fk_identity);


alter table o_rem_reminder add r_email_subject varchar2(255 char);
update o_rem_reminder set r_email_subject=r_description;


alter table o_qti_assessment_marks add q_hidden_rubrics clob default null;


alter table o_gta_task add g_submission_date date default null;
alter table o_gta_task add g_submission_revisions_date date default null;
alter table o_gta_task add g_collection_date date default null;

alter table o_gta_task add g_assignment_due_date date default null;
alter table o_gta_task add g_submission_due_date date default null;
alter table o_gta_task add g_revisions_due_date date default null;
alter table o_gta_task add g_solution_due_date date default null;

alter table o_gta_task add g_acceptation_date date default null;
alter table o_gta_task add g_solution_date date default null;
alter table o_gta_task add g_graduation_date date default null;

alter table o_gta_task add g_submission_ndocs number(20) default null;
alter table o_gta_task add g_submission_revisions_ndocs number(20) default null;
alter table o_gta_task add g_collection_ndocs number(20) default null;

create table o_gta_task_revision_date (
  id number(20) not null,
  creationdate date not null,
  g_status varchar2(36 char) not null,
  g_rev_loop number(20) not null,
  g_date date not null,
  fk_task number(20) not null,
  primary key (id)
);

alter table o_gta_task_revision_date add constraint gtaskrev_to_task_idx foreign key (fk_task) references o_gta_task (id);
create index idx_gtaskrev_to_task_idx on o_gta_task_revision_date (fk_task);

alter table o_gta_task add g_allow_reset_date date default null;
alter table o_gta_task add fk_allow_reset_identity number(20) default null;

alter table o_gta_task add constraint gtaskreset_to_allower_idx foreign key (fk_allow_reset_identity) references o_bs_identity (id);
create index idx_gtaskreset_to_allower_idx on o_gta_task (fk_allow_reset_identity);


alter table alter table o_info_message add attachmentpath varchar(1024) default null;


alter table o_as_entry add lastcoachmodified date default null;
alter table o_as_entry add lastusermodified date default null;

alter table o_as_eff_statement add column lastcoachmodified date default null;
alter table o_as_eff_statement add column lastusermodified date default null;




