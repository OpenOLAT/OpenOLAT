-- Coaching
create index idx_as_entry_root_cov_idx on o_as_entry (fk_entry, fk_identity) include (a_subident, a_passed, a_completion) where a_entry_root=true;
drop index idx_as_entry_root_fk_idx;
