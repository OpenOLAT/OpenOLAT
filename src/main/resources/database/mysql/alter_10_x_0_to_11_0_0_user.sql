alter table o_user add column u_firstname varchar(255);
alter table o_user add column u_lastname varchar(255);
alter table o_user add column u_email varchar(255);
alter table o_user add column u_birthday varchar(255);
alter table o_user add column u_graduation varchar(255);
alter table o_user add column u_gender varchar(255);
alter table o_user add column u_telprivate varchar(255);
alter table o_user add column u_telmobile varchar(255);
alter table o_user add column u_teloffice varchar(255);
alter table o_user add column u_skype varchar(255);
alter table o_user add column u_msn varchar(255);
alter table o_user add column u_xing varchar(255);
alter table o_user add column u_icq varchar(255);
alter table o_user add column u_homepage varchar(255);
alter table o_user add column u_street varchar(255);
alter table o_user add column u_extendedaddress varchar(255);
alter table o_user add column u_pobox varchar(255);
alter table o_user add column u_zipcode varchar(255);
alter table o_user add column u_region varchar(255);
alter table o_user add column u_city varchar(255);
alter table o_user add column u_country varchar(255);
alter table o_user add column u_countrycode varchar(255);
alter table o_user add column u_institutionalname varchar(255);
alter table o_user add column u_institutionaluseridentifier varchar(255);
alter table o_user add column u_institutionalemail varchar(255);
alter table o_user add column u_orgunit varchar(255);
alter table o_user add column u_studysubject varchar(255);
alter table o_user add column u_emchangekey varchar(255);
alter table o_user add column u_emaildisabled varchar(255);
alter table o_user add column u_typeofuser varchar(255);
alter table o_user add column u_socialsecuritynumber varchar(255);

alter table o_user add column u_rank varchar(255);
alter table o_user add column u_degree varchar(255);
alter table o_user add column u_position varchar(255);
alter table o_user add column u_userinterests varchar(255);
alter table o_user add column u_usersearchedinterests varchar(255);
alter table o_user add column u_officestreet varchar(255);
alter table o_user add column u_extendedofficeaddress varchar(255);
alter table o_user add column u_officepobox varchar(255);
alter table o_user add column u_officezipcode varchar(255);
alter table o_user add column u_officecity varchar(255);
alter table o_user add column u_officecountry varchar(255);
alter table o_user add column u_officemobilephone varchar(255);
alter table o_user add column u_department varchar(255);
alter table o_user add column u_privateemail varchar(255);
alter table o_user add column u_employeenumber varchar(255);
alter table o_user add column u_organizationalunit varchar(255);

alter table o_user add column u_edupersonaffiliation varchar(255);
alter table o_user add column u_swissedupersonstaffcategory varchar(255);
alter table o_user add column u_swissedupersonhomeorg varchar(255);
alter table o_user add column u_swissedupersonstudylevel varchar(255);
alter table o_user add column u_swissedupersonhomeorgtype varchar(255);
alter table o_user add column u_swissedupersonstudybranch1 varchar(255);
alter table o_user add column u_swissedupersonstudybranch2 varchar(255);
alter table o_user add column u_swissedupersonstudybranch3 varchar(255);

alter table o_user add column u_genericselectionproperty varchar(255);
alter table o_user add column u_genericselectionproperty2 varchar(255);
alter table o_user add column u_genericselectionproperty3 varchar(255);
alter table o_user add column u_generictextproperty varchar(255);
alter table o_user add column u_generictextproperty2 varchar(255);
alter table o_user add column u_generictextproperty3 varchar(255);
alter table o_user add column u_generictextproperty4 varchar(255);
alter table o_user add column u_generictextproperty5 varchar(255);
alter table o_user add column u_genericuniquetextproperty varchar(255);
alter table o_user add column u_genericuniquetextproperty2 varchar(255);
alter table o_user add column u_genericuniquetextproperty3 varchar(255);
alter table o_user add column u_genericemailproperty1 varchar(255);
alter table o_user add column u_genericcheckboxproperty varchar(255);
alter table o_user add column u_genericcheckboxproperty2 varchar(255);
alter table o_user add column u_genericcheckboxproperty3 varchar(255);

