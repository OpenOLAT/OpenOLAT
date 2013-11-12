-- salted password
alter table o_bs_authentication add (salt varchar(255));
alter table o_bs_authentication add (hashalgorithm varchar(16));

alter table o_info_message add message_copy varchar2(4000);
update o_info_message set message_copy = message;
Commit;
update o_info_message set message = null;
commit;
alter table o_info_message modify message long;
alter table o_info_message modify message clob;
update o_info_message set message = message_copy;
Commit;
alter table o_info_message drop column message_copy;


