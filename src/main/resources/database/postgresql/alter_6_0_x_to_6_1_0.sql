-- create table for cluster locks
drop table if exists o_plock ;
create table o_plock (
    plock_id int8 not null, 
    version int8 not null,
    creationdate timestamp, 
    asset varchar(255) not null unique, 
    primary key (plock_id)
);

-- tables that keep last modified
alter table o_repositoryentry add column version int8;
update o_repositoryentry set version=0;
alter table o_repositoryentry alter column version set not null;

alter table o_gp_business add column version int8;
update o_gp_business set version=0;
alter table o_gp_business alter column version set not null;

alter table o_message add column version int8;
update o_message set version=0;
alter table o_message alter column version set not null;

alter table o_note add column version int8;
update o_note set version=0;
alter table o_note alter column version set not null;

alter table o_noti_sub add column version int8;
update o_noti_sub set version=0;
alter table o_noti_sub alter column version set not null;

alter table o_property add column version int8;
update o_property set version=0;
alter table o_property alter column version set not null;

alter table o_bs_membership add column version int8;
update o_bs_membership set version=0;
alter table o_bs_membership alter column version set not null;

alter table o_qtiresultset add column version int8;
update o_qtiresultset set version=0;
alter table o_qtiresultset alter column version set not null;

alter table o_qtiresult add column version int8;
update o_qtiresult set version=0;
alter table o_qtiresult alter column version set not null;

-- tables that drop lastModified
alter table o_forum drop column lastmodified;
alter table o_forum add column version int8;
update o_forum set version=0;
alter table o_forum alter column version set not null;

alter table o_bs_authentication drop column lastmodified;
alter table o_bs_authentication add column version int8;
update o_bs_authentication set version=0;
alter table o_bs_authentication alter column version set not null;

alter table o_bs_secgroup drop column lastmodified;
alter table o_bs_secgroup add column version int8;
update o_bs_secgroup set version=0;
alter table o_bs_secgroup alter column version set not null;

alter table o_temporarykey drop column lastmodified;
alter table o_temporarykey add column version int8;
update o_temporarykey set version=0;
alter table o_temporarykey alter column version set not null;

alter table o_noti_pub drop column lastmodified;
alter table o_noti_pub add column version int8;
update o_noti_pub set version=0;
alter table o_noti_pub alter column version set not null;

alter table o_bs_identity drop column lastmodified;
alter table o_bs_identity add column version int8;
update o_bs_identity set version=0;
alter table o_bs_identity alter column version set not null;

alter table o_olatresource drop column lastmodified;
alter table o_olatresource add column version int8;
update o_olatresource set version=0;
alter table o_olatresource alter column version set not null;

alter table o_bs_namedgroup drop column lastmodified;
alter table o_bs_namedgroup add column version int8;
update o_bs_namedgroup set version=0;
alter table o_bs_namedgroup alter column version set not null;

alter table o_catentry drop column lastmodified;
alter table o_catentry add column version int8;
update o_catentry set version=0;
alter table o_catentry alter column version set not null;

alter table o_gp_bgcontext drop column lastmodified;
alter table o_gp_bgcontext add column version int8;
update o_gp_bgcontext set version=0;
alter table o_gp_bgcontext alter column version set not null;

alter table o_references drop column lastmodified;
alter table o_references add column version int8;
update o_references set version=0;
alter table o_references alter column version set not null;

alter table o_repositorymetadata drop column lastmodified;
alter table o_repositorymetadata add column version int8;
update o_repositorymetadata set version=0;
alter table o_repositorymetadata alter column version set not null;

alter table o_user drop column lastmodified;
alter table o_user add column version int8;
update o_user set version=0;
alter table o_user alter column version set not null;

alter table o_gp_bgcontextresource_rel drop column lastmodified;
alter table o_gp_bgcontextresource_rel add column version int8;
update o_gp_bgcontextresource_rel set version=0;
alter table o_gp_bgcontextresource_rel alter column version set not null;

alter table o_gp_bgtoarea_rel drop column lastmodified;
alter table o_gp_bgtoarea_rel add column version int8;
update o_gp_bgtoarea_rel set version=0;
alter table o_gp_bgtoarea_rel alter column version set not null;

alter table o_bs_policy drop column lastmodified;
alter table o_bs_policy add column version int8;
update o_bs_policy set version=0;
alter table o_bs_policy alter column version set not null;

alter table o_gp_bgarea drop column lastmodified;
alter table o_gp_bgarea add column version int8;
update o_gp_bgarea set version=0;
alter table o_gp_bgarea alter column version set not null;

alter table o_bookmark drop column lastmodified;
alter table o_bookmark add column version int8;
update o_bookmark set version=0;
alter table o_bookmark alter column version set not null;

alter table o_lifecycle drop column lastmodified;
alter table o_lifecycle add column version int8;
update o_lifecycle set version=0;
alter table o_lifecycle alter column version set not null;

alter table oc_lock drop column lastmodified;
alter table oc_lock add column version int8;
update oc_lock set version=0;
alter table oc_lock alter column version set not null;

alter table o_readmessage drop column lastmodified;
alter table o_readmessage add column version int8;
update o_readmessage set version=0;
alter table o_readmessage alter column version set not null;

alter table o_lifecycle alter column uservalue type text;

drop index identity_forum_idx;
create index readmessage_forum_idx on o_readmessage (forum_id);
create index readmessage_identity_idx on o_readmessage (identity_id);
create index id_idx on o_olatresource (resid);