create index idx_user_firstname_idx on o_user (u_firstname);
create index idx_user_lastname_idx on o_user (u_lastname);
create index idx_user_email_idx on o_user (u_email);
create index idx_user_instname_idx on o_user (u_institutionalname);
create index idx_user_instid_idx on o_user (u_institutionaluseridentifier);
create index idx_user_instemail_idx on o_user (u_institutionalemail);
create index idx_user_creationdate_idx on o_user (creationdate);

update o_user set u_firstname=(select propvalue from o_userproperty where user_id=fk_user_id and propname='firstName') where u_firstname is null;
update o_user set u_lastname=(select propvalue from o_userproperty where user_id=fk_user_id and propname='lastName') where u_lastname is null;
update o_user set u_email=(select propvalue from o_userproperty where user_id=fk_user_id and propname='email') where u_email is null;
update o_user set u_birthday=(select propvalue from o_userproperty where user_id=fk_user_id and propname='birthDay') where u_birthday is null;
update o_user set u_graduation=(select propvalue from o_userproperty where user_id=fk_user_id and propname='graduation') where u_graduation is null;
update o_user set u_gender=(select propvalue from o_userproperty where user_id=fk_user_id and propname='gender') where u_gender is null;
update o_user set u_telprivate=(select propvalue from o_userproperty where user_id=fk_user_id and propname='telPrivate') where u_telprivate is null;
update o_user set u_telmobile=(select propvalue from o_userproperty where user_id=fk_user_id and propname='telMobile') where u_telmobile is null;
update o_user set u_teloffice=(select propvalue from o_userproperty where user_id=fk_user_id and propname='telOffice') where u_teloffice is null;
update o_user set u_skype=(select propvalue from o_userproperty where user_id=fk_user_id and propname='skype') where u_skype is null;
update o_user set u_msn=(select propvalue from o_userproperty where user_id=fk_user_id and propname='msn') where u_msn is null;
update o_user set u_xing=(select propvalue from o_userproperty where user_id=fk_user_id and propname='xing') where u_xing is null;
update o_user set u_icq=(select propvalue from o_userproperty where user_id=fk_user_id and propname='icq') where u_icq is null;
update o_user set u_homepage=(select propvalue from o_userproperty where user_id=fk_user_id and propname='homepage') where u_homepage is null;
update o_user set u_street=(select propvalue from o_userproperty where user_id=fk_user_id and propname='street') where u_street is null;
update o_user set u_extendedaddress=(select propvalue from o_userproperty where user_id=fk_user_id and propname='extendedAddress') where u_extendedaddress is null;
update o_user set u_pobox=(select propvalue from o_userproperty where user_id=fk_user_id and propname='poBox') where u_pobox is null;
update o_user set u_zipcode=(select propvalue from o_userproperty where user_id=fk_user_id and propname='zipCode') where u_zipcode is null;
update o_user set u_region=(select propvalue from o_userproperty where user_id=fk_user_id and propname='region') where u_region is null;
update o_user set u_city=(select propvalue from o_userproperty where user_id=fk_user_id and propname='city') where u_city is null;
update o_user set u_country=(select propvalue from o_userproperty where user_id=fk_user_id and propname='country') where u_country is null;
update o_user set u_countrycode=(select propvalue from o_userproperty where user_id=fk_user_id and propname='countryCode') where u_countrycode is null;
update o_user set u_institutionalname=(select propvalue from o_userproperty where user_id=fk_user_id and propname='institutionalName') where u_institutionalname is null;
update o_user set u_institutionaluseridentifier=(select propvalue from o_userproperty where user_id=fk_user_id and propname='institutionalUserIdentifier') where u_institutionaluseridentifier is null;
update o_user set u_institutionalemail=(select propvalue from o_userproperty where user_id=fk_user_id and propname='institutionalEmail') where u_institutionalemail is null;
update o_user set u_orgunit=(select propvalue from o_userproperty where user_id=fk_user_id and propname='orgUnit') where u_orgunit is null;
update o_user set u_studysubject=(select propvalue from o_userproperty where user_id=fk_user_id and propname='studySubject') where u_studysubject is null;
update o_user set u_emchangekey=(select propvalue from o_userproperty where user_id=fk_user_id and propname='emchangeKey') where u_emchangekey is null;
update o_user set u_emaildisabled=(select propvalue from o_userproperty where user_id=fk_user_id and propname='emailDisabled') where u_emaildisabled is null;
update o_user set u_typeofuser=(select propvalue from o_userproperty where user_id=fk_user_id and propname='typeOfUser') where u_typeofuser is null;
update o_user set u_socialsecuritynumber=(select propvalue from o_userproperty where user_id=fk_user_id and propname='socialSecurityNumber') where u_socialsecuritynumber is null;
update o_user set u_genericselectionproperty=(select propvalue from o_userproperty where user_id=fk_user_id and propname='genericSelectionProperty') where u_genericselectionproperty is null;
update o_user set u_genericselectionproperty2=(select propvalue from o_userproperty where user_id=fk_user_id and propname='genericSelectionProperty2') where u_genericselectionproperty2 is null;
update o_user set u_genericselectionproperty3=(select propvalue from o_userproperty where user_id=fk_user_id and propname='genericSelectionProperty3') where u_genericselectionproperty3 is null;
update o_user set u_generictextproperty=(select propvalue from o_userproperty where user_id=fk_user_id and propname='genericTextProperty') where u_generictextproperty is null;
update o_user set u_generictextproperty2=(select propvalue from o_userproperty where user_id=fk_user_id and propname='genericTextProperty2') where u_generictextproperty2 is null;
update o_user set u_generictextproperty3=(select propvalue from o_userproperty where user_id=fk_user_id and propname='genericTextProperty3') where u_generictextproperty3 is null;
update o_user set u_generictextproperty4=(select propvalue from o_userproperty where user_id=fk_user_id and propname='genericTextProperty4') where u_generictextproperty4 is null;
update o_user set u_generictextproperty5=(select propvalue from o_userproperty where user_id=fk_user_id and propname='genericTextProperty5') where u_generictextproperty5 is null;
update o_user set u_genericuniquetextproperty=(select propvalue from o_userproperty where user_id=fk_user_id and propname='genericUniqueTextProperty') where u_genericuniquetextproperty is null;
update o_user set u_genericuniquetextproperty2=(select propvalue from o_userproperty where user_id=fk_user_id and propname='genericUniqueTextProperty2') where u_genericuniquetextproperty2 is null;
update o_user set u_genericuniquetextproperty3=(select propvalue from o_userproperty where user_id=fk_user_id and propname='genericUniqueTextProperty3') where u_genericuniquetextproperty3 is null;
update o_user set u_genericemailproperty1=(select propvalue from o_userproperty where user_id=fk_user_id and propname='genericEmailProperty1') where u_genericemailproperty1 is null;
update o_user set u_genericcheckboxproperty=(select propvalue from o_userproperty where user_id=fk_user_id and propname='genericCheckboxProperty') where u_genericcheckboxproperty is null;
update o_user set u_genericcheckboxproperty2=(select propvalue from o_userproperty where user_id=fk_user_id and propname='genericCheckboxProperty2') where u_genericcheckboxproperty2 is null;
update o_user set u_genericcheckboxproperty3=(select propvalue from o_userproperty where user_id=fk_user_id and propname='genericCheckboxProperty3') where u_genericcheckboxproperty3 is null;
update o_user set u_rank=(select propvalue from o_userproperty where user_id=fk_user_id and propname='rank') where u_rank is null;
update o_user set u_degree=(select propvalue from o_userproperty where user_id=fk_user_id and propname='degree') where u_degree is null;
update o_user set u_position=(select propvalue from o_userproperty where user_id=fk_user_id and propname='position') where u_position is null;
update o_user set u_userinterests=(select propvalue from o_userproperty where user_id=fk_user_id and propname='userInterests') where u_userinterests is null;
update o_user set u_usersearchedinterests=(select propvalue from o_userproperty where user_id=fk_user_id and propname='userSearchedInterests') where u_usersearchedinterests is null;
update o_user set u_officestreet=(select propvalue from o_userproperty where user_id=fk_user_id and propname='officeStreet') where u_officestreet is null;
update o_user set u_extendedofficeaddress=(select propvalue from o_userproperty where user_id=fk_user_id and propname='extendedOfficeAddress') where u_extendedofficeaddress is null;
update o_user set u_officepobox=(select propvalue from o_userproperty where user_id=fk_user_id and propname='officePoBox') where u_officepobox is null;
update o_user set u_officezipcode=(select propvalue from o_userproperty where user_id=fk_user_id and propname='officeZipCode') where u_officezipcode is null;
update o_user set u_officecity=(select propvalue from o_userproperty where user_id=fk_user_id and propname='officeCity') where u_officecity is null;
update o_user set u_officecountry=(select propvalue from o_userproperty where user_id=fk_user_id and propname='officeCountry') where u_officecountry is null;
update o_user set u_officemobilephone=(select propvalue from o_userproperty where user_id=fk_user_id and propname='officeMobilePhone') where u_officemobilephone is null;
update o_user set u_department=(select propvalue from o_userproperty where user_id=fk_user_id and propname='department') where u_department is null;
update o_user set u_privateemail=(select propvalue from o_userproperty where user_id=fk_user_id and propname='privateEmail') where u_privateemail is null;
update o_user set u_edupersonaffiliation=(select propvalue from o_userproperty where user_id=fk_user_id and propname='eduPersonAffiliation') where u_edupersonaffiliation is null;
update o_user set u_swissedupersonhomeorg=(select propvalue from o_userproperty where user_id=fk_user_id and propname='swissEduPersonHomeOrganization') where u_swissedupersonhomeorg is null;
update o_user set u_swissedupersonstudylevel=(select propvalue from o_userproperty where user_id=fk_user_id and propname='swissEduPersonStudyLevel') where u_swissedupersonstudylevel is null;
update o_user set u_swissedupersonhomeorgtype=(select propvalue from o_userproperty where user_id=fk_user_id and propname='swissEduPersonHomeOrganizationType') where u_swissedupersonhomeorgtype is null;
update o_user set u_employeenumber=(select propvalue from o_userproperty where user_id=fk_user_id and propname='employeeNumber') where u_employeenumber is null;
update o_user set u_swissedupersonstaffcategory=(select propvalue from o_userproperty where user_id=fk_user_id and propname='swissEduPersonStaffCategory') where u_swissedupersonstaffcategory is null;
update o_user set u_organizationalunit=(select propvalue from o_userproperty where user_id=fk_user_id and propname='organizationalUnit') where u_organizationalunit is null;
update o_user set u_swissedupersonstudybranch1=(select propvalue from o_userproperty where user_id=fk_user_id and propname='swissEduPersonStudyBranch1') where u_swissedupersonstudybranch1 is null;
update o_user set u_swissedupersonstudybranch2=(select propvalue from o_userproperty where user_id=fk_user_id and propname='swissEduPersonStudyBranch2') where u_swissedupersonstudybranch2 is null;
update o_user set u_swissedupersonstudybranch3=(select propvalue from o_userproperty where user_id=fk_user_id and propname='swissEduPersonStudyBranch3') where u_swissedupersonstudybranch3 is null;

