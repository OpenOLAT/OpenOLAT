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


