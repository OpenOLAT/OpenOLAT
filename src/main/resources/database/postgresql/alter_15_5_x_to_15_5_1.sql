-- BigBlueButton
alter table o_bbb_template add column b_join_policy varchar(32) default 'disabled' not null;
alter table o_bbb_meeting add column b_join_policy varchar(32) default 'disabled' not null;




