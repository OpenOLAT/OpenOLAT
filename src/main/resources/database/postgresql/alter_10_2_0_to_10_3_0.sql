create table o_rem_reminder (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   r_description varchar(255),
   r_start timestamp,
   r_sendtime varchar(16),
   r_configuration text,
   r_email_body text,
   fk_creator bigint not null,
   fk_entry int8 not null,
   primary key (id)
);

alter table o_rem_reminder add constraint rem_reminder_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_reminder_to_repo_entry_idx on o_rem_reminder (fk_entry);
alter table o_rem_reminder add constraint rem_reminder_to_creator_idx foreign key (fk_creator) references o_bs_identity (id);
create index idx_reminder_to_creator_idx on o_rem_reminder (fk_creator);

create table o_rem_sent_reminder (
   id int8 not null,
   creationdate timestamp not null,
   r_status varchar(16),
   fk_identity int8 not null,
   fk_reminder int8 not null,
   primary key (id)
);

alter table o_rem_sent_reminder add constraint rem_sent_rem_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_sent_rem_to_ident_idx on o_rem_sent_reminder (fk_identity);
alter table o_rem_sent_reminder add constraint rem_sent_rem_to_reminder_idx foreign key (fk_reminder) references o_rem_reminder (id);
create index idx_sent_rem_to_rem_idx on o_rem_sent_reminder (fk_reminder);


create table o_gta_task_list (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_course_node_ident varchar(36),
   g_roundrobin text,
   fk_entry int8 not null,
   primary key (id)
);

create table o_gta_task (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_status varchar(36),
   g_rev_loop int4 not null default 0,
   g_taskname varchar(36),
   fk_tasklist int8 not null,
   fk_identity int8,
   fk_businessgroup int8,
   primary key (id)
);


alter table o_gta_task add constraint gtask_to_tasklist_idx foreign key (fk_tasklist) references o_gta_task_list (id);
create index idx_gtask_to_tasklist_idx on o_gta_task (fk_tasklist);
alter table o_gta_task add constraint gtask_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_gtask_to_identity_idx on o_gta_task (fk_identity);
alter table o_gta_task add constraint gtask_to_bgroup_idx foreign key (fk_businessgroup) references o_gp_business (group_id);
create index idx_gtask_to_bgroup_idx on o_gta_task (fk_businessgroup);

alter table o_gta_task_list add constraint gta_list_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_gta_list_to_repo_entry_idx on o_gta_task_list (fk_entry);


-- add index to o_loggingtable
create index log_target_resid_idx on o_loggingtable(targetresid);
create index log_ptarget_resid_idx on o_loggingtable(parentresid);
create index log_gptarget_resid_idx on o_loggingtable(grandparentresid);
create index log_ggptarget_resid_idx on o_loggingtable(greatgrandparentresid);
create index log_creationdate_idx on o_loggingtable(creationdate);
