SET FOREIGN_KEY_CHECKS = 0;

create table if not exists o_forum (
   forum_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   primary key (forum_id)
);
create table if not exists o_property (
   id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime,
   creationdate datetime,
   identity bigint,
   grp bigint,
   resourcetypename varchar(50),
   resourcetypeid bigint,
   category varchar(33),
   name varchar(255) not null,
   floatvalue FLOAT(65,30),
   longvalue bigint,
   stringvalue varchar(255),
   textvalue longtext,
   primary key (id)
);
create table if not exists o_bs_secgroup (
   id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   primary key (id)
);
create table if not exists o_gp_business (
   group_id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime,
   creationdate datetime,
   lastusage datetime,
   businessgrouptype varchar(15) not null,
   groupname varchar(255),
   descr longtext,
   minparticipants integer,
   maxparticipants integer,
   waitinglist_enabled bit,
   autocloseranks_enabled bit,
   groupcontext_fk bigint,
   fk_ownergroup bigint unique,
   fk_partipiciantgroup bigint unique,
   fk_waitinggroup bigint unique,
   primary key (group_id)
);
create table if not exists o_temporarykey (
   reglist_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   email varchar(255) not null,
   regkey varchar(255) not null,
   ip varchar(255) not null,
   mailsent bit not null,
   action varchar(255) not null,
   primary key (reglist_id)
);
create table if not exists o_bs_authentication (
   id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   identity_fk bigint not null,
   provider varchar(8),
   authusername varchar(255),
   credential varchar(255),
   primary key (id),
   unique (provider, authusername)
);
create table if not exists o_noti_pub (
   publisher_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   publishertype varchar(50) not null,
   data longtext,
   resname varchar(50),
   resid bigint,
   subident varchar(128),
   businesspath varchar(255),
   state integer,
   latestnews datetime not null,
   primary key (publisher_id)
);
create table if not exists o_qtiresultset (
   resultset_id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime not null,
   creationdate datetime,
   identity_id bigint not null,
   olatresource_fk bigint not null,
   olatresourcedetail varchar(255) not null,
   assessmentid bigint not null,
   repositoryref_fk bigint not null,
   ispassed bit,
   score FLOAT(65,30),
   duration bigint,
   primary key (resultset_id)
);
create table if not exists o_bs_identity (
   id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   lastlogin datetime,
   name varchar(128) not null unique,
   status integer,
   fk_user_id bigint unique,
   primary key (id)
);
create table if not exists o_olatresource (
   resource_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   resname varchar(50) not null,
   resid bigint not null,
   primary key (resource_id),
   unique (resname, resid)
);
create table if not exists o_bs_namedgroup (
   id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   secgroup_id bigint not null,
   groupname varchar(16),
   primary key (id),
   unique (groupname)
);
create table if not exists o_catentry (
   id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   name varchar(110) not null,
   description longtext,
   externalurl varchar(255),
   fk_repoentry bigint,
   fk_ownergroup bigint unique,
   type integer not null,
   parent_id bigint,
   primary key (id)
);
create table if not exists o_note (
   note_id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime,
   creationdate datetime,
   owner_id bigint,
   resourcetypename varchar(50) not null,
   resourcetypeid bigint not null,
   sub_type varchar(50),
   notetitle varchar(255),
   notetext longtext,
   primary key (note_id)
);
create table if not exists o_gp_bgcontext (
   groupcontext_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   name varchar(255) not null,
   descr longtext,
   grouptype varchar(15) not null,
   ownergroup_fk bigint unique,
   defaultcontext bit not null,
   primary key (groupcontext_id)
);
create table if not exists o_references (
   reference_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   source_id bigint not null,
   target_id bigint not null,
   userdata varchar(64),
   primary key (reference_id)
);
create table if not exists o_repositorymetadata (
   metadataelement_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   name varchar(255) not null,
   value longtext not null,
   fk_repositoryentry bigint not null,
   primary key (fk_repositoryentry, metadataelement_id)
);
create table if not exists o_user (
   user_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   language varchar(30),
   fontsize varchar(10),
   notification_interval varchar(16),
   presencemessagespublic bit,
   informsessiontimeout bit not null,
   primary key (user_id)
);
create table if not exists o_userproperty (
   fk_user_id bigint not null,
   propname varchar(255) not null,
   propvalue varchar(255),
   primary key (fk_user_id, propname)
);
create table if not exists o_gp_bgcontextresource_rel (
   groupcontextresource_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   oresource_id bigint not null,
   groupcontext_fk bigint not null,
   primary key (groupcontextresource_id)
);
create table if not exists o_message (
   message_id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime,
   creationdate datetime,
   title varchar(100),
   body longtext,
   parent_id bigint,
   topthread_id bigint,
   creator_id bigint not null,
   modifier_id bigint,
   forum_fk bigint,
   statuscode integer,
   numofwords integer,
   numofcharacters integer,
   primary key (message_id)
);
create table if not exists o_gp_bgtoarea_rel (
   bgtoarea_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   group_fk bigint not null,
   area_fk bigint not null,
   primary key (bgtoarea_id),
   unique (group_fk, area_fk)
);
create table if not exists o_noti_sub (
   publisher_id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime,
   creationdate datetime,
   fk_publisher bigint not null,
   fk_identity bigint not null,
   latestemailed datetime,
   primary key (publisher_id),
   unique (fk_publisher, fk_identity)
);
create table if not exists o_qtiresult (
   result_id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime not null,
   creationdate datetime,
   itemident varchar(255) not null,
   answer longtext,
   duration bigint,
   score FLOAT(65,30),
   tstamp datetime not null,
   ip varchar(255),
   resultset_fk bigint,
   primary key (result_id)
);
create table if not exists o_bs_policy (
   id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   oresource_id bigint not null,
   group_id bigint not null,
   permission varchar(16) not null,
   apply_from datetime default null,
   apply_to datetime default null,
   primary key (id),
   unique (oresource_id, group_id, permission)
);
create table if not exists o_gp_bgarea (
   area_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   name varchar(255) not null,
   descr longtext,
   groupcontext_fk bigint not null,
   primary key (area_id)
);
create table if not exists o_repositoryentry (
   repositoryentry_id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime,
   creationdate datetime,
   lastusage datetime,
   softkey varchar(30) not null unique,
   displayname varchar(110) not null,
   resourcename varchar(100) not null,
   fk_olatresource bigint unique,
   fk_ownergroup bigint unique,
   description longtext,
   initialauthor varchar(128) not null,
   accesscode integer not null default 0,
   statuscode integer,
   canlaunch bit not null,
   candownload bit not null,
   cancopy bit not null,
   canreference bit not null,
   launchcounter bigint not null,
   downloadcounter bigint not null,
   primary key (repositoryentry_id)
);
create table if not exists o_bookmark (
   bookmark_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   owner_id bigint not null,
   title varchar(255) not null,
   description longtext,
   detaildata varchar(255),
   displayrestype varchar(50) not null,
   olatrestype varchar(50) not null,
   olatreskey bigint,
   primary key (bookmark_id)
);
create table if not exists o_bs_membership (
   id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime,
   creationdate datetime,
   secgroup_id bigint not null,
   identity_id bigint not null,
   primary key (id),
   unique (secgroup_id, identity_id)
);
create table if not exists o_plock (
    plock_id bigint not null,
	version mediumint unsigned not null,
    creationdate datetime, 
    asset varchar(255) not null unique, 
    primary key (plock_id)
);

