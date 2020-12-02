-- Appointments
alter table o_ap_topic add column a_participation_visible bool default true not null;

-- VFS
alter table o_vfs_metadata add column fk_lastmodified_by bigint default null;

alter table o_vfs_metadata add constraint fmeta_modified_by_idx foreign key (fk_lastmodified_by) references o_bs_identity (id);
