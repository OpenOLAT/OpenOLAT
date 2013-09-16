-- salted password
alter table o_bs_authentication add column salt varchar(255) default null;
alter table o_bs_authentication add column hashalgorithm varchar(16) default null;


alter table o_info_message modify message longtext;
