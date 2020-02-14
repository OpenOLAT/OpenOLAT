-- grading
create table o_grad_to_identity (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_status varchar(16) default 'activated' not null,
   fk_identity int8 not null,
   fk_entry int8 not null,
   primary key (id)
);

alter table o_grad_to_identity add constraint grad_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_grad_to_ident_idx on o_grad_to_identity (fk_identity);
alter table o_grad_to_identity add constraint grad_id_to_repo_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_grad_id_to_repo_idx on o_grad_to_identity (fk_entry);


create table o_grad_assignment (
   id bigserial,
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
   fk_reference_entry int8 not null,
   fk_assessment_entry int8 not null,
   fk_grader int8,
   primary key (id)
);

alter table o_grad_assignment add constraint grad_assign_to_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
create index idx_grad_assign_to_entry_idx on o_grad_assignment (fk_reference_entry);
alter table o_grad_assignment add constraint grad_assign_to_assess_idx foreign key (fk_assessment_entry) references o_as_entry (id);
create index idx_grad_assign_to_assess_idx on o_grad_assignment (fk_assessment_entry);
alter table o_grad_assignment add constraint grad_assign_to_grader_idx foreign key (fk_grader) references o_grad_to_identity (id);
create index idx_grad_assign_to_grader_idx on o_grad_assignment (fk_grader);


create table o_grad_time_record (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_time int8 default 0 not null,
   fk_assignment int8,
   fk_grader int8 not null,
   primary key (id)
);

alter table o_grad_time_record add constraint grad_time_to_assign_idx foreign key (fk_assignment) references o_grad_assignment (id);
create index idx_grad_time_to_assign_idx on o_grad_time_record (fk_assignment);
alter table o_grad_time_record add constraint grad_time_to_grader_idx foreign key (fk_grader) references o_grad_to_identity (id);
create index idx_grad_time_to_grader_idx on o_grad_time_record (fk_grader);


create table o_grad_configuration (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_grading_enabled bool not null default false,
   g_identity_visibility varchar(32) default 'anonymous' not null,
   g_grading_period int8,
   g_notification_type varchar(32) default 'afterTestSubmission' not null,
   g_notification_subject varchar(255),
   g_notification_body text,
   g_first_reminder int8,
   g_first_reminder_subject varchar(255),
   g_first_reminder_body text,
   g_second_reminder int8,
   g_second_reminder_subject varchar(255),
   g_second_reminder_body text,
   fk_entry int8 not null,
   primary key (id)
);

alter table o_grad_configuration add constraint grad_config_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_grad_config_to_entry_idx on o_grad_configuration (fk_entry);


-- absence leave
create table o_user_absence_leave (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   u_absent_from timestamp,
   u_absent_to timestamp,
   u_resname varchar(50),
   u_resid int8,
   u_sub_ident varchar(2048),
   fk_identity int8 not null,
   primary key (id)
);

alter table o_user_absence_leave add constraint abs_leave_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_abs_leave_to_ident_idx on o_user_absence_leave (fk_identity);





