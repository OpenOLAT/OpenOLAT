-- AI
create table o_ai_usage_log (
   id bigserial,
   creationdate timestamp not null,
   a_usage_context_type varchar(128),
   a_usage_context_id varchar(36),
   a_resource_type varchar(128),
   a_resource_id int8,
   a_resource_sub_id varchar(128),
   fk_identity int8,
   a_locale varchar(32),
   a_ai_feature varchar(128) not null,
   a_duration_millis int8,
   a_status varchar(32) not null,
   a_error_code varchar(128),
   a_error_message text,
   a_model_provider varchar(64),
   a_req_model varchar(128),
   a_req_temperature decimal(6,4),
   a_req_top_p decimal(6,4),
   a_req_max_output_tokens int8,
   a_invocation_id varchar(36),
   a_service_interface varchar(255),
   a_service_method varchar(255),
   a_resp_id varchar(255),
   a_resp_model varchar(128),
   a_resp_finish_reason varchar(64),
   a_input_tokens int8,
   a_output_tokens int8,
   a_total_tokens int8,
   a_cached_input_tokens int8,
   a_reasoning_tokens int8,
   a_req_num_messages int8,
   a_req_text_length int8,
   a_cache_creation_input_tokens int8,
   primary key (id)
);
create index idx_ai_log_creation_idx on o_ai_usage_log (creationdate);

-- Taxonomy matching embeddings
create table o_tax_level_embedding (
  id bigserial primary key,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  t_text_variant varchar(16) not null,
  t_locale varchar(10) not null,
  t_embedding_text text not null,
  t_model_id varchar(128) not null,
  t_model_version varchar(64),
  t_vector_json text,
  fk_level int8 not null,
  fk_taxonomy int8 not null,
  constraint fk_tax_emb_level foreign key (fk_level)
    references o_tax_taxonomy_level(id),
  constraint fk_tax_emb_taxonomy foreign key (fk_taxonomy)
    references o_tax_taxonomy(id)
);

create index idx_tax_emb_level on o_tax_level_embedding(fk_level);
create index idx_tax_emb_taxonomy on o_tax_level_embedding(fk_taxonomy);
create unique index idx_tax_emb_unique on o_tax_level_embedding(fk_level, t_locale, t_model_id, t_text_variant);

-- Taxonomy level index state
create table o_tax_level_index_state (
  id bigserial primary key,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  t_status varchar(16) not null,
  t_attempt_count int4 not null default 0,
  t_last_error text,
  t_indexed_model_id varchar(128),
  t_indexed_model_version varchar(64),
  t_last_index_date timestamp,
  fk_level int8 not null,
  constraint fk_tax_idx_state_level foreign key (fk_level)
    references o_tax_taxonomy_level(id),
  constraint uq_tax_idx_state_level unique (fk_level)
);

create index idx_tax_lvl_idx_state_level on o_tax_level_index_state(fk_level);
create index idx_tax_lvl_idx_state_status on o_tax_level_index_state(t_status);


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
create table o_rm_building (
    id bigserial,
    creationdate timestamp not null,
    lastmodified timestamp not null,
    r_status varchar(16) not null default 'active',
    r_description varchar(255) not null,
    r_ext_id varchar(64),
    r_ext_ref varchar(255),
    r_info text,
    r_color_css varchar(255),
    r_address varchar(1024),
    r_info_url varchar(1024),
    r_geo_lat decimal(10,7),
    r_geo_lon decimal(10,7),
    primary key (id)
);

create unique index idx_rm_bld_ext_id on o_rm_building (r_ext_id);

create table o_rm_building_to_org (
    id bigserial,
    creationdate timestamp not null,
    fk_building int8 not null,
    fk_organisation int8 not null,
    primary key (id)
);

create unique index idx_rm_bld_org on o_rm_building_to_org (fk_building, fk_organisation);
alter table o_rm_building_to_org add constraint rm_bld_to_bld_idx foreign key (fk_building) references o_rm_building(id);
alter table o_rm_building_to_org add constraint rm_bld_to_org_idx foreign key (fk_organisation) references o_org_organisation(id);

create table o_rm_room (
    id bigserial,
    creationdate timestamp not null,
    lastmodified timestamp not null,
    r_status varchar(16) not null default 'active',
    r_description varchar(255) not null,
    r_ext_id varchar(64),
    r_ext_ref varchar(255),
    r_room_info text,
    r_seats integer,
    r_admin_info text,
    fk_building int8 not null,
    primary key (id)
);

