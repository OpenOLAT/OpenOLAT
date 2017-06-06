alter table o_qti_assessmentitem_session add column q_coach_comment mediumtext;

alter table o_as_entry add column a_num_assessment_docs bigint not null default 0;
