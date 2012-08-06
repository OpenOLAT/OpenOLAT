create table o_forum (
   forum_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   primary key (forum_id)
);
create table o_property (
   id int8 not null,
   version int4 not null,
   lastmodified timestamp,
   creationdate timestamp,
   identity int8,
   grp int8,
   resourcetypename varchar(50),
   resourcetypeid int8,
   category varchar(33),
   name varchar(255) not null,
   floatvalue float(24),
   longvalue int8,
   stringvalue varchar(255),
   textvalue TEXT,
   primary key (id)
);
create table o_bs_secgroup (
   id int8 not null,
   version int4 not null,
   creationdate timestamp,
   primary key (id)
);
create table o_gp_business (
   group_id int8 not null,
   version int4 not null,
   lastmodified timestamp,
   creationdate timestamp,
   lastusage timestamp,
   businessgrouptype varchar(15) not null,
   groupname varchar(255),
   descr text,
   minparticipants int4,
   maxparticipants int4,
   waitinglist_enabled bool,
   autocloseranks_enabled bool,
   groupcontext_fk int8,
   fk_resource int8 unique,
   fk_ownergroup int8 unique,
   fk_partipiciantgroup int8 unique,
   fk_waitinggroup int8 unique,
   primary key (group_id)
);
create table o_gp_business_to_resource (
   g_id bigint not null,
   version int4 not null,
   creationdate timestamp,
   fk_resource int8 not null,
   fk_group int8 not null,
   primary key (g_id)
);
create table o_temporarykey (
   reglist_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   email varchar(255) not null,
   regkey varchar(255) not null,
   ip varchar(255) not null,
   mailsent bool not null,
   action varchar(255) not null,
   primary key (reglist_id)
);
create table o_bs_authentication (
   id int8 not null,
   version int4 not null,
   creationdate timestamp,
   identity_fk int8 not null,
   provider varchar(8),
   authusername varchar(255),
   credential varchar(255),
   primary key (id),
   unique (provider, authusername)
);
create table o_noti_pub (
   publisher_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   publishertype varchar(50) not null,
   data text,
   resname varchar(50),
   resid int8,
   subident varchar(128),
   businesspath varchar(255),
   state int4,
   latestnews timestamp not null,
   primary key (publisher_id)
);
create table o_qtiresultset (
   resultset_id int8 not null,
   version int4 not null,
   lastmodified timestamp not null,
   creationdate timestamp,
   identity_id int8 not null,
   olatresource_fk int8 not null,
   olatresourcedetail varchar(255) not null,
   assessmentid int8 not null,
   repositoryref_fk int8 not null,
   ispassed bool,
   score float(24),
   duration int8,
   primary key (resultset_id)
);
create table o_bs_identity (
   id int8 not null,
   version int4 not null,
   creationdate timestamp,
   lastlogin timestamp,
   name varchar(128) not null unique,
   status integer,
   fk_user_id int8 unique,
   primary key (id)
);
create table o_olatresource (
   resource_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   resname varchar(50) not null,
   resid int8 not null,
   primary key (resource_id),
   unique (resname, resid)
);
create table o_bs_namedgroup (
   id int8 not null,
   version int4 not null,
   creationdate timestamp,
   secgroup_id int8 not null,
   groupname varchar(16),
   primary key (id),
   unique (groupname)
);
create table o_catentry (
   id int8 not null,
   version int4 not null,
   creationdate timestamp,
   name varchar(110) not null,
   description text,
   externalurl varchar(255),
   fk_repoentry int8,
   fk_ownergroup int8 unique,
   type int4 not null,
   parent_id int8,
   primary key (id)
);
create table o_note (
   note_id int8 not null,
   version int4 not null,
   lastmodified timestamp,
   creationdate timestamp,
   owner_id int8,
   resourcetypename varchar(50) not null,
   resourcetypeid int8 not null,
   sub_type varchar(50),
   notetitle varchar(255),
   notetext text,
   primary key (note_id)
);
create table o_gp_bgcontext (
   groupcontext_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   name varchar(255) not null,
   descr text,
   grouptype varchar(15) not null,
   ownergroup_fk int8 unique,
   defaultcontext bool not null,
   primary key (groupcontext_id)
);
create table o_references (
   reference_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   source_id int8 not null,
   target_id int8 not null,
   userdata varchar(64),
   primary key (reference_id)
);
create table o_repositorymetadata (
   metadataelement_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   name varchar(255) not null,
   value text not null,
   fk_repositoryentry int8 not null,
   primary key (fk_repositoryentry, metadataelement_id)
);
create table o_user (
   user_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   language varchar(30),
   fontsize varchar(10),
   notification_interval varchar(16),
   receiverealmail varchar(16),
   presencemessagespublic bool,
   informsessiontimeout bool not null,
   primary key (user_id)
);
create table o_userproperty (
   fk_user_id int8 not null,
   propname varchar(255) not null,
   propvalue varchar(255),
   primary key (fk_user_id, propname)
);
create table o_gp_bgcontextresource_rel (
   groupcontextresource_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   oresource_id int8 not null,
   groupcontext_fk int8 not null,
   primary key (groupcontextresource_id)
);
create table o_message (
   message_id int8 not null,
   version int4 not null,
   lastmodified timestamp,
   creationdate timestamp,
   title varchar(100),
   body text,
   parent_id int8,
   topthread_id int8,
   creator_id int8 not null,
   modifier_id int8,
   forum_fk int8,
   statuscode int4,
   numofwords int4,
   numofcharacters int4,
   primary key (message_id)
);
create table o_gp_bgtoarea_rel (
   bgtoarea_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   group_fk int8 not null,
   area_fk int8 not null,
   primary key (bgtoarea_id),
   unique (group_fk, area_fk)
);
create table o_noti_sub (
   publisher_id int8 not null,
   version int4 not null,
   lastmodified timestamp,
   creationdate timestamp,
   fk_publisher int8 not null,
   fk_identity int8 not null,
   latestemailed timestamp,
   primary key (publisher_id),
   unique (fk_publisher, fk_identity)
);
create table o_qtiresult (
   result_id int8 not null,
   version int4 not null,
   lastmodified timestamp not null,
   creationdate timestamp,
   itemident varchar(255) not null,
   answer text,
   duration int8,
   score float(24),
   tstamp timestamp not null,
   ip varchar(255),
   resultset_fk int8,
   primary key (result_id)
);
create table o_bs_policy (
   id int8 not null,
   version int4 not null,
   creationdate timestamp,
   oresource_id int8 not null,
   group_id int8 not null,
   permission varchar(16) not null,
   apply_from timestamp default null,
   apply_to timestamp default null,
   primary key (id),
   unique (oresource_id, group_id, permission)
);
create table o_gp_bgarea (
   area_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   name varchar(255) not null,
   descr text,
   groupcontext_fk int8,
   fk_resource int8 default null,
   primary key (area_id)
);
create table o_repositoryentry (
   repositoryentry_id int8 not null,
   version int4 not null,
   lastmodified timestamp,
   creationdate timestamp,
   lastusage timestamp,
   softkey varchar(30) not null unique,
   displayname varchar(110) not null,
   resourcename varchar(100) not null,
   fk_olatresource int8 unique,
   fk_ownergroup int8 unique,
   fk_tutorgroup int8,
   fk_participantgroup int8,
   description text,
   initialauthor varchar(128) not null,
   accesscode int4 not null,
   membersonly boolean default false,
   statuscode int4,
   canlaunch bool not null,
   candownload bool not null,
   cancopy bool not null,
   canreference bool not null,
   launchcounter int8 not null,
   downloadcounter int8 not null,
   primary key (repositoryentry_id)
);
create table o_bookmark (
   bookmark_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   owner_id int8 not null,
   title varchar(255) not null,
   description text,
   detaildata varchar(255),
   displayrestype varchar(50) not null,
   olatrestype varchar(50) not null,
   olatreskey int8,
   primary key (bookmark_id)
);
create table o_bs_membership (
   id int8 not null,
   version int4 not null,
   lastmodified timestamp,
   creationdate timestamp,
   secgroup_id int8 not null,
   identity_id int8 not null,
   primary key (id),
   unique (secgroup_id, identity_id)
);

