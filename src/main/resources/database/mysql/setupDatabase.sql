SET FOREIGN_KEY_CHECKS = 0;

create table if not exists o_forum (
   forum_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   f_refresname varchar(50),
   f_refresid bigint,
   primary key (forum_id)
);
create table o_forum_pseudonym (
   id bigint not null auto_increment,
   creationdate datetime not null,
   p_pseudonym varchar(255) not null,
   p_credential varchar(255) not null,
   p_salt varchar(255) not null,
   p_hashalgorithm varchar(16) not null,
   primary key (id)
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

create table o_bs_group (
   id bigint not null,
   creationdate datetime not null,
   g_name varchar(36),
   primary key (id)
);

create table o_bs_group_member (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_role varchar(24) not null,
   g_inheritance_mode varchar(16) default 'none' not null,
   fk_group_id bigint not null,
   fk_identity_id bigint not null,
   primary key (id)
);

create table o_bs_grant (
   id bigint not null,
   creationdate datetime not null,
   g_role varchar(32) not null,
   g_permission varchar(32) not null,
   fk_group_id bigint not null,
   fk_resource_id bigint not null,
   primary key (id)
);

create table if not exists o_gp_business (
   group_id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime,
   creationdate datetime,
   lastusage datetime,
   groupname varchar(255),
   external_id varchar(64),
   managed_flags varchar(255),
   descr longtext,
   minparticipants integer,
   maxparticipants integer,
   waitinglist_enabled bit,
   autocloseranks_enabled bit,
   ownersintern bit not null default 0,
   participantsintern bit not null default 0,
   waitingintern bit not null default 0,
   ownerspublic bit not null default 0,
   participantspublic bit not null default 0,
   waitingpublic bit not null default 0,
   downloadmembers bit not null default 0,
   allowtoleave bit not null default 1,
   fk_resource bigint unique,
   fk_group_id bigint unique,
   primary key (group_id)
);
create table if not exists o_temporarykey (
   reglist_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   email varchar(2000) not null,
   regkey varchar(255) not null,
   ip varchar(255) not null,
   valid_until datetime,
   mailsent bit not null,
   action varchar(255) not null,
   fk_identity_id bigint,
   primary key (reglist_id)
);
create table if not exists o_bs_authentication (
   id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   lastmodified datetime not null,
   identity_fk bigint not null,
   provider varchar(8),
   authusername varchar(255),
   credential varchar(255),
   salt varchar(255) default null,
   hashalgorithm varchar(16) default null,
   primary key (id),
   unique (provider, authusername)
);
create table if not exists o_bs_authentication_history (
   id bigint not null auto_increment,
   creationdate datetime,
   provider varchar(8),
   authusername varchar(255),
   credential varchar(255),
   salt varchar(255) default null,
   hashalgorithm varchar(16) default null,
   fk_identity bigint not null,
   primary key (id)
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
   external_id varchar(64),
   status integer,
   deleteddate datetime,
   deletedroles varchar(1024),
   deletedby varchar(128),
   inactivationdate datetime,
   inactivationemaildate datetime,
   expirationdate datetime,
   expirationemaildate datetime,
   reactivationdate datetime,
   deletionemaildate datetime,
   primary key (id)
);
create table o_bs_relation_role (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_role varchar(128) not null,
   g_external_id varchar(128),
   g_external_ref varchar(128),
   g_managed_flags varchar(256),
   primary key (id)
);
create table o_bs_relation_right (
   id bigint not null auto_increment,
   creationdate datetime not null,
   g_right varchar(128) not null,
   primary key (id)
);
create table o_bs_relation_role_to_right (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_role_id bigint,
   fk_right_id bigint not null,
   primary key (id)
);
create table o_bs_identity_to_identity (
   id bigint not null auto_increment,
   creationdate datetime not null,
   g_external_id varchar(128),
   g_managed_flags varchar(256),
   fk_source_id bigint not null,
   fk_target_id bigint not null,
   fk_role_id bigint not null,
   primary key (id)
);
create table o_csp_log (
   id bigint not null auto_increment,
   creationdate datetime,
   l_blocked_uri varchar(1024),
   l_disposition varchar(32),
   l_document_uri varchar(1024),
   l_effective_directive mediumtext,
   l_original_policy mediumtext,
   l_referrer varchar(1024),
   l_script_sample mediumtext,
   l_status_code varchar(1024),
   l_violated_directive varchar(1024),
   l_source_file varchar(1024),
   l_line_number bigint,
   l_column_number bigint,
   fk_identity bigint,
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
   style varchar(16),
   externalurl varchar(255),
   fk_repoentry bigint,
   fk_ownergroup bigint unique,
   type integer not null,
   parent_id bigint,
   order_index bigint, 
   short_title varchar(255),
   add_entry_position int,
   add_category_position int,
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
create table if not exists o_references (
   reference_id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   source_id bigint not null,
   target_id bigint not null,
   userdata varchar(64),
   primary key (reference_id)
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
   u_firstname varchar(255),
   u_lastname varchar(255),
   u_email varchar(255),
   u_nickname varchar(255),
   u_birthday varchar(255),
   u_graduation varchar(255),
   u_gender varchar(255),
   u_telprivate varchar(255),
   u_telmobile varchar(255),
   u_teloffice varchar(255),
   u_smstelmobile varchar(255),
   u_skype varchar(255),
   u_msn varchar(255),
   u_xing varchar(255),
   u_linkedin text(255),
   u_icq varchar(255),
   u_homepage varchar(255),
   u_street varchar(255),
   u_extendedaddress varchar(255),
   u_pobox varchar(255),
   u_zipcode varchar(255),
   u_region varchar(255),
   u_city varchar(255),
   u_country varchar(255),
   u_countrycode varchar(255),
   u_institutionalname varchar(255),
   u_institutionaluseridentifier varchar(255),
   u_institutionalemail varchar(255),
   u_orgunit varchar(255),
   u_studysubject varchar(255),
   u_emchangekey varchar(255),
   u_emaildisabled varchar(255),
   u_typeofuser varchar(255),
   u_socialsecuritynumber varchar(255),

   u_rank varchar(255),
   u_degree varchar(255),
   u_position varchar(255),
   u_userinterests varchar(255),
   u_usersearchedinterests varchar(255),
   u_officestreet varchar(255),
   u_extendedofficeaddress varchar(255),
   u_officepobox varchar(255),
   u_officezipcode varchar(255),
   u_officecity varchar(255),
   u_officecountry varchar(255),
   u_officemobilephone varchar(255),
   u_department varchar(255),
   u_privateemail varchar(255),
   u_employeenumber text(255),
   u_organizationalunit text(255),

   u_edupersonaffiliation text(255),
   u_swissedupersonstaffcategory text(255),
   u_swissedupersonhomeorg text(255),
   u_swissedupersonstudylevel text(255),
   u_swissedupersonhomeorgtype text(255),
   u_swissedupersonstudybranch1 text(255),
   u_swissedupersonstudybranch2 text(255),
   u_swissedupersonstudybranch3 text(255),

   u_genericselectionproperty varchar(255),
   u_genericselectionproperty2 varchar(255),
   u_genericselectionproperty3 varchar(255),
   u_generictextproperty varchar(255),
   u_generictextproperty2 varchar(255),
   u_generictextproperty3 varchar(255),
   u_generictextproperty4 varchar(255),
   u_generictextproperty5 varchar(255),
   u_genericuniquetextproperty varchar(255),
   u_genericuniquetextproperty2 varchar(255),
   u_genericuniquetextproperty3 varchar(255),
   u_genericemailproperty1 varchar(255),
   u_genericcheckboxproperty varchar(255),
   u_genericcheckboxproperty2 varchar(255),
   u_genericcheckboxproperty3 varchar(255),

   fk_identity bigint,
   primary key (user_id)
);
create table if not exists o_userproperty (
   fk_user_id bigint not null,
   propname varchar(255) not null,
   propvalue varchar(255),
   primary key (fk_user_id, propname)
);
create table o_user_data_export (
   id bigint not null auto_increment,
   creationdate datetime,
   lastmodified datetime,
   u_directory varchar(255),
   u_status varchar(16),
   u_export_ids varchar(2000),
   fk_identity bigint not null,
   fk_request_by bigint,
   primary key (id)
);
create table o_user_absence_leave (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   u_absent_from datetime,
   u_absent_to datetime,
   u_resname varchar(50),
   u_resid bigint,
   u_sub_ident varchar(2048),
   fk_identity bigint not null,
   primary key (id)
);

create table if not exists o_message (
   message_id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime,
   creationdate datetime,
   title varchar(100),
   body longtext,
   pseudonym varchar(255),
   guest bit default 0,
   parent_id bigint,
   topthread_id bigint,
   creator_id bigint,
   modifier_id bigint,
   modification_date datetime,
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
   subenabled bit default 1,
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
   fk_resource bigint default null,
   primary key (area_id)
);
create table if not exists o_repositoryentry (
   repositoryentry_id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime,
   creationdate datetime,
   softkey varchar(36) not null unique,
   external_id varchar(64),
   external_ref varchar(255),
   managed_flags varchar(255),
   technical_type varchar(128),
   displayname varchar(110) not null,
   resourcename varchar(100) not null,
   authors varchar(2048),
   mainlanguage varchar(255),
   location varchar(255),
   objectives text(32000),
   requirements text(32000),
   credits text(32000),
   expenditureofwork text(32000),
   fk_stats bigint not null unique,
   fk_lifecycle bigint,
   fk_olatresource bigint unique,
   description longtext,
   initialauthor varchar(128) not null,
   status varchar(16) default 'preparation' not null,
   allusers bit default 0 not null,
   guests bit default 0 not null,
   bookable bit default 0 not null,
   allowtoleave varchar(16),
   candownload bit not null,
   cancopy bit not null,
   canreference bit not null,
   deletiondate datetime default null,
   fk_deleted_by bigint default null,
   fk_educational_type bigint default null,
   primary key (repositoryentry_id)
);
create table o_re_to_group (
   id bigint not null,
   creationdate datetime not null,
   r_defgroup boolean not null,
   fk_group_id bigint not null,
   fk_entry_id bigint not null,
   primary key (id)
);
create table o_re_to_tax_level (
  id bigint not null auto_increment,
  creationdate datetime not null,
  fk_entry bigint not null,
  fk_taxonomy_level bigint not null,
  primary key (id)
);
create table o_repositoryentry_cycle (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   r_softkey varchar(64),
   r_label varchar(255),
   r_privatecycle bit default 0,
   r_validfrom datetime,
   r_validto datetime,
   primary key (id)
);
create table o_repositoryentry_stats (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   r_rating decimal(65,30),
   r_num_of_ratings bigint not null default 0,
   r_num_of_comments bigint not null default 0,
   r_launchcounter bigint not null default 0,
   r_downloadcounter bigint not null default 0,
   r_lastusage datetime not null,
   primary key (id)
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
create table o_re_educational_type (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   r_identifier varchar(128) not null,
   r_predefined bool not null default false,
   r_css_class varchar(128),
   primary key (id)
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
    windowid varchar(32) default null,
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
   id bigint not null,
   version bigint not null,
   lastmodified datetime,
   creationdate datetime,
   courseid bigint,
   identity bigint,
   category varchar(32),
   name varchar(255) not null,
   floatvalue decimal(65,30),
   longvalue bigint,
   stringvalue varchar(255),
   textvalue mediumtext,
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
  message longtext,
  attachmentpath varchar(1024),
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
   creationdate datetime,
   token varchar(64) not null,
   first_name varchar(64),
   last_name varchar(64),
   mail varchar(128),
   fk_group_id bigint,
   fk_identity_id bigint,
   primary key (id)
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
  autobooking boolean default 0,
  confirmation_email bit default 0,
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

create table o_ac_auto_advance_order (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  a_identifier_key varchar(64) not null,
  a_identifier_value varchar(64) not null,
  a_status varchar(32) not null,
  a_status_modified datetime not null,
  fk_identity int8 not null,
  fk_method int8 not null,
  primary key (id)
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
   transaction_id bigint not null,
   version bigint not null,
   creationdate datetime,
   ref_no varchar(255),
   order_id bigint not null,
   order_part_id bigint not null,
   method_id bigint not null,
   success_uuid varchar(32) not null,
   cancel_uuid varchar(32) not null,
   amount_amount DECIMAL(12,4),
   amount_currency_code VARCHAR(3),
   pay_response_date datetime,
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

-- paypal checkout
create table o_ac_checkout_transaction (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_success_uuid varchar(64) not null,
   p_cancel_uuid varchar(64) not null,
   p_order_nr varchar(64) not null,
   p_order_id bigint not null,
   p_order_part_id bigint not null,
   p_method_id bigint not null,
   p_amount_currency_code varchar(3) not null,
   p_amount_amount decimal(12,4) not null,
   p_status varchar(32) not null,
   p_paypal_order_id varchar(64),
   p_paypal_order_status varchar(64),
   p_paypal_order_status_reason text,
   p_paypal_authorization_id varchar(64),
   p_paypal_capture_id varchar(64),
   p_capture_currency_code varchar(3),
   p_capture_amount decimal(12,4),
   p_paypal_invoice_id varchar(64),
   primary key (id)
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

-- Adobe Connect
create table o_aconnect_meeting (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   a_sco_id varchar(128) default null,
   a_folder_id varchar(128) default null,
   a_env_name varchar(128) default null,
   a_name varchar(128) not null,
   a_description varchar(2000) default null,
   a_permanent bool default false not null,
   a_start_date datetime default null,
   a_leadtime bigint default 0 not null,
   a_start_with_leadtime datetime,
   a_end_date datetime default null,
   a_followuptime bigint default 0 not null,
   a_end_with_followuptime datetime,
   a_opened bool default false not null,
   a_template_id varchar(32) default null,
   a_shared_documents varchar(2000) default null,
   fk_entry_id bigint default null,
   a_sub_ident varchar(64) default null,
   fk_group_id bigint default null,
   primary key (id)
);

create table o_aconnect_user (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   a_principal_id varchar(128) default null,
   a_env_name varchar(128) default null,
   fk_identity_id bigint default null,
   primary key (id)
);

-- BigBlueButton
create table o_bbb_template (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   b_name varchar(128) not null,
   b_description varchar(2000) default null,
   b_system bool default false not null,
   b_enabled bool default true not null,
   b_external_id varchar(255) default null,
   b_external_users bool default true not null,
   b_max_concurrent_meetings int default null,
   b_max_participants int default null,
   b_max_duration bigint default null,
   b_record bool default null,
   b_breakout bool default null,
   b_mute_on_start bool default null,
   b_auto_start_recording bool default null,
   b_allow_start_stop_recording bool default null,
   b_webcams_only_for_moderator bool default null,
   b_allow_mods_to_unmute_users bool default null,
   b_lock_set_disable_cam bool default null,
   b_lock_set_disable_mic bool default null,
   b_lock_set_disable_priv_chat bool default null,
   b_lock_set_disable_public_chat bool default null,
   b_lock_set_disable_note bool default null,
   b_lock_set_locked_layout bool default null,
   b_lock_set_hide_user_list bool default null,
   b_lock_set_lock_on_join bool default null,
   b_lock_set_lock_on_join_conf bool default null,
   b_permitted_roles varchar(255) default null,
   b_guest_policy varchar(32) default null,
   primary key (id)
);

create table o_bbb_server (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   b_name varchar(128),
   b_url varchar(255) not null,
   b_shared_secret varchar(255),
   b_recording_url varchar(255),
   b_enabled bool default true,
   b_capacity_factor decimal,
   primary key (id)
);

create table o_bbb_meeting (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   b_meeting_id varchar(128) not null,
   b_attendee_pw varchar(128) not null,
   b_moderator_pw varchar(128) not null,
   b_name varchar(128) not null,
   b_description varchar(2000) default null,
   b_welcome mediumtext,
   b_layout varchar(16) default 'standard',
   b_permanent bool default false not null,
   b_guest bool default false not null,
   b_identifier varchar(64),
   b_read_identifier varchar(64),
   b_password varchar(64),
   b_start_date datetime default null,
   b_leadtime bigint default 0 not null,
   b_start_with_leadtime datetime,
   b_end_date datetime default null,
   b_followuptime bigint default 0 not null,
   b_end_with_followuptime datetime,
   b_main_presenter varchar(255),
   b_directory varchar(64) default null,
   b_recordings_publishing varchar(16) default 'auto',
   b_record bool default null,
   fk_creator_id bigint default null,
   fk_entry_id bigint default null,
   a_sub_ident varchar(64) default null,
   fk_group_id bigint default null,
   fk_template_id bigint default null,
   fk_server_id bigint,
   primary key (id)
);

create table o_bbb_attendee (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   b_role varchar(32),
   b_join_date datetime,
   b_pseudo varchar(255),
   fk_identity_id bigint,
   fk_meeting_id bigint not null,
   primary key (id)
);

create table o_bbb_recording (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   b_recording_id varchar(255) not null,
   b_publish_to varchar(128),
   b_permanent bool default null,
   b_start_date datetime default null,
   b_end_date datetime default null,
   b_url varchar(1024),
   b_type varchar(32),
   fk_meeting_id bigint not null,
   unique(b_recording_id,fk_meeting_id),
   primary key (id)
);

-- Teams
create table o_teams_meeting (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   t_subject varchar(255),
   t_description varchar(4000),
   t_main_presenter varchar(255),
   t_start_date datetime default null,
   t_leadtime bigint default 0 not null,
   t_start_with_leadtime datetime,
   t_end_date datetime default null,
   t_followuptime bigint default 0 not null,
   t_end_with_followuptime datetime,
   t_permanent bool default false,
   t_join_information varchar(4000),
   t_guest bool default false not null,
   t_identifier varchar(64),
   t_read_identifier varchar(64),
   t_online_meeting_id varchar(1024),
   t_online_meeting_join_url varchar(2000),
   t_open_participant bool not null default false,
   t_allowed_presenters varchar(32) default 'EVERYONE',
   t_access_level varchar(32) default 'EVERYONE',
   t_entry_exit_announcement bool default true,
   t_lobby_bypass_scope varchar(32) default 'ORGANIZATION_AND_FEDERATED',
   fk_entry_id bigint default null,
   a_sub_ident varchar(64) default null,
   fk_group_id bigint default null,
   fk_creator_id bigint default null,
   primary key (id)
);

create table o_teams_user (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   t_identifier varchar(128),
   t_displayname varchar(512),
   fk_identity_id bigint default null,
   unique(fk_identity_id),
   primary key (id)
);

create table o_teams_attendee (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   t_role varchar(32),
   t_join_date datetime not null,
   fk_identity_id bigint default null,
   fk_teams_user_id bigint default null,
   fk_meeting_id bigint not null,
   primary key (id)

);

-- assessment tables
-- efficiency statments
create table if not exists o_as_eff_statement (
   id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime,
   lastcoachmodified datetime,
   lastusermodified datetime,
   creationdate datetime,
   passed bit default null,
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

create table o_as_entry (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   lastcoachmodified datetime,
   lastusermodified datetime,
   a_attemtps bigint default null,
   a_last_attempt datetime null,
   a_score float(65,30) default null,
   a_passed bit default null,
   a_passed_original bit,
   a_passed_mod_date datetime,
   a_status varchar(16) default null,
   a_date_done datetime,
   a_details varchar(1024) default null,
   a_user_visibility bit default 1,
   a_fully_assessed bit default null,
   a_date_fully_assessed datetime,
   a_assessment_id bigint default null,
   a_completion float(65,30),
   a_current_run_completion float(65,30),
   a_current_run_status varchar(16),
   a_current_run_start datetime,
   a_comment text,
   a_coach_comment text,
   a_num_assessment_docs bigint not null default 0,
   a_date_start datetime,
   a_date_end datetime,
   a_date_end_original datetime,
   a_date_end_mod_date datetime,
   a_duration int8,
   a_obligation varchar(50),
   a_obligation_original varchar(50),
   a_obligation_mod_date datetime,
   a_first_visit datetime,
   a_last_visit datetime,
   a_num_visits int8,
   fk_entry bigint not null,
   a_subident varchar(512),
   a_entry_root bit default null,
   fk_reference_entry bigint,
   fk_identity bigint default null,
   fk_identity_passed_mod bigint,
   fk_identity_end_date_mod bigint,
   fk_identity_obligation_mod bigint,
   a_anon_identifier varchar(128) default null,
   primary key (id),
   unique (fk_identity, fk_entry, a_subident)
);

create table o_as_compensation (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   a_subident varchar(512),
   a_subident_name varchar(512),
   a_extra_time bigint not null,
   a_approved_by varchar(2000),
   a_approval timestamp,
   a_status varchar(32),
   fk_identity bigint not null,
   fk_creator bigint not null,
   fk_entry bigint not null,
   primary key (id)
);

create table o_as_compensation_log (
   id bigint not null auto_increment,
   creationdate datetime not null,
   a_action varchar(32) not null,
   a_val_before mediumtext,
   a_val_after mediumtext,
   a_subident varchar(512),
   fk_entry_id bigint not null,
   fk_identity_id bigint not null,
   fk_compensation_id bigint not null,
   fk_author_id bigint,
   primary key (id)
);

create table o_as_mode_course (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   a_name varchar(255),
   a_description longtext,
   a_status varchar(16),
   a_end_status varchar(32),
   a_manual_beginend bit not null default 0,
   a_begin datetime not null,
   a_leadtime bigint not null default 0,
   a_begin_with_leadtime datetime not null,
   a_end datetime not null,
   a_followuptime bigint not null default 0,
   a_end_with_followuptime datetime not null,
   a_targetaudience varchar(16),
   a_restrictaccesselements bit not null default 0,
   a_elements varchar(2048),
   a_start_element varchar(64),
   a_restrictaccessips bit not null default 0,
   a_ips varchar(2048),
   a_safeexambrowser bit not null default 0,
   a_safeexambrowserkey varchar(2048),
   a_safeexambrowserhint longtext,
   a_applysettingscoach bit not null default 0,
   fk_entry bigint not null,
   fk_lecture_block bigint,
   primary key (id)
);

create table o_as_mode_course_to_group (
   id bigint not null,
   fk_assessment_mode_id bigint not null,
   fk_group_id bigint not null,
   primary key (id)
);

create table o_as_mode_course_to_area (
   id bigint not null,
   fk_assessment_mode_id bigint not null,
   fk_area_id bigint not null,
   primary key (id)
);

create table o_as_mode_course_to_cur_el (
   id bigint not null auto_increment,
   fk_assessment_mode_id bigint not null,
   fk_cur_element_id bigint not null,
   primary key (id)
);

create table o_cer_template (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   c_name varchar(256) not null,
   c_path varchar(1024) not null,
   c_public boolean not null,
   c_format varchar(16),
   c_orientation varchar(16),
   primary key (id)
);

create table o_cer_certificate (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   c_status varchar(16) not null default 'pending',
   c_email_status varchar(16),
   c_uuid varchar(36) not null,
   c_next_recertification datetime,
   c_path varchar(1024),
   c_last boolean not null default 1,
   c_course_title varchar(255),
   c_archived_resource_id bigint not null,
   fk_olatresource bigint,
   fk_identity bigint not null,
   primary key (id)
);

create table o_goto_organizer (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_name varchar(128) default null,
   g_account_key varchar(128) default null,
   g_access_token varchar(4000) not null,
   g_renew_date datetime not null,
   g_refresh_token varchar(4000),
   g_renew_refresh_date datetime,
   g_organizer_key varchar(128) not null,
   g_username varchar(128) not null,
   g_firstname varchar(128) default null,
   g_lastname varchar(128) default null,
   g_email varchar(128) default null,
   fk_identity bigint default null,
   primary key (id)
);

create table o_goto_meeting (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_external_id varchar(128) default null,
   g_type varchar(16) not null,
   g_meeting_key varchar(128) not null,
   g_name varchar(255) default null,
   g_description varchar(2000) default null,
   g_start_date datetime default null,
   g_end_date datetime default null,
   fk_organizer_id bigint not null,
   fk_entry_id bigint default null,
   g_sub_ident varchar(64) default null,
   fk_group_id bigint default null,
   primary key (id)
);

create table o_goto_registrant (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_status varchar(16) default null,
   g_join_url varchar(1024) default null,
   g_confirm_url varchar(1024) default null,
   g_registrant_key varchar(64) default null,
   fk_meeting_id bigint not null,
   fk_identity_id bigint not null,
   primary key (id)
);

create table o_vid_transcoding (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   vid_resolution bigint default null,
   vid_width bigint default null,
   vid_height bigint default null,
   vid_size bigint default null,
   vid_format varchar(128) default null,
   vid_status bigint default null,
   vid_transcoder varchar(128) default null,
   fk_resource_id bigint not null,
   primary key (id)
);

create table o_vid_metadata (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  vid_width bigint default null,
  vid_height bigint default null,
  vid_size bigint default null,
  vid_format varchar(32) default null,
  vid_length varchar(32) default null,
  vid_url varchar(512) default null,
  fk_resource_id bigint not null,
  primary key (id)
);

-- calendar
create table o_cal_use_config (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   c_calendar_id varchar(128) not null,
   c_calendar_type varchar(16) not null,
   c_token varchar(36),
   c_cssclass varchar(36),
   c_visible bit not null default 1,
   c_aggregated_feed bit not null default 1,
   fk_identity bigint not null,
   primary key (id),
   unique (c_calendar_id, c_calendar_type, fk_identity)
);

create table o_cal_import (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   c_calendar_id varchar(128) not null,
   c_calendar_type varchar(16) not null,
   c_displayname varchar(256),
   c_lastupdate datetime not null,
   c_url varchar(1024),
   fk_identity bigint,
   primary key (id)
);

create table o_cal_import_to (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   c_to_calendar_id varchar(128) not null,
   c_to_calendar_type varchar(16) not null,
   c_lastupdate datetime not null,
   c_url varchar(1024),
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
   id bigint not null,
   lastmodified datetime,
   creationdate datetime,
   expirationdate datetime,
   mapper_uuid varchar(64),
   orig_session_id varchar(64),
   xml_config text,
   primary key (id)
);

-- qti 2.1
create table o_qti_assessmenttest_session (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_exploded bit not null default 0,
   q_cancelled bit not null default 0,
   q_author_mode bit not null default 0,
   q_finish_time datetime,
   q_termination_time datetime,
   q_duration bigint,
   q_score float(65,30) default null,
   q_manual_score float(65,30) default null,
   q_max_score float(65,30) default null,
   q_passed bit default null,
   q_num_questions bigint,
   q_num_answered_questions bigint,
   q_extra_time bigint,
   q_compensation_extra_time bigint,
   q_storage varchar(1024),
   fk_reference_entry bigint not null,
   fk_entry bigint,
   q_subident varchar(255),
   fk_identity bigint default null,
   q_anon_identifier varchar(128) default null,
   fk_assessment_entry bigint not null,
   primary key (id)
);

create table o_qti_assessmentitem_session (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_itemidentifier varchar(255) not null,
   q_sectionidentifier varchar(255) default null,
   q_testpartidentifier varchar(255) default null,
   q_duration bigint,
   q_score float(65,30) default null,
   q_manual_score float(65,30) default null,
   q_coach_comment mediumtext default null,
   q_to_review bit default 0,
   q_passed bit default null,
   q_storage varchar(1024),
   fk_assessmenttest_session bigint not null,
   primary key (id)
);

create table o_qti_assessment_response (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_responseidentifier varchar(255) not null,
   q_responsedatatype varchar(16) not null,
   q_responselegality varchar(16) not null,
   q_stringuifiedresponse mediumtext,
   fk_assessmentitem_session bigint not null,
   fk_assessmenttest_session bigint not null,
   primary key (id)
);

create table o_qti_assessment_marks (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_marks mediumtext default null,
   q_hidden_rubrics mediumtext default null,
   fk_reference_entry bigint not null,
   fk_entry bigint,
   q_subident varchar(64),
   fk_identity bigint not null,
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
   q_topic varchar(1024),
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
   q_correction_time bigint default null,
   q_assessment_type varchar(64),
   q_status varchar(32) not null,
   q_version varchar(50),
   fk_license bigint,
   q_editor varchar(256),
   q_editor_version varchar(256),
   q_format varchar(32) not null,
   q_creator varchar(1024),
   creationdate datetime not null,
   lastmodified datetime not null,
   q_status_last_modified datetime not null,
   q_dir varchar(32),
   q_root_filename varchar(255),
   fk_taxonomy_level bigint,
   fk_taxonomy_level_v2 bigint,
   fk_ownergroup bigint not null,
   primary key (id)
);

create table o_qp_item_audit_log (
  id bigint not null auto_increment,
  creationdate datetime not null,
  q_action varchar(64),
  q_val_before mediumtext,
  q_val_after mediumtext,
  q_lic_before mediumtext,
  q_lic_after mediumtext,
  q_message mediumtext,
  fk_author_id bigint,
  fk_item_id bigint,
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

-- vfs metadata
create table o_vfs_metadata (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   f_uuid varchar(64) not null,
   f_deleted boolean default 0 not null,
   f_filename varchar(256) not null,
   f_relative_path varchar(2048) not null,
   f_directory bool default false,
   f_lastmodified datetime not null,
   f_size bigint default 0,
   f_uri varchar(2000) not null,
   f_uri_protocol varchar(16) not null,
   f_cannot_thumbnails bool default false,
   f_download_count bigint default 0,
   f_comment text(32000),
   f_title varchar(2000),
   f_publisher varchar(2000),
   f_creator varchar(2000),
   f_source varchar(2000),
   f_city varchar(256),
   f_pages varchar(16),
   f_language varchar(16),
   f_url text(2000),
   f_pub_month varchar(16),
   f_pub_year varchar(16),
   f_license_type_name varchar(256),
   f_license_text mediumtext,
   f_licensor text(4000),
   f_locked_date timestamp,
   f_locked bool default false,
   f_migrated varchar(12),
   f_m_path_keys varchar(1024),
   fk_locked_identity bigint,
   f_revision_nr bigint default 0 not null,
   f_revision_comment text(32000),
   fk_license_type bigint,
   fk_initialized_by bigint,
   fk_lastmodified_by bigint,
   fk_parent bigint,
   primary key (id)
);

create table o_vfs_thumbnail (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   f_size bigint default 0 not null,
   f_max_width bigint default 0 not null,
   f_max_height bigint default 0 not null,
   f_final_width bigint default 0 not null,
   f_final_height bigint default 0 not null,
   f_fill bool default false not null,
   f_filename varchar(256) not null,
   fk_metadata bigint not null,
   primary key (id)
);

create table o_vfs_revision (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   f_revision_size bigint default 0 not null,
   f_revision_nr bigint default 0 not null,
   f_revision_filename varchar(256) not null,
   f_revision_comment text(32000),
   f_revision_lastmodified datetime not null,
   f_comment text(32000),
   f_title varchar(2000),
   f_publisher varchar(2000),
   f_creator varchar(2000),
   f_source varchar(2000),
   f_city varchar(256),
   f_pages varchar(16),
   f_language varchar(16),
   f_url text(2048),
   f_pub_month varchar(16),
   f_pub_year varchar(16),
   f_license_type_name varchar(256),
   f_license_text mediumtext,
   f_licensor text(4000),
   fk_license_type bigint,
   fk_initialized_by bigint,
   fk_lastmodified_by bigint,
   fk_metadata bigint not null,
   primary key (id)
);

-- Document editor
create table o_de_access (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   o_editor_type varchar(64) not null,
   o_expires_at datetime not null,
   o_mode varchar(64) not null,
   o_version_controlled bool not null,
   o_download bool default true,
   fk_metadata bigint not null,
   fk_identity bigint not null,
   primary key (id)
);

-- used in fxOpenOlat
create table o_de_user_info (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   o_info varchar(2048) not null,
   fk_identity bigint not null,
   primary key (id)
);

-- portfolio
create table o_pf_binder (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_title varchar(255),
   p_status varchar(32),
   p_copy_date datetime,
   p_return_date datetime,
   p_deadline datetime,
   p_summary mediumtext,
   p_image_path varchar(255),
   fk_olatresource_id bigint,
   fk_group_id bigint not null,
   fk_entry_id bigint,
   p_subident varchar(128),
   fk_template_id bigint,
   primary key (id)
);

create table o_pf_section (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   pos bigint default null,
   p_title varchar(255),
   p_description mediumtext,
   p_status varchar(32) not null default 'notStarted',
   p_begin datetime,
   p_end datetime,
   p_override_begin_end bit default 0,
   fk_group_id bigint not null,
   fk_binder_id bigint not null,
   fk_template_reference_id bigint,
   primary key (id)
);

create table o_pf_page (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   pos bigint default null,
   p_editable bit default 1,
   p_title varchar(255),
   p_summary mediumtext,
   p_status varchar(32),
   p_image_path varchar(255),
   p_image_align varchar(32),
   p_version bigint default 0,
   p_initial_publish_date datetime,
   p_last_publish_date datetime,
   fk_body_id bigint not null,
   fk_group_id bigint not null,
   fk_section_id bigint,
   primary key (id)
);

create table o_pf_page_body (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_usage bigint default 0,
   p_synthetic_status varchar(32),
   primary key (id)
);

create table o_pf_media (
   id bigint not null auto_increment,
   creationdate datetime not null,
   p_collection_date datetime not null,
   p_type varchar(64) not null,
   p_storage_path varchar(255),
   p_root_filename varchar(255),
   p_title varchar(255) not null,
   p_description mediumtext,
   p_content mediumtext,
   p_signature bigint not null default 0,
   p_reference_id varchar(255) default null,
   p_business_path varchar(255) default null,
   p_creators varchar(1024) default null,
   p_place varchar(255) default null,
   p_publisher varchar(255) default null,
   p_publication_date datetime default null,
   p_date varchar(32) default null,
   p_url varchar(1024) default null,
   p_source varchar(1024) default null,
   p_language varchar(32) default null,
   p_metadata_xml mediumtext,
   fk_author_id bigint not null,
   primary key (id)
);

create table o_pf_page_part (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   pos bigint default null,
   dtype varchar(32),
   p_content mediumtext,
   p_flow varchar(32),
   p_layout_options mediumtext,
   fk_media_id bigint,
   fk_page_body_id bigint,
   fk_form_entry_id bigint default null,
   primary key (id)
);

create table o_pf_category (
   id bigint not null auto_increment,
   creationdate datetime not null,
   p_name varchar(32),
   primary key (id)
);

create table o_pf_category_relation (
   id bigint not null auto_increment,
   creationdate datetime not null,
   p_resname varchar(64) not null,
   p_resid bigint not null,
   fk_category_id bigint not null,
   primary key (id)
);

create table o_pf_assessment_section (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_score float(65,30) default null,
   p_passed bit default null,
   p_comment mediumtext,
   fk_section_id bigint not null,
   fk_identity_id bigint not null,
   primary key (id)
);

create table o_pf_assignment (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   pos bigint default null,
   p_status varchar(32) default null,
   p_type varchar(32) not null,
   p_version bigint not null default 0,
   p_template bit default 0,
   p_title varchar(255) default null,
   p_summary mediumtext,
   p_content mediumtext,
   p_storage varchar(255) default null,
   fk_section_id bigint,
   fk_binder_id bigint,
   fk_template_reference_id bigint,
   fk_page_id bigint,
   fk_assignee_id bigint,
   p_only_auto_eva bit default 1,
   p_reviewer_see_auto_eva bit default 0,
   p_anon_extern_eva bit default 1,
   fk_form_entry_id bigint default null,
   primary key (id)
);

create table o_pf_binder_user_infos (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_initiallaunchdate datetime,
   p_recentlaunchdate datetime,
   p_visit bigint,
   fk_identity bigint,
   fk_binder bigint,
   unique(fk_identity, fk_binder),
   primary key (id)
);

create table o_pf_page_user_infos (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  p_mark bit default 0,
  p_status varchar(16) not null default 'incoming',
  p_recentlaunchdate datetime not null,
  fk_identity_id bigint not null,
  fk_page_id bigint not null,
  primary key (id)
);

-- evaluation form
create table o_eva_form_survey (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   e_resname varchar(50) not null,
   e_resid bigint not null,
   e_sub_ident varchar(2048),
   e_sub_ident2 varchar(2048),
   e_series_key bigint,
   e_series_index int,
   fk_form_entry bigint not null,
   fk_series_previous bigint,
   primary key (id)
);

create table o_eva_form_participation (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   e_identifier_type varchar(50) not null,
   e_identifier_key varchar(50) not null,
   e_status varchar(20) not null,
   e_anonymous bit not null,
   fk_executor bigint,
   fk_survey bigint,
   primary key (id)
);

create table o_eva_form_session (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   e_status varchar(16),
   e_submission_date datetime,
   e_first_submission_date datetime,
   e_email varchar(1024),
   e_firstname varchar(1024),
   e_lastname varchar(1024),
   e_age varchar(1024),
   e_gender varchar(1024),
   e_org_unit varchar(1024),
   e_study_subject varchar(1024),
   fk_survey bigint,
   fk_participation bigint unique,
   fk_identity bigint,
   fk_page_body bigint,
   fk_form_entry bigint,
   primary key (id)
);

create table o_eva_form_response (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   e_no_response bit default 0,
   e_responseidentifier varchar(64) not null,
   e_numericalresponse decimal(65,10) default null,
   e_stringuifiedresponse mediumtext,
   e_file_response_path varchar(4000),
   fk_session bigint not null,
   primary key (id)
);

-- quality management
create table o_qual_data_collection (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_status varchar(50),
   q_title varchar(200),
   q_start datetime,
   q_deadline datetime,
   q_topic_type varchar(50),
   q_topic_custom varchar(200),
   q_topic_fk_identity bigint,
   q_topic_fk_organisation bigint,
   q_topic_fk_curriculum bigint,
   q_topic_fk_curriculum_element bigint,
   q_topic_fk_repository bigint,
   fk_generator bigint,
   q_generator_provider_key bigint,
   primary key (id)
);

create table o_qual_data_collection_to_org (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_data_collection bigint not null,
   fk_organisation bigint not null,
   primary key (id)
);

create table o_qual_context (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_role varchar(20),
   q_location varchar(1024),
   fk_data_collection bigint not null,
   fk_eva_participation bigint,
   fk_eva_session bigint,
   fk_audience_repository bigint,
   fk_audience_cur_element bigint,
   primary key (id)
);

create table o_qual_context_to_organisation (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_context bigint not null,
   fk_organisation bigint not null,
   primary key (id)
);

create table o_qual_context_to_curriculum (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_context bigint not null,
   fk_curriculum bigint not null,
   primary key (id)
);

create table o_qual_context_to_cur_element (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_context bigint not null,
   fk_cur_element bigint not null,
   primary key (id)
);

create table o_qual_context_to_tax_level (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_context bigint not null,
   fk_tax_leveL bigint not null,
   primary key (id)
);

create table o_qual_reminder (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_type varchar(65) not null,
   q_send_planed datetime,
   q_send_done datetime,
   fk_data_collection bigint not null,
   primary key (id)
);

create table o_qual_report_access (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  q_type varchar(64),
  q_role varchar(64),
  q_online bit default 0,
  q_email_trigger varchar(64),
  fk_data_collection bigint,
  fk_generator bigint,
  fk_group bigint,
  primary key (id)
);

create table o_qual_generator (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_title varchar(256),
   q_type varchar(64) not null,
   q_enabled bit not null,
   q_last_run datetime,
   fk_form_entry bigint,
   primary key (id)
);

create table o_qual_generator_config (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_identifier varchar(50) not null,
   q_value mediumtext,
   fk_generator bigint not null,
   primary key (id)
);

create table o_qual_generator_to_org (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_generator bigint not null,
   fk_organisation bigint not null,
   primary key (id)
);

create table o_qual_analysis_presentation (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_name varchar(256),
   q_analysis_segment varchar(100),
   q_search_params text,
   q_heatmap_grouping text,
   q_heatmap_insufficient_only boolean default false,
   q_temporal_grouping varchar(50),
   q_trend_difference varchar(50),
   q_rubric_id varchar(50),
   fk_form_entry bigint not null,
   primary key (id)
);

-- lti
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

create table o_cl_checkbox (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   c_checkboxid varchar(50) not null,
   c_resname varchar(50) not null,
   c_resid bigint not null,
   c_ressubpath varchar(255) not null,
   primary key (id)
);

create table o_cl_check (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   c_score float(65,30),
   c_checked bit default null,
   fk_identity_id bigint not null,
   fk_checkbox_id bigint not null,
   primary key (id)
);

create table o_gta_task_list (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_course_node_ident varchar(36),
   fk_entry bigint not null,
   primary key (id)
);

create table o_gta_task (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_status varchar(36),
   g_rev_loop mediumint not null default 0,
   g_assignment_date datetime,
   g_submission_date datetime,
   g_submission_ndocs bigint,
   g_submission_revisions_date datetime,
   g_submission_revisions_ndocs bigint,
   g_collection_date datetime,
   g_collection_ndocs bigint,
   g_acceptation_date datetime,
   g_solution_date datetime,
   g_graduation_date datetime,
   g_allow_reset_date datetime,
   g_assignment_due_date datetime,
   g_submission_due_date datetime,
   g_revisions_due_date datetime,
   g_solution_due_date datetime,
   g_taskname varchar(1024),
   fk_tasklist bigint not null,
   fk_identity bigint,
   fk_businessgroup bigint,
   fk_allow_reset_identity bigint,
   primary key (id)
);

create table o_gta_task_revision (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_status varchar(36) not null,
   g_rev_loop mediumint not null default 0,
   g_date timestamp,
   g_rev_comment mediumtext,
   g_rev_comment_lastmodified datetime,
   fk_task bigint not null,
   fk_comment_author bigint,
   primary key (id)
);

create table o_gta_task_revision_date (
  id bigint not null auto_increment,
  creationdate datetime not null,
  g_status varchar(36) not null,
  g_rev_loop bigint not null,
  g_date datetime not null,
  fk_task bigint not null,
  primary key (id)
);

create table o_gta_mark (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  fk_tasklist_id int8 not null,
  fk_marker_identity_id int8 not null,
  fk_participant_identity_id int8 not null,
  primary key (id)
);

create table o_rem_reminder (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   r_description varchar(255),
   r_start datetime,
   r_sendtime varchar(16),
   r_configuration mediumtext,
   r_email_subject varchar(255),
   r_email_body mediumtext,
   fk_creator bigint not null,
   fk_entry bigint not null,
   primary key (id)
);

create table o_rem_sent_reminder (
   id bigint not null,
   creationdate datetime not null,
   r_status varchar(16),
   fk_identity bigint not null,
   fk_reminder bigint not null,
   primary key (id)
);

create table o_ex_task (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   e_name varchar(255) not null,
   e_status varchar(16) not null,
   e_status_before_edit varchar(16),
   e_executor_node varchar(16),
   e_executor_boot_id varchar(64),
   e_task mediumtext not null,
   e_scheduled datetime,
   e_ressubpath varchar(2048),
   fk_resource_id bigint,
   fk_identity_id bigint,
   primary key (id)
);

create table o_ex_task_modifier (
   id bigint not null,
   creationdate datetime not null,
   fk_task_id bigint not null,
   fk_identity_id bigint not null,
   primary key (id)
);

-- sms
create table o_sms_message_log (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   s_message_uuid varchar(256) not null,
   s_server_response varchar(256),
   s_service_id varchar(32) not null,
   fk_identity bigint not null,
   primary key (id)
);

-- webfeed
create table o_feed (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   f_resourceable_id bigint,
   f_resourceable_type varchar(64),
   f_title varchar(1024),
   f_description mediumtext,
   f_author varchar(255),
   f_image_name varchar(1024),
   f_external boolean,
   f_external_feed_url varchar(4000),
   f_external_image_url varchar(4000),
   primary key (id)
);

create table o_feed_item (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   f_title varchar(1024),
   f_description mediumtext,
   f_content mediumtext,
   f_author varchar(255),
   f_guid varchar(255),
   f_external_link varchar(4000),
   f_draft boolean,
   f_publish_date datetime,
   f_width bigint,
   f_height bigint,
   f_filename varchar(1024),
   f_type varchar(255),
   f_length bigint,
   f_external_url varchar(4000),
   fk_feed_id bigint not null,
   fk_identity_author_id bigint,
   fk_identity_modified_id bigint,
   primary key (id)
);

-- lectures
create table o_lecture_reason (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  l_enabled bool default true not null,
  l_title varchar(255),
  l_descr varchar(2000),
  primary key (id)
);

create table o_lecture_absence_category (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   l_enabled bool default true not null,
   l_title varchar(255),
   l_descr mediumtext,
   primary key (id)
);

create table o_lecture_absence_notice (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   l_type varchar(32),
   l_absence_reason mediumtext,
   l_absence_authorized bit default null,
   l_start_date datetime not null,
   l_end_date datetime not null,
   l_target varchar(32) default 'allentries' not null,
   l_attachments_dir varchar(255),
   fk_identity bigint not null,
   fk_notifier bigint,
   fk_authorizer bigint,
   fk_absence_category bigint,
   primary key (id)
);

create table o_lecture_block (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  l_external_id varchar(255),
  l_managed_flags varchar(255),
  l_title varchar(255),
  l_descr mediumtext,
  l_preparation mediumtext,
  l_location varchar(255),
  l_comment mediumtext,
  l_start_date datetime not null,
  l_end_date datetime not null,
  l_compulsory bit default 1,
  l_eff_end_date datetime,
  l_planned_lectures_num bigint not null default 0,
  l_effective_lectures_num bigint not null default 0,
  l_effective_lectures varchar(128),
  l_auto_close_date datetime default null,
  l_status varchar(16) not null,
  l_roll_call_status varchar(16) not null,
  fk_reason bigint,
  fk_entry bigint not null,
  fk_teacher_group bigint not null,
  primary key (id)
);

create table o_lecture_block_to_group (
  id bigint not null auto_increment,
  fk_lecture_block bigint not null,
  fk_group bigint not null,
  primary key (id)
);

create table o_lecture_notice_to_block (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_lecture_block bigint not null,
   fk_absence_notice bigint not null,
   primary key (id)
);

create table o_lecture_notice_to_entry (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_entry bigint not null,
   fk_absence_notice bigint not null,
   primary key (id)
);

create table o_lecture_block_roll_call (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  l_comment mediumtext,
  l_lectures_attended varchar(128),
  l_lectures_absent varchar(128),
  l_lectures_attended_num bigint not null default 0,
  l_lectures_absent_num bigint not null default 0,
  l_absence_notice_lectures varchar(128),
  l_absence_reason mediumtext,
  l_absence_authorized bit default null,
  l_absence_appeal_date datetime,
  l_absence_supervisor_noti_date datetime,
  l_appeal_reason mediumtext,
  l_appeal_status mediumtext,
  l_appeal_status_reason mediumtext,
  fk_lecture_block bigint not null,
  fk_identity bigint not null,
  fk_absence_category bigint,
  fk_absence_notice bigint,
  primary key (id)
);

create table o_lecture_reminder (
  id bigint not null auto_increment,
  creationdate datetime not null,
  l_status varchar(16) not null,
  fk_lecture_block bigint not null,
  fk_identity bigint not null,
  primary key (id)
);

create table o_lecture_participant_summary (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  l_first_admission_date datetime default null,
  l_required_attendance_rate float(65,30) default null,
  l_attended_lectures bigint not null default 0,
  l_absent_lectures bigint not null default 0,
  l_excused_lectures bigint not null default 0,
  l_planneds_lectures bigint not null default 0,
  l_attendance_rate float(65,30) default null,
  l_cal_sync bit default 0,
  l_cal_last_sync_date datetime default null,
  fk_entry bigint not null,
  fk_identity bigint not null,
  primary key (id),
  unique (fk_entry, fk_identity)
);

create table o_lecture_entry_config (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  l_lecture_enabled bit default null,
  l_override_module_def bit default 0,
  l_rollcall_enabled bit default null,
  l_calculate_attendance_rate bit default null,
  l_required_attendance_rate float(65,30) default null,
  l_sync_calendar_teacher bit default null,
  l_sync_calendar_participant bit default null,
  l_sync_calendar_course bit default null,
  l_assessment_mode bool default null,
  l_assessment_mode_lead bigint default null,
  l_assessment_mode_followup bigint default null,
  l_assessment_mode_ips varchar(2048),
  l_assessment_mode_seb varchar(2048),
  fk_entry bigint not null,
  unique(fk_entry),
  primary key (id)
);

create table o_lecture_block_audit_log (
  id bigint not null auto_increment,
  creationdate datetime not null,
  l_action varchar(32),
  l_val_before mediumtext,
  l_val_after mediumtext,
  l_message mediumtext,
  fk_lecture_block bigint,
  fk_roll_call bigint,
  fk_absence_notice bigint,
  fk_entry bigint,
  fk_identity bigint,
  fk_author bigint,
  primary key (id)
);

create table o_lecture_block_to_tax_level (
  id bigint not null auto_increment,
  creationdate datetime not null,
  fk_lecture_block bigint not null,
  fk_taxonomy_level bigint not null,
  primary key (id)
);

-- taxonomy
create table o_tax_taxonomy (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  t_identifier varchar(64),
  t_displayname varchar(255) not null,
  t_description mediumtext,
  t_external_id varchar(64),
  t_managed_flags varchar(255),
  t_directory_path varchar(255),
  t_directory_lost_found_path varchar(255),
  fk_group bigint not null,
  primary key (id)
);

create table o_tax_taxonomy_level_type (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  t_identifier varchar(64),
  t_displayname varchar(255) not null,
  t_description mediumtext,
  t_external_id varchar(64),
  t_managed_flags varchar(255),
  t_css_class varchar(64),
  t_visible bit default 1,
  t_library_docs bit default 1,
  t_library_manage bit default 1,
  t_library_teach_read bit default 1,
  t_library_teach_readlevels bigint not null default 0,
  t_library_teach_write bit default 0,
  t_library_have_read bit default 1,
  t_library_target_read bit default 1,
  fk_taxonomy bigint not null,
  primary key (id)
);

create table o_tax_taxonomy_type_to_type (
  id bigint not null auto_increment,
  fk_type bigint not null,
  fk_allowed_sub_type bigint not null,
  primary key (id)
);

create table o_tax_taxonomy_level (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  t_identifier varchar(64),
  t_displayname varchar(255) not null,
  t_description mediumtext,
  t_external_id varchar(64),
  t_sort_order bigint,
  t_directory_path varchar(255),
  t_m_path_keys varchar(255),
  t_m_path_identifiers varchar(1024),
  t_enabled bit default 1,
  t_managed_flags varchar(255),
  fk_taxonomy bigint not null,
  fk_parent bigint,
  fk_type bigint,
  primary key (id)
);

create table o_tax_taxonomy_competence (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  t_type varchar(16),
  t_achievement float(65,30) default null,
  t_reliability float(65,30) default null,
  t_expiration_date datetime,
  t_external_id varchar(64),
  t_source_text varchar(255),
  t_source_url varchar(255),
  fk_level bigint not null,
  fk_identity bigint not null,
  primary key (id)
);

create table o_tax_competence_audit_log (
  id bigint not null auto_increment,
  creationdate datetime not null,
  t_action varchar(32),
  t_val_before mediumtext,
  t_val_after mediumtext,
  t_message mediumtext,
  fk_taxonomy bigint,
  fk_taxonomy_competence bigint,
  fk_identity bigint,
  fk_author bigint,
  primary key (id)
);

-- dialog elements
create table o_dialog_element (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  d_filename varchar(2048),
  d_filesize bigint,
  d_subident varchar(64) not null,
  fk_author bigint,
  fk_entry bigint not null,
  fk_forum bigint not null,
  primary key (id)
);

-- licenses
create table o_lic_license_type (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  l_name varchar(128) not null unique,
  l_text mediumtext,
  l_css_class varchar(64),
  l_predefined boolean not null default false,
  l_sort_order int not null,
  primary key (id)
);

create table o_lic_license_type_activation (
  id bigint not null auto_increment,
  creationdate timestamp not null,
  l_handler_type varchar(128) not null,
  fk_license_type_id bigint not null,
  primary key (id)
);

create table o_lic_license (
  id bigint not null auto_increment,
  creationdate timestamp not null,
  lastmodified datetime not null,
  l_resname varchar(50) not null,
  l_resid bigint not null,
  l_licensor varchar(4000),
  l_freetext mediumtext,
  fk_license_type_id bigint not null,
  primary key (id)
);

-- organisation
create table o_org_organisation_type (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  o_identifier varchar(64),
  o_displayname varchar(255) not null,
  o_description mediumtext,
  o_external_id varchar(64),
  o_managed_flags varchar(255),
  o_css_class varchar(64),
  primary key (id)
);

create table o_org_organisation (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  o_identifier varchar(64),
  o_displayname varchar(255) not null,
  o_description mediumtext,
  o_m_path_keys varchar(255),
  o_external_id varchar(64),
  o_managed_flags varchar(255),
  o_status varchar(32),
  o_css_class varchar(64),
  fk_group bigint not null,
  fk_root bigint,
  fk_parent bigint,
  fk_type bigint,
  primary key (id)
);

create table o_org_type_to_type (
  id bigint not null auto_increment,
  fk_type bigint not null,
  fk_allowed_sub_type bigint not null,
  primary key (id)
);

create table o_re_to_organisation (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  r_master bit default 0,
  fk_entry bigint not null,
  fk_organisation bigint not null,
  primary key (id)
);

-- curriculum
create table o_cur_element_type (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description mediumtext,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  c_calendars varchar(16),
  c_lectures varchar(16),
  c_learning_progress varchar(16),
  c_css_class varchar(64),
  primary key (id)
);

create table o_cur_curriculum (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description mediumtext,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  c_status varchar(32),
  c_degree varchar(255),
  fk_group bigint not null,
  fk_organisation bigint,
  primary key (id)
);

create table o_cur_curriculum_element (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  pos bigint,
  pos_cur bigint,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description mediumtext,
  c_status varchar(32),
  c_begin datetime,
  c_end datetime,
  c_external_id varchar(64),
  c_m_path_keys varchar(255),
  c_managed_flags varchar(255),
  c_calendars varchar(16),
  c_lectures varchar(16),
  c_learning_progress varchar(16),
  fk_group bigint not null,
  fk_parent bigint,
  fk_curriculum bigint not null,
  fk_curriculum_parent bigint,
  fk_type bigint,
  primary key (id)
);

create table o_cur_element_type_to_type (
  id bigint not null auto_increment,
  fk_type bigint not null,
  fk_allowed_sub_type bigint not null,
  primary key (id)
);

create table o_cur_element_to_tax_level (
  id bigint not null auto_increment,
  creationdate datetime not null,
  fk_cur_element bigint not null,
  fk_taxonomy_level bigint not null,
  primary key (id)
);

-- edu-sharing
create table o_es_usage (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   e_identifier varchar(64) not null,
   e_resname varchar(50) not null,
   e_resid bigint not null,
   e_sub_path varchar(256),
   e_object_url varchar(255) not null,
   e_version varchar(64),
   e_mime_type varchar(128),
   e_media_type varchar(128),
   e_width varchar(8),
   e_height varchar(8),
   fk_identity bigint not null,
   primary key (id)
);

-- livestream
create table o_livestream_launch (
   id bigint not null auto_increment,
   creationdate datetime not null,
   l_launch_date datetime not null,
   fk_entry bigint not null,
   l_subident varchar(128) not null,
   fk_identity bigint not null,
   primary key (id)
);
-- Livestream
create table o_livestream_url_template (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   l_name varchar(64) not null,
   l_url1 varchar(2048),
   l_url2 varchar(2048),
   primary key (id)
);

-- grading
create table o_grad_to_identity (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_status varchar(16) default 'activated' not null,
   fk_identity bigint not null,
   fk_entry bigint not null,
   primary key (id)
);

create table o_grad_assignment (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_status varchar(16) default 'unassigned' not null,
   g_assessment_date datetime,
   g_assignment_date datetime,
   g_assignment_notification datetime,
   g_reminder_1 datetime,
   g_reminder_2 datetime,
   g_deadline datetime,
   g_extended_deadline datetime,
   g_closed datetime,
   fk_reference_entry bigint not null,
   fk_assessment_entry bigint not null,
   fk_grader bigint,
   primary key (id)
);

create table o_grad_time_record (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_time bigint default 0 not null,
   g_metadata_time bigint default 0 not null,   
   g_date_record date not null,
   fk_assignment bigint,
   fk_grader bigint not null,
   primary key (id)
);

create table o_grad_configuration (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_grading_enabled bool not null default false,
   g_identity_visibility varchar(32) default 'anonymous' not null,
   g_grading_period bigint,
   g_notification_type varchar(32) default 'afterTestSubmission' not null,
   g_notification_subject varchar(255),
   g_notification_body mediumtext,
   g_first_reminder bigint,
   g_first_reminder_subject varchar(255),
   g_first_reminder_body mediumtext,
   g_second_reminder bigint,
   g_second_reminder_subject varchar(255),
   g_second_reminder_body mediumtext,
   fk_entry bigint not null,
   primary key (id)
);

-- course disclaimer
create table o_course_disclaimer_consent(
    id bigint not null auto_increment,
    disc_1_accepted boolean not null,
    disc_2_accepted boolean not null,
    creationdate datetime not null,
    lastmodified datetime not null,
    fk_repository_entry bigint not null,
    fk_identity bigint not null,
    primary key (id)
);

-- Appointments
create table o_ap_topic (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   a_title varchar(256),
   a_description varchar(4000),
   a_type varchar(64) not null,
   a_multi_participation bool default true not null,
   a_auto_confirmation bool default false not null,
   a_participation_visible bool default true not null,
   fk_group_id bigint,
   fk_entry_id bigint not null,
   a_sub_ident varchar(64) not null,
   primary key (id)
);

create table o_ap_organizer (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   fk_topic_id bigint not null,
   fk_identity_id bigint not null,
   primary key (id)
);

create table o_ap_topic_to_group (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_topic_id bigint not null,
   fk_group_id bigint,
   primary key (id)
);

create table o_ap_appointment (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   a_status varchar(64) not null,
   a_status_mod_date datetime,
   a_start datetime,
   a_end datetime,
   a_location varchar(256),
   a_details varchar(4000),
   a_max_participations integer,
   fk_topic_id bigint not null,
   fk_meeting_id bigint,
   fk_teams_id bigint,
   primary key (id)
);

create table o_ap_participation (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   fk_appointment_id bigint not null,
   fk_identity_id bigint not null,
   fk_identity_created_by bigint not null,
   primary key (id)
);

-- Organiation role rights
create table o_org_role_to_right (
   id bigint not null auto_increment,
   creationdate datetime not null,
   o_role varchar(255) not null,
   o_right varchar(255) not null,
   fk_organisation bigint not null,
   primary key (id)
);

-- Contact tracing
create table o_ct_location (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   l_reference varchar(255),
   l_titel varchar(255),
   l_room varchar(255),
   l_sector varchar(255),
   l_table varchar(255),
   l_building varchar(255),
   l_seat_number boolean default false not null,
   l_qr_id varchar(255) not null,
   l_qr_text varchar(4000),
   l_guests boolean default true not null,
   l_printed boolean default false not null,
   unique(l_qr_id),
   primary key (id)
);

create table o_ct_registration (
   id bigint not null auto_increment,
   creationdate datetime not null,
   l_deletion_date datetime not null,
   l_start_date datetime not null,
   l_end_date datetime,
   l_nick_name varchar(255),
   l_first_name varchar(255),
   l_last_name varchar(255),
   l_street varchar(255),
   l_extra_line varchar(255),
   l_zip_code varchar(255),
   l_city varchar(255),
   l_email varchar(255),
   l_institutional_email varchar(255),
   l_generic_email varchar(255),
   l_private_phone varchar(255),
   l_mobile_phone varchar(255),
   l_office_phone varchar(255),
   l_seat_number varchar(64),
   fk_location bigint not null,
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
   from o_bs_identity as ident
   inner join o_user as us on (ident.id = us.fk_identity)
);

create view o_gp_business_to_repository_v as (
    select
        grp.group_id as grp_id,
        repoentry.repositoryentry_id as re_id,
        repoentry.displayname as re_displayname
    from o_gp_business as grp
    inner join o_re_to_group as relation on (relation.fk_group_id = grp.fk_group_id)
    inner join o_repositoryentry as repoentry on (repoentry.repositoryentry_id = relation.fk_entry_id)
);

create view o_bs_gp_membership_v as (
   select
      membership.id as membership_id,
      membership.fk_identity_id as fk_identity_id,
      membership.lastmodified as lastmodified,
      membership.creationdate as creationdate,
      membership.g_role as g_role,
      gp.group_id as group_id
   from o_bs_group_member as membership
   inner join o_gp_business as gp on (gp.fk_group_id=membership.fk_group_id)
);

create or replace view o_re_membership_v as (
   select
      bmember.id as membership_id,
      bmember.creationdate as creationdate,
      bmember.lastmodified as lastmodified,
      bmember.fk_identity_id as fk_identity_id,
      bmember.g_role as g_role,
      re.repositoryentry_id as fk_entry_id
   from o_repositoryentry as re
   inner join o_re_to_group relgroup on (relgroup.fk_entry_id=re.repositoryentry_id and relgroup.r_defgroup=1)
   inner join o_bs_group_member as bmember on (bmember.fk_group_id=relgroup.fk_group_id)
);

-- contacts
create view o_gp_contactkey_v as (
   select
      bg_member.id as membership_id,
      bg_member.fk_identity_id as member_id,
      bg_member.g_role as membership_role,
      bg_me.fk_identity_id as me_id,
      bgroup.group_id as bg_id
   from o_gp_business as bgroup
   inner join o_bs_group_member as bg_member on (bg_member.fk_group_id = bgroup.fk_group_id)
   inner join o_bs_group_member as bg_me on (bg_me.fk_group_id = bgroup.fk_group_id)
   where
      (bgroup.ownersintern=1 and bg_member.g_role='coach')
      or
      (bgroup.participantsintern=1 and bg_member.g_role='participant')
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
   from o_gp_business as bgroup
   inner join o_bs_group_member as bg_member on (bg_member.fk_group_id = bgroup.fk_group_id)
   inner join o_bs_identity as id_member on (bg_member.fk_identity_id = id_member.id)
   inner join o_user as us_member on (id_member.id = us_member.fk_identity)
   inner join o_bs_group_member as bg_me on (bg_me.fk_group_id = bgroup.fk_group_id)
   where
      (bgroup.ownersintern=1 and bg_member.g_role='coach')
      or
      (bgroup.participantsintern=1 and bg_member.g_role='participant')
);


-- instant messaging
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

-- question pool
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
alter table o_forum_pseudonym ENGINE = InnoDB;
alter table o_property ENGINE = InnoDB;
alter table o_bs_secgroup ENGINE = InnoDB;
alter table o_bs_group ENGINE = InnoDB;
alter table o_bs_group_member ENGINE = InnoDB;
alter table o_bs_relation_role ENGINE = InnoDB;
alter table o_bs_relation_right ENGINE = InnoDB;
alter table o_bs_relation_role_to_right ENGINE = InnoDB;
alter table o_re_to_group ENGINE = InnoDB;
alter table o_re_to_tax_level ENGINE = InnoDB;
alter table o_bs_grant ENGINE = InnoDB;
alter table o_repositoryentry_cycle ENGINE = InnoDB;
alter table o_re_educational_type ENGINE = InnoDB;
alter table o_lti_outcome ENGINE = InnoDB;
alter table o_user ENGINE = InnoDB;
alter table o_userproperty ENGINE = InnoDB;
alter table o_user_data_export ENGINE = InnoDB;
alter table o_user_absence_leave ENGINE = InnoDB;
alter table o_message ENGINE = InnoDB;
alter table o_temporarykey ENGINE = InnoDB;
alter table o_bs_authentication ENGINE = InnoDB;
alter table o_bs_authentication_history ENGINE = InnoDB;
alter table o_qtiresult ENGINE = InnoDB;
alter table o_qtiresultset ENGINE = InnoDB;
alter table o_bs_identity ENGINE = InnoDB;
alter table o_csp_log ENGINE = InnoDB;
alter table o_olatresource ENGINE = InnoDB;
alter table o_bs_policy ENGINE = InnoDB;
alter table o_bs_namedgroup ENGINE = InnoDB;
alter table o_bs_membership ENGINE = InnoDB;
alter table o_repositoryentry ENGINE = InnoDB;
alter table o_repositoryentry_stats ENGINE = InnoDB;
alter table o_references ENGINE = InnoDB;
alter table o_gp_business ENGINE = InnoDB;
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
alter table o_ac_auto_advance_order ENGINE = InnoDB;
alter table o_as_eff_statement ENGINE = InnoDB;
alter table o_as_user_course_infos ENGINE = InnoDB;
alter table o_as_mode_course ENGINE = InnoDB;
alter table o_as_entry ENGINE = InnoDB;
alter table o_as_compensation ENGINE = InnoDB;
alter table o_as_compensation_log ENGINE = InnoDB;
alter table o_as_mode_course_to_area ENGINE = InnoDB;
alter table o_as_mode_course_to_cur_el ENGINE = InnoDB;
alter table o_cal_use_config ENGINE = InnoDB;
alter table o_cal_import ENGINE = InnoDB;
alter table o_cal_import_to ENGINE = InnoDB;
alter table o_mapper ENGINE = InnoDB;
alter table o_qti_assessmenttest_session ENGINE = InnoDB;
alter table o_qti_assessmentitem_session ENGINE = InnoDB;
alter table o_qti_assessment_response ENGINE = InnoDB;
alter table o_qti_assessment_marks ENGINE = InnoDB;
alter table o_qp_pool ENGINE = InnoDB;
alter table o_qp_taxonomy_level ENGINE = InnoDB;
alter table o_qp_item ENGINE = InnoDB;
alter table o_qp_pool_2_item ENGINE = InnoDB;
alter table o_qp_share_item ENGINE = InnoDB;
alter table o_qp_item_collection ENGINE = InnoDB;
alter table o_qp_collection_2_item ENGINE = InnoDB;
alter table o_qp_edu_context ENGINE = InnoDB;
alter table o_qp_item_type ENGINE = InnoDB;
alter table o_qp_license ENGINE = InnoDB;
alter table o_om_room_reference ENGINE = InnoDB;
alter table o_aconnect_meeting ENGINE = InnoDB;
alter table o_aconnect_user ENGINE = InnoDB;
alter table o_bbb_template ENGINE = InnoDB;
alter table o_bbb_meeting ENGINE = InnoDB;
alter table o_bbb_server ENGINE = InnoDB;
alter table o_bbb_attendee ENGINE = InnoDB;
alter table o_bbb_recording ENGINE = InnoDB;
alter table o_teams_meeting ENGINE = InnoDB;
alter table o_teams_user ENGINE = InnoDB;
alter table o_teams_attendee ENGINE = InnoDB;
alter table o_im_message ENGINE = InnoDB;
alter table o_im_notification ENGINE = InnoDB;
alter table o_im_roster_entry ENGINE = InnoDB;
alter table o_im_preferences ENGINE = InnoDB;
alter table o_ex_task ENGINE = InnoDB;
alter table o_ex_task_modifier ENGINE = InnoDB;
alter table o_checklist ENGINE = InnoDB;
alter table o_cl_checkbox ENGINE = InnoDB;
alter table o_cl_check ENGINE = InnoDB;
alter table o_gta_task_list ENGINE = InnoDB;
alter table o_gta_task ENGINE = InnoDB;
alter table o_gta_task_revision ENGINE = InnoDB;
alter table o_gta_task_revision_date ENGINE = InnoDB;
alter table o_gta_mark ENGINE = InnoDB;
alter table o_cer_template ENGINE = InnoDB;
alter table o_cer_certificate ENGINE = InnoDB;
alter table o_rem_reminder ENGINE = InnoDB;
alter table o_rem_sent_reminder ENGINE = InnoDB;
alter table o_goto_organizer ENGINE = InnoDB;
alter table o_goto_meeting ENGINE = InnoDB;
alter table o_goto_registrant ENGINE = InnoDB;
alter table o_vid_transcoding ENGINE = InnoDB;
alter table o_vid_metadata ENGINE = InnoDB;
alter table o_pf_category_relation ENGINE = InnoDB;
alter table o_pf_category ENGINE = InnoDB;
alter table o_pf_media ENGINE = InnoDB;
alter table o_pf_page_part ENGINE = InnoDB;
alter table o_pf_section ENGINE = InnoDB;
alter table o_pf_page_body ENGINE = InnoDB;
alter table o_pf_page ENGINE = InnoDB;
alter table o_pf_binder ENGINE = InnoDB;
alter table o_pf_assessment_section ENGINE = InnoDB;
alter table o_pf_assignment ENGINE = InnoDB;
alter table o_pf_binder_user_infos ENGINE = InnoDB;
alter table o_eva_form_participation ENGINE = InnoDB;
alter table o_eva_form_session ENGINE = InnoDB;
alter table o_eva_form_response ENGINE = InnoDB;
alter table o_eva_form_survey ENGINE = InnoDB;
alter table o_qual_data_collection ENGINE = InnoDB;
alter table o_qual_data_collection_to_org ENGINE = InnoDB;
alter table o_qual_context ENGINE = InnoDB;
alter table o_qual_context_to_organisation ENGINE = InnoDB;
alter table o_qual_context_to_curriculum ENGINE = InnoDB;
alter table o_qual_context_to_cur_element ENGINE = InnoDB;
alter table o_qual_context_to_tax_level ENGINE = InnoDB;
alter table o_qual_reminder ENGINE = InnoDB;
alter table o_qual_report_access ENGINE = InnoDB;
alter table o_qual_generator ENGINE = InnoDB;
alter table o_qual_generator_config ENGINE = InnoDB;
alter table o_qual_generator_to_org ENGINE = InnoDB;
alter table o_qual_analysis_presentation ENGINE = InnoDB;
alter table o_vfs_metadata ENGINE = InnoDB;
alter table o_vfs_thumbnail ENGINE = InnoDB;
alter table o_vfs_revision ENGINE = InnoDB;
alter table o_de_access ENGINE = InnoDB;
alter table o_de_user_info ENGINE = InnoDB;
alter table o_sms_message_log ENGINE = InnoDB;
alter table o_feed ENGINE = InnoDB;
alter table o_feed_item ENGINE = InnoDB;
alter table o_lecture_reason ENGINE = InnoDB;
alter table o_lecture_absence_category ENGINE = InnoDB;
alter table o_lecture_absence_notice ENGINE = InnoDB;
alter table o_lecture_notice_to_block ENGINE = InnoDB;
alter table o_lecture_notice_to_entry ENGINE = InnoDB;
alter table o_lecture_block ENGINE = InnoDB;
alter table o_lecture_block_to_group ENGINE = InnoDB;
alter table o_lecture_block_roll_call ENGINE = InnoDB;
alter table o_lecture_reminder ENGINE = InnoDB;
alter table o_lecture_participant_summary ENGINE = InnoDB;
alter table o_lecture_entry_config ENGINE = InnoDB;
alter table o_lecture_block_audit_log ENGINE = InnoDB;
alter table o_lecture_block_to_tax_level ENGINE = InnoDB;
alter table o_tax_taxonomy ENGINE = InnoDB;
alter table o_tax_taxonomy_level_type ENGINE = InnoDB;
alter table o_tax_taxonomy_type_to_type ENGINE = InnoDB;
alter table o_tax_taxonomy_level ENGINE = InnoDB;
alter table o_tax_taxonomy_competence ENGINE = InnoDB;
alter table o_lic_license_type ENGINE = InnoDB;
alter table o_lic_license_type_activation ENGINE = InnoDB;
alter table o_lic_license ENGINE = InnoDB;
alter table o_org_organisation_type ENGINE = InnoDB;
alter table o_org_organisation ENGINE = InnoDB;
alter table o_org_type_to_type ENGINE = InnoDB;
alter table o_org_role_to_right ENGINE = InnoDB;
alter table o_re_to_organisation ENGINE = InnoDB;
alter table o_cur_element_type ENGINE = InnoDB;
alter table o_cur_curriculum ENGINE = InnoDB;
alter table o_cur_curriculum_element ENGINE = InnoDB;
alter table o_cur_element_type_to_type ENGINE = InnoDB;
alter table o_cur_element_to_tax_level ENGINE = InnoDB;
alter table o_es_usage ENGINE = InnoDB;
alter table o_livestream_launch ENGINE = InnoDB;
alter table o_grad_to_identity ENGINE = InnoDB;
alter table o_grad_assignment ENGINE = InnoDB;
alter table o_grad_time_record ENGINE = InnoDB;
alter table o_grad_configuration ENGINE = InnoDB;
alter table o_course_disclaimer_consent ENGINE = InnoDB;
alter table o_ap_topic ENGINE = InnoDB;
alter table o_ap_organizer ENGINE = InnoDB;
alter table o_ap_topic_to_group ENGINE = InnoDB;
alter table o_ap_appointment ENGINE = InnoDB;
alter table o_ap_participation ENGINE = InnoDB;
alter table o_ct_location ENGINE = InnoDB;
alter table o_ct_registration ENGINE = InnoDB;

-- rating
alter table o_userrating add constraint FKF26C8375236F20X foreign key (creator_id) references o_bs_identity (id);
create index rtn_id_idx on o_userrating (resid);
create index rtn_name_idx on o_userrating (resname);
create index rtn_subpath_idx on o_userrating (ressubpath(255));
create index rtn_rating_idx on o_userrating (rating);
create index rtn_rating_res_idx on o_userrating (resid, resname, creator_id, rating);

-- comment
alter table o_usercomment add constraint FK92B6864A18251F0 foreign key (parent_key) references o_usercomment (comment_id);
alter table o_usercomment add constraint FKF26C8375236F20A foreign key (creator_id) references o_bs_identity (id);
create index cmt_id_idx on o_usercomment (resid);
create index cmt_name_idx on o_usercomment (resname);
create index cmt_subpath_idx on o_usercomment (ressubpath(255));

-- checkpoint
alter table o_checkpoint_results add constraint FK9E30F4B661159ZZY foreign key (checkpoint_fk) references o_checkpoint (checkpoint_id) ;
alter table o_checkpoint_results add constraint FK9E30F4B661159ZZX foreign key (identity_fk) references o_bs_identity (id);

alter table o_checkpoint add constraint FK9E30F4B661159ZZZ foreign key (checklist_fk) references o_checklist (checklist_id);

-- plock
create index asset_idx on o_plock (asset);

-- property
alter table o_property add constraint FKB60B1BA5190E5 foreign key (grp) references o_gp_business (group_id);
alter table o_property add constraint FKB60B1BA5F7E870BE foreign key (identity) references o_bs_identity (id);

create index idx_prop_indexresid_idx on o_property (resourcetypeid);
create index idx_prop_category_idx on o_property (category);
create index idx_prop_name_idx on o_property (name);
create index idx_prop_restype_idx on o_property (resourcetypename);

-- group
alter table o_bs_group_member add constraint member_identity_ctx foreign key (fk_identity_id) references o_bs_identity (id);
alter table o_bs_group_member add constraint member_group_ctx foreign key (fk_group_id) references o_bs_group (id);
create index group_role_member_idx on o_bs_group_member (fk_group_id,g_role,fk_identity_id);

alter table o_re_to_group add constraint re_to_group_group_ctx foreign key (fk_group_id) references o_bs_group (id);
alter table o_re_to_group add constraint re_to_group_re_ctx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);

alter table o_gp_business add constraint gp_to_group_business_ctx foreign key (fk_group_id) references o_bs_group (id);

-- business group
alter table o_gp_business add constraint idx_bgp_rsrc foreign key (fk_resource) references o_olatresource (resource_id);

create index gp_name_idx on o_gp_business (groupname);
create index idx_grp_lifecycle_soft_idx on o_gp_business (external_id);

alter table o_bs_namedgroup add constraint FKBAFCBBC4B85B522C foreign key (secgroup_id) references o_bs_secgroup (id);
create index groupname_idx on o_bs_namedgroup (groupname);

-- area
alter table o_gp_bgarea add constraint idx_area_to_resource foreign key (fk_resource) references o_olatresource (resource_id);
create index name_idx on o_gp_bgarea (name);

alter table o_gp_bgtoarea_rel add constraint FK9B663F2D1E2E7685 foreign key (group_fk) references o_gp_business (group_id);
alter table o_gp_bgtoarea_rel add constraint FK9B663F2DD381B9B7 foreign key (area_fk) references o_gp_bgarea (area_id);

-- bs
alter table o_bs_authentication add constraint FKC6A5445652595FE6 foreign key (identity_fk) references o_bs_identity (id);
create index provider_idx on o_bs_authentication (provider);
create index credential_idx on o_bs_authentication (credential);
create index authusername_idx on o_bs_authentication (authusername);

alter table o_bs_authentication_history add constraint auth_hist_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);

create index name_idx on o_bs_identity (name);
create index identstatus_idx on o_bs_identity (status);
create index idx_ident_creationdate_idx on o_bs_identity (creationdate);
create index idx_id_lastlogin_idx on o_bs_identity (lastlogin);



alter table o_bs_membership add constraint FK7B6288B45259603C foreign key (identity_id) references o_bs_identity (id);
alter table o_bs_membership add constraint FK7B6288B4B85B522C foreign key (secgroup_id) references o_bs_secgroup (id);

alter table o_bs_invitation add constraint inv_to_group_group_ctx foreign key (fk_group_id) references o_bs_group (id);
alter table o_bs_invitation add constraint invit_to_id_idx foreign key (fk_identity_id) references o_bs_identity (id);

-- user to user relations
alter table o_bs_relation_role ENGINE = InnoDB;

create index idx_right_idx on o_bs_relation_right (g_right);

alter table o_bs_relation_role_to_right add constraint role_to_right_role_idx foreign key (fk_role_id) references o_bs_relation_role (id);
alter table o_bs_relation_role_to_right add constraint role_to_right_right_idx foreign key (fk_right_id) references o_bs_relation_right (id);

alter table o_bs_identity_to_identity add constraint id_to_id_source_idx foreign key (fk_source_id) references o_bs_identity (id);
alter table o_bs_identity_to_identity add constraint id_to_id_target_idx foreign key (fk_target_id) references o_bs_identity (id);
alter table o_bs_identity_to_identity add constraint id_to_role_idx foreign key (fk_role_id) references o_bs_relation_role (id);

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
alter table o_user add constraint idx_un_user_to_ident_idx UNIQUE (fk_identity);

alter table o_user_data_export add constraint usr_dataex_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_user_data_export add constraint usr_dataex_to_requ_idx foreign key (fk_request_by) references o_bs_identity (id);

alter table o_user_absence_leave add constraint abs_leave_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);

-- csp
create index idx_csp_log_to_ident_idx on o_csp_log (fk_identity);

-- temporary key
create index idx_tempkey_identity_idx on o_temporarykey (fk_identity_id);

-- pub sub
create index name_idx on o_noti_pub (resname, resid, subident);

alter table o_noti_sub add constraint FK4FB8F04749E53702 foreign key (fk_publisher) references o_noti_pub (publisher_id);
alter table o_noti_sub add constraint FK4FB8F0476B1F22F8 foreign key (fk_identity) references o_bs_identity (id);

-- qti
alter table o_qtiresultset add constraint FK14805D0F5259603C foreign key (identity_id) references o_bs_identity (id);

create index oresdetindex on o_qtiresultset (olatresourcedetail);
create index oresindex on o_qtiresultset (olatresource_fk);
create index reprefindex on o_qtiresultset (repositoryref_fk);
create index assindex on o_qtiresultset (assessmentid);

alter table o_qtiresult add constraint FK3563E67340EF401F foreign key (resultset_fk) references o_qtiresultset (resultset_id);
create index itemindex on o_qtiresult (itemident);

-- catalog entry
alter table o_catentry add constraint FKF4433C2C7B66B0D0 foreign key (parent_id) references o_catentry (id);
alter table o_catentry add constraint FKF4433C2CA1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup (id);
alter table o_catentry add constraint FKF4433C2CDDD69946 foreign key (fk_repoentry) references o_repositoryentry (repositoryentry_id);

-- references
alter table o_references add constraint FKE971B4589AC44FBF foreign key (source_id) references o_olatresource (resource_id);
alter table o_references add constraint FKE971B458CF634A89 foreign key (target_id) references o_olatresource (resource_id);

-- resources
create index name_idx on o_olatresource (resname);
create index id_idx on o_olatresource (resid);

-- repository
alter table o_repositoryentry add constraint FK2F9C439888C31018 foreign key (fk_olatresource) references o_olatresource (resource_id);

create index re_status_idx on o_repositoryentry (status);
create index initialAuthor_idx on o_repositoryentry (initialauthor);
create index resource_idx on o_repositoryentry (resourcename);
create index displayname_idx on o_repositoryentry (displayname);
create index softkey_idx on o_repositoryentry (softkey);
create index idx_re_lifecycle_extid_idx on o_repositoryentry (external_id);
create index idx_re_lifecycle_extref_idx on o_repositoryentry (external_ref);

alter table o_repositoryentry add constraint idx_re_lifecycle_fk foreign key (fk_lifecycle) references o_repositoryentry_cycle(id);
create index idx_re_lifecycle_soft_idx on o_repositoryentry_cycle (r_softkey);

alter table o_repositoryentry add constraint repoentry_stats_ctx foreign key (fk_stats) references o_repositoryentry_stats (id);

alter table o_repositoryentry add constraint re_deleted_to_identity_idx foreign key (fk_deleted_by) references o_bs_identity (id);

alter table o_re_to_tax_level add constraint re_to_lev_re_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_re_to_tax_level add constraint re_to_lev_tax_lev_idx foreign key (fk_taxonomy_level) references o_tax_taxonomy_level (id);

alter table o_repositoryentry add constraint idx_re_edu_type_fk foreign key (fk_educational_type) references o_re_educational_type(id);
create unique index idc_re_edu_type_ident on o_re_educational_type (r_identifier);

-- access control
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

create index idx_ac_aao_id_idx on o_ac_auto_advance_order(id);
create index idx_ac_aao_identifier_idx on o_ac_auto_advance_order(a_identifier_key, a_identifier_value);
create index idx_ac_aao_ident_idx on o_ac_auto_advance_order(fk_identity);
alter table o_ac_auto_advance_order add constraint aao_ident_idx foreign key (fk_identity) references o_bs_identity (id);

-- reservations
alter table o_ac_reservation add constraint idx_rsrv_to_rsrc_rsrc foreign key (fk_resource) references o_olatresource (resource_id);
alter table o_ac_reservation add constraint idx_rsrv_to_rsrc_identity foreign key (fk_identity) references o_bs_identity (id);

-- note
alter table o_note add constraint FKC2D855C263219E27 foreign key (owner_id) references o_bs_identity (id);
create index resid_idx on o_note (resourcetypeid);
create index owner_idx on o_note (owner_id);
create index restype_idx on o_note (resourcetypename);

-- ex_task
alter table o_ex_task add constraint idx_ex_task_ident_id foreign key (fk_identity_id) references o_bs_identity(id);
alter table o_ex_task add constraint idx_ex_task_rsrc_id foreign key (fk_resource_id) references o_olatresource(resource_id);
alter table o_ex_task_modifier add constraint idx_ex_task_mod_ident_id foreign key (fk_identity_id) references o_bs_identity(id);
alter table o_ex_task_modifier add constraint idx_ex_task_mod_task_id foreign key (fk_task_id) references o_ex_task(id);

-- checklist
alter table o_cl_check add constraint check_identity_ctx foreign key (fk_identity_id) references o_bs_identity (id);
alter table o_cl_check add constraint check_box_ctx foreign key (fk_checkbox_id) references o_cl_checkbox (id);
alter table o_cl_check add unique check_identity_unique_ctx (fk_identity_id, fk_checkbox_id);
create index idx_checkbox_uuid_idx on o_cl_checkbox (c_checkboxid);

-- group tasks
alter table o_gta_task add constraint gtask_to_tasklist_idx foreign key (fk_tasklist) references o_gta_task_list (id);
alter table o_gta_task add constraint gtask_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_gta_task add constraint gtask_to_bgroup_idx foreign key (fk_businessgroup) references o_gp_business (group_id);
alter table o_gta_task add constraint gtaskreset_to_allower_idx foreign key (fk_allow_reset_identity) references o_bs_identity (id);

alter table o_gta_task_list add constraint gta_list_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);

alter table o_gta_task_revision add constraint task_rev_to_task_idx foreign key (fk_task) references o_gta_task (id);
alter table o_gta_task_revision add constraint task_rev_to_ident_idx foreign key (fk_comment_author) references o_bs_identity (id);

alter table o_gta_task_revision_date add constraint gtaskrev_to_task_idx foreign key (fk_task) references o_gta_task (id);

alter table o_gta_mark add constraint gtamark_tasklist_idx foreign key (fk_tasklist_id) references o_gta_task_list (id);

-- reminders
alter table o_rem_reminder add constraint rem_reminder_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_rem_reminder add constraint rem_reminder_to_creator_idx foreign key (fk_creator) references o_bs_identity (id);

alter table o_rem_sent_reminder add constraint rem_sent_rem_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_rem_sent_reminder add constraint rem_sent_rem_to_reminder_idx foreign key (fk_reminder) references o_rem_reminder (id);


-- lifecycle
create index lc_pref_idx on o_lifecycle (persistentref);
create index lc_type_idx on o_lifecycle (persistenttypename);
create index lc_action_idx on o_lifecycle (action);

-- mark
alter table o_mark add constraint FKF26C8375236F21X foreign key (creator_id) references o_bs_identity (id);

create index mark_all_idx on o_mark(resname,resid,creator_id);
create index mark_id_idx on o_mark(resid);
create index mark_name_idx on o_mark(resname);
create index mark_subpath_idx on o_mark(ressubpath(255));
create index mark_businesspath_idx on o_mark(businesspath(255));

-- forum
create index idx_forum_ref_idx on o_forum (f_refresid, f_refresname);
alter table o_message add constraint FKF26C8375236F20E foreign key (creator_id) references o_bs_identity (id);
alter table o_message add constraint FKF26C837A3FBEB83 foreign key (modifier_id) references o_bs_identity (id);
alter table o_message add constraint FKF26C8377B66B0D0 foreign key (parent_id) references o_message (message_id);
alter table o_message add constraint FKF26C8378EAC1DBB foreign key (topthread_id) references o_message (message_id);
alter table o_message add constraint FKF26C8371CB7C4A3 foreign key (forum_fk) references o_forum (forum_id);
create index forum_msg_pseudonym_idx on o_message (pseudonym);

create index readmessage_forum_idx on o_readmessage (forum_id);
create index readmessage_identity_idx on o_readmessage (identity_id);

create index forum_pseudonym_idx on o_forum_pseudonym (p_pseudonym);

-- project broker
create index projectbroker_project_broker_idx on o_projectbroker_project (projectbroker_fk);
create index projectbroker_project_id_idx on o_projectbroker_project (project_id);
create index o_projectbroker_customfields_idx on o_projectbroker_customfields (fk_project_id);

-- info messages
alter table o_info_message add constraint FKF85553465A4FA5DC foreign key (fk_author_id) references o_bs_identity (id);
alter table o_info_message add constraint FKF85553465A4FA5EF foreign key (fk_modifier_id) references o_bs_identity (id);

create index imsg_resid_idx on o_info_message (resid);

-- db course
alter table o_co_db_entry add constraint FK_DB_ENTRY_TO_IDENT foreign key (identity) references o_bs_identity (id);

create index o_co_db_course_idx on o_co_db_entry (courseid);
create index o_co_db_cat_idx on o_co_db_entry (category);
create index o_co_db_name_idx on o_co_db_entry (name);

-- open meeting
alter table o_om_room_reference add constraint idx_omroom_to_bgroup foreign key (businessgroup) references o_gp_business (group_id);
create index idx_omroom_residname on o_om_room_reference (resourcetypename,resourcetypeid);

-- Adobe Connect
alter table o_aconnect_meeting add constraint aconnect_meet_entry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
alter table o_aconnect_meeting add constraint aconnect_meet_grp_idx foreign key (fk_group_id) references o_gp_business (group_id);

alter table o_aconnect_user add constraint aconn_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);

-- Bigbluebutton
alter table o_bbb_meeting add constraint bbb_meet_entry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
alter table o_bbb_meeting add constraint bbb_meet_grp_idx foreign key (fk_group_id) references o_gp_business (group_id);
alter table o_bbb_meeting add constraint bbb_meet_template_idx foreign key (fk_template_id) references o_bbb_template (id);
alter table o_bbb_meeting add constraint bbb_meet_creator_idx foreign key (fk_creator_id) references o_bs_identity (id);
alter table o_bbb_meeting add constraint bbb_meet_serv_idx foreign key (fk_server_id) references o_bbb_server (id);
alter table o_bbb_meeting add constraint bbb_dir_idx unique (b_directory);

alter table o_bbb_attendee add constraint bbb_attend_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
alter table o_bbb_attendee add constraint bbb_attend_meet_idx foreign key (fk_meeting_id) references o_bbb_meeting (id);

alter table o_bbb_recording add constraint bbb_record_meet_idx foreign key (fk_meeting_id) references o_bbb_meeting (id);

-- Teams
alter table o_teams_meeting add constraint teams_meet_entry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
alter table o_teams_meeting add constraint teams_meet_grp_idx foreign key (fk_group_id) references o_gp_business (group_id);
alter table o_teams_meeting add constraint teams_meet_creator_idx foreign key (fk_creator_id) references o_bs_identity (id);

alter table o_teams_user add constraint teams_user_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);

alter table o_teams_attendee add constraint teams_att_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
alter table o_teams_attendee add constraint teams_att_user_idx foreign key (fk_teams_user_id) references o_teams_user (id);
alter table o_teams_attendee add constraint teams_att_meet_idx foreign key (fk_meeting_id) references o_teams_meeting (id);

-- tag
alter table o_tag add constraint FK6491FCA5A4FA5DC foreign key (fk_author_id) references o_bs_identity (id);

-- mail
alter table o_mail add constraint FKF86663165A4FA5DC foreign key (fk_from_id) references o_mail_recipient (recipient_id);
create index idx_mail_meta_id_idx on o_mail (meta_mail_id);

alter table o_mail_recipient add constraint FKF86663165A4FA5DG foreign key (fk_recipient_id) references o_bs_identity (id);

alter table o_mail_to_recipient add constraint FKF86663165A4FA5DE foreign key (fk_mail_id) references o_mail (mail_id);
alter table o_mail_to_recipient add constraint FKF86663165A4FA5DD foreign key (fk_recipient_id) references o_mail_recipient (recipient_id);

alter table o_mail_attachment add constraint FKF86663165A4FA5DF foreign key (fk_att_mail_id) references o_mail (mail_id);
create index idx_mail_att_checksum_idx on o_mail_attachment (datas_checksum);
create index idx_mail_path_idx on o_mail_attachment (datas_path(255));
create index idx_mail_att_siblings_idx on o_mail_attachment (datas_checksum, mimetype, datas_size, datas_name);

-- instant messaging
alter table o_im_message add constraint idx_im_msg_to_fromid foreign key (fk_from_identity_id) references o_bs_identity (id);
create index idx_im_msg_res_idx on o_im_message (msg_resid,msg_resname);

alter table o_im_notification add constraint idx_im_not_to_toid foreign key (fk_to_identity_id) references o_bs_identity (id);
alter table o_im_notification add constraint idx_im_not_to_fromid foreign key (fk_from_identity_id) references o_bs_identity (id);
create index idx_im_chat_res_idx on o_im_notification (chat_resid,chat_resname);

alter table o_im_roster_entry add constraint idx_im_rost_to_id foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_im_rost_res_idx on o_im_roster_entry (r_resid,r_resname);

alter table o_im_preferences add constraint idx_im_prfs_to_id foreign key (fk_from_identity_id) references o_bs_identity (id);

-- efficiency statements
alter table o_as_eff_statement add unique eff_statement_id_cstr (fk_identity, fk_resource_id), add constraint eff_statement_id_cstr foreign key (fk_identity) references o_bs_identity (id);
create index eff_statement_repo_key_idx on o_as_eff_statement (course_repo_key);
create index idx_eff_stat_course_ident_idx on o_as_eff_statement (fk_identity,course_repo_key);

-- course infos
alter table o_as_user_course_infos add index user_course_infos_id_cstr (fk_identity), add constraint user_course_infos_id_cstr foreign key (fk_identity) references o_bs_identity (id);
alter table o_as_user_course_infos add index user_course_infos_res_cstr (fk_resource_id), add constraint user_course_infos_res_cstr foreign key (fk_resource_id) references o_olatresource (resource_id);
alter table o_as_user_course_infos add unique (fk_identity, fk_resource_id);

alter table o_as_entry add constraint as_entry_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_as_entry add constraint as_entry_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_as_entry add constraint as_entry_to_refentry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);

create index idx_as_entry_to_id_idx on o_as_entry (a_assessment_id);
create index idx_as_entry_start_idx on o_as_entry (a_date_start);

-- disadvantage compensation
alter table o_as_compensation add constraint compensation_ident_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_as_compensation add constraint compensation_crea_idx foreign key (fk_creator) references o_bs_identity (id);
alter table o_as_compensation add constraint compensation_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);

