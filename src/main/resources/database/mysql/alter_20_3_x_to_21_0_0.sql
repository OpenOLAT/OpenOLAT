-- AI
create table o_ai_usage_log (
   id bigint auto_increment,
   creationdate timestamp not null,
   a_usage_context_type varchar(128),
   a_usage_context_id varchar(36),
   a_resource_type varchar(128),
   a_resource_id bigint,
   a_resource_sub_id varchar(128),
   fk_identity bigint,
   a_locale varchar(32),
   a_ai_feature varchar(128) not null,
   a_duration_millis bigint,
   a_status varchar(32) not null,
   a_error_code varchar(128),
   a_error_message mediumtext,
   a_model_provider varchar(64),
   a_req_model varchar(128),
   a_req_temperature decimal(6,4),
   a_req_top_p decimal(6,4),
   a_req_max_output_tokens bigint,
   a_invocation_id varchar(36),
   a_service_interface varchar(255),
   a_service_method varchar(255),
   a_resp_id varchar(255),
   a_resp_model varchar(128),
   a_resp_finish_reason varchar(64),
   a_input_tokens bigint,
   a_output_tokens bigint,
   a_total_tokens bigint,
   a_cached_input_tokens bigint,
   a_reasoning_tokens bigint,
   a_req_num_messages bigint,
   a_req_text_length bigint,
   a_cache_creation_input_tokens bigint,
   primary key (id)
);
create index idx_ai_log_creation_idx on o_ai_usage_log (creationdate);

-- ac offer indexes
drop index idx_offer_guest_idx on o_ac_offer;
drop index idx_offer_open_idx on o_ac_offer;
create index idx_offer_catalog_guest_idx on o_ac_offer (is_valid, guest_access, catalog_web_publish, fk_resource_id);
create index idx_offer_catalog_open_idx on o_ac_offer (is_valid, open_access, catalog_web_publish, fk_resource_id);
create index idx_rel_oto_org_offer_idx on o_ac_offer_to_organisation (fk_organisation, fk_offer);
create index idx_offeracc_offer_method_idx on o_ac_offer_access (fk_offer_id, fk_method_id);

-- ac cancelling enabled
alter table o_ac_offer add column cancelling_enabled bool default true not null;
alter table o_ac_order_part add column cancelling_enabled bool default true not null;
alter table o_ac_order_line add column cancelling_enabled bool default true not null;
-- ac offer valid config
alter table o_ac_offer add column valid_status varchar(255);
alter table o_ac_offer add column valid_date_config varchar(2000);

-- Learning resources
alter table o_repositoryentry add column finished_access varchar(32);

-- To-dos
alter table o_todo_task add column t_relative_dates varchar(2000);

-- Room management
create table o_rm_location (
    id bigint not null auto_increment,
    creationdate datetime not null,
    lastmodified datetime not null,
    r_status varchar(16) not null default 'active',
    r_name varchar(255) not null,
    r_ext_id varchar(64),
    r_ext_ref varchar(255),
    r_description text,
    r_address varchar(1024),
    r_info_url varchar(1024),
    r_geo_lat decimal(10,7),
    r_geo_lon decimal(10,7),
    primary key (id)
);
alter table o_rm_location ENGINE = InnoDB;

create unique index idx_rm_loc_ext_id on o_rm_location (r_ext_id);

create table o_rm_location_to_org (
    id bigint not null auto_increment,
    creationdate datetime not null,
    fk_location bigint not null,
    fk_organisation bigint not null,
    primary key (id)
);
alter table o_rm_location_to_org ENGINE = InnoDB;

create unique index idx_rm_loc_org on o_rm_location_to_org (fk_location, fk_organisation);
alter table o_rm_location_to_org add constraint rm_loc_to_loc_idx foreign key (fk_location) references o_rm_location(id);
alter table o_rm_location_to_org add constraint rm_loc_to_org_idx foreign key (fk_organisation) references o_org_organisation(id);

create table o_rm_room (
    id bigint not null auto_increment,
    creationdate datetime not null,
    lastmodified datetime not null,
    r_status varchar(16) not null default 'active',
    r_name varchar(255) not null,
    r_ext_id varchar(64),
    r_ext_ref varchar(255),
    r_description text,
    r_seats integer,
    r_admin_info text,
    fk_location bigint not null,
    primary key (id)
);
alter table o_rm_room ENGINE = InnoDB;

create unique index idx_rm_room_ext_id on o_rm_room (r_ext_id);
alter table o_rm_room add constraint rm_room_to_loc_idx foreign key (fk_location) references o_rm_location(id);
create index idx_rm_room_loc on o_rm_room(fk_location);

