

-- Access
create table o_ac_offer_to_organisation (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  fk_offer number(20) not null,
  fk_organisation number(20) not null,
  primary key (id)
);

alter table o_ac_offer_to_organisation add constraint rel_oto_offer_idx foreign key (fk_offer) references o_ac_offer(offer_id);
create index idx_rel_oto_offer_idx on o_ac_offer_to_organisation (fk_offer);
alter table o_ac_offer_to_organisation add constraint rel_oto_org_idx foreign key (fk_organisation) references o_org_organisation(id);
create index idx_rel_oto_org_idx on o_ac_offer_to_organisation (fk_organisation);

alter table o_ac_offer add open_access number default 0 not null;
alter table o_ac_offer add guest_access number default 0 not null;
alter table o_ac_offer add catalog_publish number default 0 not null;
alter table o_ac_offer add catalog_web_publish number default 0 not null;

create index idx_offer_guest_idx on o_ac_offer (guest_access);
create index idx_offer_open_idx on o_ac_offer (open_access);

alter table o_repositoryentry add publicvisible number default 0 not null;


-- Catalog V2
create table o_ca_launcher (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   c_type varchar2(50),
   c_identifier varchar2(32),
   c_sort_order number(20),
   c_enabled number default 1 not null,
   c_config varchar2(1024),
   primary key (id)
);
create table o_ca_filter (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   c_type varchar2(50),
   c_sort_order number(20),
   c_enabled number default 1 not null,
   c_default_visible number default 1 not null,
   c_config varchar2(1024),
   primary key (id)
);

alter table o_repositoryentry add status_published_date date;

-- Practice
alter table o_qti_assessmentitem_session add q_attempts number(20) default null;
alter table o_qti_assessmentitem_session add q_externalrefidentifier varchar2(64) default null;
create index idx_item_ext_ref_idx on o_qti_assessmentitem_session (q_externalrefidentifier);

alter table o_as_entry add a_share number default null;

create table o_practice_resource (
  id number(20) generated always as identity,
   lastmodified date not null,
   creationdate date not null,
   fk_entry number(20) not null,
   p_subident varchar2(64) not null,
   fk_test_entry number(20),
   fk_item_collection number(20),
   fk_pool number(20),
   fk_resource_share number(20),
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
  id number(20) generated always as identity,
   lastmodified date not null,
   creationdate date not null,
   p_identifier varchar(64) not null,
   p_level number(20) default 0,
   p_attempts number(20) default 0,
   p_correct_answers number(20) default 0,
   p_incorrect_answers number(20) default 0,
   p_last_attempt_date date,
   p_last_attempt_passed number default null,
   fk_identity number(20) not null,
   primary key (id)
);

alter table o_practice_global_item_ref add constraint pract_global_ident_idx foreign key (fk_identity) references o_bs_identity(id);
create index idx_pract_global_ident_idx on o_practice_global_item_ref (fk_identity);

create index idx_pract_global_id_uu_idx on o_practice_global_item_ref (fk_identity,p_identifier);


