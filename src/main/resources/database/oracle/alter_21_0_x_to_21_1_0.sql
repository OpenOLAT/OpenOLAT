-- OO-9596
alter table o_gr_grade_system add g_default number default 0 not null;

-- Certificates
alter table o_cer_certificate add c_generation_retries number(20) default 0 not null;
alter table o_cer_certificate add c_generation_next_date date;
alter table o_cer_certificate add c_generation_data CLOB;

-- OO-9594
alter table o_info_message_to_group add sendmailto varchar2(255);
alter table o_info_message_to_cur_el add sendmailto varchar2(255);

-- Teams meeting
alter table o_teams_meeting add t_recordings_publishing varchar(64);
alter table o_teams_meeting add t_record number default 0 not null;
alter table o_teams_meeting add t_record_auto_start number default 0 not null;

alter table o_teams_meeting add t_organizer_azure_id varchar(255);
alter table o_teams_meeting add t_organizer_token CLOB;
alter table o_teams_meeting add fk_organizer_id number(20);

alter table o_teams_meeting add constraint teams_org_ident_idx foreign key (fk_organizer_id) references o_bs_identity (id);
create index idx_teams_org_ident_idx on o_teams_meeting(fk_organizer_id);

create table o_teams_recording (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   t_recording_id varchar(512),
   t_start_date date,
   t_end_date date,
   t_status varchar(16) not null,
   t_permanent number,
   t_publish_to varchar(128),
   t_attempts number(20) default 0 not null,
   fk_recording_metadata_id number(20),
   fk_meeting_id number(20) not null,
   primary key (id)
);

alter table o_teams_recording add constraint teams_rec_meet_idx foreign key (fk_meeting_id) references o_teams_meeting (id);
create index idx_teams_rec_meet_idx on o_teams_recording (fk_meeting_id);

alter table o_teams_recording add constraint teams_rec_data_idx foreign key (fk_recording_metadata_id) references o_vfs_metadata(id);
create index idx_teams_rec_data_idx on o_teams_recording (fk_recording_metadata_id);

create unique index idx_teams_rec_graph_unique on o_teams_recording (t_recording_id, fk_meeting_id);

-- Access control
alter table o_eva_form_survey add e_display_name varchar2(255 char);

create table o_ac_offer_to_survey (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  a_pos number(20) not null,
  fk_offer number(20) not null,
  fk_survey number(20) not null,
  primary key (id)
);
alter table o_ac_offer_to_survey add constraint ac_offer_to_survey_offer_idx foreign key (fk_offer) references o_ac_offer(offer_id);
alter table o_ac_offer_to_survey add constraint ac_offer_to_survey_survey_idx foreign key (fk_survey) references o_eva_form_survey(id);
create unique index idx_ac_offer_to_survey_uniq_idx on o_ac_offer_to_survey (fk_offer, fk_survey);

alter table o_cur_curriculum_element add c_order_form_required number default 0 not null;

alter table o_user add u_customernumber varchar2(255 char);
alter table o_org_organisation add o_customernumber varchar2(255 char);
alter table o_ac_billing_address add a_customer_number varchar2(255 char);

-- LTI
alter table o_lti_shared_tool_deployment drop constraint unique_deploy_platform;
