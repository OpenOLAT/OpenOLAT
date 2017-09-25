alter table o_feed alter column f_external_feed_url type character varying(4000);
alter table o_feed alter column f_external_image_url type character varying(4000);
alter table o_feed alter column f_description type character varying(4000);
alter table o_feed alter column f_image_name type character varying(1024);

alter table o_feed_item alter column f_external_url type character varying(4000);
alter table o_feed_item alter column f_external_link type character varying(4000);
