alter table o_gp_business add (ownersintern number default 0 not null);
alter table o_gp_business add (participantsintern number default 0 not null);
alter table o_gp_business add (waitingintern number default 0 not null);
alter table o_gp_business add (ownerspublic number default 0 not null);
alter table o_gp_business add (participantspublic number default 0 not null);
alter table o_gp_business add (waitingpublic number default 0 not null);
alter table o_gp_business add (downloadmembers number default 0 not null);

create view o_gp_contact_participant_v as (
   select
      bg_part_member.id as membership_id,
      bgroup.group_id as bg_id,
      bgroup.groupname as bg_name,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_part_member.identity_id as bg_part_member_id,
      ident.name as bg_part_member_name 
   from o_gp_business bgroup
   inner join o_bs_membership bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   inner join o_bs_identity ident on (bg_part_member.identity_id = ident.id)
   where bgroup.participantsintern=1
);

create view o_gp_contact_owner_v as (
   select
      bg_owner_member.id as membership_id,
      bgroup.group_id as bg_id,
      bgroup.groupname as bg_name,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_owner_member.identity_id as bg_owner_member_id,
      ident.name as bg_owner_member_name
   from o_gp_business bgroup
   inner join o_bs_membership bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
   inner join o_bs_identity ident on (bg_owner_member.identity_id = ident.id)
   where bgroup.ownersintern=1
);

create view o_gp_contactkey_participant_v as (
   select
      bg_part_member.id as membership_id,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_part_member.identity_id as bg_part_member_id
   from o_gp_business bgroup
   inner join o_bs_membership bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   where bgroup.participantsintern=1
);
  
create view o_gp_contactkey_owner_v as (
   select
      bg_owner_member.id as membership_id,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_owner_member.identity_id as bg_owner_member_id
   from o_gp_business bgroup
   inner join o_bs_membership bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
   where bgroup.ownersintern=1
);

-- checklist
create table o_cl_checkbox (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   c_checkboxid varchar2(50 char) not null,
   c_resname varchar2(50 char) not null,
   c_resid number(20) not null,
   c_ressubpath varchar2(255 char) not null,
   primary key (id)
);

create table o_cl_check (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   c_score float,
   c_checked number default 0,
   fk_identity_id number(20) not null,
   fk_checkbox_id number(20) not null,
   primary key (id)
);

alter table o_cl_check add constraint check_identity_ctx foreign key (fk_identity_id) references o_bs_identity (id);
create index check_to_identity_idx on o_cl_check (fk_identity_id);
alter table o_cl_check add constraint check_box_ctx foreign key (fk_checkbox_id) references o_cl_checkbox (id);
create index check_to_checkbox_idx on o_cl_check (fk_checkbox_id);
alter table o_cl_check add unique (fk_identity_id, fk_checkbox_id);
create index idx_checkbox_uuid_idx on o_cl_checkbox (c_checkboxid);

