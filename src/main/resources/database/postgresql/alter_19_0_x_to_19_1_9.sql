-- infoMessage
alter table o_info_message add column recipientmodeindividual bool not null default false;
alter table o_info_message add column notificationmodewithmail bool not null default false;