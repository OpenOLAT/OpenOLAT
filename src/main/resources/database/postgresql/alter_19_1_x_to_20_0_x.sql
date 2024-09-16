alter table o_lecture_block alter column fk_entry drop not null;
alter table o_lecture_block add column fk_curriculum_element int8;

alter table o_lecture_block add constraint lec_block_curelem_idx foreign key (fk_curriculum_element) references o_cur_curriculum_element(id);
create index idx_lec_block_curelem_idx on o_lecture_block(fk_curriculum_element);

alter table o_lecture_block_audit_log add column fk_curriculum_element int8;
