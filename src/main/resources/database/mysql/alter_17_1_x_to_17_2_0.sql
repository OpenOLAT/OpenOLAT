alter table o_repositoryentry add column canindexmetadata bool default false not null;
alter table o_lic_license_type add column l_type_oer bool default false not null;

-- video task
create table o_vid_task_session (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   v_author_mode bool default false,
   v_finish_time datetime,
   v_score float(65,30) default null,
   v_max_score float(65,30) default null,
   v_passed bool default null,
   v_result float(65,30) default null,
   v_segments bigint default 0 not null,
   v_attempt bigint default 1 not null,
   v_cancelled bool default false,
   fk_reference_entry bigint not null,
   fk_entry bigint,
   v_subident varchar(255),
   fk_identity bigint default null,
   v_anon_identifier varchar(128) default null,
   fk_assessment_entry bigint not null,
   primary key (id)
);
alter table o_vid_task_session ENGINE = InnoDB;

alter table o_vid_task_session add constraint vid_sess_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_vid_task_session add constraint vid_sess_to_vid_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
alter table o_vid_task_session add constraint vid_sess_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_vid_task_session add constraint vid_sess_to_as_entry_idx foreign key (fk_assessment_entry) references o_as_entry (id);


create table o_vid_task_selection (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   v_segment_id varchar(64),
   v_category_id varchar(64),
   v_correct bool default false,
   v_time bigint not null,
   v_raw_time varchar(255),
   fk_task_session bigint not null,
   primary key (id)
);
alter table o_vid_task_selection ENGINE = InnoDB;

alter table o_vid_task_selection add constraint vid_sel_to_session_idx foreign key (fk_task_session) references o_vid_task_session (id);
