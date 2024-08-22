-- Repository
alter table o_repositoryentry add column lifecycle_over_eval_date datetime;

-- Appointment scheduling
alter table o_ap_appointment add column a_meeting_title varchar(1024);
alter table o_ap_appointment add column a_meeting_url varchar(1024);
alter table o_ap_appointment add column a_recording_enabled boolean default false not null;