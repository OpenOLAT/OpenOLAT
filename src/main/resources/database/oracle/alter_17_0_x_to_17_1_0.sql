
-- Catalog
create table o_ca_launcher_to_organisation (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   fk_launcher number(20) not null,
   fk_organisation number(20) not null,
   primary key (id)
);

alter table o_ca_launcher_to_organisation add constraint rel_lto_launcher_idx foreign key (fk_launcher) references o_ca_launcher(id);
create index idx_rel_lto_launcher_idx on o_ca_launcher_to_organisation (fk_launcher);
alter table o_ca_launcher_to_organisation add constraint rel_lto_org_idx foreign key (fk_organisation) references o_org_organisation(id);
create index idx_rel_lto_org_idx on o_ca_launcher_to_organisation (fk_organisation);

