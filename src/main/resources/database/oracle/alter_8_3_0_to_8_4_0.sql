-- instant messaging
create table o_im_message (
   id number(20) not null,
   creationdate date,
   msg_resname varchar2(50 char) not null,
   msg_resid number(20) not null,
   msg_anonym number default 0,
   msg_from varchar2(255 char) not null,
   msg_body clob,
   fk_from_identity_id number(20) not null,
   primary key (id)
);
alter table o_im_message add constraint idx_im_msg_to_fromid foreign key (fk_from_identity_id) references o_bs_identity (id);
create index idx_im_msg_res_idx on o_im_message (msg_resid,msg_resname);
create index idx_im_msg_from_idx on o_im_message(fk_from_identity_id);

create table o_im_notification (
   id number(20) not null,
   creationdate date,
   chat_resname varchar2(50 char) not null,
   chat_resid number(20) not null,
   fk_to_identity_id number(20) not null,
   fk_from_identity_id number(20) not null,
   primary key (id)
);
alter table o_im_notification add constraint idx_im_not_to_toid foreign key (fk_to_identity_id) references o_bs_identity (id);
alter table o_im_notification add constraint idx_im_not_to_fromid foreign key (fk_from_identity_id) references o_bs_identity (id);
create index idx_im_chat_res_idx on o_im_notification (chat_resid,chat_resname);
create index idx_im_chat_to_idx on o_im_notification (fk_to_identity_id);
create index idx_im_chat_from_idx on o_im_notification (fk_from_identity_id);

create table o_im_roster_entry (
   id number(20) not null,
   creationdate date,
   r_resname varchar2(50 char) not null,
   r_resid number(20) not null,
   r_nickname varchar2(255 char),
   r_fullname varchar2(255 char),
   r_vip number default 0,
   r_anonym number default 0,
   fk_identity_id number(20) not null,
   primary key (id)
);
alter table o_im_roster_entry add constraint idx_im_rost_to_id foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_im_rost_res_idx on o_im_roster_entry (r_resid,r_resname);
create index idx_im_rost_ident_idx on o_im_roster_entry (fk_identity_id);

create table o_im_preferences (
   id number(20) not null,
   creationdate date,
   visible_to_others number default 0,
   roster_def_status varchar2(12 char),
   fk_from_identity_id number(20) not null,
   primary key (id)
);
alter table o_im_preferences add constraint idx_im_prfs_to_id foreign key (fk_from_identity_id) references o_bs_identity (id);
create index idx_im_prefs_ident_idx on o_im_preferences (fk_from_identity_id);

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
   from o_im_roster_entry entry
   inner join o_bs_identity ident on (entry.fk_identity_id = ident.id)
);

-- views for contacts
create view o_gp_visible_participant_v as (
   select
      bg_part_member.id as membership_id,
      bgroup.group_id as bg_id,
      bgroup.groupname as bg_name,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_part_member.identity_id as bg_part_member_id,
      ident.name as bg_part_member_name 
   from o_gp_business bgroup
   inner join o_property bconfig on (bconfig.grp = bgroup.group_id and bconfig.name = 'displayMembers' and bconfig.category = 'config' and bconfig.longValue in (2,3,6,7))
   inner join o_bs_membership bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   inner join o_bs_identity ident on (bg_part_member.identity_id = ident.id)
 );
   
create view o_gp_visible_owner_v as ( 
   select
      bg_owner_member.id as membership_id,
      bgroup.group_id as bg_id,
      bgroup.groupname as bg_name,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_owner_member.identity_id as bg_owner_member_id,
      ident.name as bg_owner_member_name
   from o_gp_business bgroup
   inner join o_property bconfig on (bconfig.grp = bgroup.group_id and bconfig.name = 'displayMembers' and bconfig.category = 'config' and bconfig.longValue in (1,3,5,7))
   inner join o_bs_membership bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
   inner join o_bs_identity ident on (bg_owner_member.identity_id = ident.id)
);

drop view o_re_member_v;

-- add missing index
create index idx_gp_to_rsrc_resource on o_gp_business_to_resource(fk_resource);
create index idx_gp_to_rsrc_group on o_gp_business_to_resource(fk_group);

create index idx_area_resource on o_gp_bgarea (fk_resource);

create index idx_repoentry_tutor on o_repositoryentry(fk_tutorgroup);
create index idx_repoentry_parti on o_repositoryentry(fk_participantgroup);

create index idx_property_to_group on o_property(grp);
