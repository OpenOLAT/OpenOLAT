create table o_qti_test_session (
   id int8 not null,
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
   q_course_subident varchar(36),
   primary key (id)
);

alter table o_qti_test_session add constraint qti_sess_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_testess_to_repo_entry_idx on o_qti_test_session (fk_entry);
alter table o_qti_test_session add constraint qti_sess_to_course_entry_idx foreign key (fk_course) references o_repositoryentry (repositoryentry_id);
create index idx_qti_sess_to_course_entry_idx on o_qti_test_session (fk_course);
alter table o_qti_test_session add constraint qti_sess_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_qti_sess_to_identity_idx on o_qti_test_session (fk_identity);