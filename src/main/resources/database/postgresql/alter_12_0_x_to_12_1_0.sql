-- auto access controll
create table o_ac_auto_advance_order (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  a_identifier_key varchar(64) not null,
  a_identifier_value varchar(64) not null,
  a_status varchar(32) not null,
  a_status_modified timestamp not null,
  fk_identity int8 not null,
  fk_method int8 not null,
  primary key (id)
);

create index idx_ac_aao_id_idx on o_ac_auto_advance_order(id);
create index idx_ac_aao_identifier_idx on o_ac_auto_advance_order(a_identifier_key, a_identifier_value);
create index idx_ac_aao_ident_idx on o_ac_auto_advance_order(fk_identity);
alter table o_ac_auto_advance_order add constraint aao_ident_idx foreign key (fk_identity) references o_bs_identity (id);


-- lectures
alter table o_lecture_block_roll_call add column l_absence_supervisor_noti_date timestamp default null;