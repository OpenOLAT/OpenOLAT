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
  textvalue CLOB,
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
  fk_resource number(20),
  fk_ownergroup number(20),
  fk_partipiciantgroup number(20),
  fk_waitinggroup number(20),
  CONSTRAINT u_o_gp_business UNIQUE (fk_ownergroup),
  CONSTRAINT u_o_gp_business01 UNIQUE (fk_partipiciantgroup),
  CONSTRAINT u_o_gp_business02 UNIQUE (fk_waitinggroup),
  CONSTRAINT u_o_gp_business03 UNIQUE (fk_resource),
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
  issuspended number,
  fullyassessed number,
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
  receiverealmail varchar2(16 char),
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
  body CLOB,
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
  apply_from date default null,
  apply_to date default null,
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
  groupcontext_fk number(20),
  fk_resource number(20),
  PRIMARY KEY (area_id)
);

--
-- Table: o_gp_business_to_resource
--;

create table o_gp_business_to_resource (
   g_id number(20) not null,
   version number(20) not null,
   creationdate date,
   fk_resource number(20) not null,
   fk_group number(20) not null,
   primary key (g_id)
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
  fk_tutorgroup number(20),
  fk_participantgroup number(20),
  description varchar2(4000),
  initialauthor varchar2(128 char) NOT NULL,
  accesscode number(11) NOT NULL,
  membersonly number default 0,
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
--create index ocl_asset_idx on oc_lock (asset);
create index FK9E30F4B66115906D on oc_lock (identity_fk);
alter table oc_lock  add constraint FK9E30F4B66115906D foreign key (identity_fk) references o_bs_identity (id);

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
  title varchar2(255 char),
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
  title varchar2(255 char),
  description varchar2(4000),
  modestring varchar2(64 char) NOT NULL,
  checklist_fk number(20),
  PRIMARY KEY (checkpoint_id)
);
alter table o_checkpoint add constraint FK9E30F4B661159ZZZ foreign key (checklist_fk) references o_checklist (checklist_id);

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
  title varchar2(150 char),
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
  lastmodified date,
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
-- Table: o_ep_artefact
--;

create table o_ep_artefact (
  artefact_id number(20) not null,
  artefact_type varchar(32 char) not null,
  version number(20) not null,
  creationdate date,
  collection_date date,
  title varchar(512 char),
  description varchar(4000 char),
  signature number(20) default 0,
  businesspath varchar(2048 char),
  fulltextcontent clob,
  reflexion clob,
  source varchar(2048 char),
  add_prop1 varchar(2048 char),
  add_prop2 varchar(2048 char),
  add_prop3 varchar(2048 char),
  fk_struct_el_id number(20),
  fk_artefact_auth_id number(20) not null,
  primary key (artefact_id)
);

--
-- Table: o_ep_collect_restriction
--;

create table o_ep_collect_restriction (
  collect_id number(20) not null,
  version number(20) not null,
  creationdate date,
  artefact_type varchar(256 char),
  amount number(20) default -1,
  restriction varchar(32 char),
  pos number(20) default 0,
  fk_struct_el_id number(20),
  primary key (collect_id)
);

--
-- Table: o_ep_struct_el
--;

create table o_ep_struct_el (
  structure_id number(20) not null,
  structure_type varchar(32 char) not null,
  version number(20) not null,
  creationdate date,
  returndate date,
  copydate date,
  lastsyncheddate date,
  deadline date,
  title varchar(512 char),
  description varchar(2048 char),
  struct_el_source number(20),
  target_resname varchar(50 char),
  target_resid number(20),
  target_ressubpath varchar(2048 char),
  target_businesspath varchar(2048 char),
  style varchar(128 char),  
  status varchar(32 char),
  viewmode varchar(32 char),
  fk_struct_root_id number(20),
  fk_struct_root_map_id number(20),
  fk_map_source_id number(20),
  fk_ownergroup number(20),
  fk_olatresource number(20) not null,
  primary key (structure_id)  
);

--
-- Table: o_ep_struct_struct_link
--;

create table o_ep_struct_struct_link (
  link_id number(20) not null,
  version number(20) not null,
  creationdate date,
  pos number(20) default 0,
  fk_struct_parent_id number(20) not null,
  fk_struct_child_id number(20) not null,
  primary key (link_id)
);

--
-- Table: o_ep_struct_artefact_link
--;

create table o_ep_struct_artefact_link (
  link_id number(20) not null,
  version number(20) not null,
  creationdate date,
  pos number(20) default 0,
  reflexion clob,
  fk_auth_id number(20),
  fk_struct_id number(20) not null,
  fk_artefact_id number(20) not null,
  primary key (link_id)
);

--
-- Table: o_bs_invitation
--;

create table o_bs_invitation (
   id number(20) not null,
   version number(20) not null,
   creationdate date,
   token varchar(64 char) not null,
   first_name varchar(64 char),
   last_name varchar(64 char),
   mail varchar(128 char),
   fk_secgroup number(20),
   primary key (id)
);

--
-- Table: o_tag
--;

create table o_tag (
  tag_id number(20) not null,
  version number(20) not null,
  creationdate date,
  tag varchar(128 char) not null,
  resname varchar(50 char) not null,
  resid number(20) not null,
  ressubpath varchar(2048 char),
  businesspath varchar(2048 char),
  fk_author_id number(20) not null,
  primary key (tag_id)
);

--
-- Table: o_mail
--;

create table o_mail (
  mail_id number(20) not null,
  meta_mail_id varchar(64 char),
  creationdate date,
  lastmodified date,
  resname varchar(50 char),
  resid number(20),
  ressubpath varchar(2048 char),
  businesspath varchar(2048 char),
  subject varchar(512 char),
  body clob,
  fk_from_id number(20),
  primary key (mail_id)
);

