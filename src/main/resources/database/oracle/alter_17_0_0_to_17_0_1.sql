-- Taxonomy
alter table o_tax_taxonomy_level modify t_identifier varchar(255);
alter table o_tax_taxonomy_level modify t_m_path_identifiers varchar(32000);


-- Curriculum
alter table o_cur_curriculum add c_lectures number default 0 not null;


