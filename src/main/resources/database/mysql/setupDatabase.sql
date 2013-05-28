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
   fk_resource bigint unique,
   fk_ownergroup bigint unique,
   fk_partipiciantgroup bigint unique,
   fk_waitinggroup bigint unique,
   primary key (group_id)
);
create table if not exists o_gp_business_to_resource (
   g_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   fk_resource bigint not null,
   fk_group bigint not null,
   primary key (g_id)
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
   issuspended bit default 0,
   fullyassessed bit default 0,
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
   receiverealmail varchar(16),
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
   groupcontext_fk bigint,
   fk_resource bigint default null,
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
   fk_tutorgroup bigint,
	 fk_participantgroup bigint,
   description longtext,
   initialauthor varchar(128) not null,
   accesscode integer not null default 0,
   membersonly bit default 0,
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
   title varchar(255),
   description longtext,
   primary key (checklist_id)
);
alter table o_checklist ENGINE = InnoDB;

create table if not exists o_checkpoint (
   checkpoint_id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime not null,
   title varchar(255),
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
	lastmodified datetime,
	resname varchar(50) not null, 
	resid bigint not null, 
	ressubpath varchar(2048), 
    creator_id bigint not null,
	rating integer not null, 
	primary key (rating_id)
);

create table o_co_db_entry (
   id int8 not null,
   version int8 not null,
   lastmodified timestamp,
   creationdate timestamp,
   courseid int8,
   identity int8,
   category varchar(32),
   name varchar(255) not null,
   floatvalue decimal(65,30),
   longvalue int8,
   stringvalue varchar(255),
   textvalue TEXT,
   primary key (id)
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

-- mail system

create table if not exists o_mail (
  mail_id bigint NOT NULL,
  meta_mail_id varchar(64),
  creationdate datetime,
	lastmodified datetime,
	resname varchar(50),
  resid bigint,
  ressubpath varchar(2048),
  businesspath varchar(2048),
  subject varchar(512),
  body longtext,
  fk_from_id bigint,
  primary key (mail_id)
);

-- mail recipient
create table if not exists o_mail_to_recipient (
  pos mediumint NOT NULL default 0,
  fk_mail_id bigint,
  fk_recipient_id bigint
);

create table if not exists o_mail_recipient (
  recipient_id bigint NOT NULL,
  recipientvisible bit,
  deleted bit,
  mailread bit,
  mailmarked bit,
  email varchar(255),
  recipientgroup varchar(255),
  creationdate datetime,
  fk_recipient_id bigint,
  primary key (recipient_id)
);

-- mail attachments
create table o_mail_attachment (
   attachment_id bigint NOT NULL,
   creationdate datetime,
   datas mediumblob,
   datas_size bigint,
   datas_name varchar(255),
   datas_checksum bigint,
   datas_path varchar(1024),
   datas_lastmodified datetime,
   mimetype varchar(255),
   fk_att_mail_id bigint,
   primary key (attachment_id)
);

-- access control
create table  if not exists o_ac_offer (
	offer_id bigint NOT NULL,
  creationdate datetime,
	lastmodified datetime,
	is_valid bit default 1,
	validfrom datetime,
	validto datetime,
  version mediumint unsigned not null,
  resourceid bigint,
  resourcetypename varchar(255),
  resourcedisplayname varchar(255),
  token varchar(255),
  price_amount DECIMAL(12,4),
	price_currency_code VARCHAR(3),
	offer_desc VARCHAR(2000),
  fk_resource_id bigint,
	primary key (offer_id)
);

create table if not exists o_ac_method (
	method_id bigint NOT NULL,
	access_method varchar(32),
  version mediumint unsigned not null,
  creationdate datetime,
	lastmodified datetime,
	is_valid bit default 1,
	is_enabled bit default 1,
	validfrom datetime,
	validto datetime,
	primary key (method_id)
);

create table if not exists o_ac_offer_access (
	offer_method_id bigint NOT NULL,
  version mediumint unsigned not null,
  creationdate datetime,
	is_valid bit default 1,
	validfrom datetime,
	validto datetime,
  fk_offer_id bigint,
  fk_method_id bigint,
	primary key (offer_method_id)
);

-- access cart
create table if not exists o_ac_order (
	order_id bigint NOT NULL,
  version mediumint unsigned not null,
  creationdate datetime,
	lastmodified datetime,
	is_valid bit default 1,
	total_lines_amount DECIMAL(12,4),
	total_lines_currency_code VARCHAR(3),
	total_amount DECIMAL(12,4),
	total_currency_code VARCHAR(3),
	discount_amount DECIMAL(12,4),
	discount_currency_code VARCHAR(3),
	order_status VARCHAR(32) default 'NEW',
  fk_delivery_id bigint,
	primary key (order_id)
);

create table if not exists o_ac_order_part (
	order_part_id bigint NOT NULL,
  version mediumint unsigned not null,
  pos mediumint unsigned,
  creationdate datetime,
  total_lines_amount DECIMAL(12,4),
  total_lines_currency_code VARCHAR(3),
  total_amount DECIMAL(12,4),
  total_currency_code VARCHAR(3),
  fk_order_id bigint,
	primary key (order_part_id)
);

create table if not exists o_ac_order_line (
	order_item_id bigint NOT NULL,
  version mediumint unsigned not null,
  pos mediumint unsigned,
  creationdate datetime,
	unit_price_amount DECIMAL(12,4),
	unit_price_currency_code VARCHAR(3),
	total_amount DECIMAL(12,4),
	total_currency_code VARCHAR(3),
  fk_order_part_id bigint,
  fk_offer_id bigint,
	primary key (order_item_id)
);

create table if not exists o_ac_transaction (
	transaction_id bigint NOT NULL,
  version mediumint unsigned not null,
  creationdate datetime,
  trx_status VARCHAR(32) default 'NEW',
  amount_amount DECIMAL(12,4),
  amount_currency_code VARCHAR(3),
  fk_order_part_id bigint,
  fk_order_id bigint,
  fk_method_id bigint,
	primary key (transaction_id)
);

create table  if not exists o_ac_reservation (
   reservation_id bigint NOT NULL,
   creationdate datetime,
   lastmodified datetime,
   version mediumint unsigned not null,
   expirationdate datetime,
   reservationtype varchar(32),
   fk_identity bigint not null,
   fk_resource bigint not null,
   primary key (reservation_id)
);

create table if not exists o_ac_paypal_transaction (
   transaction_id int8 not null,
   version int8 not null,
   creationdate timestamp,
   ref_no varchar(255),
   order_id int8 not null,
   order_part_id int8 not null,
   method_id int8 not null,
   success_uuid varchar(32) not null,
	 cancel_uuid varchar(32) not null,
	 amount_amount DECIMAL(12,4),
	 amount_currency_code VARCHAR(3),
   pay_response_date timestamp,
   pay_key varchar(255),
	 ack varchar(255),
	 build varchar(255),
	 coorelation_id varchar(255),
	 payment_exec_status varchar(255),
	 ipn_transaction_id varchar(255),
	 ipn_transaction_status varchar(255),
	 ipn_sender_transaction_id varchar(255),
	 ipn_sender_transaction_status varchar(255),
	 ipn_sender_email varchar(255),
	 ipn_verify_sign varchar(255),
	 ipn_pending_reason varchar(255),
	 trx_status VARCHAR(32) not null default 'NEW',
	 trx_amount DECIMAL(12,4),
	 trx_currency_code VARCHAR(3),
   primary key (transaction_id)
);

-- openmeetings
create table if not exists o_om_room_reference (
   id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime,
   creationdate datetime,
   businessgroup bigint,
   resourcetypename varchar(50),
   resourcetypeid bigint,
   ressubpath varchar(255),
   roomId bigint,
   config longtext,
   primary key (id)
);

-- assessment tables
-- efficiency statments
create table if not exists o_as_eff_statement (
   id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime,
   creationdate datetime,
   passed bit(0),
   score float(65,30),
   total_nodes mediumint,
   attempted_nodes mediumint,
   passed_nodes mediumint,
   course_title varchar(255),
   course_short_title varchar(128),
   course_repo_key bigint,
   statement_xml longtext,
   fk_identity bigint,
   fk_resource_id bigint,
   primary key (id)
);

-- user to course informations (was property initial and recent launch dates)
create table o_as_user_course_infos (
   id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   lastmodified datetime,
   initiallaunchdate datetime,
   recentlaunchdate datetime,
   visit mediumint,
   timespend bigint,
   fk_identity bigint,
   fk_resource_id bigint,
   primary key (id)
);

-- instant messaging
create table if not exists o_im_message (
   id bigint not null,
   creationdate datetime,
   msg_resname varchar(50) not null,
   msg_resid bigint not null,
   msg_anonym bit default 0,
   msg_from varchar(255) not null,
   msg_body longtext,
   fk_from_identity_id bigint not null,
   primary key (id)
);

create table if not exists o_im_notification (
   id bigint not null,
   creationdate datetime,
   chat_resname varchar(50) not null,
   chat_resid bigint not null,
   fk_to_identity_id bigint not null,
   fk_from_identity_id bigint not null,
   primary key (id)
);

create table if not exists o_im_roster_entry (
   id bigint not null,
   creationdate datetime,
   r_resname varchar(50) not null,
   r_resid bigint not null,
   r_nickname varchar(255),
   r_fullname varchar(255),
   r_anonym bit default 0,
   r_vip bit default 0,
   fk_identity_id bigint not null,
   primary key (id)
);

create table if not exists o_im_preferences (
   id bigint not null,
   creationdate datetime,
   visible_to_others bit default 0,
   roster_def_status varchar(12),
   fk_from_identity_id bigint not null,
   primary key (id)
);

-- add mapper table
create table o_mapper (
   id int8 not null,
   lastmodified timestamp,
   creationdate timestamp,
   expirationdate datetime,
   mapper_uuid varchar(64),
   orig_session_id varchar(64),
   xml_config TEXT,
   primary key (id)
);

-- question item
create table o_qp_pool (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_name varchar(255) not null,
   q_public bit default 0,
   fk_ownergroup bigint,
   primary key (id)
);

create table o_qp_taxonomy_level (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_field varchar(255) not null,
   q_mat_path_ids varchar(1024),
   q_mat_path_names varchar(2048),
   fk_parent_field bigint,
   primary key (id)
);

create table o_qp_item (
   id bigint not null,
   q_identifier varchar(36) not null,
   q_master_identifier varchar(36),
   q_title varchar(1024) not null,
   q_description varchar(2048),
   q_keywords varchar(1024),
   q_coverage varchar(1024),
   q_additional_informations varchar(256),
   q_language varchar(16),
   fk_edu_context bigint,
   q_educational_learningtime varchar(32),
   fk_type bigint,
   q_difficulty decimal(10,9),
   q_stdev_difficulty decimal(10,9),
   q_differentiation decimal(10,9),
   q_num_of_answers_alt bigint not null default 0,
   q_usage bigint not null default 0,
   q_assessment_type varchar(64),
   q_status varchar(32) not null,
   q_version varchar(50),
   fk_license bigint,
   q_editor varchar(256),
   q_editor_version varchar(256),
   q_format varchar(32) not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_dir varchar(32),
   q_root_filename varchar(255),
   fk_taxonomy_level bigint,
   fk_ownergroup bigint not null,
   primary key (id)
);

create table o_qp_pool_2_item (
   id bigint not null,
   creationdate datetime not null,
   q_editable bit default 0,
   fk_pool_id bigint not null,
   fk_item_id bigint not null,
   primary key (id)
);

create table o_qp_share_item (
   id bigint not null,
   creationdate datetime not null,
   q_editable bit default 0,
   fk_resource_id bigint not null,
   fk_item_id bigint not null,
   primary key (id)
);

create table o_qp_item_collection (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_name varchar(256),
   fk_owner_id bigint not null,
   primary key (id)
);

create table o_qp_collection_2_item (
   id bigint not null,
   creationdate datetime not null,
   fk_collection_id bigint not null,
   fk_item_id bigint not null,
   primary key (id)
);

create table o_qp_edu_context (
   id bigint not null,
   creationdate datetime not null,
   q_level varchar(256) not null,
   q_deletable bit default 0,
   primary key (id)
);

create table if not exists o_qp_item_type (
   id bigint not null,
   creationdate datetime not null,
   q_type varchar(256) not null,
   q_deletable bit default 0,
   primary key (id)
);

create table if not exists o_qp_license (
   id bigint not null,
   creationdate datetime not null,
   q_license varchar(256) not null,
   q_text varchar(2048),
   q_deletable bit default 0,
   primary key (id)
);

create table o_lti_outcome (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   r_ressubpath varchar(2048),
   r_action varchar(255) not null,
   r_outcome_key varchar(255) not null,
   r_outcome_value varchar(2048),
   fk_resource_id bigint not null,
   fk_identity_id bigint not null,
   primary key (id)
);


-- user view
create view o_bs_identity_short_v as (
   select
      ident.id as id_id,
      ident.name as id_name,
      ident.lastlogin as id_lastlogin,
      ident.status as id_status,
      us.user_id as us_id,
      p_firstname.propvalue as first_name,
      p_lastname.propvalue as last_name,
      p_email.propvalue as email
   from o_bs_identity as ident
   inner join o_user as us on (ident.fk_user_id = us.user_id)
   left join o_userproperty as p_firstname on (us.user_id = p_firstname.fk_user_id and p_firstname.propName = 'firstName')
   left join o_userproperty as p_lastname on (us.user_id = p_lastname.fk_user_id and p_lastname.propName = 'lastName')
   left join o_userproperty as p_email on (us.user_id = p_email.fk_user_id and p_email.propName = 'email')
);

create view o_gp_contextresource_2_group_v as (
   select
      cg_bg2resource.groupcontextresource_id as groupcontextresource_id,
      cg_bgcontext.groupcontext_id as groupcontext_id,
      cg_bgroup.group_id as group_id,
      cg_bg2resource.oresource_id as oresource_id,
      cg_bgcontext.grouptype as grouptype,
      cg_bgcontext.defaultcontext as defaultcontext,
      cg_bgroup.groupname as groupname,
      cg_bgroup.fk_ownergroup as fk_ownergroup,
      cg_bgroup.fk_partipiciantgroup as fk_partipiciantgroup,
      cg_bgroup.fk_waitinggroup as fk_waitinggroup
   from o_gp_bgcontextresource_rel as cg_bg2resource
   inner join o_gp_bgcontext as cg_bgcontext on (cg_bg2resource.groupcontext_fk = cg_bgcontext.groupcontext_id)
   inner join o_gp_business as cg_bgroup on (cg_bg2resource.groupcontext_fk = cg_bgroup.groupcontext_fk)
);

-- eportfolio views
-- views

-- notifications for e-portfolio
create or replace view o_ep_notifications_struct_v as (
   select
      struct.structure_id as struct_id,
      struct.structure_type as struct_type,
      struct.title as struct_title,
      struct.fk_struct_root_id as struct_root_id,
      struct.fk_struct_root_map_id as struct_root_map_id,
      (case when struct.structure_type = 'page' then struct.structure_id else parent_struct.structure_id end) as page_key,
      struct_link.creationdate as creation_date
   from o_ep_struct_el as struct
   inner join o_ep_struct_struct_link as struct_link on (struct_link.fk_struct_child_id = struct.structure_id)
   inner join o_ep_struct_el as parent_struct on (struct_link.fk_struct_parent_id = parent_struct.structure_id)
   where struct.structure_type = 'page' or parent_struct.structure_type = 'page'
);

create or replace view o_ep_notifications_art_v as (
   select
      artefact.artefact_id as artefact_id,
      artefact_link.link_id as link_id,
      artefact.title as artefact_title,
      (case when struct.structure_type = 'page' then struct.title else root_struct.title end ) as struct_title,
      struct.structure_type as struct_type,
      struct.structure_id as struct_id,
      root_struct.structure_id as struct_root_id,
      root_struct.structure_type as struct_root_type,
      struct.fk_struct_root_map_id as struct_root_map_id,
      (case when struct.structure_type = 'page' then struct.structure_id else root_struct.structure_id end ) as page_key,
      artefact_link.fk_auth_id as author_id,
      artefact_link.creationdate as creation_date
   from o_ep_struct_el as struct
   inner join o_ep_struct_artefact_link as artefact_link on (artefact_link.fk_struct_id = struct.structure_id)
   inner join o_ep_artefact as artefact on (artefact_link.fk_artefact_id = artefact.artefact_id)
   left join o_ep_struct_el as root_struct on (struct.fk_struct_root_id = root_struct.structure_id)
);
 
create or replace view o_ep_notifications_rating_v as (
   select
      urating.rating_id as rating_id,
      map.structure_id as map_id,
      map.title as map_title,
      cast(urating.ressubpath as unsigned) as page_key,
      page.title as page_title,
      urating.creator_id as author_id,
      urating.creationdate as creation_date,
      urating.lastmodified as last_modified 
   from o_userrating as urating
   inner join o_olatresource as rating_resource on (rating_resource.resid = urating.resid and rating_resource.resname = urating.resname)
   inner join o_ep_struct_el as map on (map.fk_olatresource = rating_resource.resource_id)
   left join o_ep_struct_el as page on (page.fk_struct_root_map_id = map.structure_id and page.structure_id = urating.ressubpath)
);

create or replace view o_ep_notifications_comment_v as (
   select
      ucomment.comment_id as comment_id,
      map.structure_id as map_id,
      map.title as map_title,
      cast(ucomment.ressubpath as unsigned) as page_key,
      page.title as page_title,
      ucomment.creator_id as author_id,
      ucomment.creationdate as creation_date
   from o_usercomment as ucomment
   inner join o_olatresource as comment_resource on (comment_resource.resid = ucomment.resid and comment_resource.resname = ucomment.resname)
   inner join o_ep_struct_el as map on (map.fk_olatresource = comment_resource.resource_id)
   left join o_ep_struct_el as page on (page.fk_struct_root_map_id = map.structure_id and page.structure_id = ucomment.ressubpath)
);

create or replace view o_gp_business_to_repository_v as (
	select 
		grp.group_id as grp_id,
		repoentry.repositoryentry_id as re_id,
		repoentry.displayname as re_displayname
	from o_gp_business as grp
	inner join o_gp_business_to_resource as relation on (relation.fk_group = grp.group_id)
	inner join o_repositoryentry as repoentry on (repoentry.fk_olatresource = relation.fk_resource)
);

create or replace view o_bs_gp_membership_v as (
   select
      membership.id as membership_id,
      membership.identity_id as identity_id,
      membership.lastmodified as lastmodified,
      membership.creationdate as creationdate,
      owned_gp.group_id as owned_gp_id,
      participant_gp.group_id as participant_gp_id,
      waiting_gp.group_id as waiting_gp_id
   from o_bs_membership as membership
   left join o_gp_business as owned_gp on (membership.secgroup_id = owned_gp.fk_ownergroup)
   left join o_gp_business as participant_gp on (membership.secgroup_id = participant_gp.fk_partipiciantgroup)
   left join o_gp_business as waiting_gp on (membership.secgroup_id = waiting_gp.fk_waitinggroup)
   where (owned_gp.group_id is not null or participant_gp.group_id is not null or waiting_gp.group_id is not null)
);

create or replace view o_gp_business_v  as (
   select
      gp.group_id as group_id,
      gp.groupname as groupname,
      gp.lastmodified as lastmodified,
      gp.creationdate as creationdate,
      gp.lastusage as lastusage,
      gp.descr as descr,
      gp.minparticipants as minparticipants,
      gp.maxparticipants as maxparticipants,
      gp.waitinglist_enabled as waitinglist_enabled,
      gp.autocloseranks_enabled as autocloseranks_enabled,
      (select count(part.id) from o_bs_membership as part where part.secgroup_id = gp.fk_partipiciantgroup) as num_of_participants,
      (select count(pending.reservation_id) from o_ac_reservation as pending where pending.fk_resource = gp.fk_resource) as num_of_pendings,
      (select count(own.id) from o_bs_membership as own where own.secgroup_id = gp.fk_ownergroup) as num_of_owners,
      (case when gp.waitinglist_enabled = 1
         then 
           (select count(waiting.id) from o_bs_membership as waiting where waiting.secgroup_id = gp.fk_partipiciantgroup)
         else
           0
      end) as num_waiting,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=1
         and (offer.validfrom is null or offer.validfrom <= current_timestamp())
         and (offer.validto is null or offer.validto >= current_timestamp())
      ) as num_of_valid_offers,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=1
      ) as num_of_offers,
      (select count(relation.fk_resource) from o_gp_business_to_resource as relation 
         where relation.fk_group = gp.group_id
      ) as num_of_relations,
      gp.fk_resource as fk_resource,
      gp.fk_ownergroup as fk_ownergroup,
      gp.fk_partipiciantgroup as fk_partipiciantgroup,
      gp.fk_waitinggroup as fk_waitinggroup
   from o_gp_business as gp
);

