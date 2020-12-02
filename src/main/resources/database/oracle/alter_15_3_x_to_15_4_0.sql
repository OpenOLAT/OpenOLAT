-- Appointments
alter table o_ap_topic add a_participation_visible number default 1 not null;

-- VFS
alter table o_vfs_metadata add fk_lastmodified_by number(20) default null;

alter table o_vfs_metadata add constraint fmeta_modified_by_idx foreign key (fk_lastmodified_by) references o_bs_identity (id);
create index idx_fmeta_modified_by_idx on o_vfs_metadata (fk_lastmodified_by);
