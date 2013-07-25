-- onyx
alter table o_qtiresultset add column issuspended bool default false;
alter table o_qtiresultset add column fullyassessed bool default false;

alter table o_checklist alter column title drop not null;
alter table o_checkpoint alter column title drop not null;


-- question item
create table o_qp_pool (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_name varchar(255) not null,
   q_public boolean default false,
   fk_ownergroup int8,
   primary key (id)
);

create table o_qp_taxonomy_level (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_field varchar(255) not null,
   q_mat_path_ids varchar(1024),
   q_mat_path_names varchar(2048),
   fk_parent_field int8,
   primary key (id)
);

create table o_qp_item (
   id int8 not null,
   q_identifier varchar(36) not null,
   q_master_identifier varchar(36),
   q_title varchar(1024) not null,
   q_description varchar(2048),
   q_keywords varchar(1024),
   q_coverage varchar(1024),
   q_additional_informations varchar(256),
   q_language varchar(16),
   fk_edu_context bigint,
   q_educational_learningtime varchar(32),
   fk_type bigint,
   q_difficulty decimal(10,9),
   q_stdev_difficulty decimal(10,9),
   q_differentiation decimal(10,9),
   q_num_of_answers_alt int8 not null default 0,
   q_usage int8 not null default 0,
   q_assessment_type varchar(64),
   q_status varchar(32) not null,
   q_version varchar(50),
   fk_license int8,
   q_editor varchar(256),
   q_editor_version varchar(256),
   q_format varchar(32) not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_dir varchar(32),
   q_root_filename varchar(255),
   fk_taxonomy_level int8,
   fk_ownergroup int8 not null,
   primary key (id)
);

create table o_qp_pool_2_item (
   id int8 not null,
   creationdate timestamp not null,
   q_editable boolean default false,
   fk_pool_id int8 not null,
   fk_item_id int8 not null,
   primary key (id)
);

create table o_qp_share_item (
   id int8 not null,
   creationdate timestamp not null,
   q_editable boolean default false,
   fk_resource_id int8 not null,
   fk_item_id int8 not null,
   primary key (id)
);

create table o_qp_item_collection (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_name varchar(256),
   fk_owner_id int8 not null,
   primary key (id)
);

create table o_qp_collection_2_item (
   id int8 not null,
   creationdate timestamp not null,
   fk_collection_id int8 not null,
   fk_item_id int8 not null,
   primary key (id)
);

create table o_qp_edu_context (
   id int8 not null,
   creationdate timestamp not null,
   q_level varchar(256) not null,
   q_deletable boolean default false,
   primary key (id)
);

create table o_qp_item_type (
   id int8 not null,
   creationdate timestamp not null,
   q_type varchar(256) not null,
   q_deletable boolean default false,
   primary key (id)
);

create table o_qp_license (
   id int8 not null,
   creationdate timestamp not null,
   q_license varchar(256) not null,
   q_text varchar(2048),
   q_deletable boolean default false,
   primary key (id)
);


-- views with rating
create or replace view o_qp_item_v as (
   select
      item.id as item_id,
      item.q_identifier as item_identifier,
      item.q_master_identifier as item_master_identifier,
      item.q_title as item_title,
      item.q_language as item_language,
      item.q_keywords as item_keywords,
      item.q_coverage as item_coverage,
      item.q_additional_informations as item_additional_informations,
      taxlevel.q_field as item_taxonomy_level,
      educontext.q_level as item_edu_context,
      item.q_educational_learningtime as item_educational_learningtime,
      itemtype.q_type as item_type,
      item.q_difficulty as item_difficulty,
      item.q_stdev_difficulty as item_stdev_difficulty,
      item.q_differentiation as item_differentiation,
      item.q_num_of_answers_alt as item_num_of_answers_alt,
      item.q_usage as item_usage,
      item.q_version as item_version,
      item.q_status as item_status,
      item.q_format as item_format,
      item.creationdate as item_creationdate,
      item.lastmodified as item_lastmodified,
      ownership.identity_id as owner_id,
      mark.creator_id as mark_creator,
      (case when mark.creator_id is null then false else true end) as marked,
      (select avg(rating.rating) from o_userrating as rating
         where rating.resid=item.id and rating.resname='QuestionItem' and rating.ressubpath is null
      ) as item_rating
   from o_qp_item as item
   inner join o_bs_secgroup as ownergroup on (ownergroup.id = item.fk_ownergroup)
   left join o_bs_membership as ownership on (ownergroup.id = ownership.secgroup_id) 
   left join o_qp_taxonomy_level as taxlevel on (item.fk_taxonomy_level = taxlevel.id)
   left join o_qp_item_type as itemtype on (item.fk_type = itemtype.id)
   left join o_qp_edu_context as educontext on (item.fk_edu_context = educontext.id)
   left join o_mark as mark on (mark.resid = item.id and mark.resname = 'QuestionItem')
);