--
-- Table: o_mail_to_recipient
--;

create table o_mail_to_recipient (
  pos number(20) default 0,
  fk_mail_id number(20),
  fk_recipient_id number(20)
);

--
-- Table: o_mail_recipient
--; 

create table o_mail_recipient (
  recipient_id number(20) NOT NULL,
  recipientvisible number,
  deleted number,
  mailread number,
  mailmarked number,
  email varchar(255 char),
  recipientgroup varchar(255 char),
  creationdate date,
  fk_recipient_id number(20),
  primary key (recipient_id)
);

--
-- Table: o_mail_attachment
--; 

create table o_mail_attachment (
  attachment_id number(20) NOT NULL,
  creationdate date,
  datas blob,
  datas_size number(20),
  datas_name varchar(255 char),
  mimetype varchar(255 char),
  fk_att_mail_id number(20),
  primary key (attachment_id)
);

--
-- Table: o_ac_offer
--; 

create table o_ac_offer (
  offer_id number(20) NOT NULL,
  creationdate date,
  lastmodified date,
  is_valid number default 1,
  validfrom date,
  validto date,
  version number(20) not null,
  resourceid number(20),
  resourcetypename varchar(255 char),
  resourcedisplayname varchar(255 char),
  token varchar(255 char),
  price_amount number(20,2),
  price_currency_code VARCHAR(3 char),
  offer_desc VARCHAR(2000 char),
  fk_resource_id number(20),
  primary key (offer_id)
);

--
-- Table: o_ac_method
--; 

create table o_ac_method (
  method_id number(20) NOT NULL,
  access_method varchar(32 char),
  version number(20) not null,
  creationdate date,
  lastmodified date,
  is_valid number default 1,
  is_enabled number default 1,
  validfrom date,
  validto date,
  primary key (method_id)
);

--
-- Table: o_ac_offer_access
--; 

create table o_ac_offer_access (
  offer_method_id number(20) NOT NULL,
  version number(20) not null,
  creationdate date,
  is_valid number default 1,
  validfrom date,
  validto date,
  fk_offer_id number(20),
  fk_method_id number(20),
  primary key (offer_method_id)
);

--
-- Table: o_ac_order
--; 

create table o_ac_order (
  order_id number(20) NOT NULL,
  version number(20) not null,
  creationdate date,
  lastmodified date,
  is_valid number default 1,
  total_lines_amount number(20,2),
  total_lines_currency_code VARCHAR(3 char),
  total_amount number(20,2),
  total_currency_code VARCHAR(3 char),
  discount_amount number(20,2),
  discount_currency_code VARCHAR(3 char),
  order_status VARCHAR(32 char) default 'NEW',
  fk_delivery_id number(20),
  primary key (order_id)
);

--
-- Table: o_ac_order_part
--; 

create table o_ac_order_part (
  order_part_id number(20) NOT NULL,
  version number(20) not null,
  pos number(20),
  creationdate date,
  total_lines_amount number(20,2),
  total_lines_currency_code VARCHAR(3 char),
  total_amount number(20,2),
  total_currency_code VARCHAR(3 char),
  fk_order_id number(20),
  primary key (order_part_id)
);

--
-- Table: o_ac_order_line
--; 

create table o_ac_order_line (
  order_item_id number(20) NOT NULL,
  version number(20) not null,
  pos number(20),
  creationdate date,
  unit_price_amount number(20,2),
  unit_price_currency_code VARCHAR(3 char),
  total_amount number(20,2),
  total_currency_code VARCHAR(3 char),
  fk_order_part_id number(20),
  fk_offer_id number(20),
  primary key (order_item_id)
); 

--
-- Table: o_ac_transaction
--; 

create table o_ac_transaction (
  transaction_id number(20) NOT NULL,
  version number(20) not null,
  creationdate date,
  trx_status VARCHAR(32 char) default 'NEW',
  amount_amount number(20,2),
  amount_currency_code VARCHAR(3 char),
  fk_order_part_id number(20),
  fk_order_id number(20),
  fk_method_id number(20),
  primary key (transaction_id)
);

--
-- Table: o_ac_reservation
--; 

create table o_ac_reservation (
   reservation_id number(20) NOT NULL,
   creationdate date,
   lastmodified date,
   version number(20) not null,
   expirationdate date,
   reservationtype varchar(32),
   fk_identity number(20) not null,
   fk_resource number(20) not null,
   primary key (reservation_id)
);

--
-- Table: o_ac_paypal_transaction
--; 

create table o_ac_paypal_transaction (
   transaction_id number(20) not null,
   version number(20) not null,
   creationdate date,
   ref_no varchar(255 char),
   order_id number(20) not null,
   order_part_id number(20) not null,
   method_id number(20) not null,
   success_uuid varchar(32 char) not null,
   cancel_uuid varchar(32 char) not null,
   amount_amount DECIMAL,
   amount_currency_code VARCHAR(3 char),
   pay_response_date date,
   pay_key varchar(255 char),
   ack varchar(255 char),
   build varchar(255 char),
   coorelation_id varchar(255 char),
   payment_exec_status varchar(255 char),
   ipn_transaction_id varchar(255 char),
   ipn_transaction_status varchar(255 char),
   ipn_sender_transaction_id varchar(255 char),
   ipn_sender_transaction_status varchar(255 char),
   ipn_sender_email varchar(255 char),
   ipn_verify_sign varchar(255 char),
   ipn_pending_reason varchar(255 char),
   trx_status VARCHAR(32 char) default 'NEW' not null,
   trx_amount NUMBER (21,20),
   trx_currency_code VARCHAR(3 char),
   primary key (transaction_id)
);

--
-- Table: o_stat_lastupdated
--; 

