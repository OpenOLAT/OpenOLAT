-- Assessment
alter table o_as_entry add a_first_visit datetime;
alter table o_as_entry add a_last_visit datetime;
alter table o_as_entry add a_num_visits int8;
alter table o_as_entry add a_date_done datetime;

-- forum
alter table o_forum add f_refresname varchar(50);
alter table o_forum add f_refresid bigint;
create index idx_forum_ref_idx on o_forum (f_refresid, f_refresname);
