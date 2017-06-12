-- webfeed
create table o_feed ( 
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   f_resourceable_id number(20),
   f_resourceable_type varchar(64),
   f_title varchar(1024), 
   f_description varchar(1024),
   f_author varchar(255),
   f_image_name varchar(255),
   f_external number(2) default 0,
   f_external_feed_url varchar(1024),
   f_external_image_url varchar(1024),
   primary key (id)
);

create table o_feed_item (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   f_title varchar(1024),
   f_description clob,
   f_content clob,
   f_author varchar(255),
   f_guid varchar(255),
   f_external_link varchar(1024),
   f_draft number(2) default 0,
   f_publish_date date,
   f_width number(20),
   f_height number(20),
   f_filename varchar(1024),
   f_type varchar(255),
   f_length number(20),
   f_external_url varchar(1024),
   fk_feed_id number(20),
   fk_identity_author_id number(20),
   fk_identity_modified_id number(20),
   primary key (id)
);

create index idx_feed_resourceable_idx on o_feed (f_resourceable_id, f_resourceable_type);
alter table o_feed_item add constraint item_to_feed_fk foreign key(fk_feed_id) references o_feed(id);
create index idx_item_feed_idx on o_feed_item(fk_feed_id);
alter table o_feed_item add constraint feed_item_to_ident_author_fk foreign key (fk_identity_author_id) references o_bs_identity (id);
create index idx_item_ident_author_idx on o_feed_item (fk_identity_author_id);
alter table o_feed_item add constraint feed_item_to_ident_modified_fk foreign key (fk_identity_modified_id) references o_bs_identity (id);
create index idx_item_ident_modified_idx on o_feed_item (fk_identity_modified_id);
