-- Taxonomy
alter table o_tax_taxonomy_level modify column t_identifier varchar(255);
alter table o_tax_taxonomy_level modify column t_m_path_identifiers text;


-- Curriculum
alter table o_cur_curriculum add column c_lectures bool default false not null;

