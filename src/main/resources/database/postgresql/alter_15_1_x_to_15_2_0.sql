alter table o_user add column u_nickname varchar(255);


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

