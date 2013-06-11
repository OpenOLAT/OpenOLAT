-- instant messaging
create table if not exists o_im_message (
   id bigint not null,
   creationdate datetime,
   msg_resname varchar(50) not null,
   msg_resid bigint not null,
   msg_anonym bit default 0,
   msg_from varchar(255) not null,
   msg_body longtext,
   fk_from_identity_id bigint not null,
   primary key (id)
);
alter table o_im_message ENGINE = InnoDB;
alter table o_im_message add constraint idx_im_msg_to_fromid foreign key (fk_from_identity_id) references o_bs_identity (id);
create index idx_im_msg_res_idx on o_im_message (msg_resid,msg_resname);

create table if not exists o_im_notification (
   id bigint not null,
   creationdate datetime,
   chat_resname varchar(50) not null,
   chat_resid bigint not null,
   fk_to_identity_id bigint not null,
   fk_from_identity_id bigint not null,
   primary key (id)
);
alter table o_im_notification ENGINE = InnoDB;
alter table o_im_notification add constraint idx_im_not_to_toid foreign key (fk_to_identity_id) references o_bs_identity (id);
alter table o_im_notification add constraint idx_im_not_to_fromid foreign key (fk_from_identity_id) references o_bs_identity (id);
create index idx_im_chat_res_idx on o_im_notification (chat_resid,chat_resname);

create table if not exists o_im_roster_entry (
   id bigint not null,
   creationdate datetime,
   r_resname varchar(50) not null,
   r_resid bigint not null,
   r_nickname varchar(255),
   r_fullname varchar(255),
   r_anonym bit default 0,
   r_vip bit default 0,
   fk_identity_id bigint not null,
   primary key (id)
);
alter table o_im_roster_entry ENGINE = InnoDB;
alter table o_im_roster_entry add constraint idx_im_rost_to_id foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_im_rost_res_idx on o_im_roster_entry (r_resid,r_resname);

create table if not exists o_im_preferences (
   id bigint not null,
   creationdate datetime,
   visible_to_others bit default 0,
   roster_def_status varchar(12),
   fk_from_identity_id bigint not null,
   primary key (id)
);
alter table o_im_preferences ENGINE = InnoDB;
alter table o_im_preferences add constraint idx_im_prfs_to_id foreign key (fk_from_identity_id) references o_bs_identity (id);


create or replace view o_im_roster_entry_v as (
   select
      entry.id as re_id,
      entry.creationdate as re_creationdate,
      ident.id as ident_id,
      ident.name as ident_name,
      entry.r_nickname as re_nickname,
      entry.r_fullname as re_fullname,
      entry.r_anonym as re_anonym,
      entry.r_vip as re_vip,
      entry.r_resname as re_resname,
      entry.r_resid as re_resid
   from o_im_roster_entry as entry
   inner join o_bs_identity as ident on (entry.fk_identity_id = ident.id)
);

-- views for contacts
create or replace view o_gp_visible_participant_v as (
   select
      bg_part_member.id as membership_id,
      bgroup.group_id as bg_id,
      bgroup.groupname as bg_name,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_part_member.identity_id as bg_part_member_id,
      ident.name as bg_part_member_name 
   from o_gp_business as bgroup
   inner join o_property as bconfig on (bconfig.grp = bgroup.group_id and bconfig.name = 'displayMembers' and bconfig.category = 'config' and bconfig.longValue in (2,3,6,7))
   inner join o_bs_membership as bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   inner join o_bs_identity as ident on (bg_part_member.identity_id = ident.id)
 );
   
create or replace view o_gp_visible_owner_v as ( 
   select
      bg_owner_member.id as membership_id,
      bgroup.group_id as bg_id,
      bgroup.groupname as bg_name,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_owner_member.identity_id as bg_owner_member_id,
      ident.name as bg_owner_member_name
   from o_gp_business as bgroup
   inner join o_property as bconfig on (bconfig.grp = bgroup.group_id and bconfig.name = 'displayMembers' and bconfig.category = 'config' and bconfig.longValue in (1,3,5,7))
   inner join o_bs_membership as bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
   inner join o_bs_identity as ident on (bg_owner_member.identity_id = ident.id)
);