create table o_rm_room_booking (
    id bigint not null auto_increment,
    creationdate datetime not null,
    lastmodified datetime not null,
    r_start_date datetime not null,
    r_end_date datetime not null,
    r_buffer_before integer not null default 0,
    r_buffer_after integer not null default 0,
    fk_room bigint not null,
    fk_lecture_block bigint not null,
    primary key (id)
);
alter table o_rm_room_booking ENGINE = InnoDB;

create unique index idx_rm_booking_block_room on o_rm_room_booking (fk_lecture_block, fk_room);
alter table o_rm_room_booking add constraint rm_book_to_room_idx foreign key (fk_room) references o_rm_room(id);
alter table o_rm_room_booking add constraint rm_book_to_lb_idx foreign key (fk_lecture_block) references o_lecture_block(id);
create index idx_rm_book_room_time on o_rm_room_booking(fk_room, r_start_date, r_end_date);
create index idx_rm_book_lb on o_rm_room_booking(fk_lecture_block);

create table o_rm_module_log (
    id bigint not null auto_increment,
    creationdate datetime not null,
    r_action varchar(64) not null,
    r_before mediumtext,
    r_before_status varchar(64),
    r_after mediumtext,
    r_after_status varchar(64),
    fk_doer bigint,
    fk_location bigint,
    fk_room bigint,
    fk_booking bigint,
    fk_lecture_block bigint,
    primary key (id)
);
alter table o_rm_module_log ENGINE = InnoDB;

alter table o_rm_module_log add constraint rm_log_to_doer_idx foreign key (fk_doer) references o_bs_identity(id);
alter table o_rm_module_log add constraint rm_log_to_loc_idx foreign key (fk_location) references o_rm_location(id);
alter table o_rm_module_log add constraint rm_log_to_room_idx foreign key (fk_room) references o_rm_room(id);
alter table o_rm_module_log add constraint rm_log_to_book_idx foreign key (fk_booking) references o_rm_room_booking(id);
alter table o_rm_module_log add constraint rm_log_to_lb_idx foreign key (fk_lecture_block) references o_lecture_block(id);
create index idx_rm_log_room_date on o_rm_module_log(fk_room, creationdate);


-- Selectus
alter table o_user add column u_title varchar(255);

create table o_selectus_position (
   pos_id bigint not null,
   version mediumint not null,
   creationdate datetime default null,
   status varchar(32) not null,
   is_valid bool,
   planingsnumber varchar(32) default null,
   positiontitle varchar(255) default null,
   positiontitlede varchar(255) default null,
   positiontitlefr varchar(255) default null,
   shorttitle varchar(255) default null,
   shorttitlede varchar(255) default null,
   shorttitlefr varchar(255) default null,
   descr mediumtext default null,
   descrde mediumtext default null,
   descrfr mediumtext default null,
   messagecommittee mediumtext default null,
   availablelanguages varchar(64) default null,
   department varchar(255) default null,
   departmentde varchar(255) default null,
   departmentfr varchar(255) default null,
   homepage text(255) default null,
   availabledocs text(1024) default null,
   mandatorydocs text(1024) default null,
   staffdocs text(1024),
   combineddocs text(1024),
   docsizes text(1024),
   docsnames text(4000),
   docsnamesde text(4000),
   docsnamesfr text(4000),
   docsexplain text,
   docsexplainde text,
   docsexplainfr text,
   docspdfs varchar(4000) default 'all',
   docsdocx text(4000) default null,
   docsxlsx text(4000) default null,
   docsjpg text(4000) default null,
   applicationdeadline datetime default null,
   professorship varchar(32) DEFAULT 'any',
   ratingdeadline datetime default null,
   committeereminder datetime default null,
   committeeremindersent datetime default null,
   jobads text(1024),
   decisiontool bool default false,
   decisiontoollimit bigint default 0,
   applicationproject bool default false,
   academicalbackground bool default true,
   advertised bool default true not null,
   memo mediumtext default null,
   mailsetting text(32) default null,
   mailsender text(255) default null,
   mailbcc text(255) default null,
   position_duplicate_app_allowed text(32) default null,
   committee_mail_template mediumtext default null,
   committee_mail_letter mediumtext default null,
   conf_mail_template mediumtext default null,
   conf_mail_template_de mediumtext default null,
   conf_mail_template_fr mediumtext default null,
   conf_mail_letter mediumtext default null,
   conf_mail_template_mgmt mediumtext default null,
   conf_mail_template_mgmt_de mediumtext default null,
   conf_mail_template_mgmt_fr mediumtext default null,
   conf_mail_letter_mgmt mediumtext default null,
   confdup_mail_template mediumtext default null,
   confdup_mail_template_de mediumtext default null,
   confdup_mail_template_fr mediumtext default null,
   confdup_mail_letter mediumtext default null,
   tabsconfiguration text,
   customtabs varchar(255),
   rec_excluded_attributes text(4000) default null,
   rec_expert_enable boolean default false,
   rec_expert_deadline datetime,
   rec_expert_mail_template mediumtext default null,
   rec_expert_mail_letter mediumtext default null,
   rec_expert_docs varchar(2000) default 'all',
   rec_expert_fields text(32000),
   rec_referee_enable boolean default false,
   rec_referee_min bigint,
   rec_referee_max bigint,
   rec_referee_mail_type varchar(32),
   rec_referee_deadline datetime,
   rec_referee_mail_template mediumtext default null,
   rec_referee_mail_letter mediumtext default null,
   rec_referee_docs varchar(2000) default 'all',
   rec_referee_fields text(32000),
   rec_comp_expert_enable boolean default false,
   rec_comp_expert_deadline datetime,
   rec_comp_expert_mail_template mediumtext default null,
   rec_comp_expert_docs varchar(2000) default 'all',
   rec_comp_expert_mail_letter mediumtext default null,
   rec_comp_expert_fields text(32000),
   rec_public_feedback_enable bool default false,
   rec_public_feedback_deadline datetime default null,
   rec_app_referee_mgt_deadline datetime default null,
   rec_app_referee_mgt_enable boolean default false,
   committeecomment_enable bool default false,
   committeecomment_visibility varchar(64),
   review_enable bool default false,
   system_tags_enable bool default false,
   position_tags_enable bool default false,
   fk_review_definition_id bigint default null,
   fk_doc_1_id bigint default null,
   fk_doc_2_id bigint default null,
   fk_doc_3_id bigint default null,
   fk_committeegroup bigint default null,
   fk_committeeheadgroup bigint default null,
   fk_secretarygroup bigint default null,
   fk_exofficiogroup bigint default null,
   rating_policy_link_url_1 text(255),
   rating_policy_link_url_2 text(255),
   rating_policy_link_url_3 text(255),
   rating_policy_link_url_4 text(255),
   rating_policy_link_label_1 text(1024),
   rating_policy_link_label_2 text(1024),
   rating_policy_link_label_3 text(1024),
   rating_policy_link_label_4 text(1024),
   fk_org_unit_id bigint default null,
   fk_organisation_id bigint default null,
   PRIMARY KEY (pos_id)
);

