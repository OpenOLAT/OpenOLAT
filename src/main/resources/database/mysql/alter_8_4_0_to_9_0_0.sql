drop table o_qp_pool_2_item;
drop table o_qp_share_item;
drop table o_qp_collection_2_item;
drop table o_qp_item_collection;
drop table o_qp_pool;
drop table o_qp_item;
drop table o_qp_study_field;

-- question item
create table if not exists o_qp_pool (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_name varchar(255) not null,
   fk_ownergroup bigint,
   fk_participantgroup bigint,
   primary key (id)
);

create table if not exists o_qp_study_field (
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
   q_uuid varchar(36) not null,
   q_subject varchar(255) not null,
   q_keywords varchar(2048),
   q_type varchar(64),
   q_language varchar(16),
   q_status varchar(32) not null,
   q_description varchar(4000),
   q_copyright varchar(2048),
   q_point decimal,
   q_difficulty decimal,
   q_selectivity decimal,
   q_usage bigint not null,
   q_test_type varchar(64),
   q_level varchar(64),
   q_format varchar(32) not null,
   q_editor varchar(256),
   creationdate datetime not null,
   lastmodified datetime not null,
   q_version varchar(32),
   q_dir varchar(32),
   q_root_filename varchar(255),
   fk_study_field bigint,
   fk_ownergroup bigint,
   primary key (id)
);

create table if not exists o_qp_pool_2_item (
   id bigint not null,
   creationdate datetime not null,
   fk_pool_id bigint not null,
   fk_item_id bigint not null,
   primary key (id)
);

create table if not exists o_qp_share_item (
   id bigint not null,
   creationdate datetime not null,
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
alter table o_qp_pool add constraint idx_qp_pool_part_grp_id foreign key (fk_participantgroup) references o_bs_secgroup(id);

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

alter table o_qp_item add constraint idx_qp_pool_2_field_id foreign key (fk_study_field) references o_qp_study_field(id);
alter table o_qp_item add constraint idx_qp_item_owner_id foreign key (fk_ownergroup) references o_bs_secgroup(id);
alter table o_qp_study_field add constraint idx_qp_field_2_parent_id foreign key (fk_parent_field) references o_qp_study_field(id);


