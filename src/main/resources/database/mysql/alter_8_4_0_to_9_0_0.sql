-- question item
create table if not exists o_qp_pool (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_name varchar(255) not null,
   primary key (id)
);

create table if not exists o_qp_study_field (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_field varchar(255) not null,
   fk_parent_field bigint,
   primary key (id)
);

create table if not exists o_qp_item (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_subject varchar(255) not null,
   q_point decimal,
   q_type varchar(64) not null,
   q_status varchar(32) not null,
   fk_study_field bigint,
   primary key (id)
);

create table if not exists o_qp_pool_2_item (
   id bigint not null,
   creationdate datetime not null,
   fk_pool_id bigint not null,
   fk_item_id bigint not null,
   primary key (id)
);

alter table o_qp_pool_2_item add constraint idx_qp_pool_2_item_pool_id foreign key (fk_pool_id) references o_qp_pool(id);
alter table o_qp_pool_2_item add constraint idx_qp_pool_2_item_item_id foreign key (fk_item_id) references o_qp_item(id);
alter table o_qp_item add constraint idx_qp_pool_2_field_id foreign key (fk_study_field) references o_qp_study_field(id);
alter table o_qp_study_field add constraint idx_qp_field_2_parent_id foreign key (fk_parent_field) references o_qp_study_field(id);







