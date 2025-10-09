alter table o_ac_reservation add confirmableby varchar(32);
alter table o_ac_reservation modify (userconfirmable null);