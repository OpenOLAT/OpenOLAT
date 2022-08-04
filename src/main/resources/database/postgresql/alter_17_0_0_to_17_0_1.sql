-- Taxonomy
alter table o_tax_taxonomy_level alter column t_identifier type varchar(255);
alter table o_tax_taxonomy_level alter column t_m_path_identifiers type varchar(32000);


-- Curriculum
alter table o_cur_curriculum add column c_lectures bool default false not null;

