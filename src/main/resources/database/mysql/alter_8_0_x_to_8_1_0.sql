-- user view
create view o_bs_identity_short_v as (
   select
      ident.id as id_id,
      ident.name as id_name,
      ident.lastlogin as id_lastlogin,
      ident.status as id_status,
      us.user_id as us_id,
      p_firstname.propvalue as first_name,
      p_lastname.propvalue as last_name,
      p_email.propvalue as email
   from o_bs_identity as ident
   inner join o_user as us on (ident.fk_user_id = us.user_id)
   left join o_userproperty as p_firstname on (us.user_id = p_firstname.fk_user_id and p_firstname.propName = 'firstName')
   left join o_userproperty as p_lastname on (us.user_id = p_lastname.fk_user_id and p_lastname.propName = 'lastName')
   left join o_userproperty as p_email on (us.user_id = p_email.fk_user_id and p_email.propName = 'email')
);

-- assessment tables
-- efficiency statments
create table if not exists o_as_eff_statement (
   id bigint not null,
   version mediumint unsigned not null,
   lastmodified datetime,
   creationdate datetime,
   passed bit(0),
   score bigint,
   total_nodes bigint,
   attempted_nodes bigint,
   passed_nodes bigint,
   course_title varchar(255),
   course_short_title varchar(128),
   course_repo_key bigint,
   statement_xml longtext,
   fk_identity bigint,
   fk_resource_id bigint,
   primary key (id)
);
alter table o_as_eff_statement ENGINE = InnoDB;
alter table o_as_eff_statement add unique eff_statement_id_cstr (fk_identity, fk_resource_id), add constraint eff_statement_id_cstr foreign key (fk_identity) references o_bs_identity (id);
create index eff_statement_repo_key_idx on o_as_eff_statement (course_repo_key);

-- user to course informations (was property initial and recent launch dates)
create table o_as_user_course_infos (
   id bigint not null,
   version mediumint unsigned not null,
   creationdate datetime,
   lastmodified datetime,
   initiallaunchdate datetime,
   recentlaunchdate datetime,
   visit bigint,
   timespend bigint,
   fk_identity bigint,
   fk_resource_id bigint,
   primary key (id)
);
alter table o_as_user_course_infos ENGINE = InnoDB;
alter table o_as_user_course_infos add index user_course_infos_id_cstr (fk_identity), add constraint user_course_infos_id_cstr foreign key (fk_identity) references o_bs_identity (id);
alter table o_as_user_course_infos add index user_course_infos_res_cstr (fk_resource_id), add constraint user_course_infos_res_cstr foreign key (fk_resource_id) references o_olatresource (resource_id);
alter table o_as_user_course_infos add unique (fk_identity, fk_resource_id);

-- assessment results
-- help view
drop view if exists o_gp_contextresource_2_group_v;
create view o_gp_contextresource_2_group_v as (
   select
      cg_bg2resource.groupcontextresource_id as groupcontextresource_id,
      cg_bgcontext.groupcontext_id as groupcontext_id,
      cg_bgroup.group_id as group_id,
      cg_bg2resource.oresource_id as oresource_id,
      cg_bgcontext.grouptype as grouptype,
      cg_bgcontext.defaultcontext as defaultcontext,
      cg_bgroup.groupname as groupname,
      cg_bgroup.fk_ownergroup as fk_ownergroup,
      cg_bgroup.fk_partipiciantgroup as fk_partipiciantgroup,
      cg_bgroup.fk_waitinggroup as fk_waitinggroup
   from o_gp_bgcontextresource_rel as cg_bg2resource
   inner join o_gp_bgcontext as cg_bgcontext on (cg_bg2resource.groupcontext_fk = cg_bgcontext.groupcontext_id)
   inner join o_gp_business as cg_bgroup on (cg_bg2resource.groupcontext_fk = cg_bgroup.groupcontext_fk)
);