create table o_stat_lastupdated (
  lastupdated date not null,
  from_datetime date not null,
  until_datetime date not null
);
-- important: initialize with old date!
insert into o_stat_lastupdated values(to_date('1999-01-01'), to_date('1999-01-01'), to_date('1999-01-01'));

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

--
-- Table: o_mark
--;

create table o_mark (
  mark_id number(20) not null,
  version number(20) not null,
  creationdate date,
  resname varchar(50 char) not null,
  resid number(20) not null,
  ressubpath varchar(2048 char),
  businesspath varchar(2048 char),
  creator_id number(20) not null,
  primary key (mark_id)
);

--
-- Table: o_om_room_reference
--;

create table o_om_room_reference (
   id number(20) not null,
   version number(20) not null,
   lastmodified date,
   creationdate date,
   businessgroup number(20),
   resourcetypename varchar(50),
   resourcetypeid number(20),
   ressubpath varchar(255),
   roomId number(20),
   config clob,
   primary key (id)
);

--
-- Table: o_as_eff_statement
--;

create table o_as_eff_statement (
   id number(20) not null,
   version number(20) not null,
   lastmodified date,
   creationdate date,
   passed number,
   score float(4),
   total_nodes number(20),
   attempted_nodes number(20),
   passed_nodes number(20),
   course_title varchar(255 char),
   course_short_title varchar(128 char),
   course_repo_key number(20),
   statement_xml clob,
   fk_identity number(20),
   fk_resource_id number(20),
   CONSTRAINT u_o_as_eff_statement UNIQUE (fk_identity, fk_resource_id),
   primary key (id)
);

--
-- Table: o_as_user_course_infos
--;

create table o_as_user_course_infos (
   id number(20) not null,
   version number(20) not null,
   creationdate date,
   lastmodified date,
   initiallaunchdate date,
   recentlaunchdate date,
   visit number(20),
   timespend number(20),
   fk_identity number(20),
   fk_resource_id number(20),
   CONSTRAINT u_o_as_user_course_infos UNIQUE (fk_identity, fk_resource_id),
   primary key (id)
);

--
-- Table: o_im_message
--;

create table o_im_message (
   id number(20) not null,
   creationdate date,
   msg_resname varchar2(50 char) not null,
   msg_resid number(20) not null,
   msg_anonym number default 0,
   msg_from varchar2(255 char) not null,
   msg_body clob,
   fk_from_identity_id number(20) not null,
   primary key (id)
);

--
-- Table: o_im_notification
--;

create table o_im_notification (
   id number(20) not null,
   creationdate date,
   chat_resname varchar(50) not null,
   chat_resid number(20) not null,
   fk_to_identity_id number(20) not null,
   fk_from_identity_id number(20) not null,
   primary key (id)
);

--
-- Table: o_im_roster_entry
--;

create table o_im_roster_entry (
   id number(20) not null,
   creationdate date,
   r_resname varchar2(50 char) not null,
   r_resid number(20) not null,
   r_nickname varchar2(255 char),
   r_fullname varchar2(255 char),
   r_vip number default 0,
   r_anonym number default 0,
   fk_identity_id number(20) not null,
   primary key (id)
);

--
-- Table: o_im_preferences
--;

create table o_im_preferences (
   id number(20) not null,
   creationdate date,
   visible_to_others number default 0,
   roster_def_status varchar(12),
   fk_from_identity_id number(20) not null,
   primary key (id)
);

--
-- Table: o_mapper
--;

create table o_mapper (
   id number(20) not null,
   lastmodified date,
   creationdate date,
   mapper_uuid varchar(64 char),
   orig_session_id varchar(64 char),
   xml_config CLOB,
   primary key (id)
);

--
-- Tables: question pools
--;


create table o_qp_pool (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   q_name varchar2(255 char) not null,
   q_public number default 0,
   fk_ownergroup number(20),
   primary key (id)
);

create table o_qp_taxonomy_level (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   q_field varchar2(255 char) not null,
   q_mat_path_ids varchar2(1024 char),
   q_mat_path_names varchar2(2048 char),
   fk_parent_field number(20),
   primary key (id)
);

create table o_qp_item (
   id number(20) not null,
   q_identifier varchar2(36 char) not null,
   q_master_identifier varchar2(36 char),
   q_title varchar2(1024 char) not null,
   q_description varchar2(2048 char),
   q_keywords varchar2(1024 char),
   q_coverage varchar2(1024 char),
   q_additional_informations varchar2(256 char),
   q_language varchar2(16 char),
   fk_edu_context number(20),
   q_educational_learningtime varchar2(32 char),
   fk_type number(20),
   q_difficulty decimal(10,9),
   q_stdev_difficulty decimal(10,9),
   q_differentiation decimal(10,9),
   q_num_of_answers_alt number(20) default 0 not null,
   q_usage number(20) default 0 not null,
   q_assessment_type varchar2(64 char),
   q_status varchar2(32 char) not null,
   q_version varchar2(50 char),
   fk_license number(20),
   q_editor varchar2(256 char),
   q_editor_version varchar2(256 char),
   q_format varchar2(32 char) not null,
   creationdate date not null,
   lastmodified date not null,
   q_dir varchar2(32 char),
   q_root_filename varchar2(255 char),
   fk_taxonomy_level number(20),
   fk_ownergroup number(20) not null,
   primary key (id)
);

create table o_qp_pool_2_item (
   id number(20) not null,
   creationdate date not null,
   q_editable number default 0,
   fk_pool_id number(20) not null,
   fk_item_id number(20) not null,
   primary key (id)
);

create table o_qp_share_item (
   id number(20) not null,
   creationdate date not null,
   q_editable number default 0,
   fk_resource_id number(20) not null,
   fk_item_id number(20) not null,
   primary key (id)
);

create table o_qp_item_collection (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   q_name varchar2(256 char),
   fk_owner_id number(20) not null,
   primary key (id)
);

