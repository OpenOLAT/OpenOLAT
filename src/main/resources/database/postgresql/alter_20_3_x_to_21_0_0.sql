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
