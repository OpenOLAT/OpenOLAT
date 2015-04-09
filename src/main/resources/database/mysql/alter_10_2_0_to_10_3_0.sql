create table o_rem_reminder (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   r_description varchar(255),
   r_configuration mediumtext,
   r_email_body mediumtext,
   fk_entry bigint not null,
   primary key (id)
);
alter table o_rem_reminder ENGINE = InnoDB;

alter table o_rem_reminder add constraint rem_reminder_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);

create table o_rem_sent_reminder (
   id bigint not null,
   creationdate datetime not null,
   r_status varchar(16),
   fk_identity bigint not null,
   fk_reminder bigint not null,
   primary key (id)
);
alter table o_rem_sent_reminder ENGINE = InnoDB;

alter table o_rem_sent_reminder add constraint rem_sent_rem_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_rem_sent_reminder add constraint rem_sent_rem_to_reminder_idx foreign key (fk_reminder) references o_rem_reminder (id);



create table o_gta_task_list (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_course_node_ident varchar(36),
   g_roundrobin mediumtext,
   fk_entry bigint not null,
   primary key (id)
);
alter table o_gta_task_list ENGINE = InnoDB;

create table o_gta_task (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_status varchar(36),
   g_rev_loop mediumint not null default 0,
   g_taskname varchar(36),
   fk_tasklist bigint not null,
   fk_identity bigint,
   fk_businessgroup bigint,
   primary key (id)
);
alter table o_gta_task ENGINE = InnoDB;


alter table o_gta_task add constraint gtask_to_tasklist_idx foreign key (fk_tasklist) references o_gta_task_list (id);
alter table o_gta_task add constraint gtask_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_gta_task add constraint gtask_to_bgroup_idx foreign key (fk_businessgroup) references o_gp_business (group_id);



alter table o_gta_task_list add constraint gta_list_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);