create table o_selectus_pos_attribute_def (
   id bigint not null,
   lastmodified datetime,
   creationdate datetime,
   pos bigint,
   orderpos bigint default 0,
   label varchar(255),
   labelde varchar(255),
   labelfr varchar(255),
   placeholder varchar(255),
   placeholderde varchar(255),
   placeholderfr varchar(255),
   applicationtab varchar(32),
   attributetype varchar(2000),
   attributemandatory bool default false,
   attributeconfiguration text,
   fk_position_id bigint,
   primary key (id)
);

create table o_selectus_application (
   app_id bigint not null,
   version mediumint not null,
   creationdate datetime default null,
   last_modified datetime default null,
   application_lid bigint DEFAULT 0,
   applanguage varchar(16) default null,
   is_valid bool,
   firstname varchar(255) default null,
   lastname varchar(255) default null,
   persontitle varchar(64) default null,
   academictitle varchar(255) default null,
   gender varchar(32) default null,
   marital_status varchar(32) default null,
   disability bool default false,
   birthday date default null,
   nationality varchar(64) default null,
   addnationalities varchar(2000) default null,
   mail varchar(64) default null,
   phone varchar(64) default null,
   mobile_phone varchar(64) default null,
   addressline1 varchar(255) default null,
   addressline2 varchar(255) default null,
   addressline3 varchar(255) default null,
   addr_type varchar(32),
   organization varchar(255),
   unit varchar(255),
   zipcode varchar(64) default null,
   city varchar(64) default null,
   country varchar(64) default null,
   biz_addressline1 varchar(255) default null,
   biz_addressline2 varchar(255) default null,
   biz_addressline3 varchar(255) default null,
   biz_zipcode varchar(255) default null,
   biz_city varchar(255) default null,
   biz_country varchar(255) default null,
   biz_phone varchar(255) default null,
   biz_mail varchar(255) default null,
   affiliation varchar(255) default null,
   currentposition varchar(255) default null,
   originalpublications bigint default null,
   firstauthorships bigint default null,
   lastauthorships bigint default null,
   citations bigint default null,
   impactfactor decimal default null,
   hfactor decimal default null,
   highestdegreetype text(255) default null,
   highestdegreedescr text(1024) default null,
   highestdegreeinstitution text(255) default null,
   highestdegreedate datetime default null,
   highestdegreeworkedsince text(1024) default null,
   workedoutacademia text(1024) default null,
   workedoutacademiacare text(1024) default null,
   careerdescription mediumtext default null,
   dissertationtitle text(255) default null,
   dissertationinstitution text(255) default null,
   dissertationdate datetime default null,
   dissertationkeyword1 text(1024) default null,
   dissertationkeyword2 text(1024) default null,
   dissertationkeyword3 text(1024) default null,
   habilitationtitle text(255) default null,
   habilitationinstitution text(255) default null,
   habilitationdate datetime default null,
   orcid varchar(255) default null,
   projecttitle text(1024) default null,
   projectfinancialimpact varchar(1024) default null,
   projectfinancialimpact_2 varchar(1024) default null,
   projectfinancialimpact_3 varchar(1024) default null,
   projectfinancialimpact_4 varchar(1024) default null,
   projectfinancialimpact_5 varchar(1024) default null,
   projectstartdate datetime default null,
   projectduration text(1024) default null,
   projectdescription mediumtext default null,
   acronym text(1024) default null,
   keywords text(1024) default null,
   disciplines text(1024) default null,
   public_feedback_enable bool default false,
   public_feedback_deadline datetime default null,
   public_feedback_key varchar(64) default null,
   after_position_closed bool default false,
   withdrawn bool DEFAULT false,
   withdrawn_date datetime default null,
   status varchar(16) not null default 'active',
   onhold_date datetime,
   rejected_date datetime,
   noteligible_date datetime,
   granted_date datetime,
   hired_date datetime,
   status_comment text,
   acceptterms bool default null,
   jobad varchar(255),
   expertblacklist text(4000) default null,
   expertconsent bool default null,
   memo mediumtext default null,
   committeecomment mediumtext default null,
   decision int4,
   applicanturl varchar(255) default null,
   report_ratings_2 bigint default null,
   report_ratings_1 bigint default null,
   report_ratings_0 bigint default null,
   report_ratings_abstentions bigint default null,
   report_experts bigint default null,
   report_experts_letters bigint default null,
   report_referees bigint default null,
   report_referees_letters bigint default null,
   report_comp_experts bigint default null,
   report_comp_experts_letters bigint default null,
   fk_identity_id bigint default null,
   fk_coveringletter_id bigint default null,
   fk_curriculumvitae_id bigint default null,
   fk_publicationlist_id bigint default null,
   fk_researchstatement_id bigint default null,
   fk_teachingstatement_id bigint default null,
   fk_leadership_id bigint default null,
   fk_listofreferees_id bigint default null,
   fk_projectlist_id bigint default null,
   fk_referenceletters_id bigint default null,
   fk_teachingassessment_id bigint default null,
   fk_certificateofstudy_id bigint default null,
   fk_degreecertificates_id bigint default null,
   fk_dissertation_id bigint default null,
   fk_habilitation_id bigint default null,
   fk_clinicaldisciplines_id bigint default null,
   fk_surgicaldisciplines_id bigint default null,
   fk_reprints_id bigint default null,
   fk_externalfunding_id bigint default null,
   fk_publication1_id bigint default null,
   fk_publication2_id bigint default null,
   fk_publication3_id bigint default null,
   fk_publication4_id bigint default null,
   fk_publication5_id bigint default null,
   fk_otherdocument_id bigint default null,
   fk_position_id bigint,
   fk_combineddocument_id bigint default null,
   PRIMARY KEY (app_id)
);