create index comp_log_entry_idx on o_as_compensation_log (fk_entry_id);
create index comp_log_ident_idx on o_as_compensation_log (fk_identity_id);

-- gotomeeting
alter table o_goto_organizer add constraint goto_organ_owner_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_goto_organ_okey_idx on o_goto_organizer(g_organizer_key);
create index idx_goto_organ_uname_idx on o_goto_organizer(g_username);

alter table o_goto_meeting add constraint goto_meet_repoentry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
alter table o_goto_meeting add constraint goto_meet_busgrp_idx foreign key (fk_group_id) references o_gp_business (group_id);
alter table o_goto_meeting add constraint goto_meet_organizer_idx foreign key (fk_organizer_id) references o_goto_organizer (id);

alter table o_goto_registrant add constraint goto_regis_meeting_idx foreign key (fk_meeting_id) references o_goto_meeting (id);
alter table o_goto_registrant add constraint goto_regis_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);

-- video
alter table o_vid_transcoding add constraint fk_resource_id_idx foreign key (fk_resource_id) references o_olatresource (resource_id);
create index vid_status_trans_idx on o_vid_transcoding(vid_status);
create index vid_transcoder_trans_idx on o_vid_transcoding(vid_transcoder);
alter table o_vid_metadata add constraint vid_meta_rsrc_idx foreign key (fk_resource_id) references o_olatresource (resource_id);


