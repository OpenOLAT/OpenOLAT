-- auto access controll
create table o_ac_auto_advance_order (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  a_identifier_key varchar(64) not null,
  a_identifier_value varchar(64) not null,
  a_status varchar(32) not null,
  a_status_modified date not null,
  fk_identity number(20) not null,
  fk_method number(20) not null,
  primary key (id)
);

create index idx_ac_aao_id_idx on o_ac_auto_advance_order(id);
create index idx_ac_aao_identifier_idx on o_ac_auto_advance_order(a_identifier_key, a_identifier_value);
create index idx_ac_aao_ident_idx on o_ac_auto_advance_order(fk_identity);
alter table o_ac_auto_advance_order add constraint aao_ident_idx foreign key (fk_identity) references o_bs_identity (id);


-- lectures
alter table o_lecture_block_roll_call add l_absence_supervisor_noti_date date default null;