alter table o_repositoryentry add column canreference bool;
update o_repositoryentry set canreference = false;
alter table o_repositoryentry alter column canreference set not null;
alter table o_bs_identity add column lastlogin timestamp;
