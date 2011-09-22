-- tables
-- 
-- Created by SQL::Translator::Producer::Oracle
-- Created on Sat Mar  6 13:48:52 2010
-- 
-- We assume that default NLS_DATE_FORMAT has been changed
-- but we set it here anyway to be self-consistent.
ALTER SESSION SET NLS_DATE_FORMAT = 'YYYY-MM-DD HH24:MI:SS';

--
-- Table: o_forum
--;

CREATE TABLE o_forum (
  forum_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  PRIMARY KEY (forum_id)
);

--
-- Table: o_property
--;

CREATE TABLE o_property (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  lastmodified date,
  creationdate date,
  identity number(20),
  grp number(20),
  resourcetypename varchar2(50 char),
  resourcetypeid number(20),
  category varchar2(33 char),
  name varchar2(255 char) NOT NULL,
  floatvalue float,
  longvalue number(20),
  stringvalue varchar2(255 char),
  textvalue varchar2(4000),
  PRIMARY KEY (id)
);

--
-- Table: o_bs_secgroup
--;

CREATE TABLE o_bs_secgroup (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  PRIMARY KEY (id)
);

--
-- Table: o_gp_business
--;

CREATE TABLE o_gp_business (
  group_id number(20) NOT NULL,
  version number(20) NOT NULL,
  lastmodified date,
  creationdate date,
  lastusage date,
  businessgrouptype varchar2(15 char) NOT NULL,
  groupname varchar2(255 char),
  descr varchar2(4000),
  minparticipants number(11),
  maxparticipants number(11),
  waitinglist_enabled number,
  autocloseranks_enabled number,
  groupcontext_fk number(20),
  fk_ownergroup number(20),
  fk_partipiciantgroup number(20),
  fk_waitinggroup number(20),
  CONSTRAINT u_o_gp_business UNIQUE (fk_ownergroup),
  CONSTRAINT u_o_gp_business01 UNIQUE (fk_partipiciantgroup),
  CONSTRAINT u_o_gp_business02 UNIQUE (fk_waitinggroup),
  PRIMARY KEY (group_id)
);

--
-- Table: o_temporarykey
--;

CREATE TABLE o_temporarykey (
  reglist_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  email varchar2(255 char) NOT NULL,
  regkey varchar2(255 char) NOT NULL,
  ip varchar2(255 char) NOT NULL,
  mailsent number NOT NULL,
  action varchar2(255 char) NOT NULL,
  PRIMARY KEY (reglist_id)
);

--
-- Table: o_bs_authentication
--;

CREATE TABLE o_bs_authentication (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  identity_fk number(20) NOT NULL,
  provider varchar2(8 char),
  authusername varchar2(255 char),
  credential varchar2(255 char),
  PRIMARY KEY (id),
  CONSTRAINT u_o_bs_authentication UNIQUE (provider, authusername)
);

--
-- Table: o_noti_pub
--;

CREATE TABLE o_noti_pub (
  publisher_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  publishertype varchar2(50 char) NOT NULL,
  data varchar2(4000),
  resname varchar2(50 char),
  resid number(20),
  subident varchar2(128 char),
  businesspath varchar2(255 char),
  state number(11),
  latestnews date NOT NULL,
  PRIMARY KEY (publisher_id)
);

--
-- Table: o_qtiresultset
--;

CREATE TABLE o_qtiresultset (
  resultset_id number(20) NOT NULL,
  version number(20) NOT NULL,
  lastmodified date NOT NULL,
  creationdate date,
  identity_id number(20) NOT NULL,
  olatresource_fk number(20) NOT NULL,
  olatresourcedetail varchar2(255 char) NOT NULL,
  assessmentid number(20) NOT NULL,
  repositoryref_fk number(20) NOT NULL,
  ispassed number,
  score float,
  duration number(20),
  PRIMARY KEY (resultset_id)
);

--
-- Table: o_bs_identity
--;

CREATE TABLE o_bs_identity (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  lastlogin date,
  name varchar2(128 char) NOT NULL,
  status number(11),
  fk_user_id number(20),
  CONSTRAINT u_o_bs_identity UNIQUE (name),
  CONSTRAINT u_o_bs_identity01 UNIQUE (fk_user_id),
  PRIMARY KEY (id)
);

--
-- Table: o_olatresource
--;

CREATE TABLE o_olatresource (
  resource_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  resname varchar2(50 char) NOT NULL,
  resid number(20) NOT NULL,
  PRIMARY KEY (resource_id),
  CONSTRAINT u_o_olatresource UNIQUE (resname, resid)
);

--
-- Table: o_bs_namedgroup
--;


