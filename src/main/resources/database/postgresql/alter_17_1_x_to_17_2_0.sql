alter table o_repositoryentry add column canindexmetadata bool default false not null;
alter table o_lic_license_type add column l_type_oer bool default false not null;


-- video task
create table o_vid_task_session (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   v_author_mode bool default false,
   v_finish_time timestamp,
   v_score decimal default null,
   v_max_score decimal default null,
   v_passed bool default null,
   v_attempt int8 default 1 not null,
   v_cancelled bool default false,
   fk_reference_entry int8 not null,
   fk_entry int8,
   v_subident varchar(255),
   fk_identity int8 default null,
   v_anon_identifier varchar(128) default null,
   fk_assessment_entry int8 not null,
   primary key (id)
);

alter table o_vid_task_session add constraint vid_sess_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_vid_sess_to_repo_entry_idx on o_vid_task_session (fk_entry);
alter table o_vid_task_session add constraint vid_sess_to_vid_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
create index idx_vid_sess_to_vid_entry_idx on o_vid_task_session (fk_reference_entry);
alter table o_vid_task_session add constraint vid_sess_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_vid_sess_to_identity_idx on o_vid_task_session (fk_identity);
alter table o_vid_task_session add constraint vid_sess_to_as_entry_idx foreign key (fk_assessment_entry) references o_as_entry (id);
create index idx_vid_sess_to_as_entry_idx on o_vid_task_session (fk_assessment_entry);


create table o_vid_task_selection (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   v_segment_id varchar(64),
   v_category_id varchar(64),
   v_correct bool default false,
   v_time int8 not null,
   v_raw_time varchar(255),
   fk_task_session int8 not null,
   primary key (id)
);

alter table o_vid_task_selection add constraint vid_sel_to_session_idx foreign key (fk_task_session) references o_vid_task_session (id);
create index idx_vid_sel_to_session_idx on o_vid_task_selection (fk_task_session);








