alter table o_ac_offer add column autobooking boolean default 0;

alter table o_gta_task add column g_assignment_date datetime;

update o_gta_task set g_assignment_date=creationdate;

create index idx_mail_meta_id_idx on o_mail (meta_mail_id);