create table o_qp_collection_2_item (
   id number(20) not null,
   creationdate date not null,
   fk_collection_id number(20) not null,
   fk_item_id number(20) not null,
   primary key (id)
);

create table o_qp_edu_context (
   id number(20) not null,
   creationdate date not null,
   q_level varchar2(256 char) not null,
   q_deletable number default 0,
   primary key (id)
);

create table o_qp_item_type (
   id number(20) not null,
   creationdate date not null,
   q_type varchar2(256 char) not null,
   q_deletable number default 0,
   primary key (id)
);

create table o_qp_license (
   id number(20) not null,
   creationdate date not null,
   q_license varchar2(256 char) not null,
   q_text varchar2(2048 char),
   q_deletable number default 0,
   primary key (id)
);

--
-- Table: o_co_db_entry
--;

create table o_co_db_entry (
   id number(20) not null,
   version number(20) not null,
   lastmodified date,
   creationdate date,
   courseid number(20),
   identity number(20),
   category varchar(32 char),
   name varchar(255 char) not null,
   floatvalue float,
   longvalue number(20),
   stringvalue varchar(255 char),
   textvalue varchar2(4000 char),
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
   from o_bs_identity ident
   inner join o_user us on (ident.fk_user_id = us.user_id)
   left join o_userproperty p_firstname on (us.user_id = p_firstname.fk_user_id and p_firstname.propName = 'firstName')
   left join o_userproperty p_lastname on (us.user_id = p_lastname.fk_user_id and p_lastname.propName = 'lastName')
   left join o_userproperty p_email on (us.user_id = p_email.fk_user_id and p_email.propName = 'email')
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
   from o_gp_bgcontextresource_rel cg_bg2resource
   inner join o_gp_bgcontext cg_bgcontext on (cg_bg2resource.groupcontext_fk = cg_bgcontext.groupcontext_id)
   inner join o_gp_business cg_bgroup on (cg_bg2resource.groupcontext_fk = cg_bgroup.groupcontext_fk)
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
   from o_ep_struct_el struct
   inner join o_ep_struct_struct_link struct_link on (struct_link.fk_struct_child_id = struct.structure_id)
   inner join o_ep_struct_el parent_struct on (struct_link.fk_struct_parent_id = parent_struct.structure_id)
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
   from o_ep_struct_el struct
   inner join o_ep_struct_artefact_link artefact_link on (artefact_link.fk_struct_id = struct.structure_id)
   inner join o_ep_artefact artefact on (artefact_link.fk_artefact_id = artefact.artefact_id)
   left join o_ep_struct_el root_struct on (struct.fk_struct_root_id = root_struct.structure_id)
);

create or replace view o_ep_notifications_rating_v as (
   select
      urating.rating_id as rating_id,
      map.structure_id as map_id,
      map.title as map_title,
      cast(urating.ressubpath as number(20)) as page_key,
      page.title as page_title,
      urating.creator_id as author_id,
      urating.creationdate as creation_date,
      urating.lastmodified as last_modified 
   from o_userrating urating
   inner join o_olatresource rating_resource on (rating_resource.resid = urating.resid and rating_resource.resname = urating.resname)
   inner join o_ep_struct_el map on (map.fk_olatresource = rating_resource.resource_id)
   left join o_ep_struct_el page on (page.fk_struct_root_map_id = map.structure_id and page.structure_id = cast(urating.ressubpath as integer))
);


create or replace view o_ep_notifications_comment_v as (
   select
      ucomment.comment_id as comment_id,
      map.structure_id as map_id,
      map.title as map_title,
      cast(ucomment.ressubpath as number(20)) as page_key,
      page.title as page_title,
      ucomment.creator_id as author_id,
      ucomment.creationdate as creation_date
   from o_usercomment ucomment
   inner join o_olatresource comment_resource on (comment_resource.resid = ucomment.resid and comment_resource.resname = ucomment.resname)
   inner join o_ep_struct_el map on (map.fk_olatresource = comment_resource.resource_id)
   left join o_ep_struct_el page on (page.fk_struct_root_map_id = map.structure_id and page.structure_id = cast(ucomment.ressubpath as integer))
);

create or replace view o_gp_business_to_repository_v as (
	select 
		grp.group_id as grp_id,
		repoentry.repositoryentry_id as re_id,
		repoentry.displayname as re_displayname
	from o_gp_business grp
	inner join o_gp_business_to_resource relation on (relation.fk_group = grp.group_id)
	inner join o_repositoryentry repoentry on (repoentry.fk_olatresource = relation.fk_resource)
);

create or replace view o_re_strict_member_v as (
   select
      re.repositoryentry_id as re_id,
      re_part_member.identity_id as re_part_member_id,
      re_tutor_member.identity_id as re_tutor_member_id,
      re_owner_member.identity_id as re_owner_member_id,
      bg_part_member.identity_id as bg_part_member_id,
      bg_owner_member.identity_id as bg_owner_member_id
   from o_repositoryentry re
   left join o_bs_membership re_part_member on (re_part_member.secgroup_id = re.fk_participantgroup)
   left join o_bs_membership re_tutor_member on (re_tutor_member.secgroup_id = re.fk_tutorgroup)
   left join o_bs_membership re_owner_member on (re_owner_member.secgroup_id = re.fk_ownergroup)
   left join o_gp_business_to_resource bgroup_rel on (bgroup_rel.fk_resource = re.fk_olatresource)
   left join o_gp_business bgroup on (bgroup.group_id = bgroup_rel.fk_group)
   left join o_bs_membership bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   left join o_bs_membership bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
   where re.membersonly=1 and re.accesscode=1
);

