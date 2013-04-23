-- update null value of user properties
update o_userproperty set propvalue='_' where propvalue is null;

-- relation groups to resources
create table o_gp_business_to_resource (
   g_id number(20) not null,
   version number(20) not null,
   creationdate date,
   fk_resource number(20) not null,
   fk_group number(20) not null,
   primary key (g_id)
);
alter table o_gp_business_to_resource add constraint idx_bgp_to_rsrc_rsrc foreign key (fk_resource) references o_olatresource (resource_id);
alter table o_gp_business_to_resource add constraint idx_bgp_to_rsrc_group foreign key (fk_group) references o_gp_business (group_id);

-- groups
alter table o_gp_business add fk_resource number(20) unique;
alter table o_gp_business add constraint idx_bgp_rsrc foreign key (fk_resource) references o_olatresource (resource_id);


-- area
alter table o_gp_bgarea modify groupcontext_fk number(20) null;
alter table o_gp_bgarea add fk_resource number(20);
alter table o_gp_bgarea add constraint idx_area_to_resource foreign key (fk_resource) references o_olatresource (resource_id);

-- view
create or replace view o_gp_business_to_repository_v as (
	select 
		grp.group_id as grp_id,
		repoentry.repositoryentry_id as re_id,
		repoentry.displayname as re_displayname
	from o_gp_business grp
	inner join o_gp_business_to_resource relation on (relation.fk_group = grp.group_id)
	inner join o_repositoryentry repoentry on (repoentry.fk_olatresource = relation.fk_resource)
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
   from o_repositoryentry re
   left join o_bs_membership re_part_member on (re_part_member.secgroup_id = re.fk_participantgroup)
   left join o_bs_membership re_tutor_member on (re_tutor_member.secgroup_id = re.fk_tutorgroup)
   left join o_bs_membership re_owner_member on (re_owner_member.secgroup_id = re.fk_ownergroup)
   left join o_gp_business_to_resource bgroup_rel on (bgroup_rel.fk_resource = re.fk_olatresource)
   left join o_gp_business bgroup on (bgroup.group_id = bgroup_rel.fk_group)
   left join o_bs_membership bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   left join o_bs_membership bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
);

create or replace view o_re_strict_member_v as (
   select
      re.repositoryentry_id as re_id,
      re_part_member.identity_id as re_part_member_id,
      re_tutor_member.identity_id as re_tutor_member_id,
      re_owner_member.identity_id as re_owner_member_id,
      bg_part_member.identity_id as bg_part_member_id,
      bg_owner_member.identity_id as bg_owner_member_id
   from o_repositoryentry re
   left join o_bs_membership re_part_member on (re_part_member.secgroup_id = re.fk_participantgroup)
   left join o_bs_membership re_tutor_member on (re_tutor_member.secgroup_id = re.fk_tutorgroup)
   left join o_bs_membership re_owner_member on (re_owner_member.secgroup_id = re.fk_ownergroup)
   left join o_gp_business_to_resource bgroup_rel on (bgroup_rel.fk_resource = re.fk_olatresource)
   left join o_gp_business bgroup on (bgroup.group_id = bgroup_rel.fk_group)
   left join o_bs_membership bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   left join o_bs_membership bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
   where re.membersonly=1 and re.accesscode=1
);

create or replace view o_re_strict_participant_v as (
   select
      re.repositoryentry_id as re_id,
      re_part_member.identity_id as re_part_member_id,
      bg_part_member.identity_id as bg_part_member_id
   from o_repositoryentry re
   left join o_bs_membership re_part_member on (re_part_member.secgroup_id = re.fk_participantgroup)
   left join o_gp_business_to_resource bgroup_rel on (bgroup_rel.fk_resource = re.fk_olatresource)
   left join o_gp_business bgroup on (bgroup.group_id = bgroup_rel.fk_group)
   left join o_bs_membership bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   where (re.membersonly=1 and re.accesscode=1) or re.accesscode>=3
);

create or replace view o_re_strict_tutor_v as (
   select
      re.repositoryentry_id as re_id,
      re_tutor_member.identity_id as re_tutor_member_id,
      re_owner_member.identity_id as re_owner_member_id,
      bg_owner_member.identity_id as bg_owner_member_id
   from o_repositoryentry re
   left join o_bs_membership re_tutor_member on (re_tutor_member.secgroup_id = re.fk_tutorgroup)
   left join o_bs_membership re_owner_member on (re_owner_member.secgroup_id = re.fk_ownergroup)
   left join o_gp_business_to_resource bgroup_rel on (bgroup_rel.fk_resource = re.fk_olatresource)
   left join o_gp_business bgroup on (bgroup.group_id = bgroup_rel.fk_group)
   left join o_bs_membership bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
   where (re.membersonly=1 and re.accesscode=1) or re.accesscode>=3
);

