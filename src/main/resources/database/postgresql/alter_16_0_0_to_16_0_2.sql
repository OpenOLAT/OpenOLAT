-- BigBlueButton
alter table o_bbb_meeting alter column b_recordings_publishing type varchar(128);
alter table o_bbb_meeting alter column b_recordings_publishing set default 'all';
