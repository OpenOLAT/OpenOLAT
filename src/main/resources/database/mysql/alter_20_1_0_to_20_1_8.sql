alter table o_ac_reservation add column confirmableby varchar(32);
alter table o_ac_reservation modify column userconfirmable bool null;