-- Efficiency statment
alter table o_as_eff_statement add column last_statement bool default true not null;
alter table o_as_eff_statement add column archive_path varchar(255);
alter table o_as_eff_statement add column archive_certificate int8;
alter table o_as_eff_statement add column completion float8;

alter table o_as_eff_statement drop constraint o_as_eff_statement_fk_identity_fk_resource_id_key;

alter table o_as_entry add column a_run int8 default 1 not null;

alter table o_as_user_course_infos add column run int8 default 1 not null;
