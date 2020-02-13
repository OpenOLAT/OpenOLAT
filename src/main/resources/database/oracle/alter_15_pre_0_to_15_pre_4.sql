-- grading
create table o_grad_to_identity (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_status varchar(16) default 'activated' not null,
   fk_identity number(20) not null,
   fk_entry number(20) not null,
   primary key (id)
);

alter table o_grad_to_identity add constraint grad_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_grad_to_ident_idx on o_grad_to_identity (fk_identity);
alter table o_grad_to_identity add constraint grad_id_to_repo_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_grad_id_to_repo_idx on o_grad_to_identity (fk_entry);


create table o_grad_assignment (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_status varchar(16) default 'unassigned' not null,
   g_assessment_date timestamp,
   g_assignment_date timestamp,
   g_reminder_1 timestamp,
   g_reminder_2 timestamp,
   g_deadline timestamp,
   g_extended_deadline timestamp,
   g_closed timestamp,
   fk_reference_entry number(20) not null,
   fk_assessment_entry number(20) not null,
   fk_grader number(20),
   primary key (id)
);

alter table o_grad_assignment add constraint grad_assign_to_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
create index idx_grad_assign_to_entry_idx on o_grad_assignment (fk_reference_entry);
alter table o_grad_assignment add constraint grad_assign_to_assess_idx foreign key (fk_assessment_entry) references o_as_entry (id);
create index idx_grad_assign_to_assess_idx on o_grad_assignment (fk_assessment_entry);
alter table o_grad_assignment add constraint grad_assign_to_grader_idx foreign key (fk_grader) references o_grad_to_identity (id);
create index idx_grad_assign_to_grader_idx on o_grad_assignment (fk_grader);


create table o_grad_time_record (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_time number(20) default 0 not null,
   fk_assignment number(20),
   fk_grader number(20) not null,
   primary key (id)
);

alter table o_grad_time_record add constraint grad_time_to_assign_idx foreign key (fk_assignment) references o_grad_assignment (id);
create index idx_grad_time_to_assign_idx on o_grad_time_record (fk_assignment);
alter table o_grad_time_record add constraint grad_time_to_grader_idx foreign key (fk_grader) references o_grad_to_identity (id);
create index idx_grad_time_to_grader_idx on o_grad_time_record (fk_grader);


create table o_grad_configuration (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_grading_enabled number default 0 not null,
   g_identity_visibility varchar(32) default 'anonymous' not null,
   g_grading_period number(20),
   g_notification_type varchar(32) default 'afterTestSubmission' not null,
   g_notification_subject varchar(255),
   g_notification_body CLOB,
   g_first_reminder number(20),
   g_first_reminder_subject varchar(255),
   g_first_reminder_body CLOB,
   g_second_reminder number(20),
   g_second_reminder_subject varchar(255),
   g_second_reminder_body CLOB,
   fk_entry number(20) not null,
   primary key (id)
);

alter table o_grad_configuration add constraint grad_config_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_grad_config_to_entry_idx on o_grad_configuration (fk_entry);


