create table o_bbb_template (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   b_name varchar(128) not null,
   b_description varchar(2000) default null,
   b_system bool default false not null,
   b_external_id varchar(255) default null,
   b_max_concurrent_meetings int default null,
   b_max_participants int8 default null,
   b_mute_on_start bool default null,
   b_auto_start_recording bool default null,
   b_allow_start_stop_recording bool default null,
   b_webcams_only_for_moderator bool default null,
   b_allow_mods_to_unmute_users bool default null,
   b_lock_set_disable_cam bool default null,
   b_lock_set_disable_mic bool default null,
   b_lock_set_disable_priv_chat bool default null,
   b_lock_set_disable_public_chat bool default null,
   b_lock_set_disable_note bool default null,
   b_lock_set_locked_layout bool default null,
   b_guest_policy varchar(32) default null,
   primary key (id)
);

create table o_bbb_meeting (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   b_meeting_id varchar(128) not null,
   b_attendee_pw varchar(128) not null,
   b_moderator_pw varchar(128) not null,
   b_name varchar(128) not null,
   b_description varchar(2000) default null,
   b_welcome text,
   b_permanent bool default false not null,
   b_start_date timestamp default null,
   b_leadtime bigint default 0 not null,
   b_start_with_leadtime timestamp,
   b_end_date timestamp default null,
   b_followuptime bigint default 0 not null,
   b_end_with_followuptime timestamp,
   fk_entry_id int8 default null,
   a_sub_ident varchar(64) default null,
   fk_group_id int8 default null,
   fk_template_id int8 default null,
   primary key (id)
);

alter table o_bbb_meeting add constraint bbb_meet_entry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_bbb_meet_entry_idx on o_bbb_meeting(fk_entry_id);
alter table o_bbb_meeting add constraint bbb_meet_grp_idx foreign key (fk_group_id) references o_gp_business (group_id);
create index idx_bbb_meet_grp_idx on o_bbb_meeting(fk_group_id);
alter table o_bbb_meeting add constraint bbb_meet_template_idx foreign key (fk_template_id) references o_bbb_template (id);
create index idx_bbb_meet_template_idx on o_bbb_meeting(fk_template_id);
