alter table o_gp_business add column ownersintern bit not null default 0;
alter table o_gp_business add column participantsintern bit not null default 0;
alter table o_gp_business add column waitingintern bit not null default 0;
alter table o_gp_business add column ownerspublic bit not null default 0;
alter table o_gp_business add column participantspublic bit not null default 0;
alter table o_gp_business add column waitingpublic bit not null default 0;
alter table o_gp_business add column downloadmembers bit not null default 0;

create or replace view o_gp_contact_participant_v as (
   select
      bg_part_member.id as membership_id,
      bgroup.group_id as bg_id,
      bgroup.groupname as bg_name,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_part_member.identity_id as bg_part_member_id,
      ident.name as bg_part_member_name 
   from o_gp_business as bgroup
   inner join o_bs_membership as bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   inner join o_bs_identity as ident on (bg_part_member.identity_id = ident.id)
   where bgroup.participantsintern=1
);
   
create or replace view o_gp_contact_owner_v as (
   select
      bg_owner_member.id as membership_id,
      bgroup.group_id as bg_id,
      bgroup.groupname as bg_name,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_owner_member.identity_id as bg_owner_member_id,
      ident.name as bg_owner_member_name
   from o_gp_business as bgroup
   inner join o_bs_membership as bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
   inner join o_bs_identity as ident on (bg_owner_member.identity_id = ident.id)
   where bgroup.ownersintern=1
);

create or replace view o_gp_contactkey_participant_v as (
   select
      bg_part_member.id as membership_id,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_part_member.identity_id as bg_part_member_id
   from o_gp_business as bgroup
   inner join o_bs_membership as bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   where bgroup.participantsintern=1
);
   
create or replace view o_gp_contactkey_owner_v as (
   select
      bg_owner_member.id as membership_id,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_owner_member.identity_id as bg_owner_member_id
   from o_gp_business as bgroup
   inner join o_bs_membership as bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
   where bgroup.ownersintern=1
);

