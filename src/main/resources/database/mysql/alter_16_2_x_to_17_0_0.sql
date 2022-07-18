

-- Access
create table o_ac_offer_to_organisation (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  fk_offer bigint not null,
  fk_organisation bigint not null,
  primary key (id)
);
alter table o_ac_offer_to_organisation ENGINE = InnoDB;

alter table o_ac_offer_to_organisation add constraint rel_oto_offer_idx foreign key (fk_offer) references o_ac_offer (offer_id);
alter table o_ac_offer_to_organisation add constraint rel_oto_org_idx foreign key (fk_organisation) references o_org_organisation (id);

alter table o_ac_offer add column open_access bool default false not null;
alter table o_ac_offer add column guest_access bool default false not null;
alter table o_ac_offer add column catalog_publish bool default false not null;
alter table o_ac_offer add column catalog_web_publish bool default false not null;

create index idx_offer_guest_idx on o_ac_offer (guest_access);
create index idx_offer_open_idx on o_ac_offer (open_access);

alter table o_repositoryentry add column publicvisible bool default false not null;
alter table o_repositoryentry add column status_published_date datetime;

-- Taxonomy
alter table o_tax_taxonomy_level add column t_i18n_suffix varchar(64);
alter table o_tax_taxonomy_level add column t_media_path varchar(255);
alter table o_tax_taxonomy_level modify column t_displayname varchar(255);


-- Catalog V2
create table o_ca_launcher (
   id bigint not null auto_increment,
   lastmodified datetime not null,
   creationdate datetime not null,
   c_type varchar(50),
   c_identifier varchar(32),
   c_sort_order integer,
   c_enabled bool not null default true,
   c_config varchar(4000),
   primary key (id)
);
create table o_ca_filter (
   id bigint not null auto_increment,
   lastmodified datetime not null,
   creationdate datetime not null,
   c_type varchar(50),
   c_sort_order integer,
   c_enabled bool not null default true,
   c_default_visible bool not null default true,
   c_config varchar(4000),
   primary key (id)
);

alter table o_ca_launcher ENGINE = InnoDB;
alter table o_ca_filter ENGINE = InnoDB;


-- Practice
alter table o_qti_assessmentitem_session add column q_attempts bigint default null;
alter table o_qti_assessmentitem_session add column q_externalrefidentifier varchar(64) default null;
create index idx_item_ext_ref_idx on o_qti_assessmentitem_session (q_externalrefidentifier);

alter table o_as_entry add column a_share bit default null;

create table o_practice_resource (
  id bigint not null auto_increment,
   lastmodified datetime not null,
   creationdate datetime not null,
   fk_entry bigint not null,
   p_subident varchar(64) not null,
   fk_test_entry bigint,
   fk_item_collection bigint,
   fk_pool bigint,
   fk_resource_share bigint,
   primary key (id)
);
alter table o_practice_resource ENGINE = InnoDB;

alter table o_practice_resource add constraint pract_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_practice_resource add constraint pract_test_entry_idx foreign key (fk_test_entry) references o_repositoryentry (repositoryentry_id);
alter table o_practice_resource add constraint pract_item_coll_idx foreign key (fk_item_collection) references o_qp_item_collection (id);
alter table o_practice_resource add constraint pract_poll_idx foreign key (fk_pool) references o_qp_pool (id);
alter table o_practice_resource add constraint pract_rsrc_share_idx foreign key (fk_resource_share) references o_olatresource(resource_id);


create table o_practice_global_item_ref (
  id bigint not null auto_increment,
   lastmodified datetime not null,
   creationdate datetime not null,
   p_identifier varchar(64) not null,
   p_level bigint default 0,
   p_attempts bigint default 0,
   p_correct_answers bigint default 0,
   p_incorrect_answers bigint default 0,
   p_last_attempt_date datetime,
   p_last_attempt_passed bool default null,
   fk_identity bigint not null,
   primary key (id)
);
alter table o_practice_global_item_ref ENGINE = InnoDB;

alter table o_practice_global_item_ref add constraint pract_global_ident_idx foreign key (fk_identity) references o_bs_identity(id);

create index idx_pract_global_id_uu_idx on o_practice_global_item_ref (fk_identity,p_identifier);


-- LTI 1.3
alter table o_lti_tool_deployment add column l_nrps bool default true;
alter table o_lti_tool_deployment add column l_context_id varchar(255);

alter table o_lti_tool_deployment add column fk_group_id bigint;

alter table o_lti_tool_deployment add constraint dep_to_group_idx foreign key (fk_group_id) references o_gp_business(group_id);


-- Certificates
alter table o_cer_certificate add column c_external_id varchar(64);
alter table o_cer_certificate add column c_managed_flags varchar(255);


-- Zoom
create table o_zoom_profile (
                                id bigint not null auto_increment,
                                creationdate datetime not null,
                                lastmodified datetime not null,
                                z_name varchar(255) not null,
                                z_status varchar(255) not null,
                                z_lti_key varchar(255) not null,
                                z_mail_domains varchar(1024),
                                z_students_can_host bool default false,
                                z_token varchar(255) not null,
                                fk_lti_tool_id bigint not null,
                                primary key (id)
);
alter table o_zoom_profile ENGINE = InnoDB;

create table o_zoom_config (
                               id bigint not null auto_increment,
                               creationdate datetime not null,
                               lastmodified datetime not null,
                               z_description varchar(255),
                               fk_profile bigint not null,
                               fk_lti_tool_deployment_id bigint not null,
                               primary key (id)
);
alter table o_zoom_config ENGINE = InnoDB;

alter table o_zoom_profile add constraint zoom_profile_tool_idx foreign key (fk_lti_tool_id) references o_lti_tool (id);
create index idx_zoom_profile_tool_idx on o_zoom_profile (fk_lti_tool_id);

alter table o_zoom_config add constraint zoom_config_profile_idx foreign key (fk_profile) references o_zoom_profile (id);
create index idx_zoom_config_profile_idx on o_zoom_config (fk_profile);

alter table o_zoom_config add constraint zoom_config_tool_deployment_idx foreign key (fk_lti_tool_deployment_id) references o_lti_tool_deployment (id);
create index idx_zoom_config_tool_deployment_idx on o_zoom_config (fk_lti_tool_deployment_id);

-- External users
alter table o_gp_business add column invitations_coach_enabled bool default true not null;

alter table o_bs_invitation add column i_type varchar(32) default 'binder' not null;
alter table o_bs_invitation add column i_url varchar(512) default null;
alter table o_bs_invitation add column i_roles varchar(255) default null;
alter table o_bs_invitation add column i_registration bool default false not null;
alter table o_bs_invitation add column i_additional_infos mediumtext default null;