create or replace view o_re_strict_member_v as (
   select
      re.repositoryentry_id as re_id,
      re_part_member.identity_id as re_part_member_id,
      re_tutor_member.identity_id as re_tutor_member_id,
      re_owner_member.identity_id as re_owner_member_id,
      bg_part_member.identity_id as bg_part_member_id,
      bg_owner_member.identity_id as bg_owner_member_id
   from o_repositoryentry as re
   left join o_bs_membership as re_part_member on (re_part_member.secgroup_id = re.fk_participantgroup)
   left join o_bs_membership as re_tutor_member on (re_tutor_member.secgroup_id = re.fk_tutorgroup)
   left join o_bs_membership as re_owner_member on (re_owner_member.secgroup_id = re.fk_ownergroup)
   left join o_gp_business_to_resource as bgroup_rel on (bgroup_rel.fk_resource = re.fk_olatresource)
   left join o_gp_business as bgroup on (bgroup.group_id = bgroup_rel.fk_group)
   left join o_bs_membership as bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   left join o_bs_membership as bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
   where re.membersonly=1 and re.accesscode=1
);

create or replace view o_re_strict_participant_v as (
   select
      re.repositoryentry_id as re_id,
      re_part_member.identity_id as re_part_member_id,
      bg_part_member.identity_id as bg_part_member_id
   from o_repositoryentry as re
   left join o_bs_membership as re_part_member on (re_part_member.secgroup_id = re.fk_participantgroup)
   left join o_gp_business_to_resource as bgroup_rel on (bgroup_rel.fk_resource = re.fk_olatresource)
   left join o_gp_business as bgroup on (bgroup.group_id = bgroup_rel.fk_group)
   left join o_bs_membership as bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   where (re.membersonly=1 and re.accesscode=1) or re.accesscode>=3
);

