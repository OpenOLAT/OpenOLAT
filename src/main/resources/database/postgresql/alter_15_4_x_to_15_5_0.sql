-- Portfolio
alter table o_pf_page_body add column p_usage int8 default 1;
alter table o_pf_page_body add column p_synthetic_status varchar(32);


-- VFS
alter table o_vfs_metadata rename column fk_author to fk_initialized_by;
alter table o_vfs_revision rename column fk_author to fk_initialized_by;
alter table o_vfs_revision add column fk_lastmodified_by bigint default null;

alter table o_vfs_revision add constraint fvers_modified_by_idx foreign key (fk_lastmodified_by) references o_bs_identity (id);
create index idx_fvers_mod_by_idx on o_vfs_revision (fk_lastmodified_by);
