-- grading
create table o_grad_to_identity (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_status varchar(16) default 'activated' not null,
   fk_identity bigint not null,
   fk_entry bigint not null,
   primary key (id)
);
alter table o_grad_to_identity ENGINE = InnoDB;

alter table o_grad_to_identity add constraint grad_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_grad_to_identity add constraint grad_id_to_repo_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);


create table o_grad_assignment (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_status varchar(16) default 'unassigned' not null,
   g_assessment_date datetime,
   g_assignment_date datetime,
   g_reminder_1 datetime,
   g_reminder_2 datetime,
   g_deadline datetime,
   g_extended_deadline datetime,
   g_closed datetime,
   fk_reference_entry bigint not null,
   fk_assessment_entry bigint not null,
   fk_grader bigint,
   primary key (id)
);
alter table o_grad_assignment ENGINE = InnoDB;

alter table o_grad_assignment add constraint grad_assign_to_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
alter table o_grad_assignment add constraint grad_assign_to_assess_idx foreign key (fk_assessment_entry) references o_as_entry (id);
alter table o_grad_assignment add constraint grad_assign_to_grader_idx foreign key (fk_grader) references o_grad_to_identity (id);


create table o_grad_time_record (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_time int8 default 0 not null,
   fk_assignment bigint,
   fk_grader bigint not null,
   primary key (id)
);
alter table o_grad_time_record ENGINE = InnoDB;

alter table o_grad_time_record add constraint grad_time_to_assign_idx foreign key (fk_assignment) references o_grad_assignment (id);
alter table o_grad_time_record add constraint grad_time_to_grader_idx foreign key (fk_grader) references o_grad_to_identity (id);


create table o_grad_configuration (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_grading_enabled bool not null default false,
   g_identity_visibility varchar(32) default 'anonymous' not null,
   g_grading_period bigint,
   g_notification_type varchar(32) default 'afterTestSubmission' not null,
   g_notification_subject varchar(255),
   g_notification_body mediumtext,
   g_first_reminder bigint,
   g_first_reminder_subject varchar(255),
   g_first_reminder_body mediumtext,
   g_second_reminder bigint,
   g_second_reminder_subject varchar(255),
   g_second_reminder_body mediumtext,
   fk_entry bigint not null,
   primary key (id)
);
alter table o_grad_configuration ENGINE = InnoDB;

alter table o_grad_configuration add constraint grad_config_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);

-- Catalog sorting
alter table o_catentry add column order_index bigint default 0;