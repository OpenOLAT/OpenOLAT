alter table o_ac_offer add column autobooking boolean default 0;

create index idx_mail_meta_id_idx on o_mail (meta_mail_id);