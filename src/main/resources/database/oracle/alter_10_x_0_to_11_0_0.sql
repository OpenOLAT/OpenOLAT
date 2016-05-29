create table o_as_entry (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   a_attemtps number(20) default null,
   a_score decimal default null,
   a_passed number default null,
   a_status varchar2(16 char) default null,
   a_details varchar2(1024 char) default null,
   a_fully_assessed number default null,
   a_assessment_id number(20) default null,
   a_completion float,
   a_comment clob,
   a_coach_comment clob,
   fk_entry number(20) not null,
   a_subident varchar2(64 char),
   fk_reference_entry number(20),
   fk_identity number(20) not null,
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



create table o_qti_assessmenttest_session (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   q_exploded number default 0 not null,
   q_author_mode number default 0 not null,
   q_finish_time date,
   q_termination_time date,
   q_duration number(20),
   q_score decimal default null,
   q_passed number default null,
   q_storage varchar2(1024 char),
   fk_reference_entry number(20) not null,
   fk_entry number(20),
   q_subident varchar2(64 char),
   fk_identity number(20) not null,
   fk_assessment_entry number(20) not null,
   primary key (id)
);

alter table o_qti_assessmenttest_session add constraint qti_sess_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_testess_to_repo_entry_idx on o_qti_assessmenttest_session (fk_entry);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_course_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
create index idx_qti_sess_to_centry_idx on o_qti_assessmenttest_session (fk_reference_entry);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_qti_sess_to_identity_idx on o_qti_assessmenttest_session (fk_identity);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_as_entry_idx foreign key (fk_assessment_entry) references o_as_entry (id);
create index idx_qti_sess_to_as_entry_idx on o_qti_assessmenttest_session (fk_assessment_entry);

create table o_qti_assessmentitem_session (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   q_itemidentifier varchar2(64 char) not null,
   q_sectionidentifier varchar2(64 char) default null,
   q_testpartidentifier varchar2(64 char) default null,
   q_duration number(20),
   q_score decimal default null,
   q_passed number default null,
   q_storage varchar2(1024 char),
   fk_assessmenttest_session number(20) not null,
   primary key (id)
);

alter table o_qti_assessmentitem_session add constraint qti_itemsess_to_testsess_idx foreign key (fk_assessmenttest_session) references o_qti_assessmenttest_session (id);
create index idx_itemsess_to_testsess_idx on o_qti_assessmentitem_session (fk_assessmenttest_session);
create index idx_item_identifier_idx on o_qti_assessmentitem_session (q_itemidentifier);

create table o_qti_assessment_response (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   q_responseidentifier varchar2(64 char) not null,
   q_responsedatatype varchar2(16 char) not null,
   q_responselegality varchar2(16 char) not null,
   q_stringuifiedresponse clob,
   fk_assessmentitem_session number(20) not null,
   fk_assessmenttest_session number(20) not null,
   primary key (id)
);

alter table o_qti_assessment_response add constraint qti_resp_to_testsession_idx foreign key (fk_assessmenttest_session) references o_qti_assessmenttest_session (id);
create index idx_resp_to_testsession_idx on o_qti_assessment_response (fk_assessmenttest_session);
alter table o_qti_assessment_response add constraint qti_resp_to_itemsession_idx foreign key (fk_assessmentitem_session) references o_qti_assessmentitem_session (id);
create index idx_resp_to_itemsession_idx on o_qti_assessment_response (fk_assessmentitem_session);
create index idx_response_identifier_idx on o_qti_assessment_response (q_responseidentifier);

create table o_qti_assessment_marks (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   q_marks clob default null,
   fk_reference_entry number(20) not null,
   fk_entry number(20),
   q_subident varchar2(64 char),
   fk_identity number(20) not null,
   primary key (id)
);

alter table o_qti_assessment_marks add constraint qti_marks_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_marks_to_repo_entry_idx on o_qti_assessment_marks (fk_entry);
alter table o_qti_assessment_marks add constraint qti_marks_to_course_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
create index idx_qti_marks_to_centry_idx on o_qti_assessment_marks (fk_reference_entry);
alter table o_qti_assessment_marks add constraint qti_marks_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_qti_marks_to_identity_idx on o_qti_assessment_marks (fk_identity);




