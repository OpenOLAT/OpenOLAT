-- AI
create table o_ai_usage_log (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate timestamp not null,
   a_usage_context_type varchar2(128),
   a_usage_context_id varchar2(36),
   a_resource_type varchar2(128),
   a_resource_id number(19,0),
   a_resource_sub_id varchar2(128),
   fk_identity number(19,0),
   a_locale varchar2(32),
   a_ai_feature varchar2(128) not null,
   a_duration_millis number(20),
   a_status varchar2(32) not null,
   a_error_code varchar2(128),
   a_error_message clob,
   a_model_provider varchar2(64),
   a_req_model varchar2(128),
   a_req_temperature number(6,4),
   a_req_top_p number(6,4),
   a_req_max_output_tokens number(20),
   a_invocation_id varchar2(36),
   a_service_interface varchar2(255),
   a_service_method varchar2(255),
   a_resp_id varchar2(255),
   a_resp_model varchar2(128),
   a_resp_finish_reason varchar2(64),
   a_input_tokens number(20),
   a_output_tokens number(20),
   a_total_tokens number(20),
   a_cached_input_tokens number(20),
   a_reasoning_tokens number(20),
   a_req_num_messages number(20),
   a_req_text_length number(20),
   a_cache_creation_input_tokens number(20),
   primary key (id)
);
create index idx_ai_log_creation_idx on o_ai_usage_log (creationdate);

-- ac offer indexes
drop index idx_offer_guest_idx;
drop index idx_offer_open_idx;
drop index idx_rel_oto_org_idx;
drop index idx_offeracc_offer_idx;
create index idx_offer_catalog_guest_idx on o_ac_offer (is_valid, guest_access, catalog_web_publish, fk_resource_id);
create index idx_offer_catalog_open_idx on o_ac_offer (is_valid, open_access, catalog_web_publish, fk_resource_id);
create index idx_rel_oto_org_offer_idx on o_ac_offer_to_organisation (fk_organisation, fk_offer);
create index idx_offeracc_offer_method_idx on o_ac_offer_access (fk_offer_id, fk_method_id);

-- ac cancelling enabled
alter table o_ac_offer add cancelling_enabled number default 1 not null;
alter table o_ac_order_part add cancelling_enabled number default 1 not null;
alter table o_ac_order_line add cancelling_enabled number default 1 not null;
-- ac offer valid config
alter table o_ac_offer add valid_status varchar(255 char);
alter table o_ac_offer add valid_date_config varchar(2000 char);

-- To-dos
alter table o_todo_task add t_relative_dates varchar(2000 char);

-- Room management
create table o_rm_location (
    id number(20) GENERATED ALWAYS AS IDENTITY,
    creationdate timestamp not null,
    lastmodified timestamp not null,
    r_status varchar2(16) default 'active' not null,
    r_name varchar2(255) not null,
    r_ext_id varchar2(64),
    r_ext_ref varchar2(255),
    r_description clob,
    r_address varchar2(1024),
    r_info_url varchar2(1024),
    r_geo_lat number(10,7),
    r_geo_lon number(10,7),
    primary key (id)
);

create unique index idx_rm_loc_ext_id on o_rm_location (r_ext_id);

create table o_rm_location_to_org (
    id number(20) GENERATED ALWAYS AS IDENTITY,
    creationdate timestamp not null,
    fk_location number(19,0) not null,
    fk_organisation number(19,0) not null,
    primary key (id)
);

create unique index idx_rm_loc_org on o_rm_location_to_org (fk_location, fk_organisation);
alter table o_rm_location_to_org add constraint rm_loc_to_loc_idx foreign key (fk_location) references o_rm_location(id);
alter table o_rm_location_to_org add constraint rm_loc_to_org_idx foreign key (fk_organisation) references o_org_organisation(id);

create table o_rm_room (
    id number(20) GENERATED ALWAYS AS IDENTITY,
    creationdate timestamp not null,
    lastmodified timestamp not null,
    r_status varchar2(16) default 'active' not null,
    r_name varchar2(255) not null,
    r_ext_id varchar2(64),
    r_ext_ref varchar2(255),
    r_description clob,
    r_seats number(10,0),
    r_admin_info clob,
    fk_location number(19,0) not null,
    primary key (id)
);

create unique index idx_rm_room_ext_id on o_rm_room (r_ext_id);
alter table o_rm_room add constraint rm_room_to_loc_idx foreign key (fk_location) references o_rm_location(id);
create index idx_rm_room_loc on o_rm_room(fk_location);

create table o_rm_room_booking (
    id number(20) GENERATED ALWAYS AS IDENTITY,
    creationdate timestamp not null,
    lastmodified timestamp not null,
    r_start_date timestamp not null,
    r_end_date timestamp not null,
    r_buffer_before number(10,0) default 0 not null,
    r_buffer_after number(10,0) default 0 not null,
    fk_room number(19,0) not null,
    fk_lecture_block number(19,0) not null,
    primary key (id)
);

create unique index idx_rm_booking_block_room on o_rm_room_booking (fk_lecture_block, fk_room);
alter table o_rm_room_booking add constraint rm_book_to_room_idx foreign key (fk_room) references o_rm_room(id);
alter table o_rm_room_booking add constraint rm_book_to_lb_idx foreign key (fk_lecture_block) references o_lecture_block(id);
create index idx_rm_book_room_time on o_rm_room_booking(fk_room, r_start_date, r_end_date);
create index idx_rm_book_lb on o_rm_room_booking(fk_lecture_block);

