create table o_gta_task_revision (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_status varchar(36) not null,
   g_rev_loop mediumint not null default 0,
   g_date timestamp,
   g_rev_comment mediumtext,
   g_rev_comment_lastmodified datetime,
   fk_task bigint not null,
   fk_comment_author bigint,
   primary key (id)
);
alter table o_gta_task_revision ENGINE = InnoDB;

alter table o_gta_task_revision add constraint task_rev_to_task_idx foreign key (fk_task) references o_gta_task (id);
alter table o_gta_task_revision add constraint task_rev_to_ident_idx foreign key (fk_comment_author) references o_bs_identity (id);


-- notifications
alter table o_noti_sub add column subenabled bit default 1;