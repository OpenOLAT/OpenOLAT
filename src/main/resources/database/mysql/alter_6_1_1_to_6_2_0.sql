alter table o_repositoryentry add column statuscode integer;
update o_repositoryentry set statuscode=1;

alter table o_repositoryentry modify displayname varchar(110);