CREATE TABLE o_bs_namedgroup (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  secgroup_id number(20) NOT NULL,
  groupname varchar2(16 char),
  PRIMARY KEY (id),
  CONSTRAINT u_o_bs_namedgroup UNIQUE (groupname)
);

--
-- Table: o_catentry
--;

CREATE TABLE o_catentry (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  name varchar2(110 char) NOT NULL,
  description varchar2(4000),
  externalurl varchar2(255 char),
  fk_repoentry number(20),
  fk_ownergroup number(20),
  type number(11) NOT NULL,
  parent_id number(20),
  CONSTRAINT u_o_catentry UNIQUE (fk_ownergroup),
  PRIMARY KEY (id)
);

--
-- Table: o_note
--;

CREATE TABLE o_note (
  note_id number(20) NOT NULL,
  version number(20) NOT NULL,
  lastmodified date,
  creationdate date,
  owner_id number(20),
  resourcetypename varchar2(50 char) NOT NULL,
  resourcetypeid number(20) NOT NULL,
  sub_type varchar2(50 char),
  notetitle varchar2(255 char),
  notetext varchar2(4000),
  PRIMARY KEY (note_id)
);

--
-- Table: o_gp_bgcontext
--;

CREATE TABLE o_gp_bgcontext (
  groupcontext_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  name varchar2(255 char) NOT NULL,
  descr varchar2(4000),
  grouptype varchar2(15 char) NOT NULL,
  ownergroup_fk number(20),
  defaultcontext number NOT NULL,
  CONSTRAINT u_o_gp_bgcontext UNIQUE (ownergroup_fk),
  PRIMARY KEY (groupcontext_id)
);

--
-- Table: o_references
--;

CREATE TABLE o_references (
  reference_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  source_id number(20) NOT NULL,
  target_id number(20) NOT NULL,
  userdata varchar2(64 char),
  PRIMARY KEY (reference_id)
);

--
-- Table: o_repositorymetadata
--;

CREATE TABLE o_repositorymetadata (
  metadataelement_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  name varchar2(255 char) NOT NULL,
  value varchar2(4000) NOT NULL,
  fk_repositoryentry number(20) NOT NULL,
  PRIMARY KEY (fk_repositoryentry, metadataelement_id)
);

--
-- Table: o_user
--;

CREATE TABLE o_user (
  user_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  language varchar2(10 char),
  fontsize varchar2(10 char),
  notification_interval varchar2(16 char),
  presencemessagespublic number,
  informsessiontimeout number NOT NULL,
  PRIMARY KEY (user_id)
);

--
-- Table: o_userproperty
--;

CREATE TABLE o_userproperty (
  fk_user_id number(20) NOT NULL,
  propname varchar2(255 char) NOT NULL,
  propvalue varchar2(255 char),
  PRIMARY KEY (fk_user_id, propname)
);

--
-- Table: o_gp_bgcontextresource_rel
--;

CREATE TABLE o_gp_bgcontextresource_rel (
  groupcontextresource_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  oresource_id number(20) NOT NULL,
  groupcontext_fk number(20) NOT NULL,
  PRIMARY KEY (groupcontextresource_id)
);

--
-- Table: o_message
--;

CREATE TABLE o_message (
  message_id number(20) NOT NULL,
  version number(20) NOT NULL,
  lastmodified date,
  creationdate date,
  title varchar2(100 char),
  body varchar2(4000),
  parent_id number(20),
  topthread_id number(20),
  creator_id number(20) NOT NULL,
  modifier_id number(20),
  forum_fk number(20),
  statuscode number(11),
  numofwords number(11),
  numofcharacters number(11),
  PRIMARY KEY (message_id)
);

--
-- Table: o_gp_bgtoarea_rel
--;

CREATE TABLE o_gp_bgtoarea_rel (
  bgtoarea_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  group_fk number(20) NOT NULL,
  area_fk number(20) NOT NULL,
  PRIMARY KEY (bgtoarea_id),
  CONSTRAINT u_o_gp_bgtoarea_rel UNIQUE (group_fk, area_fk)
);

--
-- Table: o_noti_sub
--;

CREATE TABLE o_noti_sub (
  publisher_id number(20) NOT NULL,
  version number(20) NOT NULL,
  lastmodified date,
  creationdate date,
  fk_publisher number(20) NOT NULL,
  fk_identity number(20) NOT NULL,
  latestemailed date,
  PRIMARY KEY (publisher_id),
  CONSTRAINT u_o_noti_sub UNIQUE (fk_publisher, fk_identity)
);

--
-- Table: o_qtiresult
--;

CREATE TABLE o_qtiresult (
  result_id number(20) NOT NULL,
  version number(20) NOT NULL,
  lastmodified date NOT NULL,
  creationdate date,
  itemident varchar2(255 char) NOT NULL,
  answer varchar2(4000),
  duration number(20),
  score float,
  tstamp date NOT NULL,
  ip varchar2(255 char),
  resultset_fk number(20),
  PRIMARY KEY (result_id)
);

