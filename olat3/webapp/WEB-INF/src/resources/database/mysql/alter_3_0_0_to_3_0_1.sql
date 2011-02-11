##################
# change column name. timestamp is a reserverd sql keyword
alter table o_qtiresult change timestamp tstamp timestamp not null;

##################
# Change problematic float definition to a decimal definition. When storing a long in a float
# the retrieved long is not the same as the one put when bigger than 1000000
# 1) get already stored properties, save them in a textfile
select concat('update o_property set floatvalue=\'',round(floatvalue,5),'\' where id=\'',id,'\';') from o_property where floatvalue is not null into outfile '/tmp/alter_floatvalue.sql';

# 2) Alter the table definition
alter table o_property modify floatvalue decimal(78,36);

# 3) now manually at command line:
# mysql -u olat -p olat < /tmp/alter_floatvalue.sql

# 4) Change the same problematic float fields in qtiresult and qtiresultset
# No data will be migrated, it is very unlikely that a user has a score that is bigger than 1000000...
alter table o_qtiresult modify score decimal(78,36);
alter table o_qtiresultset modify score decimal(78,36);

##
## BuddyGroup related stuff
##
drop table if exists o_bg_buddygroups ;
--
create table o_bg_buddygroups (
   group_id BIGINT not null,
   lastmodified DATETIME not null,
   creationdate DATETIME,
   lastusage DATETIME,
   groupname VARCHAR(128),
   descr text,
   intromessage text,
   fk_ownergroup BIGINT unique,
   fk_partipiciantgroup BIGINT unique,
   primary key (group_id)
) ;
--
alter table o_bg_buddygroups type = InnoDB;
--
create index bg_name_idx on o_bg_buddygroups (groupname);
alter table o_bg_buddygroups add index FK4A14ACF0C06E3EF3 (fk_partipiciantgroup), add constraint FK4A14ACF0C06E3EF3 foreign key (fk_partipiciantgroup) references o_bs_secgroup (id) ;
alter table o_bg_buddygroups add index FK4A14ACF0A1FAC766 (fk_ownergroup), add constraint FK4A14ACF0A1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup (id) ;
--