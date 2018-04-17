alter table o_bs_authentication add column lastmodified datetime;
update o_bs_authentication set lastmodified=creationdate;
alter table o_bs_authentication modify lastmodified datetime not null;
