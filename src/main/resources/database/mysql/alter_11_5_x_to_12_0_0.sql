-- webfeed
create table o_feed (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
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
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   f_title varchar(1024),
   f_description mediumtext,
   f_content mediumtext,
   f_author varchar(255),
   f_guid varchar(255),
   f_external_link varchar(1024),
   f_draft boolean,
   f_publish_date datetime,
   f_width bigint,
   f_height bigint,
   f_filename varchar(1024),
   f_type varchar(255),
   f_length bigint,
   f_external_url varchar(1024),
   fk_feed_id bigint not null,
   fk_identity_author_id bigint,
   fk_identity_modified_id bigint,
   primary key (id)
);

alter table o_feed ENGINE = InnoDB;
alter table o_feed_item ENGINE = InnoDB;

create index idx_feed_resourceable_idx on o_feed (f_resourceable_id, f_resourceable_type);
alter table o_feed_item add constraint item_to_feed_fk foreign key(fk_feed_id) references o_feed(id);
create index idx_item_feed_idx on o_feed_item(fk_feed_id);
alter table o_feed_item add constraint feed_item_to_ident_author_fk foreign key (fk_identity_author_id) references o_bs_identity (id);
create index idx_item_ident_author_idx on o_feed_item(fk_identity_author_id);
alter table o_feed_item add constraint feed_item_to_ident_modified_fk foreign key (fk_identity_modified_id) references o_bs_identity (id);
create index idx_item_ident_modified_idx on o_feed_item(fk_identity_modified_id);



create table o_lecture_reason (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  l_title varchar(255),
  l_descr varchar(2000),
  primary key (id)
);
alter table o_lecture_reason ENGINE = InnoDB;


create table o_lecture_block (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  l_external_id varchar(255),
  l_managed_flags varchar(255),
  l_title varchar(255),
  l_descr mediumtext,
  l_preparation mediumtext,
  l_location varchar(255),
  l_comment mediumtext,
  l_start_date datetime not null,
  l_end_date datetime not null,
  l_compulsory bit default 1,
  l_eff_end_date datetime,
  l_planned_lectures_num bigint not null default 0,
  l_effective_lectures_num bigint not null default 0,
  l_effective_lectures varchar(128),
  l_auto_close_date datetime default null,
  l_status varchar(16) not null,
  l_roll_call_status varchar(16) not null,
  fk_reason bigint,
  fk_entry bigint not null,
  fk_teacher_group bigint not null,
  primary key (id)
);
alter table o_lecture_block ENGINE = InnoDB;

alter table o_lecture_block add constraint lec_block_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_lecture_block add constraint lec_block_gcoach_idx foreign key (fk_teacher_group) references o_bs_group (id);
alter table o_lecture_block add constraint lec_block_reason_idx foreign key (fk_reason) references o_lecture_reason (id);


create table o_lecture_block_to_group (
  id bigint not null auto_increment,
  fk_lecture_block bigint not null,
  fk_group bigint not null,
  primary key (id)
);
alter table o_lecture_block_to_group ENGINE = InnoDB;

alter table o_lecture_block_to_group add constraint lec_block_to_block_idx foreign key (fk_group) references o_bs_group (id);
alter table o_lecture_block_to_group add constraint lec_block_to_group_idx foreign key (fk_lecture_block) references o_lecture_block (id);


create table o_lecture_block_roll_call (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  l_comment mediumtext,
  l_lectures_attended varchar(128),
  l_lectures_absent varchar(128),
  l_lectures_attended_num bigint not null default 0,
  l_lectures_absent_num bigint not null default 0,
  l_absence_reason mediumtext,
  l_absence_authorized bit default null,
  l_absence_appeal_date datetime,
  fk_lecture_block bigint not null,
  fk_identity bigint not null,
  primary key (id)
);
alter table o_lecture_block_roll_call ENGINE = InnoDB;

alter table o_lecture_block_roll_call add constraint lec_call_block_idx foreign key (fk_lecture_block) references o_lecture_block (id);
alter table o_lecture_block_roll_call add constraint lec_call_identity_idx foreign key (fk_identity) references o_bs_identity (id);


create table o_lecture_reminder (
  id bigint not null auto_increment,
  creationdate datetime not null,
  l_status varchar(16) not null,
  fk_lecture_block bigint not null,
  fk_identity bigint not null,
  primary key (id)
);
alter table o_lecture_reminder ENGINE = InnoDB;

