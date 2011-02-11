-- add column statuscode to table o_repositoryentry
alter table o_repositoryentry add column statuscode int4;
update o_repositoryentry set statuscode=1;
