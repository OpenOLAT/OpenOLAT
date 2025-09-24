-- Certification program
create table o_cer_program (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date,
   c_identifier varchar(64),
   c_displayname varchar(255) not null,
   c_description CLOB,
   c_status varchar(16) default 'active' not null,
   c_recert_enabled number default 0 not null,
   c_recert_mode varchar(16),
   c_recert_creditpoint decimal,
   c_recert_window_enabled number default 0 not null,
   c_recert_window number(20) default 0 not null,
   c_recert_window_unit varchar(32),
   c_premature_recert_enabled number default 0 not null,
   c_validity_enabled number default 0 not null,
   c_validity_timelapse number(20) default 0 not null,
   c_validity_timelapse_unit varchar(32),
   c_cer_custom_1 varchar(4000),
   c_cer_custom_2 varchar(4000),
   c_cer_custom_3 varchar(4000),
   fk_credit_point_system number(20),
   fk_group number(20) not null,
   fk_template number(20),
   fk_resource number(20),
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
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   fk_program number(20) not null,
   fk_organisation number(20) not null,
   primary key (id)
);

alter table o_cer_program_to_organisation add constraint cer_prog_to_prog_idx foreign key (fk_program) references o_cer_program (id);
create index idx_cer_prog_to_prog_idx on o_cer_program_to_organisation (fk_program);
alter table o_cer_program_to_organisation add constraint cer_prog_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_cer_prog_to_org_idx on o_cer_program_to_organisation (fk_organisation);

create table o_cer_program_to_element (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   fk_program number(20) not null,
   fk_element number(20) not null,
   primary key (id)
);

alter table o_cer_program_to_element add constraint cer_prog_to_el_prog_idx foreign key (fk_program) references o_cer_program (id);
create index idx_cer_prog_to_el_prog_idx on o_cer_program_to_element (fk_program);
alter table o_cer_program_to_element add constraint cer_prog_to_el_element_idx foreign key (fk_element) references o_cur_curriculum_element (id);
create index idx_cer_prog_to_el_element_idx on o_cer_program_to_element (fk_element);

-- Certificate
alter table o_cer_certificate add c_recertification_count number(20);
alter table o_cer_certificate add c_recertification_win_date date;
alter table o_cer_certificate add c_recertification_paused number default 0 not null;
alter table o_cer_certificate add fk_certification_program number(20);

alter table o_cer_certificate add constraint cer_to_cprog_idx foreign key (fk_certification_program) references o_cer_program (id);
create index idx_cer_to_cprog_idx on o_cer_certificate (fk_certification_program);



