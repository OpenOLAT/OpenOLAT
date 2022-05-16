

-- Access
create table o_ac_offer_to_organisation (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  fk_offer int8 not null,
  fk_organisation int8 not null,
  primary key (id)
);

alter table o_ac_offer_to_organisation add constraint rel_oto_offer_idx foreign key (fk_offer) references o_ac_offer(offer_id);
create index idx_rel_oto_offer_idx on o_ac_offer_to_organisation (fk_offer);
alter table o_ac_offer_to_organisation add constraint rel_oto_org_idx foreign key (fk_organisation) references o_org_organisation(id);
create index idx_rel_oto_org_idx on o_ac_offer_to_organisation (fk_organisation);

alter table o_ac_offer add open_access bool default false not null;
alter table o_ac_offer add guest_access bool default false not null;
alter table o_ac_offer add catalog_publish bool default false not null;
alter table o_ac_offer add catalog_web_publish bool default false not null;

create index idx_offer_guest_idx on o_ac_offer (guest_access);
create index idx_offer_open_idx on o_ac_offer (open_access);

alter table o_repositoryentry add publicvisible bool default false not null;
