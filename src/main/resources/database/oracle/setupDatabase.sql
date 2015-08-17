-- tables


CREATE TABLE o_forum (
  forum_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  PRIMARY KEY (forum_id)
);


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


CREATE TABLE o_bs_secgroup (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  PRIMARY KEY (id)
);

CREATE TABLE o_bs_group (
   id number(20) not null,
   creationdate date not null,
   g_name varchar2(36 char),
   PRIMARY KEY (id)
);

CREATE TABLE o_bs_group_member (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   g_role varchar2(50 char) not null,
   fk_group_id number(20) not null,
   fk_identity_id number(20) not null,
   PRIMARY KEY (id)
);

CREATE TABLE o_bs_grant (
   id number(20) not null,
   creationdate date not null,
   g_role varchar2(32 char) not null,
   g_permission varchar2(32 char) not null,
   fk_group_id number(20) not null,
   fk_resource_id number(20) not null,
   PRIMARY KEY (id)
);

CREATE TABLE o_gp_business (
  group_id number(20) NOT NULL,
  version number(20) NOT NULL,
  lastmodified date,
  creationdate date,
  lastusage date,
  groupname varchar2(255 char),
  external_id varchar2(64 char),
  managed_flags varchar2(255 char),
  descr varchar2(4000),
  minparticipants number(11),
  maxparticipants number(11),
  waitinglist_enabled number,
  autocloseranks_enabled number,
  ownersintern number default 0 not null,
  participantsintern number default 0 not null,
  waitingintern number default 0 not null,
  ownerspublic number default 0 not null,
  participantspublic number default 0 not null,
  waitingpublic number default 0 not null,
  downloadmembers number default 0 not null,
  allowtoleave number default 1 not null,
  fk_resource number(20),
  fk_group_id number(20),
  CONSTRAINT u_o_gp_business03 UNIQUE (fk_resource),
  CONSTRAINT u_o_gp_business06 UNIQUE (fk_group_id),
  PRIMARY KEY (group_id)
);


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


CREATE TABLE o_bs_authentication (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  identity_fk number(20) NOT NULL,
  provider varchar2(8 char),
  authusername varchar2(255 char),
  credential varchar2(255 char),
  salt varchar2(255 char),
  hashalgorithm varchar2(16 char),
  PRIMARY KEY (id),
  CONSTRAINT u_o_bs_authentication UNIQUE (provider, authusername)
);


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


CREATE TABLE o_bs_identity (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  lastlogin date,
  name varchar2(128 char) NOT NULL,
  external_id varchar2(64 char),
  status number(11),
  fk_user_id number(20),
  CONSTRAINT u_o_bs_identity UNIQUE (name),
  CONSTRAINT u_o_bs_identity01 UNIQUE (fk_user_id),
  PRIMARY KEY (id)
);


CREATE TABLE o_olatresource (
  resource_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  resname varchar2(50 char) NOT NULL,
  resid number(20) NOT NULL,
  PRIMARY KEY (resource_id),
  CONSTRAINT u_o_olatresource UNIQUE (resname, resid)
);


CREATE TABLE o_bs_namedgroup (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  secgroup_id number(20) NOT NULL,
  groupname varchar2(16 char),
  PRIMARY KEY (id),
  CONSTRAINT u_o_bs_namedgroup UNIQUE (groupname)
);


CREATE TABLE o_catentry (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  name varchar2(110 char) NOT NULL,
  description varchar2(4000),
  style varchar2(16 char),
  externalurl varchar2(255 char),
  fk_repoentry number(20),
  fk_ownergroup number(20),
  type number(11) NOT NULL,
  parent_id number(20),
  CONSTRAINT u_o_catentry UNIQUE (fk_ownergroup),
  PRIMARY KEY (id)
);


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


CREATE TABLE o_references (
  reference_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  source_id number(20) NOT NULL,
  target_id number(20) NOT NULL,
  userdata varchar2(64 char),
  PRIMARY KEY (reference_id)
);


CREATE TABLE o_user (
  user_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  language varchar2(30 char),
  fontsize varchar2(10 char),
  notification_interval varchar2(16 char),
  receiverealmail varchar2(16 char),
  presencemessagespublic number,
  informsessiontimeout number NOT NULL,
  PRIMARY KEY (user_id)
);


CREATE TABLE o_userproperty (
  fk_user_id number(20) NOT NULL,
  propname varchar2(255 char) NOT NULL,
  propvalue varchar2(255 char),
  PRIMARY KEY (fk_user_id, propname)
);


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


CREATE TABLE o_gp_bgtoarea_rel (
  bgtoarea_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  group_fk number(20) NOT NULL,
  area_fk number(20) NOT NULL,
  PRIMARY KEY (bgtoarea_id),
  CONSTRAINT u_o_gp_bgtoarea_rel UNIQUE (group_fk, area_fk)
);


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


CREATE TABLE o_bs_policy (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  oresource_id number(20) NOT NULL,
  group_id number(20) NOT NULL,
  permission varchar2(16 char) NOT NULL,
  apply_from date DEFAULT NULL,
  apply_to date DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT u_o_bs_policy UNIQUE (oresource_id, group_id, permission)
);


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
-- Table: o_repositoryentry
--;

CREATE TABLE o_repositoryentry (
  repositoryentry_id number(20) NOT NULL,
  version number(20) NOT NULL,
  lastmodified date,
  creationdate date,
  lastusage date,
  softkey varchar2(36 char) NOT NULL,
  external_id varchar2(64 char),
  external_ref varchar2(64 char),
  managed_flags varchar2(255 char),
  displayname varchar2(110 char) NOT NULL,
  resourcename varchar2(100 char) NOT NULL,
  authors varchar2(2048 char),
  mainlanguage varchar2(255 char),
  objectives varchar2(2048 char),
  requirements varchar2(2048 char),
  credits varchar2(2048 char),
  expenditureofwork varchar2(255 char),
  fk_stats number(20) not null,
  fk_lifecycle number(20),
  fk_olatresource number(20),
  description varchar2(4000),
  initialauthor varchar2(128 char) NOT NULL,
  accesscode number(11) NOT NULL,
  membersonly number default 0,
  statuscode number(11),
  allowtoleave varchar2(16 char),
  canlaunch number NOT NULL,
  candownload number NOT NULL,
  cancopy number NOT NULL,
  canreference number NOT NULL,
  CONSTRAINT u_o_repositoryentry UNIQUE (softkey),
  CONSTRAINT u_o_repositoryentry01 UNIQUE (fk_olatresource),
  CONSTRAINT u_o_repositoryentry02 UNIQUE (fk_stats),
  PRIMARY KEY (repositoryentry_id)
);
CREATE TABLE o_re_to_group (
   id number(20) not null,
   creationdate date not null,
   r_defgroup number default 0 not null,
   fk_group_id number(20) not null,
   fk_entry_id number(20) not null,
   PRIMARY KEY (id)
);
CREATE TABLE o_repositoryentry_cycle (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   r_softkey varchar2(64 char),
   r_label varchar2(255 char),
   r_privatecycle number default 0,
   r_validfrom date,
   r_validto date,
   PRIMARY KEY (id)
);
CREATE TABLE o_repositoryentry_stats (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   r_rating float,
   r_num_of_ratings number(20) default 0 not null,
   r_num_of_comments number(20) default 0 not null,
   r_launchcounter number(20) default 0 not null,
   r_downloadcounter number(20) default 0 not null,
   r_lastusage date not null,
   PRIMARY KEY (id)
);
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


