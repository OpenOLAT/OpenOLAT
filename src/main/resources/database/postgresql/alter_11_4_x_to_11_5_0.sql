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
  l_title varchar(255),
  l_descr text,
  l_preparation text,
  l_location varchar(255),
  l_comment text, 
  l_log text,
  l_start_date timestamp not null,
  l_end_date timestamp not null,
  l_eff_end_date timestamp,
  l_planned_lectures_num int8 not null default 0,
  l_effective_lectures_num int8 not null default 0,
  l_effective_lectures varchar(128),
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
  l_log text,
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
  fk_entry int8 not null,
  unique(fk_entry),
  primary key (id)
);

alter table o_lecture_entry_config add constraint lec_entry_config_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_lec_entry_conf_entry_idx on o_lecture_entry_config(fk_entry);



