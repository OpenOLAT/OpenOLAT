-- Assessment mode
alter table o_as_mode_course add a_safeexambrowserconfig_xml clob;
alter table o_as_mode_course add a_safeexambrowserconfig_plist clob;
alter table o_as_mode_course add a_safeexambrowserconfig_pkey varchar(255);
alter table o_as_mode_course add a_safeexambrowserconfig_dload number default 1 not null;

-- VFS metadata
alter table o_vfs_metadata add f_expiration_date date default null;
create index f_exp_date_idx on o_vfs_metadata (f_expiration_date);

-- Task
alter table o_ex_task add e_progress decimal default null;
alter table o_ex_task add e_checkpoint varchar(255) default null;

-- Repository entry
alter table o_repositoryentry add teaser varchar2(255) default null;

-- IM
alter table o_im_roster_entry add r_ressubpath varchar(255) default null;
alter table o_im_roster_entry add r_channel varchar(255) default null;
alter table o_im_roster_entry add r_persistent number default 0 not null;
alter table o_im_roster_entry add r_active number default 1 not null;
alter table o_im_roster_entry add r_read_upto date default null;

create index idx_im_rost_sub_idx on o_im_roster_entry (r_resid,r_resname,r_ressubpath);

alter table o_im_message add msg_ressubpath varchar(255) default null;
alter table o_im_message add msg_channel varchar(255) default null;
alter table o_im_message add msg_type varchar(8) default 'text' not null;
alter table o_im_message add fk_meeting_id number(20);
alter table o_im_message add fk_teams_id number(20);

create index idx_im_msg_channel_idx on o_im_message (msg_resid,msg_resname,msg_ressubpath,msg_channel);

alter table o_im_message add constraint im_msg_bbb_idx foreign key (fk_meeting_id) references o_bbb_meeting (id);
create index idx_im_msg_bbb_idx on o_im_message(fk_meeting_id);
alter table o_im_message add constraint im_msg_teams_idx foreign key (fk_teams_id) references o_teams_meeting (id);
create index idx_im_msg_teams_idx on o_im_message(fk_teams_id);

alter table o_im_notification add chat_ressubpath varchar(255) default null;
alter table o_im_notification add chat_channel varchar(255) default null;
alter table o_im_notification add chat_type varchar(16) default 'message' not null;

create index idx_im_chat_typed_idx on o_im_notification (fk_to_identity_id,chat_type);

-- Message
create table o_as_message (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   lastmodified DATE not null,
   creationdate DATE not null,
   a_message varchar2(2000) not null,
   a_publication_date DATE not null,
   a_expiration_date DATE not null,
   a_publication_type varchar2(32) default 'asap' not null ,
   a_message_sent number default 0 not null,
   fk_entry number(20) not null,
   fk_author number(20),
   a_ressubpath varchar2(255),
   PRIMARY KEY (id)
);

alter table o_as_message add constraint as_msg_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_as_msg_entry_idx on o_as_message (fk_entry);

create table o_as_message_log (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   lastmodified DATE not null,
   creationdate DATE not null,
   a_read number default 0 not null,
   fk_message number(20) not null,
   fk_identity number(20) not null,
   PRIMARY KEY (id)
);

alter table o_as_message_log add constraint as_msg_log_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_as_msg_log_identity_idx on o_as_message_log (fk_identity);
alter table o_as_message_log add constraint as_msg_log_msg_idx foreign key (fk_message) references o_as_message (id);
create index idx_as_msg_log_msg_idx on o_as_message_log (fk_message);

-- Grade
create table o_gr_grade_system (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   g_identifier varchar(64) not null,
   g_predefined number default 0 not null,
   g_type varchar(32) not null,
   g_enabled number default 1 not null,
   g_resolution varchar(32),
   g_rounding varchar(32),
   g_best_grade number(20),
   g_lowest_grade number(20),
   g_cut_value decimal,
   primary key (id)
);
create table o_gr_performance_class (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   g_identifier varchar(50),
   g_best_to_lowest number(20),
   g_passed number default 0 not null,
   fk_grade_system number(20) not null,
   primary key (id)
);
create table o_gr_grade_scale (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   g_min_score decimal,
   g_max_score decimal,
   fk_grade_system number(20),
   fk_entry number(20) not null,
   g_subident varchar(64) not null,
   primary key (id)
);
create table o_gr_breakpoint (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   g_score decimal,
   g_grade varchar(50),
   g_best_to_lowest number(20),
   fk_grade_scale number(20) not null,
   primary key (id)
);
alter table o_as_entry add a_grade varchar(100);
alter table o_as_entry add a_performance_class_ident varchar(50);
alter table o_as_eff_statement add grade varchar(100);
alter table o_as_eff_statement add performance_class_ident varchar(50);
alter table o_course_element add c_grade number default 0 not null;
alter table o_course_element add c_auto_grade number default 0 not null;

create unique index idx_grade_system_ident on o_gr_grade_system (g_identifier);
alter table o_gr_grade_scale add constraint grscale_to_grsys_idx foreign key (fk_grade_system) references o_gr_grade_system (id);
create index idx_grscale_to_grsys_idx on o_gr_grade_scale (fk_grade_system);
alter table o_gr_performance_class add constraint perf_to_grsys_idx foreign key (fk_grade_system) references o_gr_grade_system (id);
create index idx_perf_to_grsys_idx on o_gr_performance_class (fk_grade_system);
alter table o_gr_grade_scale add constraint grscale_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_grscale_entry_idx on o_gr_grade_scale (fk_entry);
alter table o_gr_breakpoint add constraint grbp_to_grscale_idx foreign key (fk_grade_scale) references o_gr_grade_scale (id);
create index idx_grbp_to_grscale_idx on o_gr_breakpoint (fk_grade_scale);
