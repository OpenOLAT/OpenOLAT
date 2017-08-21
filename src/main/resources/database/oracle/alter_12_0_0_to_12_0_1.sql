alter table o_feed modify f_external_feed_url varchar(4000);
alter table o_feed modify f_external_image_url varchar(4000);
alter table o_feed modify f_description varchar(4000);
alter table o_feed modify f_image_name varchar(1024);

alter table o_feed_item modify f_external_url varchar(4000);
alter table o_feed_item modify f_external_link varchar(4000);
