-- Export
create table o_ex_export_metadata (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   e_archive_type varchar(32),
   e_title varchar(255),
   e_description varchar(4000),
   e_file_name varchar(255),
   e_file_path varchar(1024),
   e_only_administrators bool default false,
   e_expiration_date datetime,
   fk_entry bigint,
   e_sub_ident varchar(2048),
   fk_task bigint,
   fk_creator bigint,
   fk_metadata bigint,
   primary key (id)
);
alter table o_ex_export_metadata ENGINE = InnoDB;

alter table o_ex_export_metadata add constraint export_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_ex_export_metadata add constraint export_to_creator_idx foreign key (fk_creator) references o_bs_identity (id);
alter table o_ex_export_metadata add constraint export_to_task_idx foreign key (fk_task) references o_ex_task (id);
alter table o_ex_export_metadata add constraint export_to_vfsdata_idx foreign key (fk_metadata) references o_vfs_metadata(id);

-- Content Editor
alter table o_ce_page_part add column p_storage_path varchar(255);

-- Identity
alter table o_bs_identity add column plannedinactivationdate datetime;
alter table o_bs_identity add column planneddeletiondate datetime;

-- VFS
alter table o_vfs_metadata add column f_deleted_date datetime;
alter table o_vfs_metadata add column fk_deleted_by bigint;

-- Media to Page Part (Content Editor)
create table o_media_to_page_part (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   pos bigint default null,
   fk_media bigint not null,
   fk_media_version bigint default null,
   fk_identity bigint default null,
   fk_page_part bigint not null,
   primary key (id)
);
alter table o_media_to_page_part ENGINE = InnoDB;

alter table o_media_to_page_part add constraint media_to_page_part_media_idx foreign key (fk_media) references o_media (id);
alter table o_media_to_page_part add constraint media_to_page_part_media_version_idx foreign key (fk_media_version) references o_media_version (id);
alter table o_media_to_page_part add constraint media_to_page_part_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_media_to_page_part add constraint media_to_page_part_page_part_idx foreign key (fk_page_part) references o_ce_page_part (id);

-- Reminders
alter table o_rem_reminder add column r_email_copy_only bool default false;

-- Peer review
alter table o_gta_task add column g_peerreview_due_date datetime;

alter table o_gta_task add column fk_survey bigint;

alter table o_gta_task add constraint gtask_survey_idx foreign key (fk_survey) references o_eva_form_survey (id);

create table o_gta_review_assignment (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_assigned bool not null default true,
   g_status varchar(32) not null default 'open',
   g_rating float(65,30) default null,
   fk_task bigint not null,
   fk_assignee bigint not null,
   fk_participation bigint,
   primary key (id)
);
alter table o_gta_review_assignment ENGINE = InnoDB;

alter table o_gta_review_assignment add constraint assignment_to_gtask_idx foreign key (fk_task) references o_gta_task (id);
alter table o_gta_review_assignment add constraint assignee_to_gtask_idx foreign key (fk_assignee) references o_bs_identity (id);
alter table o_gta_review_assignment add constraint assignment_to_fpart_idx foreign key (fk_participation) references o_eva_form_participation (id);

-- Open Badges
create table o_badge_organization (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   b_type varchar(64) not null,
   b_organization_key varchar(80) not null,
   b_organization_value longtext not null,
   primary key (id)
);
alter table o_badge_organization ENGINE = InnoDB;

alter table o_badge_class add fk_badge_organization bigint;
alter table o_badge_class add constraint badge_class_organization_idx foreign key (fk_badge_organization) references o_badge_organization (id);

-- Topic broker
create table o_tb_broker (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   t_max_selections int8,
   t_selection_start_date datetime,
   t_selection_end_date datetime,
   t_required_enrollments int8,
   t_p_can_edit_r_enrollments bool,
   t_auto_enrollment bool,
   t_enrollment_start_date datetime,
   t_enrollment_done_date datetime,
   t_p_can_withdraw bool,
   t_withdraw_end_date datetime,
   fk_entry int8,
   t_subident varchar(64),
   primary key (id)
);
create table o_tb_participant (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   t_boost int8,
   t_required_enrollments int8,
   fk_broker bigint not null,
   fk_identity bigint not null,
   primary key (id)
);
create table o_tb_topic (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   t_identifier varchar(64),
   t_title varchar(1024),
   t_description longtext,
   t_min_participants integer,
   t_max_participants integer,
   t_sort_order integer not null,
   t_deleted_date datetime,
   fk_deleted_by bigint,
   fk_creator bigint not null,
   fk_broker bigint,
   primary key (id)
);
create table o_tb_selection (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   t_enrolled bool not null default false,
   t_sort_order integer not null,
   fk_creator bigint not null,
   fk_participant bigint not null,
   fk_topic bigint not null,
   primary key (id)
);
create table o_tb_audit_log (
   id bigint not null auto_increment,
   creationdate datetime not null,
   t_action varchar(32) not null,
   t_before longtext,
   t_after longtext,
   fk_doer bigint,
   fk_broker bigint,
   fk_participant bigint,
   fk_topic bigint,
   fk_selection bigint,
   primary key (id)
);
alter table o_tb_broker ENGINE = InnoDB;
alter table o_tb_participant ENGINE = InnoDB;
alter table o_tb_topic ENGINE = InnoDB;
alter table o_tb_selection ENGINE = InnoDB;
alter table o_tb_audit_log ENGINE = InnoDB;

create index idx_tb_broker_to_re_idx on o_tb_broker (fk_entry);
create index idx_tb_broker__enr_start_idx on o_tb_broker (t_enrollment_start_date);
alter table o_tb_participant add constraint tbpart_broker_idx foreign key (fk_broker) references o_tb_broker (id);
alter table o_tb_participant add constraint tbpart_ident_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_tb_topic add constraint tbtopic_broker_idx foreign key (fk_broker) references o_tb_broker (id);
alter table o_tb_selection add constraint tbselection_creator_idx foreign key (fk_creator) references o_bs_identity (id);
alter table o_tb_selection add constraint tbselection_topic_idx foreign key (fk_topic) references o_tb_topic (id);
alter table o_tb_selection add constraint tbselection_part_idx foreign key (fk_participant) references o_tb_participant (id);
create index idx_tb_audit_doer_idx on o_tb_audit_log (fk_doer);
create index idx_tb_audit_broker_idx on o_tb_audit_log (fk_broker);
create index idx_tb_audit_topic_idx on o_tb_audit_log (fk_topic);
create index idx_tb_audit_part_idx on o_tb_audit_log (fk_participant);

