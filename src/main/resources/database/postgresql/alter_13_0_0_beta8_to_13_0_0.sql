-- curriculum
create table o_as_mode_course_to_cur_el (
   id bigserial,
   fk_assessment_mode_id int8 not null,
   fk_cur_element_id int8 not null,
   primary key (id)
);

alter table o_as_mode_course_to_cur_el add constraint as_modetocur_el_idx foreign key (fk_cur_element_id) references o_cur_curriculum_element (id);
alter table o_as_mode_course_to_cur_el add constraint as_modetocur_mode_idx foreign key (fk_assessment_mode_id) references o_as_mode_course (id);
create index idx_as_modetocur_el_idx on o_as_mode_course_to_cur_el (fk_cur_element_id);
create index idx_as_modetocur_mode_idx on o_as_mode_course_to_cur_el (fk_assessment_mode_id);

-- quality management
create table o_qual_report_access (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  q_type varchar(64),
  q_role varchar(64),
  q_online bool default false,
  q_email_trigger varchar(64),
  fk_data_collection bigint,
  fk_generator bigint,
  primary key (id)
);

alter table o_qual_report_access add constraint qual_repacc_to_dc_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
create index o_qual_report_access_dc_idx on o_qual_report_access(fk_data_collection);
alter table o_qual_report_access add constraint qual_repacc_to_generator_idx foreign key (fk_generator) references o_qual_generator (id);
create index o_qual_report_access_gen_idx on o_qual_report_access(fk_generator);
