-- Quality management
alter table o_eva_form_survey add column e_public_part_identifier varchar(128) null;
alter table o_eva_form_participation add column e_email varchar(128) null;
alter table o_eva_form_participation add column e_first_name varchar(128) null;
alter table o_eva_form_participation add column e_last_name varchar(128) null;

create unique index idx_eva_surv_ppident_idx on o_eva_form_survey (lower(e_public_part_identifier)) where e_public_part_identifier is not null;

-- Badges
alter table o_badge_class add column b_root_id varchar(36) default null;
alter table o_badge_class add column b_version_type varchar(32) default null;
alter table o_badge_class add column b_verification_method varchar(32) default 'hosted'; 
alter table o_badge_class add column b_private_key text default null;
alter table o_badge_class add column b_public_key text default null;
alter table o_badge_class add column fk_previous_version int8 default null;
alter table o_badge_class add column fk_next_version int8 default null;

create index o_badge_class_root_id_idx on o_badge_class (b_root_id);

alter table o_badge_class add constraint badge_class_to_previous_version_idx foreign key (fk_previous_version) references o_badge_class (id);
create index idx_badge_class_to_previous_version_idx on o_badge_class(fk_previous_version);

alter table o_badge_class add constraint badge_class_to_next_version_idx foreign key (fk_next_version) references o_badge_class (id);
create index idx_badge_class_to_next_version_idx on o_badge_class(fk_next_version);

-- Topic broker
alter table o_tb_broker add column t_auto_strategy_type varchar(64);
alter table o_tb_broker add column t_operlapping_period_allowed bool default true;
alter table o_tb_topic add column t_begin_date timestamp;
alter table o_tb_topic add column t_end_date timestamp;

-- Feed
alter table o_feed add column f_push_email_comments bool default false;

-- Notifications
alter table o_noti_pub add column fk_root_publisher int8 default null;
alter table o_noti_pub add column fk_parent_publisher int8 default null;
alter table o_noti_pub add column channeltype varchar(16) default 'PULL';

alter table o_noti_pub add constraint pub_to_root_pub_idx foreign key (fk_root_publisher) references o_noti_pub (publisher_id);
create index idx_pub_to_root_pub_idx on o_noti_pub (fk_root_publisher);
alter table o_noti_pub add constraint pub_to_parent_pub_idx foreign key (fk_parent_publisher) references o_noti_pub (publisher_id);
create index idx_pub_to_parent_pub_idx on o_noti_pub (fk_parent_publisher);

-- Lecture block meeting url
alter table o_lecture_block add column l_meeting_title varchar(1024);
alter table o_lecture_block add column l_meeting_url varchar(1024);

-- Appointment scheduling
alter table o_ap_participation add a_comment varchar(4000);
alter table o_ap_appointment add a_use_enrollment_deadline boolean default false not null;
alter table o_ap_appointment add a_enrollment_deadline_minutes int8 default 0 not null;
