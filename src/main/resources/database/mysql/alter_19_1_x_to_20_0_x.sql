-- Lecture
alter table o_lecture_block add column l_external_ref varchar(128);

alter table o_lecture_block modify column fk_entry bigint null;
alter table o_lecture_block add column fk_curriculum_element bigint;

alter table o_lecture_block add constraint lec_block_curelem_idx foreign key (fk_curriculum_element) references o_cur_curriculum_element(id);

alter table o_lecture_block_audit_log add column fk_curriculum_element bigint;

-- Curriculum
alter table o_cur_element_type add column c_single_element bool default false not null;
alter table o_cur_element_type add column c_max_repo_entries bigint default -1 not null;
alter table o_cur_element_type add column c_allow_as_root bool default true not null;

alter table o_cur_curriculum_element add column fk_resource bigint;

alter table o_cur_curriculum_element add constraint cur_el_resource_idx foreign key (fk_resource) references o_olatresource (resource_id);

-- Organisations
alter table o_org_organisation add column o_location varchar(255);
create table o_org_email_domain (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  o_domain varchar(255) not null,
  o_enabled bool default true not null,
  o_subdomains_allowed bool default false not null,
  fk_organisation bigint not null,
  primary key (id)
);
alter table o_org_email_domain ENGINE = InnoDB;

alter table o_org_email_domain add constraint org_email_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);

-- Catalog
alter table o_ca_launcher add column c_web_enabled bool default true not null;

-- Reservation
alter table  o_ac_reservation add column userconfirmable bool not null default true;

-- Membership
create table o_bs_group_member_history (
  id bigint not null auto_increment,
  creationdate datetime not null,
  g_role varchar(24) not null,
  g_status varchar(32) not null,
  g_note varchar(2000),
  g_admin_note varchar(2000),
  fk_transfer_origin_id bigint,
  fk_transfer_destination_id bigint,
  fk_creator_id bigint,
  fk_group_id bigint not null,
  fk_identity_id bigint not null,
  primary key (id)
);
alter table o_bs_group_member_history ENGINE = InnoDB;

alter table o_bs_group_member_history add constraint hist_transfer_origin_idx foreign key (fk_transfer_origin_id) references o_olatresource (resource_id);
alter table o_bs_group_member_history add constraint hist_transfer_dest_idx foreign key (fk_transfer_destination_id) references o_olatresource (resource_id);

alter table o_bs_group_member_history add constraint hist_creator_idx foreign key (fk_creator_id) references o_bs_identity (id);
alter table o_bs_group_member_history add constraint hist_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);

alter table o_bs_group_member_history add constraint history_group_idx foreign key (fk_group_id) references o_bs_group (id);


-- Access control
create table o_ac_billing_address (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  a_identifier varchar(255),
  a_name_line_1 varchar(255),
  a_name_line_2 varchar(255),
  a_address_line_1 varchar(255),
  a_address_line_2 varchar(255),
  a_address_line_3 varchar(255),
  a_address_line_4 varchar(255),
  a_pobox varchar(255),
  a_region varchar(255),
  a_zip varchar(255),
  a_city varchar(255),
  a_country varchar(255),
  a_enabled  bool default true not null,
  fk_organisation bigint,
  fk_identity bigint,
  primary key (id)
);
alter table o_ac_order add column fk_billing_address bigint;

alter table o_ac_billing_address ENGINE = InnoDB;

alter table o_ac_billing_address add constraint ac_billing_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
alter table o_ac_billing_address add constraint ac_billing_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);

alter table o_ac_order add constraint ord_billing_idx foreign key (fk_billing_address) references o_ac_billing_address (id);

