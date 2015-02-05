create table o_as_mode_course (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   a_name varchar2(255 char),
   a_description clob,
   a_status varchar2(16 char),
   a_manual_beginend number default 0 not null,
   a_begin date not null,
   a_leadtime number(20) default 0 not null,
   a_begin_with_leadtime date not null,
   a_end date not null,
   a_followuptime number(20) default 0 not null,
   a_end_with_followuptime date not null,
   a_targetaudience varchar2(16 char),
   a_restrictaccesselements number default 0 not null,
   a_elements varchar2(2048 char),
   a_start_element varchar2(64 char),
   a_restrictaccessips number default 0 not null,
   a_ips varchar2(2048 char),
   a_safeexambrowser number default 0 not null,
   a_safeexambrowserkey varchar2(2048 char),
   a_safeexambrowserhint clob,
   a_applysettingscoach number default 0 not null,
   fk_entry number(20) not null,
   primary key (id)
);

create table o_as_mode_course_to_group (
   id number(20) not null,
   fk_assessment_mode_id number(20) not null,
   fk_group_id number(20) not null,
   primary key (id)
);

create table o_as_mode_course_to_area (
   id number(20) not null,
   fk_assessment_mode_id number(20) not null,
   fk_area_id number(20) not null,
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


alter table o_repositoryentry add allowtoleave varchar2(16 char);


-- refactoring coaching
drop view o_as_eff_statement_identity_v;
drop view o_as_eff_statement_students_v;
drop view o_as_eff_statement_courses_v;
drop view o_as_eff_statement_groups_v;