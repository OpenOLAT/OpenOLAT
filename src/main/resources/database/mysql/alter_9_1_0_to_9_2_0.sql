alter table o_ex_task add column e_scheduled datetime;
alter table o_ex_task add column e_ressubpath varchar(2048);
alter table o_ex_task add column e_status_before_edit varchar(16);
alter table o_ex_task add column fk_resource_id bigint;
alter table o_ex_task add column fk_identity_id bigint;

alter table o_ex_task add constraint idx_ex_task_ident_id foreign key (fk_identity_id) references o_bs_identity(id);
alter table o_ex_task add constraint idx_ex_task_rsrc_id foreign key (fk_resource_id) references o_olatresource(resource_id);

create table o_ex_task_modifier (
   id bigint not null,
   creationdate datetime not null,
   fk_task_id bigint not null,
   fk_identity_id bigint not null,
   primary key (id)
);
alter table o_ex_task_modifier ENGINE = InnoDB;

alter table o_ex_task_modifier add constraint idx_ex_task_mod_ident_id foreign key (fk_identity_id) references o_bs_identity(id);
alter table o_ex_task_modifier add constraint idx_ex_task_mod_task_id foreign key (fk_task_id) references o_ex_task(id);
