alter table o_gp_business add (fk_group_id number default 0);
alter table o_gp_business modify (businessgrouptype null);

alter table o_repositoryentry modify (softkey varchar(36));
alter table o_repositoryentry modify (launchcounter null );
alter table o_repositoryentry modify (launchcounter default 0);
alter table o_repositoryentry modify (downloadcounter null);
alter table o_repositoryentry modify (downloadcounter default 0);

-- repository entry statistics table
create table o_repositoryentry_stats (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   r_rating float,
   r_num_of_ratings number(20) default 0 not null,
   r_num_of_comments number(20) default 0 not null,
   r_launchcounter number(20) default 0 not null,
   r_downloadcounter number(20) default 0 not null,
   r_lastusage date not null,
   PRIMARY KEY (id)
);

alter table o_repositoryentry add (fk_stats number(20));
alter table o_repositoryentry add (authors varchar2(2048 char));
alter table o_repositoryentry add (mainlanguage varchar2(255 char));
alter table o_repositoryentry add (objectives varchar2(2048 char));
alter table o_repositoryentry add (requirements varchar2(2048 char));
alter table o_repositoryentry add (credits varchar2(2048 char));
alter table o_repositoryentry add (expenditureofwork varchar2(255 char));

insert into o_repositoryentry_stats (id, creationdate, lastmodified, r_rating, r_launchcounter, r_downloadcounter, r_lastusage)
  select re.repositoryentry_id, sysdate, sysdate, null, re.launchcounter, re.downloadcounter, re.lastusage from o_repositoryentry re where re.fk_stats is null;
update o_repositoryentry set fk_stats=repositoryentry_id where fk_stats is null;

alter table o_repositoryentry modify (fk_stats not null);
alter table o_repositoryentry add constraint repoentry_stats_ctx foreign key (fk_stats) references o_repositoryentry_stats (id);
create index repoentry_stats_idx on o_repositoryentry (fk_stats);

-- base group
CREATE TABLE o_bs_group (
   id number(20) not null,
   creationdate date not null,
   g_name varchar2(36 char),
   PRIMARY KEY (id)
);

CREATE TABLE o_bs_group_member (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   g_role varchar2(50 char) not null,
   fk_group_id number(20) not null,
   fk_identity_id number(20) not null,
   PRIMARY KEY (id)
);

CREATE TABLE o_re_to_group (
   id number(20) not null,
   creationdate date not null,
   r_defgroup number default 0 not null,
   fk_group_id number(20) not null,
   fk_entry_id number(20) not null,
   PRIMARY KEY (id)
);

CREATE TABLE o_bs_grant (
   id number(20) not null,
   creationdate date not null,
   g_role varchar2(32 char) not null,
   g_permission varchar2(32 char) not null,
   fk_group_id number(20) not null,
   fk_resource_id number(20) not null,
   PRIMARY KEY (id)
);

alter table o_bs_group_member add constraint member_identity_ctx foreign key (fk_identity_id) references o_bs_identity (id);
alter table o_bs_group_member add constraint member_group_ctx foreign key (fk_group_id) references o_bs_group (id);
create index member_to_identity_idx on o_bs_group_member (fk_identity_id);
create index member_to_group_idx on o_bs_group_member (fk_group_id);
create index member_to_grp_role_idx on o_bs_group_member (g_role);

alter table o_re_to_group add constraint re_to_group_group_ctx foreign key (fk_group_id) references o_bs_group (id);
alter table o_re_to_group add constraint re_to_group_re_ctx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index re_to_group_group_idx on o_re_to_group (fk_group_id);
create index re_to_group_re_idx on o_re_to_group (fk_entry_id);

-- alter table o_gp_business add constraint gp_to_group_business_ctx foreign key (fk_group_id) references o_bs_group (id);
create index gp_to_group_group_idx on o_gp_business (fk_group_id);


-- portfolio
alter table o_bs_invitation modify (fk_secgroup null);
alter table o_bs_invitation modify (version null);

alter table o_bs_invitation add (fk_group_id number(20));
alter table o_bs_invitation add constraint inv_to_group_group_ctx foreign key (fk_group_id) references o_bs_group (id);

