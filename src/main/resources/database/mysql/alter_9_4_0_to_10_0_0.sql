alter table o_gp_business add column fk_group_id bigint;
alter table o_gp_business modify businessgrouptype varchar(15) null default null;

alter table o_repositoryentry modify softkey varchar(36) not null unique;
alter table o_repositoryentry modify launchcounter bigint null default 0;
alter table o_repositoryentry modify downloadcounter bigint null default 0;

alter table o_ep_struct_el add column fk_group_id bigint;


-- repository entry statistics table
create table o_repositoryentry_stats (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   r_rating decimal(65,30),
   r_num_of_ratings bigint not null default 0,
   r_num_of_comments bigint not null default 0,
   r_launchcounter bigint not null default 0,
   r_downloadcounter bigint not null default 0,
   r_lastusage datetime not null,
   primary key (id)
);

alter table o_repositoryentry add column fk_stats bigint;
alter table o_repositoryentry add column authors varchar(2048);
alter table o_repositoryentry add column mainlanguage varchar(255);
alter table o_repositoryentry add column objectives varchar(2048);
alter table o_repositoryentry add column requirements varchar(2048);
alter table o_repositoryentry add column credits varchar(2048);
alter table o_repositoryentry add column expenditureofwork varchar(255);

insert into o_repositoryentry_stats (id, creationdate, lastmodified, r_rating, r_launchcounter, r_downloadcounter, r_lastusage)
  select re.repositoryentry_id, now(), now(), null, re.launchcounter, re.downloadcounter, re.lastusage from o_repositoryentry as re where re.fk_stats is null;
update o_repositoryentry set fk_stats=repositoryentry_id where fk_stats is null;

alter table o_repositoryentry modify fk_stats bigint not null;
alter table o_repositoryentry add constraint repoentry_stats_ctx foreign key (fk_stats) references o_repositoryentry_stats (id);

-- base group
create table o_bs_group (
   id bigint not null,
   creationdate datetime not null,
   g_name varchar(36),
   primary key (id)
);

create table o_bs_group_member (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_role varchar(50) not null,
   fk_group_id bigint not null,
   fk_identity_id bigint not null,
   primary key (id)
);

create table o_re_to_group (
   id bigint not null,
   creationdate datetime not null,
   r_defgroup boolean not null,
   fk_group_id bigint not null,
   fk_entry_id bigint not null,
   primary key (id)
);

create table o_bs_grant (
   id bigint not null,
   creationdate datetime not null,
   g_role varchar(32) not null,
   g_permission varchar(32) not null,
   fk_group_id bigint not null,
   fk_resource_id bigint not null,
   primary key (id)
);

alter table o_bs_group_member add constraint member_identity_ctx foreign key (fk_identity_id) references o_bs_identity (id);
alter table o_bs_group_member add constraint member_group_ctx foreign key (fk_group_id) references o_bs_group (id);
create index member_to_grp_role_idx on o_bs_group_member (g_role);

alter table o_re_to_group add constraint re_to_group_group_ctx foreign key (fk_group_id) references o_bs_group (id);
alter table o_re_to_group add constraint re_to_group_re_ctx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);

alter table o_gp_business add constraint gp_to_group_business_ctx foreign key (fk_group_id) references o_bs_group (id);


-- managed groups
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
      gp.external_id as external_id,
      gp.managed_flags as managed_flags,
      (select count(part.id) from o_bs_group_member as part where part.fk_group_id = gp.fk_group_id and part.g_role='participant') as num_of_participants,
      (select count(pending.reservation_id) from o_ac_reservation as pending where pending.fk_resource = gp.fk_resource) as num_of_pendings,
      (select count(own.id) from o_bs_group_member as own where own.fk_group_id = gp.fk_group_id and own.g_role='coach') as num_of_owners,
      (case when gp.waitinglist_enabled = true
         then 
           (select count(waiting.id) from o_bs_group_member as waiting where waiting.fk_group_id = gp.fk_group_id and waiting.g_role='waiting')
         else
           0
      end) as num_waiting,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=true
         and (offer.validfrom is null or offer.validfrom <= current_timestamp())
         and (offer.validto is null or offer.validto >= current_timestamp())
      ) as num_of_valid_offers,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=true
      ) as num_of_offers,
      (select count(relation.fk_entry_id) from o_re_to_group as relation 
         where relation.fk_group_id = gp.fk_group_id
      ) as num_of_relations,
      gp.fk_resource as fk_resource,
      gp.fk_group_id as fk_group_id
   from o_gp_business as gp
);

