create table o_as_entry (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   a_attemtps number(20) default null,
   a_score decimal default null,
   q_manual_score decimal default null,
   a_passed number default null,
   a_status varchar2(16 char) default null,
   a_details varchar2(1024 char) default null,
   a_fully_assessed number default null,
   a_assessment_id number(20) default null,
   a_completion float,
   a_comment clob,
   a_coach_comment clob,
   fk_entry number(20) not null,
   a_subident varchar2(64 char),
   fk_reference_entry number(20),
   fk_identity number(20) default null,
   a_anon_identifier varchar2(128 char) default null,
   primary key (id),
   unique(fk_identity, fk_entry, a_subident)
);

alter table o_as_entry add constraint as_entry_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_as_entry_to_ident_idx on o_as_entry (fk_identity);
alter table o_as_entry add constraint as_entry_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_as_entry_to_entry_idx on o_as_entry (fk_entry);
alter table o_as_entry add constraint as_entry_to_refentry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
create index idx_as_entry_to_refentry_idx on o_as_entry (fk_reference_entry);
create index idx_as_entry_to_id_idx on o_as_entry (a_assessment_id);



create table o_qti_assessmenttest_session (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   q_exploded number default 0 not null,
   q_author_mode number default 0 not null,
   q_finish_time date,
   q_termination_time date,
   q_duration number(20),
   q_score decimal default null,
   q_manual_score decimal default null,
   q_passed number default null,
   q_storage varchar2(1024 char),
   fk_reference_entry number(20) not null,
   fk_entry number(20),
   q_subident varchar2(64 char),
   fk_identity number(20) default null,
   q_anon_identifier varchar2(128 char) default null,
   fk_assessment_entry number(20) not null,
   primary key (id)
);

alter table o_qti_assessmenttest_session add constraint qti_sess_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_testess_to_repo_entry_idx on o_qti_assessmenttest_session (fk_entry);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_course_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
create index idx_qti_sess_to_centry_idx on o_qti_assessmenttest_session (fk_reference_entry);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_qti_sess_to_identity_idx on o_qti_assessmenttest_session (fk_identity);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_as_entry_idx foreign key (fk_assessment_entry) references o_as_entry (id);
create index idx_qti_sess_to_as_entry_idx on o_qti_assessmenttest_session (fk_assessment_entry);

create table o_qti_assessmentitem_session (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   q_itemidentifier varchar2(64 char) not null,
   q_sectionidentifier varchar2(64 char) default null,
   q_testpartidentifier varchar2(64 char) default null,
   q_duration number(20),
   q_score decimal default null,
   q_passed number default null,
   q_storage varchar2(1024 char),
   fk_assessmenttest_session number(20) not null,
   primary key (id)
);

alter table o_qti_assessmentitem_session add constraint qti_itemsess_to_testsess_idx foreign key (fk_assessmenttest_session) references o_qti_assessmenttest_session (id);
create index idx_itemsess_to_testsess_idx on o_qti_assessmentitem_session (fk_assessmenttest_session);
create index idx_item_identifier_idx on o_qti_assessmentitem_session (q_itemidentifier);

create table o_qti_assessment_response (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   q_responseidentifier varchar2(64 char) not null,
   q_responsedatatype varchar2(16 char) not null,
   q_responselegality varchar2(16 char) not null,
   q_stringuifiedresponse clob,
   fk_assessmentitem_session number(20) not null,
   fk_assessmenttest_session number(20) not null,
   primary key (id)
);

alter table o_qti_assessment_response add constraint qti_resp_to_testsession_idx foreign key (fk_assessmenttest_session) references o_qti_assessmenttest_session (id);
create index idx_resp_to_testsession_idx on o_qti_assessment_response (fk_assessmenttest_session);
alter table o_qti_assessment_response add constraint qti_resp_to_itemsession_idx foreign key (fk_assessmentitem_session) references o_qti_assessmentitem_session (id);
create index idx_resp_to_itemsession_idx on o_qti_assessment_response (fk_assessmentitem_session);
create index idx_response_identifier_idx on o_qti_assessment_response (q_responseidentifier);