create table o_plock (
    plock_id int8 not null, 
    version int4 not null,
    creationdate timestamp, 
    asset varchar(255) not null unique, 
    primary key (plock_id)
);

create table hibernate_unique_key (
    next_hi integer
);

create table o_lifecycle (
   id bigint not null,
   version int4 not null,
   creationdate timestamp,
   persistenttypename varchar(50) not null,
   persistentref bigint not null,
   action varchar(50) not null,
   lctimestamp timestamp,
   uservalue text,
   primary key (id)
);

create table oc_lock (
	lock_id int8 not null, 
	version int4 not null,
	creationdate timestamp, 
	identity_fk int8 not null, 
	asset varchar(120) not null unique, 
	primary key (lock_id)
);
create index ocl_asset_idx on oc_lock (asset);
alter table oc_lock add constraint FK9E30F4B66115906D foreign key (identity_fk) references o_bs_identity;

create table o_readmessage (
	id int8 not null, 
	version int4 not null,
    creationdate timestamp,
	identity_id int8 not null, 
	forum_id int8 not null, 
	message_id int8 not null, 
	primary key (id));
	
create table o_loggingtable (
	log_id int8 not null,
	creationdate timestamp,
	sourceclass varchar(255),
	sessionid varchar(255) not null,
	user_id int8,
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
	simpleduration int8 not null,
	resourceadminaction bool not null,
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

create table o_checklist (
   checklist_id int8 not null,
   version int4 not null,
   lastmodified timestamp not null,
   title varchar(255) not null,
   description text,
   primary key (checklist_id)
);

create table o_checkpoint (
   checkpoint_id int8 not null,
   version int4 not null,
   lastmodified timestamp not null,
   title varchar(255) not null,
   description text,
   modestring varchar(64) not null,
   checklist_fk int8,
   primary key (checkpoint_id)
);
alter table o_checkpoint add constraint FK9E30F4B661159ZZZ foreign key (checklist_fk) references o_checklist;

create table o_checkpoint_results (
   checkpoint_result_id bigint not null,
   version int4 not null,
   lastmodified timestamp not null,
   result bool not null,
   checkpoint_fk int8,
   identity_fk int8, 
   primary key (checkpoint_result_id)
);

create table o_projectbroker (
   projectbroker_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   primary key (projectbroker_id)
);

create table o_projectbroker_project (
   project_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   title varchar(100),
   description text,
   state varchar(20),
   maxMembers integer,
   attachmentFileName varchar(100),
   mailNotificationEnabled bool not null,
   projectgroup_fk int8 not null,
   projectbroker_fk int8 not null,
   candidategroup_fk int8 not null,
   primary key (project_id)
);

create table o_projectbroker_customfields (
   fk_project_id int8 not null,
   propname varchar(255) not null,
   propvalue varchar(255),
   primary key (fk_project_id, propname)
);

create table o_usercomment (
	comment_id int8 not null, 
	version int4 not null, 
	creationdate timestamp, 
	resname varchar(50) not null, 
	resid int8 not null, 
	ressubpath varchar(2048), 
	creator_id int8 not null,
	commenttext text, 
	parent_key int8, 
	primary key (comment_id)
);
create table o_userrating (
	rating_id int8 not null, 
	version int4 not null, 
	creationdate timestamp, 
	lastmodified timestamp,
	resname varchar(50) not null, 
	resid int8 not null, 
	ressubpath varchar(2048), 
	creator_id int8 not null,
	rating int4 not null, 
	primary key (rating_id)
);
create table o_info_message (
  info_id int8  NOT NULL,
  version int4 NOT NULL,
  creationdate timestamp,
  modificationdate timestamp,
  title varchar(2048),
  message varchar(2048),
  resname varchar(50) NOT NULL,
  resid int8 NOT NULL,
  ressubpath varchar(2048),
  businesspath varchar(2048),
  fk_author_id int8,
  fk_modifier_id int8,
  primary key (info_id)
) ;

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

-- eportfolio arteafcts
create table o_ep_artefact (
  artefact_id int8 not null,
  artefact_type varchar(32) not null,
  version int4 not null,
  creationdate timestamp,
  collection_date timestamp,
  title varchar(512),
  description varchar(4000),
  signature int4 default 0,
  businesspath varchar(2048),
  fulltextcontent text,
  reflexion text,
  source varchar(2048),
  add_prop1 varchar(2048),
  add_prop2 varchar(2048),
  add_prop3 varchar(2048),
  fk_struct_el_id int8,
  fk_artefact_auth_id int8 not null,
  primary key (artefact_id)
);

-- eportfolio collect restrictions
create table o_ep_collect_restriction (
  collect_id int8 not null,
  version int4 not null,
  creationdate timestamp,
  artefact_type varchar(256),
  amount int4 not null default -1,
  restriction varchar(32),
  pos int4 not null default 0,
  fk_struct_el_id int8,
  primary key (collect_id)
);

-- eportfolio structure element
create table o_ep_struct_el (
  structure_id int8 not null,
  structure_type varchar(32) not null,
  version int4 not null,
  creationdate timestamp,
  returndate timestamp default null,
  copydate timestamp default null,
  lastsyncheddate timestamp default null,
  deadline timestamp default null,
  title varchar(512),
  description varchar(2048),
  struct_el_source int8,
  target_resname varchar(50),
  target_resid int8,
  target_ressubpath varchar(2048),
  target_businesspath varchar(2048),
  style varchar(128),  
  status varchar(32),
  viewmode varchar(32),
  fk_struct_root_id int8,
  fk_struct_root_map_id int8,
  fk_map_source_id int8,
  fk_ownergroup int8,
  fk_olatresource int8 not null,
  primary key (structure_id)  
);

-- eportfolio structure to structure link
create table o_ep_struct_struct_link (
  link_id int8 not null,
  version int4 not null,
  creationdate timestamp,
  pos int4 not null default 0,
  fk_struct_parent_id int8 not null,
  fk_struct_child_id int8 not null,
  primary key (link_id)
);

-- eportfolio structure to artefact link
create table o_ep_struct_artefact_link (
  link_id int8 not null,
  version int4 not null,
  creationdate timestamp,
  pos int4 not null default 0,
  reflexion text,
  fk_auth_id int8,
  fk_struct_id int8 not null,
  fk_artefact_id int8 not null,
  primary key (link_id)
);

-- invitation
create table o_bs_invitation (
   id int8 not null,
   version int4 not null,
   creationdate timestamp,
   token varchar(64) not null,
   first_name varchar(64),
   last_name varchar(64),
   mail varchar(128),
   fk_secgroup int8,
   primary key (id)
);

-- tagging
create table o_tag (
  tag_id int8 not null,
  version int4 not null,
  creationdate timestamp,
  tag varchar(128) not null,
  resname varchar(50) not null,
  resid int8 not null,
  ressubpath varchar(2048),
  businesspath varchar(2048),
  fk_author_id int8 not null,
  primary key (tag_id)
);

create table o_mail (
  mail_id int8 not null,
  meta_mail_id varchar(64),
  creationdate timestamp,
	lastmodified timestamp,
	resname varchar(50),
  resid int8,
  ressubpath varchar(2048),
  businesspath varchar(2048),
  subject varchar(512),
  body text,
  fk_from_id int8,
  primary key (mail_id)
);

-- mail recipient
create table o_mail_to_recipient (
  pos int4 NOT NULL default 0,
  fk_mail_id int8,
  fk_recipient_id int8
);

create table o_mail_recipient (
  recipient_id int8 NOT NULL,
  recipientvisible boolean,
  deleted boolean,
  mailread boolean,
  mailmarked boolean,
  email varchar(255),
  recipientgroup varchar(255),
  creationdate timestamp,
  fk_recipient_id int8,
  primary key (recipient_id)
);

-- mail attachments
create table o_mail_attachment (
	attachment_id int8 NOT NULL,
  creationdate timestamp,
	datas bytea,
	datas_size int8,
	datas_name varchar(255),
	mimetype varchar(255),
  fk_att_mail_id int8,
	primary key (attachment_id)
);

-- access control
create table o_ac_offer (
	offer_id int8 NOT NULL,
  creationdate timestamp,
	lastmodified timestamp,
	is_valid boolean default true,
	validfrom timestamp,
	validto timestamp,
  version int4 not null,
  resourceid int8,
  resourcetypename varchar(255),
  resourcedisplayname varchar(255),
  token varchar(255),
	price_amount DECIMAL,
	price_currency_code VARCHAR(3),
	offer_desc VARCHAR(2000),
  fk_resource_id int8,
	primary key (offer_id)
);
create table o_ac_method (
	method_id int8 NOT NULL,
	access_method varchar(32),
  version int4 not null,
  creationdate timestamp,
	lastmodified timestamp,
	is_valid boolean default true,
	is_enabled boolean default true,
	validfrom timestamp,
	validto timestamp,
	primary key (method_id)
);
create table o_ac_offer_access (
	offer_method_id int8 NOT NULL,
  version int4 not null,
  creationdate timestamp,
	is_valid boolean default true,
	validfrom timestamp,
	validto timestamp,
  fk_offer_id int8,
  fk_method_id int8,
	primary key (offer_method_id)
);
-- access cart
create table o_ac_order (
	order_id int8 NOT NULL,
  version int4 not null,
  creationdate timestamp,
	lastmodified timestamp,
	is_valid boolean default true,
	total_lines_amount DECIMAL,
	total_lines_currency_code VARCHAR(3),
	total_amount DECIMAL,
	total_currency_code VARCHAR(3),
	discount_amount DECIMAL,
	discount_currency_code VARCHAR(3),
	order_status VARCHAR(32) default 'NEW',
  fk_delivery_id int8,
	primary key (order_id)
);
create table o_ac_order_part (
	order_part_id int8 NOT NULL,
  version int4 not null,
  pos int4,
  creationdate timestamp,
  total_lines_amount DECIMAL,
	total_lines_currency_code VARCHAR(3),
	total_amount DECIMAL,
	total_currency_code VARCHAR(3),
  fk_order_id int8,
	primary key (order_part_id)
);
create table o_ac_order_line (
	order_item_id int8 NOT NULL,
  version int4 not null,
  pos int4,
  creationdate timestamp,
  unit_price_amount DECIMAL,
	unit_price_currency_code VARCHAR(3),
	total_amount DECIMAL,
	total_currency_code VARCHAR(3),
  fk_order_part_id int8,
  fk_offer_id int8,
	primary key (order_item_id)
); 
create table o_ac_transaction (
	transaction_id int8 NOT NULL,
  version int4 not null,
  creationdate timestamp,
  trx_status VARCHAR(32) default 'NEW',
	amount_amount DECIMAL,
	amount_currency_code VARCHAR(3),
  fk_order_part_id int8,
  fk_order_id int8,
  fk_method_id int8,
	primary key (transaction_id)
);


create table o_stat_lastupdated (

	lastupdated timestamp not null

);
-- important: initialize with old date!
insert into o_stat_lastupdated values(date('1999-01-01'));


--insert into o_stat_dayofweek (businesspath,resid,day,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,dayofweek(creationdate) day,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,day;
create table o_stat_dayofweek (

	id bigserial,
	businesspath varchar(2048) not null,
	resid int8 not null,
	day int4 not null,
	value int4 not null,
	primary key (id)

);
create index statdow_resid_idx on o_stat_dayofweek (resid);


--insert into o_stat_hourofday (businesspath,resid,hour,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,hour(creationdate) hour,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,hour;
create table o_stat_hourofday (

	id bigserial,
	businesspath varchar(2048) not null,
	resid int8 not null,
	hour int4 not null,
	value int4 not null,
	primary key (id)

);
create index stathod_resid_idx on o_stat_hourofday (resid);


--insert into o_stat_weekly (businesspath,resid,week,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,concat(year(creationdate),'-',week(creationdate)) week,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,week;
create table o_stat_weekly (

	id bigserial,
	businesspath varchar(2048) not null,
	resid int8 not null,
	week varchar(7) not null,
	value int4 not null,
	primary key (id)

);
create index statwee_resid_idx on o_stat_weekly (resid);


--insert into o_stat_daily (businesspath,resid,day,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,date(creationdate) day,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,day;
create table o_stat_daily (

	id bigserial,
	businesspath varchar(2048) not null,
	resid int8 not null,
	day timestamp not null,
	value int4 not null,
	primary key (id)

);
create index statday_resid_idx on o_stat_daily (resid);


--insert into o_stat_homeorg (businesspath,resid,homeorg,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,userproperty2 homeorg,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,homeorg;
create table o_stat_homeorg (

	id bigserial,
	businesspath varchar(2048) not null,
	resid int8 not null,
	homeorg varchar(255) not null,
	value int4 not null,
	primary key (id)

);
create index stathor_resid_idx on o_stat_homeorg (resid);


--insert into o_stat_orgtype (businesspath,resid,orgtype,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,userproperty4 orgtype,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,orgtype;
create table o_stat_orgtype (

	id bigserial,
	businesspath varchar(2048) not null,
	resid int8 not null,
	orgtype varchar(255),
	value int4 not null,
	primary key (id)

);
create index statorg_resid_idx on o_stat_orgtype (resid);


--insert into o_stat_studylevel (businesspath,resid,studylevel,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,userproperty3 studylevel,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,studylevel;
create table o_stat_studylevel (

	id bigserial,
	businesspath varchar(2048) not null,
	resid int8 not null,
	studylevel varchar(255) not null,
	value int4 not null,
	primary key (id)

);
create index statstl_resid_idx on o_stat_studylevel (resid);


--insert into o_stat_studybranch3 (businesspath,resid,studybranch3,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,userproperty10 studybranch3,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,studybranch3;
create table o_stat_studybranch3 (

	id bigserial,
	businesspath varchar(2048) not null,
	resid int8 not null,
	studybranch3 varchar(255),
	value int4 not null,
	primary key (id)

);
create index statstb_resid_idx on o_stat_studybranch3 (resid);

create table o_mark (
  mark_id int8 not null,
  version int4 not null,
  creationdate timestamp,
  resname varchar(50) not null,
  resid int8 not null,
  ressubpath varchar(2048),
  businesspath varchar(2048),
  creator_id int8 not null,
  primary key (mark_id)
);

-- efficiency statments
create table o_as_eff_statement (
   id int8 not null,
   version int4 not null,
   lastmodified timestamp,
   creationdate timestamp,
   passed boolean,
   score float4,
   total_nodes int4,
   attempted_nodes int4,
   passed_nodes int4,
   course_title varchar(255),
   course_short_title varchar(128),
   course_repo_key int8,
   statement_xml text,
   fk_identity int8,
   fk_resource_id int8,
   unique(fk_identity, fk_resource_id),
   primary key (id)
);

-- user to course informations (was property initial and recent launch dates)
create table o_as_user_course_infos (
   id int8 not null,
   version int4 not null,
   creationdate timestamp,
   lastmodified timestamp,
   initiallaunchdate timestamp,
   recentlaunchdate timestamp,
   visit int4,
   timespend int8,
   fk_identity int8,
   fk_resource_id int8,
   unique(fk_identity, fk_resource_id),
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

-- assessment results
-- help view
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
      cast(urating.ressubpath as int8) as page_key,
      page.title as page_title,
      urating.creator_id as author_id,
      urating.creationdate as creation_date,
      urating.lastmodified as last_modified 
   from o_userrating as urating
   inner join o_olatresource as rating_resource on (rating_resource.resid = urating.resid and rating_resource.resname = urating.resname)
   inner join o_ep_struct_el as map on (map.fk_olatresource = rating_resource.resource_id)
   left join o_ep_struct_el as page on (page.fk_struct_root_map_id = map.structure_id and page.structure_id = cast(urating.ressubpath as integer))
);

create or replace view o_ep_notifications_comment_v as (
   select
      ucomment.comment_id as comment_id,
      map.structure_id as map_id,
      map.title as map_title,
      cast(ucomment.ressubpath as int8) as page_key,
      page.title as page_title,
      ucomment.creator_id as author_id,
      ucomment.creationdate as creation_date
   from o_usercomment as ucomment
   inner join o_olatresource as comment_resource on (comment_resource.resid = ucomment.resid and comment_resource.resname = ucomment.resname)
   inner join o_ep_struct_el as map on (map.fk_olatresource = comment_resource.resource_id)
   left join o_ep_struct_el as page on (page.fk_struct_root_map_id = map.structure_id and page.structure_id = cast(ucomment.ressubpath as integer))
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
      (select count(own.id) from o_bs_membership as own where own.secgroup_id = gp.fk_ownergroup) as num_of_owners,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=true
         and (offer.validfrom is null or offer.validfrom >= current_timestamp)
         and (offer.validto is null or offer.validto <= current_timestamp)
      ) as num_of_valid_offers,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=true
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

create or replace view o_re_member_v as (
   select
      re.repositoryentry_id as re_id,
      re.membersonly as re_membersonly,
      re.accesscode as re_accesscode,
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
   where re.membersonly=true and re.accesscode=1
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
   where re.membersonly=true and re.accesscode=1
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
   where re.membersonly=true and re.accesscode=1
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
      (select count(own.id) from o_bs_membership as own where own.secgroup_id = gp.fk_ownergroup) as num_of_owners,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=true
         and (offer.validfrom is null or offer.validfrom >= current_timestamp)
         and (offer.validto is null or offer.validto <= current_timestamp)
      ) as num_of_valid_offers,
      (select count(offer.offer_id) from o_ac_offer as offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=true
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

create or replace view o_re_member_v as (
   select
      re.repositoryentry_id as re_id,
      re.membersonly as re_membersonly,
      re.accesscode as re_accesscode,
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
   where re.membersonly=true and re.accesscode=1
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
   where re.membersonly=true and re.accesscode=1
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
   where re.membersonly=true and re.accesscode=1
);

create index userrating_id_idx on o_userrating (resid);
create index userrating_name_idx on o_userrating (resname);
create index userrating_subpath_idx on o_userrating (ressubpath);
create index userrating_rating_idx on o_userrating (rating);
create index FKF26C8375236F20X on o_userrating (creator_id);
alter table o_userrating add constraint FKF26C8375236F20X foreign key (creator_id) references o_bs_identity (id);

create index usercmt_id_idx on o_usercomment (resid);
create index usercmt_name_idx on o_usercomment (resname);
create index usercmt_subpath_idx on o_usercomment (ressubpath);
create index FK92B6864A18251F0 on o_usercomment (parent_key);
create index FKF26C8375236F20A on o_usercomment (creator_id);
alter table o_usercomment add constraint FK92B6864A18251F0 foreign key (parent_key) references o_usercomment (comment_id);
alter table o_usercomment add constraint FKF26C8375236F20A foreign key (creator_id) references o_bs_identity (id);

alter table o_checkpoint_results add constraint FK9E30F4B661159ZZY foreign key (checkpoint_fk) references o_checkpoint;
alter table o_checkpoint_results add constraint FK9E30F4B661159ZZX foreign key (identity_fk) references o_bs_identity;

create index asset_idx on o_plock (asset);
create index resid_idx1 on o_property (resourcetypeid);
create index category_idx on o_property (category);
create index name_idx1 on o_property (name);
create index restype_idx1 on o_property (resourcetypename);
alter table o_property add constraint FKB60B1BA5190E5 foreign key (grp) references o_gp_business;
alter table o_property add constraint FKB60B1BA5F7E870BE foreign key (identity) references o_bs_identity;
create index gp_name_idx on o_gp_business (groupname);
create index gp_type_idx on o_gp_business (businessgrouptype);
alter table o_gp_business add constraint FKCEEB8A86DF6BCD14 foreign key (groupcontext_fk) references o_gp_bgcontext;
alter table o_gp_business add constraint FKCEEB8A86A1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup;
alter table o_gp_business add constraint FKCEEB8A86C06E3EF3 foreign key (fk_partipiciantgroup) references o_bs_secgroup;
alter table o_gp_business add constraint idx_bgp_rsrc foreign key (fk_resource) references o_olatresource (resource_id);
alter table o_gp_business add constraint idx_bgp_waiting foreign key (fk_waitinggroup) references o_bs_secgroup (id);
alter table o_gp_business_to_resource add constraint idx_bgp_to_rsrc_rsrc foreign key (fk_resource) references o_olatresource (resource_id);
alter table o_gp_business_to_resource add constraint idx_bgp_to_rsrc_group foreign key (fk_group) references o_gp_business (group_id);
create index provider_idx on o_bs_authentication (provider);
create index credential_idx on o_bs_authentication (credential);
create index authusername_idx on o_bs_authentication (authusername);
alter table o_bs_authentication add constraint FKC6A5445652595FE6 foreign key (identity_fk) references o_bs_identity;
create index name_idx2 on o_noti_pub (resname, resid, subident);
create index oresdetindex on o_qtiresultset (olatresourcedetail);
create index oresindex on o_qtiresultset (olatresource_fk);
create index reprefindex on o_qtiresultset (repositoryref_fk);
create index assindex on o_qtiresultset (assessmentid);
alter table o_qtiresultset add constraint FK14805D0F5259603C foreign key (identity_id) references o_bs_identity;
create index name_idx3 on o_bs_identity (name);
create index identstatus_idx on o_bs_identity (status);
alter table o_bs_identity add constraint FKFF94111CD1A80C95 foreign key (fk_user_id) references o_user;
alter table o_userproperty add constraint FK4B04D83FD1A80C95 foreign key (fk_user_id) references o_user;

create index  name_idx4 on o_olatresource (resname);
create index  id_idx on o_olatresource (resid);

create index groupname_idx on o_bs_namedgroup (groupname);
alter table o_bs_namedgroup add constraint FKBAFCBBC4B85B522C foreign key (secgroup_id) references o_bs_secgroup;
alter table o_catentry add constraint FKF4433C2C7B66B0D0 foreign key (parent_id) references o_catentry;
alter table o_catentry add constraint FKF4433C2CA1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup;
alter table o_catentry add constraint FKF4433C2CDDD69946 foreign key (fk_repoentry) references o_repositoryentry;
create index resid_idx2 on o_note (resourcetypeid);
create index owner_idx on o_note (owner_id);
create index restype_idx2 on o_note (resourcetypename);
alter table o_note add constraint FKC2D855C263219E27 foreign key (owner_id) references o_bs_identity;
create index type_idx on o_gp_bgcontext (grouptype);
create index default_idx on o_gp_bgcontext (defaultcontext);
create index name_idx5 on o_gp_bgcontext (name);
alter table o_gp_bgcontext add constraint FK1C154FC47E4A0638 foreign key (ownergroup_fk) references o_bs_secgroup;
alter table o_references add constraint FKE971B4589AC44FBF foreign key (source_id) references o_olatresource;
alter table o_references add constraint FKE971B458CF634A89 foreign key (target_id) references o_olatresource;
alter table o_repositorymetadata add constraint FKDB97A6493F14E3EE foreign key (fk_repositoryentry) references o_repositoryentry;
create index propvalue_idx on o_userproperty (propvalue);
alter table o_gp_bgcontextresource_rel add constraint FK9903BEAC9F9C3F1D foreign key (oresource_id) references o_olatresource;
alter table o_gp_bgcontextresource_rel add constraint FK9903BEACDF6BCD14 foreign key (groupcontext_fk) references o_gp_bgcontext;
alter table o_message add constraint FKF26C8375236F20E foreign key (creator_id) references o_bs_identity;
alter table o_message add constraint FKF26C837A3FBEB83 foreign key (modifier_id) references o_bs_identity;
alter table o_message add constraint FKF26C8377B66B0D0 foreign key (parent_id) references o_message;
alter table o_message add constraint FKF26C8378EAC1DBB foreign key (topthread_id) references o_message;
alter table o_message add constraint FKF26C8371CB7C4A3 foreign key (forum_fk) references o_forum;
alter table o_gp_bgtoarea_rel add constraint FK9B663F2D1E2E7685 foreign key (group_fk) references o_gp_business;
alter table o_gp_bgtoarea_rel add constraint FK9B663F2DD381B9B7 foreign key (area_fk) references o_gp_bgarea;
alter table o_noti_sub add constraint FK4FB8F04749E53702 foreign key (fk_publisher) references o_noti_pub;
alter table o_noti_sub add constraint FK4FB8F0476B1F22F8 foreign key (fk_identity) references o_bs_identity;
create index itemindex on o_qtiresult (itemident);
alter table o_qtiresult add constraint FK3563E67340EF401F foreign key (resultset_fk) references o_qtiresultset;
alter table o_bs_policy add constraint FK9A1C5109F9C3F1D foreign key (oresource_id) references o_olatresource;
alter table o_bs_policy add constraint FK9A1C5101E2E76DB foreign key (group_id) references o_bs_secgroup;
create index name_idx6 on o_gp_bgarea (name);
alter table o_gp_bgarea add constraint FK9EFAF698DF6BCD14 foreign key (groupcontext_fk) references o_gp_bgcontext;
create index descritpion_idx on o_repositoryentry (description);
create index access_idx on o_repositoryentry (accesscode);
create index initialAuthor_idx on o_repositoryentry (initialauthor);
create index resource_idx on o_repositoryentry (resourcename);
create index displayname_idx on o_repositoryentry (displayname);
create index softkey_idx on o_repositoryentry (softkey);
alter table o_repositoryentry add constraint FK2F9C439888C31018 foreign key (fk_olatresource) references o_olatresource;
alter table o_repositoryentry add constraint FK2F9C4398A1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup;
create index repo_members_only_idx on o_repositoryentry (membersonly);
alter table o_repositoryentry add constraint repo_tutor_sec_group_ctx foreign key (fk_tutorgroup) references o_bs_secgroup (id);
alter table o_repositoryentry add constraint repo_parti_sec_group_ctx foreign key (fk_participantgroup) references o_bs_secgroup (id);
alter table o_bookmark add constraint FK68C4E30663219E27 foreign key (owner_id) references o_bs_identity;
alter table o_bs_membership add constraint FK7B6288B45259603C foreign key (identity_id) references o_bs_identity;
alter table o_bs_membership add constraint FK7B6288B4B85B522C foreign key (secgroup_id) references o_bs_secgroup;
create index lc_pref_idx on o_lifecycle (persistentref);
create index lc_type_idx on o_lifecycle (persistenttypename);
create index lc_action_idx on o_lifecycle (action);
create index readmessage_forum_idx on o_readmessage (forum_id);
create index readmessage_identity_idx on o_readmessage (identity_id);
create index projectbroker_project_broker_idx on o_projectbroker_project (projectbroker_fk);
create index projectbroker_project_id_idx on o_projectbroker_project (project_id);
create index o_projectbroker_customfields_idx on o_projectbroker_customfields (fk_project_id);
create index usr_notification_interval_idx on o_user (notification_interval);

create index mark_id_idx on o_mark (resid);
create index mark_name_idx on o_mark (resname);
create index mark_subpath_idx on o_mark (ressubpath);
create index mark_businesspath_idx on o_mark (businesspath);
create index FKF26C8375236F21X on o_mark (creator_id);
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

alter table o_bs_invitation add constraint FKF26C8375236F27X foreign key (fk_secgroup) references o_bs_secgroup (id);

alter table o_tag add constraint FK6491FCA5A4FA5DC foreign key (fk_author_id) references o_bs_identity (id);

alter table o_mail_to_recipient add constraint FKF86663165A4FA5DE foreign key (fk_mail_id) references o_mail (mail_id);
alter table o_mail_recipient add constraint FKF86663165A4FA5DG foreign key (fk_recipient_id) references o_bs_identity (id);
alter table o_mail add constraint FKF86663165A4FA5DC foreign key (fk_from_id) references o_mail_recipient (recipient_id);
alter table o_mail_to_recipient add constraint FKF86663165A4FA5DD foreign key (fk_recipient_id) references o_mail_recipient (recipient_id);
alter table o_mail_attachment add constraint FKF86663165A4FA5DF foreign key (fk_att_mail_id) references o_mail (mail_id);

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

alter table o_as_eff_statement add constraint eff_statement_id_cstr foreign key (fk_identity) references o_bs_identity (id);
create index eff_statement_repo_key_idx on o_as_eff_statement (course_repo_key);
alter table o_as_user_course_infos add constraint user_course_infos_id_cstr foreign key (fk_identity) references o_bs_identity (id);
alter table o_as_user_course_infos add constraint user_course_infos_res_cstr foreign key (fk_resource_id) references o_olatresource (resource_id);


insert into hibernate_unique_key values ( 0 );
