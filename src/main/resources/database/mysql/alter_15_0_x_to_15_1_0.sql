
-- Appointments
create table o_ap_topic (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   a_title varchar(256),
   a_description varchar(4000),
   fk_entry_id bigint not null,
   a_sub_ident varchar(64) not null,
   primary key (id)
);

create table o_ap_organizer (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   fk_topic_id bigint not null,
   fk_identity_id bigint not null,
   primary key (id)
);

create table o_ap_appointment (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   a_status varchar(64) not null,
   a_status_mod_date datetime,
   a_start datetime,
   a_end datetime,
   a_location varchar(256),
   a_details varchar(4000),
   a_max_participations integer,
   fk_topic_id bigint not null,
   primary key (id)
);

create table o_ap_participation (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   fk_appointment_id bigint not null,
   fk_identity_id bigint not null,
   primary key (id)
);

alter table o_ap_topic ENGINE = InnoDB;
alter table o_ap_organizer ENGINE = InnoDB;
alter table o_ap_appointment ENGINE = InnoDB;
alter table o_ap_participation ENGINE = InnoDB;

alter table o_ap_topic add constraint ap_topic_entry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
alter table o_ap_organizer add constraint ap_organizer_topic_idx foreign key (fk_topic_id) references o_ap_topic (id);
alter table o_ap_organizer add constraint ap_organizer_identity_idx foreign key (fk_identity_id) references o_bs_identity (id);
alter table o_ap_appointment add constraint ap_appointment_topic_idx foreign key (fk_topic_id) references o_ap_topic (id);
alter table o_ap_participation add constraint ap_part_appointment_idx foreign key (fk_appointment_id) references o_ap_appointment (id);
alter table o_ap_participation add constraint ap_part_identity_idx foreign key (fk_identity_id) references o_bs_identity (id);
