-- clean setup errors
alter table o_bs_group_member modify creationdate timestamp;
alter table o_bs_group_member modify lastmodified timestamp;

alter table o_repositoryentry drop column canlaunch;

alter table o_bs_invitation drop column version;

alter table o_eva_form_session modify (fk_identity null);
alter table o_eva_form_session modify (fk_page_body null);
alter table o_eva_form_session modify (fk_form_entry null);

create table o_gta_task_revision_date (
  id number(20) not null,
  creationdate date not null,
  g_status varchar(36) not null,
  g_rev_loop number(20) not null,
  g_date date not null,
  fk_task number(20) not null,
  primary key (id)
);

create index idx_dc_to_gen_idx on o_qual_data_collection(fk_generator);

alter table o_qual_context add constraint qual_con_to_data_coll_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
alter table o_qual_reminder add constraint qual_rem_to_data_coll_idx foreign key (fk_data_collection) references o_qual_data_collection (id);

drop index idx_eva_part_executor_idx;
create index idx_eva_part_executor_idx on o_eva_form_participation (fk_executor, fk_survey);

drop index idx_eva_surv_ores_idx;
create index idx_eva_surv_ores_idx on o_eva_form_survey (e_resid, e_resname, e_sub_ident);

