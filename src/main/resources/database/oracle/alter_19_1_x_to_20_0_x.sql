alter table o_lecture_block modify (fk_entry null);
alter table o_lecture_block add fk_curriculum_element number(20);

alter table o_lecture_block add constraint lec_block_curelem_idx foreign key (fk_curriculum_element) references o_cur_curriculum_element(id);
create index idx_lec_block_curelem_idx on o_lecture_block(fk_curriculum_element);

alter table o_lecture_block_audit_log add fk_curriculum_element number(20);
