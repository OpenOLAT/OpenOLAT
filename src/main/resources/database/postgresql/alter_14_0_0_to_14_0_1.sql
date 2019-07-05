-- Adobe Connect
alter table o_aconnect_meeting add column a_leadtime bigint default 0 not null;
alter table o_aconnect_meeting add column a_start_with_leadtime timestamp;
alter table o_aconnect_meeting add column a_followuptime bigint default 0 not null;
alter table o_aconnect_meeting add column a_end_with_followuptime timestamp;
alter table o_aconnect_meeting add column a_opened bool default false not null;
alter table o_aconnect_meeting add column a_template_id varchar(32) default null;
alter table o_aconnect_meeting add column a_permanent bool default false not null;