create unique index idx_rm_room_ext_id on o_rm_room (r_ext_id);
alter table o_rm_room add constraint rm_room_to_bld_idx foreign key (fk_building) references o_rm_building(id);
create index idx_rm_room_bld on o_rm_room(fk_building);

create table o_rm_room_booking (
    id bigserial,
    creationdate timestamp not null,
    lastmodified timestamp not null,
    r_start_date timestamp not null,
    r_end_date timestamp not null,
    r_buffer_before integer not null default 0,
    r_buffer_after integer not null default 0,
    fk_room int8 not null,
    fk_lecture_block int8 not null,
    primary key (id)
);

create unique index idx_rm_booking_block_room on o_rm_room_booking (fk_lecture_block, fk_room);
alter table o_rm_room_booking add constraint rm_book_to_room_idx foreign key (fk_room) references o_rm_room(id);
alter table o_rm_room_booking add constraint rm_book_to_lb_idx foreign key (fk_lecture_block) references o_lecture_block(id);
create index idx_rm_book_room_time on o_rm_room_booking(fk_room, r_start_date, r_end_date);
create index idx_rm_book_lb on o_rm_room_booking(fk_lecture_block);

create table o_rm_module_log (
    id bigserial,
    creationdate timestamp not null,
    r_action varchar(64) not null,
    r_before text,
    r_before_status varchar(64),
    r_after text,
    r_after_status varchar(64),
    fk_doer int8,
    fk_building int8,
    fk_room int8,
    fk_booking int8,
    fk_lecture_block int8,
    primary key (id)
);

alter table o_rm_module_log add constraint rm_log_to_doer_idx foreign key (fk_doer) references o_bs_identity(id);
alter table o_rm_module_log add constraint rm_log_to_bld_idx foreign key (fk_building) references o_rm_building(id);
alter table o_rm_module_log add constraint rm_log_to_room_idx foreign key (fk_room) references o_rm_room(id);
alter table o_rm_module_log add constraint rm_log_to_book_idx foreign key (fk_booking) references o_rm_room_booking(id);
alter table o_rm_module_log add constraint rm_log_to_lb_idx foreign key (fk_lecture_block) references o_lecture_block(id);
create index idx_rm_log_room_date on o_rm_module_log(fk_room, creationdate);


-- AI essay correction result
create table o_ai_essay_correction (
  id                   bigserial,
  creationdate         timestamp not null,
  lastmodified         timestamp not null,
  fk_identity          int8 not null,
  a_item_session_key   int8,
  a_storage_path       varchar(1024),
  a_question_id        varchar(64),
  a_content_hash_at_call varchar(64),
  a_prompt_template_version varchar(40),
  a_tier               varchar(16),
  a_student_answer     text not null,
  a_status             varchar(24) not null,
  a_feedback_json      text,
  a_error_message      varchar(2048),
  a_completed          timestamp,
  primary key (id)
);

alter table o_ai_essay_correction add constraint ai_essay_corr_ident_fk foreign key (fk_identity) references o_bs_identity (id);
create index idx_ai_essay_corr_ident on o_ai_essay_correction (fk_identity);
create index idx_ai_essay_corr_item_session on o_ai_essay_correction (a_item_session_key);
create index idx_ai_essay_corr_question on o_ai_essay_correction (a_storage_path, a_question_id);



-- Selectus
alter table o_user add column u_title varchar(255);

