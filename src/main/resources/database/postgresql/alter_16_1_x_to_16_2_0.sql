-- Assessment mode
alter table o_as_mode_course add column a_safeexambrowserconfig_xml text;
alter table o_as_mode_course add column a_safeexambrowserconfig_plist text;
alter table o_as_mode_course add column a_safeexambrowserconfig_pkey varchar(255);
alter table o_as_mode_course add column a_safeexambrowserconfig_dload bool default true not null;
