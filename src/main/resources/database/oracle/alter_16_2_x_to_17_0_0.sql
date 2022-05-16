

-- Access
create table o_ac_offer_to_organisation (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  fk_offer number(20) not null,
  fk_organisation number(20) not null,
  primary key (id)
);

alter table o_ac_offer_to_organisation add constraint rel_oto_offer_idx foreign key (fk_offer) references o_ac_offer(offer_id);
create index idx_rel_oto_offer_idx on o_ac_offer_to_organisation (fk_offer);
alter table o_ac_offer_to_organisation add constraint rel_oto_org_idx foreign key (fk_organisation) references o_org_organisation(id);
create index idx_rel_oto_org_idx on o_ac_offer_to_organisation (fk_organisation);

alter table o_ac_offer add open_access number default 0 not null;
alter table o_ac_offer add guest_access number default 0 not null;
alter table o_ac_offer add catalog_publish number default 0 not null;
alter table o_ac_offer add catalog_web_publish number default 0 not null;

create index idx_offer_guest_idx on o_ac_offer (guest_access);
create index idx_offer_open_idx on o_ac_offer (open_access);

alter table o_repositoryentry add publicvisible number default 0 not null;