create table o_qti_assessment_marks (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   q_marks clob default null,
   fk_reference_entry number(20) not null,
   fk_entry number(20),
   q_subident varchar2(64 char),
   fk_identity number(20) not null,
   primary key (id)
);

alter table o_qti_assessment_marks add constraint qti_marks_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_marks_to_repo_entry_idx on o_qti_assessment_marks (fk_entry);
alter table o_qti_assessment_marks add constraint qti_marks_to_course_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
create index idx_qti_marks_to_centry_idx on o_qti_assessment_marks (fk_reference_entry);
alter table o_qti_assessment_marks add constraint qti_marks_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_qti_marks_to_identity_idx on o_qti_assessment_marks (fk_identity);


-- portfolio
create table o_pf_binder (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   p_title varchar2(255 char),
   p_status varchar2(32 char),
   p_copy_date date,
   p_return_date date,
   p_deadline date,
   p_summary CLOB,
   p_image_path varchar2(255 char),
   fk_olatresource_id number(20),
   fk_group_id number(20) not null,
   fk_entry_id number(20),
   p_subident varchar2(128 char),
   fk_template_id number(20),
   primary key (id)
);

alter table o_pf_binder add constraint pf_binder_resource_idx foreign key (fk_olatresource_id) references o_olatresource (resource_id);
create index idx_pf_binder_resource_idx on o_pf_binder (fk_olatresource_id);
alter table o_pf_binder add constraint pf_binder_group_idx foreign key (fk_group_id) references o_bs_group (id);
create index idx_pf_binder_group_idx on o_pf_binder (fk_group_id);
alter table o_pf_binder add constraint pf_binder_course_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_pf_binder_course_idx on o_pf_binder (fk_entry_id);
alter table o_pf_binder add constraint pf_binder_template_idx foreign key (fk_template_id) references o_pf_binder (id);
create index idx_pf_binder_template_idx on o_pf_binder (fk_template_id);

create table o_pf_section (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   pos number(20) default null,
   p_title varchar2(255 char),
   p_description varchar2(4000 char),
   p_status varchar2(32 char) default 'notStarted' not null,
   p_begin date,
   p_end date,
   p_override_begin_end number default 0,
   fk_group_id number(20) not null,
   fk_binder_id number(20) not null,
   fk_template_reference_id number(20),
   primary key (id)
);

alter table o_pf_section add constraint pf_section_group_idx foreign key (fk_group_id) references o_bs_group (id);
create index idx_pf_section_group_idx on o_pf_section (fk_group_id);
alter table o_pf_section add constraint pf_section_binder_idx foreign key (fk_binder_id) references o_pf_binder (id);
create index idx_pf_section_binder_idx on o_pf_section (fk_binder_id);
alter table o_pf_section add constraint pf_section_template_idx foreign key (fk_template_reference_id) references o_pf_section (id);
create index idx_pf_section_template_idx on o_pf_section (fk_template_reference_id);

create table o_pf_page (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   pos number(20) default null,
   p_title varchar2(255 char),
   p_summary varchar2(4000 char),
   p_status varchar2(32 char),
   p_image_path varchar2(255 char),
   p_image_align varchar2(32 char),
   p_version number(20) default 0,
   p_initial_publish_date date,
   p_last_publish_date date,
   fk_body_id number(20) not null,
   fk_group_id number(20) not null,
   fk_section_id number(20),
   primary key (id)
);

alter table o_pf_page add constraint pf_page_group_idx foreign key (fk_group_id) references o_bs_group (id);
create index idx_pf_page_group_idx on o_pf_page (fk_group_id);
alter table o_pf_page add constraint pf_page_section_idx foreign key (fk_section_id) references o_pf_section (id);
create index idx_pf_page_section_idx on o_pf_page (fk_section_id);

create table o_pf_media (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   p_collection_date date not null,
   p_type varchar2(64 char) not null,
   p_storage_path varchar2(255 char),
   p_root_filename varchar2(255 char),
   p_title varchar(255) not null,
   p_description varchar2(4000 char),
   p_content CLOB,
   p_signature number(20) default 0 not null,
   p_reference_id varchar2(255 char) default null,
   p_business_path varchar2(255 char) not null,
   p_creators varchar2(1024 char) default null,
   p_place varchar2(255 char) default null,
   p_publisher varchar2(255 char) default null,
   p_publication_date date default null,
   p_date varchar2(32 char) default null,
   p_url varchar2(1024 char) default null,
   p_source varchar2(1024 char) default null,
   p_language varchar2(32 char) default null,
   p_metadata_xml varchar2(4000 char),
   fk_author_id number(20) not null,
   primary key (id)
);

