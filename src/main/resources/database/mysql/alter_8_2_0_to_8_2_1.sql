alter table o_ac_reservation add column expirationdate datetime;
alter table o_ac_reservation add column reservationtype varchar(32);

alter table o_ac_reservation add constraint idx_rsrv_to_rsrc_rsrc foreign key (fk_resource) references o_olatresource (resource_id);
alter table o_ac_reservation add constraint idx_rsrv_to_rsrc_identity foreign key (fk_identity) references o_bs_identity (id);


create or replace view o_gp_business_v  as (
   select
      gp.group_id as group_id,
      gp.groupname as groupname,
      gp.lastmodified as lastmodified,
      gp.creationdate as creationdate,
      gp.lastusage as lastusage,
      gp.descr as descr,
      gp.minparticipants as minparticipants,
      gp.maxparticipants as maxparticipants,
      gp.waitinglist_enabled as waitinglist_enabled,
      gp.autocloseranks_enabled as autocloseranks_enabled,
      (select count(part.id) from o_bs_membership as part where part.secgroup_id = gp.fk_partipiciantgroup) as num_of_participants,
      (select count(pending.reservation_id) from o_ac_reservation as pending where pending.fk_resource = gp.fk_resource) as num_of_pendings,
      (select count(own.id) from o_bs_membership as own where own.secgroup_id = gp.fk_ownergroup) as num_of_owners,
      (case when gp.waitinglist_enabled = 1
         then 
           (select count(waiting.id) from o_bs_membership as waiting where waiting.secgroup_id = gp.fk_partipiciantgroup)
         else
           0
      end) as num_waiting,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=1
         and (offer.validfrom is null or offer.validfrom <= current_timestamp())
         and (offer.validto is null or offer.validto >= current_timestamp())
      ) as num_of_valid_offers,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=1
      ) as num_of_offers,
      (select count(relation.fk_resource) from o_gp_business_to_resource as relation 
         where relation.fk_group = gp.group_id
      ) as num_of_relations,
      gp.fk_resource as fk_resource,
      gp.fk_ownergroup as fk_ownergroup,
      gp.fk_partipiciantgroup as fk_partipiciantgroup,
      gp.fk_waitinggroup as fk_waitinggroup
   from o_gp_business as gp
);


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