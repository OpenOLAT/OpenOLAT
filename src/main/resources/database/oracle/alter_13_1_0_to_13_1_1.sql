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

drop index idx_eva_part_executor_idx;
create unique index idx_eva_part_executor_idx on o_eva_form_participation  (case when fk_executor is not null and fk_survey is not null then fk_executor || ',' || fk_survey end);

drop index idx_eva_surv_ores_idx;
create unique index idx_eva_surv_ores_idx on o_eva_form_survey  (case when e_sub_ident is null then e_resid || ',' || e_resname else e_resid || ',' || e_resname || ',' || e_sub_ident end);

