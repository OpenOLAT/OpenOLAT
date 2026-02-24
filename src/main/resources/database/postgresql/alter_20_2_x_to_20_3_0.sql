-- Curriculum
alter table o_cur_curriculum_element add column fk_implementation int8;

alter table o_cur_curriculum_element add constraint cur_el_to_impl_el_idx foreign key (fk_implementation) references o_cur_curriculum_element (id);
create index idx_cur_el_to_impl_el_idx on o_cur_curriculum_element (fk_implementation);

-- Certification program
alter table o_cer_program_log add column c_action varchar(64);
alter table o_cer_program_log add column c_before text;
alter table o_cer_program_log add column c_before_status varchar(64);
alter table o_cer_program_log add column c_after text;
alter table o_cer_program_log add column c_after_status varchar(64);

alter table o_cer_program_log add column fk_doer int8;
alter table o_cer_program_log add column fk_identity int8;
alter table o_cer_program_log add column fk_program int8;
alter table o_cer_program_log add column fk_element int8;

alter table o_cer_program_log alter column fk_certificate drop not null;

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
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_active bool not null default true,
   a_default bool not null default false,
   a_name varchar(255),
   a_safeexambrowserconfig_xml text,
   a_safeexambrowserconfig_plist text,
   a_safeexambrowserconfig_pkey varchar(255),
   primary key (id)
);

alter table o_as_mode_course add column fk_seb_template int8;
alter table o_as_mode_course add constraint as_mode_to_seb_template_idx foreign key (fk_seb_template) references o_as_seb_template (id);
create index idx_as_mode_to_seb_template_idx on o_as_mode_course (fk_seb_template);

alter table o_as_inspection_configuration add column fk_seb_template int8;
alter table o_as_inspection_configuration add constraint as_insp_to_seb_template_idx foreign key (fk_seb_template) references o_as_seb_template (id);
create index idx_as_insp_to_seb_template_idx on o_as_inspection_configuration (fk_seb_template);