-- calendar
alter table o_cal_use_config add constraint cal_u_conf_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_cal_u_conf_cal_id_idx on o_cal_use_config (c_calendar_id);
create index idx_cal_u_conf_cal_type_idx on o_cal_use_config (c_calendar_type);

alter table o_cal_import add constraint cal_imp_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_cal_imp_cal_id_idx on o_cal_import (c_calendar_id);
create index idx_cal_imp_cal_type_idx on o_cal_import (c_calendar_type);

create index idx_cal_imp_to_cal_id_idx on o_cal_import_to (c_to_calendar_id);
create index idx_cal_imp_to_cal_type_idx on o_cal_import_to (c_to_calendar_type);

-- mapper
create index o_mapper_uuid_idx on o_mapper (mapper_uuid);

-- qti 2.1
alter table o_qti_assessmenttest_session add constraint qti_sess_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_course_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_qti_assessmenttest_session add constraint qti_sess_to_as_entry_idx foreign key (fk_assessment_entry) references o_as_entry (id);

alter table o_qti_assessmentitem_session add constraint qti_itemsess_to_testsess_idx foreign key (fk_assessmenttest_session) references o_qti_assessmenttest_session (id);
create index idx_item_identifier_idx on o_qti_assessmentitem_session (q_itemidentifier);

