-- Lecture
alter table o_lecture_block add column l_external_ref varchar(128);

alter table o_lecture_block modify column fk_entry bigint null;
alter table o_lecture_block add column fk_curriculum_element bigint;

alter table o_lecture_block add constraint lec_block_curelem_idx foreign key (fk_curriculum_element) references o_cur_curriculum_element(id);

alter table o_lecture_block_audit_log add column fk_curriculum_element bigint;

-- Curriculum
alter table o_cur_element_type add column c_single_element bool default false not null;
alter table o_cur_element_type add column c_max_repo_entries bigint default -1 not null;
alter table o_cur_element_type add column c_allow_as_root bool default true not null;

