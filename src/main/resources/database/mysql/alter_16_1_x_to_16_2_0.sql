-- Assessment mode
alter table o_as_mode_course add column a_safeexambrowserconfig_xml mediumtext;
alter table o_as_mode_course add column a_safeexambrowserconfig_plist mediumtext;
alter table o_as_mode_course add column a_safeexambrowserconfig_pkey varchar(255);
alter table o_as_mode_course add column a_safeexambrowserconfig_dload bool not null default true;

-- VFS metadata
alter table o_vfs_metadata add column f_expiration_date timestamp default null;
create index f_exp_date_idx on o_vfs_metadata (f_expiration_date);

-- Task
alter table o_ex_task add column e_progress decimal default null;
alter table o_ex_task add column e_checkpoint varchar(255) default null;

-- IM
alter table o_im_roster_entry add column r_ressubpath varchar(255) default null;
alter table o_im_roster_entry add column r_channel varchar(255) default null;
alter table o_im_roster_entry add column r_persistent bool not null default false;
alter table o_im_roster_entry add column r_active bool not null default true;
alter table o_im_roster_entry add column r_read_upto timestamp default null;

create index idx_im_rost_sub_idx on o_im_roster_entry (r_resid,r_resname,r_ressubpath);

alter table o_im_message add column msg_ressubpath varchar(255) default null;
alter table o_im_message add column msg_channel varchar(255) default null;
alter table o_im_message add column msg_type varchar(8) not null default 'text';
alter table o_im_message add column fk_meeting_id bigint;
alter table o_im_message add column fk_teams_id bigint;

create index idx_im_msg_channel_idx on o_im_message (msg_resid,msg_resname,msg_ressubpath,msg_channel);

alter table o_im_message add constraint im_msg_bbb_idx foreign key (fk_meeting_id) references o_bbb_meeting (id);
alter table o_im_message add constraint im_msg_teams_idx foreign key (fk_teams_id) references o_teams_meeting (id);

alter table o_im_notification add column chat_ressubpath varchar(255) default null;
alter table o_im_notification add column chat_channel varchar(255) default null;
alter table o_im_notification add column chat_type varchar(16) not null default 'message';

create index idx_im_chat_typed_idx on o_im_notification (fk_to_identity_id,chat_type);