create table o_selectus_app_attribute (
   id bigint not null,
   lastmodified datetime,
   creationdate datetime,
   attrvalue varchar(4000),
   fk_definition_id bigint not null,
   fk_application_id bigint,
   fk_position_id bigint,
   primary key (id)
);

create table o_selectus_attachment (
   attachment_id bigint not null,
   version mediumint not null,
   creationdate date default null,
   file_name varchar(255),
   file_type varchar(8) default 'pdf',
   datas longblob,
   datas_size int4,
   PRIMARY KEY (attachment_id)
);

create table o_selectus_committee_report (
   id bigint not null,
   creationdate datetime default null,
   lastmodified datetime default null,
   r_role varchar(255),
   r_ratings_rights varchar(255),
   r_gender varchar(255),
   r_user_classification varchar(255),
   r_num_ratings_a bigint default null,
   r_num_ratings_b bigint default null,
   r_num_ratings_c bigint default null,
   r_num_abstentions bigint default null,
   fk_position_id bigint not null,
   PRIMARY KEY (id)
);

create table o_selectus_position_policy (
  pol_id bigint not null,
  version mediumint not null,
  creationdate datetime default null,
  dont_show_next_time bool default false,
  policytype varchar(32) default null,
  fk_position_id bigint default null,
  fk_identity_id bigint default null,
  primary key (pol_id)
);

