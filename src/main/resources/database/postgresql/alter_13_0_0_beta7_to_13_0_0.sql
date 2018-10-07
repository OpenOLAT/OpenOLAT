-- quality management
alter table o_eva_form_survey rename column fk_previous to fk_series_previous;
alter table o_eva_form_survey add e_series_key bigint;
alter table o_eva_form_survey add e_series_index int;
