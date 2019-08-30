-- Assessment
alter table o_as_entry add a_first_visit timestamp null;
alter table o_as_entry add a_last_visit timestamp null;
alter table o_as_entry add a_num_visits int8 null;