--
-- Table: o_bs_policy
--;

CREATE TABLE o_bs_policy (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  oresource_id number(20) NOT NULL,
  group_id number(20) NOT NULL,
  permission varchar2(16 char) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT u_o_bs_policy UNIQUE (oresource_id, group_id, permission)
);

--
-- Table: o_gp_bgarea
--;

CREATE TABLE o_gp_bgarea (
  area_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  name varchar2(255 char) NOT NULL,
  descr varchar2(4000),
  groupcontext_fk number(20) NOT NULL,
  PRIMARY KEY (area_id)
);

--
-- Table: o_repositoryentry
--;

CREATE TABLE o_repositoryentry (
  repositoryentry_id number(20) NOT NULL,
  version number(20) NOT NULL,
  lastmodified date,
  creationdate date,
  lastusage date,
  softkey varchar2(30 char) NOT NULL,
  displayname varchar2(110 char) NOT NULL,
  resourcename varchar2(100 char) NOT NULL,
  fk_olatresource number(20),
  fk_ownergroup number(20),
  description varchar2(4000),
  initialauthor varchar2(128 char) NOT NULL,
  accesscode number(11) NOT NULL,
  statuscode number(11),
  canlaunch number NOT NULL,
  candownload number NOT NULL,
  cancopy number NOT NULL,
  canreference number NOT NULL,
  launchcounter number(20) NOT NULL,
  downloadcounter number(20) NOT NULL,
  CONSTRAINT u_o_repositoryentry UNIQUE (softkey),
  CONSTRAINT u_o_repositoryentry01 UNIQUE (fk_olatresource),
  CONSTRAINT u_o_repositoryentry02 UNIQUE (fk_ownergroup),
  PRIMARY KEY (repositoryentry_id)
);

--
-- Table: o_bookmark
--;

CREATE TABLE o_bookmark (
  bookmark_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  owner_id number(20) NOT NULL,
  title varchar2(255 char) NOT NULL,
  description varchar2(4000),
  detaildata varchar2(255 char),
  displayrestype varchar2(50 char) NOT NULL,
  olatrestype varchar2(50 char) NOT NULL,
  olatreskey number(20),
  PRIMARY KEY (bookmark_id)
);

--
-- Table: o_bs_membership
--;

CREATE TABLE o_bs_membership (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  lastmodified date,
  creationdate date,
  secgroup_id number(20) NOT NULL,
  identity_id number(20) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT u_o_bs_membership UNIQUE (secgroup_id, identity_id)
);

--
-- Table: o_plock
--;

CREATE TABLE o_plock (
  plock_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  asset varchar2(255 char) NOT NULL,
  CONSTRAINT u_o_plock UNIQUE (asset),
  PRIMARY KEY (plock_id)
);

--
-- Table: hibernate_unique_key
--;

CREATE TABLE hibernate_unique_key (
  next_hi number(11)
);

--
-- Table: o_lifecycle
--;

CREATE TABLE o_lifecycle (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  persistenttypename varchar2(50 char) NOT NULL,
  persistentref number(20) NOT NULL,
  action varchar2(50 char) NOT NULL,
  lctimestamp date,
  uservalue varchar2(4000),
  PRIMARY KEY (id)
);

--
-- Table: oc_lock
--;

CREATE TABLE oc_lock (
  lock_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  identity_fk number(20) NOT NULL,
  asset varchar2(120 char) NOT NULL,
  CONSTRAINT u_oc_lock UNIQUE (asset),
  PRIMARY KEY (lock_id)
);

--
-- Table: o_readmessage
--;

CREATE TABLE o_readmessage (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  identity_id number(20) NOT NULL,
  forum_id number(20) NOT NULL,
  message_id number(20) NOT NULL,
  PRIMARY KEY (id)
);

--
-- Table: o_loggingtable
--;