create or replace view o_re_strict_participant_v as (
   select
      re.repositoryentry_id as re_id,
      re_part_member.identity_id as re_part_member_id,
      bg_part_member.identity_id as bg_part_member_id
   from o_repositoryentry re
   left join o_bs_membership re_part_member on (re_part_member.secgroup_id = re.fk_participantgroup)
   left join o_gp_business_to_resource bgroup_rel on (bgroup_rel.fk_resource = re.fk_olatresource)
   left join o_gp_business bgroup on (bgroup.group_id = bgroup_rel.fk_group)
   left join o_bs_membership bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup)
   where (re.membersonly=1 and re.accesscode=1) or re.accesscode>=3
);

create or replace view o_re_strict_tutor_v as (
   select
      re.repositoryentry_id as re_id,
      re_tutor_member.identity_id as re_tutor_member_id,
      re_owner_member.identity_id as re_owner_member_id,
      bg_owner_member.identity_id as bg_owner_member_id
   from o_repositoryentry re
   left join o_bs_membership re_tutor_member on (re_tutor_member.secgroup_id = re.fk_tutorgroup)
   left join o_bs_membership re_owner_member on (re_owner_member.secgroup_id = re.fk_ownergroup)
   left join o_gp_business_to_resource bgroup_rel on (bgroup_rel.fk_resource = re.fk_olatresource)
   left join o_gp_business bgroup on (bgroup.group_id = bgroup_rel.fk_group)
   left join o_bs_membership bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup)
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
   from o_bs_membership membership
   left join o_repositoryentry re_part_member on (membership.secgroup_id = re_part_member.fk_participantgroup)
   left join o_repositoryentry re_tutor_member on (membership.secgroup_id = re_tutor_member.fk_tutorgroup)
   left join o_repositoryentry re_owner_member on (membership.secgroup_id = re_owner_member.fk_ownergroup)
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
   from o_bs_membership membership
   left join o_gp_business owned_gp on (membership.secgroup_id = owned_gp.fk_ownergroup)
   left join o_gp_business participant_gp on (membership.secgroup_id = participant_gp.fk_partipiciantgroup)
   left join o_gp_business waiting_gp on (membership.secgroup_id = waiting_gp.fk_waitinggroup)
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
      (select count(part.id) from o_bs_membership part where part.secgroup_id = gp.fk_partipiciantgroup) as num_of_participants,
      (select count(pending.reservation_id) from o_ac_reservation pending where pending.fk_resource = gp.fk_resource) as num_of_pendings,
      (select count(own.id) from o_bs_membership own where own.secgroup_id = gp.fk_ownergroup) as num_of_owners,
      (case when gp.waitinglist_enabled = 1
         then 
           (select count(waiting.id) from o_bs_membership waiting where waiting.secgroup_id = gp.fk_partipiciantgroup)
         else
           0
      end) as num_waiting,
      (select count(offer.offer_id) from o_ac_offer offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=1
         and (offer.validfrom is null or offer.validfrom <= current_date)
         and (offer.validto is null or offer.validto >= current_date)
      ) as num_of_valid_offers,
      (select count(offer.offer_id) from o_ac_offer offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid=1
      ) as num_of_offers,
      (select count(relation.fk_resource) from o_gp_business_to_resource relation 
         where relation.fk_group = gp.group_id
      ) as num_of_relations,
      gp.fk_resource as fk_resource,
      gp.fk_ownergroup as fk_ownergroup,
      gp.fk_partipiciantgroup as fk_partipiciantgroup,
      gp.fk_waitinggroup as fk_waitinggroup
   from o_gp_business gp
);

create view o_gp_visible_participant_v as (
   select
      bg_part_member.id as membership_id,
      bgroup.group_id as bg_id,
      bgroup.groupname as bg_name,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_part_member.identity_id as bg_part_member_id,
      ident.name as bg_part_member_name 
   from o_gp_business bgroup
   inner join o_property bconfig on (bconfig.grp = bgroup.group_id and bconfig.name = 'displayMembers' and bconfig.category = 'config')
   inner join o_bs_membership bg_part_member on (bg_part_member.secgroup_id = bgroup.fk_partipiciantgroup and bconfig.longValue in (2,3,6,7))
   inner join o_bs_identity ident on (bg_part_member.identity_id = ident.id)
 );
   
create view o_gp_visible_owner_v as ( 
   select
      bg_owner_member.id as membership_id,
      bgroup.group_id as bg_id,
      bgroup.groupname as bg_name,
      bgroup.fk_partipiciantgroup as bg_part_sec_id,
      bgroup.fk_ownergroup as bg_owner_sec_id,
      bg_owner_member.identity_id as bg_owner_member_id,
      ident.name as bg_owner_member_name
   from o_gp_business bgroup
   inner join o_property bconfig on (bconfig.grp = bgroup.group_id and bconfig.name = 'displayMembers' and bconfig.category = 'config')
   inner join o_bs_membership bg_owner_member on (bg_owner_member.secgroup_id = bgroup.fk_ownergroup and bconfig.longValue in (1,3,5,7))
   inner join o_bs_identity ident on (bg_owner_member.identity_id = ident.id)
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
   from o_im_roster_entry entry
   inner join o_bs_identity ident on (entry.fk_identity_id = ident.id)
);


create or replace view o_qp_item_v as (
   select
      item.id as item_id,
      item.q_identifier as item_identifier,
      item.q_master_identifier as item_master_identifier,
      item.q_title as item_title,
      item.q_language as item_language,
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
      (select avg(rating.rating) from o_userrating rating
         where rating.resid=item.id and rating.resname='QuestionItem' and rating.ressubpath is null
      ) as item_rating
   from o_qp_item item
   left join o_qp_taxonomy_level taxlevel on (item.fk_taxonomy_level = taxlevel.id)
   left join o_qp_item_type itemtype on (item.fk_type = itemtype.id)
   left join o_qp_edu_context educontext on (item.fk_edu_context = educontext.id)
);