create table o_rm_module_log (
    id number(20) GENERATED ALWAYS AS IDENTITY,
    creationdate timestamp not null,
    r_action varchar2(64) not null,
    r_before clob,
    r_before_status varchar2(64),
    r_after clob,
    r_after_status varchar2(64),
    fk_doer number(19,0),
    fk_location number(19,0),
    fk_room number(19,0),
    fk_booking number(19,0),
    fk_lecture_block number(19,0),
    primary key (id)
);

alter table o_rm_module_log add constraint rm_log_to_doer_idx foreign key (fk_doer) references o_bs_identity(id);
alter table o_rm_module_log add constraint rm_log_to_loc_idx foreign key (fk_location) references o_rm_location(id);
alter table o_rm_module_log add constraint rm_log_to_room_idx foreign key (fk_room) references o_rm_room(id);
alter table o_rm_module_log add constraint rm_log_to_book_idx foreign key (fk_booking) references o_rm_room_booking(id);
alter table o_rm_module_log add constraint rm_log_to_lb_idx foreign key (fk_lecture_block) references o_lecture_block(id);
create index idx_rm_log_room_date on o_rm_module_log(fk_room, creationdate);


-- Selectus
alter table o_user add u_title varchar(255);


CREATE TABLE o_selectus_position (
  pos_id number(20) NOT NULL,
  version number(11) NOT NULL,
  creationdate date DEFAULT NULL,
  status varchar(32) NOT NULL,
  is_valid number,
  planingsnumber varchar(32) DEFAULT NULL,
  positiontitle varchar(255) DEFAULT NULL,
  positiontitlede varchar(255) DEFAULT NULL,
  positiontitlefr varchar(255) DEFAULT NULL,
  shorttitle varchar(255) DEFAULT NULL,
  shorttitlede varchar(255) DEFAULT NULL,
  shorttitlefr varchar(255) DEFAULT NULL,
  descr CLOB DEFAULT NULL,
  descrde CLOB DEFAULT NULL,
  descrfr CLOB DEFAULT NULL,
  messagecommittee CLOB DEFAULT NULL,
  availablelanguages varchar(64) DEFAULT NULL,
  department varchar(255) DEFAULT NULL,
  departmentde varchar(255) DEFAULT NULL,
  departmentfr varchar(255) DEFAULT NULL,
  homepage varchar(255) DEFAULT NULL,
  availabledocs varchar(1024) DEFAULT NULL,
  mandatorydocs varchar(1024) DEFAULT NULL,
  staffdocs varchar(1024) DEFAULT NULL,
  combineddocs varchar(1024) DEFAULT NULL,
  docsizes varchar(1024) DEFAULT NULL,
  docsnames varchar(4000) DEFAULT NULL,
  docsnamesde varchar(4000) DEFAULT NULL,
  docsnamesfr varchar(4000) DEFAULT NULL,
  docsexplain CLOB DEFAULT NULL,
  docsexplainde CLOB DEFAULT NULL,
  docsexplainfr CLOB DEFAULT NULL,
  docspdfs varchar(4000) DEFAULT 'all',
  docsdocx varchar(4000) DEFAULT NULL,
  docsxlsx varchar(4000) DEFAULT NULL,
  docsjpg varchar(4000) DEFAULT NULL,
  applicationDeadline date DEFAULT NULL,
  professorship varchar(32) DEFAULT 'any',
  ratingdeadline date DEFAULT NULL,
  committeereminder date DEFAULT NULL,
  committeeremindersent date DEFAULT NULL,
  jobads varchar(1024),
  decisiontool number default 0,
  decisiontoollimit number(20) default 0,
  applicationproject number default 0,
  academicalbackground number default 1,
  advertised number default 1 not null,
  memo CLOB DEFAULT NULL,
  mailsetting varchar(32) DEFAULT NULL,
  mailsender varchar(255) DEFAULT NULL,
  mailbcc varchar(255) DEFAULT NULL,
  position_duplicate_app_allowed varchar(32) DEFAULT NULL,
  committee_mail_template CLOB DEFAULT NULL,
  committee_mail_letter CLOB DEFAULT NULL,
  conf_mail_template CLOB DEFAULT NULL,
  conf_mail_template_de CLOB DEFAULT NULL,
  conf_mail_template_fr CLOB DEFAULT NULL,
  conf_mail_letter CLOB DEFAULT NULL,
  conf_mail_template_mgmt CLOB DEFAULT NULL,
  conf_mail_template_mgmt_de CLOB DEFAULT NULL,
  conf_mail_template_mgmt_fr CLOB DEFAULT NULL,
  conf_mail_letter_mgmt CLOB DEFAULT NULL,
  confdup_mail_template CLOB DEFAULT NULL,
  confdup_mail_template_de CLOB DEFAULT NULL,
  confdup_mail_template_fr CLOB DEFAULT NULL,
  confdup_mail_letter CLOB DEFAULT NULL,
  tabsconfiguration CLOB DEFAULT NULL,
  customtabs varchar(255),
  rec_excluded_attributes varchar(4000) default null,
  rec_expert_enable number default 0 not null,
  rec_expert_deadline date,
  rec_expert_mail_template CLOB DEFAULT NULL,
  rec_expert_mail_letter CLOB DEFAULT NULL,
  rec_expert_docs varchar(2000),
  rec_expert_fields varchar(4000),
  rec_referee_enable number default 0 not null,
  rec_referee_min number(20),
  rec_referee_max number(20),
  rec_referee_mail_type varchar(32),
  rec_referee_deadline date,
  rec_referee_mail_template CLOB DEFAULT NULL,
  rec_referee_mail_letter CLOB DEFAULT NULL,
  rec_referee_docs varchar(2000),
  rec_referee_fields varchar(4000),
  rec_comp_expert_enable number default 0 not null,
  rec_comp_expert_deadline date,
  rec_comp_expert_mail_template CLOB default null,
  rec_comp_expert_docs varchar(2000) default 'all',
  rec_comp_expert_mail_letter CLOB default null,
  rec_comp_expert_fields varchar(4000),
  rec_public_feedback_enable number default 0 not null,
  rec_public_feedback_deadline date,
  rec_app_referee_mgt_deadline date,
  rec_app_referee_mgt_enable number default 0 not null,
  committeecomment_enable number DEFAULT 0,
  committeecomment_visibility varchar(64),
  review_enable number DEFAULT 0,
  system_tags_enable number DEFAULT 0,
  position_tags_enable number DEFAULT 0,
  fk_review_definition_id number(20) DEFAULT NULL,
  fk_doc_1_id number(20) DEFAULT NULL,
  fk_doc_2_id number(20) DEFAULT NULL,
  fk_doc_3_id number(20) DEFAULT NULL,
  fk_committeegroup number(20) DEFAULT NULL,
  fk_committeeheadgroup number(20) DEFAULT NULL,
  fk_secretarygroup number(20) DEFAULT NULL,
  fk_exofficiogroup number(20) DEFAULT NULL,
  rating_policy_link_url_1 varchar(255),
  rating_policy_link_url_2 varchar(255),
  rating_policy_link_url_3 varchar(255),
  rating_policy_link_url_4 varchar(255),
  rating_policy_link_label_1 varchar(1024),
  rating_policy_link_label_2 varchar(1024),
  rating_policy_link_label_3 varchar(1024),
  rating_policy_link_label_4 varchar(1024),
  fk_org_unit_id number(20) default null,
  fk_organisation_id number(20) default null,
  PRIMARY KEY (pos_id)
);

