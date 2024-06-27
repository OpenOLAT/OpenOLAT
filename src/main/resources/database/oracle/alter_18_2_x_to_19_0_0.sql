-- Export
create table o_ex_export_metadata (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   e_archive_type varchar(32),
   e_title varchar(255),
   e_description varchar(4000),
   e_file_name varchar(255),
   e_file_path varchar(1024),
   e_only_administrators number default 0,
   e_expiration_date date,
   fk_entry number(20),
   e_sub_ident varchar(2048),
   fk_task number(20),
   fk_creator number(20),
   fk_metadata number(20),
   primary key (id)
);

alter table o_ex_export_metadata add constraint export_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_export_to_entry_idx on o_ex_export_metadata(fk_entry);
alter table o_ex_export_metadata add constraint export_to_creator_idx foreign key (fk_creator) references o_bs_identity (id);
create index idx_export_to_creator_idx on o_ex_export_metadata(fk_creator);
alter table o_ex_export_metadata add constraint export_to_task_idx foreign key (fk_task) references o_ex_task (id);
create index idx_export_to_task_idx on o_ex_export_metadata(fk_task);
create index idx_export_sub_ident_idx on o_ex_export_metadata(e_sub_ident);
alter table o_ex_export_metadata add constraint export_to_vfsdata_idx foreign key (fk_metadata) references o_vfs_metadata(id);
create index idx_export_to_vfsdata_idx on o_ex_export_metadata(fk_metadata);

-- Content Editor
alter table o_ce_page_part add p_storage_path varchar(255);

-- Identity
alter table o_bs_identity add plannedinactivationdate date;
alter table o_bs_identity add planneddeletiondate date;

-- VFS
alter table o_vfs_metadata add f_deleted_date date;
alter table o_vfs_metadata add fk_deleted_by number(20);

-- Media to Page Part (Content Editor)
create table o_media_to_page_part (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   pos number(20) default null,
   fk_media number(20) not null,
   fk_media_version number(20) default null,
   fk_identity number(20) default null,
   fk_page_part number(20) not null,
   primary key (id)
);

alter table o_media_to_page_part add constraint media_to_page_part_media_idx foreign key (fk_media) references o_media (id);
create index idx_media_to_page_part_media_idx on o_media_to_page_part (fk_media);
alter table o_media_to_page_part add constraint media_to_page_part_media_version_idx foreign key (fk_media_version) references o_media_version (id);
create index idx_media_to_page_part_media_version_idx on o_media_to_page_part (fk_media_version);
alter table o_media_to_page_part add constraint media_to_page_part_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_media_to_page_part_identity_idx on o_media_to_page_part (fk_identity);
alter table o_media_to_page_part add constraint media_to_page_part_page_part_idx foreign key (fk_page_part) references o_ce_page_part (id);
create index idx_media_to_page_part_page_part_idx on o_media_to_page_part (fk_page_part);

-- Reminders
alter table o_rem_reminder add r_email_copy_only number default 0;

-- Peer review
alter table o_gta_task add g_peerreview_due_date date;

alter table o_gta_task add fk_survey number(20);

alter table o_gta_task add constraint gtask_survey_idx foreign key (fk_survey) references o_eva_form_survey (id);
create index idx_gtask_survey_idx on o_gta_task(fk_survey);

create table o_gta_review_assignment (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   g_assigned number default 1  not null ,
   g_status varchar(32) default 'open' not null,
   g_rating decimal default null,
   fk_task number(20) not null,
   fk_assignee number(20) not null,
   fk_participation number(20),
   primary key (id)
);

alter table o_gta_review_assignment add constraint assignment_to_gtask_idx foreign key (fk_task) references o_gta_task (id);
create index idx_assignment_to_gtask_idx on o_gta_review_assignment(fk_task);

alter table o_gta_review_assignment add constraint assignee_to_gtask_idx foreign key (fk_assignee) references o_bs_identity (id);
create index idx_assignee_to_gtask_idx on o_gta_review_assignment(fk_assignee);

alter table o_gta_review_assignment add constraint assignment_to_fpart_idx foreign key (fk_participation) references o_eva_form_participation (id);
create index idx_assignment_to_fpart_idx on o_gta_review_assignment(fk_participation);