create table o_selectus_mail_template (
   id bigint not null,
   lastmodified datetime,
   creationdate datetime,
   f_id varchar(255),
   f_name varchar(255),
   f_subject varchar(255),
   f_subject_de varchar(255),
   f_subject_fr varchar(255),
   f_body text,
   f_body_de text,
   f_body_fr text,
   f_letter text,
   fk_position_id bigint,
   primary key (id)
);

create table o_selectus_application_notes (
  notes_id bigint not null,
  version mediumint not null,
  creationdate datetime default null,
  notes_content varchar(4000),
  fk_application_id bigint default null,
  fk_identity_id bigint default null,
  primary key (notes_id)
);

create table o_selectus_public_feedback (
   id bigint not null,
   creationdate datetime default null,
   last_modified datetime default null,
   r_firstname varchar(256),
   r_lastname varchar(256),
   r_email varchar(256),
   r_external_id varchar(256),
   r_external_ref varchar(256),
   r_comment text,
   fk_application_id bigint not null,
   PRIMARY KEY (id)
);

create table o_selectus_apps_feedback (
   id bigint not null,
   creationdate datetime default null,
   last_modified datetime default null,
   r_name varchar(128),
   r_enabled bool default false not null,
   r_deadline datetime,
   r_mail_template mediumtext default null,
   r_mail_letter mediumtext default null,
   r_docs varchar(2000) default 'all',
   r_experts_docs bool default false not null,
   r_referees_docs bool default false not null,
   r_experts_comp_assessment_docs bool default false not null,
   r_fields text(32000),
   fk_position_id bigint not null,
   PRIMARY KEY (id)
);

create table o_selectus_app_feedback (
   id bigint not null,
   creationdate datetime default null,
   last_modified datetime default null,
   r_comment text,
   r_comment_date datetime,
   r_deadline datetime,
   r_status varchar(32) default 'notSent' not null,
   r_request_date datetime,
   r_last_reminder_date datetime,
   fk_config_id bigint not null,
   fk_application_id bigint not null,
   fk_identity_id bigint not null,
   PRIMARY KEY (id)
);

create table o_selectus_rejection_email_log (
  log_id bigint not null,
  version mediumint not null,
  status int4 not null,
  creationdate datetime default null,
  mail_template varchar(32) default null,
  mail_subject varchar(1024) default null,
  mail_content mediumtext default null,
  mail_rejection bool default false,
  fk_application_id bigint default null,
  fk_letter_id bigint default null,
  primary key (log_id)
);

create table o_selectus_reference (
   ref_id bigint not null,
   lastmodified datetime,
   creationdate datetime,
   version mediumint not null,
   firstname varchar(255),
   lastname varchar(255),
   title varchar(255),
   institution varchar(255),
   mail varchar(255),
   disclaimer boolean default false,
   privacydisclaimer boolean default false,
   submissionurl varchar(64),
   submissiondate datetime,
   submissiondeadline datetime,
   reftype varchar(32),
   requeststatus varchar(32) default 'notAnswered',
   adminnote text,
   dateconsent datetime,
   consentbystaff bool default null,
   status varchar(32),
   dateinvitation datetime,
   datelastreminder datetime,
   remindersbyapplicant bigint default 0,
   fk_letter_id bigint,
   fk_application_id bigint,
   primary key (ref_id)
);

create table o_selectus_reference_comment (
   id bigint not null,
   creationdate datetime default null,
   r_comment text,
   fk_reference_id bigint not null,
   PRIMARY KEY (id)
);

create table o_selectus_reference_to_app (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_reference_id bigint not null,
   fk_application_id bigint not null,
   PRIMARY KEY (id)
);

create table o_selectus_decision_rubric_def (
   def_id bigint not null,
   creationdate datetime,
   lastmodified datetime,
   d_pos bigint default 0 not null,
   d_rubric varchar(128) not null,
   d_type varchar(32) not null,
   d_sum bool default false not null,
   d_weight bigint default 10 not null,
   fk_position_id bigint not null,
   primary key (def_id)
);

create table o_selectus_decision_rubric (
   rubric_id bigint not null,
   lastmodified datetime,
   creationdate datetime,
   d_string_value varchar(1024),
   d_integer_value bigint,
   fk_definition_id bigint not null,
   fk_application_id bigint not null,
   primary key (rubric_id)
);

create table o_selectus_review_position_def (
   id bigint not null,
   lastmodified datetime,
   creationdate datetime,
   review_comment_enable bool default false,
   r_rev_name_visibility varchar(16),
   r_rev_visibility varchar(16),
   r_rev_visibility_head varchar(16),
   r_rev_visibility_secretary varchar(16),
   r_rev_visibility_exofficio varchar(16),
   r_rev_fill varchar(16) default 'fill',
   r_rev_fill_head varchar(16) default 'no',
   r_rev_fill_secretary varchar(16) default 'no',
   r_rev_fill_exofficio varchar(16) default 'no',
   r_slider_steps bigint default null,
   r_slider_left_label varchar(255),
   r_slider_right_label varchar(255),
   r_rev_statistics_enable bool default null,
   r_rev_chart_enable bool default null,
   primary key (id)
);

