alter table o_qti_assessmentitem_session add q_coach_comment CLOB;

alter table o_as_entry add a_num_assessment_docs number(20) default 0 not null;