-- question item
create table if not exists o_qp_pool (
   id int8 not null,
   creationdate timestamp,
   lastmodified timestamp,
   q_name varchar(255),
   primary key (id)
);

create table if not exists o_qp_item (
   id int8 not null,
   creationdate timestamp,
   lastmodified timestamp,
   q_subject varchar(255),
   primary key (id)
);

create table if not exists o_qp_pool_2_item (
   id int8 not null,
   creationdate timestamp,
   fk_pool_id int8 not null,
   fk_item_id int8 not null,
   primary key (id)
);
alter table o_qp_pool_2_item add constraint idx_qp_pool_2_item_pool_id foreign key (fk_pool_id) references o_qp_pool(id);
alter table o_qp_pool_2_item add constraint idx_qp_pool_2_item_item_id foreign key (fk_item_id) references o_qp_item(id);

