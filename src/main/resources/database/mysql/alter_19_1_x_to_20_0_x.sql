alter table o_lecture_block modify column fk_entry bigint null;
alter table o_lecture_block add column fk_curriculum_element bigint;

alter table o_lecture_block add constraint lec_block_curelem_idx foreign key (fk_curriculum_element) references o_cur_curriculum_element(id);

alter table o_lecture_block_audit_log add column fk_curriculum_element bigint;
