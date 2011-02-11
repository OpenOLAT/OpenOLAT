-- Schema Audit changes
-- WARNING: on a large production database these changes
-- may take some time to complete!

-- version type should be "mediumint unsigned not null" though in many
-- cases a smaller type suffices we retain this for consistency
alter table o_bookmark modify version mediumint unsigned not null;
alter table o_bs_authentication modify version mediumint unsigned not null;
alter table o_bs_identity modify version mediumint unsigned not null;
alter table o_bs_membership modify version mediumint unsigned not null;
alter table o_bs_namedgroup modify version mediumint unsigned not null;
alter table o_bs_policy modify version mediumint unsigned not null;
alter table o_bs_secgroup modify version mediumint unsigned not null;
alter table o_catentry modify version mediumint unsigned not null;
alter table o_checklist modify version mediumint unsigned not null;
alter table o_checkpoint modify version mediumint unsigned not null;
alter table o_checkpoint_results modify version mediumint unsigned not null;
alter table o_forum modify version mediumint unsigned not null;
alter table o_gp_bgarea modify version mediumint unsigned not null;
alter table o_gp_bgcontext modify version mediumint unsigned not null;
alter table o_gp_bgcontextresource_rel modify version mediumint unsigned not null;
alter table o_gp_bgtoarea_rel modify version mediumint unsigned not null;
alter table o_gp_business modify version mediumint unsigned not null;
alter table o_lifecycle modify version mediumint unsigned not null;
alter table o_message modify version mediumint unsigned not null;
alter table o_note modify version mediumint unsigned not null;
alter table o_noti_pub modify version mediumint unsigned not null;
alter table o_noti_sub modify version mediumint unsigned not null;
alter table o_olatresource modify version mediumint unsigned not null;
alter table o_plock modify version mediumint unsigned not null;
alter table o_projectbroker modify version mediumint unsigned not null;
alter table o_projectbroker_project modify version mediumint unsigned not null;
alter table o_property modify version mediumint unsigned not null;
alter table o_qtiresult modify version mediumint unsigned not null;
alter table o_qtiresultset modify version mediumint unsigned not null;
alter table o_readmessage modify version mediumint unsigned not null;
alter table o_references modify version mediumint unsigned not null;
alter table o_repositoryentry modify version mediumint unsigned not null;
alter table o_repositorymetadata modify version mediumint unsigned not null;
alter table o_temporarykey modify version mediumint unsigned not null;
alter table o_user modify version mediumint unsigned not null;
alter table o_usercomment modify version mediumint unsigned not null;
alter table o_userrating modify version mediumint unsigned not null;
alter table oc_lock modify version mediumint unsigned not null;

-- many "id" types are poorly dimensioned - these changes apply to
-- those that use "id" as an identifier, the rest have ID columns
-- that must be renamed (requires Hibernate coordination)
alter table o_bs_authentication modify id bigint unsigned not null;
-- alter table o_bs_identity modify id bigint unsigned not null;
alter table o_bs_membership modify id bigint unsigned not null;
alter table o_bs_namedgroup modify id bigint unsigned not null;
alter table o_bs_policy modify id bigint unsigned not null;
-- alter table o_bs_secgroup modify id bigint unsigned not null;
-- alter table o_catentry modify id bigint unsigned not null;
alter table o_lifecycle modify id bigint unsigned not null;
alter table o_property modify id bigint unsigned not null;
alter table o_readmessage modify id bigint unsigned not null;
alter table o_stat_daily modify id bigint unsigned not null auto_increment;
alter table o_stat_dayofweek modify id bigint unsigned not null auto_increment;
alter table o_stat_homeorg modify id bigint unsigned not null auto_increment;
alter table o_stat_hourofday modify id bigint unsigned not null auto_increment;
alter table o_stat_orgtype modify id bigint unsigned not null auto_increment;
alter table o_stat_studybranch3 modify id bigint unsigned not null auto_increment;
alter table o_stat_studylevel modify id bigint unsigned not null auto_increment;
alter table o_stat_weekly modify id bigint unsigned not null auto_increment;

