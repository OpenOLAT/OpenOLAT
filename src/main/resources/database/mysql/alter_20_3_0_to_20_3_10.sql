-- Assessment mode
alter table o_as_mode_course add column a_safeexambrowserconfig_file varchar(255);
alter table o_as_inspection_configuration add column a_safeexambrowserconfig_file varchar(255);
