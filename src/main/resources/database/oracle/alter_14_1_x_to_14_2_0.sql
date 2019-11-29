create table o_gta_task_revision (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_status varchar(36) not null,
   g_rev_loop number(20) default 0 not null,
   g_date timestamp,
   g_rev_comment CLOB,
   g_rev_comment_lastmodified timestamp,
   fk_task number(20) not null,
   fk_comment_author number(20),
   primary key (id)
);

alter table o_gta_task_revision add constraint task_rev_to_task_idx foreign key (fk_task) references o_gta_task (id);
create index idx_task_rev_to_task_idx on o_gta_task_revision (fk_task);
alter table o_gta_task_revision add constraint task_rev_to_ident_idx foreign key (fk_comment_author) references o_bs_identity (id);
create index idx_task_rev_to_ident_idx on o_gta_task_revision (fk_comment_author);