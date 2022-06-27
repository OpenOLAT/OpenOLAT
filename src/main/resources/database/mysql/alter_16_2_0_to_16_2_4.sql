-- Assessment mode
alter table o_as_mode_course add column a_external_id varchar(64);
alter table o_as_mode_course add column a_managed_flags varchar(255);