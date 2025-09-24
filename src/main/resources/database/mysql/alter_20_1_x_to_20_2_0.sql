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


-- Certificate
alter table o_cer_certificate add column c_recertification_count bigint;
alter table o_cer_certificate add column c_recertification_win_date datetime;
alter table o_cer_certificate add column c_recertification_paused bool default false not null;
alter table o_cer_certificate add column fk_certification_program bigint;

alter table o_cer_certificate add constraint cer_to_cprog_idx foreign key (fk_certification_program) references o_cer_program (id);