create or replace view o_qp_item_author_v as (
   select
      item.id as item_id,
      ownership.identity_id as item_author,
      item.q_identifier as item_identifier,
      item.q_master_identifier as item_master_identifier,
      item.q_title as item_title,
      item.q_language as item_language,
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
      (select avg(rating.rating) from o_userrating rating
         where rating.resid=item.id and rating.resname='QuestionItem' and rating.ressubpath is null
      ) as item_rating
   from o_qp_item item
   inner join o_bs_secgroup ownergroup on (ownergroup.id = item.fk_ownergroup)
   inner join o_bs_membership ownership on (ownergroup.id = ownership.secgroup_id) 
   left join o_qp_taxonomy_level taxlevel on (item.fk_taxonomy_level = taxlevel.id)
   left join o_qp_item_type itemtype on (item.fk_type = itemtype.id)
   left join o_qp_edu_context educontext on (item.fk_edu_context = educontext.id)
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
      (select avg(rating.rating) from o_userrating rating
         where rating.resid=item.id and rating.resname='QuestionItem' and rating.ressubpath is null
      ) as item_rating
   from o_qp_item item
   inner join o_qp_pool_2_item pool2item on (pool2item.fk_item_id = item.id)
   left join o_qp_taxonomy_level taxlevel on (item.fk_taxonomy_level = taxlevel.id)
   left join o_qp_item_type itemtype on (item.fk_type = itemtype.id)
   left join o_qp_edu_context educontext on (item.fk_edu_context = educontext.id)
);

create or replace view o_qp_pool_2_item_short_v as (
   select
      pool2item.id as item_to_pool_id,
      pool2item.creationdate as item_to_pool_creationdate,
      item.id as item_id,
      pool2item.q_editable as item_editable,
      pool2item.fk_pool_id as item_pool,
      pool.q_name as item_pool_name
   from o_qp_item item
   inner join o_qp_pool_2_item pool2item on (pool2item.fk_item_id = item.id)
   inner join o_qp_pool pool on (pool2item.fk_pool_id = pool.id)
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
      (select avg(rating.rating) from o_userrating rating
         where rating.resid=item.id and rating.resname='QuestionItem' and rating.ressubpath is null
      ) as item_rating
   from o_qp_item item
   inner join o_qp_share_item shareditem on (shareditem.fk_item_id = item.id)
   left join o_qp_taxonomy_level taxlevel on (item.fk_taxonomy_level = taxlevel.id)
   left join o_qp_item_type itemtype on (item.fk_type = itemtype.id)
   left join o_qp_edu_context educontext on (item.fk_edu_context = educontext.id)
);

