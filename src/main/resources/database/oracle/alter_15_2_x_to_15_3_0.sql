-- Document editor
drop table o_wopi_access;
create table o_de_access (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   o_editor_type varchar(64) not null,
   o_expires_at timestamp not null,
   o_mode varchar(64) not null,
   o_version_controlled number default 0 not null,
   fk_metadata number(20) not null,
   fk_identity number(20) not null,
   primary key (id)
);
create table o_de_user_info (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   o_info varchar(2048) not null,
   fk_identity number(20) not null,
   primary key (id)
);

create unique index idx_de_userinfo_ident_idx on o_de_user_info(fk_identity);


-- Assessment
alter table o_as_entry add a_current_run_start timestamp;

alter table o_as_mode_course add a_end_status varchar(32);

-- Disadvantage compensation
alter table o_qti_assessmenttest_session add q_compensation_extra_time number(20);

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

alter table o_as_compensation add constraint compensation_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_compensation_ident_idx on o_as_compensation(fk_identity);
alter table o_as_compensation add constraint compensation_crea_idx foreign key (fk_creator) references o_bs_identity (id);
create index idx_compensation_crea_idx on o_as_compensation(fk_creator);
alter table o_as_compensation add constraint compensation_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_compensation_entry_idx on o_as_compensation(fk_entry);


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

create index comp_log_entry_idx on o_as_compensation_log (fk_entry_id);
create index comp_log_ident_idx on o_as_compensation_log (fk_identity_id);


-- Appointments
alter table o_ap_appointment add fk_meeting_id number(20);
alter table o_ap_appointment add constraint ap_appointment_meeting_idx foreign key (fk_meeting_id) references o_bbb_meeting (id);
create index idx_ap_appointment_meeting_idx on o_ap_appointment(fk_meeting_id);

-- Organiation role rights
create table o_org_role_to_right (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   o_role varchar(255) not null,
   o_right varchar(255) not null,
   fk_organisation number(20) not null,
   primary key (id)
);

alter table o_org_role_to_right add constraint org_role_to_right_to_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_org_role_to_r_to_org_idx on o_org_role_to_right(fk_organisation);


-- Lectures
alter table o_lecture_reason add l_enabled number default 1 not null;

-- Absences
alter table o_lecture_absence_category add l_enabled number default 1 not null;

-- Contact tracing
create table o_contact_tracing_location (
    id number(20) generated  always as identity,
    creationdate date not null,
    lastmodified date not null,
    l_reference varchar2(255) not null,
    l_titel varchar2(255) not null,
    l_room varchar2(255) not null,
    l_building varchar2(255) not null,
    l_qr_id varchar2(255) not null,
    l_qr_text varchar2(4000),
    l_guests number not null,
    unique(l_qr_id),
    primary key (id)
);

create table o_contact_tracing_entry (
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
    fk_location number(20) not null,
    primary key (id)
);