create or replace view o_re_membership_v as (
   select
      membership.id as membership_id,
      membership.identity_id as identity_id,
      membership.lastmodified as lastmodified,
      membership.creationdate as creationdate,
      re_owner_member.repositoryentry_id as owner_re_id,
      re_owner_member.fk_olatresource as owner_ores_id,
      re_tutor_member.repositoryentry_id as tutor_re_id,
      re_tutor_member.fk_olatresource as tutor_ores_id,
      re_part_member.repositoryentry_id as participant_re_id,
      re_part_member.fk_olatresource as participant_ores_id
   from o_bs_membership membership
   left join o_repositoryentry re_part_member on (membership.secgroup_id = re_part_member.fk_participantgroup)
   left join o_repositoryentry re_tutor_member on (membership.secgroup_id = re_tutor_member.fk_tutorgroup)
   left join o_repositoryentry re_owner_member on (membership.secgroup_id = re_owner_member.fk_ownergroup)
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
   from o_bs_membership membership
   left join o_gp_business owned_gp on (membership.secgroup_id = owned_gp.fk_ownergroup)
   left join o_gp_business participant_gp on (membership.secgroup_id = participant_gp.fk_partipiciantgroup)
   left join o_gp_business waiting_gp on (membership.secgroup_id = waiting_gp.fk_waitinggroup)
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
      (select count(part.id) from o_bs_membership part where part.secgroup_id = gp.fk_partipiciantgroup) as num_of_participants,
      (select count(own.id) from o_bs_membership own where own.secgroup_id = gp.fk_ownergroup) as num_of_owners,
      (case when gp.waitinglist_enabled = 1
         then 
           (select count(waiting.id) from o_bs_membership waiting where waiting.secgroup_id = gp.fk_partipiciantgroup)
         else
           0
      end) as num_waiting,
      (select count(offer.offer_id) from o_ac_offer offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=1
         and (offer.validfrom is null or offer.validfrom >= current_date)
         and (offer.validto is null or offer.validto <= current_date)
      ) as num_of_valid_offers,
      (select count(offer.offer_id) from o_ac_offer offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=1
      ) as num_of_offers,
      (select count(relation.fk_resource) from o_gp_business_to_resource relation 
         where relation.fk_group = gp.group_id
      ) as num_of_relations,
      gp.fk_resource as fk_resource,
      gp.fk_ownergroup as fk_ownergroup,
      gp.fk_partipiciantgroup as fk_partipiciantgroup,
      gp.fk_waitinggroup as fk_waitinggroup
   from o_gp_business gp
);

-- add paypal transactions
create table o_ac_paypal_transaction (
   transaction_id number(20) not null,
   version number(20) not null,
   creationdate date,
   ref_no varchar2(255 char),
   order_id number(20) not null,
   order_part_id number(20) not null,
   method_id number(20) not null,
   success_uuid varchar2(32 char) not null,
   cancel_uuid varchar2(32 char) not null,
   amount_amount DECIMAL,
   amount_currency_code varchar2(3 char),
   pay_response_date date,
   pay_key varchar2(255 char),
   ack varchar2(255 char),
   build varchar2(255 char),
   coorelation_id varchar2(255 char),
   payment_exec_status varchar2(255 char),
   ipn_transaction_id varchar2(255 char),
   ipn_transaction_status varchar2(255 char),
   ipn_sender_transaction_id varchar2(255 char),
   ipn_sender_transaction_status varchar2(255 char),
   ipn_sender_email varchar2(255 char),
   ipn_verify_sign varchar2(255 char),
   ipn_pending_reason varchar2(255 char),
   trx_status varchar2(32 char) default 'NEW' not null,
   trx_amount NUMBER (21,20),
   trx_currency_code varchar2(3 char),
   primary key (transaction_id)
);
create index paypal_pay_key_idx on o_ac_paypal_transaction (pay_key);
create index paypal_pay_trx_id_idx on o_ac_paypal_transaction (ipn_transaction_id);
create index paypal_pay_s_trx_id_idx on o_ac_paypal_transaction (ipn_sender_transaction_id);

create table o_ac_reservation (
   reservation_id number(20) NOT NULL,
   creationdate date,
   lastmodified date,
   version number(20) not null,
   fk_identity number(20) not null,
   fk_resource number(20) not null,
   primary key (reservation_id)
);

-- course db
create table o_co_db_entry (
   id number(20) not null,
   version number(20) not null,
   lastmodified date,
   creationdate date,
   courseid number(20),
   identity number(20),
   category varchar2(32 char),
   name varchar2(255 char) not null,
   floatvalue float,
   longvalue number(20),
   stringvalue varchar2(255 char),
   textvalue varchar2(4000 char),
   primary key (id)
);
create index o_co_db_course_idx on o_co_db_entry (courseid);
create index o_co_db_cat_idx on o_co_db_entry (category);
create index o_co_db_name_idx on o_co_db_entry (name);
alter table o_co_db_entry add constraint FKB60B1BA5F7E870XY foreign key (identity) references o_bs_identity;

-- add mapper table
create table o_mapper (
   id number(20) not null,
   lastmodified date,
   creationdate date,
   mapper_uuid varchar2(64 char),
   orig_session_id varchar2(64 char),
   xml_config CLOB,
   primary key (id)
);
create index o_mapper_uuid_idx on o_mapper (mapper_uuid);

