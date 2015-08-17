alter table o_ac_offer add autobooking number default 0 not null;

alter table o_gta_task add g_assignment_date date;

update o_gta_task set g_assignment_date=creationdate;

create index idx_mail_meta_id_idx on o_mail (meta_mail_id);
