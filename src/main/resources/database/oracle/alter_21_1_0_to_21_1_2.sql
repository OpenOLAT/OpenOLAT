-- ac cancelling
alter table o_ac_offer add cancelling_anytime number default 0 not null;
alter table o_ac_order_part add cancelling_anytime number default 0 not null;
alter table o_ac_order_line add cancelling_anytime number default 0 not null;
alter table o_ac_offer modify cancelling_enabled default 0;
alter table o_ac_order_part modify cancelling_enabled default 0;
alter table o_ac_order_line modify cancelling_enabled default 0;