create table o_selectus_review_element_def (
   id bigint not null,
   lastmodified datetime,
   creationdate datetime,
   pos bigint default null,
   r_type varchar(16),
   r_label varchar(255),
   fk_pos_rev_id bigint not null,
   primary key (id)
);

create table o_selectus_review_response (
   id bigint not null,
   lastmodified datetime,
   creationdate datetime,
   r_string_value varchar(4000),
   r_integer_value bigint,
   fk_reviewer_id bigint not null,
   fk_element_id bigint not null,
   fk_application_id bigint not null,
   primary key (id)
);

create table o_selectus_app_comment (
   id bigint not null,
   lastmodified datetime,
   creationdate datetime,
   r_deleted bool default false,
   r_comment text,
   fk_author_id bigint not null,
   fk_application_id bigint not null,
   fk_reviewer_id bigint,
   fk_app_comment_id bigint,
   primary key (id)
);

create table o_selectus_app_comment_vote (
   id bigint not null,
   lastmodified datetime,
   creationdate datetime,
   r_up bool,
   fk_voter_id bigint not null,
   fk_app_comment_id bigint,
   primary key (id)
);

create table o_selectus_category (
   id bigint not null,
   lastmodified datetime,
   creationdate datetime,
   r_name varchar(255),
   r_color varchar(16),
   fk_position_id bigint,
   primary key (id)
);

create table o_selectus_app_category (
   id bigint not null,
   creationdate datetime,
   r_administrative bool default false,
   fk_application_id bigint not null,
   fk_category_id bigint not null,
   primary key (id)
);

create table o_selectus_assignment (
   id bigint not null,
   creationdate datetime,
   fk_application_id bigint,
   fk_assignee_id bigint,
   primary key (id)
);

create table o_selectus_org_unit (
   id bigint not null,
   lastmodified datetime,
   creationdate datetime,
   unitname varchar(255),
   unitnamede varchar(255),
   unitnamefr varchar(255),
   url varchar(255),
   description varchar(2000),
   systemconfig bool default true,
   staffmail varchar(255),
   staffbcc varchar(255),
   mailsignature varchar(2000),
   fk_organisation_id bigint,
   primary key (id)
);

create table o_selectus_audit_log (
   id bigint not null,
   creationdate datetime not null,
   r_action varchar(32) not null,
   r_action_target varchar(32) not null,
   r_val_before text,
   r_val_after text,
   r_message varchar(2000),
   r_message_i18n varchar(2000),
   r_message_val_1 varchar(255),
   r_message_val_2 varchar(255),
   r_message_val_3 varchar(255),
   r_message_val_4 varchar(255),
   r_message_val_5 varchar(255),
   fk_position_id bigint not null,
   fk_application_id bigint,
   fk_committee_identity_id bigint,
   fk_rating_id bigint,
   fk_comment_id bigint,
   fk_reference_id bigint,
   fk_feedback_id bigint,
   fk_identity_id bigint,
   primary key (id)
);

create table o_selectus_audit_log_read (
   id bigint not null,
   creationdate datetime not null,
   r_read bool default true not null,
   fk_identity_id bigint not null,
   fk_audit_log_id bigint not null,
   primary key (id)
);

create table o_selectus_audit_log_usettings (
   id bigint not null,
   creationdate datetime,
   lastmodified datetime,
   r_enabled bool default false not null,
   r_interval varchar(16) default 'never' not null,
   fk_identity_id bigint not null,
   primary key (id)
);

create table o_selectus_audit_log_u_notifs (
   id bigint not null,
   creationdate datetime,
   r_last_mail_date datetime,
   fk_identity_id bigint not null,
   primary key (id)
);

