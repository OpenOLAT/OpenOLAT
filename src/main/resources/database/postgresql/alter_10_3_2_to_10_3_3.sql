alter table o_ac_offer add column autobooking bool not null default false;

alter table o_gta_task add column g_assignment_date timestamp;

update o_gta_task set g_assignment_date=creationdate;

