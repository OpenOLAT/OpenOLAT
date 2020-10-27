-- Repository
drop index descritpion_idx;
alter table o_repositoryentry modify external_ref varchar2(255 char);
