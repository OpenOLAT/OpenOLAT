alter table o_ac_reservation add column expirationdate timestamp;
alter table o_ac_reservation add column reservationtype varchar(32);

alter table o_ac_reservation add constraint idx_rsrv_to_rsrc_rsrc foreign key (fk_resource) references o_olatresource (resource_id);
alter table o_ac_reservation add constraint idx_rsrv_to_rsrc_identity foreign key (fk_identity) references o_bs_identity (id);


drop view o_gp_business_v;
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
      (select count(pending.reservation_id) from o_ac_reservation as pending where pending.fk_resource = gp.fk_resource) as num_of_pendings,
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
         and (offer.validfrom is null or offer.validfrom <= current_timestamp)
         and (offer.validto is null or offer.validto >= current_timestamp)
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

-- openmeetings
create table o_om_room_reference (
   id int8 not null,
   version int4 not null,
   lastmodified timestamp,
   creationdate timestamp,
   businessgroup int8,
   resourcetypename varchar(50),
   resourcetypeid int8,
   ressubpath varchar(255),
   roomId int8,
   config text,
   primary key (id)
);

alter table o_om_room_reference  add constraint idx_omroom_to_bgroup foreign key (businessgroup) references o_gp_business (group_id);
create index idx_omroom_residname on o_om_room_reference (resourcetypename,resourcetypeid);