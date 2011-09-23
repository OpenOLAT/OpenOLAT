alter table o_repositoryentry add column lastusage timestamp;
alter table o_bs_identity add column status integer;
update o_bs_identity set status = '2';

create table o_lifecycle (
   id bigint not null,
   lastmodified timestamp not null,
   creationdate timestamp,
   persistenttypename varchar(50) not null,
   persistentref bigint not null,
   action varchar(50) not null,
   lctimestamp timestamp,
   uservalue varchar(255),
   primary key (id)
);
create index lc_pref_idx on o_lifecycle (persistentref);
create index lc_type_idx on o_lifecycle (persistenttypename);
create index lc_action_idx on o_lifecycle (action);
create index identstatus_idx on o_bs_identity (status);
alter table o_message add column statuscode int;
update o_message set statuscode='0';
