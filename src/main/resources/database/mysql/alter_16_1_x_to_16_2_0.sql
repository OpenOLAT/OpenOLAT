-- Assessment mode
alter table o_as_mode_course add column a_safeexambrowserconfig_xml mediumtext;
alter table o_as_mode_course add column a_safeexambrowserconfig_plist mediumtext;
alter table o_as_mode_course add column a_safeexambrowserconfig_pkey varchar(255);
alter table o_as_mode_course add column a_safeexambrowserconfig_dload bool not null default true;

-- VFS metadata
alter table o_vfs_metadata add column f_expiration_date timestamp default null;
create index f_exp_date_idx on o_vfs_metadata (f_expiration_date);

-- Task
alter table o_ex_task add column e_progress decimal default null;
alter table o_ex_task add column e_checkpoint varchar(255) default null;

-- Repository entry
alter table o_repositoryentry add column teaser varchar(255) default null;

-- IM
alter table o_im_roster_entry add column r_ressubpath varchar(255) default null;
alter table o_im_roster_entry add column r_channel varchar(255) default null;
alter table o_im_roster_entry add column r_persistent bool not null default false;
alter table o_im_roster_entry add column r_active bool not null default true;
alter table o_im_roster_entry add column r_read_upto timestamp default null;

create index idx_im_rost_sub_idx on o_im_roster_entry (r_resid,r_resname,r_ressubpath);

alter table o_im_message add column msg_ressubpath varchar(255) default null;
alter table o_im_message add column msg_channel varchar(255) default null;
alter table o_im_message add column msg_type varchar(8) not null default 'text';
alter table o_im_message add column fk_meeting_id bigint;
alter table o_im_message add column fk_teams_id bigint;

create index idx_im_msg_channel_idx on o_im_message (msg_resid,msg_resname,msg_ressubpath,msg_channel);

alter table o_im_message add constraint im_msg_bbb_idx foreign key (fk_meeting_id) references o_bbb_meeting (id);
alter table o_im_message add constraint im_msg_teams_idx foreign key (fk_teams_id) references o_teams_meeting (id);

alter table o_im_notification add column chat_ressubpath varchar(255) default null;
alter table o_im_notification add column chat_channel varchar(255) default null;
alter table o_im_notification add column chat_type varchar(16) not null default 'message';

create index idx_im_chat_typed_idx on o_im_notification (fk_to_identity_id,chat_type);

-- Message
create table o_as_message (
   id bigint not null auto_increment,
   lastmodified datetime not null,
   creationdate datetime not null,
   a_message varchar(2000) not null,
   a_publication_date datetime not null,
   a_expiration_date datetime not null,
   a_publication_type varchar(32) not null default 'asap',
   a_message_sent bool not null default false,
   fk_entry bigint not null,
   fk_author bigint,
   a_ressubpath varchar(255),
   primary key (id)
);
alter table o_as_message ENGINE = InnoDB;

alter table o_as_message add constraint as_msg_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);

create table o_as_message_log (
   id bigint not null auto_increment,
   lastmodified datetime not null,
   creationdate datetime not null,
   a_read bool not null default false,
   fk_message bigint not null,
   fk_identity bigint not null,
   primary key (id)
);
alter table o_as_message_log ENGINE = InnoDB;

alter table o_as_message_log add constraint as_msg_log_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_as_message_log add constraint as_msg_log_msg_idx foreign key (fk_message) references o_as_message (id);

-- Grade
create table o_gr_grade_system (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_identifier varchar(64) not null,
   g_predefined bool not null default false,
   g_type varchar(32) not null,
   g_enabled bool not null default true,
   g_resolution varchar(32),
   g_rounding varchar(32),
   g_best_grade integer,
   g_lowest_grade integer,
   g_cut_value decimal(65,30),
   primary key (id)
);
create table o_gr_performance_class (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_identifier varchar(50),
   g_best_to_lowest integer,
   g_passed bool not null default false,
   fk_grade_system bigint not null,
   primary key (id)
);
create table o_gr_grade_scale (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_min_score decimal(65,30),
   g_max_score decimal(65,30),
   fk_grade_system bigint,
   fk_entry bigint not null,
   g_subident varchar(64) not null,
   primary key (id)
);
create table o_gr_breakpoint (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_score decimal(65,30),
   g_grade varchar(50),
   g_best_to_lowest integer,
   fk_grade_scale bigint not null,
   primary key (id)
;
alter table o_as_entry add a_grade varchar(100);
alter table o_as_entry add a_performance_class_ident varchar(50);
alter table o_as_eff_statement add grade varchar(100);
alter table o_as_eff_statement add performance_class_ident varchar(50);
alter table o_course_element add c_grade bool not null default false;
alter table o_course_element add c_auto_grade bool not null default false;

alter table o_gr_grade_system ENGINE = InnoDB;
alter table o_gr_performance_class ENGINE = InnoDB;
alter table o_gr_grade_scale ENGINE = InnoDB;
alter table o_gr_breakpoint ENGINE = InnoDB;

create unique index idx_grsys_ident on o_gr_grade_system (g_identifier);
alter table o_gr_grade_scale add constraint grscale_to_entry_idx foreign key (fk_grade_system) references o_gr_grade_system (id);
alter table o_gr_performance_class add constraint perf_to_grsys_idx foreign key (fk_grade_system) references o_gr_grade_system (id);
alter table o_gr_grade_scale add constraint grscale_to_grsys_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_gr_breakpoint add constraint grbp_to_grsys_idx foreign key (fk_grade_scale) references o_gr_grade_scale (id);