create table o_selectus_pos_attribute_def (
   id number(20) not null,
   lastmodified date,
   creationdate date,
   pos number(20),
   orderpos number(20) default 0,
   label varchar(255),
   labelde varchar(255),
   labelfr varchar(255),
   placeholder varchar(255),
   placeholderde varchar(255),
   placeholderfr varchar(255),
   applicationtab varchar(32),
   attributetype varchar(2000),
   attributemandatory number default 0,
   attributeconfiguration CLOB,
   fk_position_id number(20),
   primary key (id)
);

CREATE TABLE o_selectus_application (
 app_id number(20) NOT NULL,
 version number(11) NOT NULL,
 creationdate date DEFAULT NULL,
 last_modified date DEFAULT NULL,
 application_lid number(20) DEFAULT 0,
 applanguage varchar(16) DEFAULT NULL,
 is_valid number,
 firstname varchar(255) DEFAULT NULL,
 lastname varchar(255) DEFAULT NULL,
 persontitle varchar(64) DEFAULT NULL,
 academictitle varchar(255) DEFAULT NULL,
 gender varchar(32) DEFAULT NULL,
 marital_status varchar(32) DEFAULT NULL,
 disability number DEFAULT 0,
 birthday date DEFAULT NULL,
 nationality varchar(64) DEFAULT NULL,
 addnationalities varchar(2000) DEFAULT NULL,
 mail varchar(64) DEFAULT NULL,
 phone varchar(64) DEFAULT NULL,
 mobile_phone varchar(64) DEFAULT NULL,
 addressline1 varchar(255) DEFAULT NULL,
 addressline2 varchar(255) DEFAULT NULL,
 addressline3 varchar(255) DEFAULT NULL,
 addr_type varchar(32),
 organization varchar(255),
 unit varchar(255),
 zipcode varchar(64) DEFAULT NULL,
 city varchar(64) DEFAULT NULL,
 country varchar(64) DEFAULT NULL,
 biz_addressline1 varchar(255) DEFAULT NULL,
 biz_addressline2 varchar(255) DEFAULT NULL,
 biz_addressline3 varchar(255 ) DEFAULT NULL,
 biz_zipcode varchar(255) DEFAULT NULL,
 biz_city varchar(255) DEFAULT NULL,
 biz_country varchar(255) DEFAULT NULL,
 biz_phone varchar(255) DEFAULT NULL,
 biz_mail varchar(255) DEFAULT NULL,
 affiliation varchar(255) DEFAULT NULL,
 currentposition varchar(255) DEFAULT NULL,
 originalpublications number(20) DEFAULT NULL,
 firstauthorships number(20) DEFAULT NULL,
 lastauthorships number(20) DEFAULT NULL,
 citations number(20) DEFAULT NULL,
 impactfactor float DEFAULT NULL,
 hfactor float DEFAULT NULL,
 highestdegreetype varchar(255) DEFAULT NULL,
 highestdegreedescr varchar(1024) DEFAULT NULL,
 highestdegreeinstitution varchar(255) DEFAULT NULL,
 highestdegreedate date DEFAULT NULL,
 highestdegreeworkedsince varchar(1024) DEFAULT NULL,
 workedoutacademia varchar(1024) DEFAULT NULL,
 workedoutacademiacare varchar(1024) DEFAULT NULL,
 careerdescription CLOB DEFAULT NULL,
 dissertationtitle varchar(255) DEFAULT NULL,
 dissertationinstitution varchar(255) DEFAULT NULL,
 dissertationdate date DEFAULT NULL,
 dissertationkeyword1 varchar(1024) DEFAULT NULL,
 dissertationkeyword2 varchar(1024) DEFAULT NULL,
 dissertationkeyword3 varchar(1024) DEFAULT NULL,
 habilitationtitle varchar(255) DEFAULT NULL,
 habilitationinstitution varchar(255) DEFAULT NULL,
 habilitationdate date DEFAULT NULL,
 orcid varchar(255) default null,
 projecttitle varchar(1024) default null,
 projectfinancialimpact varchar(1024) default null,
 projectfinancialimpact_2 varchar(1024) default null,
 projectfinancialimpact_3 varchar(1024) default null,
 projectfinancialimpact_4 varchar(1024) default null,
 projectfinancialimpact_5 varchar(1024) default null,
 projectstartdate date default null,
 projectduration varchar(1024) default null,
 projectdescription CLOB default null,
 acronym varchar(1024) default null,
 keywords varchar(1024) default null,
 disciplines varchar(1024) default null,
 public_feedback_enable number default 0 not null,
 public_feedback_deadline date default null,
 public_feedback_key varchar(64) default null,
 after_position_closed number DEFAULT 0,
 withdrawn number DEFAULT 0,
 withdrawn_date date DEFAULT NULL,
 status varchar(16) DEFAULT 'active' NOT NULL,
 onhold_date date DEFAULT NULL,
 rejected_date date DEFAULT NULL,
 noteligible_date date DEFAULT NULL,
 granted_date date DEFAULT NULL,
 hired_date date DEFAULT NULL,
 status_comment CLOB,
 acceptterms number DEFAULT NULL,
 jobad varchar(255),
 expertblacklist varchar(4000) default null,
 expertconsent number default null,
 memo CLOB DEFAULT NULL,
 committeecomment CLOB DEFAULT NULL,
 decision integer,
 applicanturl varchar(255) DEFAULT NULL,
 report_ratings_2 number(20) DEFAULT NULL,
 report_ratings_1 number(20) DEFAULT NULL,
 report_ratings_0 number(20) DEFAULT NULL,
 report_ratings_abstentions number(20) DEFAULT NULL,
 report_experts number(20) DEFAULT NULL,
 report_experts_letters number(20) DEFAULT NULL,
 report_referees number(20) DEFAULT NULL,
 report_referees_letters number(20) DEFAULT NULL,
 report_comp_experts number(20) DEFAULT NULL,
 report_comp_experts_letters number(20) DEFAULT NULL,
 fk_identity_id number(20) DEFAULT NULL,
 fk_coveringletter_id number(20) DEFAULT NULL,
 fk_curriculumvitae_id number(20) DEFAULT NULL,
 fk_publicationlist_id number(20) DEFAULT NULL,
 fk_researchstatement_id number(20) DEFAULT NULL,
 fk_teachingstatement_id number(20) DEFAULT NULL,
 fk_certificateofstudy_id number(20) DEFAULT NULL,
 fk_leadership_id number(20) DEFAULT NULL,
 fk_listofreferees_id number(20) DEFAULT NULL,
 fk_projectlist_id number(20) DEFAULT NULL,
 fk_referenceletters_id number(20) DEFAULT NULL,
 fk_teachingassessment_id number(20) DEFAULT NULL,
 fk_degreecertificates_id number(20) DEFAULT NULL,
 fk_dissertation_id number(20) DEFAULT NULL,
 fk_habilitation_id number(20) DEFAULT NULL,
 fk_clinicaldisciplines_id number(20) DEFAULT NULL,
 fk_surgicaldisciplines_id number(20) DEFAULT NULL,
 fk_reprints_id number(20) DEFAULT NULL,
 fk_externalfunding_id number(20) DEFAULT NULL,
 fk_publication1_id number(20) DEFAULT NULL,
 fk_publication2_id number(20) DEFAULT NULL,
 fk_publication3_id number(20) DEFAULT NULL,
 fk_publication4_id number(20) DEFAULT NULL,
 fk_publication5_id number(20) DEFAULT NULL,
 fk_otherdocument_id number(20) DEFAULT NULL,
 fk_position_id number(20),
 fk_combineddocument_id number(20) DEFAULT NULL,
 PRIMARY KEY (app_id)
);

