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



create table o_qti_assessment_session (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_exploded bit not null default 0,
   q_author_mode bit not null default 0,
   q_finish_time datetime,
   q_termination_time datetime,
   q_score float(65,30) default null,
   q_passed bit default null, 
   q_storage varchar(32),
   fk_reference_entry bigint not null,
   fk_entry bigint,
   q_subident varchar(64),
   fk_identity bigint not null,
   fk_assessment_entry bigint not null,
   primary key (id)
);
alter table o_qti_assessment_session ENGINE = InnoDB;

alter table o_qti_assessment_session add constraint qti_sess_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_qti_assessment_session add constraint qti_sess_to_course_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
alter table o_qti_assessment_session add constraint qti_sess_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_qti_assessment_session add constraint qti_sess_to_as_entry_idx foreign key (fk_assessment_entry) references o_as_entry (id);
