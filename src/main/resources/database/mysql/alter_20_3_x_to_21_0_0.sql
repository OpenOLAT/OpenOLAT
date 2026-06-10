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


-- AI usage log essay extension
alter table o_ai_usage_log add column a_assessment_item_identifier varchar(64);
alter table o_ai_usage_log add column a_content_hash_at_call varchar(64);
alter table o_ai_usage_log add column a_prompt_template_version varchar(40);
alter table o_ai_usage_log add column a_tier varchar(16);
alter table o_ai_usage_log add column a_assessment_item_session_key bigint;

create index idx_ai_usage_log_item_id on o_ai_usage_log (a_assessment_item_identifier);
create index idx_ai_usage_log_item_session on o_ai_usage_log (a_assessment_item_session_key);

-- AI essay correction result
create table o_ai_essay_correction (
  id                   bigint not null auto_increment,
  creationdate         datetime not null,
  lastmodified         datetime not null,
  fk_identity          bigint not null,
  a_item_session_key   bigint,
  a_storage_path       varchar(1024),
  a_question_id        varchar(64),
  a_student_answer     mediumtext not null,
  a_status             varchar(24) not null,
  a_feedback_json      mediumtext,
  a_error_message      varchar(2048),
  a_completed          datetime,
  primary key (id)
);
alter table o_ai_essay_correction ENGINE = InnoDB;

alter table o_ai_essay_correction add constraint ai_essay_corr_ident_fk foreign key (fk_identity) references o_bs_identity (id);
create index idx_ai_essay_corr_item_session on o_ai_essay_correction (a_item_session_key);
create index idx_ai_essay_corr_question on o_ai_essay_correction (a_storage_path, a_question_id);
