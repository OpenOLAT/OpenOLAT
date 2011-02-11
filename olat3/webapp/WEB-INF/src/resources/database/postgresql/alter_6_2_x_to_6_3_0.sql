drop table if exists o_loggingtable;
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
   version int8 not null,
   lastmodified timestamp not null,
   title varchar(255) not null,
   description text,
   primary key (checklist_id)
);

create table o_checkpoint (
   checkpoint_id int8 not null,
   version int8 not null,
   lastmodified timestamp not null,
   title varchar(255) not null,
   description text,
   mode varchar(64) not null,
   checklist_fk int8,
   primary key (checkpoint_id)
);

create table o_checkpoint_results (
   checkpoint_result_id bigint not null,
   version int8 not null,
   lastmodified timestamp not null,
   result bool not null,
   checkpoint_fk int8,
   identity_fk int8, 
   primary key (checkpoint_result_id)
);

alter table o_checkpoint_results add constraint FK9E30F4B661159ZZY foreign key (checkpoint_fk) references o_checkpoint;
alter table o_checkpoint_results add constraint FK9E30F4B661159ZZX foreign key (identity_fk) references o_bs_identity;
alter table o_checkpoint add constraint FK9E30F4B661159ZZZ foreign key (checklist_fk) references o_checklist;

-- new tables for new course-node 'project-broker'
create table o_projectbroker (
   projectbroker_id int8 not null,
   version int8 not null,
   creationdate timestamp,
   primary key (projectbroker_id)
);

create table o_projectbroker_project (
   project_id int8 not null,
   version int8 not null,
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

create index projectbroker_project_broker_idx on o_projectbroker_project (projectbroker_fk);
create index projectbroker_project_id_idx on o_projectbroker_project (project_id);
create index o_projectbroker_customfields_idx on o_projectbroker_customfields (fk_project_id);


alter table o_message add column numofwords int8;
update o_message set numofwords=0;
alter table o_message add column numofcharacters int8;
update o_message set numofcharacters=0;

-- new tables for user comments and ratings for blogs etc
create table o_userrating (
	rating_id int8 not null, 
	version int8 not null, 
	creationdate timestamp, 
	resname varchar(50) not null, 
	resid int8 not null, 
	ressubpath varchar(2048), 
	creator_id int8 not null,
	rating int8 not null, 
	primary key (rating_id)
);

create table o_usercomment (
	comment_id int8 not null, 
	version int8 not null, 
	creationdate timestamp, 
	resname varchar(50) not null, 
	resid int8 not null, 
	ressubpath varchar(2048), 
	creator_id int8 not null,
	commenttext text, 
	parent_key int8, 
	primary key (comment_id)
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

alter table o_user add column notification_interval varchar(16);
create index usr_notification_interval_idx on o_user (notification_interval);

alter table o_noti_pub add businesspath varchar(255);
alter table o_noti_sub drop column latestread;
alter table o_noti_sub drop column transresid;
alter table o_noti_sub drop column transsubidentifier;
alter table o_noti_sub drop column data;

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

--catalog-entry.name must have same length like repository-entry.displayname 
ALTER TABLE o_catentry ALTER name TYPE varchar(110);