create or replace view o_qp_item_author_v as (
   select
      item.id as item_id,
      ownership.identity_id as item_author,
      item.q_identifier as item_identifier,
      item.q_master_identifier as item_master_identifier,
      item.q_title as item_title,
      item.q_language as item_language,
      item.q_keywords as item_keywords,
      item.q_coverage as item_coverage,
      item.q_additional_informations as item_additional_informations,
      taxlevel.q_field as item_taxonomy_level,
      educontext.q_level as item_edu_context,
      item.q_educational_learningtime as item_educational_learningtime,
      itemtype.q_type as item_type,
      item.q_difficulty as item_difficulty,
      item.q_stdev_difficulty as item_stdev_difficulty,
      item.q_differentiation as item_differentiation,
      item.q_num_of_answers_alt as item_num_of_answers_alt,
      item.q_usage as item_usage,
      item.q_version as item_version,
      item.q_status as item_status,
      item.q_format as item_format,
      item.creationdate as item_creationdate,
      item.lastmodified as item_lastmodified,
      mark.creator_id as mark_creator,
      (case when mark.creator_id is null then false else true end) as marked,
      (select avg(rating.rating) from o_userrating as rating
         where rating.resid=item.id and rating.resname='QuestionItem' and rating.ressubpath is null
      ) as item_rating
   from o_qp_item as item
   inner join o_bs_secgroup as ownergroup on (ownergroup.id = item.fk_ownergroup)
   inner join o_bs_membership as ownership on (ownergroup.id = ownership.secgroup_id) 
   left join o_mark as mark on (mark.resid = item.id and mark.resname = 'QuestionItem')
   left join o_qp_taxonomy_level as taxlevel on (item.fk_taxonomy_level = taxlevel.id)
   left join o_qp_item_type as itemtype on (item.fk_type = itemtype.id)
   left join o_qp_edu_context as educontext on (item.fk_edu_context = educontext.id)
);

create or replace view o_qp_item_pool_v as (
   select
      item.id as item_id,
      pool2item.q_editable as item_editable,
      pool2item.fk_pool_id as item_pool,
      item.q_identifier as item_identifier,
      item.q_master_identifier as item_master_identifier,
      item.q_title as item_title,
      item.q_language as item_language,
      item.q_keywords as item_keywords,
      item.q_coverage as item_coverage,
      item.q_additional_informations as item_additional_informations,
      taxlevel.q_field as item_taxonomy_level,
      educontext.q_level as item_edu_context,
      item.q_educational_learningtime as item_educational_learningtime,
      itemtype.q_type as item_type,
      item.q_difficulty as item_difficulty,
      item.q_stdev_difficulty as item_stdev_difficulty,
      item.q_differentiation as item_differentiation,
      item.q_num_of_answers_alt as item_num_of_answers_alt,
      item.q_usage as item_usage,
      item.q_version as item_version,
      item.q_status as item_status,
      item.q_format as item_format,
      item.creationdate as item_creationdate,
      item.lastmodified as item_lastmodified,
      mark.creator_id as mark_creator,
      (case when mark.creator_id is null then false else true end) as marked,
      (select avg(rating.rating) from o_userrating as rating
         where rating.resid=item.id and rating.resname='QuestionItem' and rating.ressubpath is null
      ) as item_rating
   from o_qp_item as item
   inner join o_qp_pool_2_item as pool2item on (pool2item.fk_item_id = item.id)
   left join o_mark as mark on (mark.resid = item.id and mark.resname = 'QuestionItem')
   left join o_qp_taxonomy_level as taxlevel on (item.fk_taxonomy_level = taxlevel.id)
   left join o_qp_item_type as itemtype on (item.fk_type = itemtype.id)
   left join o_qp_edu_context as educontext on (item.fk_edu_context = educontext.id)
);