create table o_selectus_position (
   pos_id int8 not null,
   version int4 not null,
   creationdate timestamp default null,
   status varchar(32) not null,
   is_valid bool,
   planingsnumber varchar(32) default null,
   positiontitle varchar(255) default null,
   positiontitlede varchar(255) default null,
   positiontitlefr varchar(255) default null,
   shorttitle varchar(255) default null,
   shorttitlede varchar(255) default null,
   shorttitlefr varchar(255) default null,
   descr text default null,
   descrde text default null,
   descrfr text default null,
   messagecommittee text default null,
   availablelanguages varchar(64) default null,
   department varchar(255) default null,
   departmentde varchar(255) default null,
   departmentfr varchar(255) default null,
   homepage varchar(255) default null,
   availabledocs varchar(1024) default null,
   mandatorydocs varchar(1024) default null,
   staffdocs varchar(1024),
   combineddocs varchar(1024),
   docsizes varchar(1024),
   docsnames varchar(4000),
   docsnamesde varchar(4000),
   docsnamesfr varchar(4000),
   docsexplain text,
   docsexplainde text,
   docsexplainfr text,
   docspdfs varchar(4000) default 'all',
   docsdocx varchar(4000) default null,
   docsxlsx varchar(4000) default null,
   docsjpg varchar(4000) default null,
   applicationdeadline timestamp default null,
   professorship varchar(32) DEFAULT 'any',
   ratingdeadline timestamp default null,
   committeereminder timestamp default null,
   committeeremindersent timestamp default null,
   jobads varchar(1024),
   decisiontool bool default false,
   decisiontoollimit int8 default 0,
   applicationproject bool default false,
   academicalbackground bool default true,
   advertised bool default true not null,
   memo text default null,
   mailsetting varchar(32) default null,
   mailsender varchar(255) default null,
   mailbcc varchar(255) default null,
   position_duplicate_app_allowed varchar(32) default null,
   committee_mail_subject varchar(255) default null,
   committee_mail_template text default null,
   committee_mail_letter text default null,
   conf_mail_template text default null,
   conf_mail_template_de text default null,
   conf_mail_template_fr text default null,
   conf_mail_letter text default null,
   conf_mail_template_mgmt text default null,
   conf_mail_template_mgmt_de text default null,
   conf_mail_template_mgmt_fr text default null,
   conf_mail_letter_mgmt text default null,
   confdup_mail_template text default null,
   confdup_mail_template_de text default null,
   confdup_mail_template_fr text default null,
   confdup_mail_letter text default null,
   tabsconfiguration text,
   customtabs varchar(255),
   rec_excluded_attributes varchar(4000) default null,
   rec_expert_enable boolean default false,
   rec_expert_deadline timestamp,
   rec_expert_mail_subject varchar(255) default null,
   rec_expert_mail_template text default null,
   rec_expert_mail_letter text default null,
   rec_expert_docs varchar(2000) default 'all',
   rec_expert_fields varchar(32000),
   confsub_expert_mail_subject varchar(255) default null,
   confsub_expert_mail_template text default null,
   rec_referee_enable boolean default false,
   rec_referee_min int8,
   rec_referee_max int8,
   rec_referee_mail_type varchar(32),
   rec_referee_deadline timestamp,
   rec_referee_mail_subject varchar(255) default null,
   rec_referee_mail_template text default null,
   rec_referee_mail_letter text default null,
   rec_referee_docs varchar(2000) default 'all',
   rec_referee_fields varchar(32000),
   confsub_referee_mail_subject varchar(255) default null,
   confsub_referee_mail_template text default null,
   rec_comp_expert_enable boolean default false,
   rec_comp_expert_deadline timestamp,
   rec_comp_expert_mail_subject varchar(255) default null,
   rec_comp_expert_mail_template text default null,
   rec_comp_expert_docs varchar(2000) default 'all',
   rec_comp_expert_mail_letter text default null,
   rec_comp_expert_fields varchar(32000),
   confs_comp_expert_mail_subject varchar(255) default null,
   confs_comp_expert_mail_template text default null,
   rec_public_feedback_enable bool default false,
   rec_public_feedback_deadline timestamp default null,
   rec_app_referee_mgt_deadline timestamp default null,
   rec_app_referee_mgt_enable boolean default false,
   committeecomment_enable bool default false,
   committeecomment_visibility varchar(64),
   review_enable bool default false,
   system_tags_enable bool default false,
   position_tags_enable bool default false,
   fk_review_definition_id int8 default null,
   fk_doc_1_id int8 default null,
   fk_doc_2_id int8 default null,
   fk_doc_3_id int8 default null,
   fk_committeegroup int8 default null,
   fk_committeeheadgroup int8 default null,
   fk_secretarygroup int8 default null,
   fk_exofficiogroup int8 default null,
   rating_policy_link_url_1 varchar(255),
   rating_policy_link_url_2 varchar(255),
   rating_policy_link_url_3 varchar(255),
   rating_policy_link_url_4 varchar(255),
   rating_policy_link_label_1 varchar(1024),
   rating_policy_link_label_2 varchar(1024),
   rating_policy_link_label_3 varchar(1024),
   rating_policy_link_label_4 varchar(1024),
   fk_org_unit_id int8 default null,
   fk_organisation_id int8 default null,
   PRIMARY KEY (pos_id)
);

