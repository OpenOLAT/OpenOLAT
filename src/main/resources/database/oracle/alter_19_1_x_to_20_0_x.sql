-- Lecture
alter table o_lecture_block add l_external_ref varchar(128);

alter table o_lecture_block modify (fk_entry null);
alter table o_lecture_block add fk_curriculum_element number(20);

alter table o_lecture_block add constraint lec_block_curelem_idx foreign key (fk_curriculum_element) references o_cur_curriculum_element(id);
create index idx_lec_block_curelem_idx on o_lecture_block(fk_curriculum_element);

alter table o_lecture_block_audit_log add fk_curriculum_element number(20);

-- Curriculum
alter table o_cur_element_type add c_single_element number default 0 not null;
alter table o_cur_element_type add c_max_repo_entries number(20) default -1 not null;
alter table o_cur_element_type add c_allow_as_root number default 1 not null;

alter table o_cur_curriculum_element add fk_resource number(20);

alter table o_cur_curriculum_element add constraint cur_el_resource_idx foreign key (fk_resource) references o_olatresource (resource_id);
create index idx_cur_el_resource_idx on o_cur_curriculum_element (fk_resource);

-- Organisations
alter table o_org_organisation add o_location varchar(255);
create table o_org_email_domain (
  id number(20) GENERATED ALWAYS AS IDENTITY,
  creationdate date not null,
  lastmodified date not null,
  o_domain varchar(255) not null,
  o_enabled number default 1 not null,
  o_subdomains_allowed number default 0 not null,
  fk_organisation number(20) not null,
  primary key (id)
);

alter table o_org_email_domain add constraint org_email_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_org_email_to_org_idx on o_org_email_domain (fk_organisation);

-- Catalog
alter table o_ca_launcher add c_web_enabled number default 1 not null;

-- Reservation
alter table o_ac_reservation add userconfirmable number(1) default 1 not null;

-- Membership
create table o_bs_group_member_history (
  id number(20) GENERATED ALWAYS AS IDENTITY,
  creationdate date not null,
  g_role varchar(24) not null,
  g_status varchar(32) not null,
  g_note varchar(2000),
  g_admin_note varchar(2000),
  fk_transfer_origin_id number(20),
  fk_transfer_destination_id number(20),
  fk_creator_id number(20),
  fk_group_id number(20) not null,
  fk_identity_id number(20) not null,
  primary key (id)
);

alter table o_bs_group_member_history add constraint hist_transfer_origin_idx foreign key (fk_transfer_origin_id) references o_olatresource (resource_id);
create index idx_hist_transfer_origin_idx on o_bs_group_member_history (fk_transfer_origin_id);
alter table o_bs_group_member_history add constraint hist_transfer_dest_idx foreign key (fk_transfer_destination_id) references o_olatresource (resource_id);
create index idx_hist_transfer_dest_idx on o_bs_group_member_history (fk_transfer_destination_id);

alter table o_bs_group_member_history add constraint hist_creator_idx foreign key (fk_creator_id) references o_bs_identity (id);
create index idx_hist_creator_idx on o_bs_group_member_history (fk_creator_id);
alter table o_bs_group_member_history add constraint hist_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_hist_ident_idx on o_bs_group_member_history (fk_identity_id);

alter table o_bs_group_member_history add constraint history_group_idx foreign key (fk_group_id) references o_bs_group (id);
create index idx_history_group_idx on o_bs_group_member_history (fk_group_id);


-- Access control
create table o_ac_billing_address (
  id number(20) GENERATED ALWAYS AS IDENTITY,
  creationdate date not null,
  lastmodified date not null,
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
  a_enabled number default 1 not null,
  fk_organisation number(20),
  fk_identity number(20),
  primary key (id)
);
alter table o_ac_order add fk_billing_address number(20);

alter table o_ac_billing_address add constraint ac_billing_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_ac_billing_to_org_idx on o_ac_billing_address (fk_organisation);
alter table o_ac_billing_address add constraint ac_billing_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_ac_billing_to_ident_idx on o_ac_billing_address (fk_identity);

alter table o_ac_order add constraint ord_billing_idx foreign key (fk_billing_address) references o_ac_billing_address (id);
create index idx_ord_billing_idx on o_ac_order (fk_billing_address);
