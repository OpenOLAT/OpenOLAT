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


