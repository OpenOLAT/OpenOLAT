create table o_as_entry (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_attemtps int8 default null,
   a_score decimal default null,
   a_passed bool default null,
   a_status varchar(16) default null,
   a_details varchar(1024) default null,
   a_fully_assessed bool default null,
   a_assessment_id int8 default null,
   a_completion float(24),
   a_comment text,
   a_coach_comment text,
   fk_entry int8 not null,
   a_subident varchar(64),
   fk_reference_entry int8,
   fk_identity int8 default null,
   a_anon_identifier varchar(128) default null,
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
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_exploded bool default false,
   q_author_mode bool default false,
   q_finish_time timestamp,
   q_termination_time timestamp,
   q_duration int8,
   q_score decimal default null,
   q_manual_score decimal default null,
   q_passed bool default null,
   q_storage varchar(1024),
   fk_reference_entry int8 not null,
   fk_entry int8,
   q_subident varchar(64),
   fk_identity int8 default null,
   q_anon_identifier varchar(128) default null,
   fk_assessment_entry int8 not null,
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
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_itemidentifier varchar(64) not null,
   q_sectionidentifier varchar(64) default null,
   q_testpartidentifier varchar(64) default null,
   q_duration int8,
   q_score decimal default null,
   q_manual_score decimal default null,
   q_passed bool default null,
   q_storage varchar(1024),
   fk_assessmenttest_session int8 not null,
   primary key (id)
);

alter table o_qti_assessmentitem_session add constraint qti_itemsess_to_testsess_idx foreign key (fk_assessmenttest_session) references o_qti_assessmenttest_session (id);
create index idx_itemsess_to_testsess_idx on o_qti_assessmentitem_session (fk_assessmenttest_session);
create index idx_item_identifier_idx on o_qti_assessmentitem_session (q_itemidentifier);

create table o_qti_assessment_response (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_responseidentifier varchar(64) not null,
   q_responsedatatype varchar(16) not null,
   q_responselegality varchar(16) not null,
   q_stringuifiedresponse text,
   fk_assessmentitem_session int8 not null,
   fk_assessmenttest_session int8 not null,
   primary key (id)
);

alter table o_qti_assessment_response add constraint qti_resp_to_testsession_idx foreign key (fk_assessmenttest_session) references o_qti_assessmenttest_session (id);
create index idx_resp_to_testsession_idx on o_qti_assessment_response (fk_assessmenttest_session);
alter table o_qti_assessment_response add constraint qti_resp_to_itemsession_idx foreign key (fk_assessmentitem_session) references o_qti_assessmentitem_session (id);
create index idx_resp_to_itemsession_idx on o_qti_assessment_response (fk_assessmentitem_session);
create index idx_response_identifier_idx on o_qti_assessment_response (q_responseidentifier);

create table o_qti_assessment_marks (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_marks text default null,
   fk_reference_entry int8 not null,
   fk_entry int8,
   q_subident varchar(64),
   fk_identity int8 not null,
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
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_title varchar(255),
   p_status varchar(32),
   p_copy_date timestamp,
   p_return_date timestamp,
   p_deadline timestamp,
   p_summary text,
   p_image_path varchar(255),
   fk_olatresource_id int8,
   fk_group_id int8 not null,
   fk_entry_id int8,
   p_subident varchar(128),
   fk_template_id int8,
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
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   pos int8 default null,
   p_title varchar(255),
   p_description text,
   p_status varchar(32) not null default 'notStarted',
   p_begin timestamp,
   p_end timestamp,
   p_override_begin_end bool default false,
   fk_group_id int8 not null,
   fk_binder_id int8 not null,
   fk_template_reference_id int8,
   primary key (id)
);

alter table o_pf_section add constraint pf_section_group_idx foreign key (fk_group_id) references o_bs_group (id);
create index idx_pf_section_group_idx on o_pf_section (fk_group_id);
alter table o_pf_section add constraint pf_section_binder_idx foreign key (fk_binder_id) references o_pf_binder (id);
create index idx_pf_section_binder_idx on o_pf_section (fk_binder_id);
alter table o_pf_section add constraint pf_section_template_idx foreign key (fk_template_reference_id) references o_pf_section (id);
create index idx_pf_section_template_idx on o_pf_section (fk_template_reference_id);

