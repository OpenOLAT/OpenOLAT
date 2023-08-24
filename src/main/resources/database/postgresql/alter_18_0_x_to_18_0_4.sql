-- Certificate
alter table o_cer_certificate add column fk_metadata int8;

alter table o_cer_certificate add constraint certificate_metadata_idx foreign key (fk_metadata) references o_vfs_metadata(id);
create index idx_certificate_metadata_idx on o_cer_certificate (fk_metadata);


-- Content editor
alter table o_ce_page add column fk_preview_metadata int8;
alter table o_ce_page add column p_preview_path varchar(255);

alter table o_ce_page add constraint page_preview_metadata_idx foreign key (fk_preview_metadata) references o_vfs_metadata(id);
create index idx_page_preview_metadata_idx on o_ce_page (fk_preview_metadata);
