

-- Access
create table o_ac_offer_to_organisation (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  fk_offer int8 not null,
  fk_organisation int8 not null,
  primary key (id)
);

alter table o_ac_offer_to_organisation add constraint rel_oto_offer_idx foreign key (fk_offer) references o_ac_offer(offer_id);
create index idx_rel_oto_offer_idx on o_ac_offer_to_organisation (fk_offer);
alter table o_ac_offer_to_organisation add constraint rel_oto_org_idx foreign key (fk_organisation) references o_org_organisation(id);
create index idx_rel_oto_org_idx on o_ac_offer_to_organisation (fk_organisation);

alter table o_ac_offer add open_access bool default false not null;
alter table o_ac_offer add guest_access bool default false not null;
alter table o_ac_offer add catalog_publish bool default false not null;
alter table o_ac_offer add catalog_web_publish bool default false not null;

create index idx_offer_guest_idx on o_ac_offer (guest_access);
create index idx_offer_open_idx on o_ac_offer (open_access);

alter table o_repositoryentry add publicvisible bool default false not null;
alter table o_repositoryentry add status_published_date timestamp;


-- Taxonomy
alter table o_tax_taxonomy_level add t_i18n_suffix varchar(64);
alter table o_tax_taxonomy_level add t_media_path varchar(255);
alter table o_tax_taxonomy_level alter column t_displayname drop not null;


-- Catalog V2
create table o_ca_launcher (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_type varchar(50),
   c_identifier varchar(32),
   c_sort_order int8,
   c_enabled bool not null default true,
   c_config varchar(1024),
   primary key (id)
);
create table o_ca_filter (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_type varchar(50),
   c_sort_order int8,
   c_enabled bool not null default true,
   c_default_visible bool not null default true,
   c_config varchar(1024),
   primary key (id)
);


-- Practice
alter table o_qti_assessmentitem_session add column q_attempts int8 default null;
alter table o_qti_assessmentitem_session add column q_externalrefidentifier varchar(64) default null;
create index idx_item_ext_ref_idx on o_qti_assessmentitem_session (q_externalrefidentifier);

alter table o_as_entry add column a_share bool default null;

create table o_practice_resource (
   id bigserial,
   lastmodified timestamp not null,
   creationdate timestamp not null,
   fk_entry int8 not null,
   p_subident varchar(64) not null,
   fk_test_entry int8,
   fk_item_collection int8,
   fk_pool int8,
   fk_resource_share int8,
   primary key (id)
);

alter table o_practice_resource add constraint pract_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_pract_entry_idx on o_practice_resource (fk_entry);

alter table o_practice_resource add constraint pract_test_entry_idx foreign key (fk_test_entry) references o_repositoryentry (repositoryentry_id);
create index idx_pract_test_entry_idx on o_practice_resource (fk_test_entry);
alter table o_practice_resource add constraint pract_item_coll_idx foreign key (fk_item_collection) references o_qp_item_collection (id);
create index idx_pract_item_coll_idx on o_practice_resource (fk_item_collection);
alter table o_practice_resource add constraint pract_poll_idx foreign key (fk_pool) references o_qp_pool (id);
create index idx_poll_idx on o_practice_resource (fk_pool);
alter table o_practice_resource add constraint pract_rsrc_share_idx foreign key (fk_resource_share) references o_olatresource(resource_id);
create index idx_rsrc_share_idx on o_practice_resource (fk_resource_share);


create table o_practice_global_item_ref (
   id bigserial,
   lastmodified timestamp not null,
   creationdate timestamp not null,
   p_identifier varchar(64) not null,
   p_level int8 default 0,
   p_attempts int8 default 0,
   p_correct_answers int8 default 0,
   p_incorrect_answers int8 default 0,
   p_last_attempt_date timestamp,
   p_last_attempt_passed bool default null,
   fk_identity int8 not null,
   primary key (id)
);

alter table o_practice_global_item_ref add constraint pract_global_ident_idx foreign key (fk_identity) references o_bs_identity(id);
create index idx_pract_global_ident_idx on o_practice_global_item_ref (fk_identity);

create index idx_pract_global_id_uu_idx on o_practice_global_item_ref (fk_identity,p_identifier);


-- LTI 1.3
alter table o_lti_tool_deployment add column l_nrps bool default true;
alter table o_lti_tool_deployment add column l_context_id varchar(255);

alter table o_lti_tool_deployment add column fk_group_id int8;

alter table o_lti_tool_deployment add constraint dep_to_group_idx foreign key (fk_group_id) references o_gp_business(group_id);
create index idx_dep_to_group_idx on o_lti_tool_deployment (fk_group_id);



