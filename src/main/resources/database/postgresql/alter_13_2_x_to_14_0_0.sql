-- vfs metadata
create table o_vfs_metadata (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   f_uuid varchar(64) not null,
   f_deleted bool default false not null,
   f_filename varchar(256) not null,
   f_relative_path varchar(2048) not null,
   f_directory bool default false,
   f_lastmodified timestamp not null,
   f_size bigint default 0,
   f_uri varchar(2000) not null,
   f_uri_protocol varchar(16) not null,
   f_cannot_thumbnails bool default false,
   f_download_count bigint default 0,
   f_comment varchar(32000),
   f_title varchar(2000),
   f_publisher varchar(2000),
   f_creator varchar(2000),
   f_source varchar(2000),
   f_city varchar(256),
   f_pages varchar(2000),
   f_language varchar(16),
   f_url varchar(2000),
   f_pub_month varchar(16),
   f_pub_year varchar(16),
   f_license_type_name varchar(256),
   f_license_text text,
   f_licensor varchar(4000),
   f_locked_date timestamp,
   f_locked bool default false,
   f_revision_nr bigint default 0 not null,
   f_revision_comment varchar(32000),
   f_migrated varchar(12),
   f_m_path_keys varchar(1024),
   fk_locked_identity bigint,
   fk_license_type bigint,
   fk_author bigint,
   fk_parent bigint,
   primary key (id)
);

alter table o_vfs_metadata add constraint fmeta_to_author_idx foreign key (fk_locked_identity) references o_bs_identity (id);
create index idx_fmeta_to_author_idx on o_vfs_metadata (fk_locked_identity);
alter table o_vfs_metadata add constraint fmeta_to_lockid_idx foreign key (fk_author) references o_bs_identity (id);
create index idx_fmeta_to_lockid_idx on o_vfs_metadata (fk_author);
alter table o_vfs_metadata add constraint fmeta_to_lic_type_idx foreign key (fk_license_type) references o_lic_license_type (id);
create index idx_fmeta_to_lic_type_idx on o_vfs_metadata (fk_license_type);
alter table o_vfs_metadata add constraint fmeta_to_parent_idx foreign key (fk_parent) references o_vfs_metadata (id);
create index idx_fmeta_to_parent_idx on o_vfs_metadata (fk_parent);
create index f_m_path_keys_idx on o_vfs_metadata (f_m_path_keys);
create index f_m_rel_path_idx on o_vfs_metadata (f_relative_path);
create index f_m_filename_idx on o_vfs_metadata (f_filename);
create index f_m_file_idx on o_vfs_metadata (f_relative_path,f_filename);
create index f_m_uuid_idx on o_vfs_metadata (f_uuid);

create table o_vfs_thumbnail (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   f_size bigint default 0 not null,
   f_max_width bigint default 0 not null,
   f_max_height bigint default 0 not null,
   f_final_width bigint default 0 not null,
   f_final_height bigint default 0 not null,
   f_fill bool default false not null,
   f_filename varchar(256) not null,
   fk_metadata bigint not null,
   primary key (id)
);

alter table o_vfs_thumbnail add constraint fthumb_to_meta_idx foreign key (fk_metadata) references o_vfs_metadata (id);
create index idx_fthumb_to_meta_idx on o_vfs_thumbnail (fk_metadata);


create table o_vfs_revision (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   f_revision_size bigint default 0 not null,
   f_revision_nr bigint default 0 not null,
   f_revision_filename varchar(256) not null,
   f_revision_comment varchar(32000),
   f_revision_lastmodified timestamp not null,
   f_comment varchar(32000),
   f_title varchar(2000),
   f_publisher varchar(2000),
   f_creator varchar(2000),
   f_source varchar(2000),
   f_city varchar(256),
   f_pages varchar(2000),
   f_language varchar(16),
   f_url varchar(2048),
   f_pub_month varchar(16),
   f_pub_year varchar(16),
   f_license_type_name varchar(256),
   f_license_text text,
   f_licensor varchar(4000),
   fk_license_type bigint,
   fk_author bigint,
   fk_metadata bigint not null,
   primary key (id)
);

alter table o_vfs_revision add constraint fvers_to_author_idx foreign key (fk_author) references o_bs_identity (id);
create index idx_fvers_to_author_idx on o_vfs_revision (fk_author);
alter table o_vfs_revision add constraint fvers_to_meta_idx foreign key (fk_metadata) references o_vfs_metadata (id);
create index idx_fvers_to_meta_idx on o_vfs_revision (fk_metadata);
alter table o_vfs_revision add constraint fvers_to_lic_type_idx foreign key (fk_license_type) references o_lic_license_type (id);
create index idx_fvers_to_lic_type_idx on o_vfs_revision (fk_license_type);


-- WOPI
create table o_wopi_access (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   o_app varchar(64) not null,
   o_token varchar(64) not null,
   o_expires_at timestamp,
   o_can_edit bool not null,
   o_can_close bool not null,
   o_version_controlled bool not null,
   fk_metadata bigint not null,
   fk_identity bigint not null,
   primary key (id)
);

create unique index idx_wopi_token_idx on o_wopi_access(o_token);
create index idx_wopi_ident_meta_idx on o_wopi_access(fk_identity, fk_metadata);

-- Adobe Connect
create table o_aconnect_meeting (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_sco_id varchar(128) default null,
   a_folder_id varchar(128) default null,
   a_env_name varchar(128) default null,
   a_name varchar(128) not null,
   a_description varchar(2000) default null,
   a_start_date timestamp default null,
   a_end_date timestamp default null,
   fk_entry_id int8 default null,
   a_sub_ident varchar(64) default null,
   fk_group_id int8 default null,
   primary key (id)
);

alter table o_aconnect_meeting add constraint aconnect_meet_entry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_aconnect_meet_entry_idx on o_aconnect_meeting(fk_entry_id);
alter table o_aconnect_meeting add constraint aconnect_meet_grp_idx foreign key (fk_group_id) references o_gp_business (group_id);
create index idx_aconnect_meet_grp_idx on o_aconnect_meeting(fk_group_id);


create table o_aconnect_user (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_principal_id varchar(128) default null,
   a_env_name varchar(128) default null,
   fk_identity_id int8 default null,
   primary key (id)
);

alter table o_aconnect_user add constraint aconn_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_aconn_ident_idx on o_aconnect_user (fk_identity_id);

-- Evaluation form
alter table o_eva_form_survey add e_sub_ident2 varchar(2048);

drop index idx_eva_surv_ores_idx;
create index idx_eva_surv_ores_idx on o_eva_form_survey (e_resid, e_resname, e_sub_ident, e_sub_ident2);

-- Assessment mode
alter table o_lecture_entry_config add column l_assessment_mode bool default null;
alter table o_lecture_entry_config add column l_assessment_mode_lead int8 default null;
alter table o_lecture_entry_config add column l_assessment_mode_followup int8 default null;
alter table o_lecture_entry_config add column l_assessment_mode_ips varchar(2048);
alter table o_lecture_entry_config add column l_assessment_mode_seb varchar(2048);

alter table o_as_mode_course add column fk_lecture_block int8 default null;

alter table o_as_mode_course add constraint as_mode_to_lblock_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_as_mode_to_lblock_idx on o_as_mode_course (fk_lecture_block);



