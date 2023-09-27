-- Passkey
alter table o_bs_authentication add column w_counter bigint default 0;
alter table o_bs_authentication add column w_aaguid varbinary(16);
alter table o_bs_authentication add column w_credential_id varbinary(1024);
alter table o_bs_authentication add column w_user_handle varbinary(64);
alter table o_bs_authentication add column w_cose_key varbinary(1024);
alter table o_bs_authentication add column w_attestation_object mediumtext;
alter table o_bs_authentication add column w_client_extensions mediumtext;
alter table o_bs_authentication add column w_authenticator_extensions mediumtext;
alter table o_bs_authentication add column w_transports varchar(255);


create table o_bs_recovery_key (
   id bigint not null auto_increment,
   creationdate datetime not null,
   r_recovery_key_hash varchar(128),
   r_recovery_salt varchar(64),
   r_recovery_algorithm varchar(32),
   r_use_date datetime,
   r_expiration_date datetime,
   fk_identity bigint not null,
   primary key (id)
);
alter table o_bs_recovery_key ENGINE = InnoDB;

alter table o_bs_recovery_key add constraint rec_key_to_ident_idx foreign key (fk_identity) references o_bs_identity(id);


-- LTI 1.3
alter table o_lti_tool add column l_deep_linking bool;

create table o_lti_content_item (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   l_type varchar(32) not null,
   l_url varchar(1024),
   l_title varchar(255),
   l_text mediumtext,
   l_media_type varchar(255),
   l_html mediumtext,
   l_width bigint,
   l_height bigint,
   l_icon_url varchar(1024),
   l_icon_height bigint,
   l_icon_width bigint,
   l_thumbnail_url varchar(1024),
   l_thumbnail_height bigint,
   l_thumbnail_width bigint,
   l_presentation varchar(64),
   l_window_targetname varchar(1024),
   l_window_width bigint,
   l_window_height bigint,
   l_window_features varchar(2048),
   l_iframe_width bigint,
   l_iframe_height bigint,
   l_iframe_src varchar(1024),
   l_custom mediumtext,
   l_lineitem_label varchar(1024),
   l_lineitem_score_maximum float(65,30),
   l_lineitem_resource_id varchar(1024),
   l_lineitem_tag varchar(1024),
   l_lineitem_grades_release bool,
   l_available_start datetime,
   l_available_end datetime,
   l_submission_start datetime,
   l_submission_end datetime,
   l_expires_at datetime,
   fk_tool_id bigint not null,
   fk_tool_deployment_id bigint,
   primary key (id)
);
alter table o_lti_content_item ENGINE = InnoDB;

alter table o_lti_content_item add constraint ltiitem_to_tool_idx foreign key (fk_tool_id) references o_lti_tool(id);

alter table o_lti_content_item add constraint ltiitem_to_deploy_idx foreign key (fk_tool_deployment_id) references o_lti_tool_deployment(id);