create table o_ep_struct_to_group (
   id number(20) not null,
   creationdate date not null,
   r_defgroup number default 0 not null,
   r_role varchar2(64 char),
   r_valid_from date,
   r_valid_to date,
   fk_group_id number(20),
   fk_struct_id number(20),
   PRIMARY KEY (id)
);
alter table o_ep_struct_to_group add constraint struct_to_group_group_ctx foreign key (fk_group_id) references o_bs_group (id);
alter table o_ep_struct_to_group add constraint struct_to_group_re_ctx foreign key (fk_struct_id) references o_ep_struct_el (structure_id);


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
      (select count(part.id) from o_bs_group_member part where part.fk_group_id = gp.fk_group_id and part.g_role='participant') as num_of_participants,
      (select count(pending.reservation_id) from o_ac_reservation pending where pending.fk_resource = gp.fk_resource) as num_of_pendings,
      (select count(own.id) from o_bs_group_member own where own.fk_group_id = gp.fk_group_id and own.g_role='coach') as num_of_owners,
      (case when gp.waitinglist_enabled > 0
         then 
           (select count(waiting.id) from o_bs_group_member waiting where waiting.fk_group_id = gp.fk_group_id and waiting.g_role='waiting')
         else
           0
      end) as num_waiting,
      (select count(offer.offer_id) from o_ac_offer offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid > 0
         and (offer.validfrom is null or offer.validfrom <= current_date)
         and (offer.validto is null or offer.validto >= current_date)
      ) as num_of_valid_offers,
      (select count(offer.offer_id) from o_ac_offer offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid > 0
      ) as num_of_offers,
      (select count(relation.fk_entry_id) from o_re_to_group relation 
         where relation.fk_group_id = gp.fk_group_id
      ) as num_of_relations,
      gp.fk_resource as fk_resource,
      gp.fk_group_id as fk_group_id
   from o_gp_business gp
);

create or replace view o_bs_gp_membership_v as (
   select
      gp.group_id as group_id,
      membership.id as membership_id,
      membership.fk_identity_id as fk_identity_id,
      membership.lastmodified as lastmodified,
      membership.creationdate as creationdate,
      membership.g_role as g_role
   from o_bs_group_member membership
   inner join o_gp_business gp on (gp.fk_group_id=membership.fk_group_id)
);

create or replace view o_gp_member_v as (
   select
      gp.group_id as bg_id,
      gp.groupname as bg_name,
      gp.creationdate as bg_creationdate,
      gp.managed_flags as bg_managed_flags,
      gp.descr as bg_desc,
      membership.fk_identity_id as member_id
   from o_gp_business gp
   inner join o_bs_group_member membership on (membership.fk_group_id = gp.fk_group_id and membership.g_role in ('coach','participant'))
);

create or replace view o_gp_business_to_repository_v as (
	select 
		grp.group_id as grp_id,
		repoentry.repositoryentry_id as re_id,
		repoentry.displayname as re_displayname
	from o_gp_business grp
	inner join o_re_to_group relation on (relation.fk_group_id = grp.fk_group_id)
	inner join o_repositoryentry repoentry on (repoentry.repositoryentry_id = relation.fk_entry_id)
);

-- contacts
create view o_gp_contactkey_v as (
   select
      bg_member.id as membership_id,
      bg_member.fk_identity_id as member_id,
      bg_member.g_role as membership_role,
      bg_me.fk_identity_id as me_id,
      bgroup.group_id as bg_id
   from o_gp_business bgroup
   inner join o_bs_group_member bg_member on (bg_member.fk_group_id = bgroup.fk_group_id)
   inner join o_bs_group_member bg_me on (bg_me.fk_group_id = bgroup.fk_group_id)
   where
      (bgroup.ownersintern>0 and bg_member.g_role='coach')
      or
      (bgroup.participantsintern>0 and bg_member.g_role='participant')
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
      bgroup.groupname as bg_name
   from o_gp_business bgroup
   inner join o_bs_group_member bg_member on (bg_member.fk_group_id = bgroup.fk_group_id)
   inner join o_bs_identity id_member on (bg_member.fk_identity_id = id_member.id)
   inner join o_user us_member on (id_member.fk_user_id = us_member.user_id)
   inner join o_userproperty first_member on (first_member.fk_user_id = us_member.user_id and first_member.propname='firstName')
   inner join o_userproperty last_member on (last_member.fk_user_id = us_member.user_id and last_member.propname='lastName')
   inner join o_bs_group_member bg_me on (bg_me.fk_group_id = bgroup.fk_group_id)
   where
      (bgroup.ownersintern>0 and bg_member.g_role='coach')
      or
      (bgroup.participantsintern>0 and bg_member.g_role='participant')
);

create or replace view o_re_membership_v as (
   select
      bmember.id as membership_id,
      bmember.creationdate as creationdate,
      bmember.lastmodified as lastmodified,
      bmember.fk_identity_id as fk_identity_id,
      bmember.g_role as g_role,
      re.repositoryentry_id as fk_entry_id
   from o_repositoryentry re
   inner join o_re_to_group relgroup on (relgroup.fk_entry_id=re.repositoryentry_id and relgroup.r_defgroup=1)
   inner join o_bs_group_member bmember on (bmember.fk_group_id=relgroup.fk_group_id) 
);

