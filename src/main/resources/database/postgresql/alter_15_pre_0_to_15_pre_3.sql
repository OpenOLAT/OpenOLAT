-- Assessment
alter table o_as_entry add a_obligation_original varchar(50);
alter table o_as_entry add a_obligation_mod_date timestamp;
alter table o_as_entry add fk_identity_obligation_mod int8;
alter table o_as_entry add a_date_end timestamp;
alter table o_as_entry add a_date_end_original timestamp;
alter table o_as_entry add a_date_end_mod_date timestamp;
alter table o_as_entry add fk_identity_end_date_mod int8;