create or replace view o_qp_pool_2_item_short_v as (
   select
      pool2item.id as item_to_pool_id,
      pool2item.creationdate as item_to_pool_creationdate,
      item.id as item_id,
      pool2item.q_editable as item_editable,
      pool2item.fk_pool_id as item_pool,
      pool.q_name as item_pool_name
   from o_qp_item as item
   inner join o_qp_pool_2_item as pool2item on (pool2item.fk_item_id = item.id)
   inner join o_qp_pool as pool on (pool2item.fk_pool_id = pool.id)
);

create or replace view o_qp_item_shared_v as (
   select
      item.id as item_id,
      shareditem.q_editable as item_editable,
      shareditem.fk_resource_id as item_resource_id,
      item.q_identifier as item_identifier,
      item.q_master_identifier as item_master_identifier,
      item.q_title as item_title,
      item.q_language as item_language,
      item.q_keywords as item_keywords,
      item.q_coverage as item_coverage,
      item.q_additional_informations as item_additional_informations,
      taxlevel.q_field as item_taxonomy_level,
      educontext.q_level as item_edu_context,
      item.q_educational_learningtime as item_educational_learningtime,
      itemtype.q_type as item_type,
      item.q_difficulty as item_difficulty,
      item.q_stdev_difficulty as item_stdev_difficulty,
      item.q_differentiation as item_differentiation,
      item.q_num_of_answers_alt as item_num_of_answers_alt,
      item.q_usage as item_usage,
      item.q_version as item_version,
      item.q_status as item_status,
      item.q_format as item_format,
      item.creationdate as item_creationdate,
      item.lastmodified as item_lastmodified,
      mark.creator_id as mark_creator,
      (case when mark.creator_id is null then false else true end) as marked,
      (select avg(rating.rating) from o_userrating as rating
         where rating.resid=item.id and rating.resname='QuestionItem' and rating.ressubpath is null
      ) as item_rating
   from o_qp_item as item
   inner join o_qp_share_item as shareditem on (shareditem.fk_item_id = item.id)
   left join o_mark as mark on (mark.resid = item.id and mark.resname = 'QuestionItem')
   left join o_qp_taxonomy_level as taxlevel on (item.fk_taxonomy_level = taxlevel.id)
   left join o_qp_item_type as itemtype on (item.fk_type = itemtype.id)
   left join o_qp_edu_context as educontext on (item.fk_edu_context = educontext.id)
);

create or replace view o_qp_share_2_item_short_v as (
   select
      shareditem.id as item_to_share_id,
      shareditem.creationdate as item_to_share_creationdate,
      item.id as item_id,
      shareditem.q_editable as item_editable,
      shareditem.fk_resource_id as resource_id,
      bgroup.groupname as resource_name
   from o_qp_item as item
   inner join o_qp_share_item as shareditem on (shareditem.fk_item_id = item.id)
   inner join o_gp_business as bgroup on (shareditem.fk_resource_id = bgroup.fk_resource)
);

alter table o_qp_pool add constraint idx_qp_pool_owner_grp_id foreign key (fk_ownergroup) references o_bs_secgroup(id);
create index idx_pool_ownergrp_idx on o_qp_pool (fk_ownergroup);

alter table o_qp_pool_2_item add constraint idx_qp_pool_2_item_pool_id foreign key (fk_pool_id) references o_qp_pool(id);
alter table o_qp_pool_2_item add constraint idx_qp_pool_2_item_item_id foreign key (fk_item_id) references o_qp_item(id);
alter table o_qp_pool_2_item add unique (fk_pool_id, fk_item_id);
create index idx_poolitem_pool_idx on o_qp_pool_2_item (fk_pool_id);
create index idx_poolitem_item_idx on o_qp_pool_2_item (fk_item_id);

