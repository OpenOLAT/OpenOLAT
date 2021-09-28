-- LTI 1.3
alter table o_lti_key alter column l_key_id drop not null;

create unique index idx_tool_client_id_idx on o_lti_tool(l_client_id);
