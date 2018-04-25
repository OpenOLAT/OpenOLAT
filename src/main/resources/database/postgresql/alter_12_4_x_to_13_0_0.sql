-- organisation
create table o_org_organisation_type (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  o_identifier varchar(64),
  o_displayname varchar(255) not null,
  o_description text,
  o_external_id varchar(64),
  o_managed_flags varchar(255),
  o_css_class varchar(64),
  primary key (id)
);

create table o_org_organisation (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  o_identifier varchar(64),
  o_displayname varchar(255) not null,
  o_description text,
  o_m_path_keys varchar(255),
  o_external_id varchar(64),
  o_managed_flags varchar(255),
  o_status varchar(32),
  o_css_class varchar(64),
  fk_group int8 not null,
  fk_root int8,
  fk_parent int8,
  fk_type int8,
  primary key (id)
);

alter table o_org_organisation add constraint org_to_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_org_to_group_idx on o_org_organisation (fk_group);
alter table o_org_organisation add constraint org_to_root_org_idx foreign key (fk_root) references o_org_organisation (id);
create index idx_org_to_root_org_idx on o_org_organisation (fk_root);
alter table o_org_organisation add constraint org_to_parent_org_idx foreign key (fk_parent) references o_org_organisation (id);
create index idx_org_to_parent_org_idx on o_org_organisation (fk_parent);
alter table o_org_organisation add constraint org_to_org_type_idx foreign key (fk_type) references o_org_organisation_type (id);
create index idx_org_to_org_type_idx on o_org_organisation (fk_type);

create table o_org_type_to_type (
  id bigserial,
  fk_type int8 not null,
  fk_allowed_sub_type int8 not null,
  primary key (id)
);

alter table o_org_type_to_type add constraint org_type_to_type_idx foreign key (fk_type) references o_org_organisation_type (id);
create index idx_org_type_to_type_idx on o_org_type_to_type (fk_type);
alter table o_org_type_to_type add constraint org_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_org_organisation_type (id);
create index idx_org_type_to_sub_type_idx on o_org_type_to_type (fk_allowed_sub_type);


create table o_re_to_organisation (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  r_master bool default false,
  fk_entry int8 not null,
  fk_organisation int8 not null,
  primary key (id)
);

alter table o_re_to_organisation add constraint rel_org_to_re_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_rel_org_to_re_idx on o_re_to_organisation (fk_entry);
alter table o_re_to_organisation add constraint rel_org_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_rel_org_to_org_idx on o_re_to_organisation (fk_organisation);


-- curriculum
create table o_cur_element_type (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description text,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  c_css_class varchar(64),
  primary key (id)
);

create table o_cur_curriculum (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description text,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  c_status varchar(32),
  c_degree varchar(255),
  fk_group int8 not null,
  fk_organisation int8,
  primary key (id)
);

alter table o_cur_curriculum add constraint cur_to_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_cur_to_group_idx on o_cur_curriculum (fk_group);
alter table o_cur_curriculum add constraint cur_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_cur_to_org_idx on o_cur_curriculum (fk_organisation);

create table o_cur_curriculum_element (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  pos int8,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description text,
  c_status varchar(32),
  c_begin timestamp,
  c_end timestamp ,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  fk_group int8 not null,
  fk_parent int8,
  fk_curriculum int8 not null,
  primary key (id)
);

alter table o_cur_curriculum_element add constraint cur_el_to_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_cur_el_to_group_idx on o_cur_curriculum_element (fk_group);
alter table o_cur_curriculum_element add constraint cur_el_to_cur_el_idx foreign key (fk_parent) references o_cur_curriculum_element (id);
create index idx_cur_el_to_cur_el_idx on o_cur_curriculum_element (fk_parent);
alter table o_cur_curriculum_element add constraint cur_el_to_cur_idx foreign key (fk_curriculum) references o_cur_curriculum (id);
create index idx_cur_el_to_cur_idx on o_cur_curriculum_element (fk_curriculum);

create table o_cur_element_type_to_type (
  id bigserial,
  fk_type int8 not null,
  fk_allowed_sub_type int8 not null,
  primary key (id)
);

alter table o_cur_element_type_to_type add constraint cur_type_to_type_idx foreign key (fk_type) references o_cur_element_type (id);
create index idx_cur_type_to_type_idx on o_cur_element_type_to_type (fk_type);
alter table o_cur_element_type_to_type add constraint cur_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_cur_element_type (id);
create index idx_cur_type_to_sub_type_idx on o_cur_element_type_to_type (fk_allowed_sub_type);

-- drop policy
alter table o_bs_policy drop constraint FK9A1C5101E2E76DB;


-- evaluation forms
alter table o_eva_form_response add column e_no_response bool default false;

alter table o_eva_form_session add column e_resname varchar(50);
alter table o_eva_form_session add column e_resid int8;
alter table o_eva_form_session add column e_sub_ident varchar(2048);

create index idx_eva_sess_ores_idx on o_eva_form_session (e_resid, e_resname, e_sub_ident);