create or replace view o_bs_gp_membership_v as (
   select
      gp.group_id as group_id,
      membership.id as membership_id,
      membership.fk_identity_id as fk_identity_id,
      membership.lastmodified as lastmodified,
      membership.creationdate as creationdate,
      membership.g_role as g_role
   from o_bs_group_member as membership
   inner join o_gp_business as gp on (gp.fk_group_id=membership.fk_group_id)
);

create or replace view o_gp_member_v as (
   select
      gp.group_id as bg_id,
      gp.groupname as bg_name,
      gp.creationdate as bg_creationdate,
      gp.managed_flags as bg_managed_flags,
      gp.descr as bg_desc,
      membership.fk_identity_id as member_id
   from o_gp_business as gp
   inner join o_bs_group_member as membership on (membership.fk_group_id = gp.fk_group_id and membership.g_role in ('coach','participant'))
);

create or replace view o_gp_business_to_repository_v as (
	select 
		grp.group_id as grp_id,
		repoentry.repositoryentry_id as re_id,
		repoentry.displayname as re_displayname
	from o_gp_business as grp
	inner join o_re_to_group as relation on (relation.fk_group_id = grp.fk_group_id)
	inner join o_repositoryentry as repoentry on (repoentry.repositoryentry_id = relation.fk_entry_id)
);

-- contacts
create view o_gp_contactkey_v as (
   select
      bg_member.id as membership_id,
      bg_member.fk_identity_id as member_id,
      bg_member.g_role as membership_role,
      bg_me.fk_identity_id as me_id,
      bgroup.group_id as bg_id
   from o_gp_business as bgroup
   inner join o_bs_group_member as bg_member on (bg_member.fk_group_id = bgroup.fk_group_id)
   inner join o_bs_group_member as bg_me on (bg_me.fk_group_id = bgroup.fk_group_id)
   where
      (bgroup.ownersintern=true and bg_member.g_role='coach')
      or
      (bgroup.participantsintern=true and bg_member.g_role='participant')
);

create view o_gp_contactext_v as (
   select
      bg_member.id as membership_id,
      bg_member.fk_identity_id as member_id,
      bg_member.g_role as membership_role,
      id_member.name as member_name,
      first_member.propvalue as member_firstname,
      last_member.propvalue as member_lastname,
      bg_me.fk_identity_id as me_id,
      bgroup.group_id as bg_id,
      bgroup.group_id as bg_name
   from o_gp_business as bgroup
   inner join o_bs_group_member as bg_member on (bg_member.fk_group_id = bgroup.fk_group_id)
   inner join o_bs_identity as id_member on (bg_member.fk_identity_id = id_member.id)
   inner join o_user as us_member on (id_member.fk_user_id = us_member.user_id)
   inner join o_userproperty as first_member on (first_member.fk_user_id = us_member.user_id and first_member.propname='firstName')
   inner join o_userproperty as last_member on (last_member.fk_user_id = us_member.user_id and last_member.propname='lastName')
   inner join o_bs_group_member as bg_me on (bg_me.fk_group_id = bgroup.fk_group_id)
   where
      (bgroup.ownersintern=true and bg_member.g_role='coach')
      or
      (bgroup.participantsintern=true and bg_member.g_role='participant')
);

create or replace view o_re_membership_v as (
   select
      bmember.id as membership_id,
      bmember.creationdate as creationdate,
      bmember.lastmodified as lastmodified,
      bmember.fk_identity_id as fk_identity_id,
      bmember.g_role as g_role,
      re.repositoryentry_id as fk_entry_id
   from o_repositoryentry as re
   inner join o_re_to_group relgroup on (relgroup.fk_entry_id=re.repositoryentry_id)
   inner join o_bs_group_member as bmember on (bmember.fk_group_id=relgroup.fk_group_id) 
);

