

-- Access
create table o_ac_offer_to_organisation (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  fk_offer bigint not null,
  fk_organisation bigint not null,
  primary key (id)
);
alter table o_ac_offer_to_organisation ENGINE = InnoDB;

alter table o_ac_offer_to_organisation add constraint rel_oto_offer_idx foreign key (fk_offer) references o_ac_offer (offer_id);
alter table o_ac_offer_to_organisation add constraint rel_oto_org_idx foreign key (fk_organisation) references o_org_organisation (id);

alter table o_ac_offer add column open_access bool default false not null;
alter table o_ac_offer add column guest_access bool default false not null;
alter table o_ac_offer add column catalog_publish bool default false not null;
alter table o_ac_offer add column catalog_web_publish bool default false not null;

create index idx_offer_guest_idx on o_ac_offer (guest_access);
create index idx_offer_open_idx on o_ac_offer (open_access);

alter table o_repositoryentry add column publicvisible bool default false not null;
