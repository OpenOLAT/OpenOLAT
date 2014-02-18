alter table o_gp_business add column ownersintern bool not null default false;
alter table o_gp_business add column participantsintern bool not null default false;
alter table o_gp_business add column waitingintern bool not null default false;
alter table o_gp_business add column ownerspublic bool not null default false;
alter table o_gp_business add column participantspublic bool not null default false;
alter table o_gp_business add column waitingpublic bool not null default false;
alter table o_gp_business add column downloadmembers bool not null default false;

drop view o_gp_visible_participant_v;
create view o_gp_contact_participant_v as (
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
   where bgroup.participantsintern=true
);
  
drop view o_gp_visible_owner_v;
create view o_gp_contact_owner_v as (
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
   where bgroup.ownersintern=true
);

create view o_gp_contactkey_participant_v as (
   select
      bg_part_member.id as membership_id,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_part_member.identity_id as bg_part_member_id
   from o_gp_business as bgroup
   inner join o_bs_membership as bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   where bgroup.participantsintern=true
);
  
create view o_gp_contactkey_owner_v as (
   select
      bg_owner_member.id as membership_id,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_owner_member.identity_id as bg_owner_member_id
   from o_gp_business as bgroup
   inner join o_bs_membership as bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
   where bgroup.ownersintern=true
);

-- checklist
create table o_cl_checkbox (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_checkboxid varchar(50) not null,
   c_resname varchar(50) not null,
   c_resid int8 not null,
   c_ressubpath varchar(255) not null,
   primary key (id)
);

create table o_cl_check (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_score float(24),
   c_checked boolean default false,
   fk_identity_id int8 not null,
   fk_checkbox_id int8 not null,
   primary key (id)
);

alter table o_cl_check add constraint check_identity_ctx foreign key (fk_identity_id) references o_bs_identity (id);
create index check_to_identity_idx on o_cl_check (fk_identity_id);
alter table o_cl_check add constraint check_box_ctx foreign key (fk_checkbox_id) references o_cl_checkbox (id);
create index check_to_checkbox_idx on o_cl_check (fk_checkbox_id);
alter table o_cl_check add unique (fk_identity_id, fk_checkbox_id);
create index idx_checkbox_uuid_idx on o_cl_checkbox (c_checkboxid);