alter table o_selectus_position ENGINE = InnoDB;
alter table o_selectus_pos_attribute_def ENGINE = InnoDB;
alter table o_selectus_application ENGINE = InnoDB;
alter table o_selectus_app_attribute ENGINE = InnoDB;
alter table o_selectus_attachment ENGINE = InnoDB;
alter table o_selectus_committee_report ENGINE = InnoDB;
alter table o_selectus_position_policy ENGINE = InnoDB;
alter table o_selectus_mail_template ENGINE = InnoDB;
alter table o_selectus_application_notes ENGINE = InnoDB;
alter table o_selectus_public_feedback ENGINE = InnoDB;
alter table o_selectus_apps_feedback ENGINE = InnoDB;
alter table o_selectus_app_feedback ENGINE = InnoDB;
alter table o_selectus_rejection_email_log ENGINE = InnoDB;
alter table o_selectus_reference ENGINE = InnoDB;
alter table o_selectus_reference_comment ENGINE = InnoDB;
alter table o_selectus_reference_to_app ENGINE = InnoDB;
alter table o_selectus_decision_rubric_def ENGINE = InnoDB;
alter table o_selectus_decision_rubric ENGINE = InnoDB;
alter table o_selectus_review_position_def ENGINE = InnoDB;
alter table o_selectus_review_element_def ENGINE = InnoDB;
alter table o_selectus_review_response ENGINE = InnoDB;
alter table o_selectus_app_comment ENGINE = InnoDB;
alter table o_selectus_app_comment_vote ENGINE = InnoDB;
alter table o_selectus_category ENGINE = InnoDB;
alter table o_selectus_app_category ENGINE = InnoDB;
alter table o_selectus_assignment ENGINE = InnoDB;
alter table o_selectus_org_unit ENGINE = InnoDB;
alter table o_selectus_audit_log ENGINE = InnoDB;
alter table o_selectus_audit_log_read ENGINE = InnoDB;
alter table o_selectus_audit_log_usettings ENGINE = InnoDB;
alter table o_selectus_audit_log_u_notifs ENGINE = InnoDB;

