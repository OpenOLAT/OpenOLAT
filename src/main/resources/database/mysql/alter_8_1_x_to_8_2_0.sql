-- relation groups to resources
create table if not exists o_gp_business_to_resource (
   g_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   fk_resource bigint not null,
   fk_group bigint not null,
   primary key (g_id)
);
alter table o_gp_business_to_resource add constraint idx_bgp_to_rsrc_rsrc foreign key (fk_resource) references o_olatresource (resource_id);
alter table o_gp_business_to_resource add constraint idx_bgp_to_rsrc_group foreign key (fk_group) references o_gp_business (group_id);

-- groups
alter table o_gp_business add column fk_resource bigint unique default null;

-- area
alter table o_gp_bgarea modify groupcontext_fk bigint;
alter table o_gp_bgarea add column fk_resource bigint default null;
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