-- QPool
alter table o_qp_item add column q_topic varchar2(1024);
alter table o_qp_item add column q_status_last_modified date;

create index idx_tax_level_path_key_idx on o_tax_taxonomy_level (t_m_path_keys);



alter table o_as_entry add a_current_run_completion decimal;
alter table o_as_entry add a_current_run_status varchar2(16 char);


alter table o_qti_assessmenttest_session add q_num_questions number(20);
alter table o_qti_assessmenttest_session add q_num_answered_questions number(20);
alter table o_qti_assessmenttest_session add q_extra_time number(20);