create or replace view o_re_strict_tutor_v as (
   select
      re.repositoryentry_id as re_id,
      re_tutor_member.identity_id as re_tutor_member_id,
      re_owner_member.identity_id as re_owner_member_id,
      bg_owner_member.identity_id as bg_owner_member_id
   from o_repositoryentry as re
   left join o_bs_membership as re_tutor_member on (re_tutor_member.secgroup_id = re.fk_tutorgroup)
   left join o_bs_membership as re_owner_member on (re_owner_member.secgroup_id = re.fk_ownergroup)
   left join o_gp_business_to_resource as bgroup_rel on (bgroup_rel.fk_resource = re.fk_olatresource)
   left join o_gp_business as bgroup on (bgroup.group_id = bgroup_rel.fk_group)
   left join o_bs_membership as bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
   where (re.membersonly=1 and re.accesscode=1) or re.accesscode>=3
);

create or replace view o_re_membership_v as (
   select
      membership.id as membership_id,
      membership.identity_id as identity_id,
      membership.lastmodified as lastmodified,
      membership.creationdate as creationdate,
      re_owner_member.repositoryentry_id as owner_re_id,
      re_owner_member.fk_olatresource as owner_ores_id,
      re_tutor_member.repositoryentry_id as tutor_re_id,
      re_tutor_member.fk_olatresource as tutor_ores_id,
      re_part_member.repositoryentry_id as participant_re_id,
      re_part_member.fk_olatresource as participant_ores_id
   from o_bs_membership as membership
   left join o_repositoryentry as re_part_member on (membership.secgroup_id = re_part_member.fk_participantgroup)
   left join o_repositoryentry as re_tutor_member on (membership.secgroup_id = re_tutor_member.fk_tutorgroup)
   left join o_repositoryentry as re_owner_member on (membership.secgroup_id = re_owner_member.fk_ownergroup)
);

