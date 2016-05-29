create table o_as_entry (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   a_attemtps bigint default null,
   a_score float(65,30) default null,
   a_passed bit default null,
   a_status varchar(16) default null,
   a_details varchar(1024) default null,
   a_fully_assessed bit default null,
   a_assessment_id bigint default null,
   a_completion float(65,30),
   a_comment text,
   a_coach_comment text,
   fk_entry bigint not null,
   a_subident varchar(64),
   fk_reference_entry bigint,
   fk_identity bigint not null,
   primary key (id),
   unique (fk_identity, fk_entry, a_subident)
);
alter table o_as_entry ENGINE = InnoDB;

alter table o_as_entry add constraint as_entry_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_as_entry add constraint as_entry_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_as_entry add constraint as_entry_to_refentry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
create index idx_as_entry_to_id_idx on o_as_entry (a_assessment_id);

create table o_qti_assessmenttest_session (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_exploded bit not null default 0,
   q_author_mode bit not null default 0,
   q_finish_time datetime,
   q_termination_time datetime,
   q_duration bigint,
   q_score float(65,30) default null,
   q_passed bit default null, 
   q_storage varchar(1024),
   fk_reference_entry bigint not null,
   fk_entry bigint,
   q_subident varchar(64),
   fk_identity bigint not null,
   fk_assessment_entry bigint not null,
   primary key (id)
);
alter table o_qti_assessmenttest_session ENGINE = InnoDB;

alter table o_qti_assessmenttest_session add constraint qti_sess_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_course_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_as_entry_idx foreign key (fk_assessment_entry) references o_as_entry (id);

create table o_qti_assessmentitem_session (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_itemidentifier varchar(64) not null,
   q_sectionidentifier varchar(64) default null,
   q_testpartidentifier varchar(64) default null,
   q_duration bigint,
   q_score decimal default null,
   q_passed bit default null,
   q_storage varchar(1024),
   fk_assessmenttest_session bigint not null,
   primary key (id)
);
alter table o_qti_assessmentitem_session ENGINE = InnoDB;

alter table o_qti_assessmentitem_session add constraint qti_itemsess_to_testsess_idx foreign key (fk_assessmenttest_session) references o_qti_assessmenttest_session (id);
create index idx_item_identifier_idx on o_qti_assessmentitem_session (q_itemidentifier);

create table o_qti_assessment_response (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_responseidentifier varchar(64) not null,
   q_responsedatatype varchar(16) not null,
   q_responselegality varchar(16) not null,
   q_stringuifiedresponse mediumtext,
   fk_assessmentitem_session bigint not null,
   fk_assessmenttest_session bigint not null,
   primary key (id)
);
alter table o_qti_assessment_response ENGINE = InnoDB;

alter table o_qti_assessment_response add constraint qti_resp_to_testsession_idx foreign key (fk_assessmenttest_session) references o_qti_assessmenttest_session (id);
alter table o_qti_assessment_response add constraint qti_resp_to_itemsession_idx foreign key (fk_assessmentitem_session) references o_qti_assessmentitem_session (id);
create index idx_response_identifier_idx on o_qti_assessment_response (q_responseidentifier);

create table o_qti_assessment_marks (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_marks mediumtext default null,
   fk_reference_entry bigint not null,
   fk_entry bigint,
   q_subident varchar(64),
   fk_identity bigint not null,
   primary key (id)
);
alter table o_qti_assessment_marks ENGINE = InnoDB;

alter table o_qti_assessment_marks add constraint qti_marks_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_qti_assessment_marks add constraint qti_marks_to_course_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
alter table o_qti_assessment_marks add constraint qti_marks_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);


