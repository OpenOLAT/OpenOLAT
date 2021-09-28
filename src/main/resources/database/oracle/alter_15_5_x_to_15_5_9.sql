-- LTI 1.3
alter table o_lti_key modify (l_key_id null);

alter table o_lti_tool add constraint idx_tool_client_id_idx unique (l_client_id);

