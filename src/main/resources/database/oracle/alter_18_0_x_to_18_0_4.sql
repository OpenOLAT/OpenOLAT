-- Certificate
alter table o_cer_certificate add fk_metadata number(20);

alter table o_cer_certificate add constraint certificate_metadata_idx foreign key (fk_metadata) references o_vfs_metadata(id);
create index idx_certificate_metadata_idx on o_cer_certificate (fk_metadata);


-- Content editor
alter table o_ce_page add fk_preview_metadata number(20);
alter table o_ce_page add p_preview_path varchar(255);

alter table o_ce_page add constraint page_preview_metadata_idx foreign key (fk_preview_metadata) references o_vfs_metadata(id);
create index idx_page_preview_metadata_idx on o_ce_page (fk_preview_metadata);
