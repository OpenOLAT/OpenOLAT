
-- To-dos
alter table o_todo_task add column t_origin_sub_title varchar(500);
alter table o_todo_task add column fk_collection bigint;

alter table o_todo_task add constraint todo_task_coll_idx foreign key (fk_collection) references o_todo_task (id);


-- Media
create table o_media_version_metadata (
   id bigint not null auto_increment,
   creationdate datetime not null,
   p_url varchar(1024) default null,
   p_width bigint default null,
   p_height bigint default null,
   p_length varchar(32) default null,
   p_format varchar(32) default null,
   primary key (id)
);
alter table o_media_version_metadata ENGINE = InnoDB;

alter table o_media_version add column fk_version_metadata bigint;

alter table o_media_version add constraint media_version_version_metadata_idx foreign key (fk_version_metadata) references o_media_version_metadata(id);


-- Weighted score
alter table o_as_entry add column a_weighted_score float(65,30) default null;
alter table o_as_entry add column a_score_scale float(65,30) default null;
alter table o_as_entry add column a_weighted_max_score float(65,30) default null;

alter table o_as_eff_statement add column weighted_score float(65,30) default null;


-- Quality management
create table o_qual_generator_override (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_identifier varchar(512) not null,
   q_start datetime,
   q_generator_provider_key bigint,
   fk_generator bigint not null,
   fk_data_collection bigint,
   primary key (id)
);
alter table o_qual_generator_override ENGINE = InnoDB;

alter table o_qual_generator_override add constraint qual_override_to_gen_idx foreign key (fk_generator) references o_qual_generator (id);
alter table o_qual_generator_override add constraint qual_override_to_dc_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
create index idx_override_ident_idx on o_qual_generator_override(q_identifier);

-- Gui Preferences
create table o_gui_prefs (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   fk_identity bigint not null,
   g_pref_attributed_class varchar(512) not null,
   g_pref_key varchar(512) not null,
   g_pref_value longtext not null,
   primary key (id)
);
alter table o_gui_prefs ENGINE = InnoDB;

alter table o_gui_prefs add constraint o_gui_prefs_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_o_gui_prefs_attrclass_idx on o_gui_prefs (g_pref_attributed_class);
create index idx_o_gui_prefs_key_idx on o_gui_prefs (g_pref_key);

-- Assessment inspection
create table o_as_inspection_configuration (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   a_name varchar(255),
   a_duration bigint not null,
   a_overview_options varchar(1000),
   a_restrictaccessips bool not null default false,
   a_ips text(32000),
   a_safeexambrowser bool not null default false,
   a_safeexambrowserkey text(32000),
   a_safeexambrowserconfig_xml mediumtext,
   a_safeexambrowserconfig_plist mediumtext,
   a_safeexambrowserconfig_pkey varchar(255),
   a_safeexambrowserconfig_dload bool default true not null,
   a_safeexambrowserhint mediumtext,
   fk_entry bigint not null,
   primary key (id)
);
alter table o_as_inspection_configuration ENGINE = InnoDB;

alter table o_as_inspection_configuration add constraint as_insp_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);

create table o_as_inspection (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   a_subident varchar(512),
   a_from datetime not null,
   a_to datetime not null,
   a_extra_time bigint,
   a_access_code varchar(128),
   a_start_time datetime,
   a_end_time datetime,
   a_end_by varchar(16),
   a_effective_duration bigint,
   a_comment mediumtext,
   a_status varchar(16) not null default 'published',
   fk_identity bigint not null,
   fk_configuration bigint not null,
   primary key (id)
);
alter table o_as_inspection ENGINE = InnoDB;

alter table o_as_inspection add constraint as_insp_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_as_inspection add constraint as_insp_to_config_idx foreign key (fk_configuration) references o_as_inspection_configuration (id);
create index idx_as_insp_subident_idx on o_as_inspection (a_subident);
create index idx_as_insp_endtime_idx on o_as_inspection (a_end_time);
create index idx_as_insp_fromto_idx on o_as_inspection (a_from,a_to);

create table o_as_inspection_log (
   id bigint not null auto_increment,
   creationdate datetime not null,
   a_action varchar(32) not null,
   a_before mediumtext,
   a_after mediumtext,
   fk_doer bigint,
   fk_inspection bigint not null,
   primary key (id)
);
alter table o_as_inspection_log ENGINE = InnoDB;

alter table o_as_inspection_log add constraint as_insp_log_to_ident_idx foreign key (fk_doer) references o_bs_identity (id);
alter table o_as_inspection_log add constraint as_log_to_insp_idx foreign key (fk_inspection) references o_as_inspection (id);

