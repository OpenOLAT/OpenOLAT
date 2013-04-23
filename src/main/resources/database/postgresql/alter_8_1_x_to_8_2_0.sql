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
   where (re.membersonly=true and re.accesscode=1) or re.accesscode>=3
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
   where (re.membersonly=true and re.accesscode=1) or re.accesscode>=3
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
   from o_bs_membership as membership
   left join o_repositoryentry as re_part_member on (membership.secgroup_id = re_part_member.fk_participantgroup)
   left join o_repositoryentry as re_tutor_member on (membership.secgroup_id = re_tutor_member.fk_tutorgroup)
   left join o_repositoryentry as re_owner_member on (membership.secgroup_id = re_owner_member.fk_ownergroup)
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
      (case when gp.waitinglist_enabled = true
         then 
           (select count(waiting.id) from o_bs_membership as waiting where waiting.secgroup_id = gp.fk_partipiciantgroup)
         else
           0
      end) as num_waiting,
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

-- add paypal transactions
create table o_ac_paypal_transaction (
   transaction_id int8 not null,
   version int8 not null,
   creationdate timestamp,
   ref_no varchar(255),
   order_id int8 not null,
   order_part_id int8 not null,
   method_id int8 not null,
   success_uuid varchar(32) not null,
   cancel_uuid varchar(32) not null,
   amount_amount DECIMAL,
   amount_currency_code VARCHAR(3),
   pay_response_date timestamp,
   pay_key varchar(255),
   ack varchar(255),
   build varchar(255),
   coorelation_id varchar(255),
   payment_exec_status varchar(255),
   ipn_transaction_id varchar(255),
   ipn_transaction_status varchar(255),
   ipn_sender_transaction_id varchar(255),
   ipn_sender_transaction_status varchar(255),
   ipn_sender_email varchar(255),
   ipn_verify_sign varchar(255),
   ipn_pending_reason varchar(255),
   trx_status VARCHAR(32) not null default 'NEW',
   trx_amount DECIMAL,
   trx_currency_code VARCHAR(3),
   primary key (transaction_id)
);
create index paypal_pay_key_idx on o_ac_paypal_transaction (pay_key);
create index paypal_pay_trx_id_idx on o_ac_paypal_transaction (ipn_transaction_id);
create index paypal_pay_s_trx_id_idx on o_ac_paypal_transaction (ipn_sender_transaction_id);

create table o_ac_reservation (
   reservation_id int8 NOT NULL,
   creationdate timestamp,
   lastmodified timestamp,
   version int4 not null,
   fk_identity int8 not null,
   fk_resource int8 not null,
   primary key (reservation_id)
);

-- course db
create table o_co_db_entry (
   id int8 not null,
   version int4 not null,
   lastmodified timestamp,
   creationdate timestamp,
   courseid int8,
   identity int8,
   category varchar(32),
   name varchar(255) not null,
   floatvalue decimal(65,30),
   longvalue int8,
   stringvalue varchar(255),
   textvalue TEXT,
   primary key (id)
);
create index o_co_db_course_idx on o_co_db_entry (courseid);
create index o_co_db_cat_idx on o_co_db_entry (category);
create index o_co_db_name_idx on o_co_db_entry (name);
alter table o_co_db_entry add constraint FKB60B1BA5F7E870XY foreign key (identity) references o_bs_identity;

-- add mapper table
create table o_mapper (
   id int8 not null,
   lastmodified timestamp,
   creationdate timestamp,
   mapper_uuid varchar(64),
   orig_session_id varchar(64),
   xml_config TEXT,
   primary key (id)
);
create index o_mapper_uuid_idx on o_mapper (mapper_uuid);