create table o_selectus_pos_attribute_def (
   id int8 not null,
   lastmodified timestamp,
   creationdate timestamp,
   pos int8,
   orderpos int8 default 0,
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
   fk_position_id int8,
   primary key (id)
);

create table o_selectus_application (
   app_id int8 not null,
   version int4 not null,
   creationdate timestamp default null,
   last_modified timestamp default null,
   application_lid int8 DEFAULT 0,
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
   originalpublications int8 default null,
   firstauthorships int8 default null,
   lastauthorships int8 default null,
   citations int8 default null,
   impactfactor decimal default null,
   hfactor decimal default null,
   highestdegreetype varchar(255) default null,
   highestdegreedescr varchar(1024) default null,
   highestdegreeinstitution varchar(255) default null,
   highestdegreedate timestamp default null,
   highestdegreeworkedsince varchar(1024) default null,
   workedoutacademia varchar(1024) default null,
   workedoutacademiacare varchar(1024) default null,
   careerdescription text default null,
   dissertationtitle varchar(255) default null,
   dissertationinstitution varchar(255) default null,
   dissertationdate timestamp default null,
   dissertationkeyword1 varchar(1024) default null,
   dissertationkeyword2 varchar(1024) default null,
   dissertationkeyword3 varchar(1024) default null,
   habilitationtitle varchar(255) default null,
   habilitationinstitution varchar(255) default null,
   habilitationdate timestamp default null,
   orcid varchar(255) default null,
   projecttitle varchar(1024) default null,
   projectfinancialimpact varchar(1024) default null,
   projectfinancialimpact_2 varchar(1024) default null,
   projectfinancialimpact_3 varchar(1024) default null,
   projectfinancialimpact_4 varchar(1024) default null,
   projectfinancialimpact_5 varchar(1024) default null,
   projectstartdate timestamp default null,
   projectduration varchar(1024) default null,
   projectdescription text default null,
   acronym varchar(1024) default null,
   keywords varchar(1024) default null,
   disciplines varchar(1024) default null,
   public_feedback_enable bool default false,
   public_feedback_deadline timestamp default null,
   public_feedback_key varchar(64) default null,
   after_position_closed bool default false,
   withdrawn bool DEFAULT false,
   withdrawn_date timestamp default null,
   status varchar(16) not null default 'active',
   onhold_date timestamp,
   rejected_date timestamp,
   noteligible_date timestamp,
   granted_date timestamp,
   hired_date timestamp,
   status_comment text,
   acceptterms bool default null,
   jobad varchar(255),
   expertblacklist varchar(4000) default null,
   expertconsent bool default null,
   memo text default null,
   committeecomment text default null,
   decision int4,
   applicanturl varchar(255) default null,
   report_ratings_2 int8 default null,
   report_ratings_1 int8 default null,
   report_ratings_0 int8 default null,
   report_ratings_abstentions int8 default null,
   report_experts int8 default null,
   report_experts_letters int8 default null,
   report_referees int8 default null,
   report_referees_letters int8 default null,
   report_comp_experts int8 default null,
   report_comp_experts_letters int8 default null,
   fk_identity_id bigint default null,
   fk_coveringletter_id int8 default null,
   fk_curriculumvitae_id int8 default null,
   fk_publicationlist_id int8 default null,
   fk_researchstatement_id int8 default null,
   fk_teachingstatement_id int8 default null,
   fk_leadership_id int8 default null,
   fk_listofreferees_id int8 default null,
   fk_projectlist_id int8 default null,
   fk_referenceletters_id int8 default null,
   fk_teachingassessment_id int8 default null,
   fk_certificateofstudy_id int8 default null,
   fk_degreecertificates_id int8 default null,
   fk_dissertation_id int8 default null,
   fk_habilitation_id int8 default null,
   fk_clinicaldisciplines_id int8 default null,
   fk_surgicaldisciplines_id int8 default null,
   fk_reprints_id int8 default null,
   fk_externalfunding_id int8 default null,
   fk_publication1_id int8 default null,
   fk_publication2_id int8 default null,
   fk_publication3_id int8 default null,
   fk_publication4_id int8 default null,
   fk_publication5_id int8 default null,
   fk_otherdocument_id int8 default null,
   fk_position_id int8,
   fk_combineddocument_id int8 default null,
   PRIMARY KEY (app_id)
);

