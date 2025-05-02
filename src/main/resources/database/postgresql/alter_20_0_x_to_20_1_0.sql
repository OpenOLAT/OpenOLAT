-- Quality management
alter table o_eva_form_participation add column e_email varchar(128) null;
alter table o_eva_form_participation add column e_first_name varchar(128) null;
alter table o_eva_form_participation add column e_last_name varchar(128) null;