-- coaching
create or replace view o_as_eff_statement_students_v as (
   select
      sg_re.repositoryentry_id as re_id,
      sg_coach.fk_identity_id as tutor_id,
      sg_participant.fk_identity_id as student_id,
      sg_statement.id as st_id,
      (case when sg_statement.passed > 0 then 1 else 0 end) as st_passed,
      (case when sg_statement.passed = 0 then 1 else 0 end) as st_failed,
      (case when sg_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sg_statement.score as st_score,
      pg_initial_launch.id as pg_id
   from o_repositoryentry sg_re
   inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id)
   inner join o_bs_group_member sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role='coach')
   inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant')
   left join o_as_eff_statement sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource)
   left join o_as_user_course_infos pg_initial_launch on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id)
   group by sg_re.repositoryentry_id, sg_coach.fk_identity_id, sg_participant.fk_identity_id,
      sg_statement.passed, sg_statement.id, sg_statement.score, pg_initial_launch.id
);

create or replace view o_as_eff_statement_courses_v as (
   select
      sg_re.repositoryentry_id as re_id,
      sg_re.displayname as re_name,
      sg_coach.fk_identity_id as tutor_id,
      sg_participant.fk_identity_id as student_id,
      sg_statement.id as st_id,
      (case when sg_statement.passed > 0 then 1 else 0 end) as st_passed,
      (case when sg_statement.passed = 0 then 1 else 0 end) as st_failed,
      (case when sg_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sg_statement.score as st_score,
      pg_initial_launch.id as pg_id
   from o_repositoryentry sg_re
   inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id)
   inner join o_bs_group_member sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role='coach')
   inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant')
   left join o_as_eff_statement sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource)
   left join o_as_user_course_infos pg_initial_launch on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id)
   group by sg_re.repositoryentry_id, sg_re.displayname, sg_coach.fk_identity_id, sg_participant.fk_identity_id,
      sg_statement.passed, sg_statement.id, sg_statement.score, pg_initial_launch.id
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
      (case when sg_statement.passed > 0 then 1 else 0 end) as st_passed,
      (case when sg_statement.passed = 0 then 1 else 0 end) as st_failed,
      (case when sg_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sg_statement.score as st_score,
      pg_initial_launch.id as pg_id
   from o_repositoryentry sg_re
   inner join o_re_to_group togroup on (togroup.fk_entry_id = sg_re.repositoryentry_id)
   inner join o_gp_business sg_bg on (sg_bg.fk_group_id=togroup.fk_group_id)
   inner join o_bs_group_member sg_coach on (sg_coach.fk_group_id=togroup.fk_group_id and sg_coach.g_role='coach')
   inner join o_bs_group_member sg_participant on (sg_participant.fk_group_id=sg_coach.fk_group_id and sg_participant.g_role='participant')
   left join o_as_eff_statement sg_statement on (sg_statement.fk_identity = sg_participant.fk_identity_id and sg_statement.fk_resource_id = sg_re.fk_olatresource)
   left join o_as_user_course_infos pg_initial_launch on (pg_initial_launch.fk_resource_id = sg_re.fk_olatresource and pg_initial_launch.fk_identity = sg_participant.fk_identity_id)
   group by sg_re.repositoryentry_id, sg_re.displayname, sg_bg.group_id, sg_bg.groupname,
      sg_coach.fk_identity_id, sg_participant.fk_identity_id,
      sg_statement.passed, sg_statement.id, sg_statement.score, pg_initial_launch.id
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
alter table o_gp_business drop constraints FKCEEB8A86DF6BCD14;
alter table o_gp_business drop constraints FKCEEB8A86A1FAC766;
alter table o_gp_business drop constraints FKCEEB8A86C06E3EF3;
alter table o_gp_business drop constraints idx_bgp_rsrc;
alter table o_gp_business drop constraints idx_bgp_waiting;

alter table o_repositoryentry drop constraints FK2F9C439888C31018;
alter table o_repositoryentry drop constraints FK2F9C4398A1FAC766;
alter table o_repositoryentry drop constraints repo_tutor_sec_group_ctx;
alter table o_repositoryentry drop constraints repo_parti_sec_group_ctx;

alter table o_repositorymetadata drop constraints FKDB97A6493F14E3EE;

alter table o_bs_policy drop constraints FK9A1C5109F9C3F1D;


alter table o_bookmark drop constraints FK68C4E30663219E27;

alter table o_gp_business_to_resource drop constraints idx_bgp_to_rsrc_rsrc;
alter table o_gp_business_to_resource drop constraints idx_bgp_to_rsrc_group;

alter table o_gp_bgcontext drop constraints FK1C154FC47E4A0638;
alter table o_gp_bgcontextresource_rel drop constraints FK9903BEAC9F9C3F1D;
alter table o_gp_bgcontextresource_rel drop constraints FK9903BEACDF6BCD14;

alter table o_gp_bgarea drop constraints FK9EFAF698DF6BCD14;

alter table o_ep_struct_el drop constraints FKF26C8375236F29X;

alter table o_bs_invitation drop constraints FKF26C8375236F27X;