alter table o_pf_media add constraint pf_media_author_idx foreign key (fk_author_id) references o_bs_identity (id);
create index idx_pf_media_author_idx on o_pf_media (fk_author_id);
create index idx_media_storage_path_idx on o_pf_media (p_business_path);

create table o_pf_page_body (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   primary key (id)
);

alter table o_pf_page add constraint pf_page_body_idx foreign key (fk_body_id) references o_pf_page_body (id);
create index idx_pf_page_body_idx on o_pf_page (fk_body_id);


create table o_pf_page_part (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   pos number(20) default null,
   dtype varchar2(32 char),
   p_content CLOB,
   p_flow varchar2(32 char),
   p_layout_options varchar2(2000 char),
   fk_media_id number(20),
   fk_page_body_id number(20),
   primary key (id)
);

alter table o_pf_page_part add constraint pf_page_page_body_idx foreign key (fk_page_body_id) references o_pf_page_body (id);
create index idx_pf_page_page_body_idx on o_pf_page_part (fk_page_body_id);
alter table o_pf_page_part add constraint pf_page_media_idx foreign key (fk_media_id) references o_pf_media (id);
create index idx_pf_page_media_idx on o_pf_page_part (fk_media_id);


create table o_pf_category (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   p_name varchar2(32 char),
   primary key (id)
);

create index idx_category_name_idx on o_pf_category (p_name);


create table o_pf_category_relation (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   p_resname varchar2(64 char) not null,
   p_resid number(20) not null,
   fk_category_id number(20) not null,
   primary key (id)
);

alter table o_pf_category_relation add constraint pf_category_rel_cat_idx foreign key (fk_category_id) references o_pf_category (id);
create index idx_pf_category_rel_cat_idx on o_pf_category_relation (fk_category_id);
create index idx_category_rel_resid_idx on o_pf_category_relation (p_resid);


create table o_pf_assessment_section (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   p_score decimal default null,
   p_passed number default null,
   p_comment CLOB,
   fk_section_id number(20) not null,
   fk_identity_id number(20) not null,
   primary key (id)
);

alter table o_pf_assessment_section add constraint pf_asection_section_idx foreign key (fk_section_id) references o_pf_section (id);
create index idx_pf_asection_section_idx on o_pf_assessment_section (fk_section_id);
alter table o_pf_assessment_section add constraint pf_asection_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_pf_asection_ident_idx on o_pf_assessment_section (fk_identity_id);


create table o_pf_assignment (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   pos number(20) default null,
   p_status varchar2(32 char) default null,
   p_type varchar2(32 char) not null,
   p_version number(20) default 0 not null,
   p_title varchar2(255 char) default null,
   p_summary CLOB,
   p_content CLOB,
   p_storage varchar2(255 char) default null,
   fk_section_id number(20) not null,
   fk_template_reference_id number(20),
   fk_page_id number(20),
   fk_assignee_id number(20),
   primary key (id)
);

alter table o_pf_assignment add constraint pf_assign_section_idx foreign key (fk_section_id) references o_pf_section (id);
create index idx_pf_assign_section_idx on o_pf_assignment (fk_section_id);
alter table o_pf_assignment add constraint pf_assign_ref_assign_idx foreign key (fk_template_reference_id) references o_pf_assignment (id);
create index idx_pf_assign_ref_assign_idx on o_pf_assignment (fk_template_reference_id);
alter table o_pf_assignment add constraint pf_assign_page_idx foreign key (fk_page_id) references o_pf_page (id);
create index idx_pf_assign_page_idx on o_pf_assignment (fk_page_id);
alter table o_pf_assignment add constraint pf_assign_assignee_idx foreign key (fk_assignee_id) references o_bs_identity (id);
create index idx_pf_assign_assignee_idx on o_pf_assignment (fk_assignee_id);












