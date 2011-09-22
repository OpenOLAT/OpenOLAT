create table tmp_o_gp_business (
   group_id int8 not null,
   lastmodified timestamp not null,
   creationdate timestamp,
   lastusage timestamp,
   businessgrouptype varchar(15) not null,
   groupname varchar(128),
   descr text,
   minparticipants int4,
   maxparticipants int4,
   waitinglist_enabled bool,
   autocloseranks_enabled bool,
   groupcontext_fk int8,
   fk_ownergroup int8 unique,
   fk_partipiciantgroup int8 unique,
   fk_waitinggroup int8 unique,
   primary key (group_id)
);

insert into tmp_o_gp_business
( group_id,lastmodified,creationdate,lastusage,businessgrouptype, groupname,descr,minparticipants,maxparticipants,
   groupcontext_fk, fk_ownergroup, fk_partipiciantgroup
)
select group_id,lastmodified,creationdate,lastusage,businessgrouptype, groupname,descr,minparticipants,maxparticipants,
   groupcontext_fk, fk_ownergroup, fk_partipiciantgroup
from o_gp_business;

alter table o_gp_business drop constraint FKCEEB8A86DF6BCD14;
alter table o_gp_business drop constraint FKCEEB8A86A1FAC766;
alter table o_gp_business drop constraint FKCEEB8A86C06E3EF3;
alter table o_property drop constraint FKB60B1BA5190E5;
alter table o_gp_bgtoarea_rel drop constraint FK9B663F2D1E2E7685;

drop table o_gp_business;
ALTER TABLE tmp_o_gp_business RENAME TO o_gp_business;

alter table o_gp_business add constraint FKCEEB8A86DF6BCD14 foreign key (groupcontext_fk) references o_gp_bgcontext;
alter table o_gp_business add constraint FKCEEB8A86A1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup;
alter table o_gp_business add constraint FKCEEB8A86C06E3EF3 foreign key (fk_partipiciantgroup) references o_bs_secgroup;
alter table o_property add constraint FKB60B1BA5190E5 foreign key (grp) references o_gp_business;
alter table o_gp_bgtoarea_rel add constraint FK9B663F2D1E2E7685 foreign key (group_fk) references o_gp_business;
-- initilize existing value
update o_gp_business set waitinglist_enabled = 'FALSE' ;
update o_gp_business set autocloseranks_enabled = 'FALSE' ;

