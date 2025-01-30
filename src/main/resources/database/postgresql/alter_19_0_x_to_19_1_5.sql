-- Evaluation form
alter table o_eva_form_participation add column e_run int8 default 1 not null;
alter table o_eva_form_participation add column e_last_run bool not null default true;

drop index idx_eva_part_executor_idx;
create index idx_eva_part_executor_idx on o_eva_form_participation (fk_executor, fk_survey) where fk_executor is not null;
