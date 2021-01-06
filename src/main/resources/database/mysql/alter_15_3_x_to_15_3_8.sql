-- BigBlueButton
alter table o_bbb_meeting add column b_password varchar(64) default null;
alter table o_bbb_meeting add column b_directory varchar(64) default null;
alter table o_bbb_meeting add constraint bbb_dir_idx unique (b_directory);

alter table o_bbb_recording add column b_permanent bool default null;

-- Livestream
create table o_livestream_url_template (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   l_name varchar(64) not null,
   l_url1 varchar(2048),
   l_url2 varchar(2048),
   primary key (id)
);
