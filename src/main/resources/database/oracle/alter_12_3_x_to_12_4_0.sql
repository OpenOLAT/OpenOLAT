
-- Evaluation forms
alter table o_eva_form_response drop column e_responsedatatype;
alter table o_eva_form_response add column e_file_response_path varchar(4000);

-- access control
alter table o_ac_offer add confirmation_email number default 0;

-- qti
alter table o_qti_assessmentitem_session add q_to_review  number default 0;

-- licenses
create table o_lic_license_type (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  l_name varchar2(128) not null unique,
  l_text CLOB,
  l_css_class varchar2(64),
  l_predefined number not null,
  l_sort_order number(20) not null,
  primary key (id)
);

create table o_lic_license_type_activation (
  id number(20) generated always as identity,
  creationdate date not null,
  l_handler_type varchar2(128) not null,
  fk_license_type_id number(20) not null,
  primary key (id)
);

create table o_lic_license (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  l_resname varchar2(50) not null,
  l_resid number(20)  not null,
  l_licensor varchar2(4000),
  l_freetext CLOB,
  fk_license_type_id number(20) not null,
  primary key (id)
);

alter table o_lic_license_type_activation add constraint lic_activation_type_fk foreign key (fk_license_type_id) references o_lic_license_type (id);
create index lic_activation_type_idx on o_lic_license_type_activation (fk_license_type_id);
alter table o_lic_license add constraint lic_license_type_fk foreign key (fk_license_type_id) references o_lic_license_type (id);
create index lic_license_type_idx on o_lic_license (fk_license_type_id);
create unique index lic_license_ores_idx on o_lic_license (l_resid, l_resname);

alter table o_qp_item_audit_log add column q_lic_before CLOB;
alter table o_qp_item_audit_log add column q_lic_after CLOB;

