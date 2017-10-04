create table o_gta_mark (
  id bigserial not null,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  fk_tasklist_id int8 not null,
  fk_marker_identity_id int8 not null,
  fk_participant_identity_id int8 not null,
  primary key (id)
);

alter table o_gta_mark add constraint gtamark_tasklist_idx foreign key (fk_tasklist_id) references o_gta_task_list (id);
create index idx_gtamark_tasklist_idx on o_gta_mark (fk_tasklist_id);