CREATE TABLE o_plock (
  plock_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  asset varchar2(255 char) NOT NULL,
  CONSTRAINT u_o_plock UNIQUE (asset),
  PRIMARY KEY (plock_id)
);


CREATE TABLE hibernate_unique_key (
  next_hi number(11)
);


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


CREATE TABLE oc_lock (
  lock_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  identity_fk number(20) NOT NULL,
  asset varchar2(120 char) NOT NULL,
  CONSTRAINT u_oc_lock UNIQUE (asset),
  PRIMARY KEY (lock_id)
);


CREATE TABLE o_readmessage (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  identity_id number(20) NOT NULL,
  forum_id number(20) NOT NULL,
  message_id number(20) NOT NULL,
  PRIMARY KEY (id)
);


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


CREATE TABLE o_checklist (
  checklist_id number(20) NOT NULL,
  version number(20) NOT NULL,
  lastmodified date NOT NULL,
  title varchar2(255 char),
  description varchar2(4000),
  PRIMARY KEY (checklist_id)
);


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


CREATE TABLE o_checkpoint_results (
  checkpoint_result_id number(20) NOT NULL,
  version number(20) NOT NULL,
  lastmodified date NOT NULL,
  result number NOT NULL,
  checkpoint_fk number(20),
  identity_fk number(20),
  PRIMARY KEY (checkpoint_result_id)
);


CREATE TABLE o_projectbroker (
  projectbroker_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  PRIMARY KEY (projectbroker_id)
);


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


CREATE TABLE o_projectbroker_customfields (
  fk_project_id number(20) NOT NULL,
  propname varchar2(255 char) NOT NULL,
  propvalue varchar2(255 char),
  PRIMARY KEY (fk_project_id, propname)
);


CREATE TABLE o_usercomment (
  comment_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  resname varchar2(50 char) NOT NULL,
  resid number(20) NOT NULL,
  ressubpath varchar2(2048 char),
  creator_id number(20) NOT NULL,
  commenttext varchar2(4000),
  parent_key number(20),
  PRIMARY KEY (comment_id)
);


CREATE TABLE o_userrating (
  rating_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  lastmodified date,
  resname varchar2(50 char) NOT NULL,
  resid number(20) NOT NULL,
  ressubpath varchar2(2048 char),
  creator_id number(20) NOT NULL,
  rating number(11) NOT NULL,
  PRIMARY KEY (rating_id)
);

CREATE TABLE o_info_message (
  info_id number(20)  NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  modificationdate date,
  title varchar2(2048 char),
  message clob,
  resname varchar(50 char) NOT NULL,
  resid number(20) NOT NULL,
  ressubpath varchar2(2048 char),
  businesspath varchar2(2048 char),
  fk_author_id number(20),
  fk_modifier_id number(20),
  PRIMARY KEY (info_id)
);

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

create table o_ep_struct_struct_link (
  link_id number(20) not null,
  version number(20) not null,
  creationdate date,
  pos number(20) default 0,
  fk_struct_parent_id number(20) not null,
  fk_struct_child_id number(20) not null,
  primary key (link_id)
);

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
create table o_ep_struct_to_group (
   id number(20) not null,
   creationdate date not null,
   r_defgroup number default 0 not null,
   r_role varchar2(64 char),
   r_valid_from date,
   r_valid_to date,
   fk_group_id number(20),
   fk_struct_id number(20),
   PRIMARY KEY (id)
);
create table o_bs_invitation (
   id number(20) not null,
   version number(20) not null,
   creationdate date,
   token varchar(64 char) not null,
   first_name varchar(64 char),
   last_name varchar(64 char),
   mail varchar(128 char),
   fk_group_id number(20),
   primary key (id)
);

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

create table o_mail_to_recipient (
  pos number(20) default 0,
  fk_mail_id number(20),
  fk_recipient_id number(20)
);

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

create table o_mail_attachment (
  attachment_id number(20) NOT NULL,
  creationdate date,
  datas blob,
  datas_size number(20),
  datas_name varchar(255 char),
  mimetype varchar(255 char),
  datas_checksum number(20),
  datas_path varchar2(1024 char),
  datas_lastmodified date,
  fk_att_mail_id number(20),
  primary key (attachment_id)
);

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
  autobooking number default 0 not null,
  price_amount number(20,2),
  price_currency_code VARCHAR(3 char),
  offer_desc VARCHAR(2000 char),
  fk_resource_id number(20),
  primary key (offer_id)
);

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

CREATE TABLE o_stat_lastupdated (
  lastupdated date not null,
  from_datetime date not null,
  until_datetime date not null
);


CREATE SEQUENCE sq_o_stat_dayofweek_id;

CREATE TABLE o_stat_dayofweek (
  id number(20) NOT NULL,
  businesspath varchar2(2048 char) NOT NULL,
  resid number(20) NOT NULL,
  day number(11) NOT NULL,
  value number(11) NOT NULL,
  PRIMARY KEY (id)
);


CREATE SEQUENCE sq_o_stat_hourofday_id;

CREATE TABLE o_stat_hourofday (
  id number(20) NOT NULL,
  businesspath varchar2(2048 char) NOT NULL,
  resid number(20) NOT NULL,
  hour number(11) NOT NULL,
  value number(11) NOT NULL,
  PRIMARY KEY (id)
);


CREATE SEQUENCE sq_o_stat_weekly_id;

CREATE TABLE o_stat_weekly (
  id number(20) NOT NULL,
  businesspath varchar2(2048 char) NOT NULL,
  resid number(20) NOT NULL,
  week varchar2(7 char) NOT NULL,
  value number(11) NOT NULL,
  PRIMARY KEY (id)
);


CREATE SEQUENCE sq_o_stat_daily_id;

CREATE TABLE o_stat_daily (
  id number(20) NOT NULL,
  businesspath varchar2(2048 char) NOT NULL,
  resid number(20) NOT NULL,
  day date NOT NULL,
  value number(11) NOT NULL,
  PRIMARY KEY (id)
);


CREATE SEQUENCE sq_o_stat_homeorg_id;

CREATE TABLE o_stat_homeorg (
  id number(20) NOT NULL,
  businesspath varchar2(2048 char) NOT NULL,
  resid number(20) NOT NULL,
  homeorg varchar2(255 char) NOT NULL,
  value number(11) NOT NULL,
  PRIMARY KEY (id)
);


CREATE SEQUENCE sq_o_stat_orgtype_id;

CREATE TABLE o_stat_orgtype (
  id number(20) NOT NULL,
  businesspath varchar2(2048 char) NOT NULL,
  resid number(20) NOT NULL,
  orgtype varchar2(255 char),
  value number(11) NOT NULL,
  PRIMARY KEY (id)
);


CREATE SEQUENCE sq_o_stat_studylevel_id;

CREATE TABLE o_stat_studylevel (
  id number(20) NOT NULL,
  businesspath varchar2(2048 char) NOT NULL,
  resid number(20) NOT NULL,
  studylevel varchar2(255 char) NOT NULL,
  value number(11) NOT NULL,
  PRIMARY KEY (id)
);


CREATE SEQUENCE sq_o_stat_studybranch3_id;