-- Open Badges
create table o_badge_organization (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   b_type varchar(64) not null,
   b_organization_key varchar(80) not null,
   b_organization_value clob not null,
   primary key (id)
);

alter table o_badge_class add fk_badge_organization number(20);
alter table o_badge_class add constraint badge_class_organization_idx foreign key (fk_badge_organization) references o_badge_organization (id);
create index idx_badge_class_organization_idx on o_badge_class(fk_badge_organization);

-- Topic broker
create table o_tb_broker (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   t_max_selections number(20),
   t_selection_start_date date,
   t_selection_end_date date,
   t_required_enrollments number(20),
   t_p_can_edit_r_enrollments number,
   t_auto_enrollment number,
   t_enrollment_start_date date,
   t_enrollment_done_date date,
   t_p_can_withdraw number,
   t_withdraw_end_date date,
   fk_entry number(20),
   t_subident varchar(64),
   primary key (id)
);
create table o_tb_participant (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   t_boost number(20),
   t_required_enrollments number(20),
   fk_broker number(20) not null,
   fk_identity number(20) not null,
   primary key (id)
);
create table o_tb_topic (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   t_identifier varchar(64),
   t_title varchar(1024),
   t_description varchar(4000),
   t_min_participants number(20),
   t_max_participants number(20),
   t_sort_order number(20) not null,
   t_deleted_date date,
   fk_deleted_by number(20),
   fk_creator number(20) not null,
   fk_broker number(20) not null,
   primary key (id)
);
create table o_tb_selection (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   t_enrolled number not null default 0,
   t_sort_order number(20) not null,
   fk_creator number(20) not null,
   fk_participant number(20) not null,
   fk_topic number(20) not null,
   primary key (id)
);
create table o_tb_audit_log (
   id number(20) generated always as identity,
   creationdate date not null,
   t_action varchar(32) not null,
   t_before varchar(4000),
   t_after varchar(4000),
   fk_doer number(20),
   fk_broker number(20),
   fk_participant number(20),
   fk_topic number(20),
   fk_selection number(20),
   primary key (id)
);

-- feed tag (blog/podcast)
create table o_feed_tag (
    id number(20) generated always as identity,
    creationdate date not null,
    fk_feed number(20) not null,
    fk_feed_item number(20),
    fk_tag number(20),
    primary key (id)
);

create index idx_tb_broker_to_re_idx on o_tb_broker (fk_entry);
create index idx_tb_broker__enr_start_idx on o_tb_broker (t_enrollment_start_date);
create index idx_tb_part_to_ident_idx on o_tb_participant (fk_identity);
alter table o_tb_participant add constraint tbpart_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_tbtopic_to_broker_idx on o_tb_topic (fk_broker);
alter table o_tb_topic add constraint tbtopic_broker_idx foreign key (fk_broker) references o_tb_broker (id);
create index idx_tbpart_to_broker_idx on o_tb_participant (fk_broker);
alter table o_tb_participant add constraint tbpart_broker_idx foreign key (fk_broker) references o_tb_broker (id);
create index idx_tbselection_to_topic_idx on o_tb_selection (fk_topic);
alter table o_tb_selection add constraint tbselection_topic_idx foreign key (fk_topic) references o_tb_topic (id);
create index idx_tbselection_to_part_idx on o_tb_selection (fk_participant);
alter table o_tb_selection add constraint tbselection_part_idx foreign key (fk_participant) references o_tb_participant (id);
create index idx_tbselection_to_createor_idx on o_tb_selection (fk_creator);
alter table o_tb_selection add constraint tbselection_creator_idx foreign key (fk_creator) references o_bs_identity (id);
create index idx_tb_audit_doer_idx on o_tb_audit_log (fk_doer);
create index idx_tb_audit_broker_idx on o_tb_audit_log (fk_broker);
create index idx_tb_audit_topic_idx on o_tb_audit_log (fk_topic);
create index idx_tb_audit_part_idx on o_tb_audit_log (fk_participant);

-- feed tags
create index idx_tag_to_feed_idx on o_feed_tag (fk_feed);
create index idx_tag_to_feed_item_idx on o_feed_tag (fk_feed_item);