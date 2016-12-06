create table o_pf_binder_user_infos (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_initiallaunchdate datetime,
   p_recentlaunchdate datetime,
   p_visit bigint,
   fk_identity bigint,
   fk_binder bigint,
   unique(fk_identity, fk_binder),
   primary key (id)
);
alter table o_pf_binder_user_infos ENGINE = InnoDB;

alter table o_pf_binder_user_infos add constraint binder_user_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_pf_binder_user_infos add constraint binder_user_binder_idx foreign key (fk_binder) references o_pf_binder (id);


alter table o_repositoryentry add column deletiondate datetime default null;
alter table o_repositoryentry add column fk_deleted_by bigint default null;
alter table o_repositoryentry add constraint re_deleted_to_identity_idx foreign key (fk_deleted_by) references o_bs_identity (id);

