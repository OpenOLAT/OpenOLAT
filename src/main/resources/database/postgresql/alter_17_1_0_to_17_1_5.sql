
-- Assessment tool
create index idx_as_entry_subident_idx on o_as_entry(a_subident, fk_entry, fk_identity);
create index idx_as_entry_re_status_idx on o_as_entry(fk_entry, a_status);
