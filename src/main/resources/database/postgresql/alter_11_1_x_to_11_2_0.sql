create table o_pf_binder_user_infos (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_initiallaunchdate timestamp,
   p_recentlaunchdate timestamp,
   p_visit int4,
   fk_identity int8,
   fk_binder int8,
   unique(fk_identity, fk_binder),
   primary key (id)
);

alter table o_pf_binder_user_infos add constraint binder_user_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_binder_user_to_ident_idx on o_pf_binder_user_infos (fk_identity);
alter table o_pf_binder_user_infos add constraint binder_user_binder_idx foreign key (fk_binder) references o_pf_binder (id);
create index idx_binder_user_binder_idx on o_pf_binder_user_infos (fk_binder);


alter table o_repositoryentry add column deletiondate timestamp default null;
alter table o_repositoryentry add column fk_deleted_by int8 default null;
alter table o_repositoryentry add constraint re_deleted_to_identity_idx foreign key (fk_deleted_by) references o_bs_identity (id);
create index idx_re_deleted_to_identity_idx on o_repositoryentry (fk_deleted_by);


alter table o_pf_assignment add column fk_form_entry_id int8 default null;
alter table o_pf_assignment add constraint pf_assign_form_idx foreign key (fk_form_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_pf_assign_form_idx on o_pf_assignment (fk_form_entry_id);

alter table o_pf_assignment add column p_only_auto_eva bool default true;
alter table o_pf_assignment add column p_reviewer_see_auto_eva bool default false;
alter table o_pf_assignment add column p_anon_extern_eva bool default true;

alter table o_pf_page add column p_editable bool default true;

alter table o_pf_page_part add column fk_form_entry_id int8 default null;
alter table o_pf_page_part add constraint pf_part_form_idx foreign key (fk_form_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_pf_part_form_idx on o_pf_page_part (fk_form_entry_id);


create table o_eva_form_session (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   e_status varchar(16),
   e_submission_date timestamp,
   e_first_submission_date timestamp,
   fk_identity int8 not null,
   fk_page_body int8,
   fk_form_entry int8 not null,
   primary key (id)
);

alter table o_eva_form_session add constraint eva_session_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_eva_session_to_ident_idx on o_eva_form_session (fk_identity);
alter table o_eva_form_session add constraint eva_session_to_body_idx foreign key (fk_page_body) references o_pf_page_body (id);
create index idx_eva_session_to_body_idx on o_eva_form_session (fk_page_body);
alter table o_eva_form_session add constraint eva_session_to_form_idx foreign key (fk_form_entry) references o_repositoryentry (repositoryentry_id);
create index idx_eva_session_to_form_idx on o_eva_form_session (fk_form_entry);

create table o_eva_form_response (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   e_responseidentifier varchar(64) not null,
   e_responsedatatype varchar(16) not null,
   e_numericalresponse decimal default null,
   e_stringuifiedresponse text,
   fk_session int8 not null,
   primary key (id)
);

alter table o_eva_form_response add constraint eva_resp_to_sess_idx foreign key (fk_session) references o_eva_form_session (id);
create index idx_eva_resp_to_sess_idx on o_eva_form_response (fk_session);


alter table o_user add column fk_identity int8;

update o_user set fk_identity=(select id from o_bs_identity where user_id=fk_user_id) where fk_identity is null;

alter table o_user add constraint user_to_ident_idx foreign key (fk_identity) references o_bs_identity(id);
create index idx_user_to_ident_idx on o_user (fk_identity);
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
      (bgroup.ownersintern=true and bg_member.g_role='coach')
      or
      (bgroup.participantsintern=true and bg_member.g_role='participant')
);

-- reset status code
update o_repositoryentry set statuscode=0 where statuscode=1;




