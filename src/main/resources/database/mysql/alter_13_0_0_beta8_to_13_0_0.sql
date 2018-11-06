-- curriculum
create table o_as_mode_course_to_cur_el (
   id bigint not null auto_increment,
   fk_assessment_mode_id bigint not null,
   fk_cur_element_id bigint not null,
   primary key (id)
);
alter table o_as_mode_course_to_cur_el ENGINE = InnoDB;

alter table o_as_mode_course_to_cur_el add constraint as_modetocur_el_idx foreign key (fk_cur_element_id) references o_cur_curriculum_element (id);
alter table o_as_mode_course_to_cur_el add constraint as_modetocur_mode_idx foreign key (fk_assessment_mode_id) references o_as_mode_course (id);


-- quality management
create table o_qual_report_access (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  q_type varchar(64),
  q_role varchar(64),
  q_online bit default 0,
  q_email_trigger varchar(64),
  fk_data_collection bigint,
  fk_generator bigint,
  fk_group bigint,
  primary key (id)
);

alter table o_qual_report_access ENGINE = InnoDB;

alter table o_qual_report_access add constraint qual_repacc_to_dc_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
alter table o_qual_report_access add constraint qual_repacc_to_generator_idx foreign key (fk_generator) references o_qual_generator (id);