-- coaching
create or replace view o_as_eff_statement_students_v as (
   select
      sg_re.repositoryentry_id as re_id,
      sg_coach.fk_identity_id as tutor_id,
      sg_participant.fk_identity_id as student_id,
      sg_statement.id as st_id,
      (case when sg_statement.passed = true then 1 else 0 end) as st_passed,
      (case when sg_statement.passed = false then 1 else 0 end) as st_failed,
      (case when sg_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sg_statement.score as st_score,
      pg_initial_launch.id as pg_id
   from o_repositoryentry as sg_re
   inner join o_re_to_group as togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id)
   inner join o_bs_group_member as sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role='coach')
   inner join o_bs_group_member as sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant')
   left join o_as_eff_statement as sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource)
   left join o_as_user_course_infos as pg_initial_launch on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id)
);

create or replace view o_as_eff_statement_courses_v as (
   select
      sg_re.repositoryentry_id as re_id,
      sg_re.displayname as re_name,
      sg_coach.fk_identity_id as tutor_id,
      sg_participant.fk_identity_id as student_id,
      sg_statement.id as st_id,
      (case when sg_statement.passed = true then 1 else 0 end) as st_passed,
      (case when sg_statement.passed = false then 1 else 0 end) as st_failed,
      (case when sg_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sg_statement.score as st_score,
      pg_initial_launch.id as pg_id
   from o_repositoryentry as sg_re
   inner join o_re_to_group as togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id)
   inner join o_bs_group_member as sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role='coach')
   inner join o_bs_group_member as sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant')
   left join o_as_eff_statement as sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource)
   left join o_as_user_course_infos as pg_initial_launch on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id)
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
      (case when sg_statement.passed = true then 1 else 0 end) as st_passed,
      (case when sg_statement.passed = false then 1 else 0 end) as st_failed,
      (case when sg_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sg_statement.score as st_score,
      pg_initial_launch.id as pg_id
   from o_repositoryentry as sg_re
   inner join o_re_to_group as togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id)
   inner join o_gp_business as sg_bg on (sg_bg.fk_group_id=togroup.fk_group_id)
   inner join o_bs_group_member as sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role='coach')
   inner join o_bs_group_member as sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant')
   left join o_as_eff_statement as sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource)
   left join o_as_user_course_infos as pg_initial_launch on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id)
);

-- new views
create or replace view o_repositoryentry_my_v as (
   select
      re.repositoryentry_id as re_id,
      re.creationdate as re_creationdate,
      re.lastmodified as re_lastmodified,
      re.displayname as re_displayname,
      re.description as re_description,
      re.authors as re_authors,
      re.accesscode as re_accesscode,
      re.membersonly as re_membersonly,
      re.statuscode as re_statuscode,
      re.fk_lifecycle as fk_lifecycle,
      re.fk_olatresource as fk_olatresource,
      courseinfos.initiallaunchdate as ci_initiallaunchdate,
      courseinfos.recentlaunchdate as ci_recentlaunchdate,
      courseinfos.visit as ci_visit,
      courseinfos.timespend as ci_timespend,
      effstatement.score as eff_score,
      effstatement.passed as eff_passed,
      mark.mark_id as mark_id,
      rating.rating as rat_rating,
      stats.r_rating as stats_rating,
      stats.r_num_of_ratings as stats_num_of_ratings,
      stats.r_num_of_comments as stats_num_of_comments,
      ident.id as member_id,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = re.fk_olatresource
         and offer.is_valid=true
         and (offer.validfrom is null or offer.validfrom <= current_timestamp())
         and (offer.validto is null or offer.validto >= current_timestamp())
      ) as num_of_valid_offers,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = re.fk_olatresource
         and offer.is_valid=true
      ) as num_of_offers
   from o_repositoryentry as re
   cross join o_bs_identity as ident
   inner join o_repositoryentry_stats as stats on (re.fk_stats=stats.id)
   left join o_mark as mark on (mark.creator_id=ident.id and re.repositoryentry_id=mark.resid and mark.resname='RepositoryEntry')
   left join o_as_eff_statement as effstatement on (effstatement.fk_identity=ident.id and effstatement.fk_resource_id = re.fk_olatresource)
   left join o_userrating as rating on (rating.creator_id=ident.id and re.repositoryentry_id=rating.resid and rating.resname='RepositoryEntry')
   left join o_as_user_course_infos as courseinfos on (courseinfos.fk_identity=ident.id and re.fk_olatresource=courseinfos.fk_resource_id)
);

