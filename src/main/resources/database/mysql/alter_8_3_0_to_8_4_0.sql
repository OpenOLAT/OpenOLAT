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
alter table o_im_preferences add constraint idx_im_prfs_to_id foreign key (fk_from_identity_id) references o_bs_identity (id);

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
   inner join o_property as bconfig on (bconfig.grp = bgroup.group_id and bconfig.name = 'displayMembers' and bconfig.category = 'config')
   inner join o_bs_membership as bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup and bconfig.longValue in (2,3,6,7))
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
   inner join o_property as bconfig on (bconfig.grp = bgroup.group_id and bconfig.name = 'displayMembers' and bconfig.category = 'config')
   inner join o_bs_membership as bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup and bconfig.longValue in (1,3,5,7))
   inner join o_bs_identity as ident on (bg_owner_member.identity_id = ident.id)
);