create table o_selectus_app_attribute (
   id number(20) not null,
   lastmodified date,
   creationdate date,
   attrvalue varchar(4000),
   fk_definition_id number(20) not null,
   fk_application_id number(20),
   fk_position_id number(20),
   primary key (id)
);

CREATE TABLE o_selectus_attachment (
	attachment_id number(20) NOT NULL,
	version number(11) NOT NULL,
	creationdate date DEFAULT NULL,
    file_name VARCHAR(255),
    file_type VARCHAR(8) DEFAULT 'pdf',
	datas BLOB,
	datas_size number(11),
	PRIMARY KEY (attachment_id)
);

create table o_selectus_committee_report (
   id number(20) not null,
   creationdate timestamp default null,
   lastmodified timestamp default null,
   r_role varchar(255),
   r_ratings_rights varchar(255),
   r_gender varchar(255),
   r_user_classification varchar(255),
   r_num_ratings_a number(20) default null,
   r_num_ratings_b number(20) default null,
   r_num_ratings_c number(20) default null,
   r_num_abstentions number(20) default null,
   fk_position_id number(20) not null,
   PRIMARY KEY (id)
);

CREATE TABLE o_selectus_position_policy (
  pol_id number(20) NOT NULL,
  version number(11) NOT NULL,
  creationdate date DEFAULT NULL,
  dont_show_next_time number DEFAULT 0,
  policytype varchar(32) DEFAULT NULL,
  fk_position_id number(20) DEFAULT NULL,
  fk_identity_id number(20) DEFAULT NULL,
  PRIMARY KEY (pol_id)
);

