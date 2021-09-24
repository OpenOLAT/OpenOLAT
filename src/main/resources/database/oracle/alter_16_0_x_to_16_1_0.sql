-- Business group 
alter table o_gp_business add status varchar(32) default 'active';

alter table o_gp_business add inactivationdate date;
alter table o_gp_business add inactivationemaildate date;
alter table o_gp_business add reactivationdate date;

alter table o_gp_business add softdeleteemaildate date;
alter table o_gp_business add softdeletedate date;

alter table o_gp_business add fk_inactivatedby_id number(20);
alter table o_gp_business add fk_softdeletedby_id number(20);

alter table o_gp_business add constraint gb_bus_inactivateby_idx foreign key (fk_inactivatedby_id) references o_bs_identity (id);
create index idx_gb_bus_inactivateby_idx on o_gp_business (fk_inactivatedby_id);

alter table o_gp_business add constraint gb_bus_softdeletedby_idx foreign key (fk_softdeletedby_id) references o_bs_identity (id);
create index idx_gb_bus_softdeletedby_idx on o_gp_business (fk_softdeletedby_id);

