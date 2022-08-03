-- Taxonomy
alter table o_tax_taxonomy_level modify column t_identifier varchar(255);
alter table o_tax_taxonomy_level modify column t_m_path_identifiers text;
