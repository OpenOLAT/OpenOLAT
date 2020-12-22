-- Appointments
alter table o_ap_topic add a_participation_visible number default 1 not null;

-- VFS
alter table o_vfs_metadata add fk_lastmodified_by number(20) default null;

alter table o_vfs_metadata add constraint fmeta_modified_by_idx foreign key (fk_lastmodified_by) references o_bs_identity (id);
create index idx_fmeta_modified_by_idx on o_vfs_metadata (fk_lastmodified_by);


-- Teams
create table o_teams_meeting (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   t_subject varchar(255),
   t_description varchar(4000),
   t_main_presenter varchar(255),
   t_start_date timestamp default null,
   t_leadtime number(20) default 0 not null,
   t_start_with_leadtime timestamp,
   t_end_date timestamp default null,
   t_followuptime number(20) default 0 not null,
   t_end_with_followuptime timestamp,
   t_permanent number default 0,
   t_join_information varchar(4000),
   t_guest number default 0 not null,
   t_identifier varchar(64),
   t_read_identifier varchar(64),
   t_online_meeting_id varchar(128),
   t_online_meeting_join_url varchar(2000),
   t_allowed_presenters varchar(32) default 'EVERYONE',
   t_access_level varchar(32) default 'EVERYONE',
   t_entry_exit_announcement number default 1,
   t_lobby_bypass_scope varchar(32) default 'ORGANIZATION_AND_FEDERATED',
   fk_entry_id number(20) default null,
   a_sub_ident varchar(64) default null,
   fk_group_id number(20) default null,
   fk_creator_id number(20) default null,
   primary key (id)
);

alter table o_teams_meeting add constraint teams_meet_entry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_teams_meet_entry_idx on o_teams_meeting(fk_entry_id);
alter table o_teams_meeting add constraint teams_meet_grp_idx foreign key (fk_group_id) references o_gp_business (group_id);
create index idx_teams_meet_grp_idx on o_teams_meeting(fk_group_id);
alter table o_teams_meeting add constraint teams_meet_creator_idx foreign key (fk_creator_id) references o_bs_identity (id);
create index idx_teams_meet_creator_idx on o_teams_meeting(fk_creator_id);


create table o_teams_user (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   t_identifier varchar(128),
   t_displayname varchar(512),
   fk_identity_id number(20) default null,
   unique(fk_identity_id),
   primary key (id)
);

alter table o_teams_user add constraint teams_user_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);


create table o_teams_attendee (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   t_role varchar(32),
   t_join_date timestamp not null,
   fk_identity_id number(20) default null,
   fk_teams_user_id number(20) default null,
   fk_meeting_id number(20) not null,
   primary key (id)

);

alter table o_teams_attendee add constraint teams_att_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_teams_att_ident_idx on o_teams_attendee(fk_identity_id);
alter table o_teams_attendee add constraint teams_att_user_idx foreign key (fk_teams_user_id) references o_teams_user (id);
create index idx_teams_att_user_idx on o_teams_attendee(fk_teams_user_id);
alter table o_teams_attendee add constraint teams_att_meet_idx foreign key (fk_meeting_id) references o_teams_meeting (id);
create index idx_teams_att_meet_idx on o_teams_attendee(fk_meeting_id);


alter table o_ap_appointment add fk_teams_id number(20);
alter table o_ap_appointment add constraint ap_appointment_teams_idx foreign key (fk_teams_id) references o_teams_meeting (id);
create index idx_ap_appointment_teams_idx on o_ap_appointment(fk_teams_id);


