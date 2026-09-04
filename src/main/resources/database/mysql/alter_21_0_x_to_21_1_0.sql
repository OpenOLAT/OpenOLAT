-- OO-9596
alter table o_gr_grade_system add column g_default bool default false not null;

-- Certificates
alter table o_cer_certificate add column c_generation_retries bigint default 0 not null;
alter table o_cer_certificate add column c_generation_next_date datetime;
alter table o_cer_certificate add column c_generation_data mediumtext;

-- OO-9594
alter table o_info_message_to_group add column sendmailto varchar(255);
alter table o_info_message_to_cur_el add column sendmailto varchar(255);

-- Teams meeting
alter table o_teams_meeting add column t_recordings_publishing varchar(64);
alter table o_teams_meeting add column t_record bool default false not null;
alter table o_teams_meeting add column t_record_auto_start bool default false not null;

alter table o_teams_meeting add column t_organizer_azure_id varchar(255);
alter table o_teams_meeting add column t_organizer_token mediumtext;
alter table o_teams_meeting add column fk_organizer_id bigint;

alter table o_teams_meeting add constraint teams_org_ident_idx foreign key (fk_organizer_id) references o_bs_identity (id);

create table o_teams_recording (
   id bigint auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   t_recording_id varchar(512),
   t_start_date datetime,
   t_end_date datetime,
   t_status varchar(16) not null,
   t_permanent bool,
   t_publish_to varchar(128),
   t_attempts bigint default 0 not null,
   fk_recording_metadata_id bigint,
   fk_meeting_id bigint not null,
   primary key (id)
);
alter table o_teams_recording ENGINE = InnoDB;

alter table o_teams_recording add constraint teams_rec_meet_idx foreign key (fk_meeting_id) references o_teams_meeting (id);
alter table o_teams_recording add constraint teams_rec_data_idx foreign key (fk_recording_metadata_id) references o_vfs_metadata(id);
create unique index idx_teams_rec_graph_unique on o_teams_recording (t_recording_id(255), fk_meeting_id);

