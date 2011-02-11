SET FOREIGN_KEY_CHECKS=0;
SET AUTOCOMMIT=0;

#
# the roles and names of the basesecurity policies at olat setup time have been consolidated
#
update o_olatresource set resname='BaseSecurityModule:RAdmins' where resname = 'Basesecurity:RAdmins';
update o_olatresource set resname='BaseSecurityModule:RAuthor' where resname = 'Basesecurity:RAuthor';
update o_olatresource set resname='BaseSecurityModule:RUsers' where resname = 'Basesecurity:RUsers';
update o_olatresource set resname='SysinfoController' where resname = 'org.olat.admin.sysinfo.SysinfoController';
update o_olatresource set resname='UserAdminController' where resname = 'org.olat.admin.user.UserAdminController';
update o_olatresource set resname='UserChangePasswordController' where resname = 'org.olat.admin.user.UserChangePasswordController';
update o_olatresource set resname='UserCreateController' where resname = 'org.olat.admin.user.UserCreateController';
update o_olatresource set resname='QuotaController' where resname = 'org.olat.admin.quota.QuotaController';
update o_olatresource set resname='ChangePasswordController' where resname = 'org.olat.user.ChangePasswordController';
update o_olatresource set resname='PersonalSettingsController' where resname = 'org.olat.user.PersonalSettingsController';
update o_olatresource set resname='BaseSecurityModule:SecGroup' where resname = 'SecGroup' and resid = 0;
update o_olatresource set resname='CourseModule' where resname = 'Course';
update o_olatresource set resname='BaseSecurityModule:RGuestOnly' where resname = 'Basesecurity:RGuestOnly';
update o_olatresource set resname='BaseSecurityModule:WHOLE-OLAT' where resname = 'Basesecurity:WHOLE-OLAT';

#
# update restype and displayrestype infos 
#
update o_bookmark set olatrestype='RepositoryEntry' where olatrestype='repoEntry';
update o_bookmark set displayrestype='CourseModule' where displayrestype='Course';

#
# add identity fk to authentication
#
alter table o_bs_authentication add (identity_fk bigint);
update o_bs_authentication a, o_bs_identity i set a.identity_fk=i.id where i.fk_auth_id=a.id;
alter table o_bs_authentication modify identity_fk bigint not null;
alter table o_bs_authentication change column hashedpassword credential varchar(255);
alter table o_bs_authentication drop index hashedpassword_idx;
alter table o_bs_authentication add index  credential_idx (credential);
# If the next uncommented line fails you need to lookup your constraint name on 'fk_auth_id' with 
# 'show create table o_bs_identity;'
# and run the next line with 'o_bs_identity_ibfk_1' replaced by your constraint name to delte
# the foreign key constraint first.
# alter table o_bs_identity drop foreign key o_bs_identity_ibfk_1;
alter table o_bs_identity drop foreign key FKFF94111CB3E3F198;
alter table o_bs_identity drop index FKFF94111CB3E3F198;
alter table o_bs_identity drop column fk_auth_id;
alter table o_bs_authentication add index FKC6A5445652595FE6 (identity_fk), add constraint FKC6A5445652595FE6 foreign key (identity_fk) references o_bs_identity (id);


#
# update resourceTypeNames in olat properties
#
update o_property set resourceTypeName = 'Quota' where resourceTypeName = 'org.olat.Quota';
update o_property set resourceTypeName = 'BusinessGroup' where resourceTypeName = 'org.olat.group.BusinessGroupImpl';
update o_property set resourceTypeName = 'BusinessGroup' where resourceTypeName = 'BusinessGroupImpl';
update o_property set resourceTypeName = 'Forum' where resourceTypeName = 'forum';
update o_property set resourceTypeName = 'RepositoryEntry' where resourceTypeName = 'repoEntry';
update o_property set resourceTypeName = 'CourseModule' where resourceTypeName = 'Course';

#
# changed timestamp definition on qti results table
#
alter table o_qtiresult change column tstamp tstamp datetime not null;

#
# create index on olat resource
#
create index name_idx on o_olatresource (resname);

#
# drop old learning group tables
# WARNING: all leanring group definitions will be lost! 3.1 has a complete new 
# groupmanagement. Make sure you you don't need your 3.0 groups anymore!!
#
drop table o_gm_areagroupmembership ;
drop table o_gm_learningarea ;
drop table o_gm_learningcontext ;
drop table o_gm_learninggroup ;
drop table o_gm_right ;
drop table o_bg_buddygroups;

