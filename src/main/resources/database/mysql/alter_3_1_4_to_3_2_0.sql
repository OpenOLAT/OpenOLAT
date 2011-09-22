SET FOREIGN_KEY_CHECKS = 0;

# new preference field for instant messaging
alter table o_user add presencemessagespublic bit after fontsize;
# use valid value for existing users
update o_user set presencemessagespublic = 0;

# new table for notes of users
create table o_note (
   note_id bigint not null,
   lastmodified datetime not null,
   creationdate datetime,
   owner_id bigint,
   resourcetypename varchar(255) not null,
   resourcetypeid bigint not null,
   sub_type varchar(255),
   notetitle varchar(255),
   notetext text,
   primary key (note_id)
);

alter table o_note type = InnoDB;
create index resid_idx on o_note (resourceTypeId);
create index owner_idx on o_note (owner_id);
create index restype_idx on o_note (resourceTypeName);
alter table o_note add index FKC2D855C263219E27 (owner_id), add constraint FKC2D855C263219E27 foreign key (owner_id) references o_bs_identity (id);

# modifying field name of table o_property from 'null' to 'not null'
alter table o_property modify name varchar(255) not null;

# new field for duration in o_qtiresultset and o_qtiresult
alter table o_qtiresultset add duration bigint after creationdate;
alter table o_qtiresult add duration bigint after tstamp;

# new table o_noti_pub
create table o_noti_pub (
   publisher_id bigint not null,
   lastmodified datetime not null,
   creationdate datetime,
   publishertype varchar(50) not null,
   data text,
   resname varchar(50),
   resid bigint,
   subident varchar(128),
   state integer,
   latestnews datetime not null,
   primary key (publisher_id)
);
alter table o_noti_pub type = InnoDB;
create index notif_type_idx on o_noti_pub (publishertype);
create index name_idx on o_noti_pub (resname);


# new table o_noti_sub
create table o_noti_sub (
   publisher_id bigint not null,
   lastmodified datetime not null,
   creationdate datetime,
   fk_publisher bigint not null,
   fk_identity bigint not null,
   latestread datetime not null,
   transresid varchar(255),
   transsubidentifier varchar(255),
   data text,
   primary key (publisher_id),
   unique (fk_publisher, fk_identity)
);
alter table o_noti_sub type = InnoDB;
alter table o_noti_sub add index FK4FB8F04749E53702 (fk_publisher), add constraint FK4FB8F04749E53702 foreign key (fk_publisher) references o_noti_pub (publisher_id);
alter table o_noti_sub add index FK4FB8F0476B1F22F8 (fk_identity), add constraint FK4FB8F0476B1F22F8 foreign key (fk_identity) references o_bs_identity (id);

# modifying some field names from upper to lower case 
alter table o_property change resourceTypeName resourcetypename varchar(255);
alter table o_property change resourceTypeId resourcetypeid bigint;

alter table o_catentry change externalURL externalurl varchar(255);

alter table o_repositoryentry change initialAuthor initialauthor varchar(255) not null;
alter table o_repositoryentry change canLaunch canlaunch bit not null;
alter table o_repositoryentry change canDownload candownload bit not null;
alter table o_repositoryentry change canCopy cancopy bit not null;
alter table o_repositoryentry change launchCounter launchcounter bigint not null;
alter table o_repositoryentry change downloadCounter downloadcounter bigint not null;

# add default context flag to business group context
alter table o_gp_bgcontext add defaultcontext bit not null after ownergroup_fk;
create index default_idx on o_gp_bgcontext (defaultcontext);
# set existing contexts to be default contexts. so far multiple contexts have not been supported
update o_gp_bgcontext set defaultcontext=1;

# group field is foreign key to business group
alter table o_property modify grp bigint;
alter table o_property drop index grp_idx;
alter table o_property add index FKB60B1BA5190E5 (grp), add constraint FKB60B1BA5190E5 foreign key (grp) references o_gp_business (group_id);


alter table o_bookmark modify column displayrestype varchar(50) not null;
alter table o_bookmark modify column olatrestype varchar(50) not null;
alter table o_gp_bgcontext modify column grouptype varchar(15) not null;
alter table o_gp_business modify column businessgrouptype varchar(15) not null;
alter table o_property modify column resourcetypename varchar(50);
alter table o_repositoryentry modify column initialauthor varchar(128) not null;
drop index descritpion_idx on o_repositoryentry;
alter table o_repositoryentry modify description text;
alter table o_catentry modify description text;
alter table o_bookmark modify description text;


SET FOREIGN_KEY_CHECKS = 1;