create table o_pf_page (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   pos int8 default null,
   p_title varchar(255),
   p_summary text,
   p_status varchar(32),
   p_image_path varchar(255),
   p_image_align varchar(32),
   p_version int8 default 0,
   p_initial_publish_date timestamp,
   p_last_publish_date timestamp,
   fk_body_id int8 not null,
   fk_group_id int8 not null,
   fk_section_id int8,
   primary key (id)
);

alter table o_pf_page add constraint pf_page_group_idx foreign key (fk_group_id) references o_bs_group (id);
create index idx_pf_page_group_idx on o_pf_page (fk_group_id);
alter table o_pf_page add constraint pf_page_section_idx foreign key (fk_section_id) references o_pf_section (id);
create index idx_pf_page_section_idx on o_pf_page (fk_section_id);

create table o_pf_media (
   id bigserial,
   creationdate timestamp not null,
   p_collection_date timestamp not null,
   p_type varchar(64) not null,
   p_storage_path varchar(255),
   p_root_filename varchar(255),
   p_title varchar(255) not null,
   p_description text,
   p_content text,
   p_signature int8 not null default 0,
   p_reference_id varchar(255) default null,
   p_business_path varchar(255) not null,
   p_creators varchar(1024) default null,
   p_place varchar(255) default null,
   p_publisher varchar(255) default null,
   p_publication_date timestamp default null,
   p_date varchar(32) default null,
   p_url varchar(1024) default null,
   p_source varchar(1024) default null,
   p_language varchar(32) default null,
   p_metadata_xml text,
   fk_author_id int8 not null,
   primary key (id)
);

alter table o_pf_media add constraint pf_media_author_idx foreign key (fk_author_id) references o_bs_identity (id);
create index idx_pf_media_author_idx on o_pf_media (fk_author_id);
create index idx_media_storage_path_idx on o_pf_media (p_business_path);

create table o_pf_page_body (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   primary key (id)
);

alter table o_pf_page add constraint pf_page_body_idx foreign key (fk_body_id) references o_pf_page_body (id);
create index idx_pf_page_body_idx on o_pf_page (fk_body_id);


create table o_pf_page_part (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   pos int8 default null,
   dtype varchar(32),
   p_content text,
   p_flow varchar(32),
   p_layout_options varchar(2000),
   fk_media_id int8,
   fk_page_body_id int8,
   primary key (id)
);

alter table o_pf_page_part add constraint pf_page_page_body_idx foreign key (fk_page_body_id) references o_pf_page_body (id);
create index idx_pf_page_page_body_idx on o_pf_page_part (fk_page_body_id);
alter table o_pf_page_part add constraint pf_page_media_idx foreign key (fk_media_id) references o_pf_media (id);
create index idx_pf_page_media_idx on o_pf_page_part (fk_media_id);


create table o_pf_category (
   id bigserial,
   creationdate timestamp not null,
   p_name varchar(32),
   primary key (id)
);

create index idx_category_name_idx on o_pf_category (p_name);


create table o_pf_category_relation (
   id bigserial,
   creationdate timestamp not null,
   p_resname varchar(64) not null,
   p_resid int8 not null,
   fk_category_id int8 not null,
   primary key (id)
);

alter table o_pf_category_relation add constraint pf_category_rel_cat_idx foreign key (fk_category_id) references o_pf_category (id);
create index idx_pf_category_rel_cat_idx on o_pf_category_relation (fk_category_id);
create index idx_category_rel_resid_idx on o_pf_category_relation (p_resid);


create table o_pf_assessment_section (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_score decimal default null,
   p_passed bool default null,
   p_comment text,
   fk_section_id int8 not null,
   fk_identity_id int8 not null,
   primary key (id)
);

alter table o_pf_assessment_section add constraint pf_asection_section_idx foreign key (fk_section_id) references o_pf_section (id);
create index idx_pf_asection_section_idx on o_pf_assessment_section (fk_section_id);
alter table o_pf_assessment_section add constraint pf_asection_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_pf_asection_ident_idx on o_pf_assessment_section (fk_identity_id);


create table o_pf_assignment (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   pos int8 default null,
   p_status varchar(32) default null,
   p_type varchar(32) not null,
   p_version int8 not null default 0,
   p_title varchar(255) default null,
   p_summary text,
   p_content text,
   p_storage varchar(255) default null,
   fk_section_id int8 not null,
   fk_template_reference_id int8,
   fk_page_id int8,
   fk_assignee_id int8,
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












