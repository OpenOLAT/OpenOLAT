-- Portfolio
alter table o_pf_page_body add p_usage number(20) default 1;
alter table o_pf_page_body add p_synthetic_status varchar(32);


-- VFS
alter table o_vfs_metadata rename column "fk_author" to "fk_initialized_by";
alter table o_vfs_revision rename column "fk_author" to "fk_initialized_by";
alter table o_vfs_revision add fk_lastmodified_by number(20) default null;

alter table o_vfs_revision add constraint fvers_modified_by_idx foreign key (fk_lastmodified_by) references o_bs_identity (id);
create index idx_fvers_mod_by_idx on o_vfs_revision (fk_lastmodified_by);

-- Taxonomy linking in portfolio
create table o_pf_page_to_tax_competence (
	id number(20) generated always as identity,
	creationdate date not null,
	fk_tax_competence number(20) not null,
	fk_pf_page number(20) not null,
	primary key (id)
);

alter table o_pf_page_to_tax_competence add constraint fk_tax_competence_idx foreign key (fk_tax_competence) references o_tax_taxonomy_competence (id);
create index idx_fk_tax_competence_idx on o_pf_page_to_tax_competence (fk_tax_competence);
alter table o_pf_page_to_tax_competence add constraint fk_pf_page_idx foreign key (fk_pf_page) references o_pf_page (id);
create index idx_fk_pf_page_idx on o_pf_page_to_tax_competence (fk_pf_page);


-- Authentication
alter table o_bs_authentication add issuer varchar(255) default 'DEFAULT' not null;

alter table o_bs_authentication drop constraint u_o_bs_authentication;
alter table o_bs_authentication add constraint unique_pro_iss_authusername UNIQUE (provider, issuer, authusername);
