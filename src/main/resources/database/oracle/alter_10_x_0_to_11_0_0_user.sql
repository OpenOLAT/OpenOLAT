alter table o_user add u_firstname varchar2(255 char);
alter table o_user add u_lastname varchar2(255 char);
alter table o_user add u_email varchar2(255 char);
alter table o_user add u_birthday varchar2(255 char);
alter table o_user add u_graduation varchar2(255 char);
alter table o_user add u_gender varchar2(255 char);
alter table o_user add u_telprivate varchar2(255 char);
alter table o_user add u_telmobile varchar2(255 char);
alter table o_user add u_teloffice varchar2(255 char);
alter table o_user add u_skype varchar2(255 char);
alter table o_user add u_msn varchar2(255 char);
alter table o_user add u_xing varchar2(255 char);
alter table o_user add u_icq varchar2(255 char);
alter table o_user add u_homepage varchar2(255 char);
alter table o_user add u_street varchar2(255 char);
alter table o_user add u_extendedaddress varchar2(255 char);
alter table o_user add u_pobox varchar2(255 char);
alter table o_user add u_zipcode varchar2(255 char);
alter table o_user add u_region varchar2(255 char);
alter table o_user add u_city varchar2(255 char);
alter table o_user add u_country varchar2(255 char);
alter table o_user add u_countrycode varchar2(255 char);
alter table o_user add u_institutionalname varchar2(255 char);
alter table o_user add u_institutionaluseridentifier varchar2(255 char);
alter table o_user add u_institutionalemail varchar2(255 char);
alter table o_user add u_orgunit varchar2(255 char);
alter table o_user add u_studysubject varchar2(255 char);
alter table o_user add u_emchangekey varchar2(255 char);
alter table o_user add u_emaildisabled varchar2(255 char);
alter table o_user add u_typeofuser varchar2(255 char);
alter table o_user add u_socialsecuritynumber varchar2(255 char);

alter table o_user add u_rank varchar2(255 char);
alter table o_user add u_degree varchar2(255 char);
alter table o_user add u_position varchar2(255 char);
alter table o_user add u_userinterests varchar2(255 char);
alter table o_user add u_usersearchedinterests varchar2(255 char);
alter table o_user add u_officestreet varchar2(255 char);
alter table o_user add u_extendedofficeaddress varchar2(255 char);
alter table o_user add u_officepobox varchar2(255 char);
alter table o_user add u_officezipcode varchar2(255 char);
alter table o_user add u_officecity varchar2(255 char);
alter table o_user add u_officecountry varchar2(255 char);
alter table o_user add u_officemobilephone varchar2(255 char);
alter table o_user add u_department varchar2(255 char);
alter table o_user add u_privateemail varchar2(255 char);
alter table o_user add u_employeenumber varchar2(255 char);
alter table o_user add u_organizationalunit varchar2(255 char);

alter table o_user add u_edupersonaffiliation varchar2(255 char);
alter table o_user add u_swissedupersonstaffcategory varchar2(255 char);
alter table o_user add u_swissedupersonhomeorg varchar2(255 char);
alter table o_user add u_swissedupersonstudylevel varchar2(255 char);
alter table o_user add u_swissedupersonhomeorgtype varchar2(255 char);
alter table o_user add u_swissedupersonstudybranch1 varchar2(255 char);
alter table o_user add u_swissedupersonstudybranch2 varchar2(255 char);
alter table o_user add u_swissedupersonstudybranch3 varchar2(255 char);

alter table o_user add u_genericselectionproperty varchar2(255 char);
alter table o_user add u_genericselectionproperty2 varchar2(255 char);
alter table o_user add u_genericselectionproperty3 varchar2(255 char);
alter table o_user add u_generictextproperty varchar2(255 char);
alter table o_user add u_generictextproperty2 varchar2(255 char);
alter table o_user add u_generictextproperty3 varchar2(255 char);
alter table o_user add u_generictextproperty4 varchar2(255 char);
alter table o_user add u_generictextproperty5 varchar2(255 char);
alter table o_user add u_genericuniquetextproperty varchar2(255 char);
alter table o_user add u_genericuniquetextproperty2 varchar2(255 char);
alter table o_user add u_genericuniquetextproperty3 varchar2(255 char);
alter table o_user add u_genericemailproperty1 varchar2(255 char);
alter table o_user add u_genericcheckboxproperty varchar2(255 char);
alter table o_user add u_genericcheckboxproperty2 varchar2(255 char);
alter table o_user add u_genericcheckboxproperty3 varchar2(255 char);

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

alter table o_userproperty drop constraint FK4B04D83FD1A80C95;

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
   from o_bs_identity ident
   inner join o_user us on (ident.fk_user_id = us.user_id)
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
   from o_gp_business bgroup
   inner join o_bs_group_member bg_member on (bg_member.fk_group_id = bgroup.fk_group_id)
   inner join o_bs_identity id_member on (bg_member.fk_identity_id = id_member.id)
   inner join o_user us_member on (id_member.fk_user_id = us_member.user_id)
   inner join o_bs_group_member bg_me on (bg_me.fk_group_id = bgroup.fk_group_id)
   where
      (bgroup.ownersintern>0 and bg_member.g_role='coach')
      or
      (bgroup.participantsintern>0 and bg_member.g_role='participant')
);
