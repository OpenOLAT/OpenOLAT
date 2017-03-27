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
  fk_identity int8,
  primary key (id)
);

alter table o_lecture_block_roll_call add constraint lec_call_block_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_lec_call_block_idx on o_lecture_block_roll_call(fk_lecture_block);
alter table o_lecture_block_roll_call add constraint lec_call_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_lec_call_identity_idx on o_lecture_block_roll_call(fk_identity);





