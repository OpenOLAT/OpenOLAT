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