CREATE TABLE o_loggingtable (
  log_id number(20) NOT NULL,
  creationdate date,
  sourceclass varchar2(255 char),
  sessionid varchar2(255 char) NOT NULL,
  user_id number(20),
  username varchar2(255 char),
  userproperty1 varchar2(255 char),
  userproperty2 varchar2(255 char),
  userproperty3 varchar2(255 char),
  userproperty4 varchar2(255 char),
  userproperty5 varchar2(255 char),
  userproperty6 varchar2(255 char),
  userproperty7 varchar2(255 char),
  userproperty8 varchar2(255 char),
  userproperty9 varchar2(255 char),
  userproperty10 varchar2(255 char),
  userproperty11 varchar2(255 char),
  userproperty12 varchar2(255 char),
  actioncrudtype varchar2(1 char) NOT NULL,
  actionverb varchar2(16 char) NOT NULL,
  actionobject varchar2(32 char) NOT NULL,
  simpleduration number(20) NOT NULL,
  resourceadminaction number NOT NULL,
  businesspath varchar2(2048 char),
  greatgrandparentrestype varchar2(32 char),
  greatgrandparentresid varchar2(64 char),
  greatgrandparentresname varchar2(255 char),
  grandparentrestype varchar2(32 char),
  grandparentresid varchar2(64 char),
  grandparentresname varchar2(255 char),
  parentrestype varchar2(32 char),
  parentresid varchar2(64 char),
  parentresname varchar2(255 char),
  targetrestype varchar2(32 char),
  targetresid varchar2(64 char),
  targetresname varchar2(255 char),
  PRIMARY KEY (log_id)
);

--
-- Table: o_checklist
--;

CREATE TABLE o_checklist (
  checklist_id number(20) NOT NULL,
  version number(20) NOT NULL,
  lastmodified date NOT NULL,
  title varchar2(255 char) NOT NULL,
  description varchar2(4000),
  PRIMARY KEY (checklist_id)
);

--
-- Table: o_checkpoint
--;

CREATE TABLE o_checkpoint (
  checkpoint_id number(20) NOT NULL,
  version number(20) NOT NULL,
  lastmodified date NOT NULL,
  title varchar2(255 char) NOT NULL,
  description varchar2(4000),
  modestring varchar2(64 char) NOT NULL,
  checklist_fk number(20),
  PRIMARY KEY (checkpoint_id)
);

--
-- Table: o_checkpoint_results
--;

CREATE TABLE o_checkpoint_results (
  checkpoint_result_id number(20) NOT NULL,
  version number(20) NOT NULL,
  lastmodified date NOT NULL,
  result number NOT NULL,
  checkpoint_fk number(20),
  identity_fk number(20),
  PRIMARY KEY (checkpoint_result_id)
);

--
-- Table: o_projectbroker
--;

CREATE TABLE o_projectbroker (
  projectbroker_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  PRIMARY KEY (projectbroker_id)
);

--
-- Table: o_projectbroker_project
--;

CREATE TABLE o_projectbroker_project (
  project_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  title varchar2(100 char),
  description varchar2(4000),
  state varchar2(20 char),
  maxMembers number(11),
  attachmentFileName varchar2(100 char),
  mailNotificationEnabled number NOT NULL,
  projectgroup_fk number(20) NOT NULL,
  projectbroker_fk number(20) NOT NULL,
  candidategroup_fk number(20) NOT NULL,
  PRIMARY KEY (project_id)
);

--
-- Table: o_projectbroker_customfields
--;

CREATE TABLE o_projectbroker_customfields (
  fk_project_id number(20) NOT NULL,
  propname varchar2(255 char) NOT NULL,
  propvalue varchar2(255 char),
  PRIMARY KEY (fk_project_id, propname)
);

--
-- Table: o_usercomment
--;

CREATE TABLE o_usercomment (
  comment_id number(20) NOT NULL,
  version number(11) NOT NULL,
  creationdate date,
  resname varchar2(50 char) NOT NULL,
  resid number(20) NOT NULL,
  ressubpath varchar2(2048 char),
  creator_id number(20) NOT NULL,
  commenttext varchar2(4000),
  parent_key number(20),
  PRIMARY KEY (comment_id)
);

--
-- Table: o_userrating
--;

CREATE TABLE o_userrating (
  rating_id number(20) NOT NULL,
  version number(11) NOT NULL,
  creationdate date,
  resname varchar2(50 char) NOT NULL,
  resid number(20) NOT NULL,
  ressubpath varchar2(2048 char),
  creator_id number(20) NOT NULL,
  rating number(11) NOT NULL,
  PRIMARY KEY (rating_id)
);

--
-- Table: o_info_message
--;

CREATE TABLE o_info_message (
  info_id number(20)  NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  modificationdate date,
  title varchar2(2048 char),
  message varchar2(2048 char),
  resname varchar(50 char) NOT NULL,
  resid number(20) NOT NULL,
  ressubpath varchar2(2048 char),
  businesspath varchar2(2048 char),
  fk_author_id number(20),
  fk_modifier_id number(20),
  PRIMARY KEY (info_id)
);

--
-- Table: o_stat_dayofweek
--;

CREATE SEQUENCE sq_o_stat_dayofweek_id;

CREATE TABLE o_stat_dayofweek (
  id number(20) NOT NULL,
  businesspath varchar2(2048 char) NOT NULL,
  resid number(20) NOT NULL,
  day number(11) NOT NULL,
  value number(11) NOT NULL,
  PRIMARY KEY (id)
);

