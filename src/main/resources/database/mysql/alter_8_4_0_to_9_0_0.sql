-- onyx
alter table o_qtiresultset add column issuspended bit default 0;
alter table o_qtiresultset add column fullyassessed bit default 0;

alter table o_checklist modify column title varchar(255);
alter table o_checkpoint modify column title varchar(255);

-- question item
create table if not exists o_qp_pool (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_name varchar(255) not null,
   q_public bit default 0,
   fk_ownergroup bigint,
   primary key (id)
);

create table if not exists o_qp_taxonomy_level (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_field varchar(255) not null,
   q_mat_path_ids varchar(1024),
   q_mat_path_names varchar(2048),
   fk_parent_field bigint,
   primary key (id)
);

create table if not exists o_qp_item (
   id bigint not null,
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
   q_num_of_answers_alt bigint not null default 0,
   q_usage bigint not null default 0,
   q_assessment_type varchar(64),
   q_status varchar(32) not null,
   q_version varchar(50),
   fk_license bigint,
   q_editor varchar(256),
   q_editor_version varchar(256),
   q_format varchar(32) not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_dir varchar(32),
   q_root_filename varchar(255),
   fk_taxonomy_level bigint,
   fk_ownergroup bigint not null,
   primary key (id)
);

create table if not exists o_qp_pool_2_item (
   id bigint not null,
   creationdate datetime not null,
   q_editable bit default 0,
   fk_pool_id bigint not null,
   fk_item_id bigint not null,
   primary key (id)
);

create table if not exists o_qp_share_item (
   id bigint not null,
   creationdate datetime not null,
   q_editable bit default 0,
   fk_resource_id bigint not null,
   fk_item_id bigint not null,
   primary key (id)
);

create table if not exists o_qp_item_collection (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_name varchar(256),
   fk_owner_id bigint not null,
   primary key (id)
);

create table if not exists o_qp_collection_2_item (
   id bigint not null,
   creationdate datetime not null,
   fk_collection_id bigint not null,
   fk_item_id bigint not null,
   primary key (id)
);

create table if not exists o_qp_edu_context (
   id bigint not null,
   creationdate datetime not null,
   q_level varchar(256) not null,
   q_deletable bit default 0,
   primary key (id)
);

create table if not exists o_qp_item_type (
   id bigint not null,
   creationdate datetime not null,
   q_type varchar(256) not null,
   q_deletable bit default 0,
   primary key (id)
);

