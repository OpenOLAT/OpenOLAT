create table o_qti_assessment_session (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_exploded bool default false,
   q_author_mode bool default false,
   q_finish_time timestamp,
   q_termination_time timestamp,
   q_storage varchar(32),
   fk_identity int8 not null,
   fk_entry int8 not null,
   fk_course int8,
   q_course_subident varchar(64),
   primary key (id)
);

alter table o_qti_assessment_session add constraint qti_sess_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_testess_to_repo_entry_idx on o_qti_assessment_session (fk_entry);
alter table o_qti_assessment_session add constraint qti_sess_to_course_entry_idx foreign key (fk_course) references o_repositoryentry (repositoryentry_id);
create index idx_qti_sess_to_course_entry_idx on o_qti_assessment_session (fk_course);
alter table o_qti_assessment_session add constraint qti_sess_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_qti_sess_to_identity_idx on o_qti_assessment_session (fk_identity);



create table o_as_entry (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_attemtps int8 default null,
   a_score decimal default null,
   a_passed bool default null,
   a_fully_assessed bool default null,
   a_assessment_id int8 default null,
   a_completion float(24),
   a_comment text,
   a_coach_comment text,
   fk_entry int8 not null,
   a_subident varchar(64),
   fk_reference_entry int8 not null,
   fk_identity int8 not null,
   primary key (id),
   unique(fk_identity, fk_entry, a_subident)
);

alter table o_as_entry add constraint as_entry_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_as_entry_to_ident_idx on o_as_entry (fk_identity);
alter table o_as_entry add constraint as_entry_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_as_entry_to_entry_idx on o_as_entry (fk_entry);
alter table o_as_entry add constraint as_entry_to_refentry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
create index idx_as_entry_to_refentry_idx on o_as_entry (fk_reference_entry);

create index idx_as_entry_to_id_idx on o_as_entry (a_assessment_id);


