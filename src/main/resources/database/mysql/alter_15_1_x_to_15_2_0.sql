alter table o_user add column u_nickname varchar(255);


-- BigBlueButton
create table o_bbb_attendee (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   b_role varchar(32),
   b_join_date datetime,
   b_pseudo varchar(255),
   fk_identity_id bigint,
   fk_meeting_id bigint not null,
   primary key (id)
);

alter table o_bbb_attendee ENGINE = InnoDB;

alter table o_bbb_attendee add constraint bbb_attend_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
alter table o_bbb_attendee add constraint bbb_attend_meet_idx foreign key (fk_meeting_id) references o_bbb_meeting (id);

