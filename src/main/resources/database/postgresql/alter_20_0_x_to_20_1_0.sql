-- Quality management
alter table o_eva_form_survey add column e_public_part_identifier varchar(128) null;
alter table o_eva_form_participation add column e_email varchar(128) null;
alter table o_eva_form_participation add column e_first_name varchar(128) null;
alter table o_eva_form_participation add column e_last_name varchar(128) null;

create unique index idx_eva_surv_ppident_idx on o_eva_form_survey (lower(e_public_part_identifier)) where e_public_part_identifier is not null;
