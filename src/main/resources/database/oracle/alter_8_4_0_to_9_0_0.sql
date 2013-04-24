-- onyx
ALTER TABLE o_qtiresultset ADD (issuspended NUMBER);
ALTER TABLE o_qtiresultset ADD (fullyassessed NUMBER);

alter table o_checklist modify (title null);
alter table o_checkpoint modify (title null);

create index o_projectbroker_custflds_idx on o_projectbroker_customfields (fk_project_id);

-- question item
create table o_qp_pool (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   q_name varchar2(255 char) not null,
   q_public number default 0,
   fk_ownergroup number(20),
   primary key (id)
);

create table o_qp_taxonomy_level (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   q_field varchar2(255 char) not null,
   q_mat_path_ids varchar2(1024 char),
   q_mat_path_names varchar2(2048 char),
   fk_parent_field number(20),
   primary key (id)
);

create table o_qp_item (
   id number(20) not null,
   q_identifier varchar2(36 char) not null,
   q_master_identifier varchar2(36 char),
   q_title varchar2(1024 char) not null,
   q_description varchar2(2048 char),
   q_keywords varchar2(1024 char),
   q_coverage varchar2(1024 char),
   q_additional_informations varchar2(256 char),
   q_language varchar2(16 char),
   fk_edu_context number(20),
   q_educational_learningtime varchar2(32 char),
   fk_type number(20),
   q_difficulty decimal(10,9),
   q_stdev_difficulty decimal(10,9),
   q_differentiation decimal(10,9),
   q_num_of_answers_alt number(20) default 0 not null,
   q_usage number(20) default 0 not null,
   q_assessment_type varchar2(64 char),
   q_status varchar2(32 char) not null,
   q_version varchar2(50 char),
   fk_license number(20),
   q_editor varchar2(256 char),
   q_editor_version varchar2(256 char),
   q_format varchar2(32 char) not null,
   creationdate date not null,
   lastmodified date not null,
   q_dir varchar2(32 char),
   q_root_filename varchar2(255 char),
   fk_taxonomy_level number(20),
   fk_ownergroup number(20) not null,
   primary key (id)
);

create table o_qp_pool_2_item (
   id number(20) not null,
   creationdate date not null,
   q_editable number default 0,
   fk_pool_id number(20) not null,
   fk_item_id number(20) not null,
   primary key (id)
);

create table o_qp_share_item (
   id number(20) not null,
   creationdate date not null,
   q_editable number default 0,
   fk_resource_id number(20) not null,
   fk_item_id number(20) not null,
   primary key (id)
);

create table o_qp_item_collection (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   q_name varchar2(256 char),
   fk_owner_id number(20) not null,
   primary key (id)
);

create table o_qp_collection_2_item (
   id number(20) not null,
   creationdate date not null,
   fk_collection_id number(20) not null,
   fk_item_id number(20) not null,
   primary key (id)
);

create table o_qp_edu_context (
   id number(20) not null,
   creationdate date not null,
   q_level varchar2(256 char) not null,
   q_deletable number default 0,
   primary key (id)
);

create table o_qp_item_type (
   id number(20) not null,
   creationdate date not null,
   q_type varchar2(256 char) not null,
   q_deletable number default 0,
   primary key (id)
);

