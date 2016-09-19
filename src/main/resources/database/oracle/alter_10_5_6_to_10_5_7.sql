create index idx_struct_to_group_group_ctx on o_ep_struct_to_group (fk_group_id);
create index idx_struct_to_group_re_ctx on o_ep_struct_to_group (fk_struct_id);

create index idx_tag_to_resid_idx on o_tag (resid);