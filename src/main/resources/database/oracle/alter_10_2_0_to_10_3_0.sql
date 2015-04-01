create table o_gta_task_list (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   g_course_node_ident varchar2(36 char),
   g_roundrobin varchar2(4000 char),
   fk_entry number(20) not null,
   primary key (id)
);

create table o_gta_task (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   g_status varchar2(36 char),
   g_rev_loop number(20) default 0 not null,
   g_taskname varchar2(36 char),
   fk_tasklist number(20) not null,
   fk_identity number(20),
   fk_businessgroup number(20),
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


