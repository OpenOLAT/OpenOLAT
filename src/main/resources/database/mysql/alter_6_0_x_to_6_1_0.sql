SET FOREIGN_KEY_CHECKS = 0;

-- create table for cluster locks
drop table if exists o_plock ;
  create table o_plock (
	plock_id bigint not null, 
	version bigint not null,
	creationdate datetime, 
	asset varchar(255) not null unique,	
	primary key (plock_id)
);
alter table o_plock type = InnoDB;

-- tables that keep last modified
alter table o_repositoryentry add column version bigint;
update o_repositoryentry set version=0;
alter table o_repositoryentry modify version bigint not null;

alter table o_gp_business add column version bigint;
update o_gp_business set version=0;
alter table o_gp_business modify version bigint not null;

alter table o_message add column version bigint;
update o_message set version=0;
alter table o_message modify version bigint not null;

alter table o_note add column version bigint;
update o_note set version=0;
alter table o_note modify version bigint not null;

alter table o_noti_sub add column version bigint;
update o_noti_sub set version=0;
alter table o_noti_sub modify version bigint not null;

alter table o_property add column version bigint;
update o_property set version=0;
alter table o_property modify version bigint not null;

alter table o_bs_membership add column version bigint;
update o_bs_membership set version=0;
alter table o_bs_membership modify version bigint not null;

alter table o_qtiresultset add column version bigint;
update o_qtiresultset set version=0;
alter table o_qtiresultset modify version bigint not null;

alter table o_qtiresult add column version bigint;
update o_qtiresult set version=0;
alter table o_qtiresult modify version bigint not null;

-- tables that drop lastModified
alter table o_forum drop column lastmodified;
alter table o_forum add column version bigint;
update o_forum set version=0;
alter table o_forum modify version bigint not null;

alter table o_bs_authentication drop column lastmodified;
alter table o_bs_authentication add column version bigint;
update o_bs_authentication set version=0;
alter table o_bs_authentication modify version bigint not null;

alter table o_bs_secgroup drop column lastmodified;
alter table o_bs_secgroup add column version bigint;
update o_bs_secgroup set version=0;
alter table o_bs_secgroup modify version bigint not null;

alter table o_temporarykey drop column lastmodified;
alter table o_temporarykey add column version bigint;
update o_temporarykey set version=0;
alter table o_temporarykey modify version bigint not null;

alter table o_noti_pub drop column lastmodified;
alter table o_noti_pub add column version bigint;
update o_noti_pub set version=0;
alter table o_noti_pub modify version bigint not null;

alter table o_bs_identity drop column lastmodified;
alter table o_bs_identity add column version bigint;
update o_bs_identity set version=0;
alter table o_bs_identity modify version bigint not null;

alter table o_olatresource drop column lastmodified;
alter table o_olatresource add column version bigint;
update o_olatresource set version=0;
alter table o_olatresource modify version bigint not null;

alter table o_bs_namedgroup drop column lastmodified;
alter table o_bs_namedgroup add column version bigint;
update o_bs_namedgroup set version=0;
alter table o_bs_namedgroup modify version bigint not null;

alter table o_catentry drop column lastmodified;
alter table o_catentry add column version bigint;
update o_catentry set version=0;
alter table o_catentry modify version bigint not null;

alter table o_gp_bgcontext drop column lastmodified;
alter table o_gp_bgcontext add column version bigint;
update o_gp_bgcontext set version=0;
alter table o_gp_bgcontext modify version bigint not null;

alter table o_references drop column lastmodified;
alter table o_references add column version bigint;
update o_references set version=0;
alter table o_references modify version bigint not null;

alter table o_repositorymetadata drop column lastmodified;
alter table o_repositorymetadata add column version bigint;
update o_repositorymetadata set version=0;
alter table o_repositorymetadata modify version bigint not null;

alter table o_user drop column lastmodified;
alter table o_user add column version bigint;
update o_user set version=0;
alter table o_user modify version bigint not null;

alter table o_gp_bgcontextresource_rel drop column lastmodified;
alter table o_gp_bgcontextresource_rel add column version bigint;
update o_gp_bgcontextresource_rel set version=0;
alter table o_gp_bgcontextresource_rel modify version bigint not null;

alter table o_gp_bgtoarea_rel drop column lastmodified;
alter table o_gp_bgtoarea_rel add column version bigint;
update o_gp_bgtoarea_rel set version=0;
alter table o_gp_bgtoarea_rel modify version bigint not null;

alter table o_bs_policy drop column lastmodified;
alter table o_bs_policy add column version bigint;
update o_bs_policy set version=0;
alter table o_bs_policy modify version bigint not null;

alter table o_gp_bgarea drop column lastmodified;
alter table o_gp_bgarea add column version bigint;
update o_gp_bgarea set version=0;
alter table o_gp_bgarea modify version bigint not null;

alter table o_bookmark drop column lastmodified;
alter table o_bookmark add column version bigint;
update o_bookmark set version=0;
alter table o_bookmark modify version bigint not null;

alter table o_lifecycle drop column lastmodified;
alter table o_lifecycle add column version bigint;
update o_lifecycle set version=0;
alter table o_lifecycle modify version bigint not null;

alter table oc_lock drop column lastmodified;
alter table oc_lock add column version bigint;
update oc_lock set version=0;
alter table oc_lock modify version bigint not null;

alter table o_readmessage drop column lastmodified;
alter table o_readmessage add column version bigint;
update o_readmessage set version=0;
alter table o_readmessage modify version bigint not null;

alter table o_lifecycle change uservalue uservalue text;

drop index identity_forum_idx on o_readmessage;
create index readmessage_forum_idx on o_readmessage (forum_id);
create index readmessage_identity_idx on o_readmessage (identity_id);
create index id_idx on o_olatresource (resid);


SET FOREIGN_KEY_CHECKS = 1;

