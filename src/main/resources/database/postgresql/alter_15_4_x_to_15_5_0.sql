-- Portfolio
alter table o_pf_page_body add column p_usage int8 default 1;
alter table o_pf_page_body add column p_synthetic_status varchar(32);


-- VFS
alter table o_vfs_metadata rename column fk_author to fk_initialized_by;
alter table o_vfs_revision rename column fk_author to fk_initialized_by;
alter table o_vfs_revision add column fk_lastmodified_by bigint default null;

alter table o_vfs_revision add constraint fvers_modified_by_idx foreign key (fk_lastmodified_by) references o_bs_identity (id);
create index idx_fvers_mod_by_idx on o_vfs_revision (fk_lastmodified_by);

-- Taxonomy linking in portfolio
create table o_pf_page_to_tax_competence (
	id bigserial,
	creationdate timestamp not null,
	fk_tax_competence int8 not null,
	fk_pf_page int8 not null,
	primary key (id)
);
