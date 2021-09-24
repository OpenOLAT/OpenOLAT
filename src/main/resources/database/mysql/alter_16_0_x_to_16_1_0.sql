-- Business group 
alter table o_gp_business add column status varchar(32) default 'active';

alter table o_gp_business add column inactivationdate datetime;
alter table o_gp_business add column inactivationemaildate datetime;
alter table o_gp_business add column reactivationdate datetime;

alter table o_gp_business add column softdeleteemaildate datetime;
alter table o_gp_business add column softdeletedate datetime;

alter table o_gp_business add column fk_inactivatedby_id bigint;
alter table o_gp_business add column fk_softdeletedby_id bigint;

alter table o_gp_business add constraint gb_bus_inactivateby_idx foreign key (fk_inactivatedby_id) references o_bs_identity (id);

alter table o_gp_business add constraint gb_bus_softdeletedby_idx foreign key (fk_softdeletedby_id) references o_bs_identity (id);

