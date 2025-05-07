-- Quality management
alter table o_eva_form_survey add column e_public_part_identifier varchar(128) null;
alter table o_eva_form_participation add column e_email varchar(128) null;
alter table o_eva_form_participation add column e_first_name varchar(128) null;
alter table o_eva_form_participation add column e_last_name varchar(128) null;

create unique index idx_eva_surv_ppident_idx on o_eva_form_survey (e_public_part_identifier);

-- Badges
alter table o_badge_class add column b_root_id varchar(36) default null;
alter table o_badge_class add column b_version_type varchar(32) default null;
alter table o_badge_class add column fk_previous_version bigint default null;
alter table o_badge_class add column fk_next_version bigint default null;

create index o_badge_class_root_id_idx on o_badge_class (b_root_id);

alter table o_badge_class add constraint badge_class_to_previous_version_idx foreign key (fk_previous_version) references o_badge_class (id);
alter table o_badge_class add constraint badge_class_to_next_version_idx foreign key (fk_next_version) references o_badge_class (id);
