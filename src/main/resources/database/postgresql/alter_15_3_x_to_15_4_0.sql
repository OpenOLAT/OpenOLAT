-- Appointments
alter table o_ap_topic add column a_participation_visible bool default true not null;

-- VFS
alter table o_vfs_metadata add column fk_lastmodified_by bigint default null;

alter table o_vfs_metadata add constraint fmeta_modified_by_idx foreign key (fk_lastmodified_by) references o_bs_identity (id);
create index idx_fmeta_modified_by_idx on o_vfs_metadata (fk_lastmodified_by);

-- Identity
alter table o_bs_identity add column expirationdate timestamp default null;
alter table o_bs_identity add column expirationemaildate timestamp default null;

-- Repository
create table o_re_educational_type (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   r_identifier varchar(128) not null,
   r_predefined bool not null default false,
   r_css_class varchar(128),
   primary key (id)
);

alter table o_repositoryentry add technical_type varchar(128);
alter table o_repositoryentry add column fk_educational_type bigint default null;
alter table o_repositoryentry add constraint idx_re_edu_type_fk foreign key (fk_educational_type) references o_re_educational_type(id);
create unique index idc_re_edu_type_ident on o_re_educational_type (r_identifier);


-- Teams
create table o_teams_meeting (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   t_subject varchar(255),
   t_description varchar(4000),
   t_main_presenter varchar(255),
   t_start_date timestamp default null,
   t_leadtime int8 default 0 not null,
   t_start_with_leadtime timestamp,
   t_end_date timestamp default null,
   t_followuptime int8 default 0 not null,
   t_end_with_followuptime timestamp,
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
   fk_entry_id int8 default null,
   a_sub_ident varchar(64) default null,
   fk_group_id int8 default null,
   fk_creator_id int8 default null,
   primary key (id)
);

alter table o_teams_meeting add constraint teams_meet_entry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_teams_meet_entry_idx on o_teams_meeting(fk_entry_id);
alter table o_teams_meeting add constraint teams_meet_grp_idx foreign key (fk_group_id) references o_gp_business (group_id);
create index idx_teams_meet_grp_idx on o_teams_meeting(fk_group_id);
alter table o_teams_meeting add constraint teams_meet_creator_idx foreign key (fk_creator_id) references o_bs_identity (id);
create index idx_teams_meet_creator_idx on o_teams_meeting(fk_creator_id);


create table o_teams_user (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   t_identifier varchar(128),
   t_displayname varchar(512),
   fk_identity_id int8 default null,
   unique(fk_identity_id),
   primary key (id)
);

alter table o_teams_user add constraint teams_user_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_teams_user_ident_idx on o_teams_user(fk_identity_id);


create table o_teams_attendee (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   t_role varchar(32),
   t_join_date timestamp not null,
   fk_identity_id int8 default null,
   fk_teams_user_id int8 default null,
   fk_meeting_id int8 not null,
   primary key (id)

);

alter table o_teams_attendee add constraint teams_att_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_teams_att_ident_idx on o_teams_attendee(fk_identity_id);
alter table o_teams_attendee add constraint teams_att_user_idx foreign key (fk_teams_user_id) references o_teams_user (id);
create index idx_teams_att_user_idx on o_teams_attendee(fk_teams_user_id);
alter table o_teams_attendee add constraint teams_att_meet_idx foreign key (fk_meeting_id) references o_teams_meeting (id);
create index idx_teams_att_meet_idx on o_teams_attendee(fk_meeting_id);


alter table o_ap_appointment add fk_teams_id bigint;
alter table o_ap_appointment add constraint ap_appointment_teams_idx foreign key (fk_teams_id) references o_teams_meeting (id);
create index idx_ap_appointment_teams_idx on o_ap_appointment(fk_teams_id);