alter table o_qti_assessment_response add constraint qti_resp_to_testsession_idx foreign key (fk_assessmenttest_session) references o_qti_assessmenttest_session (id);
alter table o_qti_assessment_response add constraint qti_resp_to_itemsession_idx foreign key (fk_assessmentitem_session) references o_qti_assessmentitem_session (id);
create index idx_response_identifier_idx on o_qti_assessment_response (q_responseidentifier);

alter table o_qti_assessment_marks add constraint qti_marks_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_qti_assessment_marks add constraint qti_marks_to_course_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
alter table o_qti_assessment_marks add constraint qti_marks_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);

-- portfolio
alter table o_pf_binder add constraint pf_binder_resource_idx foreign key (fk_olatresource_id) references o_olatresource (resource_id);
alter table o_pf_binder add constraint pf_binder_group_idx foreign key (fk_group_id) references o_bs_group (id);
alter table o_pf_binder add constraint pf_binder_course_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
alter table o_pf_binder add constraint pf_binder_template_idx foreign key (fk_template_id) references o_pf_binder (id);

alter table o_pf_section add constraint pf_section_group_idx foreign key (fk_group_id) references o_bs_group (id);
alter table o_pf_section add constraint pf_section_binder_idx foreign key (fk_binder_id) references o_pf_binder (id);
alter table o_pf_section add constraint pf_section_template_idx foreign key (fk_template_reference_id) references o_pf_section (id);