CREATE TABLE o_stat_studybranch3 (
  id number(20) NOT NULL,
  businesspath varchar2(2048 char) NOT NULL,
  resid number(20) NOT NULL,
  studybranch3 varchar2(255 char),
  value number(11) NOT NULL,
  PRIMARY KEY (id)
);


CREATE TABLE o_mark (
  mark_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  resname varchar2(50 char) NOT NULL,
  resid number(20) NOT NULL,
  ressubpath varchar2(2048 char),
  businesspath varchar2(2048 char),
  creator_id number(20) NOT NULL,
  PRIMARY KEY (mark_id)
);


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

create table o_as_mode_course (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   a_name varchar2(255 char),
   a_description clob,
   a_status varchar2(16 char),
   a_manual_beginend number default 0 not null,
   a_begin date not null,
   a_leadtime number(20) default 0 not null,
   a_begin_with_leadtime date not null,
   a_end date not null,
   a_followuptime number(20) default 0 not null,
   a_end_with_followuptime date not null,
   a_targetaudience varchar2(16 char),
   a_restrictaccesselements number default 0 not null,
   a_elements varchar2(2048 char),
   a_start_element varchar2(64 char),
   a_restrictaccessips number default 0 not null,
   a_ips varchar2(2048 char),
   a_safeexambrowser number default 0 not null,
   a_safeexambrowserkey varchar2(2048 char),
   a_safeexambrowserhint clob,
   a_applysettingscoach number default 0 not null,
   fk_entry number(20) not null,
   primary key (id)
);

create table o_as_mode_course_to_group (
   id number(20) not null,
   fk_assessment_mode_id number(20) not null,
   fk_group_id number(20) not null,
   primary key (id)
);

create table o_as_mode_course_to_area (
   id number(20) not null,
   fk_assessment_mode_id number(20) not null,
   fk_area_id number(20) not null,
   primary key (id)
);

create table o_cer_template (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   c_name varchar2(256 char) not null,
   c_path varchar2(1024 char) not null,
   c_public number default 0 not null,
   c_format varchar2(16 char),
   c_orientation varchar2(16 char),
   primary key (id)
);

create table o_cer_certificate (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   c_status varchar2(16 char) default 'pending' not null,
   c_email_status varchar2(16 char),
   c_uuid varchar2(36 char) not null,
   c_path varchar2(1024 char),
   c_last number default 1 not null,
   c_course_title varchar2(255 char),
   c_archived_resource_id number(20) not null,
   fk_olatresource number(20),
   fk_identity number(20) not null,
   primary key (id)
);

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

create table o_im_notification (
   id number(20) not null,
   creationdate date,
   chat_resname varchar(50) not null,
   chat_resid number(20) not null,
   fk_to_identity_id number(20) not null,
   fk_from_identity_id number(20) not null,
   primary key (id)
);

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

create table o_im_preferences (
   id number(20) not null,
   creationdate date,
   visible_to_others number default 0,
   roster_def_status varchar(12),
   fk_from_identity_id number(20) not null,
   primary key (id)
);

create table o_mapper (
   id number(20) not null,
   lastmodified date,
   creationdate date,
   expirationdate date,
   mapper_uuid varchar(64 char),
   orig_session_id varchar(64 char),
   xml_config CLOB,
   primary key (id)
);