create or replace view o_qp_share_2_item_short_v as (
   select
      shareditem.id as item_to_share_id,
      shareditem.creationdate as item_to_share_creationdate,
      item.id as item_id,
      shareditem.q_editable as item_editable,
      shareditem.fk_resource_id as resource_id,
      bgroup.groupname as resource_name
   from o_qp_item item
   inner join o_qp_share_item shareditem on (shareditem.fk_item_id = item.id)
   inner join o_gp_business bgroup on (shareditem.fk_resource_id = bgroup.fk_resource)
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
create index userrating_id_idx on o_userrating (resid);
create index userrating_name_idx on o_userrating (resname);
create index userrating_subpath_idx on o_userrating (ressubpath);
create index userrating_rating_idx on o_userrating (rating);
create index FKF26C8375236F20X on o_userrating (creator_id);
alter table o_userrating  add constraint FKF26C8375236F20X foreign key (creator_id) references o_bs_identity (id);

create index usercmt_id_idx on o_usercomment (resid);
create index usercmt_name_idx on o_usercomment (resname);
create index usercmt_subpath_idx on o_usercomment (ressubpath);
create index FK92B6864A18251F0 on o_usercomment (parent_key);
create index FKF26C8375236F20A on o_usercomment (creator_id);
alter table o_usercomment  add constraint FK92B6864A18251F0 foreign key (parent_key) references o_usercomment (comment_id);
alter table o_usercomment  add constraint FKF26C8375236F20A foreign key (creator_id) references o_bs_identity (id);

alter table o_checkpoint_results add constraint FK9E30F4B661159ZZY foreign key (checkpoint_fk) references o_checkpoint (checkpoint_id);
alter table o_checkpoint_results add constraint FK9E30F4B661159ZZX foreign key (identity_fk) references o_bs_identity  (id);

--create index  asset_idx on o_plock (asset);

create index statdow_resid_idx on o_stat_dayofweek (resid);
create index stathod_resid_idx on o_stat_hourofday (resid);
create index statwee_resid_idx on o_stat_weekly (resid);
create index statday_resid_idx on o_stat_daily (resid);
create index stathor_resid_idx on o_stat_homeorg (resid);
create index statorg_resid_idx on o_stat_orgtype (resid);
create index statstl_resid_idx on o_stat_studylevel (resid);
create index statstb_resid_idx on o_stat_studybranch3 (resid);
create index resid_idx1 on o_property (resourcetypeid);
create index category_idx on o_property (category);
create index name_idx1 on o_property (name);
create index restype_idx1 on o_property (resourcetypename);

create index FKB60B1BA5190E5 on o_property (grp);
alter table o_property  add constraint FKB60B1BA5190E5 foreign key (grp) references o_gp_business (group_id);
create index FKB60B1BA5F7E870BE on o_property (identity);
alter table o_property  add constraint FKB60B1BA5F7E870BE foreign key (identity) references o_bs_identity (id);
create index  gp_name_idx on o_gp_business (groupname);
create index  gp_type_idx on o_gp_business (businessgrouptype);
create index FKCEEB8A86DF6BCD14 on o_gp_business (groupcontext_fk);
-- create index FKCEEB8A86A1FAC766 on o_gp_business (fk_ownergroup);
alter table o_gp_business  add constraint FKCEEB8A86A1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup (id);
-- create index FKCEEB8A86C06E3EF3 on o_gp_business (fk_partipiciantgroup);
alter table o_gp_business  add constraint FKCEEB8A86C06E3EF3 foreign key (fk_partipiciantgroup) references o_bs_secgroup (id);

alter table o_gp_business add constraint idx_bgp_rsrc foreign key (fk_resource) references o_olatresource (resource_id);

alter table o_gp_business_to_resource add constraint idx_bgp_to_rsrc_rsrc foreign key (fk_resource) references o_olatresource (resource_id);
alter table o_gp_business_to_resource add constraint idx_bgp_to_rsrc_group foreign key (fk_group) references o_gp_business (group_id);

create index  provider_idx on o_bs_authentication (provider);
create index  credential_idx on o_bs_authentication (credential);
create index  authusername_idx on o_bs_authentication (authusername);
create index FKC6A5445652595FE6 on o_bs_authentication (identity_fk);
alter table o_bs_authentication  add constraint FKC6A5445652595FE6 foreign key (identity_fk) references o_bs_identity (id);

create index name_idx2 on o_noti_pub (resname, resid, subident);
create index  oresdetindex on o_qtiresultset (olatresourcedetail);
create index  oresindex on o_qtiresultset (olatresource_fk);
create index  reprefindex on o_qtiresultset (repositoryref_fk);
create index  assindex on o_qtiresultset (assessmentid);
create index FK14805D0F5259603C on o_qtiresultset (identity_id);
alter table o_qtiresultset  add constraint FK14805D0F5259603C foreign key (identity_id) references o_bs_identity (id);
--create index name_idx3 on o_bs_identity (name);
create index  identstatus_idx on o_bs_identity (status);
-- create index FKFF94111CD1A80C95 on o_bs_identity (fk_user_id);
alter table o_bs_identity  add constraint FKFF94111CD1A80C95 foreign key (fk_user_id) references o_user (user_id);
create index FK4B04D83FD1A80C95 on o_userproperty (fk_user_id);
alter table o_userproperty  add constraint FK4B04D83FD1A80C95 foreign key (fk_user_id) references o_user (user_id);

--create index  name_idx4 on o_olatresource (resname);
--create index  id_idx on o_olatresource (resid);
--create index  groupname_idx on o_bs_namedgroup (groupname);
create index FKBAFCBBC4B85B522C on o_bs_namedgroup (secgroup_id);
alter table o_bs_namedgroup  add constraint FKBAFCBBC4B85B522C foreign key (secgroup_id) references o_bs_secgroup (id);
create index FKF4433C2C7B66B0D0 on o_catentry (parent_id);
alter table o_catentry  add constraint FKF4433C2C7B66B0D0 foreign key (parent_id) references o_catentry (id);
-- create index FKF4433C2CA1FAC766 on o_catentry (fk_ownergroup);
alter table o_catentry  add constraint FKF4433C2CA1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup (id);
create index FKF4433C2CDDD69946 on o_catentry (fk_repoentry);
alter table o_catentry  add constraint FKF4433C2CDDD69946 foreign key (fk_repoentry) references o_repositoryentry (repositoryentry_id);

create index resid_idx2 on o_note (resourcetypeid);
create index  owner_idx on o_note (owner_id);
create index restype_idx2 on o_note (resourcetypename);
-- create index FKC2D855C263219E27 on o_note (owner_id);
alter table o_note  add constraint FKC2D855C263219E27 foreign key (owner_id) references o_bs_identity (id);
create index  type_idx on o_gp_bgcontext (grouptype);
create index  default_idx on o_gp_bgcontext (defaultcontext);
create index name_idx5 on o_gp_bgcontext (name);
-- create index FK1C154FC47E4A0638 on o_gp_bgcontext (ownergroup_fk);
alter table o_gp_bgcontext  add constraint FK1C154FC47E4A0638 foreign key (ownergroup_fk) references o_bs_secgroup (id);
--create index FKE971B4589AC44FBF on o_references (source_id);
--alter table o_references  add constraint FKE971B4589AC44FBF foreign key (source_id) references o_olatresource (resource_id);
create index FKE971B458CF634A89 on o_references (target_id);
alter table o_references  add constraint FKE971B458CF634A89 foreign key (target_id) references o_olatresource (resource_id);
create index FKDB97A6493F14E3EE on o_repositorymetadata (fk_repositoryentry);
alter table o_repositorymetadata  add constraint FKDB97A6493F14E3EE foreign key (fk_repositoryentry) references o_repositoryentry (repositoryentry_id);

--create index  propvalue_idx on o_userproperty (propvalue);
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

create index  itemindex on o_qtiresult (itemident);

create index FK3563E67340EF401F on o_qtiresult (resultset_fk);
alter table o_qtiresult  add constraint FK3563E67340EF401F foreign key (resultset_fk) references o_qtiresultset (resultset_id);
create index FK9A1C5109F9C3F1D on o_bs_policy (oresource_id);
alter table o_bs_policy  add constraint FK9A1C5109F9C3F1D foreign key (oresource_id) references o_olatresource (resource_id);
create index FK9A1C5101E2E76DB on o_bs_policy (group_id);
alter table o_bs_policy  add constraint FK9A1C5101E2E76DB foreign key (group_id) references o_bs_secgroup (id);

create index name_idx6 on o_gp_bgarea (name);

alter table o_gp_bgarea add constraint idx_area_to_resource foreign key (fk_resource) references o_olatresource (resource_id);

create index descritpion_idx on o_repositoryentry (description);
create index  access_idx on o_repositoryentry (accesscode);
create index  initialAuthor_idx on o_repositoryentry (initialauthor);
create index  resource_idx on o_repositoryentry (resourcename);
create index  displayname_idx on o_repositoryentry (displayname);
--create index  softkey_idx on o_repositoryentry (softkey);

-- create index FK2F9C439888C31018 on o_repositoryentry (fk_olatresource);
alter table o_repositoryentry  add constraint FK2F9C439888C31018 foreign key (fk_olatresource) references o_olatresource (resource_id);
-- create index FK2F9C4398A1FAC766 on o_repositoryentry (fk_ownergroup);
alter table o_repositoryentry  add constraint FK2F9C4398A1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup (id);
create index repo_members_only_idx on o_repositoryentry (membersonly);
alter table o_repositoryentry add constraint repo_tutor_sec_group_ctx foreign key (fk_tutorgroup) references o_bs_secgroup (id);
alter table o_repositoryentry add constraint repo_parti_sec_group_ctx foreign key (fk_participantgroup) references o_bs_secgroup (id);
create index FK68C4E30663219E27 on o_bookmark (owner_id);
alter table o_bookmark  add constraint FK68C4E30663219E27 foreign key (owner_id) references o_bs_identity (id);
create index FK7B6288B45259603C on o_bs_membership (identity_id);
alter table o_bs_membership  add constraint FK7B6288B45259603C foreign key (identity_id) references o_bs_identity (id);
create index FK7B6288B4B85B522C on o_bs_membership (secgroup_id);
alter table o_bs_membership  add constraint FK7B6288B4B85B522C foreign key (secgroup_id) references o_bs_secgroup (id);
create index  lc_pref_idx on o_lifecycle (persistentref);
create index  lc_type_idx on o_lifecycle (persistenttypename);
create index  lc_action_idx on o_lifecycle (action);
create index  readmessage_forum_idx on o_readmessage (forum_id);
create index  readmessage_identity_idx on o_readmessage (identity_id);
create index projectbroker_prj_broker_idx on o_projectbroker_project (projectbroker_fk);
--create index projectbroker_project_id_idx on o_projectbroker_project (project_id);
create index o_projectbroker_custflds_idx on o_projectbroker_customfields (fk_project_id);
create index usr_notification_interval_idx on o_user (notification_interval);

create index mark_id_idx on o_mark (resid);
create index mark_name_idx on o_mark (resname);
create index mark_subpath_idx on o_mark (ressubpath);
create index mark_businesspath_idx on o_mark (businesspath);
create index FKF26C8375236F21X on o_mark (creator_id);
alter table o_mark add constraint FKF26C8375236F21X foreign key (creator_id) references o_bs_identity (id);

-- foreign keys
create index imsg_resid_idx on o_info_message (resid);
create index imsg_author_idx on o_info_message (fk_author_id);
alter table o_info_message  add constraint FKF85553465A4FA5DC foreign key (fk_author_id) references o_bs_identity (id);
create index imsg_modifier_idx on o_info_message (fk_modifier_id);
alter table o_info_message add constraint FKF85553465A4FA5EF foreign key (fk_modifier_id) references o_bs_identity (id);

-- big copy bth
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

create index paypal_pay_key_idx on o_ac_paypal_transaction (pay_key);
create index paypal_pay_trx_id_idx on o_ac_paypal_transaction (ipn_transaction_id);
create index paypal_pay_s_trx_id_idx on o_ac_paypal_transaction (ipn_sender_transaction_id);

alter table o_as_eff_statement add constraint eff_statement_id_cstr foreign key (fk_identity) references o_bs_identity (id);
create index eff_statement_repo_key_idx on o_as_eff_statement (course_repo_key);
alter table o_as_user_course_infos add constraint user_course_infos_id_cstr foreign key (fk_identity) references o_bs_identity (id);
alter table o_as_user_course_infos add constraint user_course_infos_res_cstr foreign key (fk_resource_id) references o_olatresource (resource_id);

alter table o_ac_reservation add constraint idx_rsrv_to_rsrc_rsrc foreign key (fk_resource) references o_olatresource (resource_id);
alter table o_ac_reservation add constraint idx_rsrv_to_rsrc_identity foreign key (fk_identity) references o_bs_identity (id);

alter table o_im_message add constraint idx_im_msg_to_fromid foreign key (fk_from_identity_id) references o_bs_identity (id);
create index idx_im_msg_res_idx on o_im_message (msg_resid,msg_resname);
alter table o_im_notification add constraint idx_im_not_to_toid foreign key (fk_to_identity_id) references o_bs_identity (id);
alter table o_im_notification add constraint idx_im_not_to_fromid foreign key (fk_from_identity_id) references o_bs_identity (id);
create index idx_im_chat_res_idx on o_im_notification (chat_resid,chat_resname);
alter table o_im_roster_entry add constraint idx_im_rost_to_id foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_im_rost_res_idx on o_im_roster_entry (r_resid,r_resname);
alter table o_im_preferences add constraint idx_im_prfs_to_id foreign key (fk_from_identity_id) references o_bs_identity (id);

create index o_co_db_course_idx on o_co_db_entry (courseid);
create index o_co_db_cat_idx on o_co_db_entry (category);
create index o_co_db_name_idx on o_co_db_entry (name);
alter table o_co_db_entry add constraint FKB60B1BA5F7E870XY foreign key (identity) references o_bs_identity;

alter table o_om_room_reference  add constraint idx_omroom_to_bgroup foreign key (businessgroup) references o_gp_business (group_id);
create index idx_omroom_residname on o_om_room_reference (resourcetypename,resourcetypeid);

create index o_mapper_uuid_idx on o_mapper (mapper_uuid);

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


insert into o_stat_lastupdated (until_datetime, lastupdated) values (to_date('1999-01-01', 'YYYY-mm-dd'), to_date('1999-01-01', 'YYYY-mm-dd'));
insert into hibernate_unique_key values ( 0 );
commit
/
