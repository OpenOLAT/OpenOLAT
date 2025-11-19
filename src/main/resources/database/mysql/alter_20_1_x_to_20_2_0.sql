-- Certification program
create table o_cer_program (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime,
   c_identifier varchar(64),
   c_displayname varchar(255) not null,
   c_description text,
   c_status varchar(16) default 'active' not null,
   c_recert_enabled bool default false not null,
   c_recert_mode varchar(16),
   c_recert_creditpoint decimal,
   c_recert_window_enabled bool default false not null,
   c_recert_window bigint default 0 not null,
   c_recert_window_unit varchar(32),
   c_premature_recert_enabled bool default false not null,
   c_validity_enabled bool default false not null,
   c_validity_timelapse bigint default 0 not null,
   c_validity_timelapse_unit varchar(32),
   c_cer_custom_1 varchar(4000),
   c_cer_custom_2 varchar(4000),
   c_cer_custom_3 varchar(4000),
   fk_credit_point_system bigint,
   fk_group bigint not null,
   fk_template bigint,
   fk_resource bigint,
   primary key (id)
);
alter table o_cer_program ENGINE = InnoDB;

alter table o_cer_program add constraint cer_progr_to_group_idx foreign key (fk_group) references o_bs_group (id);
alter table o_cer_program add constraint cer_progr_to_credsys_idx foreign key (fk_credit_point_system) references o_cp_system (id);

alter table o_cer_program add constraint cer_progr_to_template_idx foreign key (fk_template) references o_cer_template (id);

alter table o_cer_program add constraint cer_progr_to_resource_idx foreign key (fk_resource) references o_olatresource (resource_id);

create table o_cer_program_to_organisation (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_program bigint not null,
   fk_organisation bigint not null,
   primary key (id)
);
alter table o_cer_program_to_organisation ENGINE = InnoDB;

alter table o_cer_program_to_organisation add constraint cer_prog_to_prog_idx foreign key (fk_program) references o_cer_program (id);
alter table o_cer_program_to_organisation add constraint cer_prog_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);

create table o_cer_program_to_element (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_program bigint not null,
   fk_element bigint not null,
   primary key (id)
);
alter table o_cer_program_to_element ENGINE = InnoDB;

alter table o_cer_program_to_element add constraint cer_prog_to_el_prog_idx foreign key (fk_program) references o_cer_program (id);
alter table o_cer_program_to_element add constraint cer_prog_to_el_element_idx foreign key (fk_element) references o_cur_curriculum_element (id);

create table o_cer_program_mail_config (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime,
   c_title varchar(255),
   c_type varchar(32) not null,
   c_status varchar(16) not null,
   c_time bigint default 0 not null,
   c_time_unit varchar(32),
   c_balance_too_low bool default false not null,
   c_i18n_suffix varchar(64) not null,
   c_i18n_customized bool default false not null,
   fk_program bigint not null,
   primary key (id)
);
alter table o_cer_program_mail_config ENGINE = InnoDB;

alter table o_cer_program_mail_config add constraint cer_mconfig_to_prog_idx foreign key (fk_program) references o_cer_program (id);

create table o_cer_program_log (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_certificate bigint not null,
   fk_mail_configuration bigint,
   primary key (id)
);
alter table o_cer_program_log ENGINE = InnoDB;

alter table o_cer_program_log add constraint cer_plog_to_cert_idx foreign key (fk_certificate) references o_cer_certificate (id);
alter table o_cer_program_log add constraint cer_plog_to_config_idx foreign key (fk_mail_configuration) references o_cer_program_mail_config (id);

-- Certificate
alter table o_cer_certificate add column c_revoked bool default false not null;
alter table o_cer_certificate add column c_recertification_count bigint;
alter table o_cer_certificate add column c_recertification_win_date datetime;
alter table o_cer_certificate add column c_recertification_paused bool default false not null;
alter table o_cer_certificate add column fk_certification_program bigint;
alter table o_cer_certificate add column fk_uploaded_by bigint;
alter table o_cer_certificate add column c_revocation_date datetime;
alter table o_cer_certificate add column c_removal_date datetime;

alter table o_cer_certificate add constraint cer_to_cprog_idx foreign key (fk_certification_program) references o_cer_program (id);

alter table o_cer_certificate add constraint cer_to_upload_idx foreign key (fk_uploaded_by) references o_bs_identity (id);

-- Credit point
alter table o_cp_system add column c_org_restrictions bool default false not null;
alter table o_cp_system add column c_roles_restrictions bool default false not null;

create table o_cp_system_to_organisation (
  id bigint not null auto_increment,
  creationdate datetime not null,
  fk_cp_system bigint not null,
  fk_organisation bigint not null,
  primary key (id)
);
alter table o_cp_system_to_organisation ENGINE = InnoDB;

alter table o_cp_system_to_organisation add constraint rel_cpo_to_cp_sys_idx foreign key (fk_cp_system) references o_cp_system(id);
alter table o_cp_system_to_organisation add constraint rel_cpo_to_org_idx foreign key (fk_organisation) references o_org_organisation(id);