-- selectus
alter table o_selectus_application add constraint posid_to_app foreign key (fk_position_id) references o_selectus_position (pos_id);
alter table o_selectus_application add constraint att_coveringid_to_app foreign key (fk_coveringletter_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_cvid_to_app foreign key (fk_curriculumvitae_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_publicationid_to_app foreign key (fk_publicationlist_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_researchid_to_app foreign key (fk_researchstatement_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_teachingid_to_app foreign key (fk_teachingstatement_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_leadershipid_to_app foreign key (fk_leadership_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_refereesid_to_app foreign key (fk_listofreferees_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_projectid_to_app foreign key (fk_projectlist_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_referenceid_to_app foreign key (fk_referenceletters_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_teachassessment_to_app foreign key (fk_teachingassessment_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_certofstudy_to_app foreign key (fk_certificateofstudy_id) references o_selectus_attachment (attachment_id);
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
alter table o_selectus_application add constraint att_otherid_to_app foreign key (fk_otherdocument_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_application add constraint att_combined_to_app foreign key (fk_combineddocument_id) references o_selectus_attachment (attachment_id);

alter table o_selectus_application add constraint app_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);

alter table o_selectus_app_attribute add constraint attr_to_attrdef foreign key (fk_definition_id) references o_selectus_pos_attribute_def (id);
alter table o_selectus_app_attribute add constraint attr_to_app foreign key (fk_application_id) references o_selectus_application (app_id);
alter table o_selectus_app_attribute add constraint attr_to_gpos_idx foreign key (fk_position_id) references o_selectus_position (pos_id);

alter table o_selectus_assignment add constraint assign_app_idx foreign key (fk_application_id) references o_selectus_application (app_id);
alter table o_selectus_assignment add constraint assign_assignee_idx foreign key (fk_assignee_id) references o_bs_identity (id);

alter table o_selectus_public_feedback add constraint pfeedback_app_idx foreign key (fk_application_id) references o_selectus_application (app_id);

create index pfeedback_mail_idx on o_selectus_public_feedback (r_email);

alter table o_selectus_apps_feedback add constraint appsfeedback_pos_idx foreign key (fk_position_id) references o_selectus_position (pos_id);

alter table o_selectus_app_feedback add constraint appfeedback_config_idx foreign key (fk_config_id) references o_selectus_apps_feedback (id);
alter table o_selectus_app_feedback add constraint appfeedback_app_idx foreign key (fk_application_id) references o_selectus_application (app_id);
alter table o_selectus_app_feedback add constraint appfeedback_id_idx foreign key (fk_identity_id) references o_bs_identity (id);

alter table o_selectus_committee_report add constraint com_rep_to_pos_idx foreign key (fk_position_id) references o_selectus_position (pos_id);

-- position
alter table o_selectus_position add constraint att_doc_1_to_att foreign key (fk_doc_1_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_position add constraint att_doc_2_to_att foreign key (fk_doc_2_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_position add constraint att_doc_3_to_att foreign key (fk_doc_3_id) references o_selectus_attachment (attachment_id);

alter table o_selectus_position add constraint pos_review_def_idx foreign key (fk_review_definition_id) references o_selectus_review_position_def (id);

alter table o_selectus_position add constraint selectus_pos_to_org_idx foreign key (fk_organisation_id) references o_org_organisation (id);

alter table o_selectus_position_policy add constraint policy_to_pos foreign key (fk_position_id) references o_selectus_position (pos_id);
alter table o_selectus_position_policy add constraint policy_to_identity foreign key (fk_identity_id) references o_bs_identity (id);

alter table o_selectus_mail_template add constraint mtemplate_pos_idx foreign key (fk_position_id) references o_selectus_position (pos_id);

alter table o_selectus_pos_attribute_def add constraint posattrdef_to_pos foreign key (fk_position_id) references o_selectus_position (pos_id);

alter table o_selectus_application_notes add constraint app_notes_to_app foreign key (fk_application_id) references o_selectus_application (app_id);
alter table o_selectus_application_notes add constraint app_notes_to_identity foreign key (fk_identity_id) references o_bs_identity (id);

alter table o_selectus_rejection_email_log  add constraint rejection_to_app foreign key (fk_application_id) references o_selectus_application (app_id);
alter table o_selectus_rejection_email_log add constraint att_letter_to_maillog_idx foreign key (fk_letter_id) references o_selectus_attachment (attachment_id);

alter table o_selectus_reference add constraint att_letter_to_ref foreign key (fk_letter_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_reference add constraint application_to_ref foreign key (fk_application_id) references o_selectus_application (app_id);

alter table o_selectus_reference_comment add constraint ref_comm_to_ref_idx foreign key (fk_reference_id) references o_selectus_reference (ref_id);

alter table o_selectus_reference_to_app add constraint ref_to_app_idx foreign key (fk_application_id) references o_selectus_application (app_id);
alter table o_selectus_reference_to_app add constraint ref_to_ref_idx foreign key (fk_reference_id) references o_selectus_reference (ref_id);

alter table o_selectus_decision_rubric_def add constraint rubric_def_to_position_ref foreign key (fk_position_id) references o_selectus_position (pos_id);

alter table o_selectus_decision_rubric add constraint rubric_to_def_ref foreign key (fk_definition_id) references o_selectus_decision_rubric_def (def_id);
alter table o_selectus_decision_rubric add constraint rubric_to_app_ref foreign key (fk_application_id) references o_selectus_application (app_id);

alter table o_selectus_org_unit add constraint selectus_ounit_to_org_idx foreign key (fk_organisation_id) references o_org_organisation (id);

alter table o_selectus_position add constraint position_org_unit_idx foreign key (fk_org_unit_id) references o_selectus_org_unit (id);

alter table o_selectus_review_element_def add constraint rev_el_pos_idx foreign key (fk_pos_rev_id) references o_selectus_review_position_def (id);

alter table o_selectus_review_response add constraint rev_response_id_idx foreign key (fk_reviewer_id) references o_bs_identity (id);
alter table o_selectus_review_response add constraint rev_response_el_idx foreign key (fk_element_id) references o_selectus_review_element_def (id);
alter table o_selectus_review_response add constraint rev_response_app_idx foreign key (fk_application_id) references o_selectus_application (app_id);

-- comment
alter table o_selectus_app_comment add constraint app_com_author_id_idx foreign key (fk_author_id) references o_bs_identity (id);
alter table o_selectus_app_comment add constraint app_com_reviewer_id_idx foreign key (fk_reviewer_id) references o_bs_identity (id);
alter table o_selectus_app_comment add constraint app_comment_app_idx foreign key (fk_application_id) references o_selectus_application (app_id);
alter table o_selectus_app_comment add constraint app_com_app_com_idx foreign key (fk_app_comment_id) references o_selectus_app_comment (id);

alter table o_selectus_app_comment_vote add constraint app_com_voter_id_idx foreign key (fk_voter_id) references o_bs_identity (id);
alter table o_selectus_app_comment_vote add constraint vote_app_com_idx foreign key (fk_app_comment_id) references o_selectus_app_comment (id);

-- category
alter table o_selectus_app_category add constraint app_cat_app_idx foreign key (fk_application_id) references o_selectus_application (app_id);
alter table o_selectus_app_category add constraint app_cat_cat_idx foreign key (fk_category_id) references o_selectus_category (id);
alter table o_selectus_category add constraint cat_position_idx foreign key (fk_position_id) references o_selectus_position (pos_id);

-- log
create index idx_audit_to_app_idx on o_selectus_audit_log (fk_application_id);
alter table o_selectus_audit_log add constraint audit_to_to_pos_idx foreign key (fk_position_id) references o_selectus_position (pos_id);

alter table o_selectus_audit_log_read add constraint log_read_id_idx foreign key (fk_identity_id) references o_bs_identity (id);
alter table o_selectus_audit_log_read add constraint log_read_log_idx foreign key (fk_audit_log_id) references o_selectus_audit_log (id);

alter table o_selectus_audit_log_usettings add constraint log_settings_id_idx foreign key (fk_identity_id) references o_bs_identity (id);

alter table o_selectus_audit_log_u_notifs add constraint user_notifs_id_idx foreign key (fk_identity_id) references o_bs_identity (id);

