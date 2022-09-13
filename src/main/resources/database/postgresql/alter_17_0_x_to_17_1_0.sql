
-- Catalog
create table o_ca_launcher_to_organisation (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   fk_launcher int8 not null,
   fk_organisation int8 not null,
   primary key (id)
);

alter table o_ca_launcher_to_organisation add constraint rel_lto_launcher_idx foreign key (fk_launcher) references o_ca_launcher(id);
create index idx_rel_lto_launcher_idx on o_ca_launcher_to_organisation (fk_launcher);
alter table o_ca_launcher_to_organisation add constraint rel_lto_org_idx foreign key (fk_organisation) references o_org_organisation(id);
create index idx_rel_lto_org_idx on o_ca_launcher_to_organisation (fk_organisation);


-- External user
alter table o_bs_invitation add column i_status varchar(32) default 'active';


-- Business group
alter table o_gp_business add column excludeautolifecycle bool default false not null;