create or replace view o_repositoryentry_author_v as (
   select
      re.repositoryentry_id as re_id,
      re.creationdate as re_creationdate,
      re.lastmodified as re_lastmodified,
      re.displayname as re_displayname,
      re.description as re_description,
      re.softkey as re_softkey,
      re.external_id as re_external_id,
      re.external_ref as re_external_ref,
      re.initialauthor as re_author,
      re.authors as re_authors,
      re.accesscode as re_accesscode,
      re.membersonly as re_membersonly,
      re.statuscode as re_statuscode,
      re.fk_lifecycle as fk_lifecycle,
      re.fk_olatresource as fk_olatresource,
      stats.r_lastusage as re_lastusage,
      mark.mark_id as mark_id,
      ident.id as member_id,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = re.fk_olatresource
         and offer.is_valid=true
         and (offer.validfrom is null or offer.validfrom <= current_timestamp)
         and (offer.validto is null or offer.validto >= current_timestamp)
      ) as num_of_valid_offers,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = re.fk_olatresource
         and offer.is_valid=true
      ) as num_of_offers
   from o_repositoryentry as re
   cross join o_bs_identity as ident
   inner join o_repositoryentry_stats as stats on (re.fk_stats=stats.id)
   left join o_mark as mark on (mark.creator_id=ident.id and re.repositoryentry_id=mark.resid and mark.resname='RepositoryEntry')
);

-- drop views
drop view o_gp_visible_participant_v;
drop view o_gp_contact_participant_v;
drop view o_gp_visible_owner_v;
drop view o_gp_contact_owner_v;
drop view o_gp_contactkey_participant_v; 
drop view o_gp_contactkey_owner_v;

drop view o_re_tutor_v;
drop view o_re_participant_v;

drop view o_gp_contextresource_2_group_v;

drop view o_as_eff_statement_grouped_v;
drop view o_as_eff_statement_members_v;
drop view o_as_eff_statement_members_strict_v;

-- drop constraints and index
alter table o_gp_business drop foreign key FKCEEB8A86DF6BCD14;
alter table o_gp_business drop foreign key FKCEEB8A86A1FAC766;
alter table o_gp_business drop foreign key FKCEEB8A86C06E3EF3;
alter table o_gp_business drop foreign key idx_bgp_rsrc;
alter table o_gp_business drop foreign key idx_bgp_waiting;

alter table o_repositoryentry drop foreign key FK2F9C439888C31018;
alter table o_repositoryentry drop foreign key FK2F9C4398A1FAC766;
alter table o_repositoryentry drop foreign key repo_tutor_sec_group_ctx;
alter table o_repositoryentry drop foreign key repo_parti_sec_group_ctx;

alter table o_repositorymetadata drop foreign key FKDB97A6493F14E3EE;

alter table o_bookmark drop foreign key FK68C4E30663219E27;

alter table o_gp_business_to_resource drop foreign key idx_bgp_to_rsrc_rsrc;
alter table o_gp_business_to_resource drop foreign key idx_bgp_to_rsrc_group;

alter table o_gp_bgcontext drop foreign key FK1C154FC47E4A0638;
alter table o_gp_bgcontextresource_rel drop foreign key FK9903BEAC9F9C3F1D;
alter table o_gp_bgcontextresource_rel drop foreign key FK9903BEACDF6BCD14;

alter table o_gp_bgarea drop foreign key FK9EFAF698DF6BCD14;

alter table o_ep_struct_el drop foreign key FKF26C8375236F29X;