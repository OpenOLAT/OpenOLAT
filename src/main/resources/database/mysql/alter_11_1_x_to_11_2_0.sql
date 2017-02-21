create table o_pf_binder_user_infos (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_initiallaunchdate datetime,
   p_recentlaunchdate datetime,
   p_visit bigint,
   fk_identity bigint,
   fk_binder bigint,
   unique(fk_identity, fk_binder),
   primary key (id)
);
alter table o_pf_binder_user_infos ENGINE = InnoDB;

alter table o_pf_binder_user_infos add constraint binder_user_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_pf_binder_user_infos add constraint binder_user_binder_idx foreign key (fk_binder) references o_pf_binder (id);


alter table o_repositoryentry add column deletiondate datetime default null;
alter table o_repositoryentry add column fk_deleted_by bigint default null;
alter table o_repositoryentry add constraint re_deleted_to_identity_idx foreign key (fk_deleted_by) references o_bs_identity (id);


alter table o_pf_assignment add column fk_form_entry_id bigint default null;
alter table o_pf_assignment add constraint pf_assign_form_idx foreign key (fk_form_entry_id) references o_repositoryentry (repositoryentry_id);

alter table o_pf_assignment add column p_only_auto_eva bit default 1;
alter table o_pf_assignment add column p_reviewer_see_auto_eva bit default 0;
alter table o_pf_assignment add column p_anon_extern_eva bit default 1;

alter table o_pf_page add column p_editable bit default 1;

alter table o_pf_page_part add column fk_form_entry_id bigint default null;
alter table o_pf_page_part add constraint pf_part_form_idx foreign key (fk_form_entry_id) references o_repositoryentry (repositoryentry_id);


create table o_eva_form_session (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   e_status varchar(16),
   e_submission_date datetime,
   e_first_submission_date datetime,
   fk_identity bigint not null,
   fk_page_body bigint,
   fk_form_entry bigint not null,
   primary key (id)
);
alter table o_eva_form_session ENGINE = InnoDB;

alter table o_eva_form_session add constraint eva_session_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_eva_form_session add constraint eva_session_to_body_idx foreign key (fk_page_body) references o_pf_page_body (id);
alter table o_eva_form_session add constraint eva_session_to_form_idx foreign key (fk_form_entry) references o_repositoryentry (repositoryentry_id);

create table o_eva_form_response (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   e_responseidentifier varchar(64) not null,
   e_responsedatatype varchar(16) not null,
   e_numericalresponse decimal default null,
   e_stringuifiedresponse mediumtext,
   fk_session bigint not null,
   primary key (id)
);
alter table o_eva_form_response ENGINE = InnoDB;

alter table o_eva_form_response add constraint eva_resp_to_sess_idx foreign key (fk_session) references o_eva_form_session (id);


alter table o_user add column fk_identity bigint;

update o_user set fk_identity=(select id from o_bs_identity where user_id=fk_user_id) where fk_identity is null;

alter table o_user add constraint user_to_ident_idx foreign key (fk_identity) references o_bs_identity(id);
alter table o_user add constraint idx_un_user_to_ident_idx UNIQUE (fk_identity);

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
   inner join o_user as us on (ident.id = us.fk_identity)
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
   inner join o_user as us_member on (id_member.id = us_member.fk_identity)
   inner join o_bs_group_member as bg_me on (bg_me.fk_group_id = bgroup.fk_group_id)
   where
      (bgroup.ownersintern=1 and bg_member.g_role='coach')
      or
      (bgroup.participantsintern=1 and bg_member.g_role='participant')
);

-- reset status code
update o_repositoryentry set statuscode=0 where statuscode=1;