create table o_selectus_app_attribute (
   id int8 not null,
   lastmodified timestamp,
   creationdate timestamp,
   attrvalue varchar(4000),
   fk_definition_id int8 not null,
   fk_application_id int8,
   fk_position_id int8,
   primary key (id)
);

create table o_selectus_attachment (
   attachment_id int8 not null,
   version int4 not null,
   creationdate date default null,
   file_name varchar(255),
   file_type varchar(8) default 'pdf',
   datas bytea,
   datas_size int4,
   PRIMARY KEY (attachment_id)
);

create table o_selectus_committee_report (
   id int8 not null,
   creationdate timestamp default null,
   lastmodified timestamp default null,
   r_role varchar(255),
   r_ratings_rights varchar(255),
   r_gender varchar(255),
   r_user_classification varchar(255),
   r_num_ratings_a int8 default null,
   r_num_ratings_b int8 default null,
   r_num_ratings_c int8 default null,
   r_num_abstentions int8 default null,
   fk_position_id int8 not null,
   PRIMARY KEY (id)
);

create table o_selectus_position_policy (
  pol_id int8 not null,
  version int4 not null,
  creationdate timestamp default null,
  dont_show_next_time bool default false,
  policytype varchar(32) default null,
  fk_position_id int8 default null,
  fk_identity_id int8 default null,
  primary key (pol_id)
);

create table o_selectus_mail_template (
   id int8 not null,
   lastmodified timestamp,
   creationdate timestamp,
   f_id varchar(255),
   f_name varchar(255),
   f_subject varchar(255),
   f_subject_de varchar(255),
   f_subject_fr varchar(255),
   f_body text,
   f_body_de text,
   f_body_fr text,
   f_letter text,
   fk_position_id int8,
   primary key (id)
);

create table o_selectus_application_notes (
  notes_id int8 not null,
  version int4 not null,
  creationdate timestamp default null,
  notes_content varchar(4000),
  fk_application_id int8 default null,
  fk_identity_id int8 default null,
  primary key (notes_id)
);

create table o_selectus_public_feedback (
   id int8 not null,
   creationdate timestamp default null,
   last_modified timestamp default null,
   r_firstname varchar(256),
   r_lastname varchar(256),
   r_email varchar(256),
   r_external_id varchar(256),
   r_external_ref varchar(256),
   r_comment text,
   fk_application_id int8 not null,
   PRIMARY KEY (id)
);

create table o_selectus_apps_feedback (
   id int8 not null,
   creationdate timestamp default null,
   last_modified timestamp default null,
   r_name varchar(128),
   r_enabled bool default false not null,
   r_deadline timestamp,
   r_mail_subject varchar(255) default null,
   r_mail_template text default null,
   r_mail_letter text default null,
   r_docs varchar(2000) default 'all',
   r_experts_docs bool default false not null,
   r_referees_docs bool default false not null,
   r_experts_comp_assessment_docs bool default false not null,
   r_fields varchar(32000),
   fk_position_id int8 not null,
   PRIMARY KEY (id)
);

create table o_selectus_app_feedback (
   id int8 not null,
   creationdate timestamp default null,
   last_modified timestamp default null,
   r_comment text,
   r_comment_date timestamp,
   r_deadline timestamp,
   r_status varchar(32) default 'notSent' not null,
   r_request_date timestamp,
   r_last_reminder_date timestamp,
   fk_config_id int8 not null,
   fk_application_id int8 not null,
   fk_identity_id int8 not null,
   PRIMARY KEY (id)
);

create table o_selectus_rejection_email_log (
  log_id int8 not null,
  version int4 not null,
  status int4 not null,
  creationdate timestamp default null,
  mail_template varchar(32) default null,
  mail_subject varchar(1024) default null,
  mail_content text default null,
  mail_rejection bool default false,
  fk_application_id int8 default null,
  fk_letter_id int8 default null,
  primary key (log_id)
);

create table o_selectus_reference (
   ref_id int8 not null,
   lastmodified timestamp,
   creationdate timestamp,
   version int4 not null,
   firstname varchar(255),
   lastname varchar(255),
   title varchar(255),
   institution varchar(255),
   mail varchar(255),
   disclaimer boolean default false,
   privacydisclaimer boolean default false,
   submissionurl varchar(64),
   submissiondate timestamp,
   submissiondeadline timestamp,
   reftype varchar(32),
   requeststatus varchar(32) default 'notAnswered',
   adminnote text,
   dateconsent timestamp,
   consentbystaff bool default null,
   status varchar(32),
   dateinvitation timestamp,
   datelastreminder timestamp,
   remindersbyapplicant int8 default 0,
   fk_letter_id int8,
   fk_application_id int8,
   primary key (ref_id)
);

