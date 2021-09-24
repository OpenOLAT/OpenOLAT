-- Business group 
alter table o_gp_business add column status varchar(32) default 'active';

alter table o_gp_business add column inactivationdate timestamp;
alter table o_gp_business add column inactivationemaildate timestamp;
alter table o_gp_business add column reactivationdate timestamp;

alter table o_gp_business add column softdeleteemaildate timestamp;
alter table o_gp_business add column softdeletedate timestamp;

alter table o_gp_business add column fk_inactivatedby_id int8;
alter table o_gp_business add column fk_softdeletedby_id int8;

alter table o_gp_business add constraint gb_bus_inactivateby_idx foreign key (fk_inactivatedby_id) references o_bs_identity (id);
create index idx_gb_bus_inactivateby_idx on o_gp_business (fk_inactivatedby_id);

alter table o_gp_business add constraint gb_bus_softdeletedby_idx foreign key (fk_softdeletedby_id) references o_bs_identity (id);
create index idx_gb_bus_softdeletedby_idx on o_gp_business (fk_softdeletedby_id);

