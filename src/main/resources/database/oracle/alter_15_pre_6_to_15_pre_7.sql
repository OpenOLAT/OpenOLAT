-- Assessment
alter table o_as_entry add a_passed_original number;
alter table o_as_entry add a_passed_mod_date date;
alter table o_as_entry add fk_identity_passed_mod number(20);

-- Quality management
alter table o_qual_reminder modify (q_type varchar2(65));
