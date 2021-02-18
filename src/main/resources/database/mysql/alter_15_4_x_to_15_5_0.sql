-- Portfolio
alter table o_pf_page_body add column p_usage bigint default 1;
alter table o_pf_page_body add column p_synthetic_status varchar(32);


-- VFS 
alter table o_vfs_metadata change fk_author fk_initialized_by bigint(20) null;
alter table o_vfs_revision change fk_author fk_initialized_by bigint(20) null;
alter table o_vfs_revision add column fk_lastmodified_by bigint default null;

alter table o_vfs_revision add constraint fvers_modified_by_idx foreign key (fk_lastmodified_by) references o_bs_identity (id);
