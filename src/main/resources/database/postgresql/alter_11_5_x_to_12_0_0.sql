-- webfeed
create table o_feed (
   id bigserial not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   f_resourceable_id bigint,
   f_resourceable_type varchar(64),
   f_title varchar(1024),
   f_description varchar(1024),
   f_author varchar(255),
   f_image_name varchar(255),
   f_external boolean,
   f_external_feed_url varchar(1024),
   f_external_image_url varchar(1024),
   primary key (id)
);

create table o_feed_item (
   id bigserial not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   f_title varchar(1024),
   f_description text,
   f_content text,
   f_author varchar(255),
   f_guid varchar(255),
   f_external_link varchar(1024),
   f_draft boolean,
   f_publish_date timestamp,
   f_width int8,
   f_height int8,
   f_filename varchar(1024),
   f_type varchar(255),
   f_length bigint,
   f_external_url varchar(1024),
   fk_feed_id bigint,
   fk_identity_author_id int8,
   fk_identity_modified_id int8,
   primary key (id)
);

create index idx_feed_resourceable_idx on o_feed (f_resourceable_id, f_resourceable_type);
alter table o_feed_item add constraint item_to_feed_fk foreign key(fk_feed_id) references o_feed(id);
create index idx_item_feed_idx on o_feed_item(fk_feed_id);
alter table o_feed_item add constraint feed_item_to_ident_author_fk foreign key (fk_identity_author_id) references o_bs_identity (id);
create index idx_item_ident_author_idx on o_feed_item (fk_identity_author_id);
alter table o_feed_item add constraint feed_item_to_ident_modified_fk foreign key (fk_identity_modified_id) references o_bs_identity (id);
create index idx_item_ident_modified_idx on o_feed_item (fk_identity_modified_id);