-- a few "text" types are incorrectly dimensioned
alter table o_bookmark modify description tinytext not null;
alter table o_gp_bgarea modify descr tinytext;
alter table o_gp_bgcontext modify descr tinytext;
alter table o_noti_pub modify data tinytext;
alter table o_property modify textvalue mediumtext;
alter table o_loggingtable modify targetresname mediumtext;

-- the following are redundant indexes
drop index ocl_asset_idx on oc_lock;
drop index provider_idx on o_bs_authentication;
drop index FKFF94111CD1A80C95 on o_bs_identity;
drop index name_idx on o_bs_identity;
drop index FK7B6288B4B85B522C on o_bs_membership;
drop index groupname_idx on o_bs_namedgroup;
drop index FK9A1C5109F9C3F1D on o_bs_policy;
drop index FKF4433C2CA1FAC766 on o_catentry;
drop index FK1C154FC47E4A0638 on o_gp_bgcontext;
drop index FK9B663F2D1E2E7685 on o_gp_bgtoarea_rel;
drop index FKCEEB8A86A1FAC766 on o_gp_business;
drop index FKCEEB8A86C06E3EF3 on o_gp_business;
drop index owner_idx on o_note;
drop index FK4FB8F04749E53702 on o_noti_sub;
drop index name_idx on o_olatresource;
drop index asset_idx on o_plock;
drop index o_projectbroker_customfields_idx on o_projectbroker_customfields;
drop index projectbroker_project_id_idx on o_projectbroker_project;
drop index FK2F9C439888C31018 on o_repositoryentry;
drop index FK2F9C4398A1FAC766 on o_repositoryentry;
drop index softkey_idx on o_repositoryentry;
drop index FKDB97A6493F14E3EE on o_repositorymetadata;
drop index FK4B04D83FD1A80C95 on o_userproperty;


create table if not exists o_mark (
  mark_id bigint not null,
  version mediumint unsigned not null,
  creationdate datetime,
  resname varchar(50) not null,
  resid bigint not null,
  ressubpath varchar(2048),
  businesspath varchar(2048),
  creator_id bigint not null,
  primary key (mark_id)
);

create index mark_id_idx on o_mark(resid);
create index mark_name_idx on o_mark(resname);
create index mark_subpath_idx on o_mark(ressubpath);
create index mark_businesspath_idx on o_mark(businesspath);
create index FKF26C8375236F21X on o_mark(creator_id);
alter table o_mark add constraint FKF26C8375236F21X foreign key (creator_id) references o_bs_identity (id);

-- indexes for the o_stat tables - see OLAT-5163 --
create index stathor_bp_idx   on o_stat_homeorg (businesspath);
create index stathor_ho_idx   on o_stat_homeorg (homeorg);
create index statorg_bp_idx   on o_stat_orgtype (businesspath);
create index statorg_org_idx  on o_stat_orgtype (orgtype);
create index statday_bp_idx   on o_stat_daily (businesspath);
create index statday_day_idx  on o_stat_daily (day);
create index statwee_bp_idx   on o_stat_weekly (businesspath);
create index statwee_week_idx on o_stat_weekly (week);
create index statdow_bp_idx   on o_stat_dayofweek (businesspath);
create index statdow_day_idx  on o_stat_dayofweek (day);
create index stathod_bp_idx   on o_stat_hourofday (businesspath);
create index stathod_hour_idx on o_stat_hourofday (hour);
create index statstl_stl_idx  on o_stat_studylevel (studylevel);
create index statstl_bp_idx   on o_stat_studylevel (businesspath);

-- harmonisation with hsqldb datatypes
alter table o_property modify floatvalue float(65,30);
alter table o_qtiresultset modify score float(65,30);
alter table o_qtiresult modify score float(65,30);

-- locale can be longer than 10 chars (varian can be longer: de_CH_Zuerich)
alter table o_user modify language varchar(30);

-- Redeploy Help-course
delete  from o_property where name='deployedCourses' and stringvalue='help/OLAT Hilfe.zip';
