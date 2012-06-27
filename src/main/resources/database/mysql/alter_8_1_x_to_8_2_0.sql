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



alter table o_gp_bgarea add column fk_resource bigint default null;
alter table o_gp_bgarea add constraint idx_area_to_resource foreign key (fk_resource) references o_olatresource (resource_id);

alter table o_gp_bgarea modify groupcontext_fk bigint;

