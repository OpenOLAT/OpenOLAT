-- Repository
alter table o_repositoryentry add column lifecycle_over_eval_date date;

-- Appointment scheduling
alter table o_ap_appointment add column a_meeting_title varchar2(1024);
alter table o_ap_appointment add column a_meeting_url varchar2(1024);
alter table o_ap_appointment add column a_recording_enabled number default 0 not null;