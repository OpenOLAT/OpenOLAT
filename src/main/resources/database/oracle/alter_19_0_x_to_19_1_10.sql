-- References history
create table o_references_history (
   id number(20) generated always as identity,
   creationdate date not null,
   userdata varchar(64),
   fk_source number(20) not null,
   fk_target number(20) not null,
   fk_identity number(20),
   primary key (id)
);

alter table o_references_history add constraint ref_hist_source_idx foreign key (fk_source) references o_olatresource (resource_id);
create index idx_ref_hist_source_idx on o_references_history (fk_source);
alter table o_references_history add constraint ref_hist_target_idx foreign key (fk_target) references o_olatresource (resource_id);
create index idx_ref_hist_target_idx on o_references_history (fk_target);
alter table o_references_history add constraint ref_hist_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_ref_hist_ident_idx on o_references_history (fk_identity);
