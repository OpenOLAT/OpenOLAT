-- infoMessage
alter table o_info_message add column recipientmodeindividual boolean not null default 0;
alter table o_info_message add column notificationmodewithmail boolean not null default 0;