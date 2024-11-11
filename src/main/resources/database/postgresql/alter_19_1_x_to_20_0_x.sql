-- Lectures
alter table o_lecture_block add column l_external_ref varchar(128);

alter table o_lecture_block alter column fk_entry drop not null;
alter table o_lecture_block add column fk_curriculum_element int8;

alter table o_lecture_block add constraint lec_block_curelem_idx foreign key (fk_curriculum_element) references o_cur_curriculum_element(id);
create index idx_lec_block_curelem_idx on o_lecture_block(fk_curriculum_element);

alter table o_lecture_block_audit_log add column fk_curriculum_element int8;

-- Curriculum
alter table o_cur_element_type add column c_single_element bool default false not null;
alter table o_cur_element_type add column c_max_repo_entries int8 default -1 not null;
alter table o_cur_element_type add column c_allow_as_root bool default true not null;

alter table o_cur_curriculum_element add column fk_resource int8;

alter table o_cur_curriculum_element add constraint cur_el_resource_idx foreign key (fk_resource) references o_olatresource (resource_id);
create index idx_cur_el_resource_idx on o_cur_curriculum_element (fk_resource);

-- Organisations
alter table o_org_organisation add column o_location varchar(255);
create table o_org_email_domain (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  o_domain varchar(255) not null,
  o_enabled bool default true not null,
  o_subdomains_allowed bool default false not null,
  fk_organisation int8 not null,
  primary key (id)
);

alter table o_org_email_domain add constraint org_email_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_org_email_to_org_idx on o_org_email_domain (fk_organisation);

-- Catalog
alter table o_ca_launcher add column c_web_enabled bool default true not null;

-- Reservation
alter table o_ac_reservation add column userconfirmable bool not null default true;

-- Membership
create table o_bs_group_member_history (
  id bigserial,
  creationdate timestamp not null,
  g_role varchar(24) not null,
  g_status varchar(32) not null,
  g_note varchar(2000),
  g_admin_note varchar(2000),
  fk_transfer_origin_id int8,
  fk_transfer_destination_id int8,
  fk_creator_id int8,
  fk_group_id int8 not null,
  fk_identity_id int8 not null,
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
create table o_ac_cost_center (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  a_name varchar(255),
  a_account varchar(255),
  a_enabled bool default true not null,
  primary key (id)
);
create table o_ac_billing_address (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
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
  a_enabled bool default true not null,
  fk_organisation int8,
  fk_identity int8,
  primary key (id)
);
alter table o_ac_offer add column confirm_by_manager_required bool default false not null;
alter table o_ac_offer add column cancelling_fee_amount decimal;
alter table o_ac_offer add column cancelling_fee_currency_code varchar(3);
alter table o_ac_offer add column cancelling_fee_deadline_days int8;
alter table o_ac_offer add column fk_cost_center int8;
alter table o_ac_order add column purchase_order_number varchar(100);
alter table o_ac_order add column order_comment text;
alter table o_ac_order add column fk_billing_address int8;

alter table o_ac_offer add constraint ac_offer_to_cc_idx foreign key (fk_cost_center) references o_ac_cost_center (id);
create index idx_ac_offer_to_cc_idx on o_ac_offer (fk_cost_center);

alter table o_ac_billing_address add constraint ac_billing_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_ac_billing_to_org_idx on o_ac_billing_address (fk_organisation);
alter table o_ac_billing_address add constraint ac_billing_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_ac_billing_to_ident_idx on o_ac_billing_address (fk_identity);

alter table o_ac_order add constraint ord_billing_idx foreign key (fk_billing_address) references o_ac_billing_address (id);
create index idx_ord_billing_idx on o_ac_order (fk_billing_address);