CREATE TABLE o_selectus_mail_template (
   id number(20) not null,
   lastmodified timestamp,
   creationdate timestamp,
   f_id varchar(255),
   f_name varchar(255),
   f_subject varchar(255),
   f_subject_de varchar(255),
   f_subject_fr varchar(255),
   f_body CLOB,
   f_body_de CLOB,
   f_body_fr CLOB,
   f_letter CLOB,
   fk_position_id number(20),
   primary key (id)
);

CREATE TABLE o_selectus_application_notes (
  notes_id number(20) NOT NULL,
  version number(11) NOT NULL,
  creationdate date DEFAULT NULL,
  notes_content VARCHAR(4000),
  fk_application_id number(20) DEFAULT NULL,
  fk_identity_id number(20) DEFAULT NULL,
  PRIMARY KEY (notes_id)
);

create table o_selectus_public_feedback (
   id number(20) not null,
   creationdate timestamp default null,
   last_modified timestamp default null,
   r_firstname varchar(256),
   r_lastname varchar(256),
   r_email varchar(256),
   r_external_id varchar(256),
   r_external_ref varchar(256),
   r_comment CLOB,
   fk_application_id number(20) not null,
   PRIMARY KEY (id)
);

create table o_selectus_apps_feedback (
   id number(20) not null,
   creationdate timestamp default null,
   last_modified timestamp default null,
   r_name varchar(128),
   r_enabled number default 0 not null,
   r_deadline timestamp,
   r_mail_template CLOB,
   r_mail_letter CLOB,
   r_docs varchar(2000) default 'all',
   r_experts_docs number default 0 not null,
   r_referees_docs number default 0 not null,
   r_experts_comp_assessment_docs number default 0 not null,
   r_fields varchar(4000),
   fk_position_id number(20) not null,
   PRIMARY KEY (id)
);

create table o_selectus_app_feedback (
   id number(20)  not null,
   creationdate timestamp default null,
   last_modified timestamp default null,
   r_comment CLOB,
   r_comment_date timestamp,
   r_deadline timestamp,
   r_status varchar(32) default 'notSent' not null,
   r_request_date timestamp,
   r_last_reminder_date timestamp,
   fk_config_id number(20)  not null,
   fk_application_id number(20)  not null,
   fk_identity_id number(20)  not null,
   PRIMARY KEY (id)
);

CREATE TABLE o_selectus_rejection_email_log (
  log_id number(20) NOT NULL,
  version number(11) NOT NULL,
  status number(11) NOT NULL,
  creationdate date DEFAULT NULL,
  mail_template varchar(32) DEFAULT NULL,
  mail_subject varchar(1024) DEFAULT NULL,
  mail_content CLOB DEFAULT NULL,
  mail_rejection number DEFAULT 0,
  fk_application_id number(20) DEFAULT NULL,
  fk_letter_id number(20) DEFAULT NULL,
  PRIMARY KEY (log_id)
);

create table o_selectus_reference (
   ref_id number(20) NOT NULL,
   lastmodified date NOT NULL,
   creationdate date NOT NULL,
   version number(11) not null,
   firstname varchar(255),
   lastname varchar(255),
   title varchar(255),
   institution varchar(255),
   mail varchar(255),
   disclaimer number default 0 not null,
   privacydisclaimer number default 0 not null,
   submissionurl varchar(64),
   submissiondate date,
   submissiondeadline date,
   reftype varchar(32),
   requeststatus varchar(32) DEFAULT 'notAnswered',
   adminnote CLOB,
   dateconsent date default null,
   consentbystaff number default null,
   status varchar(32),
   dateinvitation date,
   datelastreminder date,
   remindersbyapplicant number(20) default 0,
   fk_letter_id number(20),
   fk_application_id number(20),
   primary key (ref_id)
);

create table o_selectus_reference_comment (
   id number(20) not null,
   creationdate timestamp NOT NULL,
   r_comment CLOB,
   fk_reference_id number(20) not null,
   PRIMARY KEY (id)
);

create table o_selectus_reference_to_app (
   id number(20) generated always as identity,
   creationdate date not null,
   fk_reference_id number(20) not null,
   fk_application_id number(20) not null,
   PRIMARY KEY (id)
);

create table o_selectus_decision_rubric_def (
   def_id number(20) NOT NULL,
   creationdate date,
   lastmodified date,
   d_pos number(20) default 0 NOT NULL,
   d_rubric varchar(128) NOT NULL,
   d_type varchar(32) NOT NULL,
   d_sum number default 0 NOT NULL,
   d_weight number(20) default 10 NOT NULL,
   fk_position_id number(20) NOT NULL,
   primary key (def_id)
);

create table o_selectus_decision_rubric (
   rubric_id number(20) NOT NULL,
   lastmodified date,
   creationdate date,
   d_string_value varchar(1024),
   d_integer_value number(20),
   fk_definition_id number(20) NOT NULL,
   fk_application_id number(20) NOT NULL,
   primary key (rubric_id)
);

create table o_selectus_review_position_def (
   id number(20) not null,
   lastmodified date,
   creationdate date,
   review_comment_enable number default 0,
   r_rev_name_visibility varchar(16),
   r_rev_visibility varchar(16),
   r_rev_visibility_head varchar(16),
   r_rev_visibility_secretary varchar(16),
   r_rev_visibility_exofficio varchar(16),
   r_rev_fill varchar(16) default 'fill',
   r_rev_fill_head varchar(16) default 'no',
   r_rev_fill_secretary varchar(16) default 'no',
   r_rev_fill_exofficio varchar(16) default 'no',
   r_slider_steps number(20) default null,
   r_slider_left_label varchar(255),
   r_slider_right_label varchar(255),
   r_rev_statistics_enable number default null,
   r_rev_chart_enable number default null,
   primary key (id)
);

