alter table o_bs_identity add deleteddate date;
alter table o_bs_identity add deletedroles varchar(1024);
alter table o_bs_identity add deletedby varchar(128);


alter table o_loggingtable drop (username, userproperty1, userproperty2, userproperty3, userproperty4, userproperty5, userproperty6, userproperty7, userproperty8, userproperty9, userproperty10, userproperty11, userproperty12);

update o_bs_identity set name=id where status=199;

update o_user set u_firstname=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_firstname=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_lastname=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_email=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_birthday=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_graduation=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_gender=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_telprivate=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_telmobile=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_teloffice=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_skype=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_msn=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_xing=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_icq=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_homepage=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_street=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_extendedaddress=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_pobox=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_zipcode=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_region=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_city=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_country=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_countrycode=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_institutionalname=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_institutionaluseridentifier=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_institutionalemail=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_orgunit=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_studysubject=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_emchangekey=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_emaildisabled=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_typeofuser=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_socialsecuritynumber=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_genericselectionproperty=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_genericselectionproperty2=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_genericselectionproperty3=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_generictextproperty=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_generictextproperty2=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_generictextproperty3=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_generictextproperty4=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_generictextproperty5=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_genericuniquetextproperty=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_genericuniquetextproperty2=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_genericuniquetextproperty3=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_genericemailproperty1=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_genericcheckboxproperty=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_genericcheckboxproperty2=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_genericcheckboxproperty3=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_rank=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_degree=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_position=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_userinterests=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_usersearchedinterests=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_officestreet=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_extendedofficeaddress=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_officepobox=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_officezipcode=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_officecity=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_officecountry=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_officemobilephone=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_department=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_privateemail=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_edupersonaffiliation=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_swissedupersonhomeorg=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_swissedupersonstudylevel=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_swissedupersonhomeorgtype=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_employeenumber=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_swissedupersonstaffcategory=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_organizationalunit=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_swissedupersonstudybranch1=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_swissedupersonstudybranch2=null where exists (select id from o_bs_identity where id=fk_identity and status=199);
update o_user set u_swissedupersonstudybranch3=null where exists (select id from o_bs_identity where id=fk_identity and status=199);

drop table o_stat_homeorg;
drop table o_stat_orgtype;
drop table o_stat_studylevel;
drop table o_stat_studybranch3;

-- user data export
create table o_user_data_export (
  id number(20) generated always as identity,
   creationdate date,
   lastmodified date,
   u_directory varchar(255),
   u_status varchar(16),
   u_export_ids varchar(2000),
   fk_identity number(20) not null,
   fk_request_by number(20),
   primary key (id)
);

alter table o_user_data_export add constraint usr_dataex_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_usr_dataex_to_ident_idx on o_user_data_export (fk_identity);
alter table o_user_data_export add constraint usr_dataex_to_requ_idx foreign key (fk_request_by) references o_bs_identity (id);
create index idx_usr_dataex_to_requ_idx on o_user_data_export (fk_request_by);



