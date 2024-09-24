-- Lecture
alter table o_lecture_block add l_external_ref varchar(128);

alter table o_lecture_block modify (fk_entry null);
alter table o_lecture_block add fk_curriculum_element number(20);

alter table o_lecture_block add constraint lec_block_curelem_idx foreign key (fk_curriculum_element) references o_cur_curriculum_element(id);
create index idx_lec_block_curelem_idx on o_lecture_block(fk_curriculum_element);

alter table o_lecture_block_audit_log add fk_curriculum_element number(20);

-- Curriculum
alter table o_cur_element_type add c_single_element number default 0 not null;
alter table o_cur_element_type add c_max_repo_entries number(20) default -1 not null;
alter table o_cur_element_type add c_allow_as_root number default 1 not null;
