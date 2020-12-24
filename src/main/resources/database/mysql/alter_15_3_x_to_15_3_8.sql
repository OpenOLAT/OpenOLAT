-- BigBlueButton
alter table o_bbb_meeting add column b_password varchar(64) default null;
alter table o_bbb_meeting add column b_directory varchar(64) default null;
alter table o_bbb_meeting add constraint bbb_dir_idx unique (b_directory);