alter table o_pf_page add constraint pf_page_group_idx foreign key (fk_group_id) references o_bs_group (id);
alter table o_pf_page add constraint pf_page_section_idx foreign key (fk_section_id) references o_pf_section (id);

alter table o_pf_page add constraint pf_page_body_idx foreign key (fk_body_id) references o_pf_page_body (id);

alter table o_pf_media add constraint pf_media_author_idx foreign key (fk_author_id) references o_bs_identity (id);
create index idx_category_rel_resid_idx on o_pf_media (p_business_path);

alter table o_pf_page_part add constraint pf_page_page_body_idx foreign key (fk_page_body_id) references o_pf_page_body (id);
alter table o_pf_page_part add constraint pf_page_media_idx foreign key (fk_media_id) references o_pf_media (id);
alter table o_pf_page_part add constraint pf_part_form_idx foreign key (fk_form_entry_id) references o_repositoryentry (repositoryentry_id);

create index idx_category_name_idx on o_pf_category (p_name);

alter table o_pf_category_relation add constraint pf_category_rel_cat_idx foreign key (fk_category_id) references o_pf_category (id);
create index idx_category_rel_resid_idx on o_pf_category_relation (p_resid);

alter table o_pf_assessment_section add constraint pf_asection_section_idx foreign key (fk_section_id) references o_pf_section (id);
alter table o_pf_assessment_section add constraint pf_asection_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);

