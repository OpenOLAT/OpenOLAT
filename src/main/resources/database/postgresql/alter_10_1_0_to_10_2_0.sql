create table o_as_mode_course (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_name varchar(255),
   a_description text,
   a_status varchar(16),
   a_manual_beginend bool not null default false,
   a_begin timestamp not null,
   a_leadtime int8 not null default 0,
   a_begin_with_leadtime timestamp not null,
   a_end timestamp not null,
   a_followuptime int8 not null default 0,
   a_end_with_followuptime timestamp not null,
   a_targetaudience varchar(16),
   a_restrictaccesselements bool not null default false,
   a_elements varchar(2048),
   a_start_element varchar(64),
   a_restrictaccessips bool not null default false,
   a_ips varchar(2048),
   a_safeexambrowser bool not null default false,
   a_safeexambrowserkey varchar(2048),
   a_safeexambrowserhint text,
   a_applysettingscoach bool not null default false,
   fk_entry int8 not null,
   primary key (id)
);

create table o_as_mode_course_to_group (
   id int8 not null,
   fk_assessment_mode_id int8 not null,
   fk_group_id int8 not null,
   primary key (id)
);

create table o_as_mode_course_to_area (
   id int8 not null,
   fk_assessment_mode_id int8 not null,
   fk_area_id int8 not null,
   primary key (id)
);

alter table o_as_mode_course add constraint as_mode_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_as_mode_to_repo_entry_idx on o_as_mode_course (fk_entry);

alter table o_as_mode_course_to_group add constraint as_modetogroup_group_idx foreign key (fk_group_id) references o_gp_business (group_id);
alter table o_as_mode_course_to_group add constraint as_modetogroup_mode_idx foreign key (fk_assessment_mode_id) references o_as_mode_course (id);
create index idx_as_modetogroup_group_idx on o_as_mode_course_to_group (fk_group_id);
create index idx_as_modetogroup_mode_idx on o_as_mode_course_to_group (fk_assessment_mode_id);

alter table o_as_mode_course_to_area add constraint as_modetoarea_area_idx foreign key (fk_area_id) references o_gp_bgarea (area_id);
alter table o_as_mode_course_to_area add constraint as_modetoarea_mode_idx foreign key (fk_assessment_mode_id) references o_as_mode_course (id);
create index idx_as_modetoarea_area_idx on o_as_mode_course_to_area (fk_area_id);
create index idx_as_modetoarea_mode_idx on o_as_mode_course_to_area (fk_assessment_mode_id);


alter table o_repositoryentry add column allowtoleave varchar(16);

-- refactoring coaching
drop view if exists o_as_eff_statement_identity_v;
drop view if exists o_as_eff_statement_students_v;
drop view if exists o_as_eff_statement_courses_v;
drop view if exists o_as_eff_statement_groups_v;