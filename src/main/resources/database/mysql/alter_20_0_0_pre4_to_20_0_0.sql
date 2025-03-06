-- Curriculum
alter table o_cur_curriculum_element add column c_auto_instantiation bigint default null;
alter table o_cur_curriculum_element add column c_auto_instantiation_unit varchar(16) default null;
alter table o_cur_curriculum_element add column c_auto_access_coach bigint default null;
alter table o_cur_curriculum_element add column c_auto_access_coach_unit varchar(16) default null;
alter table o_cur_curriculum_element add column c_auto_published bigint default null;
alter table o_cur_curriculum_element add column c_auto_published_unit varchar(16) default null;
alter table o_cur_curriculum_element add column c_auto_closed bigint default null;
alter table o_cur_curriculum_element add column c_auto_closed_unit varchar(16) default null;
