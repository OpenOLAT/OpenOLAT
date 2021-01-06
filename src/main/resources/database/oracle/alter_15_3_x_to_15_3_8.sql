-- BigBlueButton
alter table o_bbb_meeting add b_password varchar2(64) default null;
alter table o_bbb_meeting add b_directory varchar2(64) default null;
alter table o_bbb_meeting add constraint bbb_dir_idx unique (b_directory);

alter table o_bbb_recording add b_permanent number default null;

-- Livestream
create table o_livestream_url_template (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_name varchar2(64) not null,
   l_url1 varchar2(2048),
   l_url2 varchar2(2048),
   primary key (id)
);
