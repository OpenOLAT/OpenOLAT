
-- Evaluation forms
alter table o_eva_form_response drop column e_responsedatatype;
alter table o_eva_form_response add column e_file_response_path varchar(4000);

-- access control
alter table o_ac_offer add column confirmation_email bool default false;

-- qti
alter table o_qti_assessmentitem_session add column q_to_review bool default false;

-- licenses
create table o_lic_license_type (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  l_name varchar(128) not null unique,
  l_text text,
  l_css_class varchar(64),
  l_predefined bool not null default false,
  l_sort_order int8 not null,
  primary key (id)
);

create table o_lic_license_type_activation (
  id bigserial,
  creationdate timestamp not null,
  l_handler_type varchar(128) not null,
  fk_license_type_id int8 not null,
  primary key (id)
);

create table o_lic_license (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  l_resname varchar(50) not null,
  l_resid int8 not null,
  l_licensor varchar(4000),
  l_freetext text,
  fk_license_type_id int8 not null,
  primary key (id)
);

alter table o_lic_license_type_activation add constraint lic_activation_type_fk foreign key (fk_license_type_id) references o_lic_license_type (id);
create index lic_activation_type_idx on o_lic_license_type_activation (fk_license_type_id);
alter table o_lic_license add constraint lic_license_type_fk foreign key (fk_license_type_id) references o_lic_license_type (id);
create index lic_license_type_idx on o_lic_license (fk_license_type_id);
create unique index lic_license_ores_idx on o_lic_license (l_resid, l_resname);

alter table o_qp_item_audit_log add column q_lic_before text;
alter table o_qp_item_audit_log add column q_lic_after text;
