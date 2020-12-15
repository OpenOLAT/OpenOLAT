-- Appointments
alter table o_ap_topic add column a_participation_visible bool default true not null;

-- VFS
alter table o_vfs_metadata add column fk_lastmodified_by bigint default null;

alter table o_vfs_metadata add constraint fmeta_modified_by_idx foreign key (fk_lastmodified_by) references o_bs_identity (id);


-- Teams
create table o_teams_meeting (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   t_subject varchar(255),
   t_description varchar(4000),
   t_main_presenter varchar(255),
   t_start_date datetime default null,
   t_leadtime bigint default 0 not null,
   t_start_with_leadtime datetime,
   t_end_date datetime default null,
   t_followuptime bigint default 0 not null,
   t_end_with_followuptime datetime,
   t_permanent bool default false,
   t_join_information varchar(4000),
   t_guest bool default false not null,
   t_identifier varchar(64),
   t_read_identifier varchar(64),
   t_online_meeting_id varchar(128),
   t_online_meeting_join_url varchar(2000),
   t_allowed_presenters varchar(32) default 'EVERYONE',
   t_access_level varchar(32) default 'EVERYONE',
   t_entry_exit_announcement bool default true,
   t_lobby_bypass_scope varchar(32) default 'ORGANIZATION_AND_FEDERATED',
   fk_entry_id bigint default null,
   a_sub_ident varchar(64) default null,
   fk_group_id bigint default null,
   fk_creator_id bigint default null,
   primary key (id)
);
alter table o_teams_meeting ENGINE = InnoDB;

alter table o_teams_meeting add constraint teams_meet_entry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
alter table o_teams_meeting add constraint teams_meet_grp_idx foreign key (fk_group_id) references o_gp_business (group_id);
alter table o_teams_meeting add constraint teams_meet_creator_idx foreign key (fk_creator_id) references o_bs_identity (id);


create table o_teams_user (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   t_identifier varchar(128),
   t_displayname varchar(512),
   fk_identity_id bigint default null,
   unique(fk_identity_id),
   primary key (id)
);
alter table o_teams_user ENGINE = InnoDB;

alter table o_teams_user add constraint teams_user_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);


create table o_teams_attendee (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   t_role varchar(32),
   t_join_date datetime not null,
   fk_identity_id bigint default null,
   fk_teams_user_id bigint default null,
   fk_meeting_id bigint not null,
   primary key (id)

);
alter table o_teams_attendee ENGINE = InnoDB;

alter table o_teams_attendee add constraint teams_att_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
alter table o_teams_attendee add constraint teams_att_user_idx foreign key (fk_teams_user_id) references o_teams_user (id);
alter table o_teams_attendee add constraint teams_att_meet_idx foreign key (fk_meeting_id) references o_teams_meeting (id);




