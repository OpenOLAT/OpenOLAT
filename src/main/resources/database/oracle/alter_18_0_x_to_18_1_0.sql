-- Passkey
alter table o_bs_authentication add w_counter number(20) default 0;
alter table o_bs_authentication add w_aaguid raw(16);
alter table o_bs_authentication add w_credential_id raw(1024);
alter table o_bs_authentication add w_user_handle raw(64);
alter table o_bs_authentication add w_cose_key raw(1024);
alter table o_bs_authentication add w_attestation_object clob;
alter table o_bs_authentication add w_client_extensions clob;
alter table o_bs_authentication add w_authenticator_extensions clob;
alter table o_bs_authentication add w_transports varchar(255);

create table o_bs_recovery_key (
   id number(20) generated always as identity,
   creationdate date not null,
   r_recovery_key_hash varchar(128),
   r_recovery_salt varchar(64),
   r_recovery_algorithm varchar(32),
   r_use_date date,
   r_expiration_date date,
   fk_identity number(20) not null,
   primary key (id)
);

alter table o_bs_recovery_key add constraint rec_key_to_ident_idx foreign key (fk_identity) references o_bs_identity(id);
create index idx_rec_key_to_ident_idx on o_bs_recovery_key (fk_identity);

-- LTI 1.3
alter table o_lti_tool add l_deep_linking number;

create table o_lti_content_item (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   l_type varchar(32) not null,
   l_url varchar(1024),
   l_title varchar(255),
   l_text CLOB,
   l_media_type varchar(255),
   l_html CLOB,
   l_width number(20),
   l_height number(20),
   l_icon_url varchar(1024),
   l_icon_height number(20),
   l_icon_width number(20),
   l_thumbnail_url varchar(1024),
   l_thumbnail_height number(20),
   l_thumbnail_width number(20),
   l_presentation varchar(64),
   l_window_targetname varchar(1024),
   l_window_width number(20),
   l_window_height number(20),
   l_window_features varchar(2048),
   l_iframe_width number(20),
   l_iframe_height number(20),
   l_iframe_src varchar(1024),
   l_custom CLOB,
   l_lineitem_label varchar(1024),
   l_lineitem_score_maximum decimal,
   l_lineitem_resource_id varchar(1024),
   l_lineitem_tag varchar(1024),
   l_lineitem_grades_release number,
   l_available_start date,
   l_available_end date,
   l_submission_start date,
   l_submission_end date,
   l_expires_at date,
   fk_tool_id number(20) not null,
   fk_tool_deployment_id number(20),
   primary key (id)
);

alter table o_lti_content_item add constraint ltiitem_to_tool_idx foreign key (fk_tool_id) references o_lti_tool(id);
create index idx_ltiitem_to_tool_idx on o_lti_content_item (fk_tool_id);

alter table o_lti_content_item add constraint ltiitem_to_deploy_idx foreign key (fk_tool_deployment_id) references o_lti_tool_deployment(id);
create index idx_ltiitem_to_deploy_idx on o_lti_content_item (fk_tool_deployment_id);

