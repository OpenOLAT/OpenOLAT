alter table o_as_entry add column a_current_run_completion float(65,30);
alter table o_as_entry add column a_current_run_status varchar(16);



alter table o_qti_assessmenttest_session add column q_num_questions bigint;
alter table o_qti_assessmenttest_session add column q_num_answered_questions bigint;
alter table o_qti_assessmenttest_session add column q_extra_time bigint;