-- relation groups to resources
create table o_gp_business_to_resource (
   g_id bigint not null,
   version int4 not null,
   creationdate timestamp,
   fk_resource int8 not null,
   fk_group int8 not null,
   primary key (g_id)
);
alter table o_gp_business_to_resource add constraint idx_bgp_to_rsrc_rsrc foreign key (fk_resource) references o_olatresource (resource_id);
alter table o_gp_business_to_resource add constraint idx_bgp_to_rsrc_group foreign key (fk_group) references o_gp_business (group_id);

-- groups
alter table o_gp_business add column fk_resource int8 unique default null;
alter table o_gp_business add constraint idx_bgp_rsrc foreign key (fk_resource) references o_olatresource (resource_id);
alter table o_gp_business add constraint idx_bgp_waiting foreign key (fk_waitinggroup) references o_bs_secgroup (id);


-- area
alter table o_gp_bgarea alter column groupcontext_fk drop not null;
alter table o_gp_bgarea add column fk_resource int8 default null;
alter table o_gp_bgarea add constraint idx_area_to_resource foreign key (fk_resource) references o_olatresource (resource_id);

-- view
create or replace view o_gp_business_to_repository_v as (
	select 
		grp.group_id as grp_id,
		repoentry.repositoryentry_id as re_id,
		repoentry.displayname as re_displayname
	from o_gp_business as grp
	inner join o_gp_business_to_resource as relation on (relation.fk_group = grp.group_id)
	inner join o_repositoryentry as repoentry on (repoentry.fk_olatresource = relation.fk_resource)
);

create or replace view o_re_member_v as (
   select
      re.repositoryentry_id as re_id,
      re.membersonly as re_membersonly,
      re.accesscode as re_accesscode,
      re_part_member.identity_id as re_part_member_id,
      re_tutor_member.identity_id as re_tutor_member_id,
      re_owner_member.identity_id as re_owner_member_id,
      bg_part_member.identity_id as bg_part_member_id,
      bg_owner_member.identity_id as bg_owner_member_id
   from o_repositoryentry as re
   left join o_bs_membership as re_part_member on (re_part_member.secgroup_id = re.fk_participantgroup)
   left join o_bs_membership as re_tutor_member on (re_tutor_member.secgroup_id = re.fk_tutorgroup)
   left join o_bs_membership as re_owner_member on (re_owner_member.secgroup_id = re.fk_ownergroup)
   left join o_gp_business_to_resource as bgroup_rel on (bgroup_rel.fk_resource = re.fk_olatresource)
   left join o_gp_business as bgroup on (bgroup.group_id = bgroup_rel.fk_group)
   left join o_bs_membership as bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   left join o_bs_membership as bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
);

create or replace view o_re_strict_member_v as (
   select
      re.repositoryentry_id as re_id,
      re_part_member.identity_id as re_part_member_id,
      re_tutor_member.identity_id as re_tutor_member_id,
      re_owner_member.identity_id as re_owner_member_id,
      bg_part_member.identity_id as bg_part_member_id,
      bg_owner_member.identity_id as bg_owner_member_id
   from o_repositoryentry as re
   left join o_bs_membership as re_part_member on (re_part_member.secgroup_id = re.fk_participantgroup)
   left join o_bs_membership as re_tutor_member on (re_tutor_member.secgroup_id = re.fk_tutorgroup)
   left join o_bs_membership as re_owner_member on (re_owner_member.secgroup_id = re.fk_ownergroup)
   left join o_gp_business_to_resource as bgroup_rel on (bgroup_rel.fk_resource = re.fk_olatresource)
   left join o_gp_business as bgroup on (bgroup.group_id = bgroup_rel.fk_group)
   left join o_bs_membership as bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   left join o_bs_membership as bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
   where re.membersonly=true and re.accesscode=1
);

create or replace view o_re_strict_participant_v as (
   select
      re.repositoryentry_id as re_id,
      re_part_member.identity_id as re_part_member_id,
      bg_part_member.identity_id as bg_part_member_id
   from o_repositoryentry as re
   left join o_bs_membership as re_part_member on (re_part_member.secgroup_id = re.fk_participantgroup)
   left join o_gp_business_to_resource as bgroup_rel on (bgroup_rel.fk_resource = re.fk_olatresource)
   left join o_gp_business as bgroup on (bgroup.group_id = bgroup_rel.fk_group)
   left join o_bs_membership as bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   where re.membersonly=true and re.accesscode=1
);

create or replace view o_re_strict_tutor_v as (
   select
      re.repositoryentry_id as re_id,
      re_tutor_member.identity_id as re_tutor_member_id,
      re_owner_member.identity_id as re_owner_member_id,
      bg_owner_member.identity_id as bg_owner_member_id
   from o_repositoryentry as re
   left join o_bs_membership as re_tutor_member on (re_tutor_member.secgroup_id = re.fk_tutorgroup)
   left join o_bs_membership as re_owner_member on (re_owner_member.secgroup_id = re.fk_ownergroup)
   left join o_gp_business_to_resource as bgroup_rel on (bgroup_rel.fk_resource = re.fk_olatresource)
   left join o_gp_business as bgroup on (bgroup.group_id = bgroup_rel.fk_group)
   left join o_bs_membership as bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
   where re.membersonly=true and re.accesscode=1
);

create or replace view o_bs_gp_membership_v as (
   select
      membership.id as membership_id,
      membership.identity_id as identity_id,
      membership.lastmodified as lastmodified,
      membership.creationdate as creationdate,
      owned_gp.group_id as owned_gp_id,
      participant_gp.group_id as participant_gp_id,
      waiting_gp.group_id as waiting_gp_id
   from o_bs_membership as membership
   left join o_gp_business as owned_gp on (membership.secgroup_id = owned_gp.fk_ownergroup)
   left join o_gp_business as participant_gp on (membership.secgroup_id = participant_gp.fk_partipiciantgroup)
   left join o_gp_business as waiting_gp on (membership.secgroup_id = waiting_gp.fk_waitinggroup)
   where (owned_gp.group_id is not null or participant_gp.group_id is not null or waiting_gp.group_id is not null)
);

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
      (select count(own.id) from o_bs_membership as own where own.secgroup_id = gp.fk_ownergroup) as num_of_owners,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=true
         and (offer.validfrom is null or offer.validfrom >= current_timestamp)
         and (offer.validto is null or offer.validto <= current_timestamp)
      ) as num_of_valid_offers,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=true
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

