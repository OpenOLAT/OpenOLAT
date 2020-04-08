create table o_bbb_server (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   b_name varchar(128),
   b_url varchar(255) not null,
   b_shared_secret varchar(255),
   b_recording_url varchar(255),
   b_enabled bool default true,
   b_capacity_factor decimal,
   primary key (id)
);
alter table o_bbb_server ENGINE = InnoDB;

alter table o_bbb_meeting add column fk_server_id bigint;

alter table o_bbb_meeting add constraint bbb_meet_serv_idx foreign key (fk_server_id) references o_bbb_server (id);

