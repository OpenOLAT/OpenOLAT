-- quality management
alter table o_eva_form_survey change fk_previous fk_series_previous bigint(20);
alter table o_eva_form_survey add e_series_key bigint;
alter table o_eva_form_survey add e_series_index int;


-- curriculum
alter table o_cur_element_type add c_calendars varchar(16);
alter table o_cur_curriculum_element add c_calendars varchar(16);

