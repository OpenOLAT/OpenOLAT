-- Evaluation forms
alter table o_eva_form_survey add fk_previous number(20);

alter table o_eva_form_survey add constraint eva_surv_to_surv_idx foreign key (fk_previous) references o_eva_form_survey (id);
