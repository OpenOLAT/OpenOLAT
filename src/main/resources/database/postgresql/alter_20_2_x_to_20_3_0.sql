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