create table if not exists hibernate_unique_key (
    next_hi integer 
);

create table if not exists o_lifecycle (
   id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   persistenttypename varchar(50) not null,
   persistentref bigint not null,
   action varchar(50) not null,
   lctimestamp datetime,
   uservalue longtext,
   primary key (id)
);

create table if not exists oc_lock (
	lock_id bigint not null, 
	version mediumint unsigned not null, 
	creationdate datetime, 
	identity_fk bigint not null, 
	asset varchar(120) not null unique, 
	primary key (lock_id)
);

create table if not exists o_readmessage (
	id bigint not null, 
	version mediumint unsigned not null, 
	creationdate datetime,
	identity_id bigint not null, 
	forum_id bigint not null, 
	message_id bigint not null, 
	primary key (id)
);

create table if not exists o_loggingtable (

	log_id bigint not null,
	creationdate datetime,
	sourceclass varchar(255),

	sessionid varchar(255) not null,
	user_id bigint,
	username varchar(255),
	userproperty1 varchar(255),
	userproperty2 varchar(255),
	userproperty3 varchar(255),
	userproperty4 varchar(255),
	userproperty5 varchar(255),
	userproperty6 varchar(255),
	userproperty7 varchar(255),
	userproperty8 varchar(255),
	userproperty9 varchar(255),
	userproperty10 varchar(255),
	userproperty11 varchar(255),
	userproperty12 varchar(255),

	actioncrudtype varchar(1) not null,
	actionverb varchar(16) not null,
	actionobject varchar(32) not null,
	simpleduration bigint not null,
	resourceadminaction boolean not null,

	businesspath varchar(2048),
	greatgrandparentrestype varchar(32),
	greatgrandparentresid varchar(64),
	greatgrandparentresname varchar(255),
	grandparentrestype varchar(32),
	grandparentresid varchar(64),
	grandparentresname varchar(255),
	parentrestype varchar(32),
	parentresid varchar(64),
	parentresname varchar(255),
	targetrestype varchar(32),
	targetresid varchar(64),
	targetresname varchar(255),
	primary key (log_id)
);

