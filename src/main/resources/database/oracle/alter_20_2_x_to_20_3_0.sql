-- Curriculum
alter table o_cur_curriculum_element add fk_implementation number(20);

alter table o_cur_curriculum_element add constraint cur_el_to_impl_el_idx foreign key (fk_implementation) references o_cur_curriculum_element (id);
create index idx_cur_el_to_impl_el_idx on o_cur_curriculum_element (fk_implementation);

-- Certification program
alter table o_cer_program_log add c_action varchar(64);
alter table o_cer_program_log add c_before clob;
alter table o_cer_program_log add c_before_status varchar(64);
alter table o_cer_program_log add c_after clob;
alter table o_cer_program_log add c_after_status varchar(64);

alter table o_cer_program_log add fk_doer number(20);
alter table o_cer_program_log add fk_identity number(20);
alter table o_cer_program_log add fk_program number(20);
alter table o_cer_program_log add fk_element number(20);

alter table o_cer_program_log modify fk_certificate number(20) null;

alter table o_cer_program_log add constraint cer_log_to_doer_idx foreign key (fk_doer) references o_bs_identity (id);
create index idx_cer_log_to_doer_idx on o_cer_program_log (fk_doer);

alter table o_cer_program_log add constraint cer_log_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_cer_log_to_ident_idx on o_cer_program_log (fk_identity);

alter table o_cer_program_log add constraint cer_log_to_prog_idx foreign key (fk_program) references o_cer_program (id);
create index idx_cer_log_to_prog_idx on o_cer_program_log (fk_program);

alter table o_cer_program_log add constraint cer_log_to_cur_elem_idx foreign key (fk_element) references o_cur_curriculum_element (id);
create index idx_cer_log_to_cur_elem_idx on o_cer_program_log (fk_element);

-- Safe Exam Browser template
create table o_as_seb_template (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   a_active number default 1 not null,
   a_default number default 0 not null,
   a_name varchar2(255 char),
   a_safeexambrowserconfig_xml clob,
   a_safeexambrowserconfig_plist clob,
   a_safeexambrowserconfig_pkey varchar2(255 char),
   primary key (id)
);

alter table o_as_mode_course add fk_seb_template number(20);
alter table o_as_mode_course add constraint as_mode_to_seb_template_idx foreign key (fk_seb_template) references o_as_seb_template (id);
create index idx_as_mode_to_seb_template_idx on o_as_mode_course (fk_seb_template);

alter table o_as_inspection_configuration add fk_seb_template number(20);
alter table o_as_inspection_configuration add constraint as_insp_to_seb_template_idx foreign key (fk_seb_template) references o_as_seb_template (id);
create index idx_as_insp_to_seb_template_idx on o_as_inspection_configuration (fk_seb_template);
