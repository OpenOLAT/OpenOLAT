-- quality management
create table o_qual_report_access (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  q_type varchar(64),
  q_role varchar(64),
  q_online bit default 0,
  q_email_trigger varchar(64),
  fk_data_collection bigint,
  fk_generator bigint,
  fk_group bigint,
  primary key (id)
);

alter table o_qual_report_access ENGINE = InnoDB;

alter table o_qual_report_access add constraint qual_repacc_to_dc_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
alter table o_qual_report_access add constraint qual_repacc_to_generator_idx foreign key (fk_generator) references o_qual_generator (id);


-- repository
alter table o_repositoryentry add column bookable bit default 0 not null;
alter table o_repositoryentry modify objectives text(32000);
alter table o_repositoryentry modify requirements text(32000);
alter table o_repositoryentry modify credits text(32000);
alter table o_repositoryentry modify expenditureofwork text(32000);


-- binder
alter table o_pf_assignment add column p_template bit default 0;
alter table o_pf_assignment add column fk_binder_id bigint;
alter table o_pf_assignment add constraint pf_assign_binder_idx foreign key (fk_binder_id) references o_pf_binder (id);

alter table o_pf_assignment modify fk_section_id bigint null;
