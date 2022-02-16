-- Assessment mode
alter table o_as_mode_course add column a_safeexambrowserconfig_xml text;
alter table o_as_mode_course add column a_safeexambrowserconfig_plist text;
alter table o_as_mode_course add column a_safeexambrowserconfig_pkey varchar(255);
alter table o_as_mode_course add column a_safeexambrowserconfig_dload bool default true not null;

-- VFS metadata
alter table o_vfs_metadata add column f_expiration_date timestamp default null;
create index f_exp_date_idx on o_vfs_metadata (f_expiration_date);

-- Task
alter table o_ex_task add column e_progress decimal default null;
alter table o_ex_task add column e_checkpoint varchar(255) default null;