-- coaching
create or replace view o_as_eff_statement_groups_v as (
   select
      sg_re.repositoryentry_id as re_id,
      sg_re.displayname as re_name,
      sg_bg.group_id as bg_id,
      sg_bg.groupname as bg_name,
      sg_tutorMembership.identity_id as tutor_id,
      sg_studentMembership.identity_id as student_id,
      sg_statement.id as st_id,
      (case when sg_statement.passed = 1 then 1 else 0 end) as st_passed,
      (case when sg_statement.passed = 0 then 1 else 0 end) as st_failed,
      (case when sg_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sg_statement.score as st_score,
      pg_initial_launch.id as pg_id
   from o_repositoryentry as sg_re
   inner join o_olatresource as sg_reResource on (sg_re.fk_olatresource = sg_reResource.resource_id and sg_reResource.resname = 'CourseModule')
   inner join o_gp_business_to_resource as sg_bg2resource on (sg_bg2resource.fk_resource = sg_reResource.resource_id)
   inner join o_gp_business as sg_bg on (sg_bg.group_id = sg_bg2resource.fk_group)
   inner join o_bs_membership as sg_tutorMembership on (sg_bg.fk_ownergroup = sg_tutorMembership.secgroup_id)
   inner join o_bs_membership as sg_studentMembership on (sg_bg.fk_partipiciantgroup = sg_studentMembership.secgroup_id)
   left join o_as_eff_statement as sg_statement on (sg_statement.fk_resource_id = sg_reResource.resource_id and sg_statement.fk_identity = sg_studentMembership.identity_id)
   left join o_as_user_course_infos as pg_initial_launch on (pg_initial_launch.fk_resource_id = sg_reResource.resource_id and pg_initial_launch.fk_identity = sg_studentMembership.identity_id)
   group by sg_re.repositoryentry_id, sg_re.displayname, sg_bg.group_id, sg_bg.groupname, sg_tutorMembership.identity_id, 
      sg_studentMembership.identity_id, sg_statement.id, sg_statement.score, pg_initial_launch.id
);

create or replace view o_as_eff_statement_grouped_v as (
   select
      sg_re.repositoryentry_id as re_id,
      sg_re.displayname as re_name,
      sg_tutorMembership.identity_id as tutor_id,
      sg_studentMembership.identity_id as student_id,
      sg_statement.id as st_id,
      (case when sg_statement.passed = 1 then 1 else 0 end) as st_passed,
      (case when sg_statement.passed = 0 then 1 else 0 end) as st_failed,
      (case when sg_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sg_statement.score as st_score,
      pg_initial_launch.id as pg_id
   from o_repositoryentry as sg_re
   inner join o_olatresource as sg_reResource on (sg_re.fk_olatresource = sg_reResource.resource_id and sg_reResource.resname = 'CourseModule')
   inner join o_gp_business_to_resource as sg_bg2resource on (sg_bg2resource.fk_resource = sg_reResource.resource_id)
   inner join o_gp_business as sg_bg on (sg_bg.group_id = sg_bg2resource.fk_group)
   inner join o_bs_membership as sg_tutorMembership on (sg_bg.fk_ownergroup = sg_tutorMembership.secgroup_id)
   inner join o_bs_membership as sg_studentMembership on (sg_bg.fk_partipiciantgroup = sg_studentMembership.secgroup_id)
   left join o_as_eff_statement as sg_statement on (sg_statement.fk_resource_id = sg_reResource.resource_id and sg_statement.fk_identity = sg_studentMembership.identity_id)
   left join o_as_user_course_infos as pg_initial_launch on (pg_initial_launch.fk_resource_id = sg_reResource.resource_id and pg_initial_launch.fk_identity = sg_studentMembership.identity_id)
   group by sg_re.repositoryentry_id, sg_re.displayname, sg_tutorMembership.identity_id, 
      sg_studentMembership.identity_id, sg_statement.id, sg_statement.score, pg_initial_launch.id
);

create or replace view o_as_eff_statement_members_v as (
   select
      sm_re.repositoryentry_id as re_id,
      sm_re.displayname as re_name,
      sm_tutorMembership.identity_id as tutor_id,
      sm_studentMembership.identity_id as student_id,
      sm_statement.id as st_id,
      (case when sm_statement.passed = 1 then 1 else 0 end) as st_passed,
      (case when sm_statement.passed = 0 then 1 else 0 end) as st_failed,
      (case when sm_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sm_statement.score as st_score,
      pm_initial_launch.id as pg_id
   from o_repositoryentry as sm_re
   inner join o_olatresource as sm_reResource on (sm_re.fk_olatresource = sm_reResource.resource_id and sm_reResource.resname = 'CourseModule')
   inner join o_bs_membership as sm_tutorMembership on (sm_re.fk_tutorgroup = sm_tutorMembership.secgroup_id)
   inner join o_bs_membership as sm_studentMembership on (sm_re.fk_participantgroup = sm_studentMembership.secgroup_id)
   left join o_as_eff_statement as sm_statement on (sm_statement.fk_resource_id = sm_reResource.resource_id and sm_statement.fk_identity = sm_studentMembership.identity_id)
   left join o_as_user_course_infos as pm_initial_launch on (pm_initial_launch.fk_resource_id = sm_reResource.resource_id and pm_initial_launch.fk_identity = sm_studentMembership.identity_id)
);

create or replace view o_as_eff_statement_members_strict_v as (
   select
      sm_re.repositoryentry_id as re_id,
      sm_re.displayname as re_name,
      sm_tutorMembership.identity_id as tutor_id,
      sm_studentMembership.identity_id as student_id,
      sm_statement.id as st_id,
      (case when sm_statement.passed = 1 then 1 else 0 end) as st_passed,
      (case when sm_statement.passed = 0 then 1 else 0 end) as st_failed,
      (case when sm_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sm_statement.score as st_score,
      pm_initial_launch.id as pg_id
   from o_repositoryentry as sm_re
   inner join o_olatresource as sm_reResource on (
      sm_re.fk_olatresource = sm_reResource.resource_id
      and sm_reResource.resname = 'CourseModule'
      and not exists (select * from o_gp_business_to_resource as sm_re2group where sm_reResource.resource_id = sm_re2group.fk_resource)
   )
   inner join o_bs_membership as sm_tutorMembership on (sm_re.fk_tutorgroup = sm_tutorMembership.secgroup_id)
   inner join o_bs_membership as sm_studentMembership on (sm_re.fk_participantgroup = sm_studentMembership.secgroup_id)
   left join o_as_eff_statement as sm_statement on (sm_statement.fk_resource_id = sm_reResource.resource_id and sm_statement.fk_identity = sm_studentMembership.identity_id)
   left join o_as_user_course_infos as pm_initial_launch on (pm_initial_launch.fk_resource_id = sm_reResource.resource_id and pm_initial_launch.fk_identity = sm_studentMembership.identity_id)
);

drop view o_re_member_v;
drop view o_re_strict_participant_v;
drop view o_re_strict_tutor_v;





