-- Curriculum
alter table o_cur_curriculum_element add c_auto_instantiation number(20) default null;
alter table o_cur_curriculum_element add c_auto_instantiation_unit varchar(16) default null;
alter table o_cur_curriculum_element add c_auto_access_coach number(20) default null;
alter table o_cur_curriculum_element add c_auto_access_coach_unit varchar(16) default null;
alter table o_cur_curriculum_element add c_auto_published number(20) default null;
alter table o_cur_curriculum_element add c_auto_published_unit varchar(16) default null;
alter table o_cur_curriculum_element add c_auto_closed number(20) default null;
alter table o_cur_curriculum_element add c_auto_closed_unit varchar(16) default null;
