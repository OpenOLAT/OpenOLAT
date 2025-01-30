-- Evaluation form
alter table o_eva_form_participation add column e_run bigint default 1 not null;
alter table o_eva_form_participation add column e_last_run boolean not null default 1;

drop index idx_eva_part_executor_idx on o_eva_form_participation;
create index idx_eva_part_executor_idx on o_eva_form_participation (fk_executor, fk_survey);