--
-- Table: o_stat_hourofday
--;

CREATE SEQUENCE sq_o_stat_hourofday_id;

CREATE TABLE o_stat_hourofday (
  id number(20) NOT NULL,
  businesspath varchar2(2048 char) NOT NULL,
  resid number(20) NOT NULL,
  hour number(11) NOT NULL,
  value number(11) NOT NULL,
  PRIMARY KEY (id)
);

--
-- Table: o_stat_weekly
--;

CREATE SEQUENCE sq_o_stat_weekly_id;

CREATE TABLE o_stat_weekly (
  id number(20) NOT NULL,
  businesspath varchar2(2048 char) NOT NULL,
  resid number(20) NOT NULL,
  week varchar2(7 char) NOT NULL,
  value number(11) NOT NULL,
  PRIMARY KEY (id)
);

--
-- Table: o_stat_daily
--;

CREATE SEQUENCE sq_o_stat_daily_id;

CREATE TABLE o_stat_daily (
  id number(20) NOT NULL,
  businesspath varchar2(2048 char) NOT NULL,
  resid number(20) NOT NULL,
  day date NOT NULL,
  value number(11) NOT NULL,
  PRIMARY KEY (id)
);

--
-- Table: o_stat_homeorg
--;

CREATE SEQUENCE sq_o_stat_homeorg_id;

CREATE TABLE o_stat_homeorg (
  id number(20) NOT NULL,
  businesspath varchar2(2048 char) NOT NULL,
  resid number(20) NOT NULL,
  homeorg varchar2(255 char) NOT NULL,
  value number(11) NOT NULL,
  PRIMARY KEY (id)
);

--
-- Table: o_stat_orgtype
--;

CREATE SEQUENCE sq_o_stat_orgtype_id;

CREATE TABLE o_stat_orgtype (
  id number(20) NOT NULL,
  businesspath varchar2(2048 char) NOT NULL,
  resid number(20) NOT NULL,
  orgtype varchar2(255 char),
  value number(11) NOT NULL,
  PRIMARY KEY (id)
);

--
-- Table: o_stat_studylevel
--;

CREATE SEQUENCE sq_o_stat_studylevel_id;

CREATE TABLE o_stat_studylevel (
  id number(20) NOT NULL,
  businesspath varchar2(2048 char) NOT NULL,
  resid number(20) NOT NULL,
  studylevel varchar2(255 char) NOT NULL,
  value number(11) NOT NULL,
  PRIMARY KEY (id)
);

--
-- Table: o_stat_studybranch3
--;

CREATE SEQUENCE sq_o_stat_studybranch3_id;

CREATE TABLE o_stat_studybranch3 (
  id number(20) NOT NULL,
  businesspath varchar2(2048 char) NOT NULL,
  resid number(20) NOT NULL,
  studybranch3 varchar2(255 char),
  value number(11) NOT NULL,
  PRIMARY KEY (id)
);

CREATE OR REPLACE TRIGGER ai_o_stat_dayofweek_id
BEFORE INSERT ON o_stat_dayofweek
FOR EACH ROW WHEN (
 new.id IS NULL OR new.id = 0
)
BEGIN
 SELECT sq_o_stat_dayofweek_id.nextval
 INTO :new.id
 FROM dual;
END;
/

CREATE OR REPLACE TRIGGER ai_o_stat_hourofday_id
BEFORE INSERT ON o_stat_hourofday
FOR EACH ROW WHEN (
 new.id IS NULL OR new.id = 0
)
BEGIN
 SELECT sq_o_stat_hourofday_id.nextval
 INTO :new.id
 FROM dual;
END;
/

CREATE OR REPLACE TRIGGER ai_o_stat_weekly_id
BEFORE INSERT ON o_stat_weekly
FOR EACH ROW WHEN (
 new.id IS NULL OR new.id = 0
)
BEGIN
 SELECT sq_o_stat_weekly_id.nextval
 INTO :new.id
 FROM dual;
END;
/

CREATE OR REPLACE TRIGGER ai_o_stat_daily_id
BEFORE INSERT ON o_stat_daily
FOR EACH ROW WHEN (
 new.id IS NULL OR new.id = 0
)
BEGIN
 SELECT sq_o_stat_daily_id.nextval
 INTO :new.id
 FROM dual;
END;
/

CREATE OR REPLACE TRIGGER ai_o_stat_homeorg_id
BEFORE INSERT ON o_stat_homeorg
FOR EACH ROW WHEN (
 new.id IS NULL OR new.id = 0
)
BEGIN
 SELECT sq_o_stat_homeorg_id.nextval
 INTO :new.id
 FROM dual;
