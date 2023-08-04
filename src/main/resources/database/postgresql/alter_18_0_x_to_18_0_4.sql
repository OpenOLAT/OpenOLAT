alter table o_cer_certificate add column fk_metadata int8;

alter table o_cer_certificate add constraint certificate_metadata_idx foreign key (fk_metadata) references o_vfs_metadata(id);
create index idx_certificate_metadata_idx on o_cer_certificate (fk_metadata);