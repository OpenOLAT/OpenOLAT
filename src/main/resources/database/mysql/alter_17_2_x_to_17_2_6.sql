create index idx_re_audit_log_to_re_entry_idx on o_repositoryentry_audit_log (fk_entry);

-- clear old malicious logs
delete from o_repositoryentry_audit_log;