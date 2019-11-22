-- Assessment
alter table o_as_entry add a_date_done timestamp;
alter table o_as_entry add a_date_fully_assessed timestamp;
alter table o_as_entry add a_date_start timestamp;
alter table o_as_entry add a_duration int8;
alter table o_as_entry add a_obligation varchar(50);
alter table o_as_entry add a_first_visit timestamp;
alter table o_as_entry add a_last_visit timestamp;
alter table o_as_entry add a_num_visits int8;
alter table o_as_entry add a_entry_root bool;

-- Forum
alter table o_forum add f_refresname varchar(50);
alter table o_forum add f_refresid bigint;
create index idx_forum_ref_idx on o_forum (f_refresid, f_refresname);