create table o_selectus_reference_comment (
   id int8 not null,
   creationdate timestamp default null,
   r_comment text,
   fk_reference_id int8 not null,
   PRIMARY KEY (id)
);

create table o_selectus_reference_to_app (
   id bigserial,
   creationdate timestamp not null,
   fk_reference_id int8 not null,
   fk_application_id int8 not null,
   PRIMARY KEY (id)
);

create table o_selectus_decision_rubric_def (
   def_id int8 not null,
   creationdate timestamp,
   lastmodified timestamp,
   d_pos int8 default 0 not null,
   d_rubric varchar(128) not null,
   d_type varchar(32) not null,
   d_sum bool default false not null,
   d_weight int8 default 10 not null,
   fk_position_id int8 not null,
   primary key (def_id)
);

create table o_selectus_decision_rubric (
   rubric_id int8 not null,
   lastmodified timestamp,
   creationdate timestamp,
   d_string_value varchar(1024),
   d_integer_value int8,
   fk_definition_id int8 not null,
   fk_application_id int8 not null,
   primary key (rubric_id)
);

create table o_selectus_review_position_def (
   id int8 not null,
   lastmodified timestamp,
   creationdate timestamp,
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
   r_slider_steps int8 default null,
   r_slider_left_label varchar(255),
   r_slider_right_label varchar(255),
   r_rev_statistics_enable bool default null,
   r_rev_chart_enable bool default null,
   primary key (id)
);

create table o_selectus_review_element_def (
   id int8 not null,
   lastmodified timestamp,
   creationdate timestamp,
   pos int8 default null,
   r_type varchar(16),
   r_label varchar(255),
   fk_pos_rev_id int8 not null,
   primary key (id)
);

create table o_selectus_review_response (
   id int8 not null,
   lastmodified timestamp,
   creationdate timestamp,
   r_string_value varchar(4000),
   r_integer_value int8,
   fk_reviewer_id int8 not null,
   fk_element_id int8 not null,
   fk_application_id int8 not null,
   primary key (id)
);

create table o_selectus_app_comment (
   id int8 not null,
   lastmodified timestamp,
   creationdate timestamp,
   r_deleted bool default false,
   r_comment text,
   fk_author_id int8 not null,
   fk_application_id int8 not null,
   fk_reviewer_id int8,
   fk_app_comment_id int8,
   primary key (id)
);

create table o_selectus_app_comment_vote (
   id int8 not null,
   lastmodified timestamp,
   creationdate timestamp,
   r_up bool,
   fk_voter_id int8 not null,
   fk_app_comment_id int8,
   primary key (id)
);

create table o_selectus_category (
   id int8 not null,
   lastmodified timestamp,
   creationdate timestamp,
   r_name varchar(255),
   r_color varchar(16),
   fk_position_id int8,
   primary key (id)
);

create table o_selectus_app_category (
   id int8 not null,
   creationdate timestamp,
   r_administrative bool default false,
   fk_application_id int8 not null,
   fk_category_id int8 not null,
   primary key (id)
);

create table o_selectus_assignment (
   id int8 not null,
   creationdate timestamp,
   fk_application_id int8,
   fk_assignee_id int8,
   primary key (id)
);


create table o_selectus_org_unit (
   id int8 not null,
   lastmodified timestamp,
   creationdate timestamp,
   unitname varchar(255),
   unitnamede varchar(255),
   unitnamefr varchar(255),
   url varchar(255),
   description varchar(2000),
   systemconfig bool default true,
   staffmail varchar(255),
   staffbcc varchar(255),
   mailsignature varchar(2000),
   fk_organisation_id int8,
   primary key (id)
);

create table o_selectus_audit_log (
   id int8 not null,
   creationdate timestamp not null,
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
   fk_position_id int8 not null,
   fk_application_id int8,
   fk_committee_identity_id int8,
   fk_rating_id int8,
   fk_comment_id int8,
   fk_reference_id int8,
   fk_feedback_id int8,
   fk_identity_id int8,
   primary key (id)
);

create table o_selectus_audit_log_read (
   id int8 not null,
   creationdate timestamp not null,
   r_read bool default true not null,
   fk_identity_id int8 not null,
   fk_audit_log_id int8 not null,
   primary key (id)
);

