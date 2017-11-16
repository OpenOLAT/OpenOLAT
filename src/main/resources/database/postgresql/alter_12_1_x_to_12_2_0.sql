create table o_gta_mark (
  id bigserial not null,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  fk_tasklist_id int8 not null,
  fk_marker_identity_id int8 not null,
  fk_participant_identity_id int8 not null,
  primary key (id)
);

alter table o_gta_mark add constraint gtamark_tasklist_idx foreign key (fk_tasklist_id) references o_gta_task_list (id);
create index idx_gtamark_tasklist_idx on o_gta_mark (fk_tasklist_id);

-- temporary key
alter table o_temporarykey add column fk_identity_id int8;

create index idx_tempkey_identity_idx on o_temporarykey (fk_identity_id);


-- taxonomy
create table o_tax_taxonomy (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  t_identifier varchar(64),
  t_displayname varchar(255) not null,
  t_description text,
  t_external_id varchar(64),
  t_managed_flags varchar(255),
  t_directory_path varchar(255),
  t_directory_lost_found_path varchar(255),
  fk_group int8 not null,
  primary key (id)
);

alter table o_tax_taxonomy add constraint tax_to_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_tax_to_group_idx on o_tax_taxonomy (fk_group);


create table o_tax_taxonomy_level_type (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  t_identifier varchar(64),
  t_displayname varchar(255) not null,
  t_description text,
  t_external_id varchar(64),
  t_managed_flags varchar(255),
  t_css_class varchar(64),
  t_visible bool default true,
  t_library_docs bool default true,
  t_library_manage bool default true,
  t_library_teach_read bool default true,
  t_library_teach_readlevels int8 not null default 0,
  t_library_teach_write bool default false,
  t_library_have_read bool default true,
  t_library_target_read bool default true,
  fk_taxonomy int8 not null,
  primary key (id)
);

alter table o_tax_taxonomy_level_type add constraint tax_type_to_taxonomy_idx foreign key (fk_taxonomy) references o_tax_taxonomy (id);
create index idx_tax_type_to_taxonomy_idx on o_tax_taxonomy_level_type (fk_taxonomy);


create table o_tax_taxonomy_type_to_type (
  id bigserial,
  fk_type int8 not null,
  fk_allowed_sub_type int8 not null,
  primary key (id)
);

alter table o_tax_taxonomy_type_to_type add constraint tax_type_to_type_idx foreign key (fk_type) references o_tax_taxonomy_level_type (id);
create index idx_tax_type_to_type_idx on o_tax_taxonomy_type_to_type (fk_type);
alter table o_tax_taxonomy_type_to_type add constraint tax_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_tax_taxonomy_level_type (id);
create index idx_tax_type_to_sub_type_idx on o_tax_taxonomy_type_to_type (fk_allowed_sub_type);


create table o_tax_taxonomy_level (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  t_identifier varchar(64),
  t_displayname varchar(255) not null,
  t_description text,
  t_external_id varchar(64),
  t_sort_order int8,
  t_directory_path varchar(255),
  t_m_path_keys varchar(255),
  t_m_path_identifiers varchar(1024),
  t_enabled bool default true,
  t_managed_flags varchar(255),
  fk_taxonomy int8 not null,
  fk_parent int8,
  fk_type int8,
  primary key (id)
);

alter table o_tax_taxonomy_level add constraint tax_level_to_taxonomy_idx foreign key (fk_taxonomy) references o_tax_taxonomy (id);
create index idx_tax_level_to_taxonomy_idx on o_tax_taxonomy_level (fk_taxonomy);
alter table o_tax_taxonomy_level add constraint tax_level_to_tax_level_idx foreign key (fk_parent) references o_tax_taxonomy_level (id);
create index idx_tax_level_to_tax_level_idx on o_tax_taxonomy_level (fk_parent);
alter table o_tax_taxonomy_level add constraint tax_level_to_type_idx foreign key (fk_type) references o_tax_taxonomy_level_type (id);
create index idx_tax_level_to_type_idx on o_tax_taxonomy_level (fk_type);


create table o_tax_taxonomy_competence (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  t_type varchar(16),
  t_achievement decimal default null,
  t_reliability decimal default null,
  t_expiration_date timestamp,
  t_external_id varchar(64),
  t_source_text varchar(255),
  t_source_url varchar(255),
  fk_level int8 not null,
  fk_identity int8 not null,
  primary key (id)
);

alter table o_tax_taxonomy_competence add constraint tax_comp_to_tax_level_idx foreign key (fk_level) references o_tax_taxonomy_level (id);
create index idx_tax_comp_to_tax_level_idx on o_tax_taxonomy_competence (fk_level);
alter table o_tax_taxonomy_competence add constraint tax_level_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_tax_level_to_ident_idx on o_tax_taxonomy_competence (fk_identity);


create table o_tax_competence_audit_log (
  id bigserial,
  creationdate timestamp not null,
  t_action varchar(32),
  t_val_before text,
  t_val_after text,
  t_message text,
  fk_taxonomy int8,
  fk_taxonomy_competence int8,
  fk_identity int8,
  fk_author int8,
  primary key (id)
);


-- qpool
alter table o_qp_item add column fk_taxonomy_level_v2 int8;

alter table o_qp_item add constraint idx_qp_pool_2_tax_id foreign key (fk_taxonomy_level_v2) references o_tax_taxonomy_level(id);
create index idx_item_taxlon_idx on o_qp_item (fk_taxonomy_level_v2);

alter table o_qp_item drop constraint idx_qp_pool_2_field_id;
drop index idx_item_taxon_idx;




