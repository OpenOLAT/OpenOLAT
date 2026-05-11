insert into o_property (id, "version", lastmodified, creationdate, "identity", grp, resourcetypename, resourcetypeid, category, "name", floatvalue, longvalue, stringvalue, textvalue) values (1, 0, now(), now(), NULL, NULL, NULL, NULL, '_o3_', 'recruitingtool', NULL, NULL, 'true', NULL);

alter table fx_r_position rename to o_selectus_position;

alter table fx_r_pos_attribute_def rename to o_selectus_pos_attribute_def;
alter table fx_r_application rename to o_selectus_application;
alter table fx_r_app_attribute rename to o_selectus_app_attribute;
alter table fx_r_attachment rename to o_selectus_attachment;
alter table fx_r_committee_report  rename to o_selectus_committee_report;
alter table fx_r_position_policy rename to o_selectus_position_policy;
alter table fx_r_mail_template rename to o_selectus_mail_template;
alter table fx_r_application_notes rename to o_selectus_application_notes;
alter table fx_r_public_feedback rename to o_selectus_public_feedback;
alter table fx_r_apps_feedback rename to o_selectus_apps_feedback;
alter table fx_r_app_feedback rename to o_selectus_app_feedback;
alter table fx_r_rejection_email_log rename to o_selectus_rejection_email_log;
alter table fx_r_reference rename to o_selectus_reference;
alter table fx_r_reference_comment rename to o_selectus_reference_comment;
alter table fx_r_reference_to_app rename to o_selectus_reference_to_app;
alter table fx_r_decision_rubric_def rename to o_selectus_decision_rubric_def;
alter table fx_r_decision_rubric rename to o_selectus_decision_rubric;
alter table fx_r_review_position_def rename to o_selectus_review_position_def;

alter table fx_r_review_element_def rename to o_selectus_review_element_def;
alter table fx_r_review_response rename to o_selectus_review_response;
alter table fx_r_app_comment rename to o_selectus_app_comment;
alter table fx_r_app_comment_vote rename to o_selectus_app_comment_vote;
alter table fx_r_category rename to o_selectus_category;
alter table fx_r_app_category rename to o_selectus_app_category;
alter table fx_r_assignment rename to o_selectus_assignment;
alter table fx_r_org_unit rename to o_selectus_org_unit;
alter table fx_r_org_unit_member rename to o_selectus_org_unit_member;
alter table fx_r_audit_log rename to o_selectus_audit_log;
alter table fx_r_audit_log_read rename to o_selectus_audit_log_read;
alter table fx_r_audit_log_user_settings rename to o_selectus_audit_log_usettings;
alter table fx_r_audit_log_user_notifs rename to o_selectus_audit_log_u_notifs;
