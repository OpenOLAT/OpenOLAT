-- Assessment
alter table o_as_entry add a_passed_original bit;
alter table o_as_entry add a_passed_mod_date datetime;
alter table o_as_entry add fk_identity_passed_mod bigint;

-- Quality management
alter table o_qual_reminder modify column q_type varchar(65);
