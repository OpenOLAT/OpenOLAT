-- Certification program
create table o_cer_program (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp,
   c_identifier varchar(64),
   c_displayname varchar(255) not null,
   c_description text,
   c_status varchar(16) default 'active' not null,
   c_recert_enabled bool default false not null,
   c_recert_mode varchar(16),
   c_recert_creditpoint decimal,
   c_recert_window_enabled bool default false not null,
   c_recert_window int8 default 0 not null,
   c_recert_window_unit varchar(32),
   c_premature_recert_enabled bool default false not null,
   c_validity_enabled bool default false not null,
   c_validity_timelapse int8 default 0 not null,
   c_validity_timelapse_unit varchar(32),
   c_cer_custom_1 varchar(4000),
   c_cer_custom_2 varchar(4000),
   c_cer_custom_3 varchar(4000),
   fk_credit_point_system int8,
   fk_group int8 not null,
   fk_template int8,
   fk_resource int8,
   primary key (id)
);

alter table o_cer_program add constraint cer_progr_to_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_cer_progr_to_group_idx on o_cer_program (fk_group);
alter table o_cer_program add constraint cer_progr_to_credsys_idx foreign key (fk_credit_point_system) references o_cp_system (id);
create index idx_cer_progr_to_credsys_idx on o_cer_program (fk_credit_point_system);

alter table o_cer_program add constraint cer_progr_to_template_idx foreign key (fk_template) references o_cer_template (id);
create index idx_cer_progr_to_template_idx on o_cer_program(fk_template);

alter table o_cer_program add constraint cer_progr_to_resource_idx foreign key (fk_resource) references o_olatresource (resource_id);
create index idx_cer_progr_to_resource_idx on o_cer_program (fk_resource);

create table o_cer_program_to_organisation (
   id bigserial,
   creationdate timestamp not null,
   fk_program int8 not null,
   fk_organisation int8 not null,
   primary key (id)
);

alter table o_cer_program_to_organisation add constraint cer_prog_to_prog_idx foreign key (fk_program) references o_cer_program (id);
create index idx_cer_prog_to_prog_idx on o_cer_program_to_organisation (fk_program);
alter table o_cer_program_to_organisation add constraint cer_prog_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_cer_prog_to_org_idx on o_cer_program_to_organisation (fk_organisation);


create table o_cer_program_to_element (
   id bigserial,
   creationdate timestamp not null,
   fk_program int8 not null,
   fk_element int8 not null,
   primary key (id)
);

alter table o_cer_program_to_element add constraint cer_prog_to_el_prog_idx foreign key (fk_program) references o_cer_program (id);
create index idx_cer_prog_to_el_prog_idx on o_cer_program_to_element (fk_program);
alter table o_cer_program_to_element add constraint cer_prog_to_el_element_idx foreign key (fk_element) references o_cur_curriculum_element (id);
create index idx_cer_prog_to_el_element_idx on o_cer_program_to_element (fk_element);

create table o_cer_program_mail_config (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp,
   c_title varchar(255),
   c_type varchar(32) not null,
   c_status varchar(16) not null,
   c_time int8 default 0 not null,
   c_time_unit varchar(32),
   c_balance_too_low bool default false not null,
   c_i18n_suffix varchar(64) not null,
   c_i18n_customized bool default false not null,
   fk_program int8 not null,
   primary key (id)
);

alter table o_cer_program_mail_config add constraint cer_mconfig_to_prog_idx foreign key (fk_program) references o_cer_program (id);
create index idx_cer_mconfig_to_prog_idx on o_cer_program_mail_config (fk_program);


create table o_cer_program_log (
   id bigserial,
   creationdate timestamp not null,
   fk_certificate int8 not null,
   fk_mail_configuration int8,
   primary key (id)
);

alter table o_cer_program_log add constraint cer_plog_to_cert_idx foreign key (fk_certificate) references o_cer_certificate (id);
create index idx_cer_plog_to_cert_idx on o_cer_program_log (fk_certificate);
alter table o_cer_program_log add constraint cer_plog_to_config_idx foreign key (fk_mail_configuration) references o_cer_program_mail_config (id);
create index idx_cer_plog_to_config_idx on o_cer_program_log (fk_mail_configuration);

-- Certificate
alter table o_cer_certificate add column c_revoked bool default false not null;
alter table o_cer_certificate add column c_recertification_count int8;
alter table o_cer_certificate add column c_recertification_win_date timestamp;
alter table o_cer_certificate add column c_recertification_paused bool default false not null;
alter table o_cer_certificate add column fk_certification_program int8;
alter table o_cer_certificate add column fk_uploaded_by int8;
alter table o_cer_certificate add column c_revocation_date timestamp;
alter table o_cer_certificate add column c_removal_date timestamp;

alter table o_cer_certificate add constraint cer_to_cprog_idx foreign key (fk_certification_program) references o_cer_program (id);
create index idx_cer_to_cprog_idx on o_cer_certificate (fk_certification_program);

alter table o_cer_certificate add constraint cer_to_upload_idx foreign key (fk_uploaded_by) references o_bs_identity (id);
create index idx_cer_to_upload_idx on o_cer_certificate (fk_uploaded_by);

-- Credit point
alter table o_cp_system add column c_org_restrictions bool default false not null;
alter table o_cp_system add column c_roles_restrictions bool default false not null;

create table o_cp_system_to_organisation (
  id bigserial,
  creationdate timestamp not null,
  fk_cp_system int8 not null,
  fk_organisation int8 not null,
  primary key (id)
);

alter table o_cp_system_to_organisation add constraint rel_cpo_to_cp_sys_idx foreign key (fk_cp_system) references o_cp_system(id);
create index idx_rel_cpo_to_cp_sys_idx on o_cp_system_to_organisation (fk_cp_system);
alter table o_cp_system_to_organisation add constraint rel_cpo_to_org_idx foreign key (fk_organisation) references o_org_organisation(id);
create index idx_rel_cpo_to_org_idx on o_cp_system_to_organisation (fk_organisation);


-- Catalog
alter table o_repositoryentry add catalog_sort_priority int8 null;
alter table o_cur_curriculum_element add c_catalog_sort_priority int8 null;
