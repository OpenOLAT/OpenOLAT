-- References history
create table o_references_history (
   id bigint not null auto_increment,
   creationdate datetime not null,
   userdata varchar(64),
   fk_source bigint not null,
   fk_target bigint not null,
   fk_identity bigint,
   primary key (id)
);
alter table o_references_history ENGINE = InnoDB;

alter table o_references_history add constraint ref_hist_source_idx foreign key (fk_source) references o_olatresource (resource_id);
alter table o_references_history add constraint ref_hist_target_idx foreign key (fk_target) references o_olatresource (resource_id);
alter table o_references_history add constraint ref_hist_ident_idx foreign key (fk_identity) references o_bs_identity (id);
