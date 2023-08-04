alter table o_cer_certificate add column fk_metadata bigint;

alter table o_cer_certificate add constraint certificate_metadata_idx foreign key (fk_metadata) references o_vfs_metadata(id);
