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

create index idx_as_entry_start_idx on o_as_entry (a_date_start) where a_date_start is not null;

-- Curriculum
alter table o_cur_element_type add c_learning_progress varchar(16);
alter table o_cur_curriculum_element add c_learning_progress varchar(16);

-- Forum
alter table o_forum add f_refresname varchar(50);
alter table o_forum add f_refresid bigint;
create index idx_forum_ref_idx on o_forum (f_refresid, f_refresname);