create table o_selectus_audit_log_usettings (
   id int8 not null,
   creationdate timestamp,
   lastmodified timestamp,
   r_enabled bool default false not null,
   r_interval varchar(16) default 'never' not null,
   fk_identity_id int8 not null,
   primary key (id)
);

create table o_selectus_audit_log_u_notifs (
   id int8 not null,
   creationdate timestamp,
   r_last_mail_date timestamp,
   fk_identity_id int8 not null,
   primary key (id)
);

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
create index idx_app_ident_idx on o_selectus_application (fk_identity_id);

alter table o_selectus_app_attribute add constraint attr_to_attrdef foreign key (fk_definition_id) references o_selectus_pos_attribute_def (id);
create index attr_to_attrdef_idx on o_selectus_app_attribute (fk_definition_id);
alter table o_selectus_app_attribute add constraint attr_to_app foreign key (fk_application_id) references o_selectus_application (app_id);
create index attr_to_app_idx on o_selectus_app_attribute (fk_application_id);
alter table o_selectus_app_attribute add constraint attr_to_gpos_idx foreign key (fk_position_id) references o_selectus_position (pos_id);
create index idx_attr_to_gpos_idx on o_selectus_app_attribute (fk_position_id);

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

alter table o_selectus_committee_report add constraint com_rep_to_pos_idx foreign key (fk_position_id) references o_selectus_position (pos_id);
create index idx_com_rep_to_pos_idx on o_selectus_committee_report (fk_position_id);