create or replace view o_gp_visible_participant_v as (
   select
      bg_part_member.id as membership_id,
      bgroup.group_id as bg_id,
      bgroup.groupname as bg_name,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_part_member.identity_id as bg_part_member_id,
      ident.name as bg_part_member_name 
   from o_gp_business as bgroup
   inner join o_property as bconfig on (bconfig.grp = bgroup.group_id and bconfig.name = 'displayMembers' and bconfig.category = 'config' and bconfig.longValue in (2,3,6,7))
   inner join o_bs_membership as bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   inner join o_bs_identity as ident on (bg_part_member.identity_id = ident.id)
 );
   
create or replace view o_gp_visible_owner_v as ( 
   select
      bg_owner_member.id as membership_id,
      bgroup.group_id as bg_id,
      bgroup.groupname as bg_name,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_owner_member.identity_id as bg_owner_member_id,
      ident.name as bg_owner_member_name
   from o_gp_business as bgroup
   inner join o_property as bconfig on (bconfig.grp = bgroup.group_id and bconfig.name = 'displayMembers' and bconfig.category = 'config' and bconfig.longValue in (1,3,5,7))
   inner join o_bs_membership as bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
   inner join o_bs_identity as ident on (bg_owner_member.identity_id = ident.id)
);

-- coaching
create or replace view o_as_eff_statement_groups_v as (
   select
      sg_re.repositoryentry_id as re_id,
      sg_re.displayname as re_name,
      sg_bg.group_id as bg_id,
      sg_bg.groupname as bg_name,
      sg_tutorMembership.identity_id as tutor_id,
      sg_studentMembership.identity_id as student_id,
      sg_statement.id as st_id,
      (case when sg_statement.passed = 1 then 1 else 0 end) as st_passed,
      (case when sg_statement.passed = 0 then 1 else 0 end) as st_failed,
      (case when sg_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sg_statement.score as st_score,
      pg_initial_launch.id as pg_id
   from o_repositoryentry as sg_re
   inner join o_olatresource as sg_reResource on (sg_re.fk_olatresource = sg_reResource.resource_id and sg_reResource.resname = 'CourseModule')
   inner join o_gp_business_to_resource as sg_bg2resource on (sg_bg2resource.fk_resource = sg_reResource.resource_id)
   inner join o_gp_business as sg_bg on (sg_bg.group_id = sg_bg2resource.fk_group)
   inner join o_bs_membership as sg_tutorMembership on (sg_bg.fk_ownergroup = sg_tutorMembership.secgroup_id)
   inner join o_bs_membership as sg_studentMembership on (sg_bg.fk_partipiciantgroup = sg_studentMembership.secgroup_id)
   left join o_as_eff_statement as sg_statement on (sg_statement.fk_resource_id = sg_reResource.resource_id and sg_statement.fk_identity = sg_studentMembership.identity_id)
   left join o_as_user_course_infos as pg_initial_launch on (pg_initial_launch.fk_resource_id = sg_reResource.resource_id and pg_initial_launch.fk_identity = sg_studentMembership.identity_id)
   group by sg_re.repositoryentry_id, sg_re.displayname, sg_bg.group_id, sg_bg.groupname, sg_tutorMembership.identity_id, 
      sg_studentMembership.identity_id, sg_statement.id, sg_statement.score, pg_initial_launch.id
);

create or replace view o_as_eff_statement_grouped_v as (
   select
      sg_re.repositoryentry_id as re_id,
      sg_re.displayname as re_name,
      sg_tutorMembership.identity_id as tutor_id,
      sg_studentMembership.identity_id as student_id,
      sg_statement.id as st_id,
      (case when sg_statement.passed = 1 then 1 else 0 end) as st_passed,
      (case when sg_statement.passed = 0 then 1 else 0 end) as st_failed,
      (case when sg_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sg_statement.score as st_score,
      pg_initial_launch.id as pg_id
   from o_repositoryentry as sg_re
   inner join o_olatresource as sg_reResource on (sg_re.fk_olatresource = sg_reResource.resource_id and sg_reResource.resname = 'CourseModule')
   inner join o_gp_business_to_resource as sg_bg2resource on (sg_bg2resource.fk_resource = sg_reResource.resource_id)
   inner join o_gp_business as sg_bg on (sg_bg.group_id = sg_bg2resource.fk_group)
   inner join o_bs_membership as sg_tutorMembership on (sg_bg.fk_ownergroup = sg_tutorMembership.secgroup_id)
   inner join o_bs_membership as sg_studentMembership on (sg_bg.fk_partipiciantgroup = sg_studentMembership.secgroup_id)
   left join o_as_eff_statement as sg_statement on (sg_statement.fk_resource_id = sg_reResource.resource_id and sg_statement.fk_identity = sg_studentMembership.identity_id)
   left join o_as_user_course_infos as pg_initial_launch on (pg_initial_launch.fk_resource_id = sg_reResource.resource_id and pg_initial_launch.fk_identity = sg_studentMembership.identity_id)
   group by sg_re.repositoryentry_id, sg_re.displayname, sg_tutorMembership.identity_id, 
      sg_studentMembership.identity_id, sg_statement.id, sg_statement.score, pg_initial_launch.id
);

create or replace view o_as_eff_statement_members_v as (
   select
      sm_re.repositoryentry_id as re_id,
      sm_re.displayname as re_name,
      sm_tutorMembership.identity_id as tutor_id,
      sm_studentMembership.identity_id as student_id,
      sm_statement.id as st_id,
      (case when sm_statement.passed = 1 then 1 else 0 end) as st_passed,
      (case when sm_statement.passed = 0 then 1 else 0 end) as st_failed,
      (case when sm_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sm_statement.score as st_score,
      pm_initial_launch.id as pg_id
   from o_repositoryentry as sm_re
   inner join o_olatresource as sm_reResource on (sm_re.fk_olatresource = sm_reResource.resource_id and sm_reResource.resname = 'CourseModule')
   inner join o_bs_membership as sm_tutorMembership on (sm_re.fk_tutorgroup = sm_tutorMembership.secgroup_id)
   inner join o_bs_membership as sm_studentMembership on (sm_re.fk_participantgroup = sm_studentMembership.secgroup_id)
   left join o_as_eff_statement as sm_statement on (sm_statement.fk_resource_id = sm_reResource.resource_id and sm_statement.fk_identity = sm_studentMembership.identity_id)
   left join o_as_user_course_infos as pm_initial_launch on (pm_initial_launch.fk_resource_id = sm_reResource.resource_id and pm_initial_launch.fk_identity = sm_studentMembership.identity_id)
);

create or replace view o_as_eff_statement_members_strict_v as (
   select
      sm_re.repositoryentry_id as re_id,
      sm_re.displayname as re_name,
      sm_tutorMembership.identity_id as tutor_id,
      sm_studentMembership.identity_id as student_id,
      sm_statement.id as st_id,
      (case when sm_statement.passed = 1 then 1 else 0 end) as st_passed,
      (case when sm_statement.passed = 0 then 1 else 0 end) as st_failed,
      (case when sm_statement.passed is null then 1 else 0 end) as st_not_attempted,
      sm_statement.score as st_score,
      pm_initial_launch.id as pg_id
   from o_repositoryentry as sm_re
   inner join o_olatresource as sm_reResource on (
      sm_re.fk_olatresource = sm_reResource.resource_id
      and sm_reResource.resname = 'CourseModule'
      and not exists (select * from o_gp_business_to_resource as sm_re2group where sm_reResource.resource_id = sm_re2group.fk_resource)
   )
   inner join o_bs_membership as sm_tutorMembership on (sm_re.fk_tutorgroup = sm_tutorMembership.secgroup_id)
   inner join o_bs_membership as sm_studentMembership on (sm_re.fk_participantgroup = sm_studentMembership.secgroup_id)
   left join o_as_eff_statement as sm_statement on (sm_statement.fk_resource_id = sm_reResource.resource_id and sm_statement.fk_identity = sm_studentMembership.identity_id)
   left join o_as_user_course_infos as pm_initial_launch on (pm_initial_launch.fk_resource_id = sm_reResource.resource_id and pm_initial_launch.fk_identity = sm_studentMembership.identity_id)
);

create or replace view o_im_roster_entry_v as (
   select
      entry.id as re_id,
      entry.creationdate as re_creationdate,
      ident.id as ident_id,
      ident.name as ident_name,
      entry.r_nickname as re_nickname,
      entry.r_fullname as re_fullname,
      entry.r_anonym as re_anonym,
      entry.r_vip as re_vip,
      entry.r_resname as re_resname,
      entry.r_resid as re_resid
   from o_im_roster_entry as entry
   inner join o_bs_identity as ident on (entry.fk_identity_id = ident.id)
);


-- views with rating
create or replace view o_qp_item_v as (
   select
      item.id as item_id,
      item.q_identifier as item_identifier,
      item.q_master_identifier as item_master_identifier,
      item.q_title as item_title,
      item.q_language as item_language,
      item.q_keywords as item_keywords,
      taxlevel.q_field as item_taxonomy_level,
      educontext.q_level as item_edu_context,
      item.q_educational_learningtime as item_educational_learningtime,
      itemtype.q_type as item_type,
      item.q_difficulty as item_difficulty,
      item.q_stdev_difficulty as item_stdev_difficulty,
      item.q_differentiation as item_differentiation,
      item.q_num_of_answers_alt as item_num_of_answers_alt,
      item.q_usage as item_usage,
      item.q_status as item_status,
      item.q_format as item_format,
      item.creationdate as item_creationdate,
      item.lastmodified as item_lastmodified,
      ownership.identity_id as owner_id,
      mark.creator_id as mark_creator,
      (case when mark.creator_id is null then 0 else 1 end) as marked,
      (select avg(rating.rating) from o_userrating as rating
         where rating.resid=item.id and rating.resname='QuestionItem' and rating.ressubpath is null
      ) as item_rating
   from o_qp_item as item
   inner join o_bs_secgroup as ownergroup on (ownergroup.id = item.fk_ownergroup)
   left join o_bs_membership as ownership on (ownergroup.id = ownership.secgroup_id) 
   left join o_qp_taxonomy_level as taxlevel on (item.fk_taxonomy_level = taxlevel.id)
   left join o_qp_item_type as itemtype on (item.fk_type = itemtype.id)
   left join o_qp_edu_context as educontext on (item.fk_edu_context = educontext.id)
   left join o_mark as mark on (mark.resid = item.id and mark.resname = 'QuestionItem')
);

create or replace view o_qp_item_author_v as (
   select
      item.id as item_id,
      ownership.identity_id as item_author,
      item.q_identifier as item_identifier,
      item.q_master_identifier as item_master_identifier,
      item.q_title as item_title,
      item.q_language as item_language,
      item.q_keywords as item_keywords,
      taxlevel.q_field as item_taxonomy_level,
      educontext.q_level as item_edu_context,
      item.q_educational_learningtime as item_educational_learningtime,
      itemtype.q_type as item_type,
      item.q_difficulty as item_difficulty,
      item.q_stdev_difficulty as item_stdev_difficulty,
      item.q_differentiation as item_differentiation,
      item.q_num_of_answers_alt as item_num_of_answers_alt,
      item.q_usage as item_usage,
      item.q_status as item_status,
      item.q_format as item_format,
      item.creationdate as item_creationdate,
      item.lastmodified as item_lastmodified,
      mark.creator_id as mark_creator,
      (case when mark.creator_id is null then 0 else 1 end) as marked,
      (select avg(rating.rating) from o_userrating as rating
         where rating.resid=item.id and rating.resname='QuestionItem' and rating.ressubpath is null
      ) as item_rating
   from o_qp_item as item
   inner join o_bs_secgroup as ownergroup on (ownergroup.id = item.fk_ownergroup)
   inner join o_bs_membership as ownership on (ownergroup.id = ownership.secgroup_id) 
   left join o_mark as mark on (mark.resid = item.id and mark.resname = 'QuestionItem')
   left join o_qp_taxonomy_level as taxlevel on (item.fk_taxonomy_level = taxlevel.id)
   left join o_qp_item_type as itemtype on (item.fk_type = itemtype.id)
   left join o_qp_edu_context as educontext on (item.fk_edu_context = educontext.id)
);

create or replace view o_qp_item_pool_v as (
   select
      item.id as item_id,
      pool2item.q_editable as item_editable,
      pool2item.fk_pool_id as item_pool,
      item.q_identifier as item_identifier,
      item.q_master_identifier as item_master_identifier,
      item.q_title as item_title,
      item.q_language as item_language,
      item.q_keywords as item_keywords,
      taxlevel.q_field as item_taxonomy_level,
      educontext.q_level as item_edu_context,
      item.q_educational_learningtime as item_educational_learningtime,
      itemtype.q_type as item_type,
      item.q_difficulty as item_difficulty,
      item.q_stdev_difficulty as item_stdev_difficulty,
      item.q_differentiation as item_differentiation,
      item.q_num_of_answers_alt as item_num_of_answers_alt,
      item.q_usage as item_usage,
      item.q_status as item_status,
      item.q_format as item_format,
      item.creationdate as item_creationdate,
      item.lastmodified as item_lastmodified,
      mark.creator_id as mark_creator,
      (case when mark.creator_id is null then 0 else 1 end) as marked,
      (select avg(rating.rating) from o_userrating as rating
         where rating.resid=item.id and rating.resname='QuestionItem' and rating.ressubpath is null
      ) as item_rating
   from o_qp_item as item
   inner join o_qp_pool_2_item as pool2item on (pool2item.fk_item_id = item.id)
   left join o_mark as mark on (mark.resid = item.id and mark.resname = 'QuestionItem')
   left join o_qp_taxonomy_level as taxlevel on (item.fk_taxonomy_level = taxlevel.id)
   left join o_qp_item_type as itemtype on (item.fk_type = itemtype.id)
   left join o_qp_edu_context as educontext on (item.fk_edu_context = educontext.id)
);

create or replace view o_qp_pool_2_item_short_v as (
   select
      pool2item.id as item_to_pool_id,
      pool2item.creationdate as item_to_pool_creationdate,
      item.id as item_id,
      pool2item.q_editable as item_editable,
      pool2item.fk_pool_id as item_pool,
      pool.q_name as item_pool_name
   from o_qp_item as item
   inner join o_qp_pool_2_item as pool2item on (pool2item.fk_item_id = item.id)
   inner join o_qp_pool as pool on (pool2item.fk_pool_id = pool.id)
);

create or replace view o_qp_item_shared_v as (
   select
      item.id as item_id,
      shareditem.q_editable as item_editable,
      shareditem.fk_resource_id as item_resource_id,
      item.q_identifier as item_identifier,
      item.q_master_identifier as item_master_identifier,
      item.q_title as item_title,
      item.q_language as item_language,
      item.q_keywords as item_keywords,
      taxlevel.q_field as item_taxonomy_level,
      educontext.q_level as item_edu_context,
      item.q_educational_learningtime as item_educational_learningtime,
      itemtype.q_type as item_type,
      item.q_difficulty as item_difficulty,
      item.q_stdev_difficulty as item_stdev_difficulty,
      item.q_differentiation as item_differentiation,
      item.q_num_of_answers_alt as item_num_of_answers_alt,
      item.q_usage as item_usage,
      item.q_status as item_status,
      item.q_format as item_format,
      item.creationdate as item_creationdate,
      item.lastmodified as item_lastmodified,
      mark.creator_id as mark_creator,
      (case when mark.creator_id is null then 0 else 1 end) as marked,
      (select avg(rating.rating) from o_userrating as rating
         where rating.resid=item.id and rating.resname='QuestionItem' and rating.ressubpath is null
      ) as item_rating
   from o_qp_item as item
   inner join o_qp_share_item as shareditem on (shareditem.fk_item_id = item.id)
   left join o_mark as mark on (mark.resid = item.id and mark.resname = 'QuestionItem')
   left join o_qp_taxonomy_level as taxlevel on (item.fk_taxonomy_level = taxlevel.id)
   left join o_qp_item_type as itemtype on (item.fk_type = itemtype.id)
   left join o_qp_edu_context as educontext on (item.fk_edu_context = educontext.id)
);

create or replace view o_qp_share_2_item_short_v as (
   select
      shareditem.id as item_to_share_id,
      shareditem.creationdate as item_to_share_creationdate,
      item.id as item_id,
      shareditem.q_editable as item_editable,
      shareditem.fk_resource_id as resource_id,
      bgroup.groupname as resource_name
   from o_qp_item as item
   inner join o_qp_share_item as shareditem on (shareditem.fk_item_id = item.id)
   inner join o_gp_business as bgroup on (shareditem.fk_resource_id = bgroup.fk_resource)
);

create index  ocl_asset_idx on oc_lock (asset);
alter table oc_lock add index FK9E30F4B66115906D (identity_fk), add constraint FK9E30F4B66115906D foreign key (identity_fk) references o_bs_identity (id);

alter table hibernate_unique_key ENGINE = InnoDB;

alter table o_forum ENGINE = InnoDB;
alter table o_property ENGINE = InnoDB;
alter table o_bs_secgroup ENGINE = InnoDB;
alter table o_repositorymetadata ENGINE = InnoDB;
alter table o_user ENGINE = InnoDB;
alter table o_userproperty ENGINE = InnoDB;
alter table o_message ENGINE = InnoDB;
alter table o_temporarykey ENGINE = InnoDB;
alter table o_bs_authentication ENGINE = InnoDB;
alter table o_qtiresult ENGINE = InnoDB;
alter table o_qtiresultset ENGINE = InnoDB;
alter table o_bs_identity ENGINE = InnoDB;
alter table o_olatresource ENGINE = InnoDB;
alter table o_bs_policy ENGINE = InnoDB;
alter table o_bs_namedgroup ENGINE = InnoDB;
alter table o_bs_membership ENGINE = InnoDB;
alter table o_repositoryentry ENGINE = InnoDB;
alter table o_bookmark ENGINE = InnoDB;
alter table o_references ENGINE = InnoDB;
alter table o_gp_business ENGINE = InnoDB;
alter table o_gp_bgcontextresource_rel ENGINE = InnoDB;
alter table o_gp_bgcontext ENGINE = InnoDB;
alter table o_gp_bgarea ENGINE = InnoDB;
alter table o_gp_bgtoarea_rel ENGINE = InnoDB;
alter table o_catentry ENGINE = InnoDB;
alter table o_noti_pub ENGINE = InnoDB;
alter table o_noti_sub ENGINE = InnoDB;
alter table o_note ENGINE = InnoDB;
alter table o_lifecycle ENGINE = InnoDB;
alter table o_plock ENGINE = InnoDB;
alter table oc_lock ENGINE = InnoDB;
alter table o_readmessage ENGINE = InnoDB;
alter table o_projectbroker ENGINE = InnoDB;
alter table o_projectbroker_project ENGINE = InnoDB;
alter table o_projectbroker_customfields ENGINE = InnoDB;
alter table o_checkpoint ENGINE = InnoDB;
alter table o_checkpoint_results ENGINE = InnoDB;
alter table o_usercomment ENGINE = InnoDB;
alter table o_userrating ENGINE = InnoDB;
alter table o_mark ENGINE = InnoDB;
alter table o_info_message ENGINE = InnoDB;
alter table o_tag ENGINE = InnoDB;
alter table o_bs_invitation ENGINE = InnoDB;
alter table o_ep_artefact ENGINE = InnoDB;
alter table o_ep_collect_restriction ENGINE = InnoDB;
alter table o_ep_struct_el ENGINE = InnoDB;
alter table o_ep_struct_struct_link ENGINE = InnoDB;
alter table o_ep_struct_artefact_link ENGINE = InnoDB;
alter table o_co_db_entry ENGINE = InnoDB;
alter table o_mail ENGINE = InnoDB;
alter table o_mail_to_recipient ENGINE = InnoDB;
alter table o_mail_recipient ENGINE = InnoDB;
alter table o_mail_attachment ENGINE = InnoDB;
alter table o_ac_offer ENGINE = InnoDB;
alter table o_ac_method ENGINE = InnoDB;
alter table o_ac_offer_access ENGINE = InnoDB;
alter table o_ac_order ENGINE = InnoDB;
alter table o_ac_order_part ENGINE = InnoDB;
alter table o_ac_order_line ENGINE = InnoDB;
alter table o_ac_transaction ENGINE = InnoDB;
alter table o_ac_reservation ENGINE = InnoDB;
alter table o_ac_paypal_transaction ENGINE = InnoDB;
alter table o_as_eff_statement ENGINE = InnoDB;
alter table o_as_user_course_infos ENGINE = InnoDB;
alter table o_mapper ENGINE = InnoDB;


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
alter table o_gp_business add constraint idx_bgp_rsrc foreign key (fk_resource) references o_olatresource (resource_id);
alter table o_gp_business add constraint idx_bgp_waiting foreign key (fk_waitinggroup) references o_bs_secgroup (id);
alter table o_gp_business_to_resource add constraint idx_bgp_to_rsrc_rsrc foreign key (fk_resource) references o_olatresource (resource_id);
alter table o_gp_business_to_resource add constraint idx_bgp_to_rsrc_group foreign key (fk_group) references o_gp_business (group_id);
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
alter table o_gp_bgarea add constraint idx_area_to_resource foreign key (fk_resource) references o_olatresource (resource_id);
create index  access_idx on o_repositoryentry (accesscode);
create index  initialAuthor_idx on o_repositoryentry (initialauthor);
create index  resource_idx on o_repositoryentry (resourcename);
create index  displayname_idx on o_repositoryentry (displayname);
create index  softkey_idx on o_repositoryentry (softkey);
alter table o_repositoryentry add index FK2F9C439888C31018 (fk_olatresource), add constraint FK2F9C439888C31018 foreign key (fk_olatresource) references o_olatresource (resource_id);
alter table o_repositoryentry add index FK2F9C4398A1FAC766 (fk_ownergroup), add constraint FK2F9C4398A1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup (id);
alter table o_repositoryentry add constraint repo_tutor_sec_group_ctx foreign key (fk_tutorgroup) references o_bs_secgroup (id);
alter table o_repositoryentry add constraint repo_parti_sec_group_ctx foreign key (fk_participantgroup) references o_bs_secgroup (id);
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

create index o_mapper_uuid_idx on o_mapper (mapper_uuid);

alter table o_ac_reservation add constraint idx_rsrv_to_rsrc_rsrc foreign key (fk_resource) references o_olatresource (resource_id);
alter table o_ac_reservation add constraint idx_rsrv_to_rsrc_identity foreign key (fk_identity) references o_bs_identity (id);

alter table o_checkpoint_results add constraint FK9E30F4B661159ZZY foreign key (checkpoint_fk) references o_checkpoint (checkpoint_id) ;
alter table o_checkpoint_results add constraint FK9E30F4B661159ZZX foreign key (identity_fk) references o_bs_identity (id);
alter table o_checkpoint add constraint FK9E30F4B661159ZZZ foreign key (checklist_fk) references o_checklist (checklist_id);

create index cmt_id_idx on o_usercomment (resid);
create index cmt_name_idx on o_usercomment (resname);
create index cmt_subpath_idx on o_usercomment (ressubpath(255));
alter table o_usercomment add index FK92B6864A18251F0 (parent_key), add constraint FK92B6864A18251F0 foreign key (parent_key) references o_usercomment (comment_id);
alter table o_usercomment add index FKF26C8375236F20A (creator_id), add constraint FKF26C8375236F20A foreign key (creator_id) references o_bs_identity (id);
create index rtn_id_idx on o_userrating (resid);
create index rtn_name_idx on o_userrating (resname);
create index rtn_subpath_idx on o_userrating (ressubpath(255));
create index rtn_rating_idx on o_userrating (rating);
alter table o_userrating add index FKF26C8375236F20X (creator_id), add constraint FKF26C8375236F20X foreign key (creator_id) references o_bs_identity (id);

create index usr_notification_interval_idx on o_user (notification_interval);

create index mark_id_idx on o_mark(resid);
create index mark_name_idx on o_mark(resname);
create index mark_subpath_idx on o_mark(ressubpath(255));
create index mark_businesspath_idx on o_mark(businesspath(255));
create index FKF26C8375236F21X on o_mark(creator_id);
alter table o_mark add constraint FKF26C8375236F21X foreign key (creator_id) references o_bs_identity (id);

create index imsg_resid_idx on o_info_message (resid);
create index imsg_author_idx on o_info_message (fk_author_id);
alter table o_info_message add constraint FKF85553465A4FA5DC foreign key (fk_author_id) references o_bs_identity (id);
create index imsg_modifier_idx on o_info_message (fk_modifier_id);
alter table o_info_message add constraint FKF85553465A4FA5EF foreign key (fk_modifier_id) references o_bs_identity (id);

create index o_co_db_course_idx on o_co_db_entry (courseid);
create index o_co_db_cat_idx on o_co_db_entry (category);
create index o_co_db_name_idx on o_co_db_entry (name);
alter table o_co_db_entry add constraint FK_DB_ENTRY_TO_IDENT foreign key (identity) references o_bs_identity (id);

alter table o_om_room_reference  add constraint idx_omroom_to_bgroup foreign key (businessgroup) references o_gp_business (group_id);
create index idx_omroom_residname on o_om_room_reference (resourcetypename,resourcetypeid);

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

alter table o_mail_to_recipient add constraint FKF86663165A4FA5DE foreign key (fk_mail_id) references o_mail (mail_id);
alter table o_mail_recipient add constraint FKF86663165A4FA5DG foreign key (fk_recipient_id) references o_bs_identity (id);
alter table o_mail add constraint FKF86663165A4FA5DC foreign key (fk_from_id) references o_mail_recipient (recipient_id);
alter table o_mail_to_recipient add constraint FKF86663165A4FA5DD foreign key (fk_recipient_id) references o_mail_recipient (recipient_id);
alter table o_mail_attachment add constraint FKF86663165A4FA5DF foreign key (fk_att_mail_id) references o_mail (mail_id);
create index idx_mail_att_checksum_idx on o_mail_attachment (datas_checksum);
create index idx_mail_path_idx on o_mail_attachment (datas_path);
create index idx_mail_att_siblings_idx on o_mail_attachment (datas_checksum, mimetype, datas_size, datas_name);

create index ac_offer_to_resource_idx on o_ac_offer (fk_resource_id);
alter table o_ac_offer_access add constraint off_to_meth_meth_ctx foreign key (fk_method_id) references o_ac_method (method_id);
alter table o_ac_offer_access add constraint off_to_meth_off_ctx foreign key (fk_offer_id) references o_ac_offer (offer_id);
create index ac_order_to_delivery_idx on o_ac_order (fk_delivery_id);
alter table o_ac_order_part add constraint ord_part_ord_ctx foreign key (fk_order_id) references o_ac_order (order_id);
alter table o_ac_order_line add constraint ord_item_ord_part_ctx foreign key (fk_order_part_id) references o_ac_order_part (order_part_id);
alter table o_ac_order_line add constraint ord_item_offer_ctx foreign key (fk_offer_id) references o_ac_offer (offer_id);
alter table o_ac_transaction add constraint trans_ord_ctx foreign key (fk_order_id) references o_ac_order (order_id);
alter table o_ac_transaction add constraint trans_ord_part_ctx foreign key (fk_order_part_id) references o_ac_order_part (order_part_id);
alter table o_ac_transaction add constraint trans_method_ctx foreign key (fk_method_id) references o_ac_method (method_id);
create index paypal_pay_key_idx on o_ac_paypal_transaction (pay_key);
create index paypal_pay_trx_id_idx on o_ac_paypal_transaction (ipn_transaction_id);
create index paypal_pay_s_trx_id_idx on o_ac_paypal_transaction (ipn_sender_transaction_id);

alter table o_im_message add constraint idx_im_msg_to_fromid foreign key (fk_from_identity_id) references o_bs_identity (id);
create index idx_im_msg_res_idx on o_im_message (msg_resid,msg_resname);
alter table o_im_notification add constraint idx_im_not_to_toid foreign key (fk_to_identity_id) references o_bs_identity (id);
alter table o_im_notification add constraint idx_im_not_to_fromid foreign key (fk_from_identity_id) references o_bs_identity (id);
create index idx_im_chat_res_idx on o_im_notification (chat_resid,chat_resname);
alter table o_im_roster_entry add constraint idx_im_rost_to_id foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_im_rost_res_idx on o_im_roster_entry (r_resid,r_resname);
alter table o_im_preferences add constraint idx_im_prfs_to_id foreign key (fk_from_identity_id) references o_bs_identity (id);

alter table o_tag add constraint FK6491FCA5A4FA5DC foreign key (fk_author_id) references o_bs_identity (id);

alter table o_bs_invitation add constraint FKF26C8375236F27X foreign key (fk_secgroup) references o_bs_secgroup (id);

alter table o_as_eff_statement add unique eff_statement_id_cstr (fk_identity, fk_resource_id), add constraint eff_statement_id_cstr foreign key (fk_identity) references o_bs_identity (id);
create index eff_statement_repo_key_idx on o_as_eff_statement (course_repo_key);

alter table o_as_user_course_infos add index user_course_infos_id_cstr (fk_identity), add constraint user_course_infos_id_cstr foreign key (fk_identity) references o_bs_identity (id);
alter table o_as_user_course_infos add index user_course_infos_res_cstr (fk_resource_id), add constraint user_course_infos_res_cstr foreign key (fk_resource_id) references o_olatresource (resource_id);
alter table o_as_user_course_infos add unique (fk_identity, fk_resource_id);

alter table o_qp_pool add constraint idx_qp_pool_owner_grp_id foreign key (fk_ownergroup) references o_bs_secgroup(id);

alter table o_qp_pool_2_item add constraint idx_qp_pool_2_item_pool_id foreign key (fk_pool_id) references o_qp_pool(id);
alter table o_qp_pool_2_item add constraint idx_qp_pool_2_item_item_id foreign key (fk_item_id) references o_qp_item(id);
alter table o_qp_pool_2_item add unique (fk_pool_id, fk_item_id);

alter table o_qp_share_item add constraint idx_qp_share_rsrc_id foreign key (fk_resource_id) references o_olatresource(resource_id);
alter table o_qp_share_item add constraint idx_qp_share_item_id foreign key (fk_item_id) references o_qp_item(id);
alter table o_qp_share_item add unique (fk_resource_id, fk_item_id);

alter table o_qp_item_collection add constraint idx_qp_coll_owner_id foreign key (fk_owner_id) references o_bs_identity(id);

alter table o_qp_collection_2_item add constraint idx_qp_coll_coll_id foreign key (fk_collection_id) references o_qp_item_collection(id);
alter table o_qp_collection_2_item add constraint idx_qp_coll_item_id foreign key (fk_item_id) references o_qp_item(id);
alter table o_qp_collection_2_item add unique (fk_collection_id, fk_item_id);

alter table o_qp_item add constraint idx_qp_pool_2_field_id foreign key (fk_taxonomy_level) references o_qp_taxonomy_level(id);
alter table o_qp_item add constraint idx_qp_item_owner_id foreign key (fk_ownergroup) references o_bs_secgroup(id);
alter table o_qp_item add constraint idx_qp_item_edu_ctxt_id foreign key (fk_edu_context) references o_qp_edu_context(id);
alter table o_qp_item add constraint idx_qp_item_type_id foreign key (fk_type) references o_qp_item_type(id);
alter table o_qp_item add constraint idx_qp_item_license_id foreign key (fk_license) references o_qp_license(id);

alter table o_qp_taxonomy_level add constraint idx_qp_field_2_parent_id foreign key (fk_parent_field) references o_qp_taxonomy_level(id);

alter table o_qp_item_type add unique (q_type(200));

alter table o_lti_outcome add constraint idx_lti_outcome_ident_id foreign key (fk_identity_id) references o_bs_identity(id);
alter table o_lti_outcome add constraint idx_lti_outcome_rsrc_id foreign key (fk_resource_id) references o_olatresource(resource_id);

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
