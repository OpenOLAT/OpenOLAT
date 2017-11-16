create table o_gta_mark (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  fk_tasklist_id int8 not null,
  fk_marker_identity_id int8 not null,
  fk_participant_identity_id int8 not null,
  primary key (id)
);

alter table o_gta_mark ENGINE = InnoDB;

alter table o_gta_mark add constraint gtamark_tasklist_idx foreign key (fk_tasklist_id) references o_gta_task_list (id);

-- temporary key
alter table o_temporarykey add column fk_identity_id bigint;

create index idx_tempkey_identity_idx on o_temporarykey (fk_identity_id);


-- taxonomy
create table o_tax_taxonomy (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  t_identifier varchar(64),
  t_displayname varchar(255) not null,
  t_description mediumtext,
  t_external_id varchar(64),
  t_managed_flags varchar(255),
  t_directory_path varchar(255),
  t_directory_lost_found_path varchar(255),
  fk_group bigint not null,
  primary key (id)
);
alter table o_tax_taxonomy ENGINE = InnoDB;

alter table o_tax_taxonomy add constraint tax_to_group_idx foreign key (fk_group) references o_bs_group (id);


create table o_tax_taxonomy_level_type (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  t_identifier varchar(64),
  t_displayname varchar(255) not null,
  t_description mediumtext,
  t_external_id varchar(64),
  t_managed_flags varchar(255),
  t_css_class varchar(64),
  t_visible bit default 1,
  t_library_docs bit default 1,
  t_library_manage bit default 1,
  t_library_teach_read bit default 1,
  t_library_teach_readlevels bigint not null default 0,
  t_library_teach_write bit default 0,
  t_library_have_read bit default 1,
  t_library_target_read bit default 1,
  fk_taxonomy bigint not null,
  primary key (id)
);
alter table o_tax_taxonomy_level_type ENGINE = InnoDB;

alter table o_tax_taxonomy_level_type add constraint tax_type_to_taxonomy_idx foreign key (fk_taxonomy) references o_tax_taxonomy (id);


create table o_tax_taxonomy_type_to_type (
  id bigint not null auto_increment,
  fk_type bigint not null,
  fk_allowed_sub_type bigint not null,
  primary key (id)
);
alter table o_tax_taxonomy_type_to_type ENGINE = InnoDB;

alter table o_tax_taxonomy_type_to_type add constraint tax_type_to_type_idx foreign key (fk_type) references o_tax_taxonomy_level_type (id);
create index idx_tax_type_to_type_idx on o_tax_taxonomy_type_to_type (fk_type);
alter table o_tax_taxonomy_type_to_type add constraint tax_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_tax_taxonomy_level_type (id);
create index idx_tax_type_to_sub_type_idx on o_tax_taxonomy_type_to_type (fk_allowed_sub_type);


create table o_tax_taxonomy_level (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  t_identifier varchar(64),
  t_displayname varchar(255) not null,
  t_description mediumtext,
  t_external_id varchar(64),
  t_sort_order bigint,
  t_directory_path varchar(255),
  t_m_path_keys varchar(255),
  t_m_path_identifiers varchar(1024),
  t_enabled bit default 1,
  t_managed_flags varchar(255),
  fk_taxonomy bigint not null,
  fk_parent bigint,
  fk_type bigint,
  primary key (id)
);
alter table o_tax_taxonomy_level ENGINE = InnoDB;

alter table o_tax_taxonomy_level add constraint tax_level_to_taxonomy_idx foreign key (fk_taxonomy) references o_tax_taxonomy (id);
alter table o_tax_taxonomy_level add constraint tax_level_to_tax_level_idx foreign key (fk_parent) references o_tax_taxonomy_level (id);
alter table o_tax_taxonomy_level add constraint tax_level_to_type_idx foreign key (fk_type) references o_tax_taxonomy_level_type (id);


create table o_tax_taxonomy_competence (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  t_type varchar(16),
  t_achievement float(65,30) default null,
  t_reliability float(65,30) default null,
  t_expiration_date datetime,
  t_external_id varchar(64),
  t_source_text varchar(255),
  t_source_url varchar(255),
  fk_level bigint not null,
  fk_identity bigint not null,
  primary key (id)
);
alter table o_tax_taxonomy_competence ENGINE = InnoDB;

alter table o_tax_taxonomy_competence add constraint tax_comp_to_tax_level_idx foreign key (fk_level) references o_tax_taxonomy_level (id);
alter table o_tax_taxonomy_competence add constraint tax_level_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);


create table o_tax_competence_audit_log (
  id bigint not null auto_increment,
  creationdate datetime not null,
  t_action varchar(32),
  t_val_before mediumtext,
  t_val_after mediumtext,
  t_message mediumtext,
  fk_taxonomy bigint,
  fk_taxonomy_competence bigint,
  fk_identity bigint,
  fk_author bigint,
  primary key (id)
);


-- qpool
alter table o_qp_item add column fk_taxonomy_level_v2 bigint;

alter table o_qp_item add constraint idx_qp_pool_2_tax_id foreign key (fk_taxonomy_level_v2) references o_tax_taxonomy_level(id);

alter table o_qp_item drop foreign key idx_qp_pool_2_field_id;




