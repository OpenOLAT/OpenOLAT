SET FOREIGN_KEY_CHECKS = 0;

--
-- new table to store the users properties
create table o_userproperty (
   fk_user_id bigint not null,
   propname varchar(255) not null,
   propvalue varchar(255),
   primary key (fk_user_id, propname)
);
alter table o_userproperty type = InnoDB;
create index propvalue_idx on o_userproperty (propvalue);
alter table o_userproperty add index FK4B04D83FD1A80C95 (fk_user_id), add constraint FK4B04D83FD1A80C95 foreign key (fk_user_id) references o_user (user_id);

--
-- migrate data from old user table to new userproperty table
-- make sure you get this right the first time, you can not run this twice!
insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "firstName", firstname from o_user where firstname IS NOT NULL AND firstname!="";
alter table o_user drop firstname; 

insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "lastName", lastname from o_user where lastname IS NOT NULL AND lastname!="";
alter table o_user drop lastname; 

insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "email", email from o_user where email IS NOT NULL AND email!="";
alter table o_user drop email; 

insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "birthDay", birthday from o_user where birthday IS NOT NULL AND birthday!="";
alter table o_user drop birthday; 

insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "gender", gender from o_user where gender IS NOT NULL AND gender!="";
alter table o_user drop gender; 

insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "telMobile", telmobile from o_user where telmobile IS NOT NULL AND telmobile!="";
alter table o_user drop telmobile; 

insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "telOffice", teloffice from o_user where teloffice IS NOT NULL AND teloffice!="";
alter table o_user drop teloffice; 

insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "telPrivate", telprivate from o_user where telprivate IS NOT NULL AND telprivate!="";
alter table o_user drop telprivate; 

insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "institutionalUserIdentifier", institutionaluseridentifier from o_user where institutionaluseridentifier IS NOT NULL AND institutionaluseridentifier!="";
alter table o_user drop institutionaluseridentifier; 

insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "institutionalName", institutionalname from o_user where institutionalname IS NOT NULL AND institutionalname!="";
alter table o_user drop institutionalname; 

insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "institutionalEmail", institutionalemail from o_user where institutionalemail IS NOT NULL AND institutionalemail!="";
alter table o_user drop institutionalemail; 

insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "street", street from o_user where street IS NOT NULL AND street!="";
alter table o_user drop street; 

insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "extendedAddress", extendedaddress from o_user where extendedaddress IS NOT NULL AND extendedaddress!="";
alter table o_user drop extendedaddress; 

insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "poBox", pobox from o_user where pobox IS NOT NULL AND pobox!="";
alter table o_user drop pobox; 

insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "zipCode", zipcode from o_user where zipcode IS NOT NULL AND zipcode!="";
alter table o_user drop zipcode; 

insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "region", region from o_user where region IS NOT NULL AND region!="";
alter table o_user drop region; 

insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "city", city from o_user where city IS NOT NULL AND city!="";
alter table o_user drop city; 

insert into o_userproperty (fk_user_id, propname, propvalue) select user_id, "country", country from o_user where country IS NOT NULL AND country!="";
alter table o_user drop country; 
--
-- end user properties migration

-- new table to store the read messages, migrate the read messages from the o_property to the new table

create table o_readmessage (
	id bigint not null, 
	lastmodified datetime not null, 
	creationdate datetime,
	identity_id bigint not null, 
	forum_id bigint not null, 
	message_id bigint not null, 
	primary key (id),
	INDEX identity_forum_idx (identity_id, forum_id));
alter table o_readmessage type = InnoDB;

insert into o_readmessage (id, lastmodified, creationdate, identity_id, forum_id, message_id) select id, lastmodified, creationdate, identity, resourcetypeid, longvalue from o_property where category='rvst';

delete from o_property where category='rvst';

-- end read messages

-- update fontsize to new relative style
update o_user set fontsize = '110' where fontsize = 'large';
update o_user set fontsize = '100' where fontsize = 'normal';
update o_user set fontsize = '90' where fontsize = 'small';
-- end fontsize update

-- change size of a varchar title column from 100 to 300
ALTER TABLE o_message MODIFY title varchar(300); 
-- end change size of a varchar title column

-- create cluster lock table
create table oc_lock (
	lock_id bigint not null, 
	lastmodified datetime not null, 
	creationdate datetime, 
	identity_fk bigint not null, 
	asset varchar(120) not null unique, 
	primary key (lock_id)
);
create index ocl_asset_idx on oc_lock (asset);
alter table oc_lock add index FK9E30F4B66115906D (identity_fk), add constraint FK9E30F4B66115906D foreign key (identity_fk) references o_bs_identity (id);
alter table oc_lock type = InnoDB;
-- end cluster lock 

SET FOREIGN_KEY_CHECKS = 1;

-- change size of groupname column from varchar 128 to 255
ALTER TABLE o_gp_business MODIFY groupname varchar(255);
-- end change size of groupname column
