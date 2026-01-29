-- Curriculum
alter table o_cur_curriculum_element add column fk_implementation bigint;

alter table o_cur_curriculum_element add constraint cur_el_to_impl_el_idx foreign key (fk_implementation) references o_cur_curriculum_element (id);

-- Certification program
alter table o_cer_program_log add column c_action varchar(64);
alter table o_cer_program_log add column c_before mediumtext;
alter table o_cer_program_log add column c_before_status varchar(64);
alter table o_cer_program_log add column c_after mediumtext;
alter table o_cer_program_log add column c_after_status varchar(64);

alter table o_cer_program_log add column fk_doer bigint;
alter table o_cer_program_log add column fk_identity bigint;
alter table o_cer_program_log add column fk_program bigint;
alter table o_cer_program_log add column fk_element bigint;

alter table o_cer_program_log modify column fk_certificate bigint;

alter table o_cer_program_log add constraint cer_log_to_doer_idx foreign key (fk_doer) references o_bs_identity (id);

alter table o_cer_program_log add constraint cer_log_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);

alter table o_cer_program_log add constraint cer_log_to_prog_idx foreign key (fk_program) references o_cer_program (id);

alter table o_cer_program_log add constraint cer_log_to_cur_elem_idx foreign key (fk_element) references o_cur_curriculum_element (id);


