-- Document editor
drop table o_wopi_access;
create table o_de_access (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   o_editor_type varchar(64) not null,
   o_expires_at timestamp not null,
   o_mode varchar(64) not null,
   o_version_controlled bool not null,
   fk_metadata bigint not null,
   fk_identity bigint not null,
   primary key (id)
);

create table o_de_user_info (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   o_info varchar(2048) not null,
   fk_identity bigint not null,
   primary key (id)
);

create unique index idx_de_userinfo_ident_idx on o_de_user_info(fk_identity);


-- Assessment
alter table o_as_entry add column a_current_run_start timestamp;


-- Disadvantage compensation
alter table o_qti_assessmenttest_session add column q_compensation_extra_time int8;

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

alter table o_as_compensation add constraint compensation_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_compensation_ident_idx on o_as_compensation(fk_identity);
alter table o_as_compensation add constraint compensation_crea_idx foreign key (fk_creator) references o_bs_identity (id);
create index idx_compensation_crea_idx on o_as_compensation(fk_creator);
alter table o_as_compensation add constraint compensation_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_compensation_entry_idx on o_as_compensation(fk_entry);


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

create index comp_log_entry_idx on o_as_compensation_log (fk_entry_id);
create index comp_log_ident_idx on o_as_compensation_log (fk_identity_id);


-- Appointments
alter table o_ap_appointment add fk_meeting_id bigint;
alter table o_ap_appointment add constraint ap_appointment_meeting_idx foreign key (fk_meeting_id) references o_bbb_meeting (id);
create index idx_ap_appointment_meeting_idx on o_ap_appointment(fk_meeting_id);

-- Organiation role rights
create table o_org_role_to_right (
	id bigserial,
	creationdate timestamp not null,
	o_role varchar(255) not null,
	o_right varchar(255) not null,
	fk_organisation int8 not null,
	primary key (id)
);

alter table o_org_role_to_right add constraint org_role_to_right_to_organisation_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_org_role_to_right_to_organisation_idx on o_org_role_to_right (fk_organisation);


-- Lectures
alter table o_lecture_reason add column l_enabled bool default true not null;

-- Absences
alter table o_lecture_absence_category add column l_enabled bool default true not null;

-- Contact tracing
create table o_contact_tracing_location (
	id bigserial,
	creationdate timestamp not null,
	lastmodified timestamp not null,
	l_reference varchar(255) not null,
	l_titel varchar(255) not null,
	l_room varchar(255) not null,
	l_building varchar(255) not null,
	l_qr_id varchar(255) not null,
	l_guests boolean not null,
	primary key (id)
);

create table o_contact_tracing_entry (
	id bigserial,
	creationdate timestamp not null,
	l_deletion_date timestamp not null,
	l_start_date timestamp not null,
	l_end_date timestamp,
	l_nick_name varchar(255),
	l_fist_name varchar(255),
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
	fk_location int8 not null,
	primary key (id)
);

