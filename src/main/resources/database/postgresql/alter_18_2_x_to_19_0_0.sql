-- Export
create table o_ex_export_metadata (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   e_archive_type varchar(32),
   e_title varchar(255),
   e_description varchar(4000),
   e_file_name varchar(255),
   e_file_path varchar(1024),
   e_only_administrators bool default false,
   e_expiration_date timestamp,
   fk_entry int8,
   e_sub_ident varchar(2048),
   fk_task int8,
   fk_creator int8,
   fk_metadata int8,
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
alter table o_ce_page_part add column p_storage_path varchar(255);

-- Identity
alter table o_bs_identity add column plannedinactivationdate timestamp;
alter table o_bs_identity add column planneddeletiondate timestamp;

-- VFS
alter table o_vfs_metadata add column f_deleted_date timestamp;
alter table o_vfs_metadata add column fk_deleted_by int8;

-- Media to Page Part (Content Editor)
create table o_media_to_page_part (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   pos int8 default null,
   fk_media int8 not null,
   fk_media_version int8 default null,
   fk_identity int8 default null,
   fk_page_part int8 not null,
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

-- Reminder
alter table o_rem_reminder add r_email_copy_only bool default false;

-- Peer review
alter table o_gta_task add column g_peerreview_due_date timestamp;

alter table o_gta_task add column fk_survey int8;

alter table o_gta_task add constraint gtask_survey_idx foreign key (fk_survey) references o_eva_form_survey (id);
create index idx_gtask_survey_idx on o_gta_task(fk_survey);

create table o_gta_review_assignment (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_assigned bool not null default true,
   g_status varchar(32) not null default 'open',
   g_rating decimal default null,
   fk_task int8 not null,
   fk_assignee int8 not null,
   fk_participation int8,
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
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   b_type varchar(64) not null,
   b_organization_key varchar(80) not null,
   b_organization_value text not null,
   primary key (id)
);

alter table o_badge_class add fk_badge_organization int8;
alter table o_badge_class add constraint badge_class_organization_idx foreign key (fk_badge_organization) references o_badge_organization (id);
create index idx_badge_class_organization_idx on o_badge_class(fk_badge_organization);

-- Topic broker
create table o_tb_broker (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   t_max_selections int8,
   t_selection_start_date timestamp,
   t_selection_end_date timestamp,
   t_required_enrollments int8,
   t_p_can_edit_r_enrollments bool,
   t_auto_enrollment bool,
   t_enrollment_start_date timestamp,
   t_enrollment_done_date timestamp,
   t_p_can_withdraw bool,
   t_withdraw_end_date timestamp,
   fk_entry int8,
   t_subident varchar(64),
   primary key (id)
);
create table o_tb_participant (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   t_boost int8,
   t_required_enrollments int8,
   fk_broker int8 not null,
   fk_identity int8 not null,
   primary key (id)
);
create table o_tb_topic (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   t_identifier varchar(64),
   t_title varchar(1024),
   t_description text,
   t_min_participants int8,
   t_max_participants int8,
   t_sort_order int8 not null,
   t_deleted_date timestamp,
   fk_deleted_by int8,
   fk_creator int8 not null,
   fk_broker int8 not null,
   primary key (id)
);
create table o_tb_selection (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   t_enrolled bool not null default false,
   t_sort_order int8 not null,
   fk_creator int8 not null,
   fk_participant int8 not null,
   fk_topic int8 not null,
   primary key (id)
);
create table o_tb_audit_log (
   id bigserial,
   creationdate timestamp not null,
   t_action varchar(32) not null,
   t_before text,
   t_after text,
   fk_doer int8,
   fk_broker int8,
   fk_participant int8,
   fk_topic int8,
   fk_selection int8,
   primary key (id)
);

-- feed tag (blog/podcast)
create table o_feed_tag (
    id bigserial,
    creationdate timestamp not null,
    fk_feed int8 not null,
    fk_feed_item int8,
    fk_tag int8,
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
alter table o_feed_tag add constraint tag_feed_idx foreign key (fk_feed) references o_feed (id);
alter table o_feed_tag add constraint tag_feed_item_idx foreign key (fk_feed_item) references o_feed_item (id);
