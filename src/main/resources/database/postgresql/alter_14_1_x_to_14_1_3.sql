drop index f_m_rel_path_idx;
create index f_m_rel_path_idx on o_vfs_metadata (f_relative_path varchar_pattern_ops);