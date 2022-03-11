-- Assessment mode
alter table o_as_mode_course add a_safeexambrowserconfig_xml clob;
alter table o_as_mode_course add a_safeexambrowserconfig_plist clob;
alter table o_as_mode_course add a_safeexambrowserconfig_pkey varchar(255);
alter table o_as_mode_course add a_safeexambrowserconfig_dload number default 1 not null;

-- VFS metadata
alter table o_vfs_metadata add f_expiration_date date default null;
create index f_exp_date_idx on o_vfs_metadata (f_expiration_date);

-- Task
alter table o_ex_task add e_progress decimal default null;
alter table o_ex_task add e_checkpoint varchar(255) default null;

-- IM
alter table o_im_roster_entry add r_ressubpath varchar(255) default null;
alter table o_im_roster_entry add r_channel varchar(255) default null;
alter table o_im_roster_entry add r_persistent number default 0 not null;
alter table o_im_roster_entry add r_active number default 1 not null;
alter table o_im_roster_entry add r_read_upto date default null;

create index idx_im_rost_sub_idx on o_im_roster_entry (r_resid,r_resname,r_ressubpath);

alter table o_im_message add msg_ressubpath varchar(255) default null;
alter table o_im_message add msg_channel varchar(255) default null;
alter table o_im_message add msg_type varchar(8) default 'text' not null;
alter table o_im_message add fk_meeting_id number(20);
alter table o_im_message add fk_teams_id number(20);

create index idx_im_msg_channel_idx on o_im_message (msg_resid,msg_resname,msg_ressubpath,msg_channel);

alter table o_im_message add constraint im_msg_bbb_idx foreign key (fk_meeting_id) references o_bbb_meeting (id);
create index idx_im_msg_bbb_idx on o_im_message(fk_meeting_id);
alter table o_im_message add constraint im_msg_teams_idx foreign key (fk_teams_id) references o_teams_meeting (id);
create index idx_im_msg_teams_idx on o_im_message(fk_teams_id);

alter table o_im_notification add chat_ressubpath varchar(255) default null;
alter table o_im_notification add chat_channel varchar(255) default null;
alter table o_im_notification add chat_type varchar(16) default 'message' not null;

create index idx_im_chat_typed_idx on o_im_notification (fk_to_identity_id,chat_type);
