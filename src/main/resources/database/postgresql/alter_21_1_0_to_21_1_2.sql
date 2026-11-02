-- ac cancelling
alter table o_ac_offer add column cancelling_anytime bool default false not null;
alter table o_ac_order_part add column cancelling_anytime bool default false not null;
alter table o_ac_order_line add column cancelling_anytime bool default false not null;
alter table o_ac_offer alter column cancelling_enabled set default false;
alter table o_ac_order_part alter column cancelling_enabled set default false;
alter table o_ac_order_line alter column cancelling_enabled set default false;
