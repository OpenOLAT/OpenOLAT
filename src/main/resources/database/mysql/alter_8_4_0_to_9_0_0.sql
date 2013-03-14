drop table o_qp_pool_2_item;
drop table o_qp_share_item;
drop table o_qp_collection_2_item;
drop table o_qp_item_collection;
drop table o_qp_pool;
drop table o_qp_item;
drop table o_qp_taxonomy_level;

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
   q_educational_context varchar(128),
   q_educational_learningtime varchar(32),
   q_type varchar(64),
   q_difficulty decimal(10,9),
   q_stdev_difficulty decimal(10,9),
   q_differentiation decimal(10,9),
   q_num_of_answers_alt bigint not null default 0,
   q_usage bigint not null default 0,
   q_assessment_type varchar(64),
   q_status varchar(32) not null,
   q_version varchar(50),
   q_copyright varchar(2048),
   q_editor varchar(256),
   q_editor_version varchar(256),
   q_format varchar(32) not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_dir varchar(32),
   q_root_filename varchar(255),
   fk_taxonomy_level bigint,
   fk_ownergroup bigint,
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
alter table o_qp_taxonomy_level add constraint idx_qp_field_2_parent_id foreign key (fk_parent_field) references o_qp_taxonomy_level(id);


