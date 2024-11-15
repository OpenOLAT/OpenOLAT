-- Assessment
alter table o_as_entry add column a_obligation_mod_node_ident varchar(64);

-- Task
alter table o_gta_task add column g_peerreview_completed_date timestamp;
alter table o_gta_task add column g_peerreview_completed_drole varchar(16);