alter table o_pf_assignment add constraint pf_assign_section_idx foreign key (fk_section_id) references o_pf_section (id);
alter table o_pf_assignment add constraint pf_assign_binder_idx foreign key (fk_binder_id) references o_pf_binder (id);
alter table o_pf_assignment add constraint pf_assign_ref_assign_idx foreign key (fk_template_reference_id) references o_pf_assignment (id);
alter table o_pf_assignment add constraint pf_assign_page_idx foreign key (fk_page_id) references o_pf_page (id);
alter table o_pf_assignment add constraint pf_assign_assignee_idx foreign key (fk_assignee_id) references o_bs_identity (id);
alter table o_pf_assignment add constraint pf_assign_form_idx foreign key (fk_form_entry_id) references o_repositoryentry (repositoryentry_id);

alter table o_pf_binder_user_infos add constraint binder_user_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_pf_binder_user_infos add constraint binder_user_binder_idx foreign key (fk_binder) references o_pf_binder (id);

alter table o_pf_page_user_infos add constraint user_pfpage_idx foreign key (fk_identity_id) references o_bs_identity (id);
alter table o_pf_page_user_infos add constraint page_pfpage_idx foreign key (fk_page_id) references o_pf_page (id);

-- evaluation form
alter table o_eva_form_survey add constraint eva_surv_to_surv_idx foreign key (fk_series_previous) references o_eva_form_survey (id);
create index idx_eva_surv_ores_idx on o_eva_form_survey (e_resid, e_resname, e_sub_ident(255), e_sub_ident2(255));

