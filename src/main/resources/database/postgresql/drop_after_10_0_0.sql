-- drop columns
alter table o_repositoryentry drop column fk_ownergroup;
alter table o_repositoryentry drop column fk_tutorgroup;
alter table o_repositoryentry drop column fk_participantgroup;
alter table o_repositoryentry drop column launchcounter;
alter table o_repositoryentry drop column downloadcounter;
alter table o_repositoryentry drop column lastusage;

alter table o_gp_business drop column fk_ownergroup;
alter table o_gp_business drop column fk_partipiciantgroup;
alter table o_gp_business drop column fk_waitinggroup;
alter table o_gp_business drop column groupcontext_fk;
alter table o_gp_business drop column businessgrouptype;

alter table o_area drop groupcontext_fk;

alter table o_bs_invitation drop column fk_secgroup;
alter table o_bs_invitation drop column version;

alter table o_ep_struct_el drop column fk_ownergroup;

-- drop tables
drop table o_gp_bgcontext;
drop table o_gp_business_to_resource;
drop table o_gp_bgcontextresource_rel;

drop table o_repositorymetadata;

drop table o_bookmark;