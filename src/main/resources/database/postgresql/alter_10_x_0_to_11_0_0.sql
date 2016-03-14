create table o_as_entry (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_attemtps int8 default null,
   a_score decimal default null,
   a_passed bool default null,
   a_status varchar(16) default null,
   a_details varchar(1024) default null,
   a_fully_assessed bool default null,
   a_assessment_id int8 default null,
   a_completion float(24),
   a_comment text,
   a_coach_comment text,
   fk_entry int8 not null,
   a_subident varchar(64),
   fk_reference_entry int8,
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



create table o_qti_assessmenttest_session (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_exploded bool default false,
   q_author_mode bool default false,
   q_finish_time timestamp,
   q_termination_time timestamp,
   q_duration int8,
   q_score decimal default null,
   q_passed bool default null,
   q_storage varchar(1024),
   fk_reference_entry int8 not null,
   fk_entry int8,
   q_subident varchar(64),
   fk_identity int8 not null,
   fk_assessment_entry int8 not null,
   primary key (id)
);

alter table o_qti_assessmenttest_session add constraint qti_sess_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_testess_to_repo_entry_idx on o_qti_assessmenttest_session (fk_entry);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_course_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
create index idx_qti_sess_to_course_entry_idx on o_qti_assessmenttest_session (fk_reference_entry);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_qti_sess_to_identity_idx on o_qti_assessmenttest_session (fk_identity);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_as_entry_idx foreign key (fk_assessment_entry) references o_as_entry (id);
create index idx_qti_sess_to_as_entry_idx on o_qti_assessmenttest_session (fk_assessment_entry);

create table o_qti_assessmentitem_session (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_itemidentifier varchar(64) not null,
   q_duration int8,
   q_score decimal default null,
   q_passed bool default null,
   q_storage varchar(1024),
   fk_assessmenttest_session int8 not null,
   primary key (id)
);

alter table o_qti_assessmentitem_session add constraint qti_itemsess_to_testsess_idx foreign key (fk_assessmenttest_session) references o_qti_assessmenttest_session (id);
create index idx_itemsess_to_testsess_idx on o_qti_assessmentitem_session (fk_assessmenttest_session);
create index idx_item_identifier_idx on o_qti_assessmentitem_session (q_itemidentifier);

create table o_qti_assessment_response (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_responseidentifier varchar(64) not null,
   q_responsedatatype varchar(16) not null,
   q_responselegality varchar(16) not null,
   q_stringuifiedresponse text,
   fk_assessmentitem_session int8 not null,
   fk_assessmenttest_session int8 not null,
   primary key (id)
);

alter table o_qti_assessment_response add constraint qti_resp_to_testsession_idx foreign key (fk_assessmenttest_session) references o_qti_assessmenttest_session (id);
create index idx_resp_to_testsession_idx on o_qti_assessment_response (fk_assessmenttest_session);
alter table o_qti_assessment_response add constraint qti_resp_to_itemsession_idx foreign key (fk_assessmentitem_session) references o_qti_assessmentitem_session (id);
create index idx_resp_to_itemsession_idx on o_qti_assessment_response (fk_assessmentitem_session);
create index idx_response_identifier_idx on o_qti_assessment_response (q_responseidentifier);

create table o_qti_assessment_marks (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_marks text default null,
   fk_reference_entry int8 not null,
   fk_entry int8,
   q_subident varchar(64),
   fk_identity int8 not null,
   primary key (id)
);

alter table o_qti_assessment_marks add constraint qti_marks_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_marks_to_repo_entry_idx on o_qti_assessment_marks (fk_entry);
alter table o_qti_assessment_marks add constraint qti_marks_to_course_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
create index idx_qti_marks_to_course_entry_idx on o_qti_assessment_marks (fk_reference_entry);
alter table o_qti_assessment_marks add constraint qti_marks_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_qti_marks_to_identity_idx on o_qti_assessment_marks (fk_identity);