END;
/

CREATE OR REPLACE TRIGGER ai_o_stat_orgtype_id
BEFORE INSERT ON o_stat_orgtype
FOR EACH ROW WHEN (
 new.id IS NULL OR new.id = 0
)
BEGIN
 SELECT sq_o_stat_orgtype_id.nextval
 INTO :new.id
 FROM dual;
END;
/

CREATE OR REPLACE TRIGGER ai_o_stat_studylevel_id
BEFORE INSERT ON o_stat_studylevel
FOR EACH ROW WHEN (
 new.id IS NULL OR new.id = 0
)
BEGIN
 SELECT sq_o_stat_studylevel_id.nextval
 INTO :new.id
 FROM dual;
END;
/

CREATE OR REPLACE TRIGGER ai_o_stat_studybranch3_id
BEFORE INSERT ON o_stat_studybranch3
FOR EACH ROW WHEN (
 new.id IS NULL OR new.id = 0
)
BEGIN
 SELECT sq_o_stat_studybranch3_id.nextval
 INTO :new.id
 FROM dual;
END;
/

-- indexes
create index statdow_resid_idx on o_stat_dayofweek (resid);
create index stathod_resid_idx on o_stat_hourofday (resid);
create index statwee_resid_idx on o_stat_weekly (resid);
create index statday_resid_idx on o_stat_daily (resid);
create index stathor_resid_idx on o_stat_homeorg (resid);
create index statorg_resid_idx on o_stat_orgtype (resid);
create index statstl_resid_idx on o_stat_studylevel (resid);
create index statstb_resid_idx on o_stat_studybranch3 (resid);
-- create index  ocl_asset_idx on oc_lock (asset);
create index  resid_idx on o_property (resourcetypeid);
create index  category_idx on o_property (category);
create index  name_idx on o_property (name);
create index  restype_idx on o_property (resourcetypename);
create index  gp_name_idx on o_gp_business (groupname);
create index  gp_type_idx on o_gp_business (businessgrouptype);
create index  provider_idx on o_bs_authentication (provider);
create index  credential_idx on o_bs_authentication (credential);
create index  authusername_idx on o_bs_authentication (authusername);
create index  onp_name_idx on o_noti_pub (resname, resid, subident);
create index  oresdetindex on o_qtiresultset (olatresourcedetail);
create index  oresindex on o_qtiresultset (olatresource_fk);
create index  reprefindex on o_qtiresultset (repositoryref_fk);
create index  assindex on o_qtiresultset (assessmentid);
-- create index  obi_name_idx on o_bs_identity (name);
create index  identstatus_idx on o_bs_identity (status);
create index  oores_name_idx on o_olatresource (resname);
create index  id_idx on o_olatresource (resid);
-- create index  groupname_idx on o_bs_namedgroup (groupname);
create index  on_resid_idx on o_note (resourcetypeid);
create index  owner_idx on o_note (owner_id);
create index  on_restype_idx on o_note (resourcetypename);
create index  type_idx on o_gp_bgcontext (grouptype);
create index  default_idx on o_gp_bgcontext (defaultcontext);
create index  ogpbgc_name_idx on o_gp_bgcontext (name);
create index  propvalue_idx on o_userproperty (propvalue);
create index  itemindex on o_qtiresult (itemident);
create index  ogpbga_name_idx on o_gp_bgarea (name);
create index  access_idx on o_repositoryentry (accesscode);
create index  initialAuthor_idx on o_repositoryentry (initialauthor);
create index  resource_idx on o_repositoryentry (resourcename);
create index  displayname_idx on o_repositoryentry (displayname);
-- create index  softkey_idx on o_repositoryentry (softkey);
-- create index  asset_idx on o_plock (asset);
create index  lc_pref_idx on o_lifecycle (persistentref);
create index  lc_type_idx on o_lifecycle (persistenttypename);
create index  lc_action_idx on o_lifecycle (action);
create index  readmessage_forum_idx on o_readmessage (forum_id);
create index  readmessage_identity_idx on o_readmessage (identity_id);
create index  opb_project_broker_idx on o_projectbroker_project (projectbroker_fk);
-- create index  projectbroker_project_id_idx on o_projectbroker_project (project_id);
create index  opb_customfields_idx on o_projectbroker_customfields (fk_project_id);
create index cmt_id_idx on o_usercomment (resid);
create index cmt_name_idx on o_usercomment (resname);
create index cmt_subpath_idx on o_usercomment (ressubpath);
create index rtn_id_idx on o_userrating (resid);
create index rtn_name_idx on o_userrating (resname);
create index rtn_subpath_idx on o_userrating (ressubpath);
create index rtn_rating_idx on o_userrating (rating);
create index usr_notification_interval_idx on o_user (notification_interval);


