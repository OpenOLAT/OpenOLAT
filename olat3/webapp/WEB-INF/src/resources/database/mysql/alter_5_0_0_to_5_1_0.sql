SET FOREIGN_KEY_CHECKS = 0;

# add new canReference flag to repository
alter table o_repositoryentry add column canreference bit not null after cancopy;

# add new column lastlogin 
alter table o_bs_identity add column lastlogin datetime after creationdate;

SET FOREIGN_KEY_CHECKS = 1;
