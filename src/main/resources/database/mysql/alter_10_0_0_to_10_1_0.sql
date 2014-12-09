create table o_cer_template (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   c_name varchar(256) not null,
   c_path varchar(1024) not null,
   c_public boolean not null,
   c_format varchar(16),
   c_orientation varchar(16),
   primary key (id)
);
alter table o_cer_template ENGINE = InnoDB;

create table o_cer_certificate (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   c_status varchar(16) not null default 'pending',
   c_email_status varchar(16),
   c_uuid varchar(36) not null,
   c_path varchar(1024),
   c_last boolean not null default 1,
   c_course_title varchar(255),
   c_archived_resource_id bigint not null,
   fk_olatresource bigint,
   fk_identity bigint not null,
   primary key (id)
);
alter table o_cer_certificate ENGINE = InnoDB;

alter table o_cer_certificate add constraint cer_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_cer_certificate add constraint cer_to_resource_idx foreign key (fk_olatresource) references o_olatresource (resource_id);
create index cer_archived_resource_idx on o_cer_certificate (c_archived_resource_id);
create index cer_uuid_idx on o_cer_certificate (c_uuid);


alter table o_gp_business add column allowtoleave boolean default 1;


alter table o_bs_identity add column external_id varchar(64);


alter table o_catentry add column style varchar(16);


-- coaching
create or replace view o_as_eff_statement_identity_v as (
   select
      sg_re.repositoryentry_id as re_id,
      sg_participant.fk_identity_id as student_id,
      sg_statement.id as st_id,
      (case when sg_statement.passed = 1 then 1 else 0 end) as st_passed,
      (case when sg_statement.passed = 0 then 1 else 0 end) as st_failed,
      (case when sg_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sg_statement.score as st_score,
      pg_initial_launch.id as pg_id
   from o_repositoryentry as sg_re
   inner join o_re_to_group as togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id)
   inner join o_bs_group_member as sg_participant on (sg_participant.fk_group_id=togroup.fk_group_id and sg_participant.g_role='participant')
   left join o_as_eff_statement as sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource)
   left join o_as_user_course_infos as pg_initial_launch on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id)
   group by sg_re.repositoryentry_id, sg_participant.fk_identity_id,
      sg_statement.id, sg_statement.passed, sg_statement.score, pg_initial_launch.id
);

create or replace view o_as_eff_statement_students_v as (
   select
      sg_re.repositoryentry_id as re_id,
      sg_coach.fk_identity_id as tutor_id,
      sg_participant.fk_identity_id as student_id,
      sg_statement.id as st_id,
      (case when sg_statement.passed = 1 then 1 else 0 end) as st_passed,
      (case when sg_statement.passed = 0 then 1 else 0 end) as st_failed,
      (case when sg_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sg_statement.score as st_score,
      pg_initial_launch.id as pg_id
   from o_repositoryentry as sg_re
   inner join o_re_to_group as togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id)
   inner join o_bs_group_member as sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role in ('owner','coach'))
   inner join o_bs_group_member as sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant')
   left join o_as_eff_statement as sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource)
   left join o_as_user_course_infos as pg_initial_launch on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id)
   group by sg_re.repositoryentry_id, sg_coach.fk_identity_id, sg_participant.fk_identity_id,
      sg_statement.id, sg_statement.passed, sg_statement.score, pg_initial_launch.id
);

create or replace view o_as_eff_statement_courses_v as (
   select
      sg_re.repositoryentry_id as re_id,
      sg_re.displayname as re_name,
      sg_coach.fk_identity_id as tutor_id,
      sg_participant.fk_identity_id as student_id,
      sg_statement.id as st_id,
      (case when sg_statement.passed = 1 then 1 else 0 end) as st_passed,
      (case when sg_statement.passed = 0 then 1 else 0 end) as st_failed,
      (case when sg_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sg_statement.score as st_score,
      pg_initial_launch.id as pg_id
   from o_repositoryentry as sg_re
   inner join o_re_to_group as togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id)
   inner join o_bs_group_member as sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role in ('owner','coach'))
   inner join o_bs_group_member as sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant')
   left join o_as_eff_statement as sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource)
   left join o_as_user_course_infos as pg_initial_launch on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id)
   group by sg_re.repositoryentry_id, sg_re.displayname, sg_coach.fk_identity_id, sg_participant.fk_identity_id,
      sg_statement.id, sg_statement.passed, sg_statement.score, pg_initial_launch.id
);

create or replace view o_as_eff_statement_groups_v as (
   select
      sg_re.repositoryentry_id as re_id,
      sg_re.displayname as re_name,
      sg_bg.group_id as bg_id,
      sg_bg.groupname as bg_name,
      sg_coach.fk_identity_id as tutor_id,
      sg_participant.fk_identity_id as student_id,
      sg_statement.id as st_id,
      (case when sg_statement.passed = 1 then 1 else 0 end) as st_passed,
      (case when sg_statement.passed = 0 then 1 else 0 end) as st_failed,
      (case when sg_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sg_statement.score as st_score,
      pg_initial_launch.id as pg_id
   from o_repositoryentry as sg_re
   inner join o_re_to_group as togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id)
   inner join o_gp_business as sg_bg on (sg_bg.fk_group_id=togroup.fk_group_id)
   inner join o_bs_group_member as sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role in ('owner','coach'))
   inner join o_bs_group_member as sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant')
   left join o_as_eff_statement as sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource)
   left join o_as_user_course_infos as pg_initial_launch on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id)
   group by sg_re.repositoryentry_id, sg_re.displayname, sg_bg.group_id, sg_bg.groupname,
      sg_coach.fk_identity_id, sg_participant.fk_identity_id,
      sg_statement.id, sg_statement.passed, sg_statement.score, pg_initial_launch.id
);


-- clean up
drop view o_qp_item_shared_v;
drop view o_qp_item_pool_v;
drop view o_qp_item_author_v;
drop view o_qp_item_v;

drop view o_gp_member_v;