create table if not exists o_qp_license (
   id bigint not null,
   creationdate datetime not null,
   q_license varchar(256) not null,
   q_text varchar(2048),
   q_deletable bit default 0,
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
      (case when mark.creator_id is null then 0 else 1 end) as marked,
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
      (case when mark.creator_id is null then 0 else 1 end) as marked,
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
      (case when mark.creator_id is null then 0 else 1 end) as marked,
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
      (case when mark.creator_id is null then 0 else 1 end) as marked,
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

alter table o_qp_pool_2_item add constraint idx_qp_pool_2_item_pool_id foreign key (fk_pool_id) references o_qp_pool(id);
alter table o_qp_pool_2_item add constraint idx_qp_pool_2_item_item_id foreign key (fk_item_id) references o_qp_item(id);
alter table o_qp_pool_2_item add unique (fk_pool_id, fk_item_id);

alter table o_qp_share_item add constraint idx_qp_share_rsrc_id foreign key (fk_resource_id) references o_olatresource(resource_id);
alter table o_qp_share_item add constraint idx_qp_share_item_id foreign key (fk_item_id) references o_qp_item(id);
alter table o_qp_share_item add unique (fk_resource_id, fk_item_id);

alter table o_qp_item_collection add constraint idx_qp_coll_owner_id foreign key (fk_owner_id) references o_bs_identity(id);

alter table o_qp_collection_2_item add constraint idx_qp_coll_coll_id foreign key (fk_collection_id) references o_qp_item_collection(id);
alter table o_qp_collection_2_item add constraint idx_qp_coll_item_id foreign key (fk_item_id) references o_qp_item(id);
alter table o_qp_collection_2_item add unique (fk_collection_id, fk_item_id);

alter table o_qp_item add constraint idx_qp_pool_2_field_id foreign key (fk_taxonomy_level) references o_qp_taxonomy_level(id);
alter table o_qp_item add constraint idx_qp_item_owner_id foreign key (fk_ownergroup) references o_bs_secgroup(id);
alter table o_qp_item add constraint idx_qp_item_edu_ctxt_id foreign key (fk_edu_context) references o_qp_edu_context(id);
alter table o_qp_item add constraint idx_qp_item_type_id foreign key (fk_type) references o_qp_item_type(id);
alter table o_qp_item add constraint idx_qp_item_license_id foreign key (fk_license) references o_qp_license(id);

alter table o_qp_taxonomy_level add constraint idx_qp_field_2_parent_id foreign key (fk_parent_field) references o_qp_taxonomy_level(id);
create index idx_taxon_mat_pathon on o_qp_taxonomy_level (q_mat_path_ids(255));

alter table o_qp_item_type add unique (q_type(200));


-- lti
create table o_lti_outcome (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   r_ressubpath varchar(2048),
   r_action varchar(255) not null,
   r_outcome_key varchar(255) not null,
   r_outcome_value varchar(2048),
   fk_resource_id bigint not null,
   fk_identity_id bigint not null,
   primary key (id)
);
alter table o_lti_outcome ENGINE = InnoDB;

alter table o_lti_outcome add constraint idx_lti_outcome_ident_id foreign key (fk_identity_id) references o_bs_identity(id);
alter table o_lti_outcome add constraint idx_lti_outcome_rsrc_id foreign key (fk_resource_id) references o_olatresource(resource_id);

-- mapper
alter table o_mapper add column expirationdate datetime;

-- mail
alter table o_mail_attachment add column datas_checksum bigint;
alter table o_mail_attachment add column datas_path varchar(1024);
alter table o_mail_attachment add column datas_lastmodified datetime;
create index idx_mail_att_checksum_idx on o_mail_attachment (datas_checksum);
create index idx_mail_path_idx on o_mail_attachment (datas_path(255));
create index idx_mail_att_siblings_idx on o_mail_attachment (datas_checksum, mimetype, datas_size, datas_name);

-- managed groups and repository entries
create table o_repositoryentry_cycle (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   r_softkey varchar(64),
   r_label varchar(255),
   r_privatecycle bit default 0,
   r_validfrom datetime,
   r_validto datetime,
   primary key (id)
);

alter table  o_repositoryentry_cycle ENGINE = InnoDB;
create index idx_re_lifecycle_soft_idx on o_repositoryentry_cycle (r_softkey);

alter table o_repositoryentry add column external_id varchar(64);
alter table o_repositoryentry add column external_ref varchar(64);
alter table o_repositoryentry add column managed_flags varchar(255);
create index idx_re_lifecycle_extid_idx on o_repositoryentry (external_id);
create index idx_re_lifecycle_extref_idx on o_repositoryentry (external_ref);

alter table o_repositoryentry add column fk_lifecycle bigint;
alter table o_repositoryentry add constraint idx_re_lifecycle_fk foreign key (fk_lifecycle) references o_repositoryentry_cycle(id);

alter table o_gp_business add column external_id varchar(64);
alter table o_gp_business add column managed_flags varchar(255);
create index idx_grp_lifecycle_soft_idx on o_gp_business (external_id);

-- complet missing index

create index idx_ident_creationdate_idx on o_bs_identity (creationdate);
create index idx_id_lastlogin_idx on o_bs_identity (lastlogin);

create index idx_policy_grp_rsrc_idx on o_bs_policy (oresource_id, group_id);


-- task executor
create table o_ex_task (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   e_name varchar(255) not null,
   e_status varchar(16) not null,
   e_executor_node varchar(16),
   e_executor_boot_id varchar(64),
   e_task mediumtext not null,
   primary key (id)
);
alter table o_ex_task ENGINE = InnoDB;

drop view o_re_strict_participant_v;
drop view o_re_strict_tutor_v;
drop view o_re_strict_member_v;

create or replace view o_re_participant_v as
select
   re1.repositoryentry_id as re1_id,
   re2.repositoryentry_id as re2_id,
   case when re1.repositoryentry_id is null then re2.repositoryentry_id else re1.repositoryentry_id end as re_id,
   bs_member.identity_id as member_id
  from o_bs_membership as bs_member
  left join o_gp_business as bgroup on (bs_member.secgroup_id = bgroup.fk_partipiciantgroup)
  left join o_gp_business_to_resource as bgroup_rel on (bgroup.group_id = bgroup_rel.fk_group)
  left join o_repositoryentry as re1 on (bs_member.secgroup_id = re1.fk_participantgroup)
  left join o_repositoryentry as re2 on (re2.fk_olatresource = bgroup_rel.fk_resource)
  where re1.repositoryentry_id is not null or re2.repositoryentry_id is not null;

create or replace view o_re_tutor_v as
select
   re1.repositoryentry_id as re1_id,
   re2.repositoryentry_id as re2_id,
   case when re1.repositoryentry_id is null then re2.repositoryentry_id else re1.repositoryentry_id end as re_id,
   bs_member.identity_id as member_id
  from o_bs_membership as bs_member
  left join o_gp_business as bgroup on (bs_member.secgroup_id = bgroup.fk_ownergroup)
  left join o_gp_business_to_resource as bgroup_rel on (bgroup.group_id = bgroup_rel.fk_group)
  left join o_repositoryentry as re1 on (bs_member.secgroup_id = re1.fk_tutorgroup)
  left join o_repositoryentry as re2 on (re2.fk_olatresource = bgroup_rel.fk_resource)
  where re1.repositoryentry_id is not null or re2.repositoryentry_id is not null;

create or replace view o_gp_member_v as
   select
      gp.group_id as bg_id,
      gp.groupname as bg_name,
      gp.creationdate as bg_creationdate,
      gp.managed_flags as bg_managed_flags,
      gp.descr as bg_desc,
      membership.identity_id as member_id
   from o_bs_membership membership
   inner join o_gp_business gp on (membership.secgroup_id = gp.fk_ownergroup) 
   union select
      gp.group_id as bg_id,
      gp.groupname as bg_name,
      gp.creationdate as bg_creationdate,
      gp.managed_flags as bg_managed_flags,
      gp.descr as bg_desc,
      membership.identity_id as member_id
   from o_bs_membership membership
   inner join o_gp_business gp on (membership.secgroup_id = gp.fk_partipiciantgroup);

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
      (case when gp.waitinglist_enabled = 1
         then 
           (select count(waiting.id) from o_bs_membership as waiting where waiting.secgroup_id = gp.fk_partipiciantgroup)
         else
           0
      end) as num_waiting,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=1
         and (offer.validfrom is null or offer.validfrom <= current_timestamp())
         and (offer.validto is null or offer.validto >= current_timestamp())
      ) as num_of_valid_offers,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=1
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


