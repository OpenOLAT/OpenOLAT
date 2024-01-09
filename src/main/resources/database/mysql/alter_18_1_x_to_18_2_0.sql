
-- To-dos
alter table o_todo_task add column t_origin_sub_title varchar(500);

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


alter table o_gui_prefs add constraint o_gui_prefs_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_o_gui_prefs_attrclass_idx on o_gui_prefs (g_pref_attributed_class);
create index idx_o_gui_prefs_key_idx on o_gui_prefs (g_pref_key);
