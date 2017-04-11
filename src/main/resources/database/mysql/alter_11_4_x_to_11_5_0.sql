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
  l_title varchar(255),
  l_descr mediumtext,
  l_preparation mediumtext,
  l_location varchar(255),
  l_comment mediumtext, 
  l_log mediumtext,
  l_start_date datetime not null,
  l_end_date datetime not null,
  l_eff_end_date datetime,
  l_planned_lectures_num bigint not null default 0,
  l_effective_lectures_num bigint not null default 0,
  l_effective_lectures varchar(128),
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
  l_log mediumtext,
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
  l_lecture_enabled bool default null,
  l_override_module_def bool default false,
  l_rollcall_enabled bool default null,
  l_calculate_attendance_rate bool default null,
  l_required_attendance_rate float(65,30) default null,
  l_sync_calendar_teacher bool default null,
  l_sync_calendar_participant bool default null,
  fk_entry bigint not null,
  unique(fk_entry),
  primary key (id)
);
alter table o_lecture_entry_config ENGINE = InnoDB;

alter table o_lecture_entry_config add constraint lec_entry_config_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);