alter table o_qp_share_item add constraint idx_qp_share_rsrc_id foreign key (fk_resource_id) references o_olatresource(resource_id);
alter table o_qp_share_item add constraint idx_qp_share_item_id foreign key (fk_item_id) references o_qp_item(id);
alter table o_qp_share_item add unique (fk_resource_id, fk_item_id);
create index idx_shareitem_pool_idx on o_qp_share_item (fk_resource_id);
create index idx_shareitem_item_idx on o_qp_share_item (fk_item_id);

alter table o_qp_item_collection add constraint idx_qp_coll_owner_id foreign key (fk_owner_id) references o_bs_identity(id);
create index idx_itemcoll_owner_idx on o_qp_item_collection (fk_owner_id);

alter table o_qp_collection_2_item add constraint idx_qp_coll_coll_id foreign key (fk_collection_id) references o_qp_item_collection(id);
alter table o_qp_collection_2_item add constraint idx_qp_coll_item_id foreign key (fk_item_id) references o_qp_item(id);
alter table o_qp_collection_2_item add unique (fk_collection_id, fk_item_id);
create index idx_coll2item_coll_idx on o_qp_collection_2_item (fk_collection_id);
create index idx_coll2item_item_idx on o_qp_collection_2_item (fk_item_id);

alter table o_qp_item add constraint idx_qp_pool_2_field_id foreign key (fk_taxonomy_level) references o_qp_taxonomy_level(id);
alter table o_qp_item add constraint idx_qp_item_owner_id foreign key (fk_ownergroup) references o_bs_secgroup(id);
alter table o_qp_item add constraint idx_qp_item_edu_ctxt_id foreign key (fk_edu_context) references o_qp_edu_context(id);
alter table o_qp_item add constraint idx_qp_item_type_id foreign key (fk_type) references o_qp_item_type(id);
alter table o_qp_item add constraint idx_qp_item_license_id foreign key (fk_license) references o_qp_license(id);
create index idx_item_taxon_idx on o_qp_item (fk_taxonomy_level);
create index idx_item_ownergrp_idx on o_qp_item (fk_ownergroup);
create index idx_item_eductxt_idx on o_qp_item (fk_edu_context);
create index idx_item_type_idx on o_qp_item (fk_type);
create index idx_item_license_idx on o_qp_item (fk_license);

alter table o_qp_taxonomy_level add constraint idx_qp_field_2_parent_id foreign key (fk_parent_field) references o_qp_taxonomy_level(id);
create index idx_taxon_parent_idx on o_qp_taxonomy_level (fk_parent_field);
create index idx_taxon_mat_path  on o_qp_taxonomy_level (q_mat_path_ids);

alter table o_qp_item_type add constraint cst_unique_item_type unique (q_type);

-- lti
create table o_lti_outcome (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   r_ressubpath varchar(2048),
   r_action varchar(255) not null,
   r_outcome_key varchar(255) not null,
   r_outcome_value varchar(2048),
   fk_resource_id int8 not null,
   fk_identity_id int8 not null,
   primary key (id)
);

alter table o_lti_outcome add constraint idx_lti_outcome_ident_id foreign key (fk_identity_id) references o_bs_identity(id);
alter table o_lti_outcome add constraint idx_lti_outcome_rsrc_id foreign key (fk_resource_id) references o_olatresource(resource_id);
create index idx_lti_outcome_ident_id_idx on o_lti_outcome (fk_identity_id);
create index idx_lti_outcome_rsrc_id_idx on o_lti_outcome (fk_resource_id);

-- mapper
alter table o_mapper add column expirationdate timestamp;

-- mail
alter table o_mail_attachment add column datas_checksum int8;
alter table o_mail_attachment add column datas_path varchar(1024);
alter table o_mail_attachment add column datas_lastmodified timestamp;
create index idx_mail_att_checksum_idx on o_mail_attachment (datas_checksum);
create index idx_mail_path_idx on o_mail_attachment (datas_path);
create index idx_mail_att_siblings_idx on o_mail_attachment (datas_checksum, mimetype, datas_size, datas_name);