create table if not exists o_checklist (
   checklist_id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime not null,
   title varchar(255) not null,
   description longtext,
   primary key (checklist_id)
);
alter table o_checklist type = InnoDB;

create table if not exists o_checkpoint (
   checkpoint_id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime not null,
   title varchar(255) not null,
   description longtext,
   modestring varchar(64) not null,
   checklist_fk bigint,
   primary key (checkpoint_id)
);

create table if not exists o_checkpoint_results (
   checkpoint_result_id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime not null,
   result bool not null,
   checkpoint_fk bigint,
   identity_fk bigint, 
   primary key (checkpoint_result_id)
);

create table if not exists o_projectbroker (
   projectbroker_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   primary key (projectbroker_id)
);

create table if not exists o_projectbroker_project (
   project_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   title varchar(150),
   description longtext,
   state varchar(20),
   maxMembers integer,
   attachmentFileName varchar(100),
   mailNotificationEnabled boolean not null,
   projectgroup_fk bigint not null,
   projectbroker_fk bigint not null,
   candidategroup_fk bigint not null, 
   primary key (project_id)
);

create table if not exists o_projectbroker_customfields (
   fk_project_id bigint not null,
   propname varchar(255) not null,
   propvalue varchar(255),
   primary key (fk_project_id, propname)
);

create table if not exists o_usercomment (
	comment_id bigint not null, 
	version mediumint unsigned not null, 
	creationdate datetime, 
	resname varchar(50) not null, 
	resid bigint not null, 
	ressubpath varchar(2048), 
  	creator_id bigint not null,
	commenttext longtext, 
	parent_key bigint, 
	primary key (comment_id)
);
create table if not exists o_userrating (
	rating_id bigint not null, 
	version mediumint unsigned not null, 
	creationdate datetime, 
	resname varchar(50) not null, 
	resid bigint not null, 
	ressubpath varchar(2048), 
    creator_id bigint not null,
	rating integer not null, 
	primary key (rating_id)
);

create table if not exists o_stat_lastupdated (

	lastupdated datetime not null

);
-- important: initialize with old date!
insert into o_stat_lastupdated values(date('1999-01-01'));


