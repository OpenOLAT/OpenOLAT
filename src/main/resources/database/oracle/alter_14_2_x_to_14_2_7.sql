create table o_bbb_server (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   b_name varchar(128),
   b_url varchar(255) not null,
   b_shared_secret varchar(255),
   b_recording_url varchar(255),
   b_enabled number default 1 not null,
   b_capacity_factor decimal,
   primary key (id)
);

alter table o_bbb_meeting add fk_server_id number(20);

alter table o_bbb_meeting add constraint bbb_meet_serv_idx foreign key (fk_server_id) references o_bbb_server (id);
create index idx_bbb_meet_serv_idx on o_bbb_meeting(fk_server_id);

