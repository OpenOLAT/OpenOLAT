-- question item
create table if not exists o_qp_pool (
   id bigint not null,
   creationdate datetime,
   lastmodified datetime,
   q_name varchar(255),
   primary key (id)
);

create table if not exists o_qp_item (
   id bigint not null,
   creationdate datetime,
   lastmodified datetime,
   q_subject varchar(255),
   primary key (id)
);

create table if not exists o_qp_pool_2_item (
   id bigint not null,
   creationdate datetime,
   fk_pool_id bigint not null,
   fk_item_id bigint not null,
   primary key (id)
);
alter table o_qp_pool_2_item add constraint idx_qp_pool_2_item_pool_id foreign key (fk_pool_id) references o_qp_pool(id);
alter table o_qp_pool_2_item add constraint idx_qp_pool_2_item_item_id foreign key (fk_item_id) references o_qp_item(id);

