create table o_qti_assessment_session (
   id NUMBER(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   q_exploded number default 0 not null,
   q_author_mode number default 0 not null,
   q_finish_time date,
   q_termination_time date,
   q_storage varchar2(32 char),
   fk_identity number(20) not null,
   fk_entry number(20) not null,
   fk_course number(20),
   q_course_subident varchar2(64 char),
   primary key (id)
);

alter table o_qti_assessment_session add constraint qti_sess_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_testess_to_repo_entry_idx on o_qti_assessment_session (fk_entry);
alter table o_qti_assessment_session add constraint qti_sess_to_course_entry_idx foreign key (fk_course) references o_repositoryentry (repositoryentry_id);
create index idx_qti_sess_to_course_entry_idx on o_qti_assessment_session (fk_course);
alter table o_qti_assessment_session add constraint qti_sess_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_qti_sess_to_identity_idx on o_qti_assessment_session (fk_identity);