-- Safe Exam Browser
alter table o_as_seb_template add a_type varchar(16) default 'OO_FORM' not null;
alter table o_as_seb_template add a_safeexambrowserauthorhint CLOB;
alter table o_as_seb_template add a_download number default 0;
alter table o_as_seb_template add a_exit_password varchar(255);
alter table o_as_seb_template add a_allow_exit number default 0;
alter table o_as_seb_template add a_config_filename varchar(255);

alter table o_as_mode_course add a_safeexambrowser_exit_password varchar(255);
alter table o_as_mode_course add a_safeexambrowser_allow_exit number default 0;

alter table o_as_inspection_configuration add a_safeexambrowser_exit_password varchar(255);
alter table o_as_inspection_configuration add a_safeexambrowser_allow_exit number default 0;