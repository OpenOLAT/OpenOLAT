create table o_pf_binder_user_infos (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_initiallaunchdate timestamp,
   p_recentlaunchdate timestamp,
   p_visit int4,
   fk_identity int8,
   fk_binder int8,
   unique(fk_identity, fk_binder),
   primary key (id)
);

alter table o_pf_binder_user_infos add constraint binder_user_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_binder_user_to_ident_idx on o_pf_binder_user_infos (fk_identity);
alter table o_pf_binder_user_infos add constraint binder_user_binder_idx foreign key (fk_binder) references o_pf_binder (id);
create index idx_binder_user_binder_idx on o_pf_binder_user_infos (fk_binder);


alter table o_repositoryentry add column deletiondate timestamp default null;
alter table o_repositoryentry add column fk_deleted_by int8 default null;
alter table o_repositoryentry add constraint re_deleted_to_identity_idx foreign key (fk_deleted_by) references o_bs_identity (id);
create index idx_re_deleted_to_identity_idx on o_repositoryentry (fk_deleted_by);
