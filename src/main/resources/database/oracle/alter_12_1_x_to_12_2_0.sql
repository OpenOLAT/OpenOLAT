create table o_gta_mark (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  fk_tasklist_id number(20) not null,
  fk_marker_identity_id number(20) not null,
  fk_participant_identity_id number(20) not null,
  primary key (id)
);

alter table o_gta_mark add constraint gtamark_tasklist_idx foreign key (fk_tasklist) references o_gta_task_list (id);
create index idx_gtamark_tasklist_idx on o_gta_task (fk_tasklist);
