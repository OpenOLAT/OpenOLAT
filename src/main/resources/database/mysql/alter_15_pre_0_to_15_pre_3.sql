-- Assessment
alter table o_as_entry add a_date_end datetime;
alter table o_as_entry add a_date_end_original datetime;
alter table o_as_entry add a_date_end_mod_date datetime;
alter table o_as_entry add fk_identity_end_date_mod bigint;