alter table o_eva_form_participation add constraint eva_part_to_surv_idx foreign key (fk_survey) references o_eva_form_survey (id);
create unique index idx_eva_part_ident_idx on o_eva_form_participation (e_identifier_key, e_identifier_type, fk_survey);
create unique index idx_eva_part_executor_idx on o_eva_form_participation (fk_executor, fk_survey);

alter table o_eva_form_session add constraint eva_sess_to_surv_idx foreign key (fk_survey) references o_eva_form_survey (id);
alter table o_eva_form_session add constraint eva_sess_to_part_idx foreign key (fk_participation) references o_eva_form_participation (id);
alter table o_eva_form_session add constraint eva_sess_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_eva_form_session add constraint eva_sess_to_body_idx foreign key (fk_page_body) references o_pf_page_body (id);
alter table o_eva_form_session add constraint eva_sess_to_form_idx foreign key (fk_form_entry) references o_repositoryentry (repositoryentry_id);

alter table o_eva_form_response add constraint eva_resp_to_sess_idx foreign key (fk_session) references o_eva_form_session (id);
create index idx_eva_resp_report_idx on o_eva_form_response (fk_session, e_responseidentifier, e_no_response);

-- vfs metadata
alter table o_vfs_metadata add constraint fmeta_to_author_idx foreign key (fk_locked_identity) references o_bs_identity (id);
alter table o_vfs_metadata add constraint fmeta_modified_by_idx foreign key (fk_lastmodified_by) references o_bs_identity (id);
alter table o_vfs_metadata add constraint fmeta_to_lockid_idx foreign key (fk_initialized_by) references o_bs_identity (id);
alter table o_vfs_metadata add constraint fmeta_to_lic_type_idx foreign key (fk_license_type) references o_lic_license_type (id);
alter table o_vfs_metadata add constraint fmeta_to_parent_idx foreign key (fk_parent) references o_vfs_metadata (id);
create index f_m_rel_path_idx on o_vfs_metadata (f_relative_path(255));
create index f_m_file_idx on o_vfs_metadata (f_relative_path(255),f_filename(255));
create index f_m_uuid_idx on o_vfs_metadata (f_uuid);

alter table o_vfs_thumbnail add constraint fthumb_to_meta_idx foreign key (fk_metadata) references o_vfs_metadata (id);

alter table o_vfs_revision add constraint fvers_to_author_idx foreign key (fk_initialized_by) references o_bs_identity (id);
alter table o_vfs_revision add constraint fvers_modified_by_idx foreign key (fk_lastmodified_by) references o_bs_identity (id);
alter table o_vfs_revision add constraint fvers_to_meta_idx foreign key (fk_metadata) references o_vfs_metadata (id);
alter table o_vfs_metadata add constraint fvers_to_lic_type_idx foreign key (fk_license_type) references o_lic_license_type (id);

-- Document editor
create unique index idx_de_userinfo_ident_idx on o_de_user_info(fk_identity);

-- quality management
alter table o_qual_data_collection add constraint qual_dc_to_gen_idx foreign key (fk_generator) references o_qual_generator (id);
create index idx_dc_status_idx on o_qual_data_collection (q_status);

alter table o_qual_data_collection_to_org add constraint qual_dc_to_org_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
create unique index idx_qual_dc_to_org_idx on o_qual_data_collection_to_org (fk_data_collection, fk_organisation);

alter table o_qual_context add constraint qual_con_to_data_collection_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
alter table o_qual_context add constraint qual_con_to_participation_idx foreign key (fk_eva_participation) references o_eva_form_participation (id);
alter table o_qual_context add constraint qual_con_to_session_idx foreign key (fk_eva_session) references o_eva_form_session (id);

alter table o_qual_context_to_organisation add constraint qual_con_to_org_con_idx foreign key (fk_context) references o_qual_context (id);
create unique index idx_con_to_org_org_idx on o_qual_context_to_organisation (fk_organisation, fk_context);

