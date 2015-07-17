alter table o_ac_offer add autobooking number default 0 not null;

create index idx_mail_meta_id_idx on o_mail (meta_mail_id);
