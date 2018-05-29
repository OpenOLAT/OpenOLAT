alter table o_bs_identity add column deleteddate timestamp;
alter table o_bs_identity add column deletedroles varchar(1024);
alter table o_bs_identity add column deletedby varchar(128);


alter table o_loggingtable drop column username, drop column userproperty1, drop column userproperty2, drop column userproperty3, drop column userproperty4, drop column userproperty5, drop column userproperty6, drop column userproperty7, drop column userproperty8, drop column userproperty9, drop column userproperty10, drop column userproperty11, drop column userproperty12;

update o_bs_identity set name=id where status=199;

update o_user set u_firstname=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_lastname=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_email=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_birthday=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_graduation=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_gender=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_telprivate=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_telmobile=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_teloffice=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_skype=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_msn=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_xing=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_icq=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_homepage=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_street=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_extendedaddress=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_pobox=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_zipcode=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_region=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_city=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_country=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_countrycode=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_institutionalname=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_institutionaluseridentifier=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_institutionalemail=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_orgunit=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_studysubject=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_emchangekey=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_emaildisabled=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_typeofuser=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_socialsecuritynumber=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_genericselectionproperty=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_genericselectionproperty2=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_genericselectionproperty3=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_generictextproperty=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_generictextproperty2=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_generictextproperty3=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_generictextproperty4=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_generictextproperty5=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_genericuniquetextproperty=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_genericuniquetextproperty2=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_genericuniquetextproperty3=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_genericemailproperty1=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_genericcheckboxproperty=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_genericcheckboxproperty2=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_genericcheckboxproperty3=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_rank=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_degree=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_position=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_userinterests=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_usersearchedinterests=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_officestreet=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_extendedofficeaddress=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_officepobox=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_officezipcode=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_officecity=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_officecountry=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_officemobilephone=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_department=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_privateemail=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_edupersonaffiliation=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_swissedupersonhomeorg=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_swissedupersonstudylevel=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_swissedupersonhomeorgtype=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_employeenumber=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_swissedupersonstaffcategory=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_organizationalunit=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_swissedupersonstudybranch1=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_swissedupersonstudybranch2=null from o_bs_identity where id=fk_identity and status=199;
update o_user set u_swissedupersonstudybranch3=null from o_bs_identity where id=fk_identity and status=199;

drop table o_stat_homeorg;
drop table o_stat_orgtype;
drop table o_stat_studylevel;
drop table o_stat_studybranch3;

-- user data export
create table o_user_data_export (
   id bigserial,
   creationdate timestamp,
   lastmodified timestamp,
   u_directory varchar(255),
   u_status varchar(16),
   u_export_ids varchar(2000),
   fk_identity int8 not null,
   fk_request_by int8,
   primary key (id)
);

alter table o_user_data_export add constraint usr_dataex_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_usr_dataex_to_ident_idx on o_user_data_export (fk_identity);
alter table o_user_data_export add constraint usr_dataex_to_requ_idx foreign key (fk_request_by) references o_bs_identity (id);
create index idx_usr_dataex_to_requ_idx on o_user_data_export (fk_request_by);