alter table o_userproperty drop foreign key FK4B04D83FD1A80C95;

drop view o_bs_identity_short_v;
create view o_bs_identity_short_v as (
   select
      ident.id as id_id,
      ident.name as id_name,
      ident.lastlogin as id_lastlogin,
      ident.status as id_status,
      us.user_id as us_id,
      us.u_firstname as first_name,
      us.u_lastname as last_name,
      us.u_email as email
   from o_bs_identity as ident
   inner join o_user as us on (ident.fk_user_id = us.user_id)
);


drop view o_gp_contactext_v;
create view o_gp_contactext_v as (
   select
      bg_member.id as membership_id,
      bg_member.fk_identity_id as member_id,
      bg_member.g_role as membership_role,
      id_member.name as member_name,
      us_member.u_firstname as member_firstname,
      us_member.u_lastname as member_lastname,
      bg_me.fk_identity_id as me_id,
      bgroup.group_id as bg_id,
      bgroup.groupname as bg_name
   from o_gp_business as bgroup
   inner join o_bs_group_member as bg_member on (bg_member.fk_group_id = bgroup.fk_group_id)
   inner join o_bs_identity as id_member on (bg_member.fk_identity_id = id_member.id)
   inner join o_user as us_member on (id_member.fk_user_id = us_member.user_id)
   inner join o_bs_group_member as bg_me on (bg_me.fk_group_id = bgroup.fk_group_id)
   where
      (bgroup.ownersintern=true and bg_member.g_role='coach')
      or
      (bgroup.participantsintern=true and bg_member.g_role='participant')
);
