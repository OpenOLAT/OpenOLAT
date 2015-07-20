alter table o_gta_task alter column g_taskname type varchar(1024);

create index idx_mail_meta_id_idx on o_mail (meta_mail_id);
