
create table o_mark (
  mark_id int8 not null,
  version int4 not null,
  creationdate timestamp,
  resname varchar(50) not null,
  resid int8 not null,
  ressubpath varchar(2048),
  businesspath varchar(2048),
  creator_id int8 not null,
  primary key (mark_id)
);

create index mark_id_idx on o_mark(resid);
create index mark_name_idx on o_mark(resname);
create index mark_subpath_idx on o_mark(ressubpath);
create index mark_businesspath_idx on o_mark(businesspath);
create index FKF26C8375236F21X on o_mark(creator_id);
alter table o_mark add constraint FKF26C8375236F21X foreign key (creator_id) references o_bs_identity (id);

-- locale can be longer than 10 chars (varian can be longer: de_CH_Zuerich)
alter table o_user alter language type varchar(30);

-- update numofwords and numofcharacters from int8 to int4
alter table o_message alter numofwords type int4;
alter table o_message alter numofcharacters type int4;

-- update rating from int8 to int4
alter table o_userrating alter rating type int4;

-- harmonisation with hsqldb datatypes
alter table o_property alter floatvalue type float(24);
alter table o_qtiresultset alter score type float(24);
alter table o_qtiresult alter score type float(24);

-- a few "text" types are incorrectly dimensioned
alter table o_bookmark alter description type text , alter column description set not null;
alter table o_gp_bgarea alter descr type text;
alter table o_gp_bgcontext alter descr type text;
alter table o_noti_pub alter data type text;
alter table o_property alter textvalue type text;
alter table o_loggingtable alter targetresname type text;

-- update version from int8 to int4
alter table o_forum alter version type int4;
alter table o_property alter version type int4;
alter table o_bs_secgroup alter version type int4;
alter table o_gp_business alter version type int4;
alter table o_temporarykey alter version type int4;
alter table o_bs_authentication alter version type int4;
alter table o_noti_pub alter version type int4;
alter table o_qtiresultset alter version type int4;
alter table o_bs_identity alter version type int4;
alter table o_olatresource alter version type int4;
alter table o_bs_namedgroup alter version type int4;
alter table o_catentry alter version type int4;
alter table o_note alter version type int4;
alter table o_gp_bgcontext alter version type int4;
alter table o_references alter version type int4;
alter table o_repositorymetadata alter version type int4;
alter table o_user alter version type int4;
alter table o_gp_bgcontextresource_rel alter version type int4;
alter table o_message alter version type int4;
alter table o_gp_bgtoarea_rel alter version type int4;
alter table o_noti_sub alter version type int4;
alter table o_qtiresult alter version type int4;
alter table o_bs_policy alter version type int4;
alter table o_gp_bgarea alter version type int4;
alter table o_repositoryentry alter version type int4;
alter table o_bookmark alter version type int4;
alter table o_bs_membership alter version type int4;
alter table o_plock alter version type int4;
alter table o_lifecycle alter version type int4;
alter table o_readmessage alter version type int4;
alter table oc_lock alter version type int4;
alter table o_checklist alter version type int4;
alter table o_checkpoint alter version type int4;
alter table o_checkpoint_results alter version type int4;
alter table o_projectbroker alter version type int4;
alter table o_projectbroker_project alter version type int4;
alter table o_usercomment alter version type int4;
alter table o_userrating alter version type int4;

-- Redeploy Help-course
delete from o_property where name='deployedCourses' and stringvalue='help/OLAT Hilfe.zip';