create table o_selectus_review_element_def (
   id number(20) not null,
   lastmodified date,
   creationdate date,
   pos number(20) default null,
   r_type varchar(16),
   r_label varchar(255),
   fk_pos_rev_id number(20) not null,
   primary key (id)
);

create table o_selectus_review_response (
   id number(20) not null,
   lastmodified date,
   creationdate date,
   r_string_value varchar(4000),
   r_integer_value number(20),
   fk_reviewer_id number(20) not null,
   fk_element_id number(20) not null,
   fk_application_id number(20) not null,
   primary key (id)
);

create table o_selectus_app_comment (
   id number(20) not null,
   lastmodified date,
   creationdate date,
   r_deleted number default 0,
   r_comment CLOB,
   fk_author_id number(20) not null,
   fk_application_id number(20) not null,
   fk_reviewer_id number(20),
   fk_app_comment_id number(20),
   primary key (id)
);

create table o_selectus_assignment (
   id number(20) not null,
   creationdate date,
   fk_application_id number(20),
   fk_assignee_id number(20),
   primary key (id)
);

create table o_selectus_app_comment_vote (
   id number(20) not null,
   lastmodified date,
   creationdate date,
   r_up number,
   fk_voter_id number(20) not null,
   fk_app_comment_id number(20),
   primary key (id)
);

create table o_selectus_category (
   id number(20) not null,
   creationdate date,
   lastmodified date,
   r_name varchar(255),
   r_color varchar(16),
   fk_position_id number,
   primary key (id)
);

create table o_selectus_app_category (
   id number(20) not null,
   lastmodified date,
   creationdate date,
   r_administrative number default 0,
   fk_application_id number(20) not null,
   fk_category_id number(20) not null,
   primary key (id)
);


create table o_selectus_audit_log (
   id number(20) not null,
   creationdate date not null,
   r_action varchar(32) not null,
   r_action_target varchar(32) not null,
   r_val_before CLOB,
   r_val_after CLOB,
   r_message varchar(2000),
   r_message_i18n varchar(2000),
   r_message_val_1 varchar(255),
   r_message_val_2 varchar(255),
   r_message_val_3 varchar(255),
   r_message_val_4 varchar(255),
   r_message_val_5 varchar(255),
   fk_position_id number(20) not null,
   fk_application_id number(20),
   fk_committee_identity_id number(20),
   fk_rating_id number(20),
   fk_comment_id number(20),
   fk_reference_id number(20),
   fk_identity_id number(20),
   fk_feedback_id number(20),
   primary key (id)
);

create table o_selectus_audit_log_read (
   id number(20) not null,
   creationdate date not null,
   r_read number default 1 not null,
   fk_identity_id number(20) not null,
   fk_audit_log_id number(20) not null,
   primary key (id)
);

create table o_selectus_audit_log_usettings (
   id number(20) not null,
   creationdate date,
   lastmodified date,
   r_enabled number default 0 not null,
   r_interval varchar(16) default 'never' not null,
   fk_identity_id number(20) not null,
   primary key (id)
);

create table o_selectus_audit_log_u_notifs (
   id number(20) not null,
   creationdate date,
   r_last_mail_date date,
   fk_identity_id number(20) not null,
   primary key (id)
);


create table o_selectus_org_unit (
   id number(20) not null,
   lastmodified date,
   creationdate date,
   unitname varchar(255),
   unitnamede varchar(255),
   unitnamefr varchar(255),
   url varchar(255),
   description varchar(2000),
   systemconfig number default 1 not null,
   staffmail varchar(255),
   staffbcc varchar(255),
   mailsignature varchar(2000),
   fk_organisation_id number(20),
   primary key (id)
);

alter table o_selectus_position add constraint selectus_pos_to_org_idx foreign key (fk_organisation_id) references o_org_organisation (id);
create index idx_selectus_pos_to_org_idx on o_selectus_position (fk_organisation_id);

alter table o_selectus_org_unit add constraint selectus_ounit_to_org_idx foreign key (fk_organisation_id) references o_org_organisation (id);
create index idx_selectus_ounit_to_org_idx on o_selectus_org_unit (fk_organisation_id);


