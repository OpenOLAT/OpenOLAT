-- Assessment mode
alter table o_as_mode_course add a_safeexambrowserconfig_xml clob;
alter table o_as_mode_course add a_safeexambrowserconfig_plist clob;
alter table o_as_mode_course add a_safeexambrowserconfig_pkey varchar(255);
alter table o_as_mode_course add a_safeexambrowserconfig_dload number default 1 not null;

-- VFS metadata
alter table o_vfs_metadata add f_expiration_date date default null;
create index f_exp_date_idx on o_vfs_metadata (f_expiration_date);

-- Task
alter table o_ex_task add e_progress decimal default null;
alter table o_ex_task add e_checkpoint varchar(255) default null;
