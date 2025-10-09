alter table o_ac_reservation add column confirmableby varchar(32);
alter table o_ac_reservation alter column userconfirmable drop not null;