alter table o_selectus_application add constraint posid_to_app foreign key (fk_position_id) references o_selectus_position (pos_id);
alter table o_selectus_application add constraint att_coveringid_to_app foreign key (fk_coveringletter_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_cvid_to_app foreign key (fk_curriculumvitae_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_publicationid_to_app foreign key (fk_publicationlist_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_researchid_to_app foreign key (fk_researchstatement_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_teachingid_to_app foreign key (fk_teachingstatement_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_certofstudy_to_app foreign key (fk_certificateofstudy_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_leadershipid_to_app foreign key (fk_leadership_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_refereesid_to_app foreign key (fk_listofreferees_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_projectid_to_app foreign key (fk_projectlist_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_referenceid_to_app foreign key (fk_referenceletters_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_otherid_to_app foreign key (fk_otherdocument_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_combined_to_app foreign key (fk_combineddocument_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_teachassessment_to_app foreign key (fk_teachingassessment_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_degcertificates_to_app foreign key (fk_degreecertificates_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_dissertation_to_app foreign key (fk_dissertation_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_habilitation_to_app foreign key (fk_habilitation_id) references o_selectus_attachment (attachment_id);

alter table o_selectus_application add constraint att_clinicaldisciplines_to_app foreign key (fk_clinicaldisciplines_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_surgicaldisciplines_to_app foreign key (fk_surgicaldisciplines_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_reprints_to_app foreign key (fk_reprints_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_externalfunding_to_app foreign key (fk_externalfunding_id) references o_selectus_attachment (attachment_id);

alter table o_selectus_application add constraint att_publication1_to_app foreign key (fk_publication1_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_publication2_to_app foreign key (fk_publication2_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_publication3_to_app foreign key (fk_publication3_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_publication4_to_app foreign key (fk_publication4_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_publication5_to_app foreign key (fk_publication5_id) references o_selectus_attachment (attachment_id);

alter table o_selectus_application add constraint app_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_app_ident_idx on o_selectus_application (fk_identity_id);

alter table o_selectus_app_attribute add constraint attr_to_attrdef foreign key (fk_definition_id) references o_selectus_pos_attribute_def (id);
create index attr_to_attrdef_idx on o_selectus_app_attribute (fk_definition_id);
alter table o_selectus_app_attribute add constraint attr_to_app foreign key (fk_application_id) references o_selectus_application (app_id);
create index attr_to_app_idx on o_selectus_app_attribute (fk_application_id);
alter table o_selectus_app_attribute add constraint attr_to_gpos_idx foreign key (fk_position_id) references o_selectus_position (pos_id);
create index idx_attr_to_gpos_idx on o_selectus_app_attribute (fk_position_id);

alter table o_selectus_committee_report add constraint com_rep_to_pos_idx foreign key (fk_position_id) references o_selectus_position (pos_id);
create index idx_com_rep_to_pos_idx on o_selectus_committee_report (fk_position_id);

alter table o_selectus_pos_attribute_def add constraint posattrdef_to_pos foreign key (fk_position_id) references o_selectus_position (pos_id);
create index posattrdef_to_pos_idx on o_selectus_pos_attribute_def (fk_position_id);

alter table o_selectus_position add constraint att_doc_1_to_att foreign key (fk_doc_1_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_position add constraint att_doc_2_to_att foreign key (fk_doc_2_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_position add constraint att_doc_3_to_att foreign key (fk_doc_3_id) references o_selectus_attachment (attachment_id);

alter table o_selectus_position add constraint pos_review_def_idx foreign key (fk_review_definition_id) references o_selectus_review_position_def (id);
create index idx_pos_review_def_idx on o_selectus_position (fk_review_definition_id);

alter table o_selectus_position_policy add constraint policy_to_pos foreign key (fk_position_id) references o_selectus_position (pos_id);
alter table o_selectus_position_policy add constraint policy_to_identity foreign key (fk_identity_id) references o_bs_identity (id);

alter table o_selectus_mail_template add constraint mtemplate_pos_idx foreign key (fk_position_id) references o_selectus_position (pos_id);
create index idx_mtemplate_pos_idx on o_selectus_mail_template (fk_position_id);

alter table o_selectus_application_notes add constraint app_notes_to_app foreign key (fk_application_id) references o_selectus_application (app_id);
alter table o_selectus_application_notes add constraint app_notes_to_identity foreign key (fk_identity_id) references o_bs_identity (id);

alter table o_selectus_rejection_email_log add constraint rejection_to_app foreign key (fk_application_id) references o_selectus_application (app_id);
alter table o_selectus_rejection_email_log add constraint att_letter_to_maillog_idx foreign key (fk_letter_id) references o_selectus_attachment (attachment_id);
create index idx_att_letter_to_maillog_idx on o_selectus_rejection_email_log (fk_letter_id);

alter table o_selectus_reference add constraint att_letter_to_ref foreign key (fk_letter_id) references o_selectus_attachment (attachment_id);
create index idx_att_letter_to_ref_idx on o_selectus_reference (fk_letter_id);
alter table o_selectus_reference add constraint application_to_ref foreign key (fk_application_id) references o_selectus_application (app_id);
create index idx_application_to_ref_idx on o_selectus_reference (fk_application_id);

alter table o_selectus_reference_comment add constraint ref_comm_to_ref_idx foreign key (fk_reference_id) references o_selectus_reference (ref_id);
create index idx_ref_comm_to_ref_idx on o_selectus_reference_comment (fk_reference_id);

alter table o_selectus_reference_to_app add constraint ref_to_app_idx foreign key (fk_application_id) references o_selectus_application (app_id);
create index idx_ref_to_app_idx on o_selectus_reference_to_app (fk_application_id);
alter table o_selectus_reference_to_app add constraint ref_to_ref_idx foreign key (fk_reference_id) references o_selectus_reference (ref_id);
create index idx_ref_to_ref_idx on o_selectus_reference_to_app (fk_reference_id);

alter table o_selectus_decision_rubric_def add constraint rubric_def_to_position_ref foreign key (fk_position_id) references o_selectus_position (pos_id);
create index idx_rubric_def_to_position_idx on o_selectus_decision_rubric_def (fk_position_id);

alter table o_selectus_decision_rubric add constraint rubric_to_def_ref foreign key (fk_definition_id) references o_selectus_decision_rubric_def (def_id);
create index idx_rubric_to_def_idx on o_selectus_decision_rubric (fk_definition_id);
alter table o_selectus_decision_rubric add constraint rubric_to_app_ref foreign key (fk_application_id) references o_selectus_application (app_id);
create index idx_rubric_to_app_idx on o_selectus_decision_rubric (fk_application_id);

alter table o_selectus_org_unit add constraint selectus_ounit_to_org_idx foreign key (fk_organisation_id) references o_org_organisation (id);
create index idx_selectus_ounit_to_org_idx on o_selectus_org_unit (fk_organisation_id);

alter table o_selectus_position add constraint position_org_unit_idx foreign key (fk_org_unit_id) references o_selectus_org_unit (id);
create index idx_position_org_unit_idx on o_selectus_position (fk_org_unit_id);

alter table o_selectus_review_element_def add constraint rev_el_pos_idx foreign key (fk_pos_rev_id) references o_selectus_review_position_def (id);
create index idx_rev_el_pos_idx on o_selectus_review_element_def (fk_pos_rev_id);

alter table o_selectus_review_response add constraint rev_response_id_idx foreign key (fk_reviewer_id) references o_bs_identity (id);
create index idx_rev_response_id_idx on o_selectus_review_response (fk_reviewer_id);
alter table o_selectus_review_response add constraint rev_response_el_idx foreign key (fk_element_id) references o_selectus_review_element_def (id);
create index idx_rev_response_el_idx on o_selectus_review_response (fk_element_id);
alter table o_selectus_review_response add constraint rev_response_app_idx foreign key (fk_application_id) references o_selectus_application (app_id);
create index idx_rev_response_app_idx on o_selectus_review_response (fk_application_id);

alter table o_selectus_app_comment add constraint app_com_author_id_idx foreign key (fk_author_id) references o_bs_identity (id);
create index idx_app_com_author_id_idx on o_selectus_app_comment (fk_author_id);
alter table o_selectus_app_comment add constraint app_com_reviewer_id_idx foreign key (fk_reviewer_id) references o_bs_identity (id);
create index idx_app_com_reviewer_id_idx on o_selectus_app_comment (fk_reviewer_id);
alter table o_selectus_app_comment add constraint app_comment_app_idx foreign key (fk_application_id) references o_selectus_application (app_id);
create index idx_app_comment_app_idx on o_selectus_app_comment (fk_application_id);
alter table o_selectus_app_comment add constraint app_com_app_com_idx foreign key (fk_app_comment_id) references o_selectus_app_comment (id);
create index idx_app_com_app_com_idx on o_selectus_app_comment (fk_app_comment_id);

alter table o_selectus_app_comment_vote add constraint app_com_voter_id_idx foreign key (fk_voter_id) references o_bs_identity (id);
create index idx_app_com_voter_id_idx on o_selectus_app_comment_vote (fk_voter_id);
alter table o_selectus_app_comment_vote add constraint vote_app_com_idx foreign key (fk_app_comment_id) references o_selectus_app_comment (id);
create index idx_vote_app_com_idx on o_selectus_app_comment_vote (fk_app_comment_id);

alter table o_selectus_app_category add constraint app_cat_app_idx foreign key (fk_application_id) references o_selectus_application (app_id);
create index idx_app_cat_app_idx on o_selectus_app_category (fk_application_id);
alter table o_selectus_app_category add constraint app_cat_cat_idx foreign key (fk_category_id) references o_selectus_category (id);
create index idx_app_cat_cat_idx on o_selectus_app_category (fk_category_id);
alter table o_selectus_category add constraint cat_position_idx foreign key (fk_position_id) references o_selectus_position (pos_id);
create index idx_cat_position_idx on o_selectus_category (fk_position_id);

alter table o_selectus_assignment add constraint assign_app_idx foreign key (fk_application_id) references o_selectus_application (app_id);
create index idx_assign_app_idx on o_selectus_assignment (fk_application_id);

alter table o_selectus_assignment add constraint assign_assignee_idx foreign key (fk_assignee_id) references o_bs_identity (id);
create index idx_assign_assignee_idx on o_selectus_assignment (fk_assignee_id);

alter table o_selectus_public_feedback add constraint pfeedback_app_idx foreign key (fk_application_id) references o_selectus_application (app_id);
create index idx_pfeedback_app_idx on o_selectus_public_feedback (fk_application_id);

create index pfeedback_mail_idx on o_selectus_public_feedback (r_email);

alter table o_selectus_apps_feedback add constraint appsfeedback_pos_idx foreign key (fk_position_id) references o_selectus_position (pos_id);
create index idx_appsfeedback_pos_idx on o_selectus_apps_feedback (fk_position_id);

alter table o_selectus_app_feedback add constraint appfeedback_config_idx foreign key (fk_config_id) references o_selectus_apps_feedback (id);
create index idx_appfeedback_config_idx on o_selectus_app_feedback (fk_config_id);
alter table o_selectus_app_feedback add constraint appfeedback_app_idx foreign key (fk_application_id) references o_selectus_application (app_id);
create index idx_appfeedback_app_idx on o_selectus_app_feedback (fk_application_id);
alter table o_selectus_app_feedback add constraint appfeedback_id_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_appfeedback_id_idx on o_selectus_app_feedback (fk_identity_id);

-- log
create index idx_audit_to_app_idx on o_selectus_audit_log (fk_application_id);
alter table o_selectus_audit_log add constraint audit_to_to_pos_idx foreign key (fk_position_id) references o_selectus_position (pos_id);
create index idx_audit_to_to_pos_idx on o_selectus_audit_log (fk_position_id);

alter table o_selectus_audit_log_read add constraint log_read_id_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_log_read_id_idx on o_selectus_audit_log_read (fk_identity_id);
alter table o_selectus_audit_log_read add constraint log_read_log_idx foreign key (fk_audit_log_id) references o_selectus_audit_log (id);
create index idx_log_read_log_idx on o_selectus_audit_log_read (fk_audit_log_id);

alter table o_selectus_audit_log_usettings add constraint log_settings_id_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_log_settings_id_idx on o_selectus_audit_log_usettings (fk_identity_id);

alter table o_selectus_audit_log_u_notifs add constraint user_notifs_id_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_user_notifs_id_idx on o_selectus_audit_log_u_notifs (fk_identity_id);