-- managed groups and repository entries
create table o_repositoryentry_cycle (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   r_softkey varchar(64),
   r_label varchar(255),
   r_privatecycle bool default false,
   r_validfrom timestamp,
   r_validto timestamp,
   primary key (id)
);
create index idx_re_lifecycle_soft_idx on o_repositoryentry_cycle (r_softkey);

alter table o_repositoryentry add column external_id varchar(64);
alter table o_repositoryentry add column external_ref varchar(64);
alter table o_repositoryentry add column managed_flags varchar(255);
create index idx_re_lifecycle_extid_idx on o_repositoryentry (external_id);
create index idx_re_lifecycle_extref_idx on o_repositoryentry (external_ref);

alter table o_repositoryentry add column fk_lifecycle int8;
alter table o_repositoryentry add constraint idx_re_lifecycle_fk foreign key (fk_lifecycle) references o_repositoryentry_cycle(id);
create index idx_re_lifecycle_idx on o_repositoryentry (fk_lifecycle);

alter table o_gp_business add column external_id varchar(64);
alter table o_gp_business add column managed_flags varchar(255);
create index idx_grp_lifecycle_soft_idx on o_gp_business (external_id);

-- complet missing index

-- checkpoint
create index idx_chpt_checklist_fk on o_checkpoint (checklist_fk);
create index idx_chres_check_idx on o_checkpoint_results (checkpoint_fk);
create index idx_chres_ident_idx on o_checkpoint_results (identity_fk);

-- property
create index idx_prop_ident_idx on o_property (identity);
create index idx_prop_grp_idx on o_property (grp);

-- business group
create index idx_grp_to_ctxt_idx on o_gp_business (groupcontext_fk);
create index idx_grp_to_sec_grps_idx on o_gp_business (fk_ownergroup, fk_partipiciantgroup, fk_waitinggroup);

create index idx_grrsrc_to_rsrc_idx on o_gp_business_to_resource (fk_resource);
create index idx_grrsrc_to_grp_idx  on o_gp_business_to_resource (fk_group);
create index idx_grrsrc_to_rsrc_grp_idx  on o_gp_business_to_resource (fk_resource, fk_group);

-- area
create index idx_bgtoarea_grp_idx on o_gp_bgtoarea_rel (group_fk);
create index idx_bgtoarea_area_idx on o_gp_bgtoarea_rel (area_fk);

-- bs
create index idx_auth_ident_idx on o_bs_authentication (identity_fk);

create index idx_ident_creationdate_idx on o_bs_identity (creationdate);
create index idx_id_lastlogin_idx on o_bs_identity (lastlogin);

create index idx_policy_rsrc_idx on o_bs_policy (oresource_id);
create index idx_policy_grp_idx on o_bs_policy (group_id);
create index idx_policy_grp_rsrc_idx on o_bs_policy (oresource_id, group_id);

create index idx_membership_sec_idx on o_bs_membership (secgroup_id);
create index idx_membership_ident_idx on o_bs_membership (identity_id);
create index idx_membership_sec_ident_idx on o_bs_membership (identity_id, secgroup_id);

create index idx_secgroup_creationdate_idx on o_bs_secgroup (creationdate);

create index idx_invitation_grp_idx on o_bs_invitation (fk_secgroup);

create index FKBAFCBBC4B85B522C on o_bs_namedgroup (secgroup_id);

-- user
create index idx_user_creationdate_idx on o_user (creationdate);

-- pub sub
create index idx_sub_to_pub_idx on o_noti_sub (fk_publisher);
create index idx_sub_to_ident_idx on o_noti_sub (fk_identity);
create index idx_sub_to_id_pub_idx on o_noti_sub (publisher_id, fk_publisher);
create index idx_sub_to_id_ident_idx on o_noti_sub (publisher_id, fk_identity);
-- index created idx_sub_to_pub_ident_idx on unique constraint
create index idx_sub_to_id_pub_ident_idx on o_noti_sub (publisher_id, fk_publisher, fk_identity);