-- insert into o_stat_dayofweek (businesspath,resid,day,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,dayofweek(creationdate) day,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,day;
create table if not exists o_stat_dayofweek (

	id bigint unsigned not null auto_increment,
	businesspath varchar(2048) not null,
	resid bigint not null,
	day int not null,
	value int not null,
	primary key (id)

);
create index statdow_resid_idx on o_stat_dayofweek (resid);


-- insert into o_stat_hourofday (businesspath,resid,hour,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,hour(creationdate) hour,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,hour;
create table if not exists o_stat_hourofday (

	id bigint unsigned not null auto_increment,
	businesspath varchar(2048) not null,
	resid bigint not null,
	hour int not null,
	value int not null,
	primary key (id)

);
create index stathod_resid_idx on o_stat_hourofday (resid);


-- insert into o_stat_weekly (businesspath,resid,week,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,concat(year(creationdate),'-',week(creationdate)) week,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,week;
create table if not exists o_stat_weekly (

	id bigint unsigned not null auto_increment,
	businesspath varchar(2048) not null,
	resid bigint not null,
	week varchar(7) not null,
	value int not null,
	primary key (id)

);
create index statwee_resid_idx on o_stat_weekly (resid);


-- insert into o_stat_daily (businesspath,resid,day,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,date(creationdate) day,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,day;
create table if not exists o_stat_daily (

	id bigint unsigned not null auto_increment,
	businesspath varchar(2048) not null,
	resid bigint not null,
	day datetime not null,
	value int not null,
	primary key (id)

);
create index statday_resid_idx on o_stat_daily (resid);


-- insert into o_stat_homeorg (businesspath,resid,homeorg,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,userproperty2 homeorg,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,homeorg;
create table if not exists o_stat_homeorg (

	id bigint unsigned not null auto_increment,
	businesspath varchar(2048) not null,
	resid bigint not null,
	homeorg varchar(255) not null,
	value int not null,
	primary key (id)

);
create index stathor_resid_idx on o_stat_homeorg (resid);


-- insert into o_stat_orgtype (businesspath,resid,orgtype,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,userproperty4 orgtype,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,orgtype;
create table if not exists o_stat_orgtype (

	id bigint unsigned not null auto_increment,
	businesspath varchar(2048) not null,
	resid bigint not null,
	orgtype varchar(255),
	value int not null,
	primary key (id)

);
create index statorg_resid_idx on o_stat_orgtype (resid);


-- insert into o_stat_studylevel (businesspath,resid,studylevel,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,userproperty3 studylevel,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,studylevel;
create table if not exists o_stat_studylevel (

	id bigint unsigned not null auto_increment,
	businesspath varchar(2048) not null,
	resid bigint not null,
	studylevel varchar(255) not null,
	value int not null,
	primary key (id)

);
create index statstl_resid_idx on o_stat_studylevel (resid);


-- insert into o_stat_studybranch3 (businesspath,resid,studybranch3,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,userproperty10 studybranch3,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,studybranch3;
create table if not exists o_stat_studybranch3 (

	id bigint unsigned not null auto_increment,
	businesspath varchar(2048) not null,
	resid bigint not null,
	studybranch3 varchar(255),
	value int not null,
	primary key (id)

);
create index statstb_resid_idx on o_stat_studybranch3 (resid);


create table if not exists o_mark (
  mark_id bigint not null,
  version mediumint unsigned not null,
  creationdate datetime,
  resname varchar(50) not null,
  resid bigint not null,
  ressubpath varchar(2048),
  businesspath varchar(2048),
  creator_id bigint not null,
  primary key (mark_id)
);

create table if not exists o_info_message (
  info_id bigint  NOT NULL,
  version mediumint NOT NULL,
  creationdate datetime,
  modificationdate datetime,
  title varchar(2048),
  message varchar(2048),
  resname varchar(50) NOT NULL,
  resid bigint NOT NULL,
  ressubpath varchar(2048),
  businesspath varchar(2048),
  fk_author_id bigint,
  fk_modifier_id bigint,
  primary key (info_id)
);


create table if not exists o_tag (
  tag_id bigint not null,
  version mediumint unsigned not null,
  creationdate datetime,
  tag varchar(128) not null,
  resname varchar(50) not null,
  resid bigint not null,
  ressubpath varchar(2048),
  businesspath varchar(2048),
  fk_author_id bigint not null,
  primary key (tag_id)
);

create table if not exists o_bs_invitation (
   id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   token varchar(64) not null,
   first_name varchar(64),
   last_name varchar(64),
   mail varchar(128),
   fk_secgroup bigint,
   primary key (id)
);

create table if not exists o_ep_artefact (
  artefact_id bigint not null,
  artefact_type varchar(32) not null,
  version mediumint unsigned not null,
  creationdate datetime,
  collection_date datetime,
  title varchar(512),
  description varchar(4000),
  signature mediumint default 0,
  businesspath varchar(2048),
  fulltextcontent longtext,
  reflexion longtext,
  source varchar(2048),
  add_prop1 varchar(2048),
  add_prop2 varchar(2048),
  add_prop3 varchar(2048),
  fk_struct_el_id bigint,
  fk_artefact_auth_id bigint not null,
  primary key (artefact_id)
);
create table if not exists o_ep_collect_restriction (
  collect_id bigint not null,
  version mediumint unsigned not null,
  creationdate datetime,
  artefact_type varchar(256),
  amount mediumint not null default -1,
  restriction varchar(32),
  pos mediumint unsigned not null default 0,
  fk_struct_el_id bigint,
  primary key (collect_id)
);
create table if not exists o_ep_struct_el (
  structure_id bigint not null,
  structure_type varchar(32) not null,
  version mediumint unsigned not null,
  creationdate datetime,
  returndate datetime default null,
  copydate datetime default null,
  lastsyncheddate datetime default null,
  deadline datetime default null,
  title varchar(512),
  description varchar(2048),
  struct_el_source bigint,
  target_resname varchar(50),
  target_resid bigint,
  target_ressubpath varchar(2048),
  target_businesspath varchar(2048),
  style varchar(128),  
  status varchar(32),
  viewmode varchar(32),
  fk_struct_root_id bigint,
  fk_struct_root_map_id bigint,
  fk_map_source_id bigint,
  fk_ownergroup bigint,
  fk_olatresource bigint not null,
  primary key (structure_id)  
);
create table if not exists o_ep_struct_struct_link (
  link_id bigint not null,
  version mediumint unsigned not null,
  creationdate datetime,
  pos mediumint unsigned not null default 0,
  fk_struct_parent_id bigint not null,
  fk_struct_child_id bigint not null,
  primary key (link_id)
);
create table if not exists o_ep_struct_artefact_link (
  link_id bigint not null,
  version mediumint unsigned not null,
  creationdate datetime,
  pos mediumint unsigned not null default 0,
  reflexion longtext,
  fk_auth_id bigint,
  fk_struct_id bigint not null,
  fk_artefact_id bigint not null,
  primary key (link_id)
);


create index  ocl_asset_idx on oc_lock (asset);
alter table oc_lock add index FK9E30F4B66115906D (identity_fk), add constraint FK9E30F4B66115906D foreign key (identity_fk) references o_bs_identity (id);

alter table hibernate_unique_key type = InnoDB;

alter table o_forum type = InnoDB;
alter table o_property type = InnoDB;
alter table o_bs_secgroup type = InnoDB;
alter table o_repositorymetadata type = InnoDB;
alter table o_user type = InnoDB;
alter table o_userproperty type = InnoDB;
alter table o_message type = InnoDB;
alter table o_temporarykey type = InnoDB;
alter table o_bs_authentication type = InnoDB;
alter table o_qtiresult type = InnoDB;
alter table o_qtiresultset type = InnoDB;
alter table o_bs_identity type = InnoDB;
alter table o_olatresource type = InnoDB;
alter table o_bs_policy type = InnoDB;
alter table o_bs_namedgroup type = InnoDB;
alter table o_bs_membership type = InnoDB;
alter table o_repositoryentry type = InnoDB;
alter table o_bookmark type = InnoDB;
alter table o_references type = InnoDB;
alter table o_gp_business type = InnoDB;
alter table o_gp_bgcontextresource_rel type = InnoDB;
alter table o_gp_bgcontext type = InnoDB;
alter table o_gp_bgarea type = InnoDB;
alter table o_gp_bgtoarea_rel type = InnoDB;
alter table o_catentry type = InnoDB;
alter table o_noti_pub type = InnoDB;
alter table o_noti_sub type = InnoDB;
alter table o_note type = InnoDB;
alter table o_lifecycle type = InnoDB;
alter table o_plock type = InnoDB;
alter table oc_lock type = InnoDB;
alter table o_readmessage type = InnoDB;
alter table o_projectbroker type = InnoDB;
alter table o_projectbroker_project type = InnoDB;
alter table o_projectbroker_customfields type = InnoDB;
alter table o_checkpoint type = InnoDB;
alter table o_checkpoint_results type = InnoDB;
alter table o_usercomment type = InnoDB;
alter table o_userrating type = InnoDB;
alter table o_mark type = InnoDB;
alter table o_info_message type = InnoDB;
alter table o_tag type = InnoDB;
alter table o_bs_invitation type = InnoDB;
alter table o_ep_artefact type = InnoDB;
alter table o_ep_collect_restriction type = InnoDB;
alter table o_ep_struct_el type = InnoDB;
alter table o_ep_struct_struct_link type = InnoDB;
alter table o_ep_struct_artefact_link type = InnoDB;


create index  resid_idx on o_property (resourcetypeid);
create index  category_idx on o_property (category);
create index  name_idx on o_property (name);
create index  restype_idx on o_property (resourcetypename);
alter table o_property add index FKB60B1BA5190E5 (grp), add constraint FKB60B1BA5190E5 foreign key (grp) references o_gp_business (group_id);
alter table o_property add index FKB60B1BA5F7E870BE (identity), add constraint FKB60B1BA5F7E870BE foreign key (identity) references o_bs_identity (id);
create index  gp_name_idx on o_gp_business (groupname);
create index  gp_type_idx on o_gp_business (businessgrouptype);
alter table o_gp_business add index FKCEEB8A86DF6BCD14 (groupcontext_fk), add constraint FKCEEB8A86DF6BCD14 foreign key (groupcontext_fk) references o_gp_bgcontext (groupcontext_id);
alter table o_gp_business add index FKCEEB8A86A1FAC766 (fk_ownergroup), add constraint FKCEEB8A86A1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup (id);
alter table o_gp_business add index FKCEEB8A86C06E3EF3 (fk_partipiciantgroup), add constraint FKCEEB8A86C06E3EF3 foreign key (fk_partipiciantgroup) references o_bs_secgroup (id);
create index  provider_idx on o_bs_authentication (provider);
create index  credential_idx on o_bs_authentication (credential);
create index  authusername_idx on o_bs_authentication (authusername);
alter table o_bs_authentication add index FKC6A5445652595FE6 (identity_fk), add constraint FKC6A5445652595FE6 foreign key (identity_fk) references o_bs_identity (id);
create index  name_idx on o_noti_pub (resname, resid, subident);
create index  oresdetindex on o_qtiresultset (olatresourcedetail);
create index  oresindex on o_qtiresultset (olatresource_fk);
create index  reprefindex on o_qtiresultset (repositoryref_fk);
create index  assindex on o_qtiresultset (assessmentid);
alter table o_qtiresultset add index FK14805D0F5259603C (identity_id), add constraint FK14805D0F5259603C foreign key (identity_id) references o_bs_identity (id);
create index  name_idx on o_bs_identity (name);
create index  identstatus_idx on o_bs_identity (status);
alter table o_bs_identity add index FKFF94111CD1A80C95 (fk_user_id), add constraint FKFF94111CD1A80C95 foreign key (fk_user_id) references o_user (user_id);
alter table o_userproperty add index FK4B04D83FD1A80C95 (fk_user_id), add constraint FK4B04D83FD1A80C95 foreign key (fk_user_id) references o_user (user_id);
create index  name_idx on o_olatresource (resname);
create index  id_idx on o_olatresource (resid);
create index  groupname_idx on o_bs_namedgroup (groupname);
alter table o_bs_namedgroup add index FKBAFCBBC4B85B522C (secgroup_id), add constraint FKBAFCBBC4B85B522C foreign key (secgroup_id) references o_bs_secgroup (id);
alter table o_catentry add index FKF4433C2C7B66B0D0 (parent_id), add constraint FKF4433C2C7B66B0D0 foreign key (parent_id) references o_catentry (id);
alter table o_catentry add index FKF4433C2CA1FAC766 (fk_ownergroup), add constraint FKF4433C2CA1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup (id);
alter table o_catentry add index FKF4433C2CDDD69946 (fk_repoentry), add constraint FKF4433C2CDDD69946 foreign key (fk_repoentry) references o_repositoryentry (repositoryentry_id);
create index  resid_idx on o_note (resourcetypeid);
create index  owner_idx on o_note (owner_id);
create index  restype_idx on o_note (resourcetypename);
alter table o_note add index FKC2D855C263219E27 (owner_id), add constraint FKC2D855C263219E27 foreign key (owner_id) references o_bs_identity (id);
create index  type_idx on o_gp_bgcontext (grouptype);
create index  default_idx on o_gp_bgcontext (defaultcontext);
create index  name_idx on o_gp_bgcontext (name);
alter table o_gp_bgcontext add index FK1C154FC47E4A0638 (ownergroup_fk), add constraint FK1C154FC47E4A0638 foreign key (ownergroup_fk) references o_bs_secgroup (id);
alter table o_references add index FKE971B4589AC44FBF (source_id), add constraint FKE971B4589AC44FBF foreign key (source_id) references o_olatresource (resource_id);
alter table o_references add index FKE971B458CF634A89 (target_id), add constraint FKE971B458CF634A89 foreign key (target_id) references o_olatresource (resource_id);
alter table o_repositorymetadata add index FKDB97A6493F14E3EE (fk_repositoryentry), add constraint FKDB97A6493F14E3EE foreign key (fk_repositoryentry) references o_repositoryentry (repositoryentry_id);
create index  propvalue_idx on o_userproperty (propvalue);
alter table o_gp_bgcontextresource_rel add index FK9903BEAC9F9C3F1D (oresource_id), add constraint FK9903BEAC9F9C3F1D foreign key (oresource_id) references o_olatresource (resource_id);
alter table o_gp_bgcontextresource_rel add index FK9903BEACDF6BCD14 (groupcontext_fk), add constraint FK9903BEACDF6BCD14 foreign key (groupcontext_fk) references o_gp_bgcontext (groupcontext_id);
alter table o_message add index FKF26C8375236F20E (creator_id), add constraint FKF26C8375236F20E foreign key (creator_id) references o_bs_identity (id);
alter table o_message add index FKF26C837A3FBEB83 (modifier_id), add constraint FKF26C837A3FBEB83 foreign key (modifier_id) references o_bs_identity (id);
alter table o_message add index FKF26C8377B66B0D0 (parent_id), add constraint FKF26C8377B66B0D0 foreign key (parent_id) references o_message (message_id);
alter table o_message add index FKF26C8378EAC1DBB (topthread_id), add constraint FKF26C8378EAC1DBB foreign key (topthread_id) references o_message (message_id);
alter table o_message add index FKF26C8371CB7C4A3 (forum_fk), add constraint FKF26C8371CB7C4A3 foreign key (forum_fk) references o_forum (forum_id);
alter table o_gp_bgtoarea_rel add index FK9B663F2D1E2E7685 (group_fk), add constraint FK9B663F2D1E2E7685 foreign key (group_fk) references o_gp_business (group_id);
alter table o_gp_bgtoarea_rel add index FK9B663F2DD381B9B7 (area_fk), add constraint FK9B663F2DD381B9B7 foreign key (area_fk) references o_gp_bgarea (area_id);
alter table o_noti_sub add index FK4FB8F04749E53702 (fk_publisher), add constraint FK4FB8F04749E53702 foreign key (fk_publisher) references o_noti_pub (publisher_id);
alter table o_noti_sub add index FK4FB8F0476B1F22F8 (fk_identity), add constraint FK4FB8F0476B1F22F8 foreign key (fk_identity) references o_bs_identity (id);
create index  itemindex on o_qtiresult (itemident);
alter table o_qtiresult add index FK3563E67340EF401F (resultset_fk), add constraint FK3563E67340EF401F foreign key (resultset_fk) references o_qtiresultset (resultset_id);
alter table o_bs_policy add index FK9A1C5109F9C3F1D (oresource_id), add constraint FK9A1C5109F9C3F1D foreign key (oresource_id) references o_olatresource (resource_id);
alter table o_bs_policy add index FK9A1C5101E2E76DB (group_id), add constraint FK9A1C5101E2E76DB foreign key (group_id) references o_bs_secgroup (id);
create index  name_idx on o_gp_bgarea (name);
alter table o_gp_bgarea add index FK9EFAF698DF6BCD14 (groupcontext_fk), add constraint FK9EFAF698DF6BCD14 foreign key (groupcontext_fk) references o_gp_bgcontext (groupcontext_id);
create index  access_idx on o_repositoryentry (accesscode);
create index  initialAuthor_idx on o_repositoryentry (initialauthor);
create index  resource_idx on o_repositoryentry (resourcename);
create index  displayname_idx on o_repositoryentry (displayname);
create index  softkey_idx on o_repositoryentry (softkey);
alter table o_repositoryentry add index FK2F9C439888C31018 (fk_olatresource), add constraint FK2F9C439888C31018 foreign key (fk_olatresource) references o_olatresource (resource_id);
alter table o_repositoryentry add index FK2F9C4398A1FAC766 (fk_ownergroup), add constraint FK2F9C4398A1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup (id);
alter table o_bookmark add index FK68C4E30663219E27 (owner_id), add constraint FK68C4E30663219E27 foreign key (owner_id) references o_bs_identity (id);
alter table o_bs_membership add index FK7B6288B45259603C (identity_id), add constraint FK7B6288B45259603C foreign key (identity_id) references o_bs_identity (id);
alter table o_bs_membership add index FK7B6288B4B85B522C (secgroup_id), add constraint FK7B6288B4B85B522C foreign key (secgroup_id) references o_bs_secgroup (id);
create index  asset_idx on o_plock (asset);
create index  lc_pref_idx on o_lifecycle (persistentref);
create index  lc_type_idx on o_lifecycle (persistenttypename);
create index  lc_action_idx on o_lifecycle (action);
create index  readmessage_forum_idx on o_readmessage (forum_id);
create index  readmessage_identity_idx on o_readmessage (identity_id);
create index  projectbroker_project_broker_idx on o_projectbroker_project (projectbroker_fk);
create index  projectbroker_project_id_idx on o_projectbroker_project (project_id);
create index  o_projectbroker_customfields_idx on o_projectbroker_customfields (fk_project_id);

alter table o_checkpoint_results add constraint FK9E30F4B661159ZZY foreign key (checkpoint_fk) references o_checkpoint (checkpoint_id) ;
alter table o_checkpoint_results add constraint FK9E30F4B661159ZZX foreign key (identity_fk) references o_bs_identity (id);
alter table o_checkpoint add constraint FK9E30F4B661159ZZZ foreign key (checklist_fk) references o_checklist (checklist_id);

create index cmt_id_idx on o_usercomment (resid);
create index cmt_name_idx on o_usercomment (resname);
create index cmt_subpath_idx on o_usercomment (ressubpath);
alter table o_usercomment add index FK92B6864A18251F0 (parent_key), add constraint FK92B6864A18251F0 foreign key (parent_key) references o_usercomment (comment_id);
alter table o_usercomment add index FKF26C8375236F20A (creator_id), add constraint FKF26C8375236F20A foreign key (creator_id) references o_bs_identity (id);
create index rtn_id_idx on o_userrating (resid);
create index rtn_name_idx on o_userrating (resname);
create index rtn_subpath_idx on o_userrating (ressubpath);
create index rtn_rating_idx on o_userrating (rating);
alter table o_userrating add index FKF26C8375236F20X (creator_id), add constraint FKF26C8375236F20X foreign key (creator_id) references o_bs_identity (id);

create index usr_notification_interval_idx on o_user (notification_interval);

create index mark_id_idx on o_mark(resid);
create index mark_name_idx on o_mark(resname);
create index mark_subpath_idx on o_mark(ressubpath);
create index mark_businesspath_idx on o_mark(businesspath);
create index FKF26C8375236F21X on o_mark(creator_id);
alter table o_mark add constraint FKF26C8375236F21X foreign key (creator_id) references o_bs_identity (id);

create index imsg_resid_idx on o_info_message (resid);
create index imsg_author_idx on o_info_message (fk_author_id);
alter table o_info_message add constraint FKF85553465A4FA5DC foreign key (fk_author_id) references o_bs_identity (id);
create index imsg_modifier_idx on o_info_message (fk_modifier_id);
alter table o_info_message add constraint FKF85553465A4FA5EF foreign key (fk_modifier_id) references o_bs_identity (id);

alter table o_ep_artefact add constraint FKF26C8375236F28X foreign key (fk_artefact_auth_id) references o_bs_identity (id);
alter table o_ep_struct_el add constraint FKF26C8375236F26X foreign key (fk_olatresource) references o_olatresource (resource_id);
alter table o_ep_struct_el add constraint FKF26C8375236F29X foreign key (fk_ownergroup) references o_bs_secgroup (id);
alter table o_ep_struct_el add constraint FK4ECC1C8D636191A1 foreign key (fk_map_source_id) references o_ep_struct_el (structure_id);
alter table o_ep_struct_el add constraint FK4ECC1C8D76990817 foreign key (fk_struct_root_id) references o_ep_struct_el (structure_id);
alter table o_ep_struct_el add constraint FK4ECC1C8D76990818 foreign key (fk_struct_root_map_id) references o_ep_struct_el (structure_id);
alter table o_ep_artefact add constraint FKA0070D12316A97B4 foreign key (fk_struct_el_id) references o_ep_struct_el (structure_id);
alter table o_ep_collect_restriction add constraint FKA0070D12316A97B5 foreign key (fk_struct_el_id) references o_ep_struct_el (structure_id);
alter table o_ep_struct_struct_link add constraint FKF26C8375236F22X foreign key (fk_struct_parent_id) references o_ep_struct_el (structure_id);
alter table o_ep_struct_struct_link add constraint FKF26C8375236F23X foreign key (fk_struct_child_id) references o_ep_struct_el (structure_id);
alter table o_ep_struct_artefact_link add constraint FKF26C8375236F24X foreign key (fk_struct_id) references o_ep_struct_el (structure_id);
alter table o_ep_struct_artefact_link add constraint FKF26C8375236F25X foreign key (fk_artefact_id) references o_ep_artefact (artefact_id);
alter table o_ep_struct_artefact_link add constraint FKF26C8375236F26Y foreign key (fk_auth_id) references o_bs_identity (id);

alter table o_tag add constraint FK6491FCA5A4FA5DC foreign key (fk_author_id) references o_bs_identity (id);

alter table o_bs_invitation add constraint FKF26C8375236F27X foreign key (fk_secgroup) references o_bs_secgroup (id);

insert into hibernate_unique_key values ( 0 );

-- the following are redundant indexes
drop index ocl_asset_idx on oc_lock;
drop index provider_idx on o_bs_authentication;
drop index FKFF94111CD1A80C95 on o_bs_identity;
drop index name_idx on o_bs_identity;
drop index FK7B6288B4B85B522C on o_bs_membership;
drop index groupname_idx on o_bs_namedgroup;
drop index FK9A1C5109F9C3F1D on o_bs_policy;
drop index FKF4433C2CA1FAC766 on o_catentry;
drop index FK1C154FC47E4A0638 on o_gp_bgcontext;
drop index FK9B663F2D1E2E7685 on o_gp_bgtoarea_rel;
drop index FKCEEB8A86A1FAC766 on o_gp_business;
drop index FKCEEB8A86C06E3EF3 on o_gp_business;
drop index owner_idx on o_note;
drop index FK4FB8F04749E53702 on o_noti_sub;
drop index name_idx on o_olatresource;
drop index asset_idx on o_plock;
drop index o_projectbroker_customfields_idx on o_projectbroker_customfields;
drop index projectbroker_project_id_idx on o_projectbroker_project;
drop index FK2F9C439888C31018 on o_repositoryentry;
drop index FK2F9C4398A1FAC766 on o_repositoryentry;
drop index softkey_idx on o_repositoryentry;
drop index FKDB97A6493F14E3EE on o_repositorymetadata;
drop index FK4B04D83FD1A80C95 on o_userproperty;

SET FOREIGN_KEY_CHECKS = 1;
