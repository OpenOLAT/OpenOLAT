-- Inactive user
alter table o_bs_identity add column inactivationdate timestamp;
alter table o_bs_identity add column inactivationemaildate timestamp;
alter table o_bs_identity add column deletionemaildate timestamp;


-- BigBlueButton
alter table o_bbb_meeting add column b_layout varchar(16) default 'standard';
alter table o_bbb_meeting add column b_guest bool default false not null;
alter table o_bbb_meeting add column b_identifier varchar(64);
alter table o_bbb_meeting add column b_read_identifier varchar(64);


-- Appointments
create table o_ap_topic (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_title varchar(256),
   a_description varchar(4000),
   fk_entry_id int8 not null,
   a_sub_ident varchar(64) not null,
   primary key (id)
);

create table o_ap_organizer (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   fk_topic_id int8 not null,
   fk_identity_id int8 not null,
   primary key (id)
);

create table o_ap_appointment (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_status varchar(64) not null,
   a_status_mod_date timestamp,
   a_start timestamp,
   a_end timestamp,
   a_location varchar(256),
   a_details varchar(4000),
   a_max_participations int8,
   fk_topic_id int8 not null,
   primary key (id)
);

create table o_ap_participation (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   fk_appointment_id int8 not null,
   fk_identity_id int8 not null,
   primary key (id)
);

alter table o_ap_topic add constraint ap_topic_entry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_ap_topic_entry_idx on o_ap_topic(fk_entry_id);
alter table o_ap_organizer add constraint ap_organizer_topic_idx foreign key (fk_topic_id) references o_ap_topic (id);
create index idx_ap_organizer_topic_idx on o_ap_organizer(fk_topic_id);
alter table o_ap_organizer add constraint ap_organizer_identity_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_ap_organizer_identitiy_idx on o_ap_organizer(fk_identity_id);
alter table o_ap_appointment add constraint ap_appointment_topic_idx foreign key (fk_topic_id) references o_ap_topic (id);
create index idx_ap_appointment_topic_idx on o_ap_appointment(fk_topic_id);
alter table o_ap_participation add constraint ap_part_appointment_idx foreign key (fk_appointment_id) references o_ap_appointment (id);
create index idx_ap_part_appointment_idx on o_ap_participation(fk_appointment_id);
alter table o_ap_participation add constraint ap_part_identity_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_ap_part_identitiy_idx on o_ap_participation(fk_identity_id);

-- Quality management
create index idx_eva_part_survey_idx on o_eva_form_participation (fk_survey);