-- foreign keys
create index FK9E30F4B66115906D on oc_lock (identity_fk);
alter table oc_lock  add constraint FK9E30F4B66115906D foreign key (identity_fk) references o_bs_identity (id);
create index FKB60B1BA5190E5 on o_property (grp);
alter table o_property  add constraint FKB60B1BA5190E5 foreign key (grp) references o_gp_business (group_id);
create index FKB60B1BA5F7E870BE on o_property (identity);
alter table o_property  add constraint FKB60B1BA5F7E870BE foreign key (identity) references o_bs_identity (id);
create index FKCEEB8A86DF6BCD14 on o_gp_business (groupcontext_fk);
alter table o_gp_business  add constraint FKCEEB8A86DF6BCD14 foreign key (groupcontext_fk) references o_gp_bgcontext (groupcontext_id);
-- create index FKCEEB8A86A1FAC766 on o_gp_business (fk_ownergroup);
alter table o_gp_business  add constraint FKCEEB8A86A1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup (id);
-- create index FKCEEB8A86C06E3EF3 on o_gp_business (fk_partipiciantgroup);
alter table o_gp_business  add constraint FKCEEB8A86C06E3EF3 foreign key (fk_partipiciantgroup) references o_bs_secgroup (id);
create index FKC6A5445652595FE6 on o_bs_authentication (identity_fk);
alter table o_bs_authentication  add constraint FKC6A5445652595FE6 foreign key (identity_fk) references o_bs_identity (id);
create index FK14805D0F5259603C on o_qtiresultset (identity_id);
alter table o_qtiresultset  add constraint FK14805D0F5259603C foreign key (identity_id) references o_bs_identity (id);
-- create index FKFF94111CD1A80C95 on o_bs_identity (fk_user_id);
alter table o_bs_identity  add constraint FKFF94111CD1A80C95 foreign key (fk_user_id) references o_user (user_id);
create index FK4B04D83FD1A80C95 on o_userproperty (fk_user_id);
alter table o_userproperty  add constraint FK4B04D83FD1A80C95 foreign key (fk_user_id) references o_user (user_id);
create index FKBAFCBBC4B85B522C on o_bs_namedgroup (secgroup_id);
alter table o_bs_namedgroup  add constraint FKBAFCBBC4B85B522C foreign key (secgroup_id) references o_bs_secgroup (id);
create index FKF4433C2C7B66B0D0 on o_catentry (parent_id);
alter table o_catentry  add constraint FKF4433C2C7B66B0D0 foreign key (parent_id) references o_catentry (id);
-- create index FKF4433C2CA1FAC766 on o_catentry (fk_ownergroup);
alter table o_catentry  add constraint FKF4433C2CA1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup (id);
create index FKF4433C2CDDD69946 on o_catentry (fk_repoentry);
alter table o_catentry  add constraint FKF4433C2CDDD69946 foreign key (fk_repoentry) references o_repositoryentry (repositoryentry_id);
-- create index FKC2D855C263219E27 on o_note (owner_id);
alter table o_note  add constraint FKC2D855C263219E27 foreign key (owner_id) references o_bs_identity (id);
-- create index FK1C154FC47E4A0638 on o_gp_bgcontext (ownergroup_fk);
alter table o_gp_bgcontext  add constraint FK1C154FC47E4A0638 foreign key (ownergroup_fk) references o_bs_secgroup (id);
create index FKE971B4589AC44FBF on o_references (source_id);
alter table o_references  add constraint FKE971B4589AC44FBF foreign key (source_id) references o_olatresource (resource_id);
create index FKE971B458CF634A89 on o_references (target_id);
alter table o_references  add constraint FKE971B458CF634A89 foreign key (target_id) references o_olatresource (resource_id);
create index FKDB97A6493F14E3EE on o_repositorymetadata (fk_repositoryentry);
alter table o_repositorymetadata  add constraint FKDB97A6493F14E3EE foreign key (fk_repositoryentry) references o_repositoryentry (repositoryentry_id);
create index FK9903BEAC9F9C3F1D on o_gp_bgcontextresource_rel (oresource_id);
alter table o_gp_bgcontextresource_rel  add constraint FK9903BEAC9F9C3F1D foreign key (oresource_id) references o_olatresource (resource_id);
create index FK9903BEACDF6BCD14 on o_gp_bgcontextresource_rel (groupcontext_fk);
alter table o_gp_bgcontextresource_rel  add constraint FK9903BEACDF6BCD14 foreign key (groupcontext_fk) references o_gp_bgcontext (groupcontext_id);
create index FKF26C8375236F20E on o_message (creator_id);
alter table o_message  add constraint FKF26C8375236F20E foreign key (creator_id) references o_bs_identity (id);
create index FKF26C837A3FBEB83 on o_message (modifier_id);
alter table o_message  add constraint FKF26C837A3FBEB83 foreign key (modifier_id) references o_bs_identity (id);
create index FKF26C8377B66B0D0 on o_message (parent_id);
alter table o_message  add constraint FKF26C8377B66B0D0 foreign key (parent_id) references o_message (message_id);
create index FKF26C8378EAC1DBB on o_message (topthread_id);
alter table o_message  add constraint FKF26C8378EAC1DBB foreign key (topthread_id) references o_message (message_id);
create index FKF26C8371CB7C4A3 on o_message (forum_fk);
alter table o_message  add constraint FKF26C8371CB7C4A3 foreign key (forum_fk) references o_forum (forum_id);
create index FK9B663F2D1E2E7685 on o_gp_bgtoarea_rel (group_fk);
alter table o_gp_bgtoarea_rel  add constraint FK9B663F2D1E2E7685 foreign key (group_fk) references o_gp_business (group_id);
create index FK9B663F2DD381B9B7 on o_gp_bgtoarea_rel (area_fk);
alter table o_gp_bgtoarea_rel  add constraint FK9B663F2DD381B9B7 foreign key (area_fk) references o_gp_bgarea (area_id);
create index FK4FB8F04749E53702 on o_noti_sub (fk_publisher);
alter table o_noti_sub  add constraint FK4FB8F04749E53702 foreign key (fk_publisher) references o_noti_pub (publisher_id);
create index FK4FB8F0476B1F22F8 on o_noti_sub (fk_identity);
alter table o_noti_sub  add constraint FK4FB8F0476B1F22F8 foreign key (fk_identity) references o_bs_identity (id);
create index FK3563E67340EF401F on o_qtiresult (resultset_fk);
alter table o_qtiresult  add constraint FK3563E67340EF401F foreign key (resultset_fk) references o_qtiresultset (resultset_id);
create index FK9A1C5109F9C3F1D on o_bs_policy (oresource_id);
alter table o_bs_policy  add constraint FK9A1C5109F9C3F1D foreign key (oresource_id) references o_olatresource (resource_id);
create index FK9A1C5101E2E76DB on o_bs_policy (group_id);
alter table o_bs_policy  add constraint FK9A1C5101E2E76DB foreign key (group_id) references o_bs_secgroup (id);
create index FK9EFAF698DF6BCD14 on o_gp_bgarea (groupcontext_fk);
alter table o_gp_bgarea  add constraint FK9EFAF698DF6BCD14 foreign key (groupcontext_fk) references o_gp_bgcontext (groupcontext_id);
-- create index FK2F9C439888C31018 on o_repositoryentry (fk_olatresource);
alter table o_repositoryentry  add constraint FK2F9C439888C31018 foreign key (fk_olatresource) references o_olatresource (resource_id);
-- create index FK2F9C4398A1FAC766 on o_repositoryentry (fk_ownergroup);
alter table o_repositoryentry  add constraint FK2F9C4398A1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup (id);
create index FK68C4E30663219E27 on o_bookmark (owner_id);
alter table o_bookmark  add constraint FK68C4E30663219E27 foreign key (owner_id) references o_bs_identity (id);
create index FK7B6288B45259603C on o_bs_membership (identity_id);
alter table o_bs_membership  add constraint FK7B6288B45259603C foreign key (identity_id) references o_bs_identity (id);
create index FK7B6288B4B85B522C on o_bs_membership (secgroup_id);
alter table o_bs_membership  add constraint FK7B6288B4B85B522C foreign key (secgroup_id) references o_bs_secgroup (id);
create index FK92B6864A18251F0 on o_usercomment (parent_key);
alter table o_usercomment  add constraint FK92B6864A18251F0 foreign key (parent_key) references o_usercomment (comment_id);
create index FKF26C8375236F20A on o_usercomment (creator_id);
alter table o_usercomment  add constraint FKF26C8375236F20A foreign key (creator_id) references o_bs_identity (id);
create index FKF26C8375236F20X on o_userrating (creator_id);
alter table o_userrating  add constraint FKF26C8375236F20X foreign key (creator_id) references o_bs_identity (id);
create index imsg_resid_idx on o_info_message (resid);
create index imsg_author_idx on o_info_message (fk_author_id);
alter table o_info_message  add constraint FKF85553465A4FA5DC foreign key (fk_author_id) references o_bs_identity (id);
create index imsg_modifier_idx on o_info_message (fk_modifier_id);
alter table o_info_message add constraint FKF85553465A4FA5EF foreign key (fk_modifier_id) references o_bs_identity (id);

insert into hibernate_unique_key values ( 0 );
commit
/
