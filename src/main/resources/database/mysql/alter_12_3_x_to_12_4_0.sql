
-- Evaluation forms
alter table o_eva_form_response drop column e_responsedatatype;
alter table o_eva_form_response add column e_file_response_path varchar(4000);

-- access control
alter table o_ac_offer add column confirmation_email bit default 0;

-- qti
alter table o_qti_assessmentitem_session add column q_to_review bit default 0;

-- licenses
create table o_lic_license_type (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  l_name varchar(128) not null unique,
  l_text mediumtext,
  l_css_class varchar(64),
  l_predefined boolean not null default false,
  l_sort_order int not null,
  primary key (id)
);

create table o_lic_license_type_activation (
  id bigint not null auto_increment,
  creationdate timestamp not null,
  l_handler_type varchar(128) not null,
  fk_license_type_id bigint not null,
  primary key (id)
);

create table o_lic_license (
  id bigint not null auto_increment,
  creationdate timestamp not null,
  lastmodified datetime not null,
  l_resname varchar(50) not null,
  l_resid bigint not null,
  l_licensor varchar(4000),
  l_freetext mediumtext,
  fk_license_type_id bigint not null,
  primary key (id)
);

alter table o_lic_license_type ENGINE = InnoDB;
alter table o_lic_license_type_activation ENGINE = InnoDB;
alter table o_lic_license ENGINE = InnoDB;


alter table o_lic_license_type_activation add constraint lic_activation_type_fk foreign key (fk_license_type_id) references o_lic_license_type (id);
create index lic_activation_type_idx on o_lic_license_type_activation (fk_license_type_id);
alter table o_lic_license add constraint lic_license_type_fk foreign key (fk_license_type_id) references o_lic_license_type (id);
create index lic_license_type_idx on o_lic_license (fk_license_type_id);
create unique index lic_license_ores_idx on o_lic_license (l_resid, l_resname);

alter table o_qp_item_audit_log add column q_lic_before mediumtext;
alter table o_qp_item_audit_log add column q_lic_after mediumtext;
