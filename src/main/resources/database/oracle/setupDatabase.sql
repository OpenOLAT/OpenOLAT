-- tables
CREATE TABLE o_forum (
  forum_id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  f_refresname varchar(50),
  f_refresid number(20),
  PRIMARY KEY (forum_id)
);
CREATE TABLE o_forum_pseudonym (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   p_pseudonym varchar2(255 char) NOT NULL,
   p_credential varchar2(255 char) NOT NULL,
   p_salt varchar2(255 char) NOT NULL,
   p_hashalgorithm varchar2(16 char) NOT NULL,
   PRIMARY KEY (id)
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
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_role varchar2(24 char) not null,
   g_inheritance_mode varchar2(16 char) default 'none' not null,
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
  email varchar2(2000 char) NOT NULL,
  regkey varchar2(255 char) NOT NULL,
  ip varchar2(255 char) NOT NULL,
  valid_until date,
  mailsent number NOT NULL,
  action varchar2(255 char) NOT NULL,
  fk_identity_id number(20),
  PRIMARY KEY (reglist_id)
);


CREATE TABLE o_bs_authentication (
  id number(20) NOT NULL,
  version number(20) NOT NULL,
  creationdate date,
  lastmodified date NOT NULL,
  identity_fk number(20) NOT NULL,
  provider varchar2(8 char),
  authusername varchar2(255 char),
  credential varchar2(255 char),
  salt varchar2(255 char),
  hashalgorithm varchar2(16 char),
  PRIMARY KEY (id),
  CONSTRAINT u_o_bs_authentication UNIQUE (provider, authusername)
);


CREATE TABLE o_bs_authentication_history (
   id number(20) generated always as identity,
   creationdate date,
   provider varchar(8),
   authusername varchar(255),
   credential varchar(255),
   salt varchar(255) default null,
   hashalgorithm varchar(16) default null,
   fk_identity number(20) not null,
   primary key (id)
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
  deleteddate date,
  deletedroles varchar(1024),
  deletedby varchar(128),
  inactivationdate date,
  inactivationemaildate date,
  reactivationdate date,
  deletionemaildate date,
  CONSTRAINT u_o_bs_identity UNIQUE (name),
  PRIMARY KEY (id)
);

CREATE TABLE o_bs_relation_role (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_role varchar(128) not null,
   g_external_id varchar(128),
   g_external_ref varchar(128),
   g_managed_flags varchar(256),
   PRIMARY KEY (id)
);

CREATE TABLE o_bs_relation_right (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   g_right varchar(128) not null,
   PRIMARY KEY (id)
);

CREATE TABLE o_bs_relation_role_to_right (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   fk_role_id number(20),
   fk_right_id number(20) not null,
   PRIMARY KEY (id)
);

CREATE TABLE o_bs_identity_to_identity (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   g_external_id varchar(128),
   g_managed_flags varchar(256),
   fk_source_id number(20) not null,
   fk_target_id number(20) not null,
   fk_role_id number(20) not null,
   PRIMARY KEY (id)
);

CREATE TABLE o_csp_log (
  id number(20) generated always as identity,
  creationdate date,
  l_blocked_uri varchar(1024),
  l_disposition varchar(32),
  l_document_uri varchar(1024),
  l_effective_directive CLOB,
  l_original_policy CLOB,
  l_referrer varchar(1024),
  l_script_sample CLOB,
  l_status_code varchar(1024),
  l_violated_directive varchar(1024),
  l_source_file varchar(1024),
  l_line_number number(20),
  l_column_number number(20),
  fk_identity number(20),
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
  order_index number(20),
  short_title varchar2(255 char),
  add_entry_position number default null,
  add_category_position number default null,
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
   u_firstname varchar2(255 char),
   u_lastname varchar2(255 char),
   u_email varchar2(255 char),
   u_nickname varchar2(255 char),
   u_birthday varchar2(255 char),
   u_graduation varchar2(255 char),
   u_gender varchar2(255 char),
   u_telprivate varchar2(255 char),
   u_telmobile varchar2(255 char),
   u_teloffice varchar2(255 char),
   u_smstelmobile varchar2(255 char),
   u_skype varchar2(255 char),
   u_msn varchar2(255 char),
   u_xing varchar2(255 char),
   u_linkedin varchar2(255 char),
   u_icq varchar2(255 char),
   u_homepage varchar2(255 char),
   u_street varchar2(255 char),
   u_extendedaddress varchar2(255 char),
   u_pobox varchar2(255 char),
   u_zipcode varchar2(255 char),
   u_region varchar2(255 char),
   u_city varchar2(255 char),
   u_country varchar2(255 char),
   u_countrycode varchar2(255 char),
   u_institutionalname varchar2(255 char),
   u_institutionaluseridentifier varchar2(255 char),
   u_institutionalemail varchar2(255 char),
   u_orgunit varchar2(255 char),
   u_studysubject varchar2(255 char),
   u_emchangekey varchar2(255 char),
   u_emaildisabled varchar2(255 char),
   u_typeofuser varchar2(255 char),
   u_socialsecuritynumber varchar2(255 char),

   u_rank varchar2(255 char),
   u_degree varchar2(255 char),
   u_position varchar2(255 char),
   u_userinterests varchar2(255 char),
   u_usersearchedinterests varchar2(255 char),
   u_officestreet varchar2(255 char),
   u_extendedofficeaddress varchar2(255 char),
   u_officepobox varchar2(255 char),
   u_officezipcode varchar2(255 char),
   u_officecity varchar2(255 char),
   u_officecountry varchar2(255 char),
   u_officemobilephone varchar2(255 char),
   u_department varchar2(255 char),
   u_privateemail varchar2(255 char),
   u_employeenumber varchar2(255 char),
   u_organizationalunit varchar2(255 char),

   u_edupersonaffiliation varchar2(255 char),
   u_swissedupersonstaffcategory varchar2(255 char),
   u_swissedupersonhomeorg varchar2(255 char),
   u_swissedupersonstudylevel varchar2(255 char),
   u_swissedupersonhomeorgtype varchar2(255 char),
   u_swissedupersonstudybranch1 varchar2(255 char),
   u_swissedupersonstudybranch2 varchar2(255 char),
   u_swissedupersonstudybranch3 varchar2(255 char),

   u_genericselectionproperty varchar2(255 char),
   u_genericselectionproperty2 varchar2(255 char),
   u_genericselectionproperty3 varchar2(255 char),
   u_generictextproperty varchar2(255 char),
   u_generictextproperty2 varchar2(255 char),
   u_generictextproperty3 varchar2(255 char),
   u_generictextproperty4 varchar2(255 char),
   u_generictextproperty5 varchar2(255 char),
   u_genericuniquetextproperty varchar2(255 char),
   u_genericuniquetextproperty2 varchar2(255 char),
   u_genericuniquetextproperty3 varchar2(255 char),
   u_genericemailproperty1 varchar2(255 char),
   u_genericcheckboxproperty varchar2(255 char),
   u_genericcheckboxproperty2 varchar2(255 char),
   u_genericcheckboxproperty3 varchar2(255 char),

   fk_identity number(20),
   PRIMARY KEY (user_id)
);


CREATE TABLE o_userproperty (
  fk_user_id number(20) NOT NULL,
  propname varchar2(255 char) NOT NULL,
  propvalue varchar2(255 char),
  PRIMARY KEY (fk_user_id, propname)
);

CREATE TABLE o_user_data_export (
  id number(20) generated always as identity,
   creationdate date,
   lastmodified date,
   u_directory varchar(255),
   u_status varchar(16),
   u_export_ids varchar(2000),
   fk_identity number(20) not null,
   fk_request_by number(20),
   PRIMARY KEY (id)
);

CREATE TABLE o_user_absence_leave (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   u_absent_from timestamp,
   u_absent_to timestamp,
   u_resname varchar(50),
   u_resid number(20),
   u_sub_ident varchar(2048),
   fk_identity number(20) not null,
   primary key (id)
);

