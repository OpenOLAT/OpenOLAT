alter table o_user add u_nickname varchar2(255 char);


-- BigBlueButton
create table o_bbb_attendee (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   b_role varchar2(32),
   b_join_date date,
   b_pseudo varchar2(255),
   fk_identity_id number(20),
   fk_meeting_id number(20) not null,
   primary key (id)
);

alter table o_bbb_attendee add constraint bbb_attend_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_bbb_attend_ident_idx on o_bbb_attendee(fk_identity_id);
alter table o_bbb_attendee add constraint bbb_attend_meet_idx foreign key (fk_meeting_id) references o_bbb_meeting (id);
create index idx_bbb_attend_meet_idx on o_bbb_attendee(fk_meeting_id);


alter table o_bbb_meeting add b_main_presenter varchar2(255);
alter table o_bbb_meeting add fk_creator_id number(20);
alter table o_bbb_meeting add constraint bbb_meet_creator_idx foreign key (fk_creator_id) references o_bs_identity (id);
create index idx_bbb_meet_creator_idx on o_bbb_meeting(fk_creator_id);
