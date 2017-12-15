-- QPool
alter table o_qp_item add column q_topic varchar(1024);
alter table o_qp_item add column q_status_last_modified datetime;

create index idx_tax_level_path_key_idx on o_tax_taxonomy_level (t_m_path_keys);
