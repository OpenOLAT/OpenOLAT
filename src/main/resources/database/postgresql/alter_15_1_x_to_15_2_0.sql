alter table o_user add column u_nickname varchar(255);

drop view o_bs_identity_short_v;
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

-- BigBlueButton
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

alter table o_bbb_attendee add constraint bbb_attend_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_bbb_attend_ident_idx on o_bbb_attendee(fk_identity_id);
alter table o_bbb_attendee add constraint bbb_attend_meet_idx foreign key (fk_meeting_id) references o_bbb_meeting (id);
create index idx_bbb_attend_meet_idx on o_bbb_attendee(fk_meeting_id);


alter table o_bbb_meeting add column b_main_presenter varchar(255);
alter table o_bbb_meeting add column fk_creator_id int8;
alter table o_bbb_meeting add constraint bbb_meet_creator_idx foreign key (fk_creator_id) references o_bs_identity (id);
create index idx_bbb_meet_creator_idx on o_bbb_meeting(fk_creator_id);


alter table o_bbb_meeting add column b_recordings_publishing varchar(16) default 'auto';

create table o_bbb_recording (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   b_recording_id varchar(255) not null,
   b_publish_to varchar(128),
   b_start_date timestamp default null,
   b_end_date timestamp default null,
   b_url varchar(1024),
   b_type varchar(32),
   fk_meeting_id int8 not null,
   unique(b_recording_id,fk_meeting_id),
   primary key (id)
);

alter table o_bbb_recording add constraint bbb_record_meet_idx foreign key (fk_meeting_id) references o_bbb_meeting (id);
create index idx_bbb_record_meet_idx on o_bbb_recording(fk_meeting_id);
