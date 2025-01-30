-- Evaluation form
alter table o_eva_form_participation add e_run number(20) default 1 not null;
alter table o_eva_form_participation add e_last_run number default 1 not null;
