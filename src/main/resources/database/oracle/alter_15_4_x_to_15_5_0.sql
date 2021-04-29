-- Portfolio
alter table o_pf_page_body add p_usage number(20) default 1;
alter table o_pf_page_body add p_synthetic_status varchar(32);


-- VFS
alter table o_vfs_metadata add f_revision_temp_nr number(20) default null;
alter table o_vfs_metadata rename column "fk_author" to "fk_initialized_by";
alter table o_vfs_revision add f_revision_temp_nr number(20) default null;
alter table o_vfs_revision rename column "fk_author" to "fk_initialized_by";
alter table o_vfs_revision add fk_lastmodified_by number(20) default null;

alter table o_vfs_revision add constraint fvers_modified_by_idx foreign key (fk_lastmodified_by) references o_bs_identity (id);
create index idx_fvers_mod_by_idx on o_vfs_revision (fk_lastmodified_by);

-- Taxonomy linking in portfolio
create table o_pf_page_to_tax_competence (
   id number(20) generated always as identity,
   creationdate date not null,
   fk_tax_competence number(20) not null,
   fk_pf_page number(20) not null,
   primary key (id)
);

alter table o_pf_page_to_tax_competence add constraint fk_tax_competence_idx foreign key (fk_tax_competence) references o_tax_taxonomy_competence (id);
create index idx_fk_tax_competence_idx on o_pf_page_to_tax_competence (fk_tax_competence);
alter table o_pf_page_to_tax_competence add constraint fk_pf_page_idx foreign key (fk_pf_page) references o_pf_page (id);
create index idx_fk_pf_page_idx on o_pf_page_to_tax_competence (fk_pf_page);

alter table o_tax_taxonomy_level_type add t_allow_as_competence number default 1 not null;
alter table o_tax_taxonomy_competence add t_link_location varchar(255) default 'UNDEFINED' not null;


-- Authentication
alter table o_bs_authentication add issuer varchar(255) default 'DEFAULT' not null;

alter table o_bs_authentication drop constraint u_o_bs_authentication;
alter table o_bs_authentication add constraint unique_pro_iss_authusername UNIQUE (provider, issuer, authusername);


-- Business group
alter table o_gp_business add technical_type varchar2(32) default 'business' not null;
create index gp_tech_type_idx on o_gp_business (technical_type);


-- LTI 1.3
create table o_lti_tool (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   l_tool_type varchar2(16) default 'EXTERNAL' not null,
   l_tool_url varchar2(2000) not null,
   l_tool_name varchar2(255) not null,
   l_client_id varchar2(128) not null,
   l_public_key CLOB,
   l_public_key_url varchar2(2000),
   l_public_key_type varchar2(16),
   l_initiate_login_url varchar2(2000),
   primary key (id)
);

create table o_lti_tool_deployment (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   l_deployment_id varchar2(128) not null unique,
   l_target_url varchar2(1024),
   l_send_attributes varchar2(2048),
   l_send_custom_attributes CLOB,
   l_author_roles varchar2(2048),
   l_coach_roles varchar2(2048),
   l_participant_roles varchar2(2048),
   l_assessable number default 0 not null,
   l_display varchar2(32),
   l_display_height varchar2(32),
   l_display_width varchar2(32),
   l_skip_launch_page number default 0 not null,
   fk_tool_id number(20) not null,
   fk_entry_id number(20),
   l_sub_ident varchar2(64),
   primary key (id)
);

alter table o_lti_tool_deployment add constraint lti_sdep_to_tool_idx foreign key (fk_tool_id) references o_lti_tool (id);
create index idx_lti_sdep_to_tool_idx on o_lti_tool_deployment (fk_tool_id);
alter table o_lti_tool_deployment add constraint lti_sdep_to_re_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_lti_sdep_to_re_idx on o_lti_tool_deployment (fk_entry_id);


create table o_lti_platform (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   l_name varchar2(255),
   l_mail_matching number default 0 not null,
   l_scope varchar2(32) default 'SHARED' not null,
   l_issuer varchar2(255) not null,
   l_client_id varchar2(128) not null,
   l_key_id varchar2(64) not null,
   l_public_key CLOB not null,
   l_private_key CLOB not null,
   l_authorization_uri varchar2(2000),
   l_token_uri varchar2(2000),
   l_jwk_set_uri varchar2(2000),
   primary key (id)
);

create table o_lti_shared_tool_deployment (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   l_deployment_id varchar2(255),
   fk_platform_id number(20),
   fk_entry_id number(20),
   fk_group_id number(20),
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
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   l_context_id varchar2(255),
   l_service_type varchar2(16) not null,
   l_service_endpoint varchar2(255) not null,
   fk_deployment_id number(20) not null,
   primary key (id)
);

alter table o_lti_shared_tool_service add constraint lti_sha_ser_to_dep_idx foreign key (fk_deployment_id) references o_lti_shared_tool_deployment (id);
create index idx_lti_sha_ser_to_dep_idx on o_lti_shared_tool_service (fk_deployment_id);


create table o_lti_key (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   l_key_id varchar2(255) not null,
   l_public_key CLOB,
   l_private_key CLOB,
   l_algorithm varchar2(64) not null,
   l_issuer varchar2(1024) not null,
   primary key (id)
);

create index idx_lti_kid_idx on o_lti_key (l_key_id);


-- Document editor
alter table o_de_access add o_edit_start_date date default null;
