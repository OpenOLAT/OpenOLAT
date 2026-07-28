-- Safe Exam Browser
alter table o_as_seb_template add column a_type varchar(16) default 'OO_FORM' not null;
alter table o_as_seb_template add column a_safeexambrowserauthorhint mediumtext;
alter table o_as_seb_template add column a_download bool;
alter table o_as_seb_template add column a_exit_password varchar(255);
alter table o_as_seb_template add column a_allow_exit bool;
alter table o_as_seb_template add column a_config_filename varchar(255);

alter table o_as_mode_course add column a_safeexambrowser_exit_password varchar(255);
alter table o_as_mode_course add column a_safeexambrowser_allow_exit bool;

alter table o_as_inspection_configuration add column a_safeexambrowser_exit_password varchar(255);
alter table o_as_inspection_configuration add column a_safeexambrowser_allow_exit bool;
