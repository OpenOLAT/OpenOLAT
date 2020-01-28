-- Assessment
alter table o_as_entry add a_date_end timestamp;
alter table o_as_entry add a_date_end_original timestamp;
alter table o_as_entry add a_date_end_mod_date timestamp;
alter table o_as_entry add fk_identity_end_date_mod int8;
