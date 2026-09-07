-- OO-9596
alter table o_gr_grade_system add column g_default bool default false not null;


-- Certificates
alter table o_cer_certificate add column c_generation_retries int8 default 0 not null;
alter table o_cer_certificate add column c_generation_next_date timestamp;
alter table o_cer_certificate add column c_generation_data text;

-- OO-9594
alter table o_info_message_to_group add column sendmailto varchar(255);
alter table o_info_message_to_cur_el add column sendmailto varchar(255);

-- Teams meeting
alter table o_teams_meeting add column t_recordings_publishing varchar(64);
alter table o_teams_meeting add column t_record bool default false not null;
alter table o_teams_meeting add column t_record_auto_start bool default false not null;

alter table o_teams_meeting add column t_organizer_azure_id varchar(255);
alter table o_teams_meeting add column t_organizer_token text;
alter table o_teams_meeting add column fk_organizer_id int8;

alter table o_teams_meeting add constraint teams_org_ident_idx foreign key (fk_organizer_id) references o_bs_identity (id);
create index idx_teams_org_ident_idx on o_teams_meeting(fk_organizer_id);

create table o_teams_recording (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   t_recording_id varchar(512),
   t_start_date timestamp,
   t_end_date timestamp,
   t_status varchar(16) not null,
   t_permanent bool,
   t_publish_to varchar(128),
   t_attempts int8 default 0 not null,
   fk_recording_metadata_id int8,
   fk_meeting_id int8 not null,
   primary key (id)
);

alter table o_teams_recording add constraint teams_rec_meet_idx foreign key (fk_meeting_id) references o_teams_meeting (id);
create index idx_teams_rec_meet_idx on o_teams_recording (fk_meeting_id);

alter table o_teams_recording add constraint teams_rec_data_idx foreign key (fk_recording_metadata_id) references o_vfs_metadata(id);
create index idx_teams_rec_data_idx on o_teams_recording (fk_recording_metadata_id);

create unique index idx_teams_rec_graph_unique on o_teams_recording (t_recording_id, fk_meeting_id);

-- Access control
alter table o_eva_form_survey add column e_display_name varchar(255);

create table o_ac_offer_to_survey (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  a_pos int4 not null,
  fk_offer int8 not null,
  fk_survey int8 not null,
  primary key (id)
);
alter table o_ac_offer_to_survey add constraint ac_offer_to_survey_offer_idx foreign key (fk_offer) references o_ac_offer(offer_id);
create index idx_ac_offer_to_survey_offer_idx on o_ac_offer_to_survey (fk_offer);
alter table o_ac_offer_to_survey add constraint ac_offer_to_survey_survey_idx foreign key (fk_survey) references o_eva_form_survey(id);
create unique index idx_ac_offer_to_survey_uniq_idx on o_ac_offer_to_survey (fk_offer, fk_survey);

alter table o_cur_curriculum_element add column c_order_form_required bool default false not null;
