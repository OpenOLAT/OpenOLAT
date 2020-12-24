-- BigBlueButton
alter table o_bbb_meeting add b_password varchar2(64) default null;
alter table o_bbb_meeting add b_directory varchar2(64) default null;
alter table o_bbb_meeting add constraint bbb_dir_idx unique (b_directory);

