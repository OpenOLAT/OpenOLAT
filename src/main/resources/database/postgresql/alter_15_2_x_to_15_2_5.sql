-- Coaching tool
create index idx_as_entry_root_id_idx on o_as_entry (id) where a_entry_root=true;
create index idx_as_entry_root_fk_idx on o_as_entry (fk_entry, fk_identity) where a_entry_root=true;