create table o_qti_assessment_session (
   id NUMBER(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   q_exploded number default 0 not null,
   q_author_mode number default 0 not null,
   q_finish_time date,
   q_termination_time date,
   q_storage varchar2(32 char),
   fk_identity number(20) not null,
   fk_entry number(20) not null,
   fk_course number(20),
   q_course_subident varchar2(64 char),
   primary key (id)
);

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

create table o_lti_outcome (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   r_ressubpath varchar2(2048 char),
   r_action varchar2(255 char) not null,
   r_outcome_key varchar2(255 char) not null,
   r_outcome_value varchar2(2048 char),
   fk_resource_id number(20) not null,
   fk_identity_id number(20) not null,
   primary key (id)
);

create table o_cl_checkbox (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   c_checkboxid varchar2(50 char) not null,
   c_resname varchar2(50 char) not null,
   c_resid number(20) not null,
   c_ressubpath varchar2(255 char) not null,
   primary key (id)
);

create table o_cl_check (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   c_score float,
   c_checked number default 0,
   fk_identity_id number(20) not null,
   fk_checkbox_id number(20) not null,
   primary key (id)
);

create table o_gta_task_list (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   g_course_node_ident varchar2(36 char),
   g_roundrobin varchar2(4000 char),
   fk_entry number(20) not null,
   primary key (id)
);

create table o_gta_task (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   g_status varchar2(36 char),
   g_rev_loop number(20) default 0 not null,
   g_taskname varchar2(36 char),
   g_assignment_date date,
   fk_tasklist number(20) not null,
   fk_identity number(20),
   fk_businessgroup number(20),
   primary key (id)
);

create table o_rem_reminder (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   r_description varchar(255),
   r_start date,
   r_sendtime varchar(16),
   r_configuration clob,
   r_email_body clob,
   fk_creator number(20) not null,
   fk_entry number(20) not null,
   primary key (id)
);

create table o_rem_sent_reminder (
   id int8 not null,
   creationdate timestamp not null,
   r_status varchar(16),
   fk_identity int8 not null,
   fk_reminder int8 not null,
   primary key (id)
);

create table o_ex_task (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   e_name varchar2(255 char) not null,
   e_status varchar2(16 char) not null,
   e_status_before_edit varchar2(16 char),
   e_executor_node varchar2(16 char),
   e_executor_boot_id varchar2(64 char),
   e_task clob not null,
   e_scheduled date,
   e_ressubpath varchar2(2048 char),
   fk_resource_id number(20),
   fk_identity_id number(20),
   primary key (id)
);

create table o_ex_task_modifier (
   id number(20) not null,
   creationdate date not null,
   fk_task_id number(20) not null,
   fk_identity_id number(20) not null,
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

create view o_gp_business_to_repository_v as (
	select 
		grp.group_id as grp_id,
		repoentry.repositoryentry_id as re_id,
		repoentry.displayname as re_displayname
	from o_gp_business grp
	inner join o_re_to_group relation on (relation.fk_group_id = grp.fk_group_id)
	inner join o_repositoryentry repoentry on (repoentry.repositoryentry_id = relation.fk_entry_id)
);

create view o_bs_gp_membership_v as (
   select
      gp.group_id as group_id,
      membership.id as membership_id,
      membership.fk_identity_id as fk_identity_id,
      membership.lastmodified as lastmodified,
      membership.creationdate as creationdate,
      membership.g_role as g_role
   from o_bs_group_member membership
   inner join o_gp_business gp on (gp.fk_group_id=membership.fk_group_id)
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
      gp.external_id as external_id,
      gp.managed_flags as managed_flags,
      (select count(part.id) from o_bs_group_member part where part.fk_group_id = gp.fk_group_id and part.g_role='participant') as num_of_participants,
      (select count(pending.reservation_id) from o_ac_reservation pending where pending.fk_resource = gp.fk_resource) as num_of_pendings,
      (select count(own.id) from o_bs_group_member own where own.fk_group_id = gp.fk_group_id and own.g_role='coach') as num_of_owners,
      (case when gp.waitinglist_enabled > 0
         then 
           (select count(waiting.id) from o_bs_group_member waiting where waiting.fk_group_id = gp.fk_group_id and waiting.g_role='waiting')
         else
           0
      end) as num_waiting,
      (select count(offer.offer_id) from o_ac_offer offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid > 0
         and (offer.validfrom is null or offer.validfrom <= current_date)
         and (offer.validto is null or offer.validto >= current_date)
      ) as num_of_valid_offers,
      (select count(offer.offer_id) from o_ac_offer offer 
         where offer.fk_resource_id = gp.fk_resource
         and offer.is_valid > 0
      ) as num_of_offers,
      (select count(relation.fk_entry_id) from o_re_to_group relation 
         where relation.fk_group_id = gp.fk_group_id
      ) as num_of_relations,
      gp.fk_resource as fk_resource,
      gp.fk_group_id as fk_group_id
   from o_gp_business gp
);

create or replace view o_re_membership_v as (
   select
      bmember.id as membership_id,
      bmember.creationdate as creationdate,
      bmember.lastmodified as lastmodified,
      bmember.fk_identity_id as fk_identity_id,
      bmember.g_role as g_role,
      re.repositoryentry_id as fk_entry_id
   from o_repositoryentry re
   inner join o_re_to_group relgroup on (relgroup.fk_entry_id=re.repositoryentry_id and relgroup.r_defgroup=1)
   inner join o_bs_group_member bmember on (bmember.fk_group_id=relgroup.fk_group_id) 
);

-- contacts
create view o_gp_contactkey_v as (
   select
      bg_member.id as membership_id,
      bg_member.fk_identity_id as member_id,
      bg_member.g_role as membership_role,
      bg_me.fk_identity_id as me_id,
      bgroup.group_id as bg_id
   from o_gp_business bgroup
   inner join o_bs_group_member bg_member on (bg_member.fk_group_id = bgroup.fk_group_id)
   inner join o_bs_group_member bg_me on (bg_me.fk_group_id = bgroup.fk_group_id)
   where
      (bgroup.ownersintern>0 and bg_member.g_role='coach')
      or
      (bgroup.participantsintern>0 and bg_member.g_role='participant')
);

create view o_gp_contactext_v as (
   select
      bg_member.id as membership_id,
      bg_member.fk_identity_id as member_id,
      bg_member.g_role as membership_role,
      id_member.name as member_name,
      first_member.propvalue as member_firstname,
      last_member.propvalue as member_lastname,
      bg_me.fk_identity_id as me_id,
      bgroup.group_id as bg_id,
      bgroup.groupname as bg_name
   from o_gp_business bgroup
   inner join o_bs_group_member bg_member on (bg_member.fk_group_id = bgroup.fk_group_id)
   inner join o_bs_identity id_member on (bg_member.fk_identity_id = id_member.id)
   inner join o_user us_member on (id_member.fk_user_id = us_member.user_id)
   inner join o_userproperty first_member on (first_member.fk_user_id = us_member.user_id and first_member.propname='firstName')
   inner join o_userproperty last_member on (last_member.fk_user_id = us_member.user_id and last_member.propname='lastName')
   inner join o_bs_group_member bg_me on (bg_me.fk_group_id = bgroup.fk_group_id)
   where
      (bgroup.ownersintern>0 and bg_member.g_role='coach')
      or
      (bgroup.participantsintern>0 and bg_member.g_role='participant')
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


-- rating
alter table o_userrating add constraint FKF26C8375236F20X foreign key (creator_id) references o_bs_identity (id);
create index FKF26C8375236F20X on o_userrating (creator_id);
create index userrating_id_idx on o_userrating (resid);
create index userrating_name_idx on o_userrating (resname);
create index userrating_subpath_idx on o_userrating (ressubpath);
create index userrating_rating_idx on o_userrating (rating);

-- comment
alter table o_usercomment add constraint FK92B6864A18251F0 foreign key (parent_key) references o_usercomment (comment_id);
create index FK92B6864A18251F0 on o_usercomment (parent_key);
alter table o_usercomment add constraint FKF26C8375236F20A foreign key (creator_id) references o_bs_identity (id);
create index FKF26C8375236F20A on o_usercomment (creator_id);
create index usercmt_id_idx on o_usercomment (resid);
create index usercmt_name_idx on o_usercomment (resname);
create index usercmt_subpath_idx on o_usercomment (ressubpath);

-- checkpoint
alter table o_checkpoint_results add constraint FK9E30F4B661159ZZY foreign key (checkpoint_fk) references o_checkpoint (checkpoint_id);
create index idx_chres_check_idx on o_checkpoint_results (checkpoint_fk);
alter table o_checkpoint_results add constraint FK9E30F4B661159ZZX foreign key (identity_fk) references o_bs_identity  (id);
create index idx_chres_ident_idx on o_checkpoint_results (identity_fk);

alter table o_checkpoint add constraint FK9E30F4B661159ZZZ foreign key (checklist_fk) references o_checklist (checklist_id);
create index idx_chpt_checklist_fk on o_checkpoint (checklist_fk);

-- plock
create index FK9E30F4B66115906D on oc_lock (identity_fk);
alter table oc_lock  add constraint FK9E30F4B66115906D foreign key (identity_fk) references o_bs_identity (id);

-- property
alter table o_property add constraint FKB60B1BA5190E5 foreign key (grp) references o_gp_business (group_id);
create index FKB60B1BA5190E5 on o_property (grp);
alter table o_property add constraint FKB60B1BA5F7E870BE foreign key (identity) references o_bs_identity (id);
create index FKB60B1BA5F7E870BE on o_property (identity);

create index resid_idx1 on o_property (resourcetypeid);
create index category_idx on o_property (category);
create index name_idx1 on o_property (name);
create index restype_idx1 on o_property (resourcetypename);

-- statistics
create index statdow_resid_idx on o_stat_dayofweek (resid);
create index stathod_resid_idx on o_stat_hourofday (resid);
create index statwee_resid_idx on o_stat_weekly (resid);
create index statday_resid_idx on o_stat_daily (resid);
create index stathor_resid_idx on o_stat_homeorg (resid);
create index statorg_resid_idx on o_stat_orgtype (resid);
create index statstl_resid_idx on o_stat_studylevel (resid);
create index statstb_resid_idx on o_stat_studybranch3 (resid);

-- group
alter table o_bs_group_member add constraint member_identity_ctx foreign key (fk_identity_id) references o_bs_identity (id);
alter table o_bs_group_member add constraint member_group_ctx foreign key (fk_group_id) references o_bs_group (id);
create index member_to_identity_idx on o_bs_group_member (fk_identity_id);
create index member_to_group_idx on o_bs_group_member (fk_group_id);
create index member_to_grp_role_idx on o_bs_group_member (g_role);

alter table o_re_to_group add constraint re_to_group_group_ctx foreign key (fk_group_id) references o_bs_group (id);
alter table o_re_to_group add constraint re_to_group_re_ctx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index re_to_group_group_idx on o_re_to_group (fk_group_id);
create index re_to_group_re_idx on o_re_to_group (fk_entry_id);

alter table o_gp_business add constraint gp_to_group_business_ctx foreign key (fk_group_id) references o_bs_group (id);
-- create index gp_to_group_group_idx on o_gp_business (fk_group_id);

-- business group
alter table o_gp_business add constraint idx_bgp_rsrc foreign key (fk_resource) references o_olatresource (resource_id);
-- index create by unique constraint

create index gp_name_idx on o_gp_business (groupname);
create index idx_grp_lifecycle_soft_idx on o_gp_business (external_id);

alter table o_bs_namedgroup add constraint FKBAFCBBC4B85B522C foreign key (secgroup_id) references o_bs_secgroup (id);
create index FKBAFCBBC4B85B522C on o_bs_namedgroup (secgroup_id);
-- index groupname_idx created by unique cosntraint

-- area
alter table o_gp_bgarea add constraint idx_area_to_resource foreign key (fk_resource) references o_olatresource (resource_id);
create index idx_area_resource on o_gp_bgarea (fk_resource);
create index name_idx6 on o_gp_bgarea (name);

alter table o_gp_bgtoarea_rel add constraint FK9B663F2D1E2E7685 foreign key (group_fk) references o_gp_business (group_id);
create index FK9B663F2D1E2E7685 on o_gp_bgtoarea_rel (group_fk);
alter table o_gp_bgtoarea_rel add constraint FK9B663F2DD381B9B7 foreign key (area_fk) references o_gp_bgarea (area_id);
create index FK9B663F2DD381B9B7 on o_gp_bgtoarea_rel (area_fk);

-- bs
alter table o_bs_authentication add constraint FKC6A5445652595FE6 foreign key (identity_fk) references o_bs_identity (id);
create index FKC6A5445652595FE6 on o_bs_authentication (identity_fk);
create index provider_idx on o_bs_authentication (provider);
create index credential_idx on o_bs_authentication (credential);
create index authusername_idx on o_bs_authentication (authusername);

alter table o_bs_identity add constraint FKFF94111CD1A80C95 foreign key (fk_user_id) references o_user (user_id);
-- index created by unique constraint
create index identstatus_idx on o_bs_identity (status);
create index idx_ident_creationdate_idx on o_bs_identity (creationdate);
create index idx_id_lastlogin_idx on o_bs_identity (lastlogin);

alter table o_bs_policy add constraint FK9A1C5101E2E76DB foreign key (group_id) references o_bs_secgroup (id);
create index FK9A1C5101E2E76DB on o_bs_policy (group_id);
create index idx_policy_grp_rsrc_idx on o_bs_policy (oresource_id, group_id);

alter table o_bs_membership add constraint FK7B6288B45259603C foreign key (identity_id) references o_bs_identity (id);
create index FK7B6288B45259603C on o_bs_membership (identity_id);
alter table o_bs_membership add constraint FK7B6288B4B85B522C foreign key (secgroup_id) references o_bs_secgroup (id);
create index FK7B6288B4B85B522C on o_bs_membership (secgroup_id);

alter table o_bs_invitation add constraint inv_to_group_group_ctx foreign key (fk_group_id) references o_bs_group (id);
create index idx_inv_to_group_group_ctx on o_bs_invitation (fk_group_id);


-- user
create index usr_notification_interval_idx on o_user (notification_interval);

alter table o_userproperty add constraint FK4B04D83FD1A80C95 foreign key (fk_user_id) references o_user (user_id);
create index FK4B04D83FD1A80C95 on o_userproperty (fk_user_id);
create index propvalue_idx on o_userproperty (propvalue);

-- pub sub
create index name_idx2 on o_noti_pub (resname, resid, subident);

alter table o_noti_sub add constraint FK4FB8F04749E53702 foreign key (fk_publisher) references o_noti_pub (publisher_id);
create index FK4FB8F04749E53702 on o_noti_sub (fk_publisher);
alter table o_noti_sub add constraint FK4FB8F0476B1F22F8 foreign key (fk_identity) references o_bs_identity (id);
create index FK4FB8F0476B1F22F8 on o_noti_sub (fk_identity);

-- qti
alter table o_qtiresultset add constraint FK14805D0F5259603C foreign key (identity_id) references o_bs_identity (id);
create index FK14805D0F5259603C on o_qtiresultset (identity_id);

create index oresdetindex on o_qtiresultset (olatresourcedetail);
create index oresindex on o_qtiresultset (olatresource_fk);
create index reprefindex on o_qtiresultset (repositoryref_fk);
create index assindex on o_qtiresultset (assessmentid);

alter table o_qtiresult add constraint FK3563E67340EF401F foreign key (resultset_fk) references o_qtiresultset (resultset_id);
create index FK3563E67340EF401F on o_qtiresult (resultset_fk);
create index itemindex on o_qtiresult (itemident);

-- catalog entry
alter table o_catentry add constraint FKF4433C2C7B66B0D0 foreign key (parent_id) references o_catentry (id);
create index FKF4433C2C7B66B0D0 on o_catentry (parent_id);
alter table o_catentry add constraint FKF4433C2CA1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup (id);
-- index created by unique cosntraint
alter table o_catentry add constraint FKF4433C2CDDD69946 foreign key (fk_repoentry) references o_repositoryentry (repositoryentry_id);
create index FKF4433C2CDDD69946 on o_catentry (fk_repoentry);

-- references
alter table o_references add constraint FKE971B4589AC44FBF foreign key (source_id) references o_olatresource (resource_id);
create index FKE971B4589AC44FBF on o_references (source_id);
alter table o_references add constraint FKE971B458CF634A89 foreign key (target_id) references o_olatresource (resource_id);
create index FKE971B458CF634A89 on o_references (target_id);

-- resources
create index name_idx4 on o_olatresource (resname);
create index id_idx on o_olatresource (resid);

-- repository
alter table o_repositoryentry  add constraint FK2F9C439888C31018 foreign key (fk_olatresource) references o_olatresource (resource_id);
-- index created by unique constraint

create index descritpion_idx on o_repositoryentry (description);
create index access_idx on o_repositoryentry (accesscode);
create index initialAuthor_idx on o_repositoryentry (initialauthor);
create index resource_idx on o_repositoryentry (resourcename);
create index displayname_idx on o_repositoryentry (displayname);
create index idx_re_lifecycle_soft_idx on o_repositoryentry_cycle (r_softkey);
create index idx_re_lifecycle_extid_idx on o_repositoryentry (external_id);
create index idx_re_lifecycle_extref_idx on o_repositoryentry (external_ref);

alter table o_repositoryentry add constraint idx_re_lifecycle_fk foreign key (fk_lifecycle) references o_repositoryentry_cycle(id);
create index idx_re_lifecycle_idx on o_repositoryentry (fk_lifecycle);

alter table o_repositoryentry add constraint repoentry_stats_ctx foreign key (fk_stats) references o_repositoryentry_stats (id);
-- create index repoentry_stats_idx on o_repositoryentry (fk_stats);

-- access control
create index ac_offer_to_resource_idx on o_ac_offer (fk_resource_id);

alter table o_ac_offer_access add constraint off_to_meth_meth_ctx foreign key (fk_method_id) references o_ac_method (method_id);
create index idx_offeracc_method_idx on o_ac_offer_access (fk_method_id);
alter table o_ac_offer_access add constraint off_to_meth_off_ctx foreign key (fk_offer_id) references o_ac_offer (offer_id);
create index idx_offeracc_offer_idx on o_ac_offer_access (fk_offer_id);

create index ac_order_to_delivery_idx on o_ac_order (fk_delivery_id);

alter table o_ac_order_part add constraint ord_part_ord_ctx foreign key (fk_order_id) references o_ac_order (order_id);
create index idx_orderpart_order_idx on o_ac_order_part (fk_order_id);

alter table o_ac_order_line add constraint ord_item_ord_part_ctx foreign key (fk_order_part_id) references o_ac_order_part (order_part_id);
create index idx_orderline_orderpart_idx on o_ac_order_line (fk_order_part_id);
alter table o_ac_order_line add constraint ord_item_offer_ctx foreign key (fk_offer_id) references o_ac_offer (offer_id);
create index idx_orderline_offer_idx on o_ac_order_line (fk_offer_id);

alter table o_ac_transaction add constraint trans_ord_ctx foreign key (fk_order_id) references o_ac_order (order_id);
create index idx_transact_order_idx on o_ac_transaction (fk_order_id);
alter table o_ac_transaction add constraint trans_ord_part_ctx foreign key (fk_order_part_id) references o_ac_order_part (order_part_id);
create index idx_transact_orderpart_idx on o_ac_transaction (fk_order_part_id);
alter table o_ac_transaction add constraint trans_method_ctx foreign key (fk_method_id) references o_ac_method (method_id);
create index idx_transact_method_idx on o_ac_transaction (fk_method_id);

create index paypal_pay_key_idx on o_ac_paypal_transaction (pay_key);
create index paypal_pay_trx_id_idx on o_ac_paypal_transaction (ipn_transaction_id);
create index paypal_pay_s_trx_id_idx on o_ac_paypal_transaction (ipn_sender_transaction_id);

-- reservations
alter table o_ac_reservation add constraint idx_rsrv_to_rsrc_rsrc foreign key (fk_resource) references o_olatresource (resource_id);
create index idx_rsrv_to_rsrc_idx on o_ac_reservation(fk_resource);
alter table o_ac_reservation add constraint idx_rsrv_to_rsrc_identity foreign key (fk_identity) references o_bs_identity (id);
create index idx_rsrv_to_rsrc_id_idx on o_ac_reservation(fk_identity);

-- note
alter table o_note add constraint FKC2D855C263219E27 foreign key (owner_id) references o_bs_identity (id);
create index owner_idx on o_note (owner_id);
create index resid_idx2 on o_note (resourcetypeid);
create index restype_idx2 on o_note (resourcetypename);

-- ex_task
alter table o_ex_task add constraint idx_ex_task_ident_id foreign key (fk_identity_id) references o_bs_identity(id);
create index idx_ex_task_ident_idx on o_ex_task (fk_identity_id);
alter table o_ex_task add constraint idx_ex_task_rsrc_id foreign key (fk_resource_id) references o_olatresource(resource_id);
create index idx_ex_task_rsrc_idx on o_ex_task (fk_resource_id);
alter table o_ex_task_modifier add constraint idx_ex_task_mod_ident_id foreign key (fk_identity_id) references o_bs_identity(id);
create index idx_ex_task_mod_ident_idx on o_ex_task_modifier (fk_identity_id);
alter table o_ex_task_modifier add constraint idx_ex_task_mod_task_id foreign key (fk_task_id) references o_ex_task(id);
create index idx_ex_task_mod_task_idx on o_ex_task_modifier (fk_task_id);

-- checklist
alter table o_cl_check add constraint check_identity_ctx foreign key (fk_identity_id) references o_bs_identity (id);
create index check_to_identity_idx on o_cl_check (fk_identity_id);
alter table o_cl_check add constraint check_box_ctx foreign key (fk_checkbox_id) references o_cl_checkbox (id);
create index check_to_checkbox_idx on o_cl_check (fk_checkbox_id);
alter table o_cl_check add unique (fk_identity_id, fk_checkbox_id);
create index idx_checkbox_uuid_idx on o_cl_checkbox (c_checkboxid);

-- group tasks
alter table o_gta_task add constraint gtask_to_tasklist_idx foreign key (fk_tasklist) references o_gta_task_list (id);
create index idx_gtask_to_tasklist_idx on o_gta_task (fk_tasklist);
alter table o_gta_task add constraint gtask_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_gtask_to_identity_idx on o_gta_task (fk_identity);
alter table o_gta_task add constraint gtask_to_bgroup_idx foreign key (fk_businessgroup) references o_gp_business (group_id);
create index idx_gtask_to_bgroup_idx on o_gta_task (fk_businessgroup);

alter table o_gta_task_list add constraint gta_list_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_gta_list_to_repo_entry_idx on o_gta_task_list (fk_entry);

-- reminders
alter table o_rem_reminder add constraint rem_reminder_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_reminder_to_repo_entry_idx on o_rem_reminder (fk_entry);
alter table o_rem_reminder add constraint rem_reminder_to_creator_idx foreign key (fk_creator) references o_bs_identity (id);
create index idx_reminder_to_creator_idx on o_rem_reminder (fk_creator);

alter table o_rem_sent_reminder add constraint rem_sent_rem_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_sent_rem_to_ident_idx on o_rem_sent_reminder (fk_identity);
alter table o_rem_sent_reminder add constraint rem_sent_rem_to_reminder_idx foreign key (fk_reminder) references o_rem_reminder (id);
create index idx_sent_rem_to_rem_idx on o_rem_sent_reminder (fk_reminder);

-- lifecycle
create index lc_pref_idx on o_lifecycle (persistentref);
create index lc_type_idx on o_lifecycle (persistenttypename);
create index lc_action_idx on o_lifecycle (action);

-- mark
alter table o_mark add constraint FKF26C8375236F21X foreign key (creator_id) references o_bs_identity (id);
create index FKF26C8375236F21X on o_mark (creator_id);

create index mark_id_idx on o_mark(resid);
create index mark_name_idx on o_mark(resname);
create index mark_subpath_idx on o_mark(ressubpath);
create index mark_businesspath_idx on o_mark(businesspath);

-- forum
alter table o_message add constraint FKF26C8375236F20E foreign key (creator_id) references o_bs_identity (id);
create index FKF26C8375236F20E on o_message (creator_id);
alter table o_message add constraint FKF26C837A3FBEB83 foreign key (modifier_id) references o_bs_identity (id);
create index FKF26C837A3FBEB83 on o_message (modifier_id);
alter table o_message add constraint FKF26C8377B66B0D0 foreign key (parent_id) references o_message (message_id);
create index FKF26C8377B66B0D0 on o_message (parent_id);
alter table o_message add constraint FKF26C8378EAC1DBB foreign key (topthread_id) references o_message (message_id);
create index FKF26C8378EAC1DBB on o_message (topthread_id);
alter table o_message add constraint FKF26C8371CB7C4A3 foreign key (forum_fk) references o_forum (forum_id);
create index FKF26C8371CB7C4A3 on o_message (forum_fk);

create index readmessage_forum_idx on o_readmessage (forum_id);
create index readmessage_identity_idx on o_readmessage (identity_id);

-- project broker
create index projectbroker_prj_broker_idx on o_projectbroker_project (projectbroker_fk);
-- index projectbroker_project_id_idx created by unique constraint
create index o_projectbroker_custflds_idx on o_projectbroker_customfields (fk_project_id);

-- info messages
alter table o_info_message add constraint FKF85553465A4FA5DC foreign key (fk_author_id) references o_bs_identity (id);
create index imsg_author_idx on o_info_message (fk_author_id);
alter table o_info_message add constraint FKF85553465A4FA5EF foreign key (fk_modifier_id) references o_bs_identity (id);
create index imsg_modifier_idx on o_info_message (fk_modifier_id);

create index imsg_resid_idx on o_info_message (resid);

-- db course
alter table o_co_db_entry add constraint FKB60B1BA5F7E870XY foreign key (identity) references o_bs_identity;
create index o_co_db_course_ident_idx on o_co_db_entry (identity);

create index o_co_db_course_idx on o_co_db_entry (courseid);
create index o_co_db_cat_idx on o_co_db_entry (category);
create index o_co_db_name_idx on o_co_db_entry (name);

-- open meeting
alter table o_om_room_reference  add constraint idx_omroom_to_bgroup foreign key (businessgroup) references o_gp_business (group_id);
create index idx_omroom_group_idx on o_om_room_reference (businessgroup);
create index idx_omroom_residname on o_om_room_reference (resourcetypename,resourcetypeid);

-- eportfolio
alter table o_ep_artefact add constraint FKF26C8375236F28X foreign key (fk_artefact_auth_id) references o_bs_identity (id);
create index idx_artfeact_to_auth_idx on o_ep_artefact (fk_artefact_auth_id);
alter table o_ep_artefact add constraint FKA0070D12316A97B4 foreign key (fk_struct_el_id) references o_ep_struct_el (structure_id);
create index idx_artfeact_to_struct_idx on o_ep_artefact (fk_struct_el_id);

alter table o_ep_struct_el add constraint FKF26C8375236F26X foreign key (fk_olatresource) references o_olatresource (resource_id);
create index idx_structel_to_rsrc_idx on o_ep_struct_el (fk_olatresource);
alter table o_ep_struct_el add constraint FK4ECC1C8D636191A1 foreign key (fk_map_source_id) references o_ep_struct_el (structure_id);
create index idx_structel_to_map_idx on o_ep_struct_el (fk_map_source_id);
alter table o_ep_struct_el add constraint FK4ECC1C8D76990817 foreign key (fk_struct_root_id) references o_ep_struct_el (structure_id);
create index idx_structel_to_root_idx on o_ep_struct_el (fk_struct_root_id);
alter table o_ep_struct_el add constraint FK4ECC1C8D76990818 foreign key (fk_struct_root_map_id) references o_ep_struct_el (structure_id);
create index idx_structel_to_rootmap_idx on o_ep_struct_el (fk_struct_root_map_id);

alter table o_ep_collect_restriction add constraint FKA0070D12316A97B5 foreign key (fk_struct_el_id) references o_ep_struct_el (structure_id);
-- index idx_collectrest_to_structel_idx created by unique constraint

alter table o_ep_struct_struct_link add constraint FKF26C8375236F22X foreign key (fk_struct_parent_id) references o_ep_struct_el (structure_id);
create index idx_structlink_to_parent_idx on o_ep_struct_struct_link (fk_struct_parent_id);
alter table o_ep_struct_struct_link add constraint FKF26C8375236F23X foreign key (fk_struct_child_id) references o_ep_struct_el (structure_id);
create index idx_structlink_to_child_idx on o_ep_struct_struct_link (fk_struct_child_id);

alter table o_ep_struct_artefact_link add constraint FKF26C8375236F24X foreign key (fk_struct_id) references o_ep_struct_el (structure_id);
create index idx_structart_to_struct_idx on o_ep_struct_artefact_link (fk_struct_id);
alter table o_ep_struct_artefact_link add constraint FKF26C8375236F25X foreign key (fk_artefact_id) references o_ep_artefact (artefact_id);
create index idx_structart_to_art_idx on o_ep_struct_artefact_link (fk_artefact_id);
alter table o_ep_struct_artefact_link add constraint FKF26C8375236F26Y foreign key (fk_auth_id) references o_bs_identity (id);
create index idx_structart_to_auth_idx on o_ep_struct_artefact_link (fk_auth_id);

alter table o_ep_struct_to_group add constraint struct_to_group_group_ctx foreign key (fk_group_id) references o_bs_group (id);
alter table o_ep_struct_to_group add constraint struct_to_group_re_ctx foreign key (fk_struct_id) references o_ep_struct_el (structure_id);

-- tag
alter table o_tag add constraint FK6491FCA5A4FA5DC foreign key (fk_author_id) references o_bs_identity (id);
create index idx_tag_to_auth_idx on o_tag (fk_author_id);

-- mail
alter table o_mail add constraint FKF86663165A4FA5DC foreign key (fk_from_id) references o_mail_recipient (recipient_id);
create index idx_mail_from_idx on o_mail (fk_from_id);

alter table o_mail_recipient add constraint FKF86663165A4FA5DG foreign key (fk_recipient_id) references o_bs_identity (id);
create index idx_mailrec_rcp_idx on o_mail_recipient (fk_recipient_id);
create index idx_mail_meta_id_idx on o_mail (meta_mail_id);

alter table o_mail_to_recipient add constraint FKF86663165A4FA5DE foreign key (fk_mail_id) references o_mail (mail_id);
create index idx_mailtorec_mail_idx on o_mail_to_recipient (fk_mail_id);
alter table o_mail_to_recipient add constraint FKF86663165A4FA5DD foreign key (fk_recipient_id) references o_mail_recipient (recipient_id);
create index idx_mailtorec_rcp_idx on o_mail_to_recipient (fk_recipient_id);

alter table o_mail_attachment add constraint FKF86663165A4FA5DF foreign key (fk_att_mail_id) references o_mail (mail_id);
create index idx_mail_att_mail_idx on o_mail_attachment (fk_att_mail_id);
create index idx_mail_att_checksum_idx on o_mail_attachment (datas_checksum);
create index idx_mail_path_idx on o_mail_attachment (datas_path);
create index idx_mail_att_siblings_idx on o_mail_attachment (datas_checksum, mimetype, datas_size, datas_name);

-- instant messaging
alter table o_im_message add constraint idx_im_msg_to_fromid foreign key (fk_from_identity_id) references o_bs_identity (id);
create index idx_im_msg_from_idx on o_im_message(fk_from_identity_id);
create index idx_im_msg_res_idx on o_im_message (msg_resid,msg_resname);

alter table o_im_notification add constraint idx_im_not_to_toid foreign key (fk_to_identity_id) references o_bs_identity (id);
create index idx_im_chat_to_idx on o_im_notification (fk_to_identity_id);
alter table o_im_notification add constraint idx_im_not_to_fromid foreign key (fk_from_identity_id) references o_bs_identity (id);
create index idx_im_chat_from_idx on o_im_notification (fk_from_identity_id);
create index idx_im_chat_res_idx on o_im_notification (chat_resid,chat_resname);

alter table o_im_roster_entry add constraint idx_im_rost_to_id foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_im_rost_ident_idx on o_im_roster_entry (fk_identity_id);
create index idx_im_rost_res_idx on o_im_roster_entry (r_resid,r_resname);

alter table o_im_preferences add constraint idx_im_prfs_to_id foreign key (fk_from_identity_id) references o_bs_identity (id);
create index idx_im_prefs_ident_idx on o_im_preferences (fk_from_identity_id);

-- efficiency statements
alter table o_as_eff_statement add constraint eff_statement_id_cstr foreign key (fk_identity) references o_bs_identity (id);
create index idx_eff_statement_ident_idx on o_as_eff_statement (fk_identity);
create index eff_statement_repo_key_idx on o_as_eff_statement (course_repo_key);

-- course infos
alter table o_as_user_course_infos add constraint user_course_infos_id_cstr foreign key (fk_identity) references o_bs_identity (id);
create index idx_ucourseinfos_ident_idx on o_as_user_course_infos (fk_identity);
alter table o_as_user_course_infos add constraint user_course_infos_res_cstr foreign key (fk_resource_id) references o_olatresource (resource_id);
create index idx_ucourseinfos_rsrc_idx on o_as_user_course_infos (fk_resource_id);

-- mapper
create index o_mapper_uuid_idx on o_mapper (mapper_uuid);

-- qti 2.1
alter table o_qti_assessment_session add constraint qti_sess_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_testess_to_repo_entry_idx on o_qti_assessment_session (fk_entry);
alter table o_qti_assessment_session add constraint qti_sess_to_course_entry_idx foreign key (fk_course) references o_repositoryentry (repositoryentry_id);
create index idx_qti_sess_to_course_entry_idx on o_qti_assessment_session (fk_course);
alter table o_qti_assessment_session add constraint qti_sess_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_qti_sess_to_identity_idx on o_qti_assessment_session (fk_identity);

-- question pool
alter table o_qp_pool add constraint idx_qp_pool_owner_grp_id foreign key (fk_ownergroup) references o_bs_secgroup(id);
create index idx_pool_ownergrp_idx on o_qp_pool (fk_ownergroup);

alter table o_qp_pool_2_item add constraint idx_qp_pool_2_item_pool_id foreign key (fk_pool_id) references o_qp_pool(id);
alter table o_qp_pool_2_item add constraint idx_qp_pool_2_item_item_id foreign key (fk_item_id) references o_qp_item(id);
alter table o_qp_pool_2_item add unique (fk_pool_id, fk_item_id);
create index idx_poolitem_pool_idx on o_qp_pool_2_item (fk_pool_id);
create index idx_poolitem_item_idx on o_qp_pool_2_item (fk_item_id);

alter table o_qp_share_item add constraint idx_qp_share_rsrc_id foreign key (fk_resource_id) references o_olatresource(resource_id);
alter table o_qp_share_item add constraint idx_qp_share_item_id foreign key (fk_item_id) references o_qp_item(id);
alter table o_qp_share_item add unique (fk_resource_id, fk_item_id);
create index idx_shareitem_pool_idx on o_qp_share_item (fk_resource_id);
create index idx_shareitem_item_idx on o_qp_share_item (fk_item_id);

alter table o_qp_item_collection add constraint idx_qp_coll_owner_id foreign key (fk_owner_id) references o_bs_identity(id);
create index idx_itemcoll_owner_idx on o_qp_item_collection (fk_owner_id);

alter table o_qp_collection_2_item add constraint idx_qp_coll_coll_id foreign key (fk_collection_id) references o_qp_item_collection(id);
alter table o_qp_collection_2_item add constraint idx_qp_coll_item_id foreign key (fk_item_id) references o_qp_item(id);
alter table o_qp_collection_2_item add unique (fk_collection_id, fk_item_id);
create index idx_coll2item_coll_idx on o_qp_collection_2_item (fk_collection_id);
create index idx_coll2item_item_idx on o_qp_collection_2_item (fk_item_id);

alter table o_qp_item add constraint idx_qp_pool_2_field_id foreign key (fk_taxonomy_level) references o_qp_taxonomy_level(id);
create index idx_item_taxon_idx on o_qp_item (fk_taxonomy_level);
alter table o_qp_item add constraint idx_qp_item_owner_id foreign key (fk_ownergroup) references o_bs_secgroup(id);
create index idx_item_ownergrp_idx on o_qp_item (fk_ownergroup);
alter table o_qp_item add constraint idx_qp_item_edu_ctxt_id foreign key (fk_edu_context) references o_qp_edu_context(id);
create index idx_item_eductxt_idx on o_qp_item (fk_edu_context);
alter table o_qp_item add constraint idx_qp_item_type_id foreign key (fk_type) references o_qp_item_type(id);
create index idx_item_type_idx on o_qp_item (fk_type);
alter table o_qp_item add constraint idx_qp_item_license_id foreign key (fk_license) references o_qp_license(id);
create index idx_item_license_idx on o_qp_item (fk_license);

alter table o_qp_taxonomy_level add constraint idx_qp_field_2_parent_id foreign key (fk_parent_field) references o_qp_taxonomy_level(id);
create index idx_taxon_parent_idx on o_qp_taxonomy_level (fk_parent_field);
create index idx_taxon_mat_pathon on o_qp_taxonomy_level (q_mat_path_ids);

alter table o_qp_item_type add constraint cst_unique_item_type unique (q_type);

-- lti outcome
alter table o_lti_outcome add constraint idx_lti_outcome_ident_id foreign key (fk_identity_id) references o_bs_identity(id);
create index idx_lti_outcome_ident_id_idx on o_lti_outcome (fk_identity_id);
alter table o_lti_outcome add constraint idx_lti_outcome_rsrc_id foreign key (fk_resource_id) references o_olatresource(resource_id);
create index idx_lti_outcome_rsrc_id_idx on o_lti_outcome (fk_resource_id);

-- assessment mode
alter table o_as_mode_course add constraint as_mode_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_as_mode_to_repo_entry_idx on o_as_mode_course (fk_entry);

alter table o_as_mode_course_to_group add constraint as_modetogroup_group_idx foreign key (fk_group_id) references o_gp_business (group_id);
alter table o_as_mode_course_to_group add constraint as_modetogroup_mode_idx foreign key (fk_assessment_mode_id) references o_as_mode_course (id);
create index idx_as_modetogroup_group_idx on o_as_mode_course_to_group (fk_group_id);
create index idx_as_modetogroup_mode_idx on o_as_mode_course_to_group (fk_assessment_mode_id);

alter table o_as_mode_course_to_area add constraint as_modetoarea_area_idx foreign key (fk_area_id) references o_gp_bgarea (area_id);
alter table o_as_mode_course_to_area add constraint as_modetoarea_mode_idx foreign key (fk_assessment_mode_id) references o_as_mode_course (id);
create index idx_as_modetoarea_area_idx on o_as_mode_course_to_area (fk_area_id);
create index idx_as_modetoarea_mode_idx on o_as_mode_course_to_area (fk_assessment_mode_id);

-- certificates
alter table o_cer_certificate add constraint cer_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index cer_identity_idx on o_cer_certificate (fk_identity);
alter table o_cer_certificate add constraint cer_to_resource_idx foreign key (fk_olatresource) references o_olatresource (resource_id);
create index cer_resource_idx on o_cer_certificate (fk_olatresource);
create index cer_archived_resource_idx on o_cer_certificate (c_archived_resource_id);
create index cer_uuid_idx on o_cer_certificate (c_uuid);

-- o_logging_table
create index log_target_resid_idx on o_loggingtable(targetresid);
create index log_ptarget_resid_idx on o_loggingtable(parentresid);
create index log_gptarget_resid_idx on o_loggingtable(grandparentresid);
create index log_ggptarget_resid_idx on o_loggingtable(greatgrandparentresid);
create index log_creationdate_idx on o_loggingtable(creationdate);


insert into o_stat_lastupdated (until_datetime, lastupdated) values (to_date('1999-01-01', 'YYYY-mm-dd'), to_date('1999-01-01', 'YYYY-mm-dd'));
insert into hibernate_unique_key values ( 0 );
commit
/