-- qti
create index FK3563E67340EF401F on o_qtiresult (resultset_fk);

-- references
create index idx_ref_source_idx on o_references (source_id);
create index idx_ref_target_idx on o_references (target_id);

-- catalog
create index idx_catentry_parent_idx on o_catentry (parent_id);
-- index created idx_catentry_ownergrp_idx on unique constraint
create index idx_catentry_re_idx on o_catentry (fk_repoentry);

-- resource
create index name_idx4 on o_olatresource (resname);

-- access control
create index idx_offeracc_method_idx on o_ac_offer_access (fk_method_id);
create index idx_offeracc_offer_idx on o_ac_offer_access (fk_offer_id);
create index idx_orderpart_order_idx on o_ac_order_part (fk_order_id);
create index idx_orderline_orderpart_idx on o_ac_order_line (fk_order_part_id);
create index idx_orderline_offer_idx on o_ac_order_line (fk_offer_id);

create index idx_transact_order_idx on o_ac_transaction (fk_order_id);
create index idx_transact_orderpart_idx on o_ac_transaction (fk_order_part_id);
create index idx_transact_method_idx on o_ac_transaction (fk_method_id);

-- reservations
create index idx_rsrv_to_rsrc_idx on o_ac_reservation(fk_resource);
create index idx_rsrv_to_rsrc_id_idx on o_ac_reservation(fk_identity);

-- forum
create index idx_message_creator_idx on o_message (creator_id);
create index idx_message_modifier_idx on o_message (modifier_id);
create index idx_message_parent_idx on o_message (parent_id);
create index idx_message_top_idx on o_message (topthread_id);
create index idx_message_forum_idx on o_message (forum_fk);

-- course db
create index o_co_db_course_ident_idx on o_co_db_entry (identity);

-- openmeeting
create index idx_omroom_group_idx on o_om_room_reference (businessgroup);

-- eportfolio
create index idx_artfeact_to_auth_idx on o_ep_artefact (fk_artefact_auth_id);
create index idx_artfeact_to_struct_idx on o_ep_artefact (fk_struct_el_id);
create index idx_structel_to_rsrc_idx on o_ep_struct_el (fk_olatresource);
create index idx_structel_to_ownegrp_idx on o_ep_struct_el (fk_ownergroup);
create index idx_structel_to_map_idx on o_ep_struct_el (fk_map_source_id);
create index idx_structel_to_root_idx on o_ep_struct_el (fk_struct_root_id);
create index idx_structel_to_rootmap_idx on o_ep_struct_el (fk_struct_root_map_id);
create index idx_collectrest_to_structel_idx on o_ep_collect_restriction (fk_struct_el_id);
create index idx_structlink_to_parent_idx on o_ep_struct_struct_link (fk_struct_parent_id);
create index idx_structlink_to_child_idx on o_ep_struct_struct_link (fk_struct_child_id);
create index idx_structart_to_struct_idx on o_ep_struct_artefact_link (fk_struct_id);
create index idx_structart_to_art_idx on o_ep_struct_artefact_link (fk_artefact_id);
create index idx_structart_to_auth_idx on o_ep_struct_artefact_link (fk_auth_id);

-- tag
create index idx_tag_to_auth_idx on o_tag (fk_author_id);

-- mail
create index idx_mail_from_idx on o_mail (fk_from_id);
create index idx_mailtorec_mail_idx on o_mail_to_recipient (fk_mail_id);
create index idx_mailrec_rcp_idx on o_mail_recipient (fk_recipient_id);
create index idx_mailtorec_rcp_idx on o_mail_to_recipient (fk_recipient_id);
create index idx_mail_att_mail_idx on o_mail_attachment (fk_att_mail_id);


-- efficiency statement
create index idx_eff_statement_ident_idx on o_as_eff_statement (fk_identity);

create index idx_ucourseinfos_ident_idx on o_as_user_course_infos (fk_identity);
create index idx_ucourseinfos_rsrc_idx on o_as_user_course_infos (fk_resource_id);

-- course infos
alter table o_as_user_course_infos add unique (fk_identity, fk_resource_id);

