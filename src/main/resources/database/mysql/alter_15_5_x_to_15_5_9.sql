-- LTI 1.3
alter table o_lti_key modify column l_key_id varchar(255);

alter table o_lti_tool add constraint idx_tool_client_id_idx UNIQUE (l_client_id);