alter table o_lecture_reminder add constraint lec_reminder_block_idx foreign key (fk_lecture_block) references o_lecture_block (id);
alter table o_lecture_reminder add constraint lec_reminder_identity_idx foreign key (fk_identity) references o_bs_identity (id);


create table o_lecture_participant_summary (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  l_first_admission_date datetime default null,
  l_required_attendance_rate float(65,30) default null,
  l_attended_lectures bigint not null default 0,
  l_absent_lectures bigint not null default 0,
  l_excused_lectures bigint not null default 0,
  l_planneds_lectures bigint not null default 0,
  l_attendance_rate float(65,30) default null,
  l_cal_sync bit default 0,
  l_cal_last_sync_date datetime default null,
  fk_entry bigint not null,
  fk_identity bigint not null,
  primary key (id),
  unique (fk_entry, fk_identity)
);
alter table o_lecture_participant_summary ENGINE = InnoDB;

alter table o_lecture_participant_summary add constraint lec_part_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_lecture_participant_summary add constraint lec_part_ident_idx foreign key (fk_identity) references o_bs_identity (id);


create table o_lecture_entry_config (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  l_lecture_enabled bit default null,
  l_override_module_def bit default 0,
  l_rollcall_enabled bit default null,
  l_calculate_attendance_rate bit default null,
  l_required_attendance_rate float(65,30) default null,
  l_sync_calendar_teacher bit default null,
  l_sync_calendar_participant bit default null,
  l_sync_calendar_course bit default null,
  fk_entry bigint not null,
  unique(fk_entry),
  primary key (id)
);
alter table o_lecture_entry_config ENGINE = InnoDB;

alter table o_lecture_entry_config add constraint lec_entry_config_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);


create table o_lecture_block_audit_log (
  id bigint not null auto_increment,
  creationdate datetime not null,
  l_action varchar(32),
  l_val_before mediumtext,
  l_val_after mediumtext,
  l_message mediumtext,
  fk_lecture_block bigint,
  fk_roll_call bigint,
  fk_entry bigint,
  fk_identity bigint,
  fk_author bigint,
  primary key (id)
);
alter table o_lecture_block_audit_log ENGINE = InnoDB;

create index idx_lec_audit_entry_idx on o_lecture_block_audit_log(fk_entry);
create index idx_lec_audit_ident_idx on o_lecture_block_audit_log(fk_identity);


alter table o_rem_reminder add column r_email_subject varchar(255);
update o_rem_reminder set r_email_subject=r_description;


alter table o_qti_assessment_marks add column q_hidden_rubrics mediumtext default null;


alter table o_gta_task add column g_submission_date datetime default null;
alter table o_gta_task add column g_submission_revisions_date datetime default null;
alter table o_gta_task add column g_collection_date datetime default null;

alter table o_gta_task add column g_assignment_due_date datetime default null;
alter table o_gta_task add column g_submission_due_date datetime default null;
alter table o_gta_task add column g_revisions_due_date datetime default null;
alter table o_gta_task add column g_solution_due_date datetime default null;

alter table o_gta_task add column g_acceptation_date datetime default null;
alter table o_gta_task add column g_solution_date datetime default null;
alter table o_gta_task add column g_graduation_date datetime default null;

alter table o_gta_task add column g_submission_ndocs bigint default null;
alter table o_gta_task add column g_submission_revisions_ndocs bigint default null;
alter table o_gta_task add column g_collection_ndocs bigint default null;

create table o_gta_task_revision_date (
  id bigint not null auto_increment,
  creationdate datetime not null,
  g_status varchar(36) not null,
  g_rev_loop bigint not null,
  g_date datetime not null,
  fk_task bigint not null,
  primary key (id)
);
alter table o_gta_task_revision_date ENGINE = InnoDB;

alter table o_gta_task_revision_date add constraint gtaskrev_to_task_idx foreign key (fk_task) references o_gta_task (id);

alter table o_gta_task add column g_allow_reset_date datetime default null;
alter table o_gta_task add column fk_allow_reset_identity bigint default null;

alter table o_gta_task add constraint gtaskreset_to_allower_idx foreign key (fk_allow_reset_identity) references o_bs_identity (id);


alter table o_info_message add column attachmentpath varchar(1024) default null;


alter table o_as_entry add column lastcoachmodified datetime default null;
alter table o_as_entry add column lastusermodified datetime default null;

alter table o_as_eff_statement add column lastcoachmodified datetime default null;
alter table o_as_eff_statement add column lastusermodified datetime default null;


