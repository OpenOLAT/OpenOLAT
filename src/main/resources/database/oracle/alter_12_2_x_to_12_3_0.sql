alter table o_as_entry add a_current_run_completion decimal;
alter table o_as_entry add a_current_run_status varchar2(16 char);


alter table o_qti_assessmenttest_session add q_num_questions number(20);
alter table o_qti_assessmenttest_session add q_num_answered_questions number(20);
alter table o_qti_assessmenttest_session add q_extra_time number(20);