-- salted password
alter table o_bs_authentication add (salt varchar(255));
alter table o_bs_authentication add (hashalgorithm varchar(16));


