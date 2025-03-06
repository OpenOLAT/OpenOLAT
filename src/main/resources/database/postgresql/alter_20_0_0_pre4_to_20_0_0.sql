-- Curriculum
alter table o_cur_curriculum_element add column c_auto_instantiation int8 default null;
alter table o_cur_curriculum_element add column c_auto_instantiation_unit varchar(16) default null;
alter table o_cur_curriculum_element add column c_auto_access_coach int8 default null;
alter table o_cur_curriculum_element add column c_auto_access_coach_unit varchar(16) default null;
alter table o_cur_curriculum_element add column c_auto_published int8 default null;
alter table o_cur_curriculum_element add column c_auto_published_unit varchar(16) default null;
alter table o_cur_curriculum_element add column c_auto_closed int8 default null;
alter table o_cur_curriculum_element add column c_auto_closed_unit varchar(16) default null;
