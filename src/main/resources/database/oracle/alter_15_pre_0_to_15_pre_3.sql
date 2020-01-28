-- Assessment
alter table o_as_entry add a_date_end date;
alter table o_as_entry add a_date_end_original date;
alter table o_as_entry add a_date_end_mod_date date;
alter table o_as_entry add fk_identity_end_date_mod number(20);
