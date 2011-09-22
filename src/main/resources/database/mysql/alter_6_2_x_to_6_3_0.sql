create table o_loggingtable (

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

create table o_checklist (
   checklist_id bigint not null,
   version bigint not null,
   lastmodified datetime not null,
   title varchar(255) not null,
   description text,
   primary key (checklist_id)
);

create table o_checkpoint (
   checkpoint_id bigint not null,
   version bigint not null,
   lastmodified datetime not null,
   title varchar(255) not null,
   description text,
   mode varchar(64) not null,
   checklist_fk bigint,
   primary key (checkpoint_id)
);

create table o_checkpoint_results (
   checkpoint_result_id bigint not null,
   version bigint not null,
   lastmodified datetime not null,
   result bool not null,
   checkpoint_fk bigint,
   identity_fk bigint, 
   primary key (checkpoint_result_id)
);

alter table o_checklist type = InnoDB;
alter table o_checkpoint type = InnoDB;
alter table o_checkpoint_results type = InnoDB;

alter table o_checkpoint_results add constraint FK9E30F4B661159ZZY foreign key (checkpoint_fk) references o_checkpoint (checkpoint_id) ;
alter table o_checkpoint_results add constraint FK9E30F4B661159ZZX foreign key (identity_fk) references o_bs_identity (id);
alter table o_checkpoint add constraint FK9E30F4B661159ZZZ foreign key (checklist_fk) references o_checklist (checklist_id);
--
-- new tables for new course-node 'project-broker'
create table o_projectbroker (
   projectbroker_id bigint not null,
   version bigint not null,
   creationdate datetime,
   primary key (projectbroker_id)
);

create table o_projectbroker_project (
   project_id bigint not null,
   version bigint not null,
   creationdate datetime,
   title varchar(100),
   description text,
   state varchar(20),
   maxMembers integer,
   attachmentFileName varchar(100),
   mailNotificationEnabled boolean not null,
   projectgroup_fk bigint not null,
   projectbroker_fk bigint not null,
   candidategroup_fk bigint not null,
   primary key (project_id)
);

create table o_projectbroker_customfields (
   fk_project_id bigint not null,
   propname varchar(255) not null,
   propvalue varchar(255),
   primary key (fk_project_id, propname)
);

alter table o_projectbroker type = InnoDB;
alter table o_projectbroker_project type = InnoDB;
alter table o_projectbroker_customfields type = InnoDB;

create index projectbroker_project_broker_idx on o_projectbroker_project (projectbroker_fk);
create index projectbroker_project_id_idx on o_projectbroker_project (project_id);
create index o_projectbroker_customfields_idx on o_projectbroker_customfields (fk_project_id);
--
-- new fields for forum messages to count words and characters
alter table o_message add column numofwords integer;
update o_message set numofwords=0;
alter table o_message add column numofcharacters integer;
update o_message set numofcharacters=0;

-- 
-- new tables for user comments and ratings for blogs etc
create table o_usercomment (
	comment_id bigint not null, 
	version integer not null, 
	creationdate datetime, 
	resname varchar(50) not null, 
	resid bigint not null, 
	ressubpath varchar(2048), 
    creator_id bigint not null,
	commenttext text, 
	parent_key bigint, 
	primary key (comment_id)
);
alter table o_usercomment type = InnoDB;
create index cmt_id_idx on o_usercomment (resid);
create index cmt_name_idx on o_usercomment (resname);
create index cmt_subpath_idx on o_usercomment (ressubpath);
alter table o_usercomment add index FK92B6864A18251F0 (parent_key), add constraint FK92B6864A18251F0 foreign key (parent_key) references o_usercomment (comment_id);
alter table o_usercomment add index FKF26C8375236F20A (creator_id), add constraint FKF26C8375236F20A foreign key (creator_id) references o_bs_identity (id);

create table o_userrating (
	rating_id bigint not null, 
	version integer not null, 
	creationdate datetime, 
	resname varchar(50) not null, 
	resid bigint not null, 
	ressubpath varchar(2048), 
    creator_id bigint not null,
	rating integer not null, 
	primary key (rating_id)
);
alter table o_userrating type = InnoDB;
create index rtn_id_idx on o_userrating (resid);
create index rtn_name_idx on o_userrating (resname);
create index rtn_subpath_idx on o_userrating (ressubpath);
create index rtn_rating_idx on o_userrating (rating);
alter table o_userrating add index FKF26C8375236F20X (creator_id), add constraint FKF26C8375236F20X foreign key (creator_id) references o_bs_identity (id);
--
-- Add new notification confiuration field
alter table o_user add notification_interval varchar(16) after fontsize;
create index usr_notification_interval_idx on o_user (notification_interval);

alter table o_noti_pub add businesspath varchar(255);
alter table o_noti_sub drop latestread;
alter table o_noti_sub drop transresid;
alter table o_noti_sub drop transsubidentifier;
alter table o_noti_sub drop data;



create table if not exists o_stat_lastupdated (

	lastupdated datetime not null

);
-- important: initialize with old date!
insert into o_stat_lastupdated values(date('1999-01-01'));


--insert into o_stat_dayofweek (businesspath,resid,day,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,dayofweek(creationdate) day,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,day;
create table if not exists o_stat_dayofweek (

	id bigint not null auto_increment,
	businesspath varchar(2048) not null,
	resid bigint not null,
	day int not null,
	value int not null,
	primary key (id)

);
create index statdow_resid_idx on o_stat_dayofweek (resid);


--insert into o_stat_hourofday (businesspath,resid,hour,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,hour(creationdate) hour,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,hour;
create table if not exists o_stat_hourofday (

	id bigint not null auto_increment,
	businesspath varchar(2048) not null,
	resid bigint not null,
	hour int not null,
	value int not null,
	primary key (id)

);
create index stathod_resid_idx on o_stat_hourofday (resid);


--insert into o_stat_weekly (businesspath,resid,week,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,concat(year(creationdate),'-',week(creationdate)) week,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,week;
create table if not exists o_stat_weekly (

	id bigint not null auto_increment,
	businesspath varchar(2048) not null,
	resid bigint not null,
	week varchar(7) not null,
	value int not null,
	primary key (id)

);
create index statwee_resid_idx on o_stat_weekly (resid);


--insert into o_stat_daily (businesspath,resid,day,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,date(creationdate) day,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,day;
create table if not exists o_stat_daily (

	id bigint not null auto_increment,
	businesspath varchar(2048) not null,
	resid bigint not null,
	day datetime not null,
	value int not null,
	primary key (id)

);
create index statday_resid_idx on o_stat_daily (resid);


--insert into o_stat_homeorg (businesspath,resid,homeorg,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,userproperty2 homeorg,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,homeorg;
create table if not exists o_stat_homeorg (

	id bigint not null auto_increment,
	businesspath varchar(2048) not null,
	resid bigint not null,
	homeorg varchar(255) not null,
	value int not null,
	primary key (id)

);
create index stathor_resid_idx on o_stat_homeorg (resid);


--insert into o_stat_orgtype (businesspath,resid,orgtype,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,userproperty4 orgtype,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,orgtype;
create table if not exists o_stat_orgtype (

	id bigint not null auto_increment,
	businesspath varchar(2048) not null,
	resid bigint not null,
	orgtype varchar(255),
	value int not null,
	primary key (id)

);
create index statorg_resid_idx on o_stat_orgtype (resid);


--insert into o_stat_studylevel (businesspath,resid,studylevel,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,userproperty3 studylevel,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,studylevel;
create table if not exists o_stat_studylevel (

	id bigint not null auto_increment,
	businesspath varchar(2048) not null,
	resid bigint not null,
	studylevel varchar(255) not null,
	value int not null,
	primary key (id)

);
create index statstl_resid_idx on o_stat_studylevel (resid);


--insert into o_stat_studybranch3 (businesspath,resid,studybranch3,value) select businesspath,substr(businesspath,locate(':',businesspath)+1,locate(']',businesspath)-locate(':',businesspath)-1) resid,userproperty10 studybranch3,count(*) cnt from o_loggingtable where actionverb='launch' and actionobject='node' group by businesspath,studybranch3;
create table if not exists o_stat_studybranch3 (

	id bigint not null auto_increment,
	businesspath varchar(2048) not null,
	resid bigint not null,
	studybranch3 varchar(255),
	value int not null,
	primary key (id)

);
create index statstb_resid_idx on o_stat_studybranch3 (resid);

--catalog-entry.name must have same length like repository-entry.displayname 
alter table o_catentry modify name varchar(110);