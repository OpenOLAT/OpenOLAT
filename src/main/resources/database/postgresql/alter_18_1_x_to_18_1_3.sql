-- LTI 1.3
create table o_lti_context (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_context_id varchar(255) not null,
   l_resource_id varchar(255),
   l_target_url varchar(1024),
   l_send_attributes varchar(2048),
   l_send_custom_attributes text,
   l_author_roles varchar(2048),
   l_coach_roles varchar(2048),
   l_participant_roles varchar(2048),
   l_assessable bool default false not null,
   l_nrps bool default true not null,
   l_display varchar(32),
   l_display_height varchar(32),
   l_display_width varchar(32),
   l_skip_launch_page bool default false not null,
   fk_entry_id int8,
   l_sub_ident varchar(64),
   fk_group_id int8,
   fk_deployment_id int8,
   primary key (id)
);

alter table o_lti_context add constraint ltictx_to_deploy_idx foreign key (fk_deployment_id) references o_lti_tool_deployment(id);
create index idx_ltictx_to_deploy_idx on o_lti_context (fk_deployment_id);
alter table o_lti_context add constraint lti_ctxt_to_re_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_lti_ctxt_to_re_idx on o_lti_context (fk_entry_id);
alter table o_lti_context add constraint ctxt_to_group_idx foreign key (fk_group_id) references o_gp_business(group_id);
create index idx_ctxt_to_group_idx on o_lti_context (fk_group_id);

alter table o_lti_tool_deployment add column l_deployment_type varchar(32);

alter table o_lti_content_item add column fk_context_id int8;
alter table o_lti_content_item add constraint ltiitem_to_context_idx foreign key (fk_context_id) references o_lti_context(id);
create index idx_ltiitem_to_context_idx on o_lti_content_item (fk_context_id);

alter table o_jup_deployment add column fk_lti_context_id int8;
alter table o_jup_deployment add constraint jup_deployment_context_idx foreign key (fk_lti_context_id) references o_lti_context (id);
create index idx_jup_deployment_context_idx on o_jup_deployment (fk_lti_context_id);

alter table o_zoom_config add column fk_lti_context_id int8;
alter table o_zoom_config add constraint zoom_config_context_idx foreign key (fk_lti_context_id) references o_lti_context (id);
create index idx_zoom_configcontext_idx on o_zoom_config (fk_lti_context_id);

