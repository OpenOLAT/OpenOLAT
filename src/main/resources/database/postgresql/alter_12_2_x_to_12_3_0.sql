-- QPool
alter table o_qp_item add column q_topic varchar(1024);
alter table o_qp_item add column q_status_last_modified timestamp;

create index idx_tax_level_path_key_idx on o_tax_taxonomy_level (t_m_path_keys);



alter table o_as_entry add column a_current_run_completion float(24);
alter table o_as_entry add column a_current_run_status varchar(16);


alter table o_qti_assessmenttest_session add column q_num_questions int8;
alter table o_qti_assessmenttest_session add column q_num_answered_questions int8;
alter table o_qti_assessmenttest_session add column q_extra_time int8;