-- task executor
create table o_ex_task (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   e_name varchar(255) not null,
   e_status varchar(16) not null,
   e_executor_node varchar(16),
   e_executor_boot_id varchar(64),
   e_task text not null,
   primary key (id)
);

drop view o_re_strict_participant_v;
drop view o_re_strict_tutor_v;
drop view o_re_strict_member_v;

create or replace view o_re_participant_v as
select
   re.repositoryentry_id as re_id,
   re_member.identity_id as member_id
   from o_repositoryentry re
   inner join o_bs_membership re_member on (re_member.secgroup_id = re.fk_participantgroup)
union select
   re.repositoryentry_id as re_id,
   bg_member.identity_id as member_id
   from o_repositoryentry re
   inner join o_gp_business_to_resource bgroup_rel on (bgroup_rel.fk_resource = re.fk_olatresource)
   inner join o_gp_business bgroup on (bgroup.group_id = bgroup_rel.fk_group)
   inner join o_bs_membership bg_member on (bg_member.secgroup_id = bgroup.fk_partipiciantgroup);

create or replace view o_re_tutor_v as
select
   re.repositoryentry_id as re_id,
   re_member.identity_id as member_id
   from o_repositoryentry re
   inner join o_bs_membership re_member on (re_member.secgroup_id = re.fk_tutorgroup)
union select
   re.repositoryentry_id as re_id,
   bg_member.identity_id as member_id
   from o_repositoryentry re
   inner join o_gp_business_to_resource bgroup_rel on (bgroup_rel.fk_resource = re.fk_olatresource)
   inner join o_gp_business bgroup on (bgroup.group_id = bgroup_rel.fk_group)
   inner join o_bs_membership bg_member on (bg_member.secgroup_id = bgroup.fk_ownergroup);

create or replace view o_gp_member_v as (
   select
      gp.group_id as bg_id,
      gp.groupname as bg_name,
      gp.creationdate as bg_creationdate,
      gp.managed_flags as bg_managed_flags,
      gp.descr as bg_desc,
      membership.identity_id as member_id
   from o_bs_membership as membership
   inner join o_gp_business as gp on (membership.secgroup_id = gp.fk_ownergroup or membership.secgroup_id = gp.fk_partipiciantgroup));

-- managed groups
drop view o_gp_business_v;

create or replace view o_gp_business_v  as (
   select
      gp.group_id as group_id,
      gp.groupname as groupname,
      gp.lastmodified as lastmodified,
      gp.creationdate as creationdate,
      gp.lastusage as lastusage,
      gp.descr as descr,
      gp.minparticipants as minparticipants,
      gp.maxparticipants as maxparticipants,
      gp.waitinglist_enabled as waitinglist_enabled,
      gp.autocloseranks_enabled as autocloseranks_enabled,
      gp.external_id as external_id,
      gp.managed_flags as managed_flags,
      (select count(part.id) from o_bs_membership as part where part.secgroup_id = gp.fk_partipiciantgroup) as num_of_participants,
      (select count(pending.reservation_id) from o_ac_reservation as pending where pending.fk_resource = gp.fk_resource) as num_of_pendings,
      (select count(own.id) from o_bs_membership as own where own.secgroup_id = gp.fk_ownergroup) as num_of_owners,
      (case when gp.waitinglist_enabled = true
         then 
           (select count(waiting.id) from o_bs_membership as waiting where waiting.secgroup_id = gp.fk_partipiciantgroup)
         else
           0
      end) as num_waiting,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=true
         and (offer.validfrom is null or offer.validfrom <= current_timestamp)
         and (offer.validto is null or offer.validto >= current_timestamp)
      ) as num_of_valid_offers,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=true
      ) as num_of_offers,
      (select count(relation.fk_resource) from o_gp_business_to_resource as relation 
         where relation.fk_group = gp.group_id
      ) as num_of_relations,
      gp.fk_resource as fk_resource,
      gp.fk_ownergroup as fk_ownergroup,
      gp.fk_partipiciantgroup as fk_partipiciantgroup,
      gp.fk_waitinggroup as fk_waitinggroup
   from o_gp_business as gp
);


