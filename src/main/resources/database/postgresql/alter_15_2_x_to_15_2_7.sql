-- Repository
drop index descritpion_idx;
alter table o_repositoryentry alter column external_ref type varchar(255);