CREATE TABLE o_message (
  message_id number(20) NOT NULL,
  version number(20) NOT NULL,
  lastmodified date,
  creationdate date,
  title varchar2(100 char),
  body CLOB,
  pseudonym varchar2(255 char),
  guest number default 0 not null,
  parent_id number(20),
  topthread_id number(20),
  creator_id number(20),
  modifier_id number(20),
  modification_date date,
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
  subenabled number default 1,
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
  external_ref varchar2(255 char),
  managed_flags varchar2(255 char),
  displayname varchar2(110 char) NOT NULL,
  resourcename varchar2(100 char) NOT NULL,
  authors varchar2(2048 char),
  mainlanguage varchar2(255 char),
  location varchar2(255 char),
  objectives varchar(32000),
  requirements varchar(32000),
  credits varchar(32000),
  expenditureofwork varchar(32000),
  fk_stats number(20) not null,
  fk_lifecycle number(20),
  fk_olatresource number(20),
  description varchar2(4000),
  initialauthor varchar2(128 char) NOT NULL,
  status varchar(16) default 'preparation' not null,
  allusers number default 0 not null,
  guests number default 0 not null,
  bookable number default 0 not null,
  allowtoleave varchar2(16 char),
  candownload number NOT NULL,
  cancopy number NOT NULL,
  canreference number NOT NULL,
  deletiondate date default null,
  fk_deleted_by number(20) default null,
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
CREATE TABLE o_re_to_tax_level (
  id number(20) generated always as identity,
  creationdate date not null,
  fk_entry number(20) not null,
  fk_taxonomy_level number(20) not null,
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
  windowid varchar2(32) default null,
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
  attachmentpath varchar(1024),
  resname varchar(50 char) NOT NULL,
  resid number(20) NOT NULL,
  ressubpath varchar2(2048 char),
  businesspath varchar2(2048 char),
  fk_author_id number(20),
  fk_modifier_id number(20),
  PRIMARY KEY (info_id)
);

create table o_bs_invitation (
   id number(20) not null,
   creationdate date,
   token varchar(64 char) not null,
   first_name varchar(64 char),
   last_name varchar(64 char),
   mail varchar(128 char),
   fk_group_id number(20),
   fk_identity_id number(20),
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
  confirmation_email number default 0,
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

-- paypal checkout
create table o_ac_checkout_transaction (
  id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   p_success_uuid varchar(64) not null,
   p_cancel_uuid varchar(64) not null,
   p_order_nr varchar(64) not null,
   p_order_id NUMBER(20) not null,
   p_order_part_id number(20) not null,
   p_method_id number(20) not null,
   p_amount_currency_code varchar(3) not null,
   p_amount_amount DECIMAL not null,
   p_status varchar(32) not null,
   p_paypal_order_id varchar(64),
   p_paypal_order_status varchar(64),
   p_paypal_order_status_reason CLOB,
   p_paypal_authorization_id varchar(64),
   p_paypal_capture_id varchar(64),
   p_capture_currency_code varchar(3),
   p_capture_amount DECIMAL,
   p_paypal_invoice_id varchar(64),
   primary key (id)
);


create table o_ac_auto_advance_order (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  a_identifier_key varchar(64) not null,
  a_identifier_value varchar(64) not null,
  a_status varchar(32) not null,
  a_status_modified date not null,
  fk_identity number(20) not null,
  fk_method number(20) not null,
  primary key (id)
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

create table o_aconnect_meeting (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_sco_id varchar(128) default null,
   a_folder_id varchar(128) default null,
   a_env_name varchar(128) default null,
   a_name varchar(128) not null,
   a_description varchar(2000) default null,
   a_permanent number default 0 not null,
   a_start_date timestamp default null,
   a_leadtime number(20) default 0 not null,
   a_start_with_leadtime timestamp,  
   a_end_date timestamp default null,
   a_followuptime number(20) default 0 not null,
   a_end_with_followuptime timestamp,
   a_opened number default 0 not null,
   a_template_id varchar(32) default null,
   a_shared_documents varchar(2000) default null,
   fk_entry_id number(20) default null,
   a_sub_ident varchar(64) default null,
   fk_group_id number(20) default null,
   primary key (id)
);

create table o_aconnect_user (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_principal_id varchar(128) default null,
   a_env_name varchar(128) default null,
   fk_identity_id number(20) default null,
   primary key (id)
);

-- BigBlueButton
create table o_bbb_template (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   b_name varchar(128) not null,
   b_description varchar(2000) default null,
   b_system number default 0 not null,
   b_enabled number default 1 not null,
   b_external_id varchar(255) default null,
   b_external_users number default 1 not null,
   b_max_concurrent_meetings int default null,
   b_max_participants int default null,
   b_max_duration number default null,
   b_record number default null,
   b_breakout number default null,
   b_mute_on_start number default null,
   b_auto_start_recording number default null,
   b_allow_start_stop_recording number default null,
   b_webcams_only_for_moderator number default null,
   b_allow_mods_to_unmute_users number default null,
   b_lock_set_disable_cam number default null,
   b_lock_set_disable_mic number default null,
   b_lock_set_disable_priv_chat number default null,
   b_lock_set_disable_public_chat number default null,
   b_lock_set_disable_note number default null,
   b_lock_set_locked_layout number default null,
   b_lock_set_hide_user_list number default null,
   b_lock_set_lock_on_join number default null,
   b_lock_set_lock_on_join_conf number default null,
   b_permitted_roles varchar(255) default null,
   b_guest_policy varchar(32) default null,
   primary key (id)
);

create table o_bbb_server (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   b_name varchar(128),
   b_url varchar(255) not null,
   b_shared_secret varchar(255),
   b_recording_url varchar(255),
   b_enabled number default 1 not null,
   b_capacity_factor decimal,
   primary key (id)
);

create table o_bbb_meeting (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   b_meeting_id varchar(128) not null,
   b_attendee_pw varchar(128) not null,
   b_moderator_pw varchar(128) not null,
   b_name varchar(128) not null,
   b_description varchar(2000) default null,
   b_welcome CLOB,
   b_layout varchar2(16) default 'standard',
   b_permanent number default 0 not null,
   b_guest number default 0 not null,
   b_identifier varchar2(64),
   b_read_identifier varchar2(64),
   b_start_date timestamp default null,
   b_leadtime number(20) default 0 not null,
   b_start_with_leadtime timestamp,
   b_end_date timestamp default null,
   b_followuptime number(20) default 0 not null,
   b_end_with_followuptime timestamp,
   b_main_presenter varchar2(255),
   b_recordings_publishing varchar2(16) default 'auto',
   b_record number default null,
   fk_creator_id number(20),
   fk_entry_id number(20) default null,
   a_sub_ident varchar(64) default null,
   fk_group_id number(20) default null,
   fk_template_id number(20) default null,
   fk_server_id number(20),
   primary key (id)
);

create table o_bbb_attendee (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   b_role varchar2(32),
   b_join_date date,
   b_pseudo varchar2(255),
   fk_identity_id number(20),
   fk_meeting_id number(20) not null,
   primary key (id)
);

create table o_bbb_recording (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   b_recording_id varchar(255) not null,
   b_publish_to varchar(128),
   b_start_date date default null,
   b_end_date date default null,
   b_url varchar(1024),
   b_type varchar(32),
   fk_meeting_id number(20) not null,
   unique(b_recording_id,fk_meeting_id),
   primary key (id)
);

-- Teams
create table o_teams_meeting (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   t_subject varchar(255),
   t_description varchar(4000),
   t_main_presenter varchar(255),
   t_start_date timestamp default null,
   t_leadtime number(20) default 0 not null,
   t_start_with_leadtime timestamp,
   t_end_date timestamp default null,
   t_followuptime number(20) default 0 not null,
   t_end_with_followuptime timestamp,
   t_permanent number default 0,
   t_join_information varchar(4000),
   t_guest number default 0 not null,
   t_identifier varchar(64),
   t_read_identifier varchar(64),
   t_online_meeting_id varchar(128),
   t_online_meeting_join_url varchar(2000),
   t_allowed_presenters varchar(32) default 'EVERYONE',
   t_access_level varchar(32) default 'EVERYONE',
   t_entry_exit_announcement number default 1,
   t_lobby_bypass_scope varchar(32) default 'ORGANIZATION_AND_FEDERATED',
   fk_entry_id number(20) default null,
   a_sub_ident varchar(64) default null,
   fk_group_id number(20) default null,
   fk_creator_id number(20) default null,
   primary key (id)
);


create table o_as_eff_statement (
   id number(20) not null,
   version number(20) not null,
   lastmodified date,
   lastcoachmodified date,
   lastusermodified date,
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

create table o_as_entry (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   lastcoachmodified date,
   lastusermodified date,
   a_attemtps number(20) default null,
   a_last_attempt date null,
   a_score decimal default null,
   a_passed number default null,
   a_passed_original number,
   a_passed_mod_date date,
   a_status varchar2(16 char) default null,
   a_date_done date,
   a_details varchar2(1024 char) default null,
   a_user_visibility number default 1,
   a_fully_assessed number default null,
   a_date_fully_assessed date,
   a_assessment_id number(20) default null,
   a_completion float,
   a_current_run_completion decimal,
   a_current_run_status varchar2(16 char),
   a_current_run_start timestamp,
   a_comment clob,
   a_coach_comment clob,
   a_num_assessment_docs number(20) default 0 not null,
   a_date_start date,
   a_date_end date,
   a_date_end_original date,
   a_date_end_mod_date date,
   a_duration number(20),
   a_obligation varchar2(50),
   a_obligation_original varchar2(50),
   a_obligation_mod_date date,
   a_first_visit date,
   a_last_visit date,
   a_num_visits number(20),
   fk_entry number(20) not null,
   a_subident varchar2(512 char),
   a_entry_root number default null,
   fk_reference_entry number(20),
   fk_identity number(20) default null,
   fk_identity_passed_mod number(20),
   fk_identity_end_date_mod number(20),
   fk_identity_obligation_mod number(20),
   a_anon_identifier varchar2(128 char) default null,
   primary key (id),
   unique(fk_identity, fk_entry, a_subident)
);

create table o_as_compensation (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_subident varchar(512),
   a_subident_name varchar(512),
   a_extra_time number(20) not null,
   a_approved_by varchar(2000),
   a_approval timestamp,
   a_status varchar(32),
   fk_identity number(20) not null,
   fk_creator number(20) not null,
   fk_entry number(20) not null,
   primary key (id)
);

create table o_as_compensation_log (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   a_action varchar(32) not null,
   a_val_before CLOB,
   a_val_after CLOB,
   a_subident varchar(512),
   fk_entry_id number(20) not null,
   fk_identity_id number(20) not null,
   fk_compensation_id number(20) not null,
   fk_author_id number(20),
   primary key (id)
);

create table o_as_mode_course (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   a_name varchar2(255 char),
   a_description clob,
   a_status varchar2(16 char),
   a_end_status varchar(32),
   a_manual_beginend number default 0 not null,
   a_begin date not null,
   a_leadtime number(20) default 0 not null,
   a_begin_with_leadtime date not null,
   a_end date not null,
   a_followuptime number(20) default 0 not null,
   a_end_with_followuptime date not null,
   a_targetaudience varchar2(16 char),
   a_restrictaccesselements number default 0 not null,
   a_elements varchar2(32000 char),
   a_start_element varchar2(64 char),
   a_restrictaccessips number default 0 not null,
   a_ips varchar2(32000 char),
   a_safeexambrowser number default 0 not null,
   a_safeexambrowserkey varchar2(32000 char),
   a_safeexambrowserhint clob,
   a_applysettingscoach number default 0 not null,
   fk_entry number(20) not null,
   fk_lecture_block number(20),
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

create table o_as_mode_course_to_cur_el (
   id number(20) generated always as identity,
   fk_assessment_mode_id number(20) not null,
   fk_cur_element_id number(20) not null,
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
   c_next_recertification date,
   c_path varchar2(1024 char),
   c_last number default 1 not null,
   c_course_title varchar2(255 char),
   c_archived_resource_id number(20) not null,
   fk_olatresource number(20),
   fk_identity number(20) not null,
   primary key (id)
);

-- gotomeeting
create table o_goto_organizer (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   g_name varchar2(128 char) default null,
   g_account_key varchar2(128 char) default null,
   g_access_token varchar2(4000) not null,
   g_renew_date date not null,
   g_refresh_token varchar(4000),
   g_renew_refresh_date date,
   g_organizer_key varchar2(128 char) not null,
   g_username varchar2(128 char) not null,
   g_firstname varchar2(128 char) default null,
   g_lastname varchar2(128 char) default null,
   g_email varchar2(128 char) default null,
   fk_identity number(20) default null,
   primary key (id)
);

create table o_goto_meeting (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   g_external_id varchar2(128 char) default null,
   g_type varchar2(16 char) not null,
   g_meeting_key varchar2(128 char) not null,
   g_name varchar2(255 char) default null,
   g_description varchar2(2000 char) default null,
   g_start_date date default null,
   g_end_date date default null,
   fk_organizer_id number(20) not null,
   fk_entry_id number(20) default null,
   g_sub_ident varchar2(64 char) default null,
   fk_group_id number(20) default null,
   primary key (id)
);

create table o_goto_registrant (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   g_status varchar2(16 char) default null,
   g_join_url varchar2(1024 char) default null,
   g_confirm_url varchar2(1024 char) default null,
   g_registrant_key varchar2(64 char) default null,
   fk_meeting_id number(20) not null,
   fk_identity_id number(20) not null,
   primary key (id)
);

create table o_vid_transcoding (
   id number(20) not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   vid_resolution number(20) default null,
   vid_width number(20) default null,
   vid_height number(20) default null,
   vid_size number(20) default null,
   vid_format varchar(128) default null,
   vid_status number(20) default null,
   vid_transcoder varchar(128) default null,
   fk_resource_id number(20) not null,
   primary key (id)
);

create table o_vid_metadata (
  id number(20) GENERATED ALWAYS AS IDENTITY,
  creationdate date not null,
  lastmodified date not null,
  vid_width number(20) default null,
  vid_height number(20) default null,
  vid_size number(20) default null,
  vid_format varchar2(32 char) default null,
  vid_length varchar2(32 char) default null,
  vid_url varchar2(512 char) default null,
  fk_resource_id number(20) not null,
  primary key (id)
);

-- calendar
create table o_cal_use_config (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   c_calendar_id varchar2(128 char) not null,
   c_calendar_type varchar2(16 char) not null,
   c_token varchar2(36 char),
   c_cssclass varchar2(36 char),
   c_visible number default 1 not null,
   c_aggregated_feed number default 1 not null,
   fk_identity number(20) not null,
   primary key (id),
   unique (c_calendar_id, c_calendar_type, fk_identity)
);

create table o_cal_import (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   c_calendar_id varchar2(128 char) not null,
   c_calendar_type varchar2(16 char) not null,
   c_displayname varchar2(256 char),
   c_lastupdate date not null,
   c_url varchar2(1024 char),
   fk_identity number(20),
   primary key (id)
);

create table o_cal_import_to (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   c_to_calendar_id varchar2(128 char) not null,
   c_to_calendar_type varchar2(16 char) not null,
   c_lastupdate date not null,
   c_url varchar2(1024 char),
   primary key (id)
);

-- instant messaging
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

create table o_qti_assessmenttest_session (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   q_exploded number default 0 not null,
   q_cancelled number default 0 not null,
   q_author_mode number default 0 not null,
   q_finish_time date,
   q_termination_time date,
   q_duration number(20),
   q_score decimal default null,
   q_manual_score decimal default null,
   q_max_score decimal default null,
   q_passed number default null,
   q_num_questions number(20),
   q_num_answered_questions number(20),
   q_extra_time number(20),
   q_compensation_extra_time number(20),
   q_storage varchar2(1024 char),
   fk_reference_entry number(20) not null,
   fk_entry number(20),
   q_subident varchar2(255 char),
   fk_identity number(20) default null,
   q_anon_identifier varchar2(128 char) default null,
   fk_assessment_entry number(20) not null,
   primary key (id)
);

create table o_qti_assessmentitem_session (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   q_itemidentifier varchar2(255 char) not null,
   q_sectionidentifier varchar2(255 char) default null,
   q_testpartidentifier varchar2(255 char) default null,
   q_duration number(20),
   q_score decimal default null,
   q_manual_score decimal default null,
   q_coach_comment CLOB,
   q_to_review  number default 0,
   q_passed number default null,
   q_storage varchar2(1024 char),
   fk_assessmenttest_session number(20) not null,
   primary key (id)
);

create table o_qti_assessment_response (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   q_responseidentifier varchar2(255 char) not null,
   q_responsedatatype varchar2(16 char) not null,
   q_responselegality varchar2(16 char) not null,
   q_stringuifiedresponse clob,
   fk_assessmentitem_session number(20) not null,
   fk_assessmenttest_session number(20) not null,
   primary key (id)
);

create table o_qti_assessment_marks (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   q_marks clob default null,
   q_hidden_rubrics clob default null,
   fk_reference_entry number(20) not null,
   fk_entry number(20),
   q_subident varchar2(64 char),
   fk_identity number(20) not null,
   primary key (id)
);

-- vfs
create table o_vfs_metadata (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   f_uuid varchar(64) not null,
   f_deleted number default 0 not null,
   f_filename varchar(256) not null,
   f_relative_path varchar(2048) not null,
   f_directory number default 0,
   f_lastmodified timestamp not null,
   f_size number(20) default 0,
   f_uri varchar(2000) not null,
   f_uri_protocol varchar(16) not null,
   f_cannot_thumbnails number default 0,
   f_download_count number(20) default 0,
   f_comment varchar(32000),
   f_title varchar(2000),
   f_publisher varchar(2000),
   f_creator varchar(2000),
   f_source varchar(2000),
   f_city varchar(256),
   f_pages varchar(2000),
   f_language varchar(16),
   f_url varchar(2000),
   f_pub_month varchar(16),
   f_pub_year varchar(16),
   f_license_type_name varchar(256),
   f_license_text CLOB,
   f_licensor varchar(4000),
   f_locked_date timestamp,
   f_locked number default 0,
   f_revision_nr number(20) default 0 not null,
   f_revision_comment varchar(32000),
   f_migrated varchar(12),
   f_m_path_keys varchar(1024),
   fk_locked_identity number(20),
   fk_license_type number(20),
   fk_author number(20),
   fk_lastmodified_by number(20),
   fk_parent number(20),
   primary key (id)
);

create table o_vfs_thumbnail (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   f_size number(20) default 0 not null,
   f_max_width number(20) default 0 not null,
   f_max_height number(20) default 0 not null,
   f_final_width number(20) default 0 not null,
   f_final_height number(20) default 0 not null,
   f_fill number default 0 not null,
   f_filename varchar(256) not null,
   fk_metadata number(20) not null,
   primary key (id)
);

create table o_vfs_revision (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   f_revision_size number(20) default 0 not null,
   f_revision_nr number(20) default 0 not null,
   f_revision_filename varchar(256) not null,
   f_revision_comment varchar(32000),
   f_revision_lastmodified timestamp not null,
   f_comment varchar(32000),
   f_title varchar(2000),
   f_publisher varchar(2000),
   f_creator varchar(2000),
   f_source varchar(2000),
   f_city varchar(256),
   f_pages varchar(2000),
   f_language varchar(16),
   f_url varchar(2048),
   f_pub_month varchar(16),
   f_pub_year varchar(16),
   f_license_type_name varchar(256),
   f_license_text CLOB,
   f_licensor varchar(4000),
   fk_license_type number(20),
   fk_author number(20),
   fk_metadata number(20) not null,
   primary key (id)
);

-- Document editor
create table o_de_access (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   o_editor_type varchar(64) not null,
   o_expires_at timestamp not null,
   o_mode varchar(64) not null,
   o_version_controlled number default 0 not null,
   o_download number default 0,
   fk_metadata number(20) not null,
   fk_identity number(20) not null,
   primary key (id)
);

-- used in fxOpenOlat
create table o_de_user_info (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   o_info varchar(2048) not null,
   fk_identity number(20) not null,
   primary key (id)
);

-- portfolio
create table o_pf_binder (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   p_title varchar2(255 char),
   p_status varchar2(32 char),
   p_copy_date date,
   p_return_date date,
   p_deadline date,
   p_summary CLOB,
   p_image_path varchar2(255 char),
   fk_olatresource_id number(20),
   fk_group_id number(20) not null,
   fk_entry_id number(20),
   p_subident varchar2(128 char),
   fk_template_id number(20),
   primary key (id)
);

create table o_pf_section (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   pos number(20) default null,
   p_title varchar2(255 char),
   p_description varchar2(4000 char),
   p_status varchar2(32 char) default 'notStarted' not null,
   p_begin date,
   p_end date,
   p_override_begin_end number default 0,
   fk_group_id number(20) not null,
   fk_binder_id number(20) not null,
   fk_template_reference_id number(20),
   primary key (id)
);

create table o_pf_page (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   pos number(20) default null,
   p_editable number default 1,
   p_title varchar2(255 char),
   p_summary varchar2(4000 char),
   p_status varchar2(32 char),
   p_image_path varchar2(255 char),
   p_image_align varchar2(32 char),
   p_version number(20) default 0,
   p_initial_publish_date date,
   p_last_publish_date date,
   fk_body_id number(20) not null,
   fk_group_id number(20) not null,
   fk_section_id number(20),
   primary key (id)
);

create table o_pf_media (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   p_collection_date date not null,
   p_type varchar2(64 char) not null,
   p_storage_path varchar2(255 char),
   p_root_filename varchar2(255 char),
   p_title varchar(255) not null,
   p_description varchar2(4000 char),
   p_content CLOB,
   p_signature number(20) default 0 not null,
   p_reference_id varchar2(255 char) default null,
   p_business_path varchar2(255 char) default null,
   p_creators varchar2(1024 char) default null,
   p_place varchar2(255 char) default null,
   p_publisher varchar2(255 char) default null,
   p_publication_date date default null,
   p_date varchar2(32 char) default null,
   p_url varchar2(1024 char) default null,
   p_source varchar2(1024 char) default null,
   p_language varchar2(32 char) default null,
   p_metadata_xml varchar2(4000 char),
   fk_author_id number(20) not null,
   primary key (id)
);

create table o_pf_page_body (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   primary key (id)
);

create table o_pf_page_part (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   pos number(20) default null,
   dtype varchar2(32 char),
   p_content CLOB,
   p_flow varchar2(32 char),
   p_layout_options CLOB,
   fk_media_id number(20),
   fk_page_body_id number(20),
   fk_form_entry_id number(20) default null,
   primary key (id)
);

create table o_pf_category (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   p_name varchar2(32 char),
   primary key (id)
);

create table o_pf_category_relation (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   p_resname varchar2(64 char) not null,
   p_resid number(20) not null,
   fk_category_id number(20) not null,
   primary key (id)
);

create table o_pf_assessment_section (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   p_score decimal default null,
   p_passed number default null,
   p_comment CLOB,
   fk_section_id number(20) not null,
   fk_identity_id number(20) not null,
   primary key (id)
);

create table o_pf_assignment (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   pos number(20) default null,
   p_status varchar2(32 char) default null,
   p_type varchar2(32 char) not null,
   p_version number(20) default 0 not null,
   p_template number default 0,
   p_title varchar2(255 char) default null,
   p_summary CLOB,
   p_content CLOB,
   p_storage varchar2(255 char) default null,
   fk_section_id number(20),
   fk_binder_id number(20),
   fk_template_reference_id number(20),
   fk_page_id number(20),
   fk_assignee_id number(20),
   p_only_auto_eva number default 1,
   p_reviewer_see_auto_eva number default 0,
   p_anon_extern_eva number default 1,
   fk_form_entry_id number(20) default null,
   primary key (id)
);

create table o_pf_binder_user_infos (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   p_initiallaunchdate date,
   p_recentlaunchdate date,
   p_visit number(20),
   fk_identity number(20),
   fk_binder number(20),
   unique(fk_identity, fk_binder),
   primary key (id)
);

create table o_pf_page_user_infos (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  p_mark number default 0,
  p_status varchar2(16 char) default 'incoming' not null,
  p_recentlaunchdate date not null,
  fk_identity_id number(20) not null,
  fk_page_id number(20) not null,
  primary key (id)
);

-- evaluation forms
create table o_eva_form_survey (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   e_resname varchar2(50) not null,
   e_resid number(20) not null,
   e_sub_ident varchar2(2048),
   e_sub_ident2 varchar2(2048),
   e_series_key number(20),
   e_series_index number(20),
   fk_form_entry number(20) not null,
   fk_series_previous number(20),
   primary key (id)
);

create table o_eva_form_participation (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   e_identifier_type varchar2(50) not null,
   e_identifier_key varchar2(50) not null,
   e_status varchar2(20) not null,
   e_anonymous number default 0 not null,
   fk_executor number(20),
   fk_survey number(20) not null,
   primary key (id)
);

create table o_eva_form_session (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   e_status varchar2(16 char),
   e_submission_date date,
   e_first_submission_date date,
   e_email varchar2(1024),
   e_firstname varchar2(1024),
   e_lastname varchar2(1024),
   e_age varchar2(1024),
   e_gender varchar2(1024),
   e_org_unit varchar2(1024),
   e_study_subject varchar2(1024),
   fk_survey number(20),
   fk_participation number(20) unique,
   fk_identity number(20),
   fk_page_body number(20),
   fk_form_entry number(20),
   primary key (id)
);

create table o_eva_form_response (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   e_no_response number default 0,
   e_responseidentifier varchar2(64 char) not null,
   e_numericalresponse decimal default null,
   e_stringuifiedresponse clob,
   e_file_response_path varchar2(4000 char),
   fk_session number(20) not null,
   primary key (id)
);

-- quality management
create table o_qual_data_collection (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   q_status varchar2(50),
   q_title varchar2(200),
   q_start date,
   q_deadline date,
   q_topic_type varchar2(50),
   q_topic_custom varchar2(200),
   q_topic_fk_identity number(20),
   q_topic_fk_organisation number(20),
   q_topic_fk_curriculum number(20),
   q_topic_fk_curriculum_element number(20),
   q_topic_fk_repository number(20),
   fk_generator number(20),
   q_generator_provider_key number(20),
   primary key (id)
);

create table o_qual_data_collection_to_org (
   id number(20) generated always as identity,
   creationdate date not null,
   fk_data_collection number(20) not null,
   fk_organisation number(20) not null,
   primary key (id)
);

create table o_qual_context (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   q_role varchar2(20),
   q_location varchar2(1024),
   fk_data_collection number(20) not null,
   fk_eva_participation number(20),
   fk_eva_session number(20),
   fk_audience_repository number(20),
   fk_audience_cur_element number(20),
   primary key (id)
);

create table o_qual_context_to_organisation (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   fk_context number(20) not null,
   fk_organisation number(20) not null,
   primary key (id)
);

create table o_qual_context_to_curriculum (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   fk_context number(20) not null,
   fk_curriculum number(20) not null,
   primary key (id)
);

create table o_qual_context_to_cur_element (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   fk_context number(20) not null,
   fk_cur_element number(20) not null,
   primary key (id)
);

create table o_qual_context_to_tax_level (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   fk_context number(20) not null,
   fk_tax_leveL number(20) not null,
   primary key (id)
);

create table o_qual_reminder (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   q_type varchar2(65),
   q_send_planed date,
   q_send_done date,
   fk_data_collection number(20) not null,
   primary key (id)
);

create table o_qual_report_access (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  q_type varchar2(64),
  q_role varchar2(64),
  q_online number default 0,
  q_email_trigger varchar2(64),
  fk_data_collection number(20),
  fk_generator number(20),
  fk_group number(20),
  primary key (id)
);

create table o_qual_generator (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   q_title varchar2(256),
   q_type varchar2(64) not null,
   q_enabled number not null,
   q_last_run date,
   fk_form_entry number(20),
   primary key (id)
);

create table o_qual_generator_config (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   q_identifier varchar2(50) not null,
   q_value clob,
   fk_generator number(20) not null,
   primary key (id)
);

create table o_qual_generator_to_org (
   id number(20) generated always as identity,
   creationdate date not null,
   fk_generator number(20) not null,
   fk_organisation number(20) not null,
   primary key (id)
);

create table o_qual_analysis_presentation (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   q_name varchar2(256),
   q_analysis_segment varchar2(100),
   q_search_params CLOB,
   q_heatmap_grouping CLOB,
   q_heatmap_insufficient_only number default 0,
   q_temporal_grouping varchar(50),
   q_trend_difference varchar(50),
   q_rubric_id varchar(50),
   fk_form_entry number(20) not null,
   primary key (id)
);

-- question pool
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
   q_topic varchar2(1024 char),
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
   q_correction_time number(20) default null,
   q_assessment_type varchar2(64 char),
   q_status varchar2(32 char) not null,
   q_version varchar2(50 char),
   fk_license number(20),
   q_editor varchar2(256 char),
   q_editor_version varchar2(256 char),
   q_format varchar2(32 char) not null,
   q_creator varchar2(1024 char),
   creationdate date not null,
   lastmodified date not null,
   q_status_last_modified date not null,
   q_dir varchar2(32 char),
   q_root_filename varchar2(255 char),
   fk_taxonomy_level number(20),
   fk_taxonomy_level_v2 number(20),
   fk_ownergroup number(20) not null,
   primary key (id)
);

create table o_qp_item_audit_log (
  id number(20) generated always as identity,
  creationdate date not null,
  q_action varchar2(64 char),
  q_val_before CLOB,
  q_val_after CLOB,
  q_lic_before CLOB,
  q_lic_after CLOB,
  q_message CLOB,
  fk_author_id number(20),
  fk_item_id number(20),
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
   fk_entry number(20) not null,
   primary key (id)
);

create table o_gta_task (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   g_status varchar2(36 char),
   g_rev_loop number(20) default 0 not null,
   g_taskname varchar2(1024 char),
   g_assignment_date date,
   g_submission_date date,
   g_submission_ndocs number(20),
   g_submission_revisions_date date,
   g_submission_revisions_ndocs number(20),
   g_collection_date date,
   g_collection_ndocs number(20),
   g_acceptation_date date,
   g_solution_date date,
   g_graduation_date date,
   g_allow_reset_date date,
   g_assignment_due_date date,
   g_submission_due_date date,
   g_revisions_due_date date,
   g_solution_due_date date,
   fk_tasklist number(20) not null,
   fk_identity number(20),
   fk_businessgroup number(20),
   fk_allow_reset_identity number(20),
   primary key (id)
);

create table o_gta_task_revision (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_status varchar(36) not null,
   g_rev_loop number(20) default 0 not null,
   g_date timestamp,
   g_rev_comment CLOB,
   g_rev_comment_lastmodified timestamp,
   fk_task number(20) not null,
   fk_comment_author number(20),
   primary key (id)
);

create table o_gta_task_revision_date (
  id number(20) not null,
  creationdate date not null,
  g_status varchar(36) not null,
  g_rev_loop number(20) not null,
  g_date date not null,
  fk_task number(20) not null,
  primary key (id)
);

create table o_gta_mark (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  fk_tasklist_id number(20) not null,
  fk_marker_identity_id number(20) not null,
  fk_participant_identity_id number(20) not null,
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
   r_email_subject varchar(255),
   r_email_body clob,
   fk_creator number(20) not null,
   fk_entry number(20) not null,
   primary key (id)
);

create table o_rem_sent_reminder (
   id number(20) not null,
   creationdate timestamp not null,
   r_status varchar(16),
   fk_identity number(20) not null,
   fk_reminder number(20) not null,
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

-- sms
create table o_sms_message_log (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   s_message_uuid varchar2(256 char) not null,
   s_server_response varchar2(256 char),
   s_service_id varchar2(32 char) not null,
   fk_identity number(20) not null,
   primary key (id)
);

-- webfeed
create table o_feed (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   f_resourceable_id number(20),
   f_resourceable_type varchar(64),
   f_title varchar(1024),
   f_description clob,
   f_author varchar(255),
   f_image_name varchar(255),
   f_external number(2) default 0,
   f_external_feed_url varchar(1024),
   f_external_image_url varchar(1024),
   primary key (id)
);

create table o_feed_item (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   f_title varchar(1024),
   f_description clob,
   f_content clob,
   f_author varchar(255),
   f_guid varchar(255),
   f_external_link varchar(1024),
   f_draft number(2) default 0,
   f_publish_date date,
   f_width number(20),
   f_height number(20),
   f_filename varchar(1024),
   f_type varchar(255),
   f_length number(20),
   f_external_url varchar(1024),
   fk_feed_id number(20),
   fk_identity_author_id number(20),
   fk_identity_modified_id number(20),
   primary key (id)
);

-- lectures
create table o_lecture_reason (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  l_enabled number default 1 not null,
  l_title varchar2(255 char),
  l_descr varchar2(2000 char),
  primary key (id)
);

create table o_lecture_absence_category (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_enabled number default 1 not null,
   l_title varchar(255),
   l_descr CLOB,
   primary key (id)
);

create table o_lecture_absence_notice (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_type varchar(32),
   l_absence_reason CLOB,
   l_absence_authorized number default null,
   l_start_date timestamp not null,
   l_end_date timestamp not null,
   l_target varchar(32) default 'allentries' not null,
   l_attachments_dir varchar(255),
   fk_identity number(20) not null,
   fk_notifier number(20),
   fk_authorizer number(20),
   fk_absence_category number(20),
   primary key (id)
);

create table o_lecture_block (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  l_external_id varchar2(255 char),
  l_managed_flags varchar2(255 char),
  l_title varchar2(255 char),
  l_descr clob,
  l_preparation clob,
  l_location varchar2(255 char),
  l_comment clob,
  l_start_date date not null,
  l_end_date date not null,
  l_compulsory number default 1 not null,
  l_eff_end_date date,
  l_planned_lectures_num number(20) default 0 not null,
  l_effective_lectures_num number(20) default 0 not null,
  l_effective_lectures varchar2(128 char),
  l_auto_close_date date default null,
  l_status varchar2(16 char) not null,
  l_roll_call_status varchar2(16 char) not null,
  fk_reason number(20),
  fk_entry number(20) not null,
  fk_teacher_group number(20) not null,
  primary key (id)
);

create table o_lecture_block_to_group (
  id number(20) generated always as identity,
  fk_lecture_block number(20) not null,
  fk_group number(20) not null,
  primary key (id)
);

create table o_lecture_notice_to_block (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   fk_lecture_block number(20) not null,
   fk_absence_notice number(20) not null,
   primary key (id)
);

create table o_lecture_notice_to_entry (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   fk_entry number(20) not null,
   fk_absence_notice number(20) not null,
   primary key (id)
);

create table o_lecture_block_roll_call (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  l_comment clob,
  l_lectures_attended varchar2(128 char),
  l_lectures_absent varchar2(128 char),
  l_lectures_attended_num number(20) default 0 not null,
  l_lectures_absent_num number(20) default 0 not null,
  l_absence_notice_lectures varchar(128),
  l_absence_reason clob,
  l_absence_authorized number default null,
  l_absence_appeal_date date,
  l_absence_supervisor_noti_date date,
  l_appeal_reason CLOB,
  l_appeal_status CLOB,
  l_appeal_status_reason CLOB,
  fk_lecture_block number(20) not null,
  fk_identity number(20) not null,
  fk_absence_category number(20),
  fk_absence_notice number(20),
  primary key (id)
);

create table o_lecture_reminder (
  id number(20) generated always as identity,
  creationdate date not null,
  l_status varchar2(16 char) not null,
  fk_lecture_block number(20) not null,
  fk_identity number(20) not null,
  primary key (id)
);

create table o_lecture_participant_summary (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  l_required_attendance_rate float(24) default null,
  l_first_admission_date date default null,
  l_attended_lectures number(20) default 0 not null,
  l_absent_lectures number(20) default 0 not null,
  l_excused_lectures number(20) default 0 not null,
  l_planneds_lectures number(20) default 0 not null,
  l_attendance_rate float(24) default null,
  l_cal_sync number default 0 not null,
  l_cal_last_sync_date date default null,
  fk_entry number(20) not null,
  fk_identity number(20) not null,
  primary key (id),
  unique (fk_entry, fk_identity)
);

create table o_lecture_entry_config (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  l_lecture_enabled number default null,
  l_override_module_def number default 0 not null,
  l_rollcall_enabled number default null,
  l_calculate_attendance_rate number default null,
  l_required_attendance_rate float(24) default null,
  l_sync_calendar_teacher number default null,
  l_sync_calendar_participant number default null,
  l_sync_calendar_course number default null,
  l_assessment_mode number default null,
  l_assessment_mode_lead number(20) default null,
  l_assessment_mode_followup number(20) default null,
  l_assessment_mode_ips varchar(2048),
  l_assessment_mode_seb varchar(2048),
  fk_entry number(20) not null,
  unique(fk_entry),
  primary key (id)
);

create table o_lecture_block_audit_log (
  id number(20) generated always as identity,
  creationdate date not null,
  l_action varchar2(32 char),
  l_val_before CLOB,
  l_val_after CLOB,
  l_message CLOB,
  fk_lecture_block number(20),
  fk_roll_call number(20),
  fk_absence_notice number(20),
  fk_entry number(20),
  fk_identity number(20),
  fk_author number(20),
  primary key (id)
);

create table o_lecture_block_to_tax_level (
  id number(20) generated always as identity,
  creationdate date not null,
  fk_lecture_block number(20) not null,
  fk_taxonomy_level number(20) not null,
  primary key (id)
);

-- taxonomy
create table o_tax_taxonomy (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  t_identifier varchar2(64 char),
  t_displayname varchar2(255 char) not null,
  t_description CLOB,
  t_external_id varchar2(64 char),
  t_managed_flags varchar2(255 char),
  t_directory_path varchar2(255 char),
  t_directory_lost_found_path varchar2(255 char),
  fk_group number(20) not null,
  primary key (id)
);

create table o_tax_taxonomy_level_type (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  t_identifier varchar2(64 char),
  t_displayname varchar2(255 char) not null,
  t_description CLOB,
  t_external_id varchar2(64 char),
  t_managed_flags varchar2(255 char),
  t_css_class varchar2(64 char),
  t_visible number default 1,
  t_library_docs number default 1,
  t_library_manage number default 1,
  t_library_teach_read number default 1,
  t_library_teach_readlevels number(20) default 0 not null,
  t_library_teach_write number default 0,
  t_library_have_read number default 1,
  t_library_target_read number default 1,
  fk_taxonomy number(20) not null,
  primary key (id)
);

create table o_tax_taxonomy_type_to_type (
  id number(20) generated always as identity,
  fk_type number(20) not null,
  fk_allowed_sub_type number(20) not null,
  primary key (id)
);

create table o_tax_taxonomy_level (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  t_identifier varchar2(64 char),
  t_displayname varchar2(255 char) not null,
  t_description CLOB,
  t_external_id varchar2(64 char),
  t_sort_order number(20),
  t_directory_path varchar2(255 char),
  t_m_path_keys varchar2(255 char),
  t_m_path_identifiers varchar2(1024 char),
  t_enabled number default 1,
  t_managed_flags varchar2(255 char),
  fk_taxonomy number(20) not null,
  fk_parent number(20),
  fk_type number(20),
  primary key (id)
);

create table o_tax_taxonomy_competence (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  t_type varchar2(16),
  t_achievement decimal default null,
  t_reliability decimal default null,
  t_expiration_date date,
  t_external_id varchar2(64 char),
  t_source_text varchar2(255 char),
  t_source_url varchar2(255 char),
  fk_level number(20) not null,
  fk_identity number(20) not null,
  primary key (id)
);

create table o_tax_competence_audit_log (
  id number(20) generated always as identity,
  creationdate date not null,
  t_action varchar2(32 char),
  t_val_before CLOB,
  t_val_after CLOB,
  t_message CLOB,
  fk_taxonomy number(20),
  fk_taxonomy_competence number(20),
  fk_identity number(20),
  fk_author number(20),
  primary key (id)
);

-- dialog elements
create table o_dialog_element (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  d_filename varchar2(2048 char),
  d_filesize number(20),
  d_subident varchar2(64 char) not null,
  fk_author number(20),
  fk_entry number(20) not null,
  fk_forum number(20) not null,
  primary key (id)
);

-- licenses
create table o_lic_license_type (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  l_name varchar2(128) not null unique,
  l_text CLOB,
  l_css_class varchar2(64),
  l_predefined number not null,
  l_sort_order number(20) not null,
  primary key (id)
);

create table o_lic_license_type_activation (
  id number(20) generated always as identity,
  creationdate date not null,
  l_handler_type varchar2(128) not null,
  fk_license_type_id number(20) not null,
  primary key (id)
);

create table o_lic_license (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  l_resname varchar2(50) not null,
  l_resid number(20)  not null,
  l_licensor varchar2(4000),
  l_freetext CLOB,
  fk_license_type_id number(20) not null,
  primary key (id)
);

-- organisation
create table o_org_organisation_type (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  o_identifier varchar(64),
  o_displayname varchar(255) not null,
  o_description CLOB,
  o_external_id varchar(64),
  o_managed_flags varchar(255),
  o_css_class varchar(64),
  primary key (id)
);

create table o_org_organisation (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  o_identifier varchar(64),
  o_displayname varchar(255) not null,
  o_description CLOB,
  o_m_path_keys varchar(255),
  o_external_id varchar(64),
  o_managed_flags varchar(255),
  o_status varchar(32),
  o_css_class varchar(64),
  fk_group number(20) not null,
  fk_root number(20),
  fk_parent number(20),
  fk_type number(20),
  primary key (id)
);

create table o_org_type_to_type (
  id number(20) generated always as identity,
  fk_type number(20) not null,
  fk_allowed_sub_type number(20) not null,
  primary key (id)
);

create table o_re_to_organisation (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  r_master number default 0,
  fk_entry number(20) not null,
  fk_organisation number(20) not null,
  primary key (id)
);

-- curriculum
create table o_cur_element_type (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description CLOB,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  c_calendars varchar(16),
  c_lectures varchar(16),
  c_learning_progress varchar(16),
  c_css_class varchar(64),
  primary key (id)
);

create table o_cur_curriculum (
   id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description CLOB,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  c_status varchar(32),
  c_degree varchar(255),
  fk_group number(20) not null,
  fk_organisation number(20),
  primary key (id)
);

create table o_cur_curriculum_element (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  pos number(20),
  pos_cur number(20),
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description CLOB,
  c_status varchar(32),
  c_begin date,
  c_end date,
  c_external_id varchar(64),
  c_m_path_keys varchar(255),
  c_managed_flags varchar(255),
  c_calendars varchar(16),
  c_lectures varchar(16),
  c_learning_progress varchar(16),
  fk_group number(20) not null,
  fk_parent number(20),
  fk_curriculum number(20) not null,
  fk_curriculum_parent number(20),
  fk_type number(20),
  primary key (id)
);

create table o_cur_element_type_to_type (
  id number(20) generated always as identity,
  fk_type number(20) not null,
  fk_allowed_sub_type number(20) not null,
  primary key (id)
);

create table o_cur_element_to_tax_level (
  id number(20) generated always as identity,
  creationdate date not null,
  fk_cur_element number(20) not null,
  fk_taxonomy_level number(20) not null,
  primary key (id)
);

-- edu-sharing
create table o_es_usage (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   e_identifier varchar2(64) not null,
   e_resname varchar2(50) not null,
   e_resid number(20) not null,
   e_sub_path varchar(256),
   e_object_url varchar2(255) not null,
   e_version varchar2(64),
   e_mime_type varchar2(128),
   e_media_type varchar2(128),
   e_width varchar2(8),
   e_height varchar2(8),
   fk_identity number(20) not null,
   primary key (id)
);

-- livestream
create table o_livestream_launch (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   l_launch_date timestamp not null,
   fk_entry  number(20) not null,
   l_subident varchar(128) not null,
   fk_identity  number(20) not null,
   primary key (id)
);

-- grading
create table o_grad_to_identity (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_status varchar(16) default 'activated' not null,
   fk_identity number(20) not null,
   fk_entry number(20) not null,
   primary key (id)
);

create table o_grad_assignment (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_status varchar(16) default 'unassigned' not null,
   g_assessment_date timestamp,
   g_assignment_date timestamp,
   g_assignment_notification timestamp,
   g_reminder_1 timestamp,
   g_reminder_2 timestamp,
   g_deadline timestamp,
   g_extended_deadline timestamp,
   g_closed timestamp,
   fk_reference_entry number(20) not null,
   fk_assessment_entry number(20) not null,
   fk_grader number(20),
   primary key (id)
);

create table o_grad_time_record (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_time number(20) default 0 not null,
   g_metadata_time number(20) default 0 not null,
   g_date_record date not null,
   fk_assignment number(20),
   fk_grader number(20) not null,
   primary key (id)
);

create table o_grad_configuration (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_grading_enabled number default 0 not null,
   g_identity_visibility varchar(32) default 'anonymous' not null,
   g_grading_period number(20),
   g_notification_type varchar(32) default 'afterTestSubmission' not null,
   g_notification_subject varchar(255),
   g_notification_body CLOB,
   g_first_reminder number(20),
   g_first_reminder_subject varchar(255),
   g_first_reminder_body CLOB,
   g_second_reminder number(20),
   g_second_reminder_subject varchar(255),
   g_second_reminder_body CLOB,
   fk_entry number(20) not null,
   primary key (id)
);

-- course disclaimer
create table o_course_disclaimer_consent(
    id number(20) generated always as identity,
    disc_1_accepted number not null,
    disc_2_accepted number not null,
    creationdate timestamp not null,
    lastmodified timestamp not null,
    fk_repository_entry number(20) not null,
    fk_identity number(20) not null,
    primary key (id)
);

-- Appointments
create table o_ap_topic (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   a_title varchar2(256),
   a_description varchar2(4000),
   a_type varchar2(64) not null,
   a_multi_participation number default 1 not null,
   a_auto_confirmation number default 0 not null,
   a_participation_visible number default 1 not null,
   fk_entry_id number(20) not null,
   fk_group_id number(20),
   a_sub_ident varchar2(64) not null,
   primary key (id)
);

create table o_ap_organizer (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   fk_topic_id number(20) not null,
   fk_identity_id number(20) not null,
   primary key (id)
);

create table o_ap_topic_to_group (
   id number(20) generated always as identity,
   creationdate date not null,
   fk_topic_id number(20) not null,
   fk_group_id number(20),
   primary key (id)
);

create table o_ap_appointment (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   a_status varchar2(64) not null,
   a_status_mod_date date,
   a_start date,
   a_end date,
   a_location varchar2(256),
   a_details varchar2(4000),
   a_max_participations number(20),
   fk_topic_id number(20) not null,
   fk_meeting_id number(20),
   primary key (id)
);

create table o_ap_participation (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   fk_appointment_id number(20) not null,
   fk_identity_id number(20) not null,
   fk_identity_created_by number(20) not null,
   primary key (id)
);

-- Organiation role rights
create table o_org_role_to_right (
   id number(20) generated always as identity,
   creationdate date not null,
   o_role varchar(255) not null,
   o_right varchar(255) not null,
   fk_organisation number(20) not null,
   primary key (id)
);

-- Contact tracing
create table o_ct_location (
   id number(20) generated  always as identity,
   creationdate date not null,
   lastmodified date not null,
   l_reference varchar2(255),
   l_titel varchar2(255),
   l_room varchar2(255),
   l_building varchar2(255),
   l_sector varchar2(255),
   l_table varchar2(255),
   l_seat_number number default 0 not null,
   l_qr_id varchar2(255) not null,
   l_qr_text varchar2(4000),
   l_guests number default 1 not null,
   l_printed number default 0 not null,
   unique(l_qr_id),
   primary key (id)
);

create table o_ct_registration (
   id number(20) generated  always as identity,
   creationdate date not null,
   l_deletion_date date not null,
   l_start_date date not null,
   l_end_date date,
   l_nick_name varchar2(255),
   l_first_name varchar2(255),
   l_last_name varchar2(255),
   l_street varchar2(255),
   l_extra_line varchar2(255),
   l_zip_code varchar2(255),
   l_city varchar2(255),
   l_email varchar2(255),
   l_institutional_email varchar2(255),
   l_generic_email varchar2(255),
   l_private_phone varchar2(255),
   l_mobile_phone varchar2(255),
   l_office_phone varchar2(255),
   l_seat_number varchar2(64),
   fk_location number(20) not null,
   primary key (id)
);


-- user view
create view o_bs_identity_short_v as (
   select
      ident.id as id_id,
      ident.name as id_name,
      ident.external_id as id_external,
      ident.lastlogin as id_lastlogin,
      ident.status as id_status,
      us.user_id as us_id,
      us.u_firstname as first_name,
      us.u_lastname as last_name,
      us.u_nickname as nick_name,
      us.u_email as email
   from o_bs_identity ident
   inner join o_user us on (ident.id = us.fk_identity)
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
      us_member.u_firstname as member_firstname,
      us_member.u_lastname as member_lastname,
      bg_me.fk_identity_id as me_id,
      bgroup.group_id as bg_id,
      bgroup.groupname as bg_name
   from o_gp_business bgroup
   inner join o_bs_group_member bg_member on (bg_member.fk_group_id = bgroup.fk_group_id)
   inner join o_bs_identity id_member on (bg_member.fk_identity_id = id_member.id)
   inner join o_user us_member on (id_member.id = us_member.fk_identity)
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



-- rating
alter table o_userrating add constraint FKF26C8375236F20X foreign key (creator_id) references o_bs_identity (id);
create index FKF26C8375236F20X on o_userrating (creator_id);
create index userrating_id_idx on o_userrating (resid);
create index userrating_name_idx on o_userrating (resname);
create index userrating_subpath_idx on o_userrating (substr(ressubpath,0,255));
create index userrating_rating_idx on o_userrating (rating);
create index userrating_rating_res_idx on o_userrating (resid, resname, creator_id, rating);

-- comment
alter table o_usercomment add constraint FK92B6864A18251F0 foreign key (parent_key) references o_usercomment (comment_id);
create index FK92B6864A18251F0 on o_usercomment (parent_key);
alter table o_usercomment add constraint FKF26C8375236F20A foreign key (creator_id) references o_bs_identity (id);
create index FKF26C8375236F20A on o_usercomment (creator_id);
create index usercmt_id_idx on o_usercomment (resid);
create index usercmt_name_idx on o_usercomment (resname);
create index usercmt_subpath_idx on o_usercomment (substr(ressubpath,0,255));

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

-- group
alter table o_bs_group_member add constraint member_identity_ctx foreign key (fk_identity_id) references o_bs_identity (id);
alter table o_bs_group_member add constraint member_group_ctx foreign key (fk_group_id) references o_bs_group (id);
create index member_to_identity_idx on o_bs_group_member (fk_identity_id);
create index member_to_group_idx on o_bs_group_member (fk_group_id);
create index group_role_member_idx on o_bs_group_member (fk_group_id,g_role,fk_identity_id);

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

alter table o_bs_authentication_history add constraint auth_hist_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_auth_hist_to_ident_idx on o_bs_authentication_history (fk_identity);

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
alter table o_bs_invitation add constraint invit_to_id_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_invit_to_id_idx on o_bs_invitation (fk_identity_id);

-- user to user relations
create index idx_right_idx on o_bs_relation_right (g_right);

alter table o_bs_relation_role_to_right add constraint role_to_right_role_idx foreign key (fk_role_id) references o_bs_relation_role (id);
create index idx_role_to_right_role_idx on o_bs_relation_role_to_right (fk_role_id);
alter table o_bs_relation_role_to_right add constraint role_to_right_right_idx foreign key (fk_right_id) references o_bs_relation_right (id);
create index idx_role_to_right_right_idx on o_bs_relation_role_to_right (fk_right_id);

alter table o_bs_identity_to_identity add constraint id_to_id_source_idx foreign key (fk_source_id) references o_bs_identity (id);
create index idx_id_to_id_source_idx on o_bs_identity_to_identity (fk_source_id);
alter table o_bs_identity_to_identity add constraint id_to_id_target_idx foreign key (fk_target_id) references o_bs_identity (id);
create index idx_id_to_id_target_idx on o_bs_identity_to_identity (fk_target_id);
alter table o_bs_identity_to_identity add constraint id_to_role_idx foreign key (fk_role_id) references o_bs_relation_role (id);
create index idx_id_to_id_role_idx on o_bs_identity_to_identity (fk_role_id);

-- user
create index usr_notification_interval_idx on o_user (notification_interval);
create index idx_user_firstname_idx on o_user (u_firstname);
create index idx_user_lastname_idx on o_user (u_lastname);
create index idx_user_nickname_idx on o_user (u_nickname);
create index idx_user_email_idx on o_user (u_email);
create index idx_user_instname_idx on o_user (u_institutionalname);
create index idx_user_instid_idx on o_user (u_institutionaluseridentifier);
create index idx_user_instemail_idx on o_user (u_institutionalemail);
create index idx_user_creationdate_idx on o_user (creationdate);

alter table o_user add constraint iuni_user_nickname_idx unique (u_nickname);

alter table o_user add constraint user_to_ident_idx foreign key (fk_identity) references o_bs_identity(id);
create index idx_user_to_ident_idx on o_user (fk_identity);
alter table o_user add constraint idx_un_user_to_ident_idx UNIQUE (fk_identity);

alter table o_user_data_export add constraint usr_dataex_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_usr_dataex_to_ident_idx on o_user_data_export (fk_identity);
alter table o_user_data_export add constraint usr_dataex_to_requ_idx foreign key (fk_request_by) references o_bs_identity (id);
create index idx_usr_dataex_to_requ_idx on o_user_data_export (fk_request_by);

alter table o_user_absence_leave add constraint abs_leave_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_abs_leave_to_ident_idx on o_user_absence_leave (fk_identity);

-- csp
create index idx_csp_log_to_ident_idx on o_csp_log (fk_identity);

-- temporary key
create index idx_tempkey_identity_idx on o_temporarykey (fk_identity_id);

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

alter table o_repositoryentry add constraint re_deleted_to_identity_idx foreign key (fk_deleted_by) references o_bs_identity (id);
create index idx_re_deleted_to_identity_idx on o_repositoryentry (fk_deleted_by);

alter table o_re_to_tax_level add constraint re_to_lev_re_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_re_to_lev_re_idx on o_re_to_tax_level (fk_entry);
alter table o_re_to_tax_level add constraint re_to_lev_tax_lev_idx foreign key (fk_taxonomy_level) references o_tax_taxonomy_level (id);
create index idx_re_to_lev_tax_lev_idx on o_re_to_tax_level (fk_taxonomy_level);

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

create index idx_ac_aao_identifier_idx on o_ac_auto_advance_order(a_identifier_key, a_identifier_value);
create index idx_ac_aao_ident_idx on o_ac_auto_advance_order(fk_identity);
alter table o_ac_auto_advance_order add constraint aao_ident_idx foreign key (fk_identity) references o_bs_identity (id);

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
alter table o_gta_task add constraint gtaskreset_to_allower_idx foreign key (fk_allow_reset_identity) references o_bs_identity (id);
create index idx_gtaskreset_to_allower_idx on o_gta_task (fk_allow_reset_identity);

alter table o_gta_task_list add constraint gta_list_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_gta_list_to_repo_entry_idx on o_gta_task_list (fk_entry);

alter table o_gta_task_revision add constraint task_rev_to_task_idx foreign key (fk_task) references o_gta_task (id);
create index idx_task_rev_to_task_idx on o_gta_task_revision (fk_task);
alter table o_gta_task_revision add constraint task_rev_to_ident_idx foreign key (fk_comment_author) references o_bs_identity (id);
create index idx_task_rev_to_ident_idx on o_gta_task_revision (fk_comment_author);

alter table o_gta_task_revision_date add constraint gtaskrev_to_task_idx foreign key (fk_task) references o_gta_task (id);
create index idx_gtaskrev_to_task_idx on o_gta_task_revision_date (fk_task);

alter table o_gta_mark add constraint gtamark_tasklist_idx foreign key (fk_tasklist_id) references o_gta_task_list (id);
create index idx_gtamark_tasklist_idx on o_gta_mark (fk_tasklist_id);

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

create index mark_all_idx on o_mark(resname,resid,creator_id);
create index mark_id_idx on o_mark(resid);
create index mark_name_idx on o_mark(resname);
create index mark_subpath_idx on o_mark(substr(ressubpath,0,255));
create index mark_businesspath_idx on o_mark(substr(businesspath,0,255));

-- forum
create index idx_forum_ref_idx on o_forum (f_refresid, f_refresname);
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
create index forum_pseudonym_idx on o_forum_pseudonym (p_pseudonym);

create index readmessage_forum_idx on o_readmessage (forum_id);
create index readmessage_identity_idx on o_readmessage (identity_id);

create index forum_msg_pseudonym_idx on o_message (pseudonym);

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

-- Adobe Connect
alter table o_aconnect_meeting add constraint aconnect_meet_entry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_aconnect_meet_entry_idx on o_aconnect_meeting(fk_entry_id);
alter table o_aconnect_meeting add constraint aconnect_meet_grp_idx foreign key (fk_group_id) references o_gp_business (group_id);
create index idx_aconnect_meet_grp_idx on o_aconnect_meeting(fk_group_id);

alter table o_aconnect_user add constraint aconn_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_aconn_ident_idx on o_aconnect_user (fk_identity_id);

-- BigBlueButton
alter table o_bbb_meeting add constraint bbb_meet_entry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_bbb_meet_entry_idx on o_bbb_meeting(fk_entry_id);
alter table o_bbb_meeting add constraint bbb_meet_grp_idx foreign key (fk_group_id) references o_gp_business (group_id);
create index idx_bbb_meet_grp_idx on o_bbb_meeting(fk_group_id);
alter table o_bbb_meeting add constraint bbb_meet_template_idx foreign key (fk_template_id) references o_bbb_template (id);
create index idx_bbb_meet_template_idx on o_bbb_meeting(fk_template_id);
alter table o_bbb_meeting add constraint bbb_meet_serv_idx foreign key (fk_server_id) references o_bbb_server (id);
create index idx_bbb_meet_serv_idx on o_bbb_meeting(fk_server_id);
alter table o_bbb_meeting add constraint bbb_meet_creator_idx foreign key (fk_creator_id) references o_bs_identity (id);
create index idx_bbb_meet_creator_idx on o_bbb_meeting(fk_creator_id);

alter table o_bbb_attendee add constraint bbb_attend_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_bbb_attend_ident_idx on o_bbb_attendee(fk_identity_id);
alter table o_bbb_attendee add constraint bbb_attend_meet_idx foreign key (fk_meeting_id) references o_bbb_meeting (id);
create index idx_bbb_attend_meet_idx on o_bbb_attendee(fk_meeting_id);

alter table o_bbb_recording add constraint bbb_record_meet_idx foreign key (fk_meeting_id) references o_bbb_meeting (id);
create index idx_bbb_record_meet_idx on o_bbb_recording(fk_meeting_id);

-- Teams
alter table o_teams_meeting add constraint teams_meet_entry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_teams_meet_entry_idx on o_teams_meeting(fk_entry_id);
alter table o_teams_meeting add constraint teams_meet_grp_idx foreign key (fk_group_id) references o_gp_business (group_id);
create index idx_teams_meet_grp_idx on o_teams_meeting(fk_group_id);
alter table o_teams_meeting add constraint teams_meet_creator_idx foreign key (fk_creator_id) references o_bs_identity (id);
create index idx_teams_meet_creator_idx on o_teams_meeting(fk_creator_id);

-- tag
alter table o_tag add constraint FK6491FCA5A4FA5DC foreign key (fk_author_id) references o_bs_identity (id);
create index idx_tag_to_auth_idx on o_tag (fk_author_id);
create index idx_tag_to_resid_idx on o_tag (resid);

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
create index idx_eff_stat_course_ident_idx on o_as_eff_statement (fk_identity,course_repo_key);

-- gotomeeting
alter table o_goto_organizer add constraint goto_organ_owner_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_goto_organ_owner_idx on o_goto_organizer(fk_identity);
create index idx_goto_organ_okey_idx on o_goto_organizer(g_organizer_key);
create index idx_goto_organ_uname_idx on o_goto_organizer(g_username);

alter table o_goto_meeting add constraint goto_meet_repoentry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_goto_meet_repoentry_idx on o_goto_meeting(fk_entry_id);
alter table o_goto_meeting add constraint goto_meet_busgrp_idx foreign key (fk_group_id) references o_gp_business (group_id);
create index idx_goto_meet_busgrp_idx on o_goto_meeting(fk_group_id);
alter table o_goto_meeting add constraint goto_meet_organizer_idx foreign key (fk_organizer_id) references o_goto_organizer (id);
create index idx_goto_meet_organizer_idx on o_goto_meeting(fk_organizer_id);

alter table o_goto_registrant add constraint goto_regis_meeting_idx foreign key (fk_meeting_id) references o_goto_meeting (id);
create index idx_goto_regis_meeting_idx on o_goto_registrant(fk_meeting_id);
alter table o_goto_registrant add constraint goto_regis_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_goto_regis_ident_idx on o_goto_registrant(fk_identity_id);

-- video
alter table o_vid_transcoding add constraint fk_resource_id_idx foreign key (fk_resource_id) references o_olatresource (resource_id);
create index idx_vid_trans_resource_idx on o_vid_transcoding(fk_resource_id);
create index vid_status_trans_idx on o_vid_transcoding(vid_status);
create index vid_transcoder_trans_idx on o_vid_transcoding(vid_transcoder);
alter table o_vid_metadata add constraint vid_meta_rsrc_idx foreign key (fk_resource_id) references o_olatresource (resource_id);
create index idx_vid_meta_rsrc_idx on o_vid_metadata(fk_resource_id);

-- calendar
alter table o_cal_use_config add constraint cal_u_conf_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_cal_u_conf_to_ident_idx on o_cal_use_config (fk_identity);
create index idx_cal_u_conf_cal_id_idx on o_cal_use_config (c_calendar_id);
create index idx_cal_u_conf_cal_type_idx on o_cal_use_config (c_calendar_type);

alter table o_cal_import add constraint cal_imp_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_cal_imp_to_ident_idx on o_cal_import (fk_identity);
create index idx_cal_imp_cal_id_idx on o_cal_import (c_calendar_id);
create index idx_cal_imp_cal_type_idx on o_cal_import (c_calendar_type);

create index idx_cal_imp_to_cal_id_idx on o_cal_import_to (c_to_calendar_id);
create index idx_cal_imp_to_cal_type_idx on o_cal_import_to (c_to_calendar_type);

-- course infos
alter table o_as_user_course_infos add constraint user_course_infos_id_cstr foreign key (fk_identity) references o_bs_identity (id);
create index idx_ucourseinfos_ident_idx on o_as_user_course_infos (fk_identity);
alter table o_as_user_course_infos add constraint user_course_infos_res_cstr foreign key (fk_resource_id) references o_olatresource (resource_id);
create index idx_ucourseinfos_rsrc_idx on o_as_user_course_infos (fk_resource_id);

alter table o_as_entry add constraint as_entry_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_as_entry_to_ident_idx on o_as_entry (fk_identity);
alter table o_as_entry add constraint as_entry_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_as_entry_to_entry_idx on o_as_entry (fk_entry);
alter table o_as_entry add constraint as_entry_to_refentry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
create index idx_as_entry_to_refentry_idx on o_as_entry (fk_reference_entry);

create index idx_as_entry_to_id_idx on o_as_entry (a_assessment_id);
create index idx_as_entry_start_idx on o_as_entry (a_date_start);

-- disadvantage compensation
alter table o_as_compensation add constraint compensation_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_compensation_ident_idx on o_as_compensation(fk_identity);
alter table o_as_compensation add constraint compensation_crea_idx foreign key (fk_creator) references o_bs_identity (id);
create index idx_compensation_crea_idx on o_as_compensation(fk_creator);
alter table o_as_compensation add constraint compensation_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_compensation_entry_idx on o_as_compensation(fk_entry);

create index comp_log_entry_idx on o_as_compensation_log (fk_entry_id);
create index comp_log_ident_idx on o_as_compensation_log (fk_identity_id);

-- mapper
create index o_mapper_uuid_idx on o_mapper (mapper_uuid);

-- qti 2.1
alter table o_qti_assessmenttest_session add constraint qti_sess_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_testess_to_repo_entry_idx on o_qti_assessmenttest_session (fk_entry);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_course_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
create index idx_qti_sess_to_centry_idx on o_qti_assessmenttest_session (fk_reference_entry);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_qti_sess_to_identity_idx on o_qti_assessmenttest_session (fk_identity);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_as_entry_idx foreign key (fk_assessment_entry) references o_as_entry (id);
create index idx_qti_sess_to_as_entry_idx on o_qti_assessmenttest_session (fk_assessment_entry);

alter table o_qti_assessmentitem_session add constraint qti_itemsess_to_testsess_idx foreign key (fk_assessmenttest_session) references o_qti_assessmenttest_session (id);
create index idx_itemsess_to_testsess_idx on o_qti_assessmentitem_session (fk_assessmenttest_session);
create index idx_item_identifier_idx on o_qti_assessmentitem_session (q_itemidentifier);

alter table o_qti_assessment_response add constraint qti_resp_to_testsession_idx foreign key (fk_assessmenttest_session) references o_qti_assessmenttest_session (id);
create index idx_resp_to_testsession_idx on o_qti_assessment_response (fk_assessmenttest_session);
alter table o_qti_assessment_response add constraint qti_resp_to_itemsession_idx foreign key (fk_assessmentitem_session) references o_qti_assessmentitem_session (id);
create index idx_resp_to_itemsession_idx on o_qti_assessment_response (fk_assessmentitem_session);
create index idx_response_identifier_idx on o_qti_assessment_response (q_responseidentifier);

alter table o_qti_assessment_marks add constraint qti_marks_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_marks_to_repo_entry_idx on o_qti_assessment_marks (fk_entry);
alter table o_qti_assessment_marks add constraint qti_marks_to_course_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
create index idx_qti_marks_to_centry_idx on o_qti_assessment_marks (fk_reference_entry);
alter table o_qti_assessment_marks add constraint qti_marks_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_qti_marks_to_identity_idx on o_qti_assessment_marks (fk_identity);

-- vfs
alter table o_vfs_metadata add constraint fmeta_to_author_idx foreign key (fk_locked_identity) references o_bs_identity (id);
create index idx_fmeta_to_author_idx on o_vfs_metadata (fk_locked_identity);
alter table o_vfs_metadata add constraint fmeta_modified_by_idx foreign key (fk_lastmodified_by) references o_bs_identity (id);
create index idx_fmeta_modified_by_idx on o_vfs_metadata (fk_lastmodified_by);
alter table o_vfs_metadata add constraint fmeta_to_lockid_idx foreign key (fk_author) references o_bs_identity (id);
create index idx_fmeta_to_lockid_idx on o_vfs_metadata (fk_author);
alter table o_vfs_metadata add constraint fmeta_to_lic_type_idx foreign key (fk_license_type) references o_lic_license_type (id);
create index idx_fmeta_to_lic_type_idx on o_vfs_metadata (fk_license_type);
alter table o_vfs_metadata add constraint fmeta_to_parent_idx foreign key (fk_parent) references o_vfs_metadata (id);
create index idx_fmeta_to_parent_idx on o_vfs_metadata (fk_parent);
create index f_m_rel_path_idx on o_vfs_metadata (f_relative_path);
create index f_m_file_idx on o_vfs_metadata (f_relative_path,f_filename);
create index f_m_uuid_idx on o_vfs_metadata (f_uuid);

alter table o_vfs_thumbnail add constraint fthumb_to_meta_idx foreign key (fk_metadata) references o_vfs_metadata (id);
create index idx_fthumb_to_meta_idx on o_vfs_thumbnail (fk_metadata);

alter table o_vfs_revision add constraint fvers_to_author_idx foreign key (fk_author) references o_bs_identity (id);
create index idx_fvers_to_author_idx on o_vfs_revision (fk_author);
alter table o_vfs_revision add constraint fvers_to_meta_idx foreign key (fk_metadata) references o_vfs_metadata (id);
create index idx_fvers_to_meta_idx on o_vfs_revision (fk_metadata);
alter table o_vfs_revision add constraint fvers_to_lic_type_idx foreign key (fk_license_type) references o_lic_license_type (id);
create index idx_fvers_to_lic_type_idx on o_vfs_revision (fk_license_type);

-- Document editor
create unique index idx_de_userinfo_ident_idx on o_de_user_info(fk_identity);

-- portfolio
alter table o_pf_binder add constraint pf_binder_resource_idx foreign key (fk_olatresource_id) references o_olatresource (resource_id);
create index idx_pf_binder_resource_idx on o_pf_binder (fk_olatresource_id);
alter table o_pf_binder add constraint pf_binder_group_idx foreign key (fk_group_id) references o_bs_group (id);
create index idx_pf_binder_group_idx on o_pf_binder (fk_group_id);
alter table o_pf_binder add constraint pf_binder_course_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_pf_binder_course_idx on o_pf_binder (fk_entry_id);
alter table o_pf_binder add constraint pf_binder_template_idx foreign key (fk_template_id) references o_pf_binder (id);
create index idx_pf_binder_template_idx on o_pf_binder (fk_template_id);

alter table o_pf_section add constraint pf_section_group_idx foreign key (fk_group_id) references o_bs_group (id);
create index idx_pf_section_group_idx on o_pf_section (fk_group_id);
alter table o_pf_section add constraint pf_section_binder_idx foreign key (fk_binder_id) references o_pf_binder (id);
create index idx_pf_section_binder_idx on o_pf_section (fk_binder_id);
alter table o_pf_section add constraint pf_section_template_idx foreign key (fk_template_reference_id) references o_pf_section (id);
create index idx_pf_section_template_idx on o_pf_section (fk_template_reference_id);

alter table o_pf_page add constraint pf_page_group_idx foreign key (fk_group_id) references o_bs_group (id);
create index idx_pf_page_group_idx on o_pf_page (fk_group_id);
alter table o_pf_page add constraint pf_page_section_idx foreign key (fk_section_id) references o_pf_section (id);
create index idx_pf_page_section_idx on o_pf_page (fk_section_id);

alter table o_pf_media add constraint pf_media_author_idx foreign key (fk_author_id) references o_bs_identity (id);
create index idx_pf_media_author_idx on o_pf_media (fk_author_id);
create index idx_media_storage_path_idx on o_pf_media (p_business_path);

alter table o_pf_page add constraint pf_page_body_idx foreign key (fk_body_id) references o_pf_page_body (id);
create index idx_pf_page_body_idx on o_pf_page (fk_body_id);

alter table o_pf_page_part add constraint pf_page_page_body_idx foreign key (fk_page_body_id) references o_pf_page_body (id);
create index idx_pf_page_page_body_idx on o_pf_page_part (fk_page_body_id);
alter table o_pf_page_part add constraint pf_page_media_idx foreign key (fk_media_id) references o_pf_media (id);
create index idx_pf_page_media_idx on o_pf_page_part (fk_media_id);
alter table o_pf_page_part add constraint pf_part_form_idx foreign key (fk_form_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_pf_part_form_idx on o_pf_page_part (fk_form_entry_id);

create index idx_category_name_idx on o_pf_category (p_name);

alter table o_pf_category_relation add constraint pf_category_rel_cat_idx foreign key (fk_category_id) references o_pf_category (id);
create index idx_pf_category_rel_cat_idx on o_pf_category_relation (fk_category_id);
create index idx_category_rel_resid_idx on o_pf_category_relation (p_resid);

alter table o_pf_assessment_section add constraint pf_asection_section_idx foreign key (fk_section_id) references o_pf_section (id);
create index idx_pf_asection_section_idx on o_pf_assessment_section (fk_section_id);
alter table o_pf_assessment_section add constraint pf_asection_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_pf_asection_ident_idx on o_pf_assessment_section (fk_identity_id);

alter table o_pf_assignment add constraint pf_assign_section_idx foreign key (fk_section_id) references o_pf_section (id);
create index idx_pf_assign_section_idx on o_pf_assignment (fk_section_id);
alter table o_pf_assignment add constraint pf_assign_binder_idx foreign key (fk_binder_id) references o_pf_binder (id);
create index idx_pf_assign_binder_idx on o_pf_assignment (fk_binder_id);
alter table o_pf_assignment add constraint pf_assign_ref_assign_idx foreign key (fk_template_reference_id) references o_pf_assignment (id);
create index idx_pf_assign_ref_assign_idx on o_pf_assignment (fk_template_reference_id);
alter table o_pf_assignment add constraint pf_assign_page_idx foreign key (fk_page_id) references o_pf_page (id);
create index idx_pf_assign_page_idx on o_pf_assignment (fk_page_id);
alter table o_pf_assignment add constraint pf_assign_assignee_idx foreign key (fk_assignee_id) references o_bs_identity (id);
create index idx_pf_assign_assignee_idx on o_pf_assignment (fk_assignee_id);

alter table o_pf_binder_user_infos add constraint binder_user_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_binder_user_to_ident_idx on o_pf_binder_user_infos (fk_identity);
alter table o_pf_binder_user_infos add constraint binder_user_binder_idx foreign key (fk_binder) references o_pf_binder (id);
create index idx_binder_user_binder_idx on o_pf_binder_user_infos (fk_binder);

alter table o_pf_page_user_infos add constraint user_pfpage_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_user_pfpage_idx on o_pf_page_user_infos (fk_identity_id);
alter table o_pf_page_user_infos add constraint page_pfpage_idx foreign key (fk_page_id) references o_pf_page (id);
create index idx_page_pfpage_idx on o_pf_page_user_infos (fk_page_id);

-- evaluation form
alter table o_eva_form_survey add constraint eva_surv_to_surv_idx foreign key (fk_series_previous) references o_eva_form_survey (id);
create index idx_eva_surv_ores_idx on o_eva_form_survey (e_resid, e_resname);

alter table o_eva_form_participation add constraint eva_part_to_surv_idx foreign key (fk_survey) references o_eva_form_survey (id);
create index idx_eva_part_survey_idx on o_eva_form_participation (fk_survey);
create unique index idx_eva_part_ident_idx on o_eva_form_participation (e_identifier_key, e_identifier_type, fk_survey);
create index idx_eva_part_executor_idx on o_eva_form_participation (fk_executor);

alter table o_eva_form_session add constraint eva_sess_to_surv_idx foreign key (fk_survey) references o_eva_form_survey (id);
create index idx_eva_sess_to_surv_idx on o_eva_form_session (fk_survey);
alter table o_eva_form_session add constraint eva_sess_to_part_idx foreign key (fk_participation) references o_eva_form_participation (id);
alter table o_eva_form_session add constraint eva_session_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_eva_session_to_ident_idx on o_eva_form_session (fk_identity);
alter table o_eva_form_session add constraint eva_session_to_body_idx foreign key (fk_page_body) references o_pf_page_body (id);
create index idx_eva_session_to_body_idx on o_eva_form_session (fk_page_body);
alter table o_eva_form_session add constraint eva_session_to_form_idx foreign key (fk_form_entry) references o_repositoryentry (repositoryentry_id);
create index idx_eva_session_to_form_idx on o_eva_form_session (fk_form_entry);

alter table o_eva_form_response add constraint eva_resp_to_sess_idx foreign key (fk_session) references o_eva_form_session (id);
create index idx_eva_resp_to_sess_idx on o_eva_form_response (fk_session);
create index idx_eva_resp_report_idx on o_eva_form_response (fk_session, e_responseidentifier, e_no_response);

-- quality management
alter table o_qual_data_collection add constraint qual_dc_to_gen_idx foreign key (fk_generator) references o_qual_generator (id);
create index idx_dc_to_gen_idx on o_qual_data_collection(fk_generator);
create index idx_dc_status_idx on o_qual_data_collection (q_status);

alter table o_qual_data_collection_to_org add constraint qual_dc_to_org_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
create unique index idx_qual_dc_to_org_idx on o_qual_data_collection_to_org (fk_data_collection, fk_organisation);

alter table o_qual_context add constraint qual_con_to_data_coll_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
create index idx_con_to_data_collection_idx on o_qual_context (fk_data_collection);
alter table o_qual_context add constraint qual_con_to_participation_idx foreign key (fk_eva_participation) references o_eva_form_participation (id);
create index idx_con_to_participation_idx on o_qual_context (fk_eva_participation);
alter table o_qual_context add constraint qual_con_to_session_idx foreign key (fk_eva_session) references o_eva_form_session (id);
create index idx_con_to_session_idx on o_qual_context (fk_eva_session);

alter table o_qual_context_to_organisation add constraint qual_con_to_org_con_idx foreign key (fk_context) references o_qual_context (id);
create index idx_con_to_org_con_idx on o_qual_context_to_organisation (fk_context);
create unique index idx_con_to_org_org_idx on o_qual_context_to_organisation (fk_organisation, fk_context);

alter table o_qual_context_to_curriculum add constraint qual_con_to_cur_con_idx foreign key (fk_context) references o_qual_context (id);
create index idx_con_to_cur_con_idx on o_qual_context_to_curriculum (fk_context);
create unique index idx_con_to_cur_cur_idx on o_qual_context_to_curriculum (fk_curriculum, fk_context);

alter table o_qual_context_to_cur_element add constraint qual_con_to_cur_ele_con_idx foreign key (fk_context) references o_qual_context (id);
create index idx_con_to_cur_ele_con_idx on o_qual_context_to_cur_element (fk_context);
create unique index idx_con_to_cur_ele_ele_idx on o_qual_context_to_cur_element (fk_cur_element, fk_context);

alter table o_qual_context_to_tax_level add constraint qual_con_to_tax_level_con_idx foreign key (fk_context) references o_qual_context (id);
create index idx_con_to_tax_level_con_idx on o_qual_context_to_tax_level (fk_context);
create unique index idx_con_to_tax_level_tax_idx on o_qual_context_to_tax_level (fk_tax_leveL, fk_context);

alter table o_qual_reminder add constraint qual_rem_to_data_coll_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
create index idx_rem_to_data_collection_idx on o_qual_reminder (fk_data_collection);

alter table o_qual_report_access add constraint qual_repacc_to_dc_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
create index o_qual_report_access_dc_idx on o_qual_report_access(fk_data_collection);
alter table o_qual_report_access add constraint qual_repacc_to_generator_idx foreign key (fk_generator) references o_qual_generator (id);
create index o_qual_report_access_gen_idx on o_qual_report_access(fk_generator);

alter table o_qual_generator_to_org add constraint qual_gen_to_org_idx foreign key (fk_generator) references o_qual_generator (id);
create unique index idx_qual_gen_to_org_idx on o_qual_generator_to_org (fk_generator, fk_organisation);

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

alter table o_qp_item add constraint idx_qp_pool_2_tax_id foreign key (fk_taxonomy_level_v2) references o_tax_taxonomy_level(id);
create index idx_item_taxlon_idx on o_qp_item (fk_taxonomy_level_v2);
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
create index idx_item_audit_item_idx on o_qp_item_audit_log (fk_item_id);

-- lti outcome
alter table o_lti_outcome add constraint idx_lti_outcome_ident_id foreign key (fk_identity_id) references o_bs_identity(id);
create index idx_lti_outcome_ident_id_idx on o_lti_outcome (fk_identity_id);
alter table o_lti_outcome add constraint idx_lti_outcome_rsrc_id foreign key (fk_resource_id) references o_olatresource(resource_id);
create index idx_lti_outcome_rsrc_id_idx on o_lti_outcome (fk_resource_id);

-- assessment mode
alter table o_as_mode_course add constraint as_mode_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_as_mode_to_repo_entry_idx on o_as_mode_course (fk_entry);
alter table o_as_mode_course add constraint as_mode_to_lblock_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_as_mode_to_lblock_idx on o_as_mode_course (fk_lecture_block);

alter table o_as_mode_course_to_group add constraint as_modetogroup_group_idx foreign key (fk_group_id) references o_gp_business (group_id);
alter table o_as_mode_course_to_group add constraint as_modetogroup_mode_idx foreign key (fk_assessment_mode_id) references o_as_mode_course (id);
create index idx_as_modetogroup_group_idx on o_as_mode_course_to_group (fk_group_id);
create index idx_as_modetogroup_mode_idx on o_as_mode_course_to_group (fk_assessment_mode_id);

alter table o_as_mode_course_to_area add constraint as_modetoarea_area_idx foreign key (fk_area_id) references o_gp_bgarea (area_id);
alter table o_as_mode_course_to_area add constraint as_modetoarea_mode_idx foreign key (fk_assessment_mode_id) references o_as_mode_course (id);
create index idx_as_modetoarea_area_idx on o_as_mode_course_to_area (fk_area_id);
create index idx_as_modetoarea_mode_idx on o_as_mode_course_to_area (fk_assessment_mode_id);

alter table o_as_mode_course_to_cur_el add constraint as_modetocur_el_idx foreign key (fk_cur_element_id) references o_cur_curriculum_element (id);
alter table o_as_mode_course_to_cur_el add constraint as_modetocur_mode_idx foreign key (fk_assessment_mode_id) references o_as_mode_course (id);
create index idx_as_modetocur_el_idx on o_as_mode_course_to_cur_el (fk_cur_element_id);
create index idx_as_modetocur_mode_idx on o_as_mode_course_to_cur_el (fk_assessment_mode_id);

-- certificates
alter table o_cer_certificate add constraint cer_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index cer_identity_idx on o_cer_certificate (fk_identity);
alter table o_cer_certificate add constraint cer_to_resource_idx foreign key (fk_olatresource) references o_olatresource (resource_id);
create index cer_resource_idx on o_cer_certificate (fk_olatresource);
create index cer_archived_resource_idx on o_cer_certificate (c_archived_resource_id);
create index cer_uuid_idx on o_cer_certificate (c_uuid);

-- sms
alter table o_sms_message_log add constraint sms_log_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_sms_log_to_identity_idx on o_sms_message_log(fk_identity);

-- webfeed
create index idx_feed_resourceable_idx on o_feed (f_resourceable_id, f_resourceable_type);
alter table o_feed_item add constraint item_to_feed_fk foreign key(fk_feed_id) references o_feed(id);
create index idx_item_feed_idx on o_feed_item(fk_feed_id);
alter table o_feed_item add constraint feed_item_to_ident_author_fk foreign key (fk_identity_author_id) references o_bs_identity (id);
create index idx_item_ident_author_idx on o_feed_item (fk_identity_author_id);
alter table o_feed_item add constraint feed_item_to_ident_modified_fk foreign key (fk_identity_modified_id) references o_bs_identity (id);
create index idx_item_ident_modified_idx on o_feed_item (fk_identity_modified_id);

-- taxonomy
alter table o_tax_taxonomy add constraint tax_to_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_tax_to_group_idx on o_tax_taxonomy (fk_group);

alter table o_tax_taxonomy_level_type add constraint tax_type_to_taxonomy_idx foreign key (fk_taxonomy) references o_tax_taxonomy (id);
create index idx_tax_type_to_taxonomy_idx on o_tax_taxonomy_level_type (fk_taxonomy);

alter table o_tax_taxonomy_type_to_type add constraint tax_type_to_type_idx foreign key (fk_type) references o_tax_taxonomy_level_type (id);
create index idx_tax_type_to_type_idx on o_tax_taxonomy_type_to_type (fk_type);
alter table o_tax_taxonomy_type_to_type add constraint tax_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_tax_taxonomy_level_type (id);
create index idx_tax_type_to_sub_type_idx on o_tax_taxonomy_type_to_type (fk_allowed_sub_type);

alter table o_tax_taxonomy_level add constraint tax_level_to_taxonomy_idx foreign key (fk_taxonomy) references o_tax_taxonomy (id);
create index idx_tax_level_to_taxonomy_idx on o_tax_taxonomy_level (fk_taxonomy);
alter table o_tax_taxonomy_level add constraint tax_level_to_tax_level_idx foreign key (fk_parent) references o_tax_taxonomy_level (id);
create index idx_tax_level_to_tax_level_idx on o_tax_taxonomy_level (fk_parent);
alter table o_tax_taxonomy_level add constraint tax_level_to_type_idx foreign key (fk_type) references o_tax_taxonomy_level_type (id);
create index idx_tax_level_to_type_idx on o_tax_taxonomy_level (fk_type);
create index idx_tax_level_path_key_idx on o_tax_taxonomy_level (t_m_path_keys);

alter table o_tax_taxonomy_competence add constraint tax_comp_to_tax_level_idx foreign key (fk_level) references o_tax_taxonomy_level (id);
create index idx_tax_comp_to_tax_level_idx on o_tax_taxonomy_competence (fk_level);
alter table o_tax_taxonomy_competence add constraint tax_level_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_tax_level_to_ident_idx on o_tax_taxonomy_competence (fk_identity);

-- lectures
alter table o_lecture_block add constraint lec_block_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_lec_block_entry_idx on o_lecture_block(fk_entry);
alter table o_lecture_block add constraint lec_block_gcoach_idx foreign key (fk_teacher_group) references o_bs_group (id);
create index idx_lec_block_gcoach_idx on o_lecture_block(fk_teacher_group);
alter table o_lecture_block add constraint lec_block_reason_idx foreign key (fk_reason) references o_lecture_reason (id);
create index idx_lec_block_reason_idx on o_lecture_block(fk_reason);

alter table o_lecture_block_roll_call add constraint absence_category_idx foreign key (fk_absence_category) references o_lecture_absence_category (id);
create index idx_absence_category_idx on o_lecture_block_roll_call (fk_absence_category);

alter table o_lecture_absence_notice add constraint notice_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_notice_identity_idx on o_lecture_absence_notice (fk_identity);
alter table o_lecture_absence_notice add constraint notice_notif_identity_idx foreign key (fk_notifier) references o_bs_identity (id);
create index idx_notice_notif_identity_idx on o_lecture_absence_notice (fk_notifier);
alter table o_lecture_absence_notice add constraint notice_auth_identity_idx foreign key (fk_authorizer) references o_bs_identity (id);
create index idx_notice_auth_identity_idx on o_lecture_absence_notice (fk_authorizer);
alter table o_lecture_absence_notice add constraint notice_category_idx foreign key (fk_absence_category) references o_lecture_absence_category (id);
create index idx_notice_category_idx on o_lecture_absence_notice (fk_absence_category);

alter table o_lecture_notice_to_block add constraint notice_to_block_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_notice_to_block_idx on o_lecture_notice_to_block (fk_lecture_block);
alter table o_lecture_notice_to_block add constraint notice_to_notice_idx foreign key (fk_absence_notice) references o_lecture_absence_notice (id);
create index idx_notice_to_notice_idx on o_lecture_notice_to_block (fk_absence_notice);

alter table o_lecture_notice_to_entry add constraint notice_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_notice_to_entry_idx on o_lecture_notice_to_entry (fk_entry);
alter table o_lecture_notice_to_entry add constraint rel_notice_e_to_notice_idx foreign key (fk_absence_notice) references o_lecture_absence_notice (id);
create index idx_rel_notice_e_to_notice_idx on o_lecture_notice_to_entry (fk_absence_notice);

alter table o_lecture_block_to_group add constraint lec_block_to_block_idx foreign key (fk_group) references o_bs_group (id);
create index idx_lec_block_to_block_idx on o_lecture_block_to_group(fk_group);
alter table o_lecture_block_to_group add constraint lec_block_to_group_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_lec_block_to_group_idx on o_lecture_block_to_group(fk_lecture_block);

alter table o_lecture_block_roll_call add constraint lec_call_block_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_lec_call_block_idx on o_lecture_block_roll_call(fk_lecture_block);
alter table o_lecture_block_roll_call add constraint lec_call_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_lec_call_identity_idx on o_lecture_block_roll_call(fk_identity);
alter table o_lecture_block_roll_call add constraint rollcall_to_notice_idx foreign key (fk_absence_notice) references o_lecture_absence_notice (id);
create index idx_rollcall_to_notice_idx on o_lecture_block_roll_call (fk_absence_notice);

alter table o_lecture_reminder add constraint lec_reminder_block_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_lec_reminder_block_idx on o_lecture_reminder(fk_lecture_block);
alter table o_lecture_reminder add constraint lec_reminder_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_lec_reminder_identity_idx on o_lecture_reminder(fk_identity);

alter table o_lecture_participant_summary add constraint lec_part_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_lec_part_entry_idx on o_lecture_participant_summary(fk_entry);
alter table o_lecture_participant_summary add constraint lec_part_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_lec_part_ident_idx on o_lecture_participant_summary(fk_identity);

alter table o_lecture_entry_config add constraint lec_entry_config_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);

create index idx_lec_audit_entry_idx on o_lecture_block_audit_log(fk_entry);
create index idx_lec_audit_ident_idx on o_lecture_block_audit_log(fk_identity);

alter table o_lecture_block_to_tax_level add constraint lblock_rel_to_lblock_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_lblock_rel_to_lblock_idx on o_lecture_block_to_tax_level (fk_lecture_block);
alter table o_lecture_block_to_tax_level add constraint lblock_rel_to_tax_lev_idx foreign key (fk_taxonomy_level) references o_tax_taxonomy_level (id);
create index idx_lblock_rel_to_tax_lev_idx on o_lecture_block_to_tax_level (fk_taxonomy_level);

-- dialog elements
alter table o_dialog_element add constraint dial_el_author_idx foreign key (fk_author) references o_bs_identity (id);
create index idx_dial_el_author_idx on o_dialog_element (fk_author);
alter table o_dialog_element add constraint dial_el_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_dial_el_entry_idx on o_dialog_element (fk_entry);
alter table o_dialog_element add constraint dial_el_forum_idx foreign key (fk_forum) references o_forum (forum_id);
create index idx_dial_el_forum_idx on o_dialog_element (fk_forum);
create index idx_dial_el_subident_idx on o_dialog_element (d_subident);

--licenses
alter table o_lic_license_type_activation add constraint lic_activation_type_fk foreign key (fk_license_type_id) references o_lic_license_type (id);
create index lic_activation_type_idx on o_lic_license_type_activation (fk_license_type_id);
alter table o_lic_license add constraint lic_license_type_fk foreign key (fk_license_type_id) references o_lic_license_type (id);
create index lic_license_type_idx on o_lic_license (fk_license_type_id);
create unique index lic_license_ores_idx on o_lic_license (l_resid, l_resname);

-- organisation
alter table o_org_organisation add constraint org_to_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_org_to_group_idx on o_org_organisation (fk_group);
alter table o_org_organisation add constraint org_to_root_org_idx foreign key (fk_root) references o_org_organisation (id);
create index idx_org_to_root_org_idx on o_org_organisation (fk_root);
alter table o_org_organisation add constraint org_to_parent_org_idx foreign key (fk_parent) references o_org_organisation (id);
create index idx_org_to_parent_org_idx on o_org_organisation (fk_parent);
alter table o_org_organisation add constraint org_to_org_type_idx foreign key (fk_type) references o_org_organisation_type (id);
create index idx_org_to_org_type_idx on o_org_organisation (fk_type);

alter table o_org_type_to_type add constraint org_type_to_type_idx foreign key (fk_type) references o_org_organisation_type (id);
create index idx_org_type_to_type_idx on o_org_type_to_type (fk_type);
alter table o_org_type_to_type add constraint org_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_org_organisation_type (id);
create index idx_org_type_to_sub_type_idx on o_org_type_to_type (fk_allowed_sub_type);

alter table o_re_to_organisation add constraint rel_org_to_re_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_rel_org_to_re_idx on o_re_to_organisation (fk_entry);
alter table o_re_to_organisation add constraint rel_org_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_rel_org_to_org_idx on o_re_to_organisation (fk_organisation);

-- curriculum
alter table o_cur_curriculum add constraint cur_to_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_cur_to_group_idx on o_cur_curriculum (fk_group);
alter table o_cur_curriculum add constraint cur_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_cur_to_org_idx on o_cur_curriculum (fk_organisation);

alter table o_cur_curriculum_element add constraint cur_el_to_group_idx foreign key (fk_group) references o_bs_group (id);
create index idx_cur_el_to_group_idx on o_cur_curriculum_element (fk_group);
alter table o_cur_curriculum_element add constraint cur_el_to_cur_el_idx foreign key (fk_parent) references o_cur_curriculum_element (id);
create index idx_cur_el_to_cur_el_idx on o_cur_curriculum_element (fk_parent);
alter table o_cur_curriculum_element add constraint cur_el_to_cur_idx foreign key (fk_curriculum) references o_cur_curriculum (id);
create index idx_cur_el_to_cur_idx on o_cur_curriculum_element (fk_curriculum);
alter table o_cur_curriculum_element add constraint cur_el_type_to_el_type_idx foreign key (fk_type) references o_cur_element_type (id);
create index idx_cur_el_type_to_el_type_idx on o_cur_curriculum_element (fk_type);

alter table o_cur_element_type_to_type add constraint cur_type_to_type_idx foreign key (fk_type) references o_cur_element_type (id);
create index idx_cur_type_to_type_idx on o_cur_element_type_to_type (fk_type);
alter table o_cur_element_type_to_type add constraint cur_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_cur_element_type (id);
create index idx_cur_type_to_sub_type_idx on o_cur_element_type_to_type (fk_allowed_sub_type);

alter table o_cur_element_to_tax_level add constraint cur_el_rel_to_cur_el_idx foreign key (fk_cur_element) references o_cur_curriculum_element (id);
create index idx_cur_el_rel_to_cur_el_idx on o_cur_element_to_tax_level (fk_cur_element);
alter table o_cur_element_to_tax_level add constraint cur_el_to_tax_level_idx foreign key (fk_taxonomy_level) references o_tax_taxonomy_level (id);
create index idx_cur_el_to_tax_level_idx on o_cur_element_to_tax_level (fk_taxonomy_level);

-- edu-sharing
create index idx_es_usage_ident_idx on o_es_usage (e_identifier);
create index idx_es_usage_ores_idx on o_es_usage (e_resid, e_resname);

-- o_logging_table
create index log_target_resid_idx on o_loggingtable(targetresid);
create index log_ptarget_resid_idx on o_loggingtable(parentresid);
create index log_gptarget_resid_idx on o_loggingtable(grandparentresid);
create index log_ggptarget_resid_idx on o_loggingtable(greatgrandparentresid);
create index log_creationdate_idx on o_loggingtable(creationdate);

-- livestream
create index idx_livestream_viewers_idx on o_livestream_launch(l_subident, l_launch_date, fk_entry, fk_identity);

-- grading
alter table o_grad_to_identity add constraint grad_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_grad_to_ident_idx on o_grad_to_identity (fk_identity);
alter table o_grad_to_identity add constraint grad_id_to_repo_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_grad_id_to_repo_idx on o_grad_to_identity (fk_entry);

alter table o_grad_assignment add constraint grad_assign_to_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
create index idx_grad_assign_to_entry_idx on o_grad_assignment (fk_reference_entry);
alter table o_grad_assignment add constraint grad_assign_to_assess_idx foreign key (fk_assessment_entry) references o_as_entry (id);
create index idx_grad_assign_to_assess_idx on o_grad_assignment (fk_assessment_entry);
alter table o_grad_assignment add constraint grad_assign_to_grader_idx foreign key (fk_grader) references o_grad_to_identity (id);
create index idx_grad_assign_to_grader_idx on o_grad_assignment (fk_grader);

alter table o_grad_time_record add constraint grad_time_to_assign_idx foreign key (fk_assignment) references o_grad_assignment (id);
create index idx_grad_time_to_assign_idx on o_grad_time_record (fk_assignment);
alter table o_grad_time_record add constraint grad_time_to_grader_idx foreign key (fk_grader) references o_grad_to_identity (id);
create index idx_grad_time_to_grader_idx on o_grad_time_record (fk_grader);

alter table o_grad_configuration add constraint grad_config_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_grad_config_to_entry_idx on o_grad_configuration (fk_entry);

-- Appointments
alter table o_ap_topic add constraint ap_topic_entry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_ap_topic_entry_idx on o_ap_topic(fk_entry_id);
alter table o_ap_organizer add constraint ap_organizer_topic_idx foreign key (fk_topic_id) references o_ap_topic (id);
create index idx_ap_organizer_topic_idx on o_ap_organizer(fk_topic_id);
alter table o_ap_organizer add constraint ap_organizer_identity_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_ap_organizer_identitiy_idx on o_ap_organizer(fk_identity_id);
alter table o_ap_topic_to_group add constraint ap_tg_topic_idx foreign key (fk_topic_id) references o_ap_topic (id);
create index idx_ap_tg_topic_idx on o_ap_topic_to_group(fk_topic_id);
create index idx_ap_tg_group_idx on o_ap_topic_to_group(fk_group_id);
alter table o_ap_appointment add constraint ap_appointment_topic_idx foreign key (fk_topic_id) references o_ap_topic (id);
create index idx_ap_appointment_topic_idx on o_ap_appointment(fk_topic_id);
alter table o_ap_appointment add constraint ap_appointment_meeting_idx foreign key (fk_meeting_id) references o_bbb_meeting (id);
create index idx_ap_appointment_meeting_idx on o_ap_appointment(fk_meeting_id);
alter table o_ap_participation add constraint ap_part_appointment_idx foreign key (fk_appointment_id) references o_ap_appointment (id);
create index idx_ap_part_appointment_idx on o_ap_participation(fk_appointment_id);
alter table o_ap_participation add constraint ap_part_identity_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_ap_part_identitiy_idx on o_ap_participation(fk_identity_id);

insert into o_stat_lastupdated (until_datetime, from_datetime, lastupdated) values (to_date('1999-01-01', 'YYYY-mm-dd'), to_date('1999-01-01', 'YYYY-mm-dd'), to_date('1999-01-01', 'YYYY-mm-dd'));
insert into hibernate_unique_key values ( 0 );

-- Organiation role rights
alter table o_org_role_to_right add constraint org_role_to_right_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_org_role_to_r_to_org_idx on o_org_role_to_right(fk_organisation);

-- Contact tracing
alter table o_ct_registration add constraint reg_to_loc_idx foreign key (fk_location) references o_ct_location (id);
create index idx_reg_to_loc_idx on o_ct_registration (fk_location);


commit
/
