-- Users
alter table o_user add column u_portrait_path varchar(32) default null;
alter table o_user add column u_logo_path varchar(32) default null;
alter table o_user add column u_initials_css_class varchar(32) default null;