#
# add business group
#
create table o_gp_bgcontext (
   groupcontext_id bigint not null,
   lastmodified datetime not null,
   creationdate datetime,
   name varchar(255) not null,
   descr text,
   grouptype varchar(255) not null,
   ownergroup_fk bigint unique,
   primary key (groupcontext_id)
);
create table o_gp_bgcontextresource_rel (
   groupcontextresource_id bigint not null,
   lastmodified datetime not null,
   creationdate datetime,
   oresource_id bigint not null,
   groupcontext_fk bigint not null,
   primary key (groupcontextresource_id)
);
create table o_gp_business (
   group_id bigint not null,
   lastmodified datetime not null,
   creationdate datetime,
   lastusage datetime,
   businessgrouptype varchar(128),
   groupname varchar(128),
   descr text,
   minparticipants integer,
   maxparticipants integer,
   groupcontext_fk bigint,
   fk_ownergroup bigint unique,
   fk_partipiciantgroup bigint unique,
   primary key (group_id)
);
create table o_gp_bgtoarea_rel (
   bgtoarea_id bigint not null,
   lastmodified datetime not null,
   creationdate datetime,
   group_fk bigint not null,
   area_fk bigint not null,
   primary key (bgtoarea_id),
   unique (group_fk, area_fk)
);
create table o_gp_bgarea (
   area_id bigint not null,
   lastmodified datetime not null,
   creationdate datetime,
   name varchar(255) not null,
   descr text,
   groupcontext_fk bigint not null,
   primary key (area_id)
);

alter table o_gp_business type = InnoDB;
alter table o_gp_bgcontextresource_rel type = InnoDB;
alter table o_gp_bgcontext type = InnoDB;
alter table o_gp_bgarea type = InnoDB;
alter table o_gp_bgtoarea_rel type = InnoDB;

create index type_idx on o_gp_bgcontext (grouptype);
create index name_idx on o_gp_bgcontext (name);
alter table o_gp_bgcontext add index FK1C154FC47E4A0638 (ownergroup_fk), add constraint FK1C154FC47E4A0638 foreign key (ownergroup_fk) references o_bs_secgroup (id);
alter table o_gp_bgcontextresource_rel add index FK9903BEAC9F9C3F1D (oresource_id), add constraint FK9903BEAC9F9C3F1D foreign key (oresource_id) references o_olatresource (resource_id);
alter table o_gp_bgcontextresource_rel add index FK9903BEACDF6BCD14 (groupcontext_fk), add constraint FK9903BEACDF6BCD14 foreign key (groupcontext_fk) references o_gp_bgcontext (groupcontext_id);
create index gp_name_idx on o_gp_business (groupname);
create index gp_type_idx on o_gp_business (businessgrouptype);
alter table o_gp_business add index FKCEEB8A86DF6BCD14 (groupcontext_fk), add constraint FKCEEB8A86DF6BCD14 foreign key (groupcontext_fk) references o_gp_bgcontext (groupcontext_id);
alter table o_gp_business add index FKCEEB8A86A1FAC766 (fk_ownergroup), add constraint FKCEEB8A86A1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup (id);
alter table o_gp_business add index FKCEEB8A86C06E3EF3 (fk_partipiciantgroup), add constraint FKCEEB8A86C06E3EF3 foreign key (fk_partipiciantgroup) references o_bs_secgroup (id);
alter table o_gp_bgtoarea_rel add index FK9B663F2D1E2E7685 (group_fk), add constraint FK9B663F2D1E2E7685 foreign key (group_fk) references o_gp_business (group_id);
alter table o_gp_bgtoarea_rel add index FK9B663F2DD381B9B7 (area_fk), add constraint FK9B663F2DD381B9B7 foreign key (area_fk) references o_gp_bgarea (area_id);
create index name_idx on o_gp_bgarea (name);
alter table o_gp_bgarea add index FK9EFAF698DF6BCD14 (groupcontext_fk), add constraint FK9EFAF698DF6BCD14 foreign key (groupcontext_fk) references o_gp_bgcontext (groupcontext_id);


#
# add catalog tables - draft
#
create table o_catentry (
   message_id bigint not null,
   lastmodified datetime not null,
   creationdate datetime,
   name varchar(100) not null,
   description varchar(255),
   externalURL varchar(255),
   fk_olatresource bigint,
   fk_ownergroup bigint not null unique,
   type integer not null,
   parent_id bigint,
   primary key (message_id)
);
alter table o_catentry type = InnoDB;
alter table o_catentry add index FKF4433C2C7B66B0D0 (parent_id), add constraint FKF4433C2C7B66B0D0 foreign key (parent_id) references o_catentry (message_id);
alter table o_catentry add index FKF4433C2CA1FAC766 (fk_ownergroup), add constraint FKF4433C2CA1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup (id);
alter table o_catentry add index FKF4433C2C88C31018 (fk_olatresource), add constraint FKF4433C2C88C31018 foreign key (fk_olatresource) references o_olatresource (resource_id);

COMMIT;
SET FOREIGN_KEY_CHECKS=1;