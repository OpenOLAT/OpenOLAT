alter table o_bs_authentication add column lastmodified timestamp;
update o_bs_authentication set lastmodified=creationdate;
alter table o_bs_authentication alter column lastmodified set not null;
