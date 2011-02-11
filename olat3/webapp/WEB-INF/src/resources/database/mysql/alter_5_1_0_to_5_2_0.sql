SET FOREIGN_KEY_CHECKS = 0;

# add new column lastusage 
alter table o_repositoryentry add column lastusage datetime after creationdate;
# add new column status 
alter table o_bs_identity add column status integer after name;
# initilize existing value
update o_bs_identity set status = '2';

create table o_lifecycle (
   id bigint not null,
   lastmodified datetime not null,
   creationdate datetime,
   persistenttypename varchar(50) not null,
   persistentref bigint not null,
   action varchar(50) not null,
   lctimestamp datetime,
   uservalue varchar(255),
   primary key (id)
);
alter table o_lifecycle type = InnoDB;
create index lc_pref_idx on o_lifecycle (persistentref);
create index lc_type_idx on o_lifecycle (persistenttypename);
create index lc_action_idx on o_lifecycle (action);
create index identstatus_idx on o_bs_identity (status);

# add new column statuscode for message
alter table o_message add column statuscode int after forum_fk;
update o_message set statuscode='0';

SET FOREIGN_KEY_CHECKS = 1;
