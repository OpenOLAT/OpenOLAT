-- Taxonomy
alter table o_tax_taxonomy_level modify t_identifier varchar(255);
alter table o_tax_taxonomy_level modify t_m_path_identifiers varchar(32000);
