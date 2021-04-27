-- Portfolio
alter table o_pf_page_body add column p_usage int8 default 1;
alter table o_pf_page_body add column p_synthetic_status varchar(32);


-- VFS
alter table o_vfs_metadata add column f_revision_temp_nr bigint default null;
alter table o_vfs_metadata rename column fk_author to fk_initialized_by;
alter table o_vfs_revision add column f_revision_temp_nr bigint default null;
alter table o_vfs_revision rename column fk_author to fk_initialized_by;
alter table o_vfs_revision add column fk_lastmodified_by bigint default null;

alter table o_vfs_revision add constraint fvers_modified_by_idx foreign key (fk_lastmodified_by) references o_bs_identity (id);
create index idx_fvers_mod_by_idx on o_vfs_revision (fk_lastmodified_by);

-- Taxonomy linking in portfolio
create table o_pf_page_to_tax_competence (
	id bigserial,
	creationdate timestamp not null,
	fk_tax_competence int8 not null,
	fk_pf_page int8 not null,
	primary key (id)
);

alter table o_pf_page_to_tax_competence add constraint fk_tax_competence_idx foreign key (fk_tax_competence) references o_tax_taxonomy_competence (id);
create index idx_fk_tax_competence_idx on o_pf_page_to_tax_competence (fk_tax_competence);
alter table o_pf_page_to_tax_competence add constraint fk_pf_page_idx foreign key (fk_pf_page) references o_pf_page (id);
create index idx_fk_pf_page_idx on o_pf_page_to_tax_competence (fk_pf_page);

alter table o_tax_taxonomy_level_type add column t_allow_as_competence bool default true not null;
alter table o_tax_taxonomy_competence add column t_link_location varchar(255) default 'UNDEFINED' not null;


-- Authentication
alter table o_bs_authentication add column issuer varchar(255) default 'DEFAULT' not null;

alter table o_bs_authentication drop constraint o_bs_authentication_provider_authusername_key;
alter table o_bs_authentication add constraint unique_pro_iss_authusername UNIQUE (provider, issuer, authusername);


-- Business group
alter table o_gp_business add column technical_type varchar(32) default 'business' not null;
create index gp_tech_type_idx on o_gp_business (technical_type);


-- LTI 1.3
create table o_lti_tool (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_tool_type varchar(16) default 'EXTERNAL' not null,
   l_tool_url varchar(2000) not null,
   l_tool_name varchar(255) not null,
   l_client_id varchar(128) not null,
   l_public_key text,
   l_public_key_url varchar(2000),
   l_public_key_type varchar(16),
   l_initiate_login_url varchar(2000),
   primary key (id)
);

create table o_lti_tool_deployment (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_deployment_id varchar(128) not null unique,
   l_target_url varchar(1024),
   l_send_attributes varchar(2048),
   l_send_custom_attributes text,
   l_author_roles varchar(2048),
   l_coach_roles varchar(2048),
   l_participant_roles varchar(2048),
   l_assessable bool default false not null,
   l_display varchar(32),
   l_display_height varchar(32),
   l_display_width varchar(32),
   l_skip_launch_page bool default false not null,
   fk_tool_id int8 not null,
   fk_entry_id int8,
   l_sub_ident varchar(64),
   primary key (id)
);

alter table o_lti_tool_deployment add constraint lti_sdep_to_tool_idx foreign key (fk_tool_id) references o_lti_tool (id);
create index idx_lti_sdep_to_tool_idx on o_lti_tool_deployment (fk_tool_id);
alter table o_lti_tool_deployment add constraint lti_sdep_to_re_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_lti_sdep_to_re_idx on o_lti_tool_deployment (fk_entry_id);


create table o_lti_platform (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_name varchar(255),
   l_mail_matching bool default false not null,
   l_scope varchar(32) default 'SHARED' not null,
   l_issuer varchar(255) not null,
   l_client_id varchar(128) not null,
   l_key_id varchar(64) not null,
   l_public_key text not null,
   l_private_key text not null,
   l_authorization_uri varchar(2000),
   l_token_uri varchar(2000),
   l_jwk_set_uri varchar(2000),
   primary key (id)
);

create table o_lti_shared_tool_deployment (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_deployment_id varchar(255),
   fk_platform_id int8,
   fk_entry_id int8,
   fk_group_id int8,
   primary key (id)
);

alter table o_lti_shared_tool_deployment add constraint unique_deploy_platform unique (l_deployment_id, fk_platform_id);
alter table o_lti_shared_tool_deployment add constraint lti_sha_dep_to_tool_idx foreign key (fk_platform_id) references o_lti_platform (id);
create index idx_lti_sha_dep_to_tool_idx on o_lti_shared_tool_deployment (fk_platform_id);
alter table o_lti_shared_tool_deployment add constraint lti_shared_to_re_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_lti_shared_to_re_idx on o_lti_shared_tool_deployment (fk_entry_id);
alter table o_lti_shared_tool_deployment add constraint lti_shared_to_bg_idx foreign key (fk_group_id) references o_gp_business (group_id);
create index idx_lti_shared_to_bg_idx on o_lti_shared_tool_deployment (fk_group_id);

create table o_lti_shared_tool_service (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_context_id varchar(255),
   l_service_type varchar(16) not null,
   l_service_endpoint varchar(255) not null,
   fk_deployment_id int8 not null,
   primary key (id)
);

alter table o_lti_shared_tool_service add constraint lti_sha_ser_to_dep_idx foreign key (fk_deployment_id) references o_lti_shared_tool_deployment (id);
create index idx_lti_sha_ser_to_dep_idx on o_lti_shared_tool_service (fk_deployment_id);


create table o_lti_key (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_key_id varchar(255) not null,
   l_public_key text,
   l_private_key text,
   l_algorithm varchar(64) not null,
   l_issuer varchar(1024) not null,
   primary key (id)
);

create index idx_lti_kid_idx on o_lti_key (l_key_id);

