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

-- livestream
create table o_livestream_launch (
   id bigint not null auto_increment,
   creationdate datetime not null,
   l_launch_date datetime not null,
   fk_entry bigint not null,
   l_subident varchar(128) not null,
   fk_identity bigint not null,
   primary key (id)
);
alter table o_livestream_launch ENGINE = InnoDB;
create index idx_livestream_viewers_idx on o_livestream_launch(l_subident, l_launch_date, fk_entry, fk_identity);


-- notifications
alter table o_noti_sub add column subenabled bit default 1;


-- index
create index mark_all_idx on o_mark(resname,resid,creator_id);
create index idx_eff_stat_course_ident_idx on o_as_eff_statement (fk_identity,course_repo_key);

-- question pool
alter table o_qp_item add column q_correction_time bigint default null;