create table o_qp_license (
   id number(20) not null,
   creationdate date not null,
   q_license varchar2(256 char) not null,
   q_text varchar2(2048 char),
   q_deletable number default 0,
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
      taxlevel.q_field as item_taxonomy_level,
      educontext.q_level as item_edu_context,
      item.q_educational_learningtime as item_educational_learningtime,
      itemtype.q_type as item_type,
      item.q_difficulty as item_difficulty,
      item.q_stdev_difficulty as item_stdev_difficulty,
      item.q_differentiation as item_differentiation,
      item.q_num_of_answers_alt as item_num_of_answers_alt,
      item.q_usage as item_usage,
      item.q_status as item_status,
      item.q_format as item_format,
      item.creationdate as item_creationdate,
      item.lastmodified as item_lastmodified,
      ownership.identity_id as owner_id,
      mark.creator_id as mark_creator,
      (case when mark.creator_id is null then 0 else 1 end) as marked,
      (select avg(rating.rating) from o_userrating rating
         where rating.resid=item.id and rating.resname='QuestionItem' and rating.ressubpath is null
      ) as item_rating
   from o_qp_item item
   inner join o_bs_secgroup ownergroup on (ownergroup.id = item.fk_ownergroup)
   left join o_bs_membership ownership on (ownergroup.id = ownership.secgroup_id) 
   left join o_qp_taxonomy_level taxlevel on (item.fk_taxonomy_level = taxlevel.id)
   left join o_qp_item_type itemtype on (item.fk_type = itemtype.id)
   left join o_qp_edu_context educontext on (item.fk_edu_context = educontext.id)
   left join o_mark mark on (mark.resid = item.id and mark.resname = 'QuestionItem')
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
      taxlevel.q_field as item_taxonomy_level,
      educontext.q_level as item_edu_context,
      item.q_educational_learningtime as item_educational_learningtime,
      itemtype.q_type as item_type,
      item.q_difficulty as item_difficulty,
      item.q_stdev_difficulty as item_stdev_difficulty,
      item.q_differentiation as item_differentiation,
      item.q_num_of_answers_alt as item_num_of_answers_alt,
      item.q_usage as item_usage,
      item.q_status as item_status,
      item.q_format as item_format,
      item.creationdate as item_creationdate,
      item.lastmodified as item_lastmodified,
      mark.creator_id as mark_creator,
      (case when mark.creator_id is null then 0 else 1 end) as marked,
      (select avg(rating.rating) from o_userrating rating
         where rating.resid=item.id and rating.resname='QuestionItem' and rating.ressubpath is null
      ) as item_rating
   from o_qp_item item
   inner join o_bs_secgroup ownergroup on (ownergroup.id = item.fk_ownergroup)
   inner join o_bs_membership ownership on (ownergroup.id = ownership.secgroup_id) 
   left join o_mark mark on (mark.resid = item.id and mark.resname = 'QuestionItem')
   left join o_qp_taxonomy_level taxlevel on (item.fk_taxonomy_level = taxlevel.id)
   left join o_qp_item_type itemtype on (item.fk_type = itemtype.id)
   left join o_qp_edu_context educontext on (item.fk_edu_context = educontext.id)
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
      taxlevel.q_field as item_taxonomy_level,
      educontext.q_level as item_edu_context,
      item.q_educational_learningtime as item_educational_learningtime,
      itemtype.q_type as item_type,
      item.q_difficulty as item_difficulty,
      item.q_stdev_difficulty as item_stdev_difficulty,
      item.q_differentiation as item_differentiation,
      item.q_num_of_answers_alt as item_num_of_answers_alt,
      item.q_usage as item_usage,
      item.q_status as item_status,
      item.q_format as item_format,
      item.creationdate as item_creationdate,
      item.lastmodified as item_lastmodified,
      mark.creator_id as mark_creator,
      (case when mark.creator_id is null then 0 else 1 end) as marked,
      (select avg(rating.rating) from o_userrating rating
         where rating.resid=item.id and rating.resname='QuestionItem' and rating.ressubpath is null
      ) as item_rating
   from o_qp_item item
   inner join o_qp_pool_2_item pool2item on (pool2item.fk_item_id = item.id)
   left join o_mark mark on (mark.resid = item.id and mark.resname = 'QuestionItem')
   left join o_qp_taxonomy_level taxlevel on (item.fk_taxonomy_level = taxlevel.id)
   left join o_qp_item_type itemtype on (item.fk_type = itemtype.id)
   left join o_qp_edu_context educontext on (item.fk_edu_context = educontext.id)
);

create or replace view o_qp_pool_2_item_short_v as (
   select
      pool2item.id as item_to_pool_id,
      pool2item.creationdate as item_to_pool_creationdate,
      item.id as item_id,
      pool2item.q_editable as item_editable,
      pool2item.fk_pool_id as item_pool,
      pool.q_name as item_pool_name
   from o_qp_item item
   inner join o_qp_pool_2_item pool2item on (pool2item.fk_item_id = item.id)
   inner join o_qp_pool pool on (pool2item.fk_pool_id = pool.id)
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
      taxlevel.q_field as item_taxonomy_level,
      educontext.q_level as item_edu_context,
      item.q_educational_learningtime as item_educational_learningtime,
      itemtype.q_type as item_type,
      item.q_difficulty as item_difficulty,
      item.q_stdev_difficulty as item_stdev_difficulty,
      item.q_differentiation as item_differentiation,
      item.q_num_of_answers_alt as item_num_of_answers_alt,
      item.q_usage as item_usage,
      item.q_status as item_status,
      item.q_format as item_format,
      item.creationdate as item_creationdate,
      item.lastmodified as item_lastmodified,
      mark.creator_id as mark_creator,
      (case when mark.creator_id is null then 0 else 1 end) as marked,
      (select avg(rating.rating) from o_userrating rating
         where rating.resid=item.id and rating.resname='QuestionItem' and rating.ressubpath is null
      ) as item_rating
   from o_qp_item item
   inner join o_qp_share_item shareditem on (shareditem.fk_item_id = item.id)
   left join o_mark mark on (mark.resid = item.id and mark.resname = 'QuestionItem')
   left join o_qp_taxonomy_level taxlevel on (item.fk_taxonomy_level = taxlevel.id)
   left join o_qp_item_type itemtype on (item.fk_type = itemtype.id)
   left join o_qp_edu_context educontext on (item.fk_edu_context = educontext.id)
);

create or replace view o_qp_share_2_item_short_v as (
   select
      shareditem.id as item_to_share_id,
      shareditem.creationdate as item_to_share_creationdate,
      item.id as item_id,
      shareditem.q_editable as item_editable,
      shareditem.fk_resource_id as resource_id,
      bgroup.groupname as resource_name
   from o_qp_item item
   inner join o_qp_share_item shareditem on (shareditem.fk_item_id = item.id)
   inner join o_gp_business bgroup on (shareditem.fk_resource_id = bgroup.fk_resource)
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
