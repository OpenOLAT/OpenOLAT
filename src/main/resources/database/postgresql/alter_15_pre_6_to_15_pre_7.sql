-- Assessment
alter table o_as_entry add a_passed_original bool;
alter table o_as_entry add a_passed_mod_date timestamp;
alter table o_as_entry add fk_identity_passed_mod int8;