-- position
alter table o_selectus_position add constraint att_doc_1_to_att foreign key (fk_doc_1_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_position add constraint att_doc_2_to_att foreign key (fk_doc_2_id) references o_selectus_attachment (attachment_id);
alter table o_selectus_position add constraint att_doc_3_to_att foreign key (fk_doc_3_id) references o_selectus_attachment (attachment_id);

alter table o_selectus_position add constraint pos_review_def_idx foreign key (fk_review_definition_id) references o_selectus_review_position_def (id);
create index idx_pos_review_def_idx on o_selectus_position (fk_review_definition_id);

alter table o_selectus_position_policy add constraint policy_to_pos foreign key (fk_position_id) references o_selectus_position (pos_id);
alter table o_selectus_position_policy add constraint policy_to_identity foreign key (fk_identity_id) references o_bs_identity (id);

alter table o_selectus_mail_template add constraint mtemplate_pos_idx foreign key (fk_position_id) references o_selectus_position (pos_id);
create index idx_mtemplate_pos_idx on o_selectus_mail_template (fk_position_id);

alter table o_selectus_pos_attribute_def add constraint posattrdef_to_pos foreign key (fk_position_id) references o_selectus_position (pos_id);
create index posattrdef_to_pos_idx on o_selectus_pos_attribute_def (fk_position_id);

alter table o_selectus_position add constraint selectus_pos_to_org_idx foreign key (fk_organisation_id) references o_org_organisation (id);
create index idx_selectus_pos_to_org_idx on o_selectus_position (fk_organisation_id);


alter table o_selectus_application_notes add constraint app_notes_to_app foreign key (fk_application_id) references o_selectus_application (app_id);
alter table o_selectus_application_notes add constraint app_notes_to_identity foreign key (fk_identity_id) references o_bs_identity (id);

alter table o_selectus_rejection_email_log  add constraint rejection_to_app foreign key (fk_application_id) references o_selectus_application (app_id);
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

-- comment
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

-- category
alter table o_selectus_app_category add constraint app_cat_app_idx foreign key (fk_application_id) references o_selectus_application (app_id);
create index idx_app_cat_app_idx on o_selectus_app_category (fk_application_id);
alter table o_selectus_app_category add constraint app_cat_cat_idx foreign key (fk_category_id) references o_selectus_category (id);
create index idx_app_cat_cat_idx on o_selectus_app_category (fk_category_id);
alter table o_selectus_category add constraint cat_position_idx foreign key (fk_position_id) references o_selectus_position (pos_id);
create index idx_cat_position_idx on o_selectus_category (fk_position_id);

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

-- Grading
create table o_grad_assignment_log (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_closed timestamp,
   g_status varchar(16),
   g_deleted bool default false not null,
   g_reference_entry_id int8 not null,
   g_reference_entry_displayname varchar(110),
   g_reference_entry_external_ref varchar(255),
   g_entry_id int8 not null,
   g_entry_displayname varchar(110),
   g_entry_external_ref varchar(255),
   g_time int8 default 0 not null,
   g_metadata_time int8 default 0 not null,
   g_assignment_id int8 not null,
   fk_grader int8 not null,
   fk_assignee int8 not null,
   primary key (id)
);

create index idx_grad_assign_log_ent_idx on o_grad_assignment_log (g_entry_id);
create index idx_grad_assign_log_idx on o_grad_assignment_log (g_assignment_id);
create index idx_grad_assign_log_ref_idx on o_grad_assignment_log (g_reference_entry_id);

alter table o_grad_assignment_log add constraint grad_assign_log_grad_idx foreign key (fk_grader) references o_bs_identity (id);
create index idx_grad_assign_log_grad_idx on o_grad_assignment_log (fk_grader);
alter table o_grad_assignment_log add constraint grad_assign_log_assign_idx foreign key (fk_assignee) references o_bs_identity (id);
create index idx_grad_assign_log_assign_idx on o_grad_assignment_log (fk_assignee);


-- Curriculum element type
alter table o_cur_element_type add column c_impl_only bool default false not null;
alter table o_cur_element_type add column c_status varchar(32) default 'active' not null;
alter table o_cur_element_type add column c_automation_config text;

-- Curriculum element automation
alter table o_cur_curriculum_element drop column if exists c_auto_instantiation;
alter table o_cur_curriculum_element drop column if exists c_auto_instantiation_unit;
alter table o_cur_curriculum_element drop column if exists c_auto_access_coach;
alter table o_cur_curriculum_element drop column if exists c_auto_access_coach_unit;
alter table o_cur_curriculum_element drop column if exists c_auto_published;
alter table o_cur_curriculum_element drop column if exists c_auto_published_unit;
alter table o_cur_curriculum_element drop column if exists c_auto_closed;
alter table o_cur_curriculum_element drop column if exists c_auto_closed_unit;
alter table o_cur_curriculum_element add column c_automation_config text;


-- Safe Exam Browser
alter table o_as_seb_template add column a_type varchar(16) default 'OO_FORM' not null;
alter table o_as_seb_template add column a_safeexambrowserauthorhint text;
alter table o_as_seb_template add column a_download bool;
alter table o_as_seb_template add column a_exit_password varchar(255);
alter table o_as_seb_template add column a_allow_exit bool;
alter table o_as_seb_template add column a_config_filename varchar(255);

alter table o_as_mode_course add column a_safeexambrowser_exit_password varchar(255);
alter table o_as_mode_course add column a_safeexambrowser_allow_exit bool;

alter table o_as_inspection_configuration add column a_safeexambrowser_exit_password varchar(255);
alter table o_as_inspection_configuration add column a_safeexambrowser_allow_exit bool;


-- Certification program
alter table o_cer_program add column fk_print_template int8;
alter table o_cer_program add column c_print_template_enabled bool default false not null;

alter table o_cer_program add constraint cer_progr_to_prtemplate_idx foreign key (fk_print_template) references o_cer_template (id);
create index idx_cer_progr_to_prtemplate_idx on o_cer_program(fk_print_template);

alter table o_cer_certificate add column c_print_path varchar(1024);
alter table o_cer_certificate add column fk_print_metadata int8;
alter table o_cer_certificate add constraint certificate_printdata_idx foreign key (fk_print_metadata) references o_vfs_metadata(id);
create index idx_certificate_printdata_idx on o_cer_certificate (fk_print_metadata);

alter table o_cer_program add column c_sn_enabled bool default false not null;
alter table o_cer_program add column c_sn_format varchar(255);
alter table o_cer_program add column c_sn_start_number int8 default 1 not null;
alter table o_cer_program add column c_sn_counter int8 default 0 not null;

alter table o_cer_entry_config add column c_sn_enabled bool default false not null;
alter table o_cer_entry_config add column c_sn_format varchar(255);
alter table o_cer_entry_config add column c_sn_start_number int8 default 1 not null;
alter table o_cer_entry_config add column c_sn_counter int8 default 0 not null;

alter table o_cer_certificate add column c_serial_number varchar(255);


-- Question pool
alter table o_qp_item add column q_ai_unsupervised_generated bool default null;
alter table o_qp_item add column q_ai_provider varchar(255) default null;
alter table o_qp_item add column q_ai_model varchar(255) default null;


