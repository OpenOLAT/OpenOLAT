-- infoMessage
alter table o_info_message add column recipientmodeindividual number default 0 not null;
alter table o_info_message add column notificationmodewithmail number default 0 not null;