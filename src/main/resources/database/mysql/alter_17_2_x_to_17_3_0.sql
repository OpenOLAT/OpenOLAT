
-- Projects
create table o_proj_project (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_external_ref varchar(128),
   p_status varchar(32),
   p_title varchar(128),
   p_teaser varchar(150),
   p_description longtext,
   fk_creator bigint not null,
   fk_group bigint not null,
   primary key (id)
);
create table o_proj_project_to_org (
  id bigint not null auto_increment,
  creationdate datetime not null,
  fk_project bigint not null,
  fk_organisation bigint not null,
  primary key (id)
);
create table o_proj_project_user_info (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_last_visit_date datetime,
   fk_project bigint not null,
   fk_identity bigint not null,
   primary key (id)
);
create table o_proj_artefact (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_type varchar(32),
   p_content_modified_date datetime not null,
   fk_content_modified_by bigint not null,
   p_status varchar(32),
   fk_project bigint not null,
   fk_creator bigint not null,
   fk_group bigint not null,
   primary key (id)
);
create table o_proj_artefact_to_artefact (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_artefact1 bigint not null,
   fk_artefact2 bigint not null,
   fk_project bigint not null,
   fk_creator bigint not null,
   primary key (id)
);
create table o_proj_file (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   fk_metadata bigint not null,
   fk_artefact bigint not null,
   primary key (id)
);
create table o_proj_note (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_title varchar(128),
   p_text longtext,
   fk_artefact bigint not null,
   primary key (id)
);
create table o_proj_activity (
   id bigint not null auto_increment,
   creationdate datetime not null,
   p_action varchar(32) not null,
   p_action_target varchar(32) not null,
   p_before longtext,
   p_after longtext,
   p_temp_identifier varchar(100),
   fk_doer bigint not null,
   fk_project bigint not null,
   fk_artefact bigint,
   fk_artefact_reference bigint,
   fk_member bigint,
   fk_organisation bigint,
   primary key (id)
);
alter table o_proj_project ENGINE = InnoDB;
alter table o_proj_project_to_org ENGINE = InnoDB;
alter table o_proj_project_user_info ENGINE = InnoDB;
alter table o_proj_artefact ENGINE = InnoDB;
alter table o_proj_artefact_to_artefact ENGINE = InnoDB;
alter table o_proj_file ENGINE = InnoDB;
alter table o_proj_note ENGINE = InnoDB;
alter table o_proj_activity ENGINE = InnoDB;

alter table o_proj_project add constraint project_creator_idx foreign key (fk_creator) references o_bs_identity(id);
alter table o_proj_project add constraint project_group_idx foreign key (fk_group) references o_bs_group (id);
alter table o_proj_project_to_org add constraint rel_pto_project_idx foreign key (fk_project) references o_proj_project (id);
alter table o_proj_project_to_org add constraint rel_pto_org_idx foreign key (fk_organisation) references o_org_organisation (id);
alter table o_proj_project_user_info add constraint rel_pui_project_idx foreign key (fk_project) references o_proj_project (id);
alter table o_proj_project_user_info add constraint rel_pui_identity_idx foreign key (fk_identity) references o_bs_identity(id);

alter table o_proj_artefact add constraint artefact_modby_idx foreign key (fk_content_modified_by) references o_bs_identity(id);
alter table o_proj_artefact add constraint artefact_project_idx foreign key (fk_project) references o_proj_project (id);
alter table o_proj_artefact add constraint artefact_creator_idx foreign key (fk_creator) references o_bs_identity(id);
alter table o_proj_artefact add constraint artefact_group_idx foreign key (fk_group) references o_bs_group (id);
alter table o_proj_artefact_to_artefact add constraint projata_artefact1_idx foreign key (fk_artefact1) references o_proj_artefact (id);
alter table o_proj_artefact_to_artefact add constraint projata_artefact2_idx foreign key (fk_artefact2) references o_proj_artefact (id);
alter table o_proj_artefact_to_artefact add constraint projata_project_idx foreign key (fk_project) references o_proj_project (id);
alter table o_proj_artefact_to_artefact add constraint projata_creator_idx foreign key (fk_creator) references o_bs_identity(id);

alter table o_proj_file add constraint file_artefact_idx foreign key (fk_artefact) references o_proj_artefact (id);
alter table o_proj_file add constraint file_metadata_idx foreign key (fk_metadata) references o_vfs_metadata(id);
alter table o_proj_note add constraint note_artefact_idx foreign key (fk_artefact) references o_proj_artefact (id);

alter table o_proj_activity add constraint activity_doer_idx foreign key (fk_doer) references o_bs_identity (id);
alter table o_proj_activity add constraint activity_project_idx foreign key (fk_project) references o_proj_project (id);
alter table o_proj_activity add constraint activity_artefact_idx foreign key (fk_artefact) references o_proj_artefact (id);
alter table o_proj_activity add constraint activity_artefact_ref_idx foreign key (fk_artefact_reference) references o_proj_artefact (id);
alter table o_proj_activity add constraint activity_member_idx foreign key (fk_member) references o_bs_identity (id);
alter table o_proj_activity add constraint activity_organisation_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_activity_temp_ident_idx on o_proj_activity (p_temp_identifier);
