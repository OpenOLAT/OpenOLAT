
-- Catalog
create table o_ca_launcher_to_organisation (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  fk_launcher bigint not null,
  fk_organisation bigint not null,
  primary key (id)
);
alter table o_ca_launcher_to_organisation ENGINE = InnoDB;

alter table o_ca_launcher_to_organisation add constraint rel_lto_launcher_idx foreign key (fk_launcher) references o_ca_launcher (id);
alter table o_ca_launcher_to_organisation add constraint rel_lto_org_idx foreign key (fk_organisation) references o_org_organisation (id);

