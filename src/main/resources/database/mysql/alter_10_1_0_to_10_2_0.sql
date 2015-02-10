create table o_as_mode_course (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   a_name varchar(255),
   a_description longtext,
   a_status varchar(16),
   a_manual_beginend bit not null default 0,
   a_begin datetime not null,
   a_leadtime bigint not null default 0,
   a_begin_with_leadtime datetime not null,
   a_end datetime not null,
   a_followuptime bigint not null default 0,
   a_end_with_followuptime datetime not null,
   a_targetaudience varchar(16),
   a_restrictaccesselements bit not null default 0,
   a_elements varchar(2048),
   a_start_element varchar(64),
   a_restrictaccessips bit not null default 0,
   a_ips varchar(2048),
   a_safeexambrowser bit not null default 0,
   a_safeexambrowserkey varchar(2048),
   a_safeexambrowserhint longtext,
   a_applysettingscoach bit not null default 0,
   fk_entry bigint not null,
   primary key (id)
);
alter table o_as_mode_course ENGINE = InnoDB;


create table o_as_mode_course_to_group (
   id bigint not null,
   fk_assessment_mode_id bigint not null,
   fk_group_id bigint not null,
   primary key (id)
);
alter table o_as_mode_course ENGINE = InnoDB;


create table o_as_mode_course_to_area (
   id bigint not null,
   fk_assessment_mode_id bigint not null,
   fk_area_id bigint not null,
   primary key (id)
);
alter table o_as_mode_course_to_area ENGINE = InnoDB;


alter table o_as_mode_course add constraint as_mode_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);

alter table o_as_mode_course_to_group add constraint as_modetogroup_group_idx foreign key (fk_group_id) references o_gp_business (group_id);
alter table o_as_mode_course_to_group add constraint as_modetogroup_mode_idx foreign key (fk_assessment_mode_id) references o_as_mode_course (id);

alter table o_as_mode_course_to_area add constraint as_modetoarea_area_idx foreign key (fk_area_id) references o_gp_bgarea (area_id);
alter table o_as_mode_course_to_area add constraint as_modetoarea_mode_idx foreign key (fk_assessment_mode_id) references o_as_mode_course (id);


alter table o_repositoryentry add column allowtoleave varchar(16);

-- refactoring coaching (maybe test views)
drop view if exists o_as_eff_statement_members_strict_v;
drop view if exists o_as_eff_statement_grouped_v;
drop view if exists o_as_eff_statement_members_v;
drop view if exists o_as_eff_statement_identities_v;
-- real ones
drop view if exists o_as_eff_statement_identity_v;
drop view if exists o_as_eff_statement_students_v;
drop view if exists o_as_eff_statement_courses_v;
drop view if exists o_as_eff_statement_groups_v;
