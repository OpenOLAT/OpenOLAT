-- Assessment mode
alter table o_as_mode_course add column a_safeexambrowserconfig_xml mediumtext;
alter table o_as_mode_course add column a_safeexambrowserconfig_plist mediumtext;
alter table o_as_mode_course add column a_safeexambrowserconfig_pkey varchar(255);
alter table o_as_mode_course add column a_safeexambrowserconfig_dload bool not null default true;
