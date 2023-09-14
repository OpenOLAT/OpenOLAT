-- Passkey
alter table o_bs_authentication add column w_counter int8 default 0;
alter table o_bs_authentication add column w_aaguid bytea;
alter table o_bs_authentication add column w_credential_id bytea;
alter table o_bs_authentication add column w_user_handle bytea;
alter table o_bs_authentication add column w_cose_key bytea;
alter table o_bs_authentication add column w_attestation_object text;
alter table o_bs_authentication add column w_client_extensions text;
alter table o_bs_authentication add column w_authenticator_extensions text;
alter table o_bs_authentication add column w_transports varchar(255);


create table o_bs_recovery_key (
   id bigserial,
   creationdate timestamp not null,
   r_recovery_key_hash varchar(128),
   r_recovery_salt varchar(64),
   r_recovery_algorithm varchar(32),
   r_use_date timestamp,
   fk_identity int8 not null,
   primary key (id)
);

alter table o_bs_recovery_key add constraint rec_key_to_ident_idx foreign key (fk_identity) references o_bs_identity(id);
create index idx_rec_key_to_ident_idx on o_bs_recovery_key (fk_identity);

-- LTI 1.3
alter table o_lti_tool add column l_deep_linking bool;

create table o_lti_content_item (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_type varchar(32) not null,
   l_url varchar(1024),
   l_title varchar(255),
   l_text text,
   l_media_type varchar(255),
   l_html text,
   l_width int8,
   l_height int8,
   l_icon_url varchar(1024),
   l_icon_height int8,
   l_icon_width int8,
   l_thumbnail_url varchar(1024),
   l_thumbnail_height int8,
   l_thumbnail_width int8,
   l_presentation varchar(64),
   l_window_targetname varchar(1024),
   l_window_width int8,
   l_window_height int8,
   l_window_features varchar(2048),
   l_iframe_width int8,
   l_iframe_height int8,
   l_iframe_src varchar(1024),
   l_custom text,
   l_lineitem_label varchar(1024),
   l_lineitem_score_maximum decimal,
   l_lineitem_resource_id varchar(1024),
   l_lineitem_tag varchar(1024),
   l_lineitem_grades_release bool,
   l_available_start timestamp,
   l_available_end timestamp,
   l_submission_start timestamp,
   l_submission_end timestamp,
   l_expires_at timestamp,
   fk_tool_id int8 not null,
   fk_tool_deployment_id int8,
   primary key (id)
);

alter table o_lti_content_item add constraint ltiitem_to_tool_idx foreign key (fk_tool_id) references o_lti_tool(id);
create index idx_ltiitem_to_tool_idx on o_lti_content_item (fk_tool_id);

alter table o_lti_content_item add constraint ltiitem_to_deploy_idx foreign key (fk_tool_deployment_id) references o_lti_tool_deployment(id);
create index idx_ltiitem_to_deploy_idx on o_lti_content_item (fk_tool_deployment_id);