alter table o_qual_context_to_curriculum add constraint qual_con_to_cur_con_idx foreign key (fk_context) references o_qual_context (id);
create unique index idx_con_to_cur_cur_idx on o_qual_context_to_curriculum (fk_curriculum, fk_context);

alter table o_qual_context_to_cur_element add constraint qual_con_to_cur_ele_con_idx foreign key (fk_context) references o_qual_context (id);
create unique index idx_con_to_cur_ele_ele_idx on o_qual_context_to_cur_element (fk_cur_element, fk_context);

alter table o_qual_context_to_tax_level add constraint qual_con_to_tax_level_con_idx foreign key (fk_context) references o_qual_context (id);
create unique index idx_con_to_tax_level_tax_idx on o_qual_context_to_tax_level (fk_tax_leveL, fk_context);

alter table o_qual_reminder add constraint qual_rem_to_data_collection_idx foreign key (fk_data_collection) references o_qual_data_collection (id);

alter table o_qual_report_access add constraint qual_repacc_to_dc_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
alter table o_qual_report_access add constraint qual_repacc_to_generator_idx foreign key (fk_generator) references o_qual_generator (id);

alter table o_qual_generator_to_org add constraint qual_gen_to_org_idx foreign key (fk_generator) references o_qual_generator (id);
create unique index idx_qual_gen_to_org_idx on o_qual_generator_to_org (fk_generator, fk_organisation);

-- question pool
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

alter table o_qp_item add constraint idx_qp_pool_2_tax_id foreign key (fk_taxonomy_level_v2) references o_tax_taxonomy_level(id);
alter table o_qp_item add constraint idx_qp_item_owner_id foreign key (fk_ownergroup) references o_bs_secgroup(id);
alter table o_qp_item add constraint idx_qp_item_edu_ctxt_id foreign key (fk_edu_context) references o_qp_edu_context(id);
alter table o_qp_item add constraint idx_qp_item_type_id foreign key (fk_type) references o_qp_item_type(id);
alter table o_qp_item add constraint idx_qp_item_license_id foreign key (fk_license) references o_qp_license(id);

alter table o_qp_taxonomy_level add constraint idx_qp_field_2_parent_id foreign key (fk_parent_field) references o_qp_taxonomy_level(id);
create index idx_taxon_mat_pathon on o_qp_taxonomy_level (q_mat_path_ids(255));

alter table o_qp_item_type add unique (q_type(200));
create index idx_item_audit_item_idx on o_qp_item_audit_log (fk_item_id);

-- lti outcome
alter table o_lti_outcome add constraint idx_lti_outcome_ident_id foreign key (fk_identity_id) references o_bs_identity(id);
alter table o_lti_outcome add constraint idx_lti_outcome_rsrc_id foreign key (fk_resource_id) references o_olatresource(resource_id);

-- assessment mode
alter table o_as_mode_course add constraint as_mode_to_repo_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_as_mode_course add constraint as_mode_to_lblock_idx foreign key (fk_lecture_block) references o_lecture_block (id);

alter table o_as_mode_course_to_group add constraint as_modetogroup_group_idx foreign key (fk_group_id) references o_gp_business (group_id);
alter table o_as_mode_course_to_group add constraint as_modetogroup_mode_idx foreign key (fk_assessment_mode_id) references o_as_mode_course (id);

alter table o_as_mode_course_to_area add constraint as_modetoarea_area_idx foreign key (fk_area_id) references o_gp_bgarea (area_id);
alter table o_as_mode_course_to_area add constraint as_modetoarea_mode_idx foreign key (fk_assessment_mode_id) references o_as_mode_course (id);

alter table o_as_mode_course_to_cur_el add constraint as_modetocur_el_idx foreign key (fk_cur_element_id) references o_cur_curriculum_element (id);
alter table o_as_mode_course_to_cur_el add constraint as_modetocur_mode_idx foreign key (fk_assessment_mode_id) references o_as_mode_course (id);

-- certificate
alter table o_cer_certificate add constraint cer_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_cer_certificate add constraint cer_to_resource_idx foreign key (fk_olatresource) references o_olatresource (resource_id);

create index cer_archived_resource_idx on o_cer_certificate (c_archived_resource_id);
create index cer_uuid_idx on o_cer_certificate (c_uuid);

-- sms
alter table o_sms_message_log add constraint sms_log_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);

-- webfeed
create index idx_feed_resourceable_idx on o_feed (f_resourceable_id, f_resourceable_type);
alter table o_feed_item add constraint item_to_feed_fk foreign key(fk_feed_id) references o_feed(id);
create index idx_item_feed_idx on o_feed_item(fk_feed_id);
alter table o_feed_item add constraint feed_item_to_ident_author_fk foreign key (fk_identity_author_id) references o_bs_identity (id);
create index idx_item_ident_author_idx on o_feed_item(fk_identity_author_id);
alter table o_feed_item add constraint feed_item_to_ident_modified_fk foreign key (fk_identity_modified_id) references o_bs_identity (id);
create index idx_item_ident_modified_idx on o_feed_item(fk_identity_modified_id);

-- lecture
alter table o_lecture_block add constraint lec_block_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_lecture_block add constraint lec_block_gcoach_idx foreign key (fk_teacher_group) references o_bs_group (id);
alter table o_lecture_block add constraint lec_block_reason_idx foreign key (fk_reason) references o_lecture_reason (id);

alter table o_lecture_block_roll_call add constraint absence_category_idx foreign key (fk_absence_category) references o_lecture_absence_category (id);

alter table o_lecture_absence_notice add constraint notice_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_lecture_absence_notice add constraint notice_notif_identity_idx foreign key (fk_notifier) references o_bs_identity (id);
alter table o_lecture_absence_notice add constraint notice_auth_identity_idx foreign key (fk_authorizer) references o_bs_identity (id);
alter table o_lecture_absence_notice add constraint notice_category_idx foreign key (fk_absence_category) references o_lecture_absence_category (id);

alter table o_lecture_notice_to_block add constraint notice_to_block_idx foreign key (fk_lecture_block) references o_lecture_block (id);
alter table o_lecture_notice_to_block add constraint notice_to_notice_idx foreign key (fk_absence_notice) references o_lecture_absence_notice (id);

alter table o_lecture_notice_to_entry add constraint notice_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_lecture_notice_to_entry add constraint rel_notice_e_to_notice_idx foreign key (fk_absence_notice) references o_lecture_absence_notice (id);

alter table o_lecture_block_to_group add constraint lec_block_to_block_idx foreign key (fk_group) references o_bs_group (id);
alter table o_lecture_block_to_group add constraint lec_block_to_group_idx foreign key (fk_lecture_block) references o_lecture_block (id);

alter table o_lecture_block_roll_call add constraint lec_call_block_idx foreign key (fk_lecture_block) references o_lecture_block (id);
alter table o_lecture_block_roll_call add constraint lec_call_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_lecture_block_roll_call add constraint rollcall_to_notice_idx foreign key (fk_absence_notice) references o_lecture_absence_notice (id);

alter table o_lecture_reminder add constraint lec_reminder_block_idx foreign key (fk_lecture_block) references o_lecture_block (id);
alter table o_lecture_reminder add constraint lec_reminder_identity_idx foreign key (fk_identity) references o_bs_identity (id);

alter table o_lecture_participant_summary add constraint lec_part_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_lecture_participant_summary add constraint lec_part_ident_idx foreign key (fk_identity) references o_bs_identity (id);

alter table o_lecture_entry_config add constraint lec_entry_config_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);

create index idx_lec_audit_entry_idx on o_lecture_block_audit_log(fk_entry);
create index idx_lec_audit_ident_idx on o_lecture_block_audit_log(fk_identity);

alter table o_lecture_block_to_tax_level add constraint lblock_rel_to_lblock_idx foreign key (fk_lecture_block) references o_lecture_block (id);
alter table o_lecture_block_to_tax_level add constraint lblock_rel_to_tax_lev_idx foreign key (fk_taxonomy_level) references o_tax_taxonomy_level (id);

-- taxonomy
alter table o_tax_taxonomy add constraint tax_to_group_idx foreign key (fk_group) references o_bs_group (id);

alter table o_tax_taxonomy_level_type add constraint tax_type_to_taxonomy_idx foreign key (fk_taxonomy) references o_tax_taxonomy (id);

alter table o_tax_taxonomy_type_to_type add constraint tax_type_to_type_idx foreign key (fk_type) references o_tax_taxonomy_level_type (id);
create index idx_tax_type_to_type_idx on o_tax_taxonomy_type_to_type (fk_type);
alter table o_tax_taxonomy_type_to_type add constraint tax_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_tax_taxonomy_level_type (id);
create index idx_tax_type_to_sub_type_idx on o_tax_taxonomy_type_to_type (fk_allowed_sub_type);

alter table o_tax_taxonomy_level add constraint tax_level_to_taxonomy_idx foreign key (fk_taxonomy) references o_tax_taxonomy (id);
alter table o_tax_taxonomy_level add constraint tax_level_to_tax_level_idx foreign key (fk_parent) references o_tax_taxonomy_level (id);
alter table o_tax_taxonomy_level add constraint tax_level_to_type_idx foreign key (fk_type) references o_tax_taxonomy_level_type (id);
create index idx_tax_level_path_key_idx on o_tax_taxonomy_level (t_m_path_keys);

alter table o_tax_taxonomy_competence add constraint tax_comp_to_tax_level_idx foreign key (fk_level) references o_tax_taxonomy_level (id);
alter table o_tax_taxonomy_competence add constraint tax_level_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);

-- dialog elements
alter table o_dialog_element add constraint dial_el_author_idx foreign key (fk_author) references o_bs_identity (id);
alter table o_dialog_element add constraint dial_el_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_dialog_element add constraint dial_el_forum_idx foreign key (fk_forum) references o_forum (forum_id);
create index idx_dial_el_subident_idx on o_dialog_element (d_subident);

-- licenses
alter table o_lic_license_type_activation add constraint lic_activation_type_fk foreign key (fk_license_type_id) references o_lic_license_type (id);
create index lic_activation_type_idx on o_lic_license_type_activation (fk_license_type_id);
alter table o_lic_license add constraint lic_license_type_fk foreign key (fk_license_type_id) references o_lic_license_type (id);
create index lic_license_type_idx on o_lic_license (fk_license_type_id);
create unique index lic_license_ores_idx on o_lic_license (l_resid, l_resname);

-- organisation
alter table o_org_organisation add constraint org_to_group_idx foreign key (fk_group) references o_bs_group (id);
alter table o_org_organisation add constraint org_to_root_org_idx foreign key (fk_root) references o_org_organisation (id);
alter table o_org_organisation add constraint org_to_parent_org_idx foreign key (fk_parent) references o_org_organisation (id);
alter table o_org_organisation add constraint org_to_org_type_idx foreign key (fk_type) references o_org_organisation_type (id);

alter table o_org_type_to_type add constraint org_type_to_type_idx foreign key (fk_type) references o_org_organisation_type (id);
alter table o_org_type_to_type add constraint org_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_org_organisation_type (id);

alter table o_re_to_organisation add constraint rel_org_to_re_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_re_to_organisation add constraint rel_org_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);

-- curriculum
alter table o_cur_curriculum add constraint cur_to_group_idx foreign key (fk_group) references o_bs_group (id);
alter table o_cur_curriculum add constraint cur_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);

alter table o_cur_curriculum_element add constraint cur_el_to_group_idx foreign key (fk_group) references o_bs_group (id);
alter table o_cur_curriculum_element add constraint cur_el_to_cur_el_idx foreign key (fk_parent) references o_cur_curriculum_element (id);
alter table o_cur_curriculum_element add constraint cur_el_to_cur_idx foreign key (fk_curriculum) references o_cur_curriculum (id);
alter table o_cur_curriculum_element add constraint cur_el_type_to_el_type_idx foreign key (fk_type) references o_cur_element_type (id);

alter table o_cur_element_type_to_type add constraint cur_type_to_type_idx foreign key (fk_type) references o_cur_element_type (id);
alter table o_cur_element_type_to_type add constraint cur_type_to_sub_type_idx foreign key (fk_allowed_sub_type) references o_cur_element_type (id);

alter table o_cur_element_to_tax_level add constraint cur_el_rel_to_cur_el_idx foreign key (fk_cur_element) references o_cur_curriculum_element (id);
alter table o_cur_element_to_tax_level add constraint cur_el_to_tax_level_idx foreign key (fk_taxonomy_level) references o_tax_taxonomy_level (id);

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
alter table o_grad_to_identity add constraint grad_id_to_repo_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);

alter table o_grad_assignment add constraint grad_assign_to_entry_idx foreign key (fk_reference_entry) references o_repositoryentry (repositoryentry_id);
alter table o_grad_assignment add constraint grad_assign_to_assess_idx foreign key (fk_assessment_entry) references o_as_entry (id);
alter table o_grad_assignment add constraint grad_assign_to_grader_idx foreign key (fk_grader) references o_grad_to_identity (id);

alter table o_grad_time_record add constraint grad_time_to_assign_idx foreign key (fk_assignment) references o_grad_assignment (id);
alter table o_grad_time_record add constraint grad_time_to_grader_idx foreign key (fk_grader) references o_grad_to_identity (id);

alter table o_grad_configuration add constraint grad_config_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);

-- Appointments
alter table o_ap_topic add constraint ap_topic_entry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
alter table o_ap_organizer add constraint ap_organizer_topic_idx foreign key (fk_topic_id) references o_ap_topic (id);
alter table o_ap_organizer add constraint ap_organizer_identity_idx foreign key (fk_identity_id) references o_bs_identity (id);
alter table o_ap_topic_to_group add constraint ap_tg_topic_idx foreign key (fk_topic_id) references o_ap_topic (id);
create index idx_ap_tg_group_idx on o_ap_topic_to_group(fk_group_id);
alter table o_ap_appointment add constraint ap_appointment_topic_idx foreign key (fk_topic_id) references o_ap_topic (id);
alter table o_ap_appointment add constraint ap_appointment_meeting_idx foreign key (fk_meeting_id) references o_bbb_meeting (id);
alter table o_ap_appointment add constraint ap_appointment_teams_idx foreign key (fk_teams_id) references o_teams_meeting (id);
alter table o_ap_participation add constraint ap_part_appointment_idx foreign key (fk_appointment_id) references o_ap_appointment (id);
alter table o_ap_participation add constraint ap_part_identity_idx foreign key (fk_identity_id) references o_bs_identity (id);

-- Organiation role rights
alter table o_org_role_to_right add constraint org_role_to_right_to_organisation_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_org_role_to_right_to_organisation_idx on o_org_role_to_right (fk_organisation);

-- Contact tracing
alter table o_ct_registration add constraint reg_to_loc_idx foreign key (fk_location) references o_ct_location (id);
create index idx_reg_to_loc_idx on o_ct_registration (fk_location);
create index idx_qr_id_idx on o_ct_location (l_qr_id);

insert into hibernate_unique_key values ( 0 );
SET FOREIGN_KEY_CHECKS = 1;

