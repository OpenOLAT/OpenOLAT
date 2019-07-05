-- Adobe Connect
alter table o_aconnect_meeting add a_leadtime number(20) default 0 not null;
alter table o_aconnect_meeting add a_start_with_leadtime timestamp;
alter table o_aconnect_meeting add a_followuptime number(20) default 0 not null;
alter table o_aconnect_meeting add a_end_with_followuptime timestamp;
alter table o_aconnect_meeting add a_opened number default 0 not null;
alter table o_aconnect_meeting add a_template_id varchar(32) default null;
alter table o_aconnect_meeting add a_permanent number default 0 not null;
