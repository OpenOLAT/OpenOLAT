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
