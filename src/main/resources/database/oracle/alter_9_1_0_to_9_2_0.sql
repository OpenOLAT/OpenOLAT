alter table o_ex_task add (e_scheduled date);
alter table o_ex_task add (e_ressubpath varchar2(2048 char));
alter table o_ex_task add (e_status_before_edit varchar2(16 char));
alter table o_ex_task add (fk_resource_id number(20));
alter table o_ex_task add (fk_identity_id number(20));

alter table o_ex_task add constraint idx_ex_task_ident_id foreign key (fk_identity_id) references o_bs_identity(id);
create index idx_ex_task_ident_idx on o_ex_task (fk_identity_id);
alter table o_ex_task add constraint idx_ex_task_rsrc_id foreign key (fk_resource_id) references o_olatresource(resource_id);
create index idx_ex_task_rsrc_idx on o_ex_task (fk_resource_id);

create table o_ex_task_modifier (
   id number(20) not null,
   creationdate date not null,
   fk_task_id number(20) not null,
   fk_identity_id number(20) not null,
   primary key (id)
);

alter table o_ex_task_modifier add constraint idx_ex_task_mod_ident_id foreign key (fk_identity_id) references o_bs_identity(id);
create index idx_ex_task_mod_ident_idx on o_ex_task_modifier (fk_identity_id);
alter table o_ex_task_modifier add constraint idx_ex_task_mod_task_id foreign key (fk_task_id) references o_ex_task(id);
create index idx_ex_task_mod_task_idx on o_ex_task_modifier (fk_task_id);
