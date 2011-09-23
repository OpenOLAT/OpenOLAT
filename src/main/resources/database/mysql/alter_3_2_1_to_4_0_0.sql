SET FOREIGN_KEY_CHECKS = 0;

# new column for subscriptions
alter table o_noti_sub add column latestemailed datetime after latestread;

SET FOREIGN_KEY_CHECKS = 1;
