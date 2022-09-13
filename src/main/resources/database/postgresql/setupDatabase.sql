create table o_forum (
   forum_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   f_refresname varchar(50),
   f_refresid bigint,
   primary key (forum_id)
);
create table o_forum_pseudonym (
   id bigserial,
   creationdate timestamp not null,
   p_pseudonym varchar(255) not null,
   p_credential varchar(255) not null,
   p_salt varchar(255) not null,
   p_hashalgorithm varchar(16) not null,
   primary key (id)
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

create table o_bs_group (
   id int8 not null,
   creationdate timestamp not null,
   g_name varchar(36),
   primary key (id)
);

create table o_bs_group_member (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_role varchar(24) not null,
   g_inheritance_mode varchar(16) default 'none' not null,
   fk_group_id int8 not null,
   fk_identity_id int8 not null,
   primary key (id)
);

create table o_bs_grant (
   id int8 not null,
   creationdate timestamp not null,
   g_role varchar(32) not null,
   g_permission varchar(32) not null,
   fk_group_id int8 not null,
   fk_resource_id int8 not null,
   primary key (id)
);

create table o_gp_business (
   group_id int8 not null,
   version int4 not null,
   lastmodified timestamp,
   creationdate timestamp,
   lastusage timestamp,
   status varchar(32) default 'active',
   excludeautolifecycle bool default false not null,
   inactivationdate timestamp,
   inactivationemaildate timestamp,
   reactivationdate timestamp,
   softdeleteemaildate timestamp,
   softdeletedate timestamp,
   data_for_restore text,
   groupname varchar(255),
   technical_type varchar(32) default 'business' not null,
   external_id varchar(64),
   managed_flags varchar(255),
   descr text,
   minparticipants int4,
   maxparticipants int4,
   waitinglist_enabled bool,
   autocloseranks_enabled bool,
   invitations_coach_enabled bool default false not null,
   lti_deployment_coach_enabled bool default false not null,
   ownersintern bool not null default false,
   participantsintern bool not null default false,
   waitingintern bool not null default false,
   ownerspublic bool not null default false,
   participantspublic bool not null default false,
   waitingpublic bool not null default false,
   downloadmembers bool not null default false,
   allowtoleave bool not null default true,
   fk_resource int8 unique,
   fk_group_id int8 unique,
   fk_inactivatedby_id int8,
   fk_softdeletedby_id int8,
   primary key (group_id)
);
create table o_temporarykey (
   reglist_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   email varchar(2000) not null,
   regkey varchar(255) not null,
   ip varchar(255) not null,
   valid_until timestamp,
   mailsent bool not null,
   action varchar(255) not null,
   fk_identity_id int8,
   primary key (reglist_id)
);
create table o_bs_authentication (
   id int8 not null,
   version int4 not null,
   creationdate timestamp,
   lastmodified timestamp not null,
   identity_fk int8 not null,
   provider varchar(8),
   issuer varchar(255) default 'DEFAULT' not null,
   authusername varchar(255),
   credential varchar(255),
   salt varchar(255) default null,
   hashalgorithm varchar(16) default null,
   primary key (id)
);
create table o_bs_authentication_history (
   id bigserial not null,
   creationdate timestamp,
   provider varchar(8),
   authusername varchar(255),
   credential varchar(255),
   salt varchar(255) default null,
   hashalgorithm varchar(16) default null,
   fk_identity int8 not null,
   primary key (id)
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

create table o_bs_identity (
   id int8 not null,
   version int4 not null,
   creationdate timestamp,
   lastlogin timestamp,
   name varchar(128) not null unique,
   external_id varchar(64),
   status integer,
   deleteddate timestamp,
   deletedroles varchar(1024),
   deletedby varchar(128),
   inactivationdate timestamp,
   inactivationemaildate timestamp,
   expirationdate timestamp,
   expirationemaildate timestamp,
   reactivationdate timestamp,
   deletionemaildate timestamp,
   primary key (id)
);
create table o_bs_relation_role (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_role varchar(128) not null,
   g_external_id varchar(128),
   g_external_ref varchar(128),
   g_managed_flags varchar(256),
   primary key (id)
);
create table o_bs_relation_right (
   id bigserial,
   creationdate timestamp not null,
   g_right varchar(128) not null,
   primary key (id)
);
create table o_bs_relation_role_to_right (
   id bigserial,
   creationdate timestamp not null,
   fk_role_id bigint,
   fk_right_id bigint not null,
   primary key (id)
);
create table o_bs_identity_to_identity (
   id bigserial,
   creationdate timestamp not null,
   g_external_id varchar(128),
   g_managed_flags varchar(256),
   fk_source_id bigint not null,
   fk_target_id bigint not null,
   fk_role_id bigint not null,
   primary key (id)
);
create table o_csp_log (
   id bigserial not null,
   creationdate timestamp,
   l_blocked_uri varchar(1024),
   l_disposition varchar(32),
   l_document_uri varchar(1024),
   l_effective_directive text,
   l_original_policy text,
   l_referrer varchar(1024),
   l_script_sample text,
   l_status_code varchar(1024),
   l_violated_directive varchar(1024),
   l_source_file varchar(1024),
   l_line_number int8,
   l_column_number int8,
   fk_identity int8,
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
   style varchar(16),
   externalurl varchar(255),
   fk_repoentry int8,
   fk_ownergroup int8 unique,
   type int4 not null,
   parent_id int8,
   order_index int8,
   short_title varchar(255),
   add_entry_position int,
   add_category_position int,
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
create table o_references (
   reference_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   source_id int8 not null,
   target_id int8 not null,
   userdata varchar(64),
   primary key (reference_id)
);
create table o_user (
   user_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   language varchar(30),
   fontsize varchar(10),
   notification_interval varchar(16),
   presencemessagespublic bool,
   informsessiontimeout bool not null,
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
   u_linkedin varchar(255),
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
   u_employeenumber varchar(255),
   u_organizationalunit varchar(255),

   u_edupersonaffiliation varchar(255),
   u_swissedupersonstaffcategory varchar(255),
   u_swissedupersonhomeorg varchar(255),
   u_swissedupersonstudylevel varchar(255),
   u_swissedupersonhomeorgtype varchar(255),
   u_swissedupersonstudybranch1 varchar(255),
   u_swissedupersonstudybranch2 varchar(255),
   u_swissedupersonstudybranch3 varchar(255),

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

   fk_identity int8,
   primary key (user_id)
);
create table o_userproperty (
   fk_user_id int8 not null,
   propname varchar(255) not null,
   propvalue varchar(255),
   primary key (fk_user_id, propname)
);

create table o_user_data_export (
   id bigserial,
   creationdate timestamp,
   lastmodified timestamp,
   u_directory varchar(255),
   u_status varchar(16),
   u_export_ids varchar(2000),
   fk_identity int8 not null,
   fk_request_by int8,
   primary key (id)
);
create table o_user_data_delete (
   id bigserial,
   creationdate timestamp,
   lastmodified timestamp,
   u_user_data text,
   u_resource_ids text,
   u_current_resource_id varchar(64),
   primary key (id)
);

create table o_user_absence_leave (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   u_absent_from timestamp,
   u_absent_to timestamp,
   u_resname varchar(50),
   u_resid int8,
   u_sub_ident varchar(2048),
   fk_identity int8 not null,
   primary key (id)
);

create table o_message (
   message_id int8 not null,
   version int4 not null,
   lastmodified timestamp,
   creationdate timestamp,
   title varchar(100),
   body text,
   pseudonym varchar(255),
   guest bool not null default false,
   parent_id int8,
   topthread_id int8,
   creator_id int8,
   modifier_id int8,
   modification_date timestamp,
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
   subenabled bool default true,
   primary key (publisher_id),
   unique (fk_publisher, fk_identity)
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
   fk_resource int8 default null,
   primary key (area_id)
);
create table o_repositoryentry (
   repositoryentry_id int8 not null,
   version int4 not null,
   lastmodified timestamp,
   creationdate timestamp,
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
   objectives varchar(32000),
   requirements varchar(32000),
   credits varchar(32000),
   expenditureofwork varchar(32000),
   fk_stats int8 unique not null,
   fk_lifecycle int8,
   fk_olatresource int8 unique,
   description text,
   teaser varchar(255),
   initialauthor varchar(128) not null,
   allowtoleave varchar(16),
   candownload bool not null,
   cancopy bool not null,
   canreference bool not null,
   invitations_owner_enabled bool default false not null,
   lti_deployment_owner_enabled bool default false not null,
   status varchar(16) default 'preparation' not null,
   status_published_date timestamp,
   allusers boolean default false not null,
   guests boolean default false not null,
   bookable boolean default false not null,
   publicvisible bool default false not null,
   deletiondate timestamp default null,
   fk_deleted_by int8 default null,
   fk_educational_type bigint default null,
   primary key (repositoryentry_id)
);
create table o_re_to_group (
   id int8 not null,
   creationdate timestamp not null,
   r_defgroup boolean not null,
   fk_group_id int8 not null,
   fk_entry_id int8 not null,
   primary key (id)
);
create table o_re_to_tax_level (
  id bigserial,
  creationdate timestamp not null,
  fk_entry int8 not null,
  fk_taxonomy_level int8 not null,
  primary key (id)
);
create table o_repositoryentry_cycle (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   r_softkey varchar(64),
   r_label varchar(255),
   r_privatecycle bool default false,
   r_validfrom timestamp,
   r_validto timestamp,
   primary key (id)
);
create table o_repositoryentry_stats (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   r_rating decimal(65,30),
   r_num_of_ratings int8 not null default 0,
   r_num_of_comments int8 not null default 0,
   r_launchcounter int8 not null default 0,
   r_downloadcounter int8 not null default 0,
   r_lastusage timestamp not null,
   primary key (id)
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
create table o_re_educational_type (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   r_identifier varchar(128) not null,
   r_predefined bool not null default false,
   r_css_class varchar(128),
   r_preset_mycourses bool not null default false,
   primary key (id)
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
   id int8 not null,
   version int4 not null,
   creationdate timestamp,
   persistenttypename varchar(50) not null,
   persistentref int8 not null,
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
    windowid varchar(32) default null,
    primary key (lock_id)
);
alter table oc_lock add constraint FK9E30F4B66115906D foreign key (identity_fk) references o_bs_identity;

create table o_readmessage (
    id int8 not null,
    version int4 not null,
    creationdate timestamp,
    identity_id int8 not null,
    forum_id int8 not null,
    message_id int8 not null,
    primary key (id)
);

create table o_loggingtable (
    log_id int8 not null,
    creationdate timestamp,
    sourceclass varchar(255),
    sessionid varchar(255) not null,
    user_id int8,
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
   title varchar(255),
   description text,
   primary key (checklist_id)
);

create table o_checkpoint (
   checkpoint_id int8 not null,
   version int4 not null,
   lastmodified timestamp not null,
   title varchar(255),
   description text,
   modestring varchar(64) not null,
   checklist_fk int8,
   primary key (checkpoint_id)
);

create table o_checkpoint_results (
   checkpoint_result_id int8 not null,
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
   title varchar(150),
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
  message text,
  attachmentpath varchar(1024),
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
   version int4 not null,
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

-- invitation
create table o_bs_invitation (
   id int8 not null,
   creationdate timestamp,
   token varchar(64) not null,
   first_name varchar(64),
   last_name varchar(64),
   mail varchar(128),
   i_type varchar(32) default 'binder' not null,
   i_status varchar(32) default 'active',
   i_url varchar(512),
   i_roles varchar(255),
   i_registration bool default false not null,
   i_additional_infos text,
   fk_group_id int8,
   fk_identity_id int8,
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
   datas_checksum int8,
   datas_path varchar(1024),
   datas_lastmodified timestamp,
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
  autobooking bool not null default false,
  confirmation_email bool default false,
  open_access bool default false not null,
  guest_access bool default false not null,
  catalog_publish bool default false not null,
  catalog_web_publish bool default false not null,
  token varchar(255),
  price_amount DECIMAL,
  price_currency_code VARCHAR(3),
  offer_desc VARCHAR(2000),
  fk_resource_id int8,
  primary key (offer_id)
);

create table o_ac_offer_to_organisation (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  fk_offer int8 not null,
  fk_organisation int8 not null,
  primary key (id)
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

create table o_ac_auto_advance_order (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  a_identifier_key varchar(64) not null,
  a_identifier_value varchar(64) not null,
  a_status varchar(32) not null,
  a_status_modified timestamp not null,
  fk_identity int8 not null,
  fk_method int8 not null,
  primary key (id)
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

create table o_ac_reservation (
   reservation_id int8 NOT NULL,
   creationdate timestamp,
   lastmodified timestamp,
   version int4 not null,
   expirationdate timestamp,
   reservationtype varchar(32),
   fk_identity int8 not null,
   fk_resource int8 not null,
   primary key (reservation_id)
);

create table o_ac_paypal_transaction (
   transaction_id int8 not null,
   version int4 not null,
   creationdate timestamp,
   ref_no varchar(255),
   order_id int8 not null,
   order_part_id int8 not null,
   method_id int8 not null,
   success_uuid varchar(32) not null,
   cancel_uuid varchar(32) not null,
   amount_amount DECIMAL,
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
   trx_amount DECIMAL,
   trx_currency_code VARCHAR(3),
   primary key (transaction_id)
);

-- paypal checkout
create table o_ac_checkout_transaction (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_success_uuid varchar(64) not null,
   p_cancel_uuid varchar(64) not null,
   p_order_nr varchar(64) not null,
   p_order_id int8 not null,
   p_order_part_id int8 not null,
   p_method_id int8 not null,
   p_amount_currency_code varchar(3) not null,
   p_amount_amount decimal not null,
   p_status varchar(32) not null,
   p_paypal_order_id varchar(64),
   p_paypal_order_status varchar(64),
   p_paypal_order_status_reason text,
   p_paypal_authorization_id varchar(64),
   p_paypal_capture_id varchar(64),
   p_capture_currency_code varchar(3),
   p_capture_amount decimal,
   p_paypal_invoice_id varchar(64),
   primary key (id)
);

-- Catalog V2
create table o_ca_launcher (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_type varchar(50),
   c_identifier varchar(32),
   c_sort_order int8,
   c_enabled bool not null default true,
   c_config varchar(4000),
   primary key (id)
);
create table o_ca_launcher_to_organisation (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   fk_launcher int8 not null,
   fk_organisation int8 not null,
   primary key (id)
);
create table o_ca_filter (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_type varchar(50),
   c_sort_order int8,
   c_enabled bool not null default true,
   c_default_visible bool not null default true,
   c_config varchar(4000),
   primary key (id)
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

-- openmeetings
create table o_om_room_reference (
   id int8 not null,
   version int4 not null,
   lastmodified timestamp,
   creationdate timestamp,
   businessgroup int8,
   resourcetypename varchar(50),
   resourcetypeid int8,
   ressubpath varchar(255),
   roomId int8,
   config text,
   primary key (id)
);

-- Adobe Connect
create table o_aconnect_meeting (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_sco_id varchar(128) default null,
   a_folder_id varchar(128) default null,
   a_env_name varchar(128) default null,
   a_name varchar(128) not null,
   a_description varchar(2000) default null,
   a_permanent bool default false not null,
   a_start_date timestamp default null,
   a_leadtime bigint default 0 not null,
   a_start_with_leadtime timestamp,
   a_end_date timestamp default null,
   a_followuptime bigint default 0 not null,
   a_end_with_followuptime timestamp,
   a_opened bool default false not null,
   a_template_id varchar(32) default null,
   a_shared_documents varchar(2000) default null,
   fk_entry_id int8 default null,
   a_sub_ident varchar(64) default null,
   fk_group_id int8 default null,
   primary key (id)
);

create table o_aconnect_user (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_principal_id varchar(128) default null,
   a_env_name varchar(128) default null,
   fk_identity_id int8 default null,
   primary key (id)
);

-- BigBlueButton
create table o_bbb_template (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   b_name varchar(128) not null,
   b_description varchar(2000) default null,
   b_system bool default false not null,
   b_enabled bool default true not null,
   b_external_id varchar(255) default null,
   b_external_users bool default true not null,
   b_max_concurrent_meetings int default null,
   b_max_participants int8 default null,
   b_max_duration int8 default null,
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
   b_join_policy varchar(32) default 'disabled' not null,
   b_guest_policy varchar(32) default null,
   primary key (id)
);

create table o_bbb_server (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   b_name varchar(128),
   b_url varchar(255) not null,
   b_shared_secret varchar(255),
   b_recording_url varchar(255),
   b_enabled bool default true,
   b_manual_only bool default false not null,
   b_capacity_factor decimal,
   primary key (id)
);

create table o_bbb_meeting (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   b_meeting_id varchar(128) not null,
   b_attendee_pw varchar(128) not null,
   b_moderator_pw varchar(128) not null,
   b_name varchar(128) not null,
   b_description varchar(2000) default null,
   b_welcome text,
   b_layout varchar(16) default 'standard',
   b_permanent bool default false not null,
   b_guest bool default false not null,
   b_identifier varchar(64),
   b_read_identifier varchar(64),
   b_password varchar(64),
   b_start_date timestamp default null,
   b_leadtime bigint default 0 not null,
   b_start_with_leadtime timestamp,
   b_end_date timestamp default null,
   b_followuptime bigint default 0 not null,
   b_end_with_followuptime timestamp,
   b_main_presenter varchar(255),
   b_directory varchar(64) default null,
   b_recordings_publishing varchar(128) default 'all',
   b_record bool default null,
   b_join_policy varchar(32) default 'disabled' not null,
   fk_creator_id int8,
   fk_entry_id int8 default null,
   a_sub_ident varchar(64) default null,
   fk_group_id int8 default null,
   fk_template_id int8 default null,
   fk_server_id int8 default null,
   primary key (id)
);

create table o_bbb_attendee (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   b_role varchar(32),
   b_join_date timestamp,
   b_pseudo varchar(255),
   fk_identity_id int8,
   fk_meeting_id int8 not null,
   primary key (id)
);

create table o_bbb_recording (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   b_recording_id varchar(255) not null,
   b_publish_to varchar(128),
   b_permanent bool default null,
   b_start_date timestamp default null,
   b_end_date timestamp default null,
   b_url varchar(1024),
   b_type varchar(32),
   fk_meeting_id int8 not null,
   unique(b_recording_id,fk_meeting_id),
   primary key (id)
);

-- Teams
create table o_teams_meeting (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   t_subject varchar(255),
   t_description varchar(4000),
   t_main_presenter varchar(255),
   t_start_date timestamp default null,
   t_leadtime int8 default 0 not null,
   t_start_with_leadtime timestamp,
   t_end_date timestamp default null,
   t_followuptime int8 default 0 not null,
   t_end_with_followuptime timestamp,
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
   fk_entry_id int8 default null,
   a_sub_ident varchar(64) default null,
   fk_group_id int8 default null,
   fk_creator_id int8 default null,
   primary key (id)
);

create table o_teams_user (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   t_identifier varchar(128),
   t_displayname varchar(512),
   fk_identity_id int8 default null,
   unique(fk_identity_id),
   primary key (id)
);

create table o_teams_attendee (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   t_role varchar(32),
   t_join_date timestamp not null,
   fk_identity_id int8 default null,
   fk_teams_user_id int8 default null,
   fk_meeting_id int8 not null,
   primary key (id)

);

-- efficiency statments
create table o_as_eff_statement (
   id int8 not null,
   version int4 not null,
   lastmodified timestamp,
   lastcoachmodified timestamp,
   lastusermodified timestamp,
   creationdate timestamp,
   passed boolean,
   score float4,
   grade varchar(100),
   grade_system_ident varchar(64),
   performance_class_ident varchar(50),
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

create table o_as_entry (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   lastcoachmodified timestamp,
   lastusermodified timestamp,
   a_attemtps int8 default null,
   a_last_attempt timestamp,
   a_score decimal default null,
   a_max_score decimal default null,
   a_grade varchar(100),
   a_grade_system_ident varchar(64),
   a_performance_class_ident varchar(50),
   a_passed bool default null,
   a_passed_original bool,
   a_passed_mod_date timestamp,
   a_status varchar(16) default null,
   a_date_done timestamp,
   a_details varchar(1024) default null,
   a_user_visibility bool,
   a_share bool,
   a_fully_assessed bool default null,
   a_date_fully_assessed timestamp,
   a_assessment_id int8 default null,
   a_completion float(24),
   a_current_run_completion float(24),
   a_current_run_status varchar(16),
   a_current_run_start timestamp,
   a_comment text,
   a_coach_comment text,
   a_num_assessment_docs int8 not null default 0,
   a_date_start timestamp,
   a_date_end timestamp,
   a_date_end_original timestamp,
   a_date_end_mod_date timestamp,
   a_duration int8,
   a_obligation varchar(50),
   a_obligation_inherited varchar(50),
   a_obligation_evaluated varchar(50),
   a_obligation_config varchar(50),
   a_obligation_original varchar(50),
   a_obligation_mod_date timestamp,
   a_first_visit timestamp,
   a_last_visit timestamp,
   a_num_visits int8,
   fk_entry int8 not null,
   a_subident varchar(512),
   a_entry_root bool,
   fk_reference_entry int8,
   fk_identity int8 default null,
   fk_identity_passed_mod int8,
   fk_identity_end_date_mod int8,
   fk_identity_obligation_mod int8,
   fk_identity_status_done int8,
   a_anon_identifier varchar(128) default null,
   primary key (id),
   unique(fk_identity, fk_entry, a_subident)
);

create table o_as_score_accounting_trigger (
   id bigserial,
   creationdate timestamp not null,
   e_identifier varchar(64) not null,
   e_business_group_key int8,
   e_organisation_key int8,
   e_curriculum_element_key int8,
   e_user_property_name varchar(64),
   e_user_property_value varchar(128),
   fk_entry int8 not null,
   e_subident varchar(64) not null,
   primary key (id)
);

create table o_as_compensation (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_subident varchar(512),
   a_subident_name varchar(512),
   a_extra_time int8 not null,
   a_approved_by varchar(2000),
   a_approval timestamp,
   a_status varchar(32),
   fk_identity bigint not null,
   fk_creator bigint not null,
   fk_entry bigint not null,
   primary key (id)
);

create table o_as_compensation_log (
   id bigserial,
   creationdate timestamp not null,
   a_action varchar(32) not null,
   a_val_before text,
   a_val_after text,
   a_subident varchar(512),
   fk_entry_id bigint not null,
   fk_identity_id bigint not null,
   fk_compensation_id bigint not null,
   fk_author_id bigint,
   primary key (id)
);

create table o_as_mode_course (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_name varchar(255),
   a_description text,
   a_external_id varchar(64),
   a_managed_flags varchar(255),
   a_status varchar(16),
   a_end_status varchar(32),
   a_manual_beginend bool not null default false,
   a_begin timestamp not null,
   a_leadtime int8 not null default 0,
   a_begin_with_leadtime timestamp not null,
   a_end timestamp not null,
   a_followuptime int8 not null default 0,
   a_end_with_followuptime timestamp not null,
   a_targetaudience varchar(16),
   a_restrictaccesselements bool not null default false,
   a_elements varchar(32000),
   a_start_element varchar(64),
   a_restrictaccessips bool not null default false,
   a_ips varchar(32000),
   a_safeexambrowser bool not null default false,
   a_safeexambrowserkey varchar(32000),
   a_safeexambrowserconfig_xml text,
   a_safeexambrowserconfig_plist text,
   a_safeexambrowserconfig_pkey varchar(255),
   a_safeexambrowserconfig_dload bool default true not null,
   a_safeexambrowserhint text,
   a_applysettingscoach bool not null default false,
   fk_entry int8 not null,
   fk_lecture_block int8,
   primary key (id)
);

create table o_as_mode_course_to_group (
   id int8 not null,
   fk_assessment_mode_id int8 not null,
   fk_group_id int8 not null,
   primary key (id)
);

create table o_as_mode_course_to_area (
   id int8 not null,
   fk_assessment_mode_id int8 not null,
   fk_area_id int8 not null,
   primary key (id)
);

create table o_as_mode_course_to_cur_el (
   id bigserial,
   fk_assessment_mode_id int8 not null,
   fk_cur_element_id int8 not null,
   primary key (id)
);

-- Assessment message
create table o_as_message (
   id bigserial,
   lastmodified timestamp not null,
   creationdate timestamp not null,
   a_message varchar(2000) not null,
   a_publication_date timestamp not null,
   a_expiration_date timestamp not null,
   a_publication_type varchar(32) not null default 'asap',
   a_message_sent bool not null default false,
   fk_entry int8 not null,
   fk_author int8,
   a_ressubpath varchar(255),
   primary key (id)
);

create table o_as_message_log (
   id bigserial,
   lastmodified timestamp not null,
   creationdate timestamp not null,
   a_read bool not null default false,
   fk_message int8 not null,
   fk_identity int8 not null,
   primary key (id)
);

-- Certificate
create table o_cer_template (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_name varchar(256) not null,
   c_path varchar(1024) not null,
   c_public bool not null,
   c_format varchar(16),
   c_orientation varchar(16),
   primary key (id)
);

create table o_cer_certificate (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_status varchar(16) not null default 'pending',
   c_email_status varchar(16),
   c_uuid varchar(36) not null,
   c_external_id varchar(64),
   c_managed_flags varchar(255),
   c_next_recertification timestamp,
   c_path varchar(1024),
   c_last bool not null default true,
   c_course_title varchar(255),
   c_archived_resource_id int8 not null,
   fk_olatresource int8,
   fk_identity int8 not null,
   primary key (id)
);

-- Grade
create table o_gr_grade_system (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_identifier varchar(64) not null,
   g_predefined bool not null default false,
   g_has_passed bool not null default false,
   g_type varchar(32) not null,
   g_enabled bool not null default true,
   g_resolution varchar(32),
   g_rounding varchar(32),
   g_best_grade int8,
   g_lowest_grade int8,
   g_cut_value decimal(65,30),
   primary key (id)
);

create table o_gr_performance_class (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_identifier varchar(50),
   g_best_to_lowest int8,
   g_passed bool not null default false,
   fk_grade_system int8 not null,
   primary key (id)
);

create table o_gr_grade_scale (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_min_score decimal(65,30),
   g_max_score decimal(65,30),
   fk_grade_system int8,
   fk_entry int8 not null,
   g_subident varchar(64) not null,
   primary key (id)
);

create table o_gr_breakpoint (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_score decimal(65,30),
   g_grade varchar(50),
   g_best_to_lowest int8,
   fk_grade_scale int8 not null,
   primary key (id)
);

-- gotomeeting
create table o_goto_organizer (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_name varchar(128) default null,
   g_account_key varchar(128) default null,
   g_access_token varchar(4000) not null,
   g_renew_date timestamp not null,
   g_refresh_token varchar(4000),
   g_renew_refresh_date timestamp,
   g_organizer_key varchar(128) not null,
   g_username varchar(128) not null,
   g_firstname varchar(128) default null,
   g_lastname varchar(128) default null,
   g_email varchar(128) default null,
   fk_identity int8 default null,
   primary key (id)
);

create table o_goto_meeting (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_external_id varchar(128) default null,
   g_type varchar(16) not null,
   g_meeting_key varchar(128) not null,
   g_name varchar(255) default null,
   g_description varchar(2000) default null,
   g_start_date timestamp default null,
   g_end_date timestamp default null,
   fk_organizer_id int8 not null,
   fk_entry_id int8 default null,
   g_sub_ident varchar(64) default null,
   fk_group_id int8 default null,
   primary key (id)
);

create table o_goto_registrant (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_status varchar(16) default null,
   g_join_url varchar(1024) default null,
   g_confirm_url varchar(1024) default null,
   g_registrant_key varchar(64) default null,
   fk_meeting_id int8 not null,
   fk_identity_id int8 not null,
   primary key (id)
);

create table o_vid_transcoding (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   vid_resolution int8 default null,
   vid_width int8 default null,
   vid_height int8 default null,
   vid_size int8 default null,
   vid_format varchar(128) default null,
   vid_status int8 default null,
   vid_transcoder varchar(128) default null,
   fk_resource_id int8 not null,
   primary key (id)
);

create table o_vid_metadata (
  id bigserial not null,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  vid_width int8 default null,
  vid_height int8 default null,
  vid_size int8 default null,
  vid_format varchar(32) default null,
  vid_length varchar(32) default null,
  vid_url varchar(512) default null,
  vid_download_enabled boolean not null default false,
  fk_resource_id int8 not null,
  primary key (id)
);

-- calendar
create table o_cal_use_config (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_calendar_id varchar(128) not null,
   c_calendar_type varchar(16) not null,
   c_token varchar(36),
   c_cssclass varchar(36),
   c_visible bool default true,
   c_aggregated_feed bool default true,
   fk_identity int8 not null,
   primary key (id),
   unique (c_calendar_id, c_calendar_type, fk_identity)
);

create table o_cal_import (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_calendar_id varchar(128) not null,
   c_calendar_type varchar(16) not null,
   c_displayname varchar(256),
   c_lastupdate timestamp not null,
   c_url varchar(1024),
   fk_identity int8,
   primary key (id)
);

create table o_cal_import_to (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_to_calendar_id varchar(128) not null,
   c_to_calendar_type varchar(16) not null,
   c_lastupdate timestamp not null,
   c_url varchar(1024),
   primary key (id)
);

-- instant messaging
create table o_im_message (
   id int8 not null,
   creationdate timestamp,
   msg_resname varchar(50) not null,
   msg_resid int8 not null,
   msg_ressubpath varchar(255),
   msg_channel varchar(255),
   msg_type varchar(16) not null default 'text',
   msg_anonym bool default false,
   msg_from varchar(255) not null,
   msg_body text,
   fk_from_identity_id int8 not null,
   fk_meeting_id int8,
   fk_teams_id int8,
   primary key (id)
);

create table o_im_notification (
   id int8 not null,
   creationdate timestamp,
   chat_resname varchar(50) not null,
   chat_resid int8 not null,
   chat_ressubpath varchar(255),
   chat_channel varchar(255),
   chat_type varchar(16) not null default 'message',
   fk_to_identity_id int8 not null,
   fk_from_identity_id int8 not null,
   primary key (id)
);

create table o_im_roster_entry (
   id int8 not null,
   creationdate timestamp,
   r_resname varchar(50) not null,
   r_resid int8 not null,
   r_ressubpath varchar(255),
   r_channel varchar(255),
   r_nickname varchar(255),
   r_fullname varchar(255),
   r_vip bool default false,
   r_anonym bool default false,
   r_persistent bool not null default false,
   r_active bool not null default true,
   r_read_upto timestamp default null,
   fk_identity_id int8 not null,
   primary key (id)
);

create table o_im_preferences (
   id int8 not null,
   creationdate timestamp,
   visible_to_others bool default false,
   roster_def_status varchar(12),
   fk_from_identity_id int8 not null,
   primary key (id)
);

-- add mapper table
create table o_mapper (
   id int8 not null,
   lastmodified timestamp,
   creationdate timestamp,
   expirationdate timestamp,
   mapper_uuid varchar(64),
   orig_session_id varchar(64),
   xml_config TEXT,
   primary key (id)
);

-- qti 2.1
create table o_qti_assessmenttest_session (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_exploded bool default false,
   q_cancelled bool default false not null,
   q_author_mode bool default false,
   q_finish_time timestamp,
   q_termination_time timestamp,
   q_duration int8,
   q_score decimal default null,
   q_manual_score decimal default null,
   q_max_score decimal default null,
   q_passed bool default null,
   q_num_questions int8,
   q_num_answered_questions int8,
   q_extra_time int8,
   q_compensation_extra_time int8,
   q_storage varchar(1024),
   fk_reference_entry int8 not null,
   fk_entry int8,
   q_subident varchar(255),
   fk_identity int8 default null,
   q_anon_identifier varchar(128) default null,
   fk_assessment_entry int8 not null,
   primary key (id)
);

create table o_qti_assessmentitem_session (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_itemidentifier varchar(255) not null,
   q_sectionidentifier varchar(255) default null,
   q_testpartidentifier varchar(255) default null,
   q_duration int8,
   q_score decimal default null,
   q_manual_score decimal default null,
   q_coach_comment text default null,
   q_to_review bool default false,
   q_passed bool default null,
   q_storage varchar(1024),
   q_attempts int8 default null,
   q_externalrefidentifier varchar(64) default null,
   fk_assessmenttest_session int8 not null,
   primary key (id)
);

create table o_qti_assessment_response (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_responseidentifier varchar(255) not null,
   q_responsedatatype varchar(16) not null,
   q_responselegality varchar(16) not null,
   q_stringuifiedresponse text,
   fk_assessmentitem_session int8 not null,
   fk_assessmenttest_session int8 not null,
   primary key (id)
);

create table o_qti_assessment_marks (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_marks text default null,
   q_hidden_rubrics text default null,
   fk_reference_entry int8 not null,
   fk_entry int8,
   q_subident varchar(64),
   fk_identity int8 not null,
   primary key (id)
);

create table o_practice_resource (
   id bigserial,
   lastmodified timestamp not null,
   creationdate timestamp not null,
   fk_entry int8 not null,
   p_subident varchar(64) not null,
   fk_test_entry int8,
   fk_item_collection int8,
   fk_pool int8,
   fk_resource_share int8,
   primary key (id)
);

create table o_practice_global_item_ref (
   id bigserial,
   lastmodified timestamp not null,
   creationdate timestamp not null,
   p_identifier varchar(64) not null,
   p_level int8 default 0,
   p_attempts int8 default 0,
   p_correct_answers int8 default 0,
   p_incorrect_answers int8 default 0,
   p_last_attempt_date timestamp,
   p_last_attempt_passed bool default null,
   fk_identity int8 not null,
   primary key (id)
);

-- vfs metadata
create table o_vfs_metadata (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   f_uuid varchar(64) not null,
   f_deleted bool default false not null,
   f_filename varchar(256) not null,
   f_relative_path varchar(2048) not null,
   f_directory bool default false,
   f_lastmodified timestamp not null,
   f_size bigint default 0,
   f_uri varchar(2000) not null,
   f_uri_protocol varchar(16) not null,
   f_cannot_thumbnails bool default false,
   f_download_count bigint default 0,
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
   f_license_text text,
   f_licensor varchar(4000),
   f_locked_date timestamp,
   f_locked bool default false,
   f_expiration_date timestamp,
   f_revision_nr bigint default 0 not null,
   f_revision_temp_nr bigint,
   f_revision_comment varchar(32000),
   f_migrated varchar(12),
   f_m_path_keys varchar(1024),
   fk_locked_identity bigint,
   fk_license_type bigint,
   fk_initialized_by bigint,
   fk_lastmodified_by bigint,
   fk_parent bigint,
   primary key (id)
);

create table o_vfs_thumbnail (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
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
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   f_revision_size bigint default 0 not null,
   f_revision_nr bigint default 0 not null,
   f_revision_temp_nr bigint,
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
   f_license_text text,
   f_licensor varchar(4000),
   fk_license_type bigint,
   fk_initialized_by bigint,
   fk_lastmodified_by bigint,
   fk_metadata bigint not null,
   primary key (id)
);

create table o_vfs_statistics (
   id bigserial,
   creationdate timestamp not null,
   f_files_amount int8 default 0,
   f_files_size int8 default 0,
   f_trash_amount int8 default 0,
   f_trash_size int8 default 0,
   f_revisions_amount int8 default 0,
   f_revisions_size int8 default 0,
   f_thumbnails_amount int8 default 0,
   f_thumbnails_size int8 default 0,
   primary key (id)
);

-- Document editor
create table o_de_access (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   o_editor_type varchar(64) not null,
   o_expires_at timestamp not null,
   o_mode varchar(64) not null,
   o_edit_start_date timestamp,
   o_version_controlled bool not null,
   o_download bool default true,
   fk_metadata bigint not null,
   fk_identity bigint not null,
   primary key (id)
);

-- used in fxOpenOlat
create table o_de_user_info (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   o_info varchar(2048) not null,
   fk_identity bigint not null,
   primary key (id)
);

-- portfolio
create table o_pf_binder (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_title varchar(255),
   p_status varchar(32),
   p_copy_date timestamp,
   p_return_date timestamp,
   p_deadline timestamp,
   p_summary text,
   p_image_path varchar(255),
   fk_olatresource_id int8,
   fk_group_id int8 not null,
   fk_entry_id int8,
   p_subident varchar(128),
   fk_template_id int8,
   primary key (id)
);

create table o_pf_section (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   pos int8 default null,
   p_title varchar(255),
   p_description text,
   p_status varchar(32) not null default 'notStarted',
   p_begin timestamp,
   p_end timestamp,
   p_override_begin_end bool default false,
   fk_group_id int8 not null,
   fk_binder_id int8 not null,
   fk_template_reference_id int8,
   primary key (id)
);

create table o_pf_page (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   pos int8 default null,
   p_editable bool default true,
   p_title varchar(255),
   p_summary text,
   p_status varchar(32),
   p_image_path varchar(255),
   p_image_align varchar(32),
   p_version int8 default 0,
   p_initial_publish_date timestamp,
   p_last_publish_date timestamp,
   fk_body_id int8 not null,
   fk_group_id int8 not null,
   fk_section_id int8,
   primary key (id)
);

create table o_pf_page_body (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_usage int8 default 0,
   p_synthetic_status varchar(32),
   primary key (id)
);

create table o_pf_page_part (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   pos int8 default null,
   dtype varchar(32),
   p_content text,
   p_flow varchar(32),
   p_layout_options text,
   fk_media_id int8,
   fk_page_body_id int8,
   fk_form_entry_id int8 default null,
   primary key (id)
);

create table o_pf_media (
   id bigserial,
   creationdate timestamp not null,
   p_collection_date timestamp not null,
   p_type varchar(64) not null,
   p_storage_path varchar(255),
   p_root_filename varchar(255),
   p_title varchar(255) not null,
   p_description text,
   p_content text,
   p_signature int8 not null default 0,
   p_reference_id varchar(255) default null,
   p_business_path varchar(255) default null,
   p_creators varchar(1024) default null,
   p_place varchar(255) default null,
   p_publisher varchar(255) default null,
   p_publication_date timestamp default null,
   p_date varchar(32) default null,
   p_url varchar(1024) default null,
   p_source varchar(1024) default null,
   p_language varchar(32) default null,
   p_metadata_xml text,
   fk_author_id int8 not null,
   primary key (id)
);

create table o_pf_category (
   id bigserial,
   creationdate timestamp not null,
   p_name varchar(32),
   primary key (id)
);

create table o_pf_category_relation (
   id bigserial,
   creationdate timestamp not null,
   p_resname varchar(64) not null,
   p_resid int8 not null,
   fk_category_id int8 not null,
   primary key (id)
);

create table o_pf_assessment_section (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_score decimal default null,
   p_passed bool default null,
   p_comment text,
   fk_section_id int8 not null,
   fk_identity_id int8 not null,
   primary key (id)
);

create table o_pf_assignment (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   pos int8 default null,
   p_status varchar(32) default null,
   p_type varchar(32) not null,
   p_version int8 not null default 0,
   p_template bool default false,
   p_title varchar(255) default null,
   p_summary text,
   p_content text,
   p_storage varchar(255) default null,
   fk_section_id int8,
   fk_binder_id int8,
   fk_template_reference_id int8,
   fk_page_id int8,
   fk_assignee_id int8,
   p_only_auto_eva bool default true,
   p_reviewer_see_auto_eva bool default false,
   p_anon_extern_eva bool default true,
   fk_form_entry_id int8 default null,
   primary key (id)
);

create table o_pf_binder_user_infos (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_initiallaunchdate timestamp,
   p_recentlaunchdate timestamp,
   p_visit int4,
   fk_identity int8,
   fk_binder int8,
   unique(fk_identity, fk_binder),
   primary key (id)
);

create table o_pf_page_user_infos (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  p_mark bool default false,
  p_status varchar(16) not null default 'incoming',
  p_recentlaunchdate timestamp not null,
  fk_identity_id int8 not null,
  fk_page_id int8 not null,
  primary key (id)
);

create table o_pf_page_to_tax_competence (
  id bigserial,
  creationdate timestamp not null,
  fk_tax_competence int8 not null,
  fk_pf_page int8 not null,
  primary key (id)
);

-- evaluation form
create table o_eva_form_survey (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
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
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   e_identifier_type varchar(50) not null,
   e_identifier_key varchar(50) not null,
   e_status varchar(20) not null,
   e_anonymous bool not null,
   fk_executor bigint,
   fk_survey bigint not null,
   primary key (id)
);

create table o_eva_form_session (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   e_status varchar(16),
   e_submission_date timestamp,
   e_first_submission_date timestamp,
   e_email varchar(1024),
   e_firstname varchar(1024),
   e_lastname varchar(1024),
   e_age varchar(1024),
   e_gender varchar(1024),
   e_org_unit varchar(1024),
   e_study_subject varchar(1024),
   fk_survey bigint,
   fk_participation bigint unique,
   fk_identity int8,
   fk_page_body int8,
   fk_form_entry int8,
   primary key (id)
);

create table o_eva_form_response (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   e_no_response bool default false,
   e_responseidentifier varchar(64) not null,
   e_numericalresponse decimal default null,
   e_stringuifiedresponse text,
   e_file_response_path varchar(4000),
   fk_session int8 not null,
   primary key (id)
);

-- quality management
create table o_qual_data_collection (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_status varchar(50),
   q_title varchar(200),
   q_start timestamp,
   q_deadline timestamp,
   q_topic_type varchar(50),
   q_topic_custom varchar(200),
   q_topic_fk_identity int8,
   q_topic_fk_organisation int8,
   q_topic_fk_curriculum int8,
   q_topic_fk_curriculum_element int8,
   q_topic_fk_repository int8,
   fk_generator bigint,
   q_generator_provider_key bigint,
   primary key (id)
);

create table o_qual_data_collection_to_org (
   id bigserial,
   creationdate timestamp not null,
   fk_data_collection bigint not null,
   fk_organisation bigint not null,
   primary key (id)
);

create table o_qual_context (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
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
   id bigserial,
   creationdate timestamp not null,
   fk_context bigint not null,
   fk_organisation bigint not null,
   primary key (id)
);

create table o_qual_context_to_curriculum (
   id bigserial,
   creationdate timestamp not null,
   fk_context bigint not null,
   fk_curriculum bigint not null,
   primary key (id)
);

create table o_qual_context_to_cur_element (
   id bigserial,
   creationdate timestamp not null,
   fk_context bigint not null,
   fk_cur_element bigint not null,
   primary key (id)
);

create table o_qual_context_to_tax_level (
   id bigserial,
   creationdate timestamp not null,
   fk_context bigint not null,
   fk_tax_leveL bigint not null,
   primary key (id)
);

create table o_qual_reminder (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_type varchar(65),
   q_send_planed timestamp,
   q_send_done timestamp,
   fk_data_collection bigint not null,
   primary key (id)
);

create table o_qual_report_access (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  q_type varchar(64),
  q_role varchar(64),
  q_online bool default false,
  q_email_trigger varchar(64),
  fk_data_collection bigint,
  fk_generator bigint,
  fk_group bigint,
  primary key (id)
);

create table o_qual_generator (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_title varchar(256),
   q_type varchar(64) not null,
   q_enabled bool not null,
   q_last_run timestamp,
   fk_form_entry bigint,
   primary key (id)
);

create table o_qual_generator_config (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_identifier varchar(50) not null,
   q_value text,
   fk_generator bigint not null,
   primary key (id)
);

create table o_qual_generator_to_org (
   id bigserial,
   creationdate timestamp not null,
   fk_generator bigint not null,
   fk_organisation bigint not null,
   primary key (id)
);

create table o_qual_analysis_presentation (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
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

-- question item
create table o_qp_pool (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_name varchar(255) not null,
   q_public boolean default false,
   fk_ownergroup int8,
   primary key (id)
);

create table o_qp_taxonomy_level (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_field varchar(255) not null,
   q_mat_path_ids varchar(1024),
   q_mat_path_names varchar(2048),
   fk_parent_field int8,
   primary key (id)
);

create table o_qp_item (
   id int8 not null,
   q_identifier varchar(36) not null,
   q_master_identifier varchar(36),
   q_title varchar(1024) not null,
   q_topic varchar(1024),
   q_description varchar(2048),
   q_keywords varchar(1024),
   q_coverage varchar(1024),
   q_additional_informations varchar(256),
   q_language varchar(16),
   fk_edu_context int8,
   q_educational_learningtime varchar(32),
   fk_type int8,
   q_difficulty decimal(10,9),
   q_stdev_difficulty decimal(10,9),
   q_differentiation decimal(10,9),
   q_num_of_answers_alt int8 not null default 0,
   q_usage int8 not null default 0,
   q_correction_time int8 default null,
   q_assessment_type varchar(64),
   q_status varchar(32) not null,
   q_version varchar(50),
   fk_license int8,
   q_editor varchar(256),
   q_editor_version varchar(256),
   q_format varchar(32) not null,
   q_creator varchar(1024),
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_status_last_modified timestamp,
   q_dir varchar(32),
   q_root_filename varchar(255),
   fk_taxonomy_level int8,
   fk_taxonomy_level_v2 int8,
   fk_ownergroup int8 not null,
   primary key (id)
);

create table o_qp_item_audit_log (
  id bigserial,
  creationdate timestamp not null,
  q_action varchar(64),
  q_val_before text,
  q_val_after text,
  q_lic_before text,
  q_lic_after text,
  q_message text,
  fk_author_id int8,
  fk_item_id int8,
  primary key (id)
);

create table o_qp_pool_2_item (
   id int8 not null,
   creationdate timestamp not null,
   q_editable boolean default false,
   fk_pool_id int8 not null,
   fk_item_id int8 not null,
   primary key (id)
);

create table o_qp_share_item (
   id int8 not null,
   creationdate timestamp not null,
   q_editable boolean default false,
   fk_resource_id int8 not null,
   fk_item_id int8 not null,
   primary key (id)
);

create table o_qp_item_collection (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_name varchar(256),
   fk_owner_id int8 not null,
   primary key (id)
);

create table o_qp_collection_2_item (
   id int8 not null,
   creationdate timestamp not null,
   fk_collection_id int8 not null,
   fk_item_id int8 not null,
   primary key (id)
);

create table o_qp_edu_context (
   id int8 not null,
   creationdate timestamp not null,
   q_level varchar(256) not null,
   q_deletable boolean default false,
   primary key (id)
);

create table o_qp_item_type (
   id int8 not null,
   creationdate timestamp not null,
   q_type varchar(256) not null,
   q_deletable boolean default false,
   primary key (id)
);

create table o_qp_license (
   id int8 not null,
   creationdate timestamp not null,
   q_license varchar(256) not null,
   q_text varchar(2048),
   q_deletable boolean default false,
   primary key (id)
);

-- LTI
create table o_lti_outcome (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   r_ressubpath varchar(2048),
   r_action varchar(255) not null,
   r_outcome_key varchar(255) not null,
   r_outcome_value varchar(2048),
   fk_resource_id int8 not null,
   fk_identity_id int8 not null,
   primary key (id)
);

-- LTI 1.3
create table o_lti_tool (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_tool_type varchar(16) default 'EXTERNAL' not null,
   l_tool_url varchar(2000) not null,
   l_tool_name varchar(255) not null,
   l_client_id varchar(128) not null,
   l_public_key text,
   l_public_key_url varchar(2000),
   l_public_key_type varchar(16),
   l_initiate_login_url varchar(2000),
   l_redirect_url varchar(2000),
   primary key (id)
);

create table o_lti_tool_deployment (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_deployment_id varchar(128) not null unique,
   l_context_id varchar(255),
   l_target_url varchar(1024),
   l_send_attributes varchar(2048),
   l_send_custom_attributes text,
   l_author_roles varchar(2048),
   l_coach_roles varchar(2048),
   l_participant_roles varchar(2048),
   l_assessable bool default false not null,
   l_nrps bool default true not null,
   l_display varchar(32),
   l_display_height varchar(32),
   l_display_width varchar(32),
   l_skip_launch_page bool default false not null,
   fk_tool_id int8 not null,
   fk_entry_id int8,
   l_sub_ident varchar(64),
   fk_group_id int8,
   primary key (id)
);

create table o_lti_platform (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_name varchar(255),
   l_mail_matching bool default false not null,
   l_scope varchar(32) default 'SHARED' not null,
   l_issuer varchar(255) not null,
   l_client_id varchar(128) not null,
   l_key_id varchar(64) not null,
   l_public_key text not null,
   l_private_key text not null,
   l_authorization_uri varchar(2000),
   l_token_uri varchar(2000),
   l_jwk_set_uri varchar(2000),
   primary key (id)
);

create table o_lti_shared_tool_deployment (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_deployment_id varchar(255),
   fk_platform_id int8,
   fk_entry_id int8,
   fk_group_id int8,
   primary key (id)
);

create table o_lti_shared_tool_service (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_context_id varchar(255),
   l_service_type varchar(16) not null,
   l_service_endpoint varchar(255) not null,
   fk_deployment_id int8 not null,
   primary key (id)
);

create table o_lti_key (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_key_id varchar(255),
   l_public_key text,
   l_private_key text,
   l_algorithm varchar(64) not null,
   l_issuer varchar(1024) not null,
   primary key (id)
);


create table o_cl_checkbox (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_checkboxid varchar(50) not null,
   c_resname varchar(50) not null,
   c_resid int8 not null,
   c_ressubpath varchar(255) not null,
   primary key (id)
);

create table o_cl_check (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_score float(24),
   c_checked boolean default false,
   fk_identity_id int8 not null,
   fk_checkbox_id int8 not null,
   primary key (id)
);

create table o_gta_task_list (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_course_node_ident varchar(36),
   fk_entry int8 not null,
   primary key (id)
);

create table o_gta_task (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_status varchar(36),
   g_rev_loop int4 not null default 0,
   g_assignment_date timestamp,
   g_submission_date timestamp,
   g_submission_ndocs int8,
   g_submission_revisions_date timestamp,
   g_submission_revisions_ndocs int8,
   g_collection_date timestamp,
   g_collection_ndocs int8,
   g_acceptation_date timestamp,
   g_solution_date timestamp,
   g_graduation_date timestamp,
   g_allow_reset_date timestamp,
   g_assignment_due_date timestamp,
   g_submission_due_date timestamp,
   g_revisions_due_date timestamp,
   g_solution_due_date timestamp,
   g_taskname varchar(1024),
   fk_tasklist int8 not null,
   fk_identity int8,
   fk_businessgroup int8,
   fk_allow_reset_identity int8,
   primary key (id)
);

create table o_gta_task_revision (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_status varchar(36) not null,
   g_rev_loop int4 not null default 0,
   g_date timestamp,
   g_rev_comment text,
   g_rev_comment_lastmodified timestamp,
   fk_task int8 not null,
   fk_comment_author int8,
   primary key (id)
);

create table o_gta_task_revision_date (
  id bigserial not null,
  creationdate timestamp not null,
  g_status varchar(36) not null,
  g_rev_loop int8 not null,
  g_date timestamp not null,
  fk_task int8 not null,
  primary key (id)
);

create table o_gta_mark (
  id bigserial not null,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  fk_tasklist_id int8 not null,
  fk_marker_identity_id int8 not null,
  fk_participant_identity_id int8 not null,
  primary key (id)
);

create table o_rem_reminder (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   r_description varchar(255),
   r_start timestamp,
   r_sendtime varchar(16),
   r_configuration text,
   r_email_subject varchar(255),
   r_email_body text,
   r_email_copy varchar(32),
   r_email_custom_copy varchar(1024),
   fk_creator int8 not null,
   fk_entry int8 not null,
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
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   e_name varchar(255) not null,
   e_status varchar(16) not null,
   e_ressubpath varchar(2048),
   e_executor_node varchar(16),
   e_executor_boot_id varchar(64),
   e_task text not null,
   e_scheduled timestamp,
   e_status_before_edit varchar(16),
   e_progress decimal,
   e_checkpoint varchar(255),
   fk_resource_id int8,
   fk_identity_id int8,
   primary key (id)
);

create table o_ex_task_modifier (
   id int8 not null,
   creationdate timestamp not null,
   fk_task_id int8 not null,
   fk_identity_id int8 not null,
   primary key (id)
);

-- sms
create table o_sms_message_log (
   id bigserial not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   s_message_uuid varchar(256) not null,
   s_server_response varchar(256),
   s_service_id varchar(32) not null,
   fk_identity int8 not null,
   primary key (id)
);

-- webfeed
create table o_feed (
   id bigserial not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   f_resourceable_id bigint,
   f_resourceable_type varchar(64),
   f_title varchar(1024),
   f_description text,
   f_author varchar(255),
   f_image_name varchar(1024),
   f_external boolean,
   f_external_feed_url varchar(4000),
   f_external_image_url varchar(4000),
   primary key (id)
);

create table o_feed_item (
   id bigserial not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   f_title varchar(1024),
   f_description text,
   f_content text,
   f_author varchar(255),
   f_guid varchar(255),
   f_external_link varchar(4000),
   f_draft boolean,
   f_publish_date timestamp,
   f_width int8,
   f_height int8,
   f_filename varchar(1024),
   f_type varchar(255),
   f_length bigint,
   f_external_url varchar(4000),
   fk_feed_id bigint,
   fk_identity_author_id int8,
   fk_identity_modified_id int8,
   primary key (id)
);

-- lectures
create table o_lecture_reason (
  id bigserial not null,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  l_enabled bool default true not null,
  l_title varchar(255),
  l_descr varchar(2000),
  primary key (id)
);

create table o_lecture_absence_category (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_enabled bool default true not null,
   l_title varchar(255),
   l_descr text,
   primary key (id)
);

create table o_lecture_absence_notice (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_type varchar(32),
   l_absence_reason text,
   l_absence_authorized bool default null,
   l_start_date timestamp not null,
   l_end_date timestamp not null,
   l_target varchar(32) default 'allentries' not null,
   l_attachments_dir varchar(255),
   fk_identity int8 not null,
   fk_notifier int8,
   fk_authorizer int8,
   fk_absence_category int8,
   primary key (id)
);

create table o_lecture_block (
  id bigserial not null,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  l_external_id varchar(255),
  l_managed_flags varchar(255),
  l_title varchar(255),
  l_descr text,
  l_preparation text,
  l_location varchar(255),
  l_comment text,
  l_start_date timestamp not null,
  l_end_date timestamp not null,
  l_compulsory bool default true,
  l_eff_end_date timestamp,
  l_planned_lectures_num int8 not null default 0,
  l_effective_lectures_num int8 not null default 0,
  l_effective_lectures varchar(128),
  l_auto_close_date timestamp default null,
  l_status varchar(16) not null,
  l_roll_call_status varchar(16) not null,
  fk_reason int8,
  fk_entry int8 not null,
  fk_teacher_group int8 not null,
  primary key (id)
);

create table o_lecture_block_to_group (
  id bigserial not null,
  fk_lecture_block int8 not null,
  fk_group int8 not null,
  primary key (id)
);

create table o_lecture_notice_to_block (
   id bigserial,
   creationdate timestamp not null,
   fk_lecture_block int8 not null,
   fk_absence_notice int8 not null,
   primary key (id)
);

create table o_lecture_notice_to_entry (
   id bigserial,
   creationdate timestamp not null,
   fk_entry int8 not null,
   fk_absence_notice int8 not null,
   primary key (id)
);

create table o_lecture_block_roll_call (
  id bigserial not null,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  l_comment text,
  l_lectures_attended varchar(128),
  l_lectures_absent varchar(128),
  l_lectures_attended_num int8 not null default 0,
  l_lectures_absent_num int8 not null default 0,
  l_absence_notice_lectures varchar(128),
  l_absence_reason text,
  l_absence_authorized bool default null,
  l_absence_appeal_date timestamp,
  l_absence_supervisor_noti_date timestamp,
  l_appeal_reason text,
  l_appeal_status text,
  l_appeal_status_reason text,
  fk_lecture_block int8 not null,
  fk_identity int8 not null,
  fk_absence_category int8,
  fk_absence_notice int8,
  primary key (id)
);

create table o_lecture_reminder (
  id bigserial not null,
  creationdate timestamp not null,
  l_status varchar(16) not null,
  fk_lecture_block int8 not null,
  fk_identity int8 not null,
  primary key (id)
);

create table o_lecture_participant_summary (
  id bigserial not null,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  l_required_attendance_rate float(24) default null,
  l_first_admission_date timestamp default null,
  l_attended_lectures int8 not null default 0,
  l_absent_lectures int8 not null default 0,
  l_excused_lectures int8 not null default 0,
  l_planneds_lectures int8 not null default 0,
  l_attendance_rate float(24) default null,
  l_cal_sync bool default false,
  l_cal_last_sync_date timestamp default null,
  fk_entry int8 not null,
  fk_identity int8 not null,
  primary key (id),
  unique (fk_entry, fk_identity)
);

create table o_lecture_entry_config (
  id bigserial not null,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  l_lecture_enabled bool default null,
  l_override_module_def bool default false,
  l_rollcall_enabled bool default null,
  l_calculate_attendance_rate bool default null,
  l_required_attendance_rate float(24) default null,
  l_sync_calendar_teacher bool default null,
  l_sync_calendar_participant bool default null,
  l_sync_calendar_course bool default null,
  l_assessment_mode bool default null,
  l_assessment_mode_lead int8 default null,
  l_assessment_mode_followup int8 default null,
  l_assessment_mode_ips varchar(2048),
  l_assessment_mode_seb varchar(2048),
  fk_entry int8 not null,
  unique(fk_entry),
  primary key (id)
);

create table o_lecture_block_audit_log (
  id bigserial not null,
  creationdate timestamp not null,
  l_action varchar(32),
  l_val_before text,
  l_val_after text,
  l_message text,
  fk_lecture_block int8,
  fk_roll_call int8,
  fk_absence_notice int8,
  fk_entry int8,
  fk_identity int8,
  fk_author int8,
  primary key (id)
);

create table o_lecture_block_to_tax_level (
  id bigserial,
  creationdate timestamp not null,
  fk_lecture_block int8 not null,
  fk_taxonomy_level int8 not null,
  primary key (id)
);

-- taxonomy
create table o_tax_taxonomy (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  t_identifier varchar(64),
  t_displayname varchar(255) not null,
  t_description text,
  t_external_id varchar(64),
  t_managed_flags varchar(255),
  t_directory_path varchar(255),
  t_directory_lost_found_path varchar(255),
  fk_group int8 not null,
  primary key (id)
);

create table o_tax_taxonomy_level_type (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  t_identifier varchar(64),
  t_displayname varchar(255) not null,
  t_description text,
  t_external_id varchar(64),
  t_managed_flags varchar(255),
  t_css_class varchar(64),
  t_visible bool default true,
  t_library_docs bool default true,
  t_library_manage bool default true,
  t_library_teach_read bool default true,
  t_library_teach_readlevels int8 not null default 0,
  t_library_teach_write bool default false,
  t_library_have_read bool default true,
  t_library_target_read bool default true,
  t_allow_as_competence bool default true not null,
  t_allow_as_subject bool default false,
  fk_taxonomy int8 not null,
  primary key (id)
);

create table o_tax_taxonomy_type_to_type (
  id bigserial,
  fk_type int8 not null,
  fk_allowed_sub_type int8 not null,
  primary key (id)
);

create table o_tax_taxonomy_level (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  t_identifier varchar(255),
  t_i18n_suffix varchar(64),
  t_displayname varchar(255),
  t_description text,
  t_external_id varchar(64),
  t_sort_order int8,
  t_directory_path varchar(255),
  t_media_path varchar(255),
  t_m_path_keys varchar(255),
  t_m_path_identifiers varchar(32000),
  t_enabled bool default true,
  t_managed_flags varchar(255),
  fk_taxonomy int8 not null,
  fk_parent int8,
  fk_type int8,
  primary key (id)
);

create table o_tax_taxonomy_competence (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  t_type varchar(16),
  t_achievement decimal default null,
  t_reliability decimal default null,
  t_expiration_date timestamp,
  t_external_id varchar(64),
  t_source_text varchar(255),
  t_source_url varchar(255),
  t_link_location varchar(255) default 'UNDEFINED' not null,
  fk_level int8 not null,
  fk_identity int8 not null,
  primary key (id)
);

create table o_tax_competence_audit_log (
  id bigserial,
  creationdate timestamp not null,
  t_action varchar(32),
  t_val_before text,
  t_val_after text,
  t_message text,
  fk_taxonomy int8,
  fk_taxonomy_competence int8,
  fk_identity int8,
  fk_author int8,
  primary key (id)
);

-- dialog elements
create table o_dialog_element (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  d_filename varchar(2048),
  d_filesize int8,
  d_subident varchar(64) not null,
  fk_author int8,
  fk_entry int8 not null,
  fk_forum int8 not null,
  primary key (id)
);

-- licenses
create table o_lic_license_type (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  l_name varchar(128) not null unique,
  l_text text,
  l_css_class varchar(64),
  l_predefined bool not null default false,
  l_sort_order int8 not null,
  primary key (id)
);

create table o_lic_license_type_activation (
  id bigserial,
  creationdate timestamp not null,
  l_handler_type varchar(128) not null,
  fk_license_type_id int8 not null,
  primary key (id)
);

create table o_lic_license (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  l_resname varchar(50) not null,
  l_resid int8 not null,
  l_licensor varchar(4000),
  l_freetext text,
  fk_license_type_id int8 not null,
  primary key (id)
);

-- organisation
create table o_org_organisation_type (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  o_identifier varchar(64),
  o_displayname varchar(255) not null,
  o_description text,
  o_external_id varchar(64),
  o_managed_flags varchar(255),
  o_css_class varchar(64),
  primary key (id)
);

create table o_org_organisation (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  o_identifier varchar(64),
  o_displayname varchar(255) not null,
  o_description text,
  o_m_path_keys varchar(255),
  o_external_id varchar(64),
  o_managed_flags varchar(255),
  o_status varchar(32),
  o_css_class varchar(64),
  fk_group int8 not null,
  fk_root int8,
  fk_parent int8,
  fk_type int8,
  primary key (id)
);

create table o_org_type_to_type (
  id bigserial,
  fk_type int8 not null,
  fk_allowed_sub_type int8 not null,
  primary key (id)
);

create table o_re_to_organisation (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  r_master bool default false,
  fk_entry int8 not null,
  fk_organisation int8 not null,
  primary key (id)
);

-- curriculum
create table o_cur_element_type (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description text,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  c_calendars varchar(16),
  c_lectures varchar(16),
  c_learning_progress varchar(16),
  c_css_class varchar(64),
  primary key (id)
);

create table o_cur_curriculum (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description text,
  c_external_id varchar(64),
  c_managed_flags varchar(255),
  c_status varchar(32),
  c_degree varchar(255),
  c_lectures bool default false not null,
  fk_group int8 not null,
  fk_organisation int8,
  primary key (id)
);

create table o_cur_curriculum_element (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  pos int8,
  pos_cur int8,
  c_identifier varchar(64),
  c_displayname varchar(255) not null,
  c_description text,
  c_status varchar(32),
  c_begin timestamp,
  c_end timestamp ,
  c_external_id varchar(64),
  c_m_path_keys varchar(255),
  c_managed_flags varchar(255),
  c_calendars varchar(16),
  c_lectures varchar(16),
  c_learning_progress varchar(16),
  fk_group int8 not null,
  fk_parent int8,
  fk_curriculum int8 not null,
  fk_curriculum_parent int8,
  fk_type int8,
  primary key (id)
);

create table o_cur_element_type_to_type (
  id bigserial,
  fk_type int8 not null,
  fk_allowed_sub_type int8 not null,
  primary key (id)
);

create table o_cur_element_to_tax_level (
  id bigserial,
  creationdate timestamp not null,
  fk_cur_element int8 not null,
  fk_taxonomy_level int8 not null,
  primary key (id)
);

-- edu-sharing
create table o_es_usage (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
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
   id bigserial,
   creationdate timestamp not null,
   l_launch_date timestamp not null,
   fk_entry int8 not null,
   l_subident varchar(128) not null,
   fk_identity int8 not null,
   primary key (id)
);

create table o_livestream_url_template (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_name varchar(64) not null,
   l_url1 varchar(2048),
   l_url2 varchar(2048),
   primary key (id)
);

-- grading
create table o_grad_to_identity (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_status varchar(16) default 'activated' not null,
   fk_identity int8 not null,
   fk_entry int8 not null,
   primary key (id)
);

create table o_grad_assignment (
   id bigserial,
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
   fk_reference_entry int8 not null,
   fk_assessment_entry int8 not null,
   fk_grader int8,
   primary key (id)
);

create table o_grad_time_record (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_time int8 default 0 not null,
   g_metadata_time int8 default 0 not null,
   g_date_record date not null,
   fk_assignment int8,
   fk_grader int8 not null,
   primary key (id)
);

create table o_grad_configuration (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_grading_enabled bool not null default false,
   g_identity_visibility varchar(32) default 'anonymous' not null,
   g_grading_period int8,
   g_notification_type varchar(32) default 'afterTestSubmission' not null,
   g_notification_subject varchar(255),
   g_notification_body text,
   g_first_reminder int8,
   g_first_reminder_subject varchar(255),
   g_first_reminder_body text,
   g_second_reminder int8,
   g_second_reminder_subject varchar(255),
   g_second_reminder_body text,
   fk_entry int8 not null,
   primary key (id)
);

-- Course
create table o_course_element (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_type varchar(32) not null,
   c_short_title varchar(32) not null,
   c_long_title varchar(1024) not null,
   c_assesseable bool not null,
   c_score_mode varchar(16) not null,
   c_grade bool not null default false,
   c_auto_grade bool not null default false,
   c_passed_mode varchar(16) not null,
   c_cut_value decimal,
   fk_entry int8 not null,
   c_subident varchar(64) not null,
   primary key (id)
);

-- Course styles
create table o_course_color_category (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_identifier varchar(128) not null,
   c_type varchar(16) not null,
   c_sort_order int8 not null,
   c_enabled bool not null default true,
   c_css_class varchar(128),
   primary key (id)
);

-- course disclaimer
create table o_course_disclaimer_consent(
    id bigserial,
    disc_1_accepted boolean not null,
    disc_2_accepted boolean not null,
    creationdate timestamp not null,
    lastmodified timestamp not null,
    fk_repository_entry int8 not null,
    fk_identity int8 not null,
    primary key (id)
);

-- Appointments
create table o_ap_topic (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_title varchar(256),
   a_description varchar(4000),
   a_type varchar(64) not null,
   a_multi_participation bool default true not null,
   a_auto_confirmation bool default false not null,
   a_participation_visible bool default true not null,
   fk_group_id int8,
   fk_entry_id int8 not null,
   a_sub_ident varchar(64) not null,
   primary key (id)
);

create table o_ap_organizer (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   fk_topic_id int8 not null,
   fk_identity_id int8 not null,
   primary key (id)
);

create table o_ap_topic_to_group (
   id bigserial,
   creationdate timestamp not null,
   fk_topic_id int8 not null,
   fk_group_id int8,
   primary key (id)
);

create table o_ap_appointment (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_status varchar(64) not null,
   a_status_mod_date timestamp,
   a_start timestamp,
   a_end timestamp,
   a_location varchar(256),
   a_details varchar(4000),
   a_max_participations int8,
   fk_topic_id int8 not null,
   fk_meeting_id bigint,
   fk_teams_id bigint,
   primary key (id)
);

create table o_ap_participation (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   fk_appointment_id int8 not null,
   fk_identity_id int8 not null,
   fk_identity_created_by int8 not null,
   primary key (id)
);

-- Organiation role rights
create table o_org_role_to_right (
    id bigserial,
    creationdate timestamp not null,
    o_role varchar(255) not null,
    o_right varchar(255) not null,
    fk_organisation int8 not null,
    primary key (id)
);

-- Contact tracing
create table o_ct_location (
    id bigserial,
    creationdate timestamp not null,
    lastmodified timestamp not null,
    l_reference varchar(255),
    l_titel varchar(255),
    l_room varchar(255),
    l_building varchar(255),
    l_sector varchar(255),
    l_table varchar(255),
    l_seat_number boolean default false not null,
    l_qr_id varchar(255) not null,
    l_qr_text varchar(4000),
    l_guests boolean default true not null,
    l_printed boolean default false not null,
    unique(l_qr_id),
    primary key (id)
);

create table o_ct_registration (
    id bigserial,
    creationdate timestamp not null,
    l_deletion_date timestamp not null,
    l_start_date timestamp not null,
    l_end_date timestamp,
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
	l_immunity_proof_level varchar(20),
	l_immunity_proof_date timestamp,
    fk_location int8 not null,
    primary key (id)
);

-- Immunity Proof
create table o_immunity_proof (
   id bigserial,
   creationdate timestamp not null,
   fk_user int8 not null,
   safedate timestamp not null,
   validated boolean not null,
   send_mail boolean not null,
   email_sent boolean not null default false,
   primary key (id)
);

-- Zoom
create table o_zoom_profile (
    id bigserial,
    creationdate timestamp not null,
    lastmodified timestamp not null,
    z_name varchar(255) not null,
    z_status varchar(255) not null,
    z_lti_key varchar(255) not null,
    z_mail_domains varchar(1024),
    z_students_can_host bool default false,
    z_token varchar(255) not null,
    fk_lti_tool_id int8 not null,
    primary key (id)
);

create table o_zoom_config (
    id bigserial,
    creationdate timestamp not null,
    lastmodified timestamp not null,
    z_description varchar(255),
    fk_profile int8 not null,
    fk_lti_tool_deployment_id int8 not null,
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
   inner join o_re_to_group relgroup on (relgroup.fk_entry_id=re.repositoryentry_id and relgroup.r_defgroup=true)
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
      (bgroup.ownersintern=true and bg_member.g_role='coach')
      or
      (bgroup.participantsintern=true and bg_member.g_role='participant')
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
      (bgroup.ownersintern=true and bg_member.g_role='coach')
      or
      (bgroup.participantsintern=true and bg_member.g_role='participant')
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


-- rating
alter table o_userrating add constraint FKF26C8375236F20X foreign key (creator_id) references o_bs_identity (id);
create index FKF26C8375236F20X on o_userrating (creator_id);
create index userrating_id_idx on o_userrating (resid);
create index userrating_name_idx on o_userrating (resname);
create index userrating_subpath_idx on o_userrating (ressubpath);
create index userrating_rating_idx on o_userrating (rating);
create index userrating_rating_res_idx on o_userrating (resid, resname, creator_id, rating);


-- comment
alter table o_usercomment add constraint FK92B6864A18251F0 foreign key (parent_key) references o_usercomment (comment_id);
create index FK92B6864A18251F0 on o_usercomment (parent_key);
alter table o_usercomment add constraint FKF26C8375236F20A foreign key (creator_id) references o_bs_identity (id);
create index FKF26C8375236F20A on o_usercomment (creator_id);
create index usercmt_id_idx on o_usercomment (resid);
create index usercmt_name_idx on o_usercomment (resname);
create index usercmt_subpath_idx on o_usercomment (ressubpath);

-- checkpoint
alter table o_checkpoint_results add constraint FK9E30F4B661159ZZY foreign key (checkpoint_fk) references o_checkpoint;
create index idx_chres_check_idx on o_checkpoint_results (checkpoint_fk);
alter table o_checkpoint_results add constraint FK9E30F4B661159ZZX foreign key (identity_fk) references o_bs_identity;
create index idx_chres_ident_idx on o_checkpoint_results (identity_fk);

alter table o_checkpoint add constraint FK9E30F4B661159ZZZ foreign key (checklist_fk) references o_checklist (checklist_id);
create index idx_chpt_checklist_fk on o_checkpoint (checklist_fk);

-- plock
create index asset_idx on o_plock (asset);

-- property
alter table o_property add constraint FKB60B1BA5190E5 foreign key (grp) references o_gp_business;
create index idx_prop_grp_idx on o_property (grp);
alter table o_property add constraint FKB60B1BA5F7E870BE foreign key (identity) references o_bs_identity;
create index idx_prop_ident_idx on o_property (identity);

create index resid_idx1 on o_property (resourcetypeid);
create index category_idx on o_property (category);
create index name_idx1 on o_property (name);
create index restype_idx1 on o_property (resourcetypename);

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
create index gp_to_group_group_idx on o_gp_business (fk_group_id);

-- business group
create index gp_name_idx on o_gp_business (groupname);
create index idx_grp_lifecycle_soft_idx on o_gp_business (external_id);
create index gp_tech_type_idx on o_gp_business (technical_type);

alter table o_bs_namedgroup add constraint FKBAFCBBC4B85B522C foreign key (secgroup_id) references o_bs_secgroup;
create index FKBAFCBBC4B85B522C on o_bs_namedgroup (secgroup_id);
create index groupname_idx on o_bs_namedgroup (groupname);

alter table o_gp_business add constraint gb_bus_inactivateby_idx foreign key (fk_inactivatedby_id) references o_bs_identity (id);
create index idx_gb_bus_inactivateby_idx on o_gp_business (fk_inactivatedby_id);

alter table o_gp_business add constraint gb_bus_softdeletedby_idx foreign key (fk_softdeletedby_id) references o_bs_identity (id);
create index idx_gb_bus_softdeletedby_idx on o_gp_business (fk_softdeletedby_id);

-- area
alter table o_gp_bgarea add constraint idx_area_to_resource foreign key (fk_resource) references o_olatresource (resource_id);
create index idx_area_resource on o_gp_bgarea (fk_resource);
create index name_idx6 on o_gp_bgarea (name);

alter table o_gp_bgtoarea_rel add constraint FK9B663F2D1E2E7685 foreign key (group_fk) references o_gp_business;
create index idx_bgtoarea_grp_idx on o_gp_bgtoarea_rel (group_fk);
alter table o_gp_bgtoarea_rel add constraint FK9B663F2DD381B9B7 foreign key (area_fk) references o_gp_bgarea;
create index idx_bgtoarea_area_idx on o_gp_bgtoarea_rel (area_fk);

-- bs
alter table o_bs_authentication add constraint FKC6A5445652595FE6 foreign key (identity_fk) references o_bs_identity;
alter table o_bs_authentication add constraint unique_pro_iss_authusername UNIQUE (provider, issuer, authusername);
create index idx_auth_ident_idx on o_bs_authentication (identity_fk);
create index provider_idx on o_bs_authentication (provider);
create index credential_idx on o_bs_authentication (credential);
create index authusername_idx on o_bs_authentication (authusername);
create index low_authusername_idx on o_bs_authentication (lower(authusername));

alter table o_bs_authentication_history add constraint auth_hist_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_auth_hist_to_ident_idx on o_bs_authentication_history (fk_identity);

create index identstatus_idx on o_bs_identity (status);
create index idx_ident_creationdate_idx on o_bs_identity (creationdate);
create index idx_id_lastlogin_idx on o_bs_identity (lastlogin);

alter table o_bs_membership add constraint FK7B6288B45259603C foreign key (identity_id) references o_bs_identity;
create index idx_membership_ident_idx on o_bs_membership (identity_id);
alter table o_bs_membership add constraint FK7B6288B4B85B522C foreign key (secgroup_id) references o_bs_secgroup;
create index idx_membership_sec_idx on o_bs_membership (secgroup_id);
create index idx_membership_sec_ident_idx on o_bs_membership (identity_id, secgroup_id);

alter table o_bs_invitation add constraint inv_to_group_group_ctx foreign key (fk_group_id) references o_bs_group (id);
create index idx_inv_to_group_group_ctx on o_bs_invitation (fk_group_id);
alter table o_bs_invitation add constraint invit_to_id_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_invit_to_id_idx on o_bs_invitation (fk_identity_id);

create index idx_secgroup_creationdate_idx on o_bs_secgroup (creationdate);

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

create index xx_idx_email_low_text on o_user(lower(u_email) text_pattern_ops);
create index xx_idx_institutionalemail_low_text on o_user(lower(u_institutionalemail) text_pattern_ops);
create index xx_idx_username_low_text on o_bs_identity(lower(name) text_pattern_ops);
create index xx_idx_nickname_low_text on o_user(lower(u_nickname) text_pattern_ops);

alter table o_user add constraint iuni_user_nickname_idx unique (u_nickname);

create index propvalue_idx on o_userproperty (propvalue);

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

alter table o_noti_sub add constraint FK4FB8F04749E53702 foreign key (fk_publisher) references o_noti_pub;
create index idx_sub_to_pub_idx on o_noti_sub (fk_publisher);
alter table o_noti_sub add constraint FK4FB8F0476B1F22F8 foreign key (fk_identity) references o_bs_identity;
create index idx_sub_to_ident_idx on o_noti_sub (fk_identity);

create index idx_sub_to_id_pub_idx on o_noti_sub (publisher_id, fk_publisher);
create index idx_sub_to_id_ident_idx on o_noti_sub (publisher_id, fk_identity);
-- index created idx_sub_to_pub_ident_idx on unique constraint
create index idx_sub_to_id_pub_ident_idx on o_noti_sub (publisher_id, fk_publisher, fk_identity);

-- catalog entry
alter table o_catentry add constraint FKF4433C2C7B66B0D0 foreign key (parent_id) references o_catentry;
create index idx_catentry_parent_idx on o_catentry (parent_id);
alter table o_catentry add constraint FKF4433C2CA1FAC766 foreign key (fk_ownergroup) references o_bs_secgroup;
alter table o_catentry add constraint FKF4433C2CDDD69946 foreign key (fk_repoentry) references o_repositoryentry;
create index idx_catentry_re_idx on o_catentry (fk_repoentry);

-- references
alter table o_references add constraint FKE971B4589AC44FBF foreign key (source_id) references o_olatresource;
create index idx_ref_source_idx on o_references (source_id);
alter table o_references add constraint FKE971B458CF634A89 foreign key (target_id) references o_olatresource;
create index idx_ref_target_idx on o_references (target_id);

-- resources
create index name_idx4 on o_olatresource (resname);
create index id_idx on o_olatresource (resid);

-- repository
create index re_status_idx on o_repositoryentry (status);
create index initialAuthor_idx on o_repositoryentry (initialauthor);
create index resource_idx on o_repositoryentry (resourcename);
create index displayname_idx on o_repositoryentry (displayname);
create index idx_re_lifecycle_soft_idx on o_repositoryentry_cycle (r_softkey);
create index idx_re_lifecycle_extid_idx on o_repositoryentry (external_id);
create index idx_re_lifecycle_extref_idx on o_repositoryentry (external_ref);

alter table o_repositoryentry add constraint idx_re_lifecycle_fk foreign key (fk_lifecycle) references o_repositoryentry_cycle(id);
create index idx_re_lifecycle_idx on o_repositoryentry (fk_lifecycle);

alter table o_repositoryentry add constraint repoentry_stats_ctx foreign key (fk_stats) references o_repositoryentry_stats (id);
create index repoentry_stats_idx on o_repositoryentry (fk_stats);

alter table o_repositoryentry add constraint re_deleted_to_identity_idx foreign key (fk_deleted_by) references o_bs_identity (id);
create index idx_re_deleted_to_identity_idx on o_repositoryentry (fk_deleted_by);

alter table o_re_to_tax_level add constraint re_to_lev_re_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_re_to_lev_re_idx on o_re_to_tax_level (fk_entry);
alter table o_re_to_tax_level add constraint re_to_lev_tax_lev_idx foreign key (fk_taxonomy_level) references o_tax_taxonomy_level (id);
create index idx_re_to_lev_tax_lev_idx on o_re_to_tax_level (fk_taxonomy_level);

alter table o_repositoryentry add constraint idx_re_edu_type_fk foreign key (fk_educational_type) references o_re_educational_type(id);
create unique index idc_re_edu_type_ident on o_re_educational_type (r_identifier);

-- access control
create index ac_offer_to_resource_idx on o_ac_offer (fk_resource_id);
create index idx_offer_guest_idx on o_ac_offer (guest_access);
create index idx_offer_open_idx on o_ac_offer (open_access);

alter table o_ac_offer_to_organisation add constraint rel_oto_offer_idx foreign key (fk_offer) references o_ac_offer(offer_id);
create index idx_rel_oto_offer_idx on o_ac_offer_to_organisation (fk_offer);
alter table o_ac_offer_to_organisation add constraint rel_oto_org_idx foreign key (fk_organisation) references o_org_organisation(id);
create index idx_rel_oto_org_idx on o_ac_offer_to_organisation (fk_organisation);

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

create index idx_ac_aao_id_idx on o_ac_auto_advance_order(id);
create index idx_ac_aao_identifier_idx on o_ac_auto_advance_order(a_identifier_key, a_identifier_value);
create index idx_ac_aao_ident_idx on o_ac_auto_advance_order(fk_identity);
alter table o_ac_auto_advance_order add constraint aao_ident_idx foreign key (fk_identity) references o_bs_identity (id);

-- reservations
alter table o_ac_reservation add constraint idx_rsrv_to_rsrc_rsrc foreign key (fk_resource) references o_olatresource (resource_id);
create index idx_rsrv_to_rsrc_idx on o_ac_reservation(fk_resource);
alter table o_ac_reservation add constraint idx_rsrv_to_rsrc_identity foreign key (fk_identity) references o_bs_identity (id);
create index idx_rsrv_to_rsrc_id_idx on o_ac_reservation(fk_identity);

-- catalog
alter table o_ca_launcher_to_organisation add constraint rel_lto_launcher_idx foreign key (fk_launcher) references o_ca_launcher(id);
create index idx_rel_lto_launcher_idx on o_ca_launcher_to_organisation (fk_launcher);
alter table o_ca_launcher_to_organisation add constraint rel_lto_org_idx foreign key (fk_organisation) references o_org_organisation(id);
create index idx_rel_lto_org_idx on o_ca_launcher_to_organisation (fk_organisation);

-- note
alter table o_note add constraint FKC2D855C263219E27 foreign key (owner_id) references o_bs_identity;
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
create index mark_subpath_idx on o_mark(ressubpath);
create index mark_businesspath_idx on o_mark(businesspath);

-- forum
create index idx_forum_ref_idx on o_forum (f_refresid, f_refresname);
alter table o_message add constraint FKF26C8375236F20E foreign key (creator_id) references o_bs_identity;
create index idx_message_creator_idx on o_message (creator_id);
alter table o_message add constraint FKF26C837A3FBEB83 foreign key (modifier_id) references o_bs_identity;
create index idx_message_modifier_idx on o_message (modifier_id);
alter table o_message add constraint FKF26C8377B66B0D0 foreign key (parent_id) references o_message;
create index idx_message_parent_idx on o_message (parent_id);
alter table o_message add constraint FKF26C8378EAC1DBB foreign key (topthread_id) references o_message;
create index idx_message_top_idx on o_message (topthread_id);
alter table o_message add constraint FKF26C8371CB7C4A3 foreign key (forum_fk) references o_forum;
create index idx_message_forum_idx on o_message (forum_fk);
create index forum_msg_pseudonym_idx on o_message (pseudonym);

create index readmessage_forum_idx on o_readmessage (forum_id);
create index readmessage_identity_idx on o_readmessage (identity_id);

create index forum_pseudonym_idx on o_forum_pseudonym (p_pseudonym);

-- project broker
create index projectbroker_project_broker_idx on o_projectbroker_project (projectbroker_fk);
-- index created on projectbroker_project_id_idx unique constraint
create index o_projectbroker_customfields_idx on o_projectbroker_customfields (fk_project_id);

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

-- openmeeting
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

-- Bigbluebutton
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
alter table o_bbb_meeting add constraint bbb_dir_idx unique (b_directory);

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

alter table o_teams_user add constraint teams_user_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_teams_user_ident_idx on o_teams_user(fk_identity_id);

alter table o_teams_attendee add constraint teams_att_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_teams_att_ident_idx on o_teams_attendee(fk_identity_id);
alter table o_teams_attendee add constraint teams_att_user_idx foreign key (fk_teams_user_id) references o_teams_user (id);
create index idx_teams_att_user_idx on o_teams_attendee(fk_teams_user_id);
alter table o_teams_attendee add constraint teams_att_meet_idx foreign key (fk_meeting_id) references o_teams_meeting (id);
create index idx_teams_att_meet_idx on o_teams_attendee(fk_meeting_id);

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
create index idx_im_msg_channel_idx on o_im_message (msg_resid,msg_resname,msg_ressubpath,msg_channel);

alter table o_im_message add constraint im_msg_bbb_idx foreign key (fk_meeting_id) references o_bbb_meeting (id);
create index idx_im_msg_bbb_idx on o_im_message(fk_meeting_id);
alter table o_im_message add constraint im_msg_teams_idx foreign key (fk_teams_id) references o_teams_meeting (id);
create index idx_im_msg_teams_idx on o_im_message(fk_teams_id);

alter table o_im_notification add constraint idx_im_not_to_toid foreign key (fk_to_identity_id) references o_bs_identity (id);
create index idx_im_chat_to_idx on o_im_notification (fk_to_identity_id);
alter table o_im_notification add constraint idx_im_not_to_fromid foreign key (fk_from_identity_id) references o_bs_identity (id);
create index idx_im_chat_from_idx on o_im_notification (fk_from_identity_id);
create index idx_im_chat_res_idx on o_im_notification (chat_resid,chat_resname);
create index idx_im_chat_typed_idx on o_im_notification (fk_to_identity_id,chat_type);

alter table o_im_roster_entry add constraint idx_im_rost_to_id foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_im_rost_res_idx on o_im_roster_entry (r_resid,r_resname);
create index idx_im_rost_ident_idx on o_im_roster_entry (fk_identity_id);
create index idx_im_rost_sub_idx on o_im_roster_entry (r_resid,r_resname,r_ressubpath);

alter table o_im_preferences add constraint idx_im_prfs_to_id foreign key (fk_from_identity_id) references o_bs_identity (id);
create index idx_im_prefs_ident_idx on o_im_preferences (fk_from_identity_id);

-- efficiency statements
alter table o_as_eff_statement add constraint eff_statement_id_cstr foreign key (fk_identity) references o_bs_identity (id);
create index idx_eff_statement_ident_idx on o_as_eff_statement (fk_identity);
create index eff_statement_repo_key_idx on o_as_eff_statement (course_repo_key);
create index idx_eff_stat_course_ident_idx on o_as_eff_statement (fk_identity,course_repo_key);

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
create index idx_as_entry_start_idx on o_as_entry (a_date_start) where a_date_start is not null;
create index idx_as_entry_root_id_idx on o_as_entry (id) where a_entry_root=true;
create index idx_as_entry_root_fk_idx on o_as_entry (fk_entry, fk_identity) where a_entry_root=true;

alter table o_as_score_accounting_trigger add constraint satrigger_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_satrigger_re_idx on o_as_score_accounting_trigger (fk_entry);
create index idx_satrigger_bs_group_idx on o_as_score_accounting_trigger (e_business_group_key) where e_business_group_key is not null;
create index idx_satrigger_org_idx on o_as_score_accounting_trigger (e_organisation_key) where e_organisation_key is not null;
create index idx_satrigger_curle_idx on o_as_score_accounting_trigger (e_curriculum_element_key) where e_curriculum_element_key is not null;
create index idx_satrigger_userprop_idx on o_as_score_accounting_trigger (e_user_property_value, e_user_property_name) where e_user_property_value is not null;

-- Assessment message
alter table o_as_message add constraint as_msg_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_as_msg_entry_idx on o_as_message (fk_entry);

alter table o_as_message_log add constraint as_msg_log_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_as_msg_log_identity_idx on o_as_message_log (fk_identity);
alter table o_as_message_log add constraint as_msg_log_msg_idx foreign key (fk_message) references o_as_message (id);
create index idx_as_msg_log_msg_idx on o_as_message_log (fk_message);

-- disadvantage compensation
alter table o_as_compensation add constraint compensation_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_compensation_ident_idx on o_as_compensation(fk_identity);
alter table o_as_compensation add constraint compensation_crea_idx foreign key (fk_creator) references o_bs_identity (id);
create index idx_compensation_crea_idx on o_as_compensation(fk_creator);
alter table o_as_compensation add constraint compensation_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_compensation_entry_idx on o_as_compensation(fk_entry);

create index comp_log_entry_idx on o_as_compensation_log (fk_entry_id);
create index comp_log_ident_idx on o_as_compensation_log (fk_identity_id);

-- Grade
create unique index idx_grade_system_ident on o_gr_grade_system (g_identifier);
alter table o_gr_grade_scale add constraint grscale_to_grsys_idx foreign key (fk_grade_system) references o_gr_grade_system (id);
create index idx_grscale_to_grsys_idx on o_gr_grade_scale (fk_grade_system);
alter table o_gr_performance_class add constraint perf_to_grsys_idx foreign key (fk_grade_system) references o_gr_grade_system (id);
create index idx_perf_to_grsys_idx on o_gr_performance_class (fk_grade_system);
alter table o_gr_grade_scale add constraint grscale_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_grscale_entry_idx on o_gr_grade_scale (fk_entry);
alter table o_gr_breakpoint add constraint grbp_to_grscale_idx foreign key (fk_grade_scale) references o_gr_grade_scale (id);
create index idx_grbp_to_grscale_idx on o_gr_breakpoint (fk_grade_scale);

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
create index idx_item_ext_ref_idx on o_qti_assessmentitem_session (q_externalrefidentifier);

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

-- Practice
alter table o_practice_resource add constraint pract_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_pract_entry_idx on o_practice_resource (fk_entry);

alter table o_practice_resource add constraint pract_test_entry_idx foreign key (fk_test_entry) references o_repositoryentry (repositoryentry_id);
create index idx_pract_test_entry_idx on o_practice_resource (fk_test_entry);
alter table o_practice_resource add constraint pract_item_coll_idx foreign key (fk_item_collection) references o_qp_item_collection (id);
create index idx_pract_item_coll_idx on o_practice_resource (fk_item_collection);
alter table o_practice_resource add constraint pract_poll_idx foreign key (fk_pool) references o_qp_pool (id);
create index idx_poll_idx on o_practice_resource (fk_pool);
alter table o_practice_resource add constraint pract_rsrc_share_idx foreign key (fk_resource_share) references o_olatresource(resource_id);
create index idx_rsrc_share_idx on o_practice_resource (fk_resource_share);

alter table o_practice_global_item_ref add constraint pract_global_ident_idx foreign key (fk_identity) references o_bs_identity(id);
create index idx_pract_global_ident_idx on o_practice_global_item_ref (fk_identity);

create index idx_pract_global_id_uu_idx on o_practice_global_item_ref (fk_identity,p_identifier);

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

alter table o_pf_page add constraint pf_page_body_idx foreign key (fk_body_id) references o_pf_page_body (id);
create index idx_pf_page_body_idx on o_pf_page (fk_body_id);

alter table o_pf_page_part add constraint pf_page_page_body_idx foreign key (fk_page_body_id) references o_pf_page_body (id);
create index idx_pf_page_page_body_idx on o_pf_page_part (fk_page_body_id);
alter table o_pf_page_part add constraint pf_page_media_idx foreign key (fk_media_id) references o_pf_media (id);
create index idx_pf_page_media_idx on o_pf_page_part (fk_media_id);
alter table o_pf_page_part add constraint pf_part_form_idx foreign key (fk_form_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_pf_part_form_idx on o_pf_page_part (fk_form_entry_id);

alter table o_pf_media add constraint pf_media_author_idx foreign key (fk_author_id) references o_bs_identity (id);
create index idx_pf_media_author_idx on o_pf_media (fk_author_id);
create index idx_media_storage_path_idx on o_pf_media (p_business_path);

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
alter table o_pf_assignment add constraint pf_assign_form_idx foreign key (fk_form_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_pf_assign_form_idx on o_pf_assignment (fk_form_entry_id);

alter table o_pf_binder_user_infos add constraint binder_user_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_binder_user_to_ident_idx on o_pf_binder_user_infos (fk_identity);
alter table o_pf_binder_user_infos add constraint binder_user_binder_idx foreign key (fk_binder) references o_pf_binder (id);
create index idx_binder_user_binder_idx on o_pf_binder_user_infos (fk_binder);

alter table o_pf_page_user_infos add constraint user_pfpage_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_user_pfpage_idx on o_pf_page_user_infos (fk_identity_id);
alter table o_pf_page_user_infos add constraint page_pfpage_idx foreign key (fk_page_id) references o_pf_page (id);
create index idx_page_pfpage_idx on o_pf_page_user_infos (fk_page_id);

alter table o_pf_page_to_tax_competence add constraint fk_tax_competence_idx foreign key (fk_tax_competence) references o_tax_taxonomy_competence (id);
create index idx_fk_tax_competence_idx on o_pf_page_to_tax_competence (fk_tax_competence);
alter table o_pf_page_to_tax_competence add constraint fk_pf_page_idx foreign key (fk_pf_page) references o_pf_page (id);
create index idx_fk_pf_page_idx on o_pf_page_to_tax_competence (fk_pf_page);

-- vfs metadata
alter table o_vfs_metadata add constraint fmeta_to_author_idx foreign key (fk_locked_identity) references o_bs_identity (id);
create index idx_fmeta_to_author_idx on o_vfs_metadata (fk_locked_identity);
alter table o_vfs_metadata add constraint fmeta_modified_by_idx foreign key (fk_lastmodified_by) references o_bs_identity (id);
create index idx_fmeta_modified_by_idx on o_vfs_metadata (fk_lastmodified_by);
alter table o_vfs_metadata add constraint fmeta_to_lockid_idx foreign key (fk_initialized_by) references o_bs_identity (id);
create index idx_fmeta_to_lockid_idx on o_vfs_metadata (fk_initialized_by);
alter table o_vfs_metadata add constraint fmeta_to_lic_type_idx foreign key (fk_license_type) references o_lic_license_type (id);
create index idx_fmeta_to_lic_type_idx on o_vfs_metadata (fk_license_type);
alter table o_vfs_metadata add constraint fmeta_to_parent_idx foreign key (fk_parent) references o_vfs_metadata (id);
create index idx_fmeta_to_parent_idx on o_vfs_metadata (fk_parent);
create index f_m_rel_path_idx on o_vfs_metadata (f_relative_path varchar_pattern_ops);
create index f_m_file_idx on o_vfs_metadata (f_relative_path,f_filename);
create index f_m_uuid_idx on o_vfs_metadata (f_uuid);
create index f_exp_date_idx on o_vfs_metadata (f_expiration_date);

alter table o_vfs_thumbnail add constraint fthumb_to_meta_idx foreign key (fk_metadata) references o_vfs_metadata (id);
create index idx_fthumb_to_meta_idx on o_vfs_thumbnail (fk_metadata);

alter table o_vfs_revision add constraint fvers_to_author_idx foreign key (fk_initialized_by) references o_bs_identity (id);
create index idx_fvers_to_author_idx on o_vfs_revision (fk_initialized_by);
alter table o_vfs_revision add constraint fvers_modified_by_idx foreign key (fk_lastmodified_by) references o_bs_identity (id);
create index idx_fvers_mod_by_idx on o_vfs_revision (fk_lastmodified_by);
alter table o_vfs_revision add constraint fvers_to_meta_idx foreign key (fk_metadata) references o_vfs_metadata (id);
create index idx_fvers_to_meta_idx on o_vfs_revision (fk_metadata);
alter table o_vfs_revision add constraint fvers_to_lic_type_idx foreign key (fk_license_type) references o_lic_license_type (id);
create index idx_fvers_to_lic_type_idx on o_vfs_revision (fk_license_type);

-- Document editor
create unique index idx_de_userinfo_ident_idx on o_de_user_info(fk_identity);

-- evaluation form
alter table o_eva_form_survey add constraint eva_surv_to_surv_idx foreign key (fk_series_previous) references o_eva_form_survey (id);
create index idx_eva_surv_ores_idx on o_eva_form_survey (e_resid, e_resname, e_sub_ident, e_sub_ident2);

alter table o_eva_form_participation add constraint eva_part_to_surv_idx foreign key (fk_survey) references o_eva_form_survey (id);
create index idx_eva_part_survey_idx on o_eva_form_participation (fk_survey);
create unique index idx_eva_part_ident_idx on o_eva_form_participation (e_identifier_key, e_identifier_type, fk_survey);
create unique index idx_eva_part_executor_idx on o_eva_form_participation (fk_executor, fk_survey) where fk_executor is not null;

alter table o_eva_form_session add constraint eva_sess_to_surv_idx foreign key (fk_survey) references o_eva_form_survey (id);
create index idx_eva_sess_to_surv_idx on o_eva_form_session (fk_survey);
alter table o_eva_form_session add constraint eva_sess_to_part_idx foreign key (fk_participation) references o_eva_form_participation (id);
create unique index idx_eva_sess_to_part_idx on o_eva_form_session (fk_participation);
alter table o_eva_form_session add constraint eva_sess_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_eva_sess_to_ident_idx on o_eva_form_session (fk_identity);
alter table o_eva_form_session add constraint eva_sess_to_body_idx foreign key (fk_page_body) references o_pf_page_body (id);
create index idx_eva_sess_to_body_idx on o_eva_form_session (fk_page_body);
alter table o_eva_form_session add constraint eva_sess_to_form_idx foreign key (fk_form_entry) references o_repositoryentry (repositoryentry_id);
create index idx_eva_sess_to_form_idx on o_eva_form_session (fk_form_entry);

alter table o_eva_form_response add constraint eva_resp_to_sess_idx foreign key (fk_session) references o_eva_form_session (id);
create index idx_eva_resp_to_sess_idx on o_eva_form_response (fk_session);
create index idx_eva_resp_report_idx on o_eva_form_response (fk_session, e_responseidentifier, e_no_response);

-- quality management
alter table o_qual_data_collection add constraint qual_dc_to_gen_idx foreign key (fk_generator) references o_qual_generator (id);
create index idx_dc_to_gen_idx on o_qual_data_collection(fk_generator);
create index idx_dc_status_idx on o_qual_data_collection (q_status) where q_status in ('READY', 'RUNNING');

alter table o_qual_data_collection_to_org add constraint qual_dc_to_org_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
create unique index idx_qual_dc_to_org_idx on o_qual_data_collection_to_org (fk_data_collection, fk_organisation);
alter table o_qual_context add constraint qual_con_to_data_collection_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
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

alter table o_qual_reminder add constraint qual_rem_to_data_collection_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
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
create index idx_taxon_mat_path on o_qp_taxonomy_level (q_mat_path_ids);

alter table o_qp_item_type add constraint cst_unique_item_type unique (q_type);

create index idx_item_audit_item_idx on o_qp_item_audit_log (fk_item_id);

-- LTI
alter table o_lti_outcome add constraint idx_lti_outcome_ident_id foreign key (fk_identity_id) references o_bs_identity(id);
create index idx_lti_outcome_ident_id_idx on o_lti_outcome (fk_identity_id);
alter table o_lti_outcome add constraint idx_lti_outcome_rsrc_id foreign key (fk_resource_id) references o_olatresource(resource_id);
create index idx_lti_outcome_rsrc_id_idx on o_lti_outcome (fk_resource_id);

-- LTI 1.3
alter table o_lti_tool_deployment add constraint lti_sdep_to_tool_idx foreign key (fk_tool_id) references o_lti_tool (id);
create index idx_lti_sdep_to_tool_idx on o_lti_tool_deployment (fk_tool_id);
alter table o_lti_tool_deployment add constraint lti_sdep_to_re_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_lti_sdep_to_re_idx on o_lti_tool_deployment (fk_entry_id);
alter table o_lti_tool_deployment add constraint dep_to_group_idx foreign key (fk_group_id) references o_gp_business(group_id);
create index idx_dep_to_group_idx on o_lti_tool_deployment (fk_group_id);

alter table o_lti_shared_tool_deployment add constraint unique_deploy_platform unique (l_deployment_id, fk_platform_id);
alter table o_lti_shared_tool_deployment add constraint lti_sha_dep_to_tool_idx foreign key (fk_platform_id) references o_lti_platform (id);
create index idx_lti_sha_dep_to_tool_idx on o_lti_shared_tool_deployment (fk_platform_id);
alter table o_lti_shared_tool_deployment add constraint lti_shared_to_re_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_lti_shared_to_re_idx on o_lti_shared_tool_deployment (fk_entry_id);
alter table o_lti_shared_tool_deployment add constraint lti_shared_to_bg_idx foreign key (fk_group_id) references o_gp_business (group_id);
create index idx_lti_shared_to_bg_idx on o_lti_shared_tool_deployment (fk_group_id);

alter table o_lti_shared_tool_service add constraint lti_sha_ser_to_dep_idx foreign key (fk_deployment_id) references o_lti_shared_tool_deployment (id);
create index idx_lti_sha_ser_to_dep_idx on o_lti_shared_tool_service (fk_deployment_id);

create index idx_lti_kid_idx on o_lti_key (l_key_id);

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

alter table o_lecture_reminder add constraint lec_reminder_block_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_lec_reminder_block_idx on o_lecture_reminder(fk_lecture_block);
alter table o_lecture_reminder add constraint lec_reminder_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_lec_reminder_identity_idx on o_lecture_reminder(fk_identity);

alter table o_lecture_participant_summary add constraint lec_part_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_lec_part_entry_idx on o_lecture_participant_summary(fk_entry);
alter table o_lecture_participant_summary add constraint lec_part_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_lec_part_ident_idx on o_lecture_participant_summary(fk_identity);

alter table o_lecture_entry_config add constraint lec_entry_config_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_lec_entry_conf_entry_idx on o_lecture_entry_config(fk_entry);

create index idx_lec_audit_entry_idx on o_lecture_block_audit_log(fk_entry);
create index idx_lec_audit_ident_idx on o_lecture_block_audit_log(fk_identity);

alter table o_lecture_block_to_tax_level add constraint lblock_rel_to_lblock_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_lblock_rel_to_lblock_idx on o_lecture_block_to_tax_level (fk_lecture_block);
alter table o_lecture_block_to_tax_level add constraint lblock_rel_to_tax_lev_idx foreign key (fk_taxonomy_level) references o_tax_taxonomy_level (id);
create index idx_lblock_rel_to_tax_lev_idx on o_lecture_block_to_tax_level (fk_taxonomy_level);

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

-- dialog elements
alter table o_dialog_element add constraint dial_el_author_idx foreign key (fk_author) references o_bs_identity (id);
create index idx_dial_el_author_idx on o_dialog_element (fk_author);
alter table o_dialog_element add constraint dial_el_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_dial_el_entry_idx on o_dialog_element (fk_entry);
alter table o_dialog_element add constraint dial_el_forum_idx foreign key (fk_forum) references o_forum (forum_id);
create index idx_dial_el_forum_idx on o_dialog_element (fk_forum);
create index idx_dial_el_subident_idx on o_dialog_element (d_subident);

-- licenses
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

-- Course
alter table o_course_element add constraint courseele_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_courseele_entry_idx on o_course_element (fk_entry);
create unique index idx_courseele_subident_idx on o_course_element (c_subident, fk_entry);

-- Course styles
create unique index idx_course_colcat_ident on o_course_color_category (c_identifier);

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
alter table o_ap_appointment add constraint ap_appointment_teams_idx foreign key (fk_teams_id) references o_teams_meeting (id);
create index idx_ap_appointment_teams_idx on o_ap_appointment(fk_teams_id);
alter table o_ap_participation add constraint ap_part_appointment_idx foreign key (fk_appointment_id) references o_ap_appointment (id);
create index idx_ap_part_appointment_idx on o_ap_participation(fk_appointment_id);
alter table o_ap_participation add constraint ap_part_identity_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_ap_part_identitiy_idx on o_ap_participation(fk_identity_id);

-- Organiation role rights
alter table o_org_role_to_right add constraint org_role_to_right_to_organisation_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_org_role_to_right_to_organisation_idx on o_org_role_to_right (fk_organisation);

-- Contact tracing
alter table o_ct_registration add constraint reg_to_loc_idx foreign key (fk_location) references o_ct_location (id);
create index idx_reg_to_loc_idx on o_ct_registration (fk_location);
create index idx_qr_id_idx on o_ct_location (l_qr_id);

-- Immunity proof
-- alter table o_immunity_proof add constraint immunity_proof_to_user_idx foreign key (fk_user) references o_user (user_id);
create unique index idx_immunity_proof on o_immunity_proof (fk_user);

-- Zoom
alter table o_zoom_profile add constraint zoom_profile_tool_idx foreign key (fk_lti_tool_id) references o_lti_tool (id);
create index idx_zoom_profile_tool_idx on o_zoom_profile (fk_lti_tool_id);

alter table o_zoom_config add constraint zoom_config_profile_idx foreign key (fk_profile) references o_zoom_profile (id);
create index idx_zoom_config_profile_idx on o_zoom_config (fk_profile);

alter table o_zoom_config add constraint zoom_config_tool_deployment_idx foreign key (fk_lti_tool_deployment_id) references o_lti_tool_deployment (id);
create index idx_zoom_config_tool_deployment_idx on o_zoom_config (fk_lti_tool_deployment_id);


insert into hibernate_unique_key values ( 0 );