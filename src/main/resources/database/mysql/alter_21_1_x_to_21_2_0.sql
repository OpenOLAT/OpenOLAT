-- MCP server
create table o_mcp_profile (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   m_identifier varchar(64) not null,
   m_name varchar(255) not null,
   m_description mediumtext,
   m_enabled bool default false not null,
   m_type varchar(16) not null,
   m_tools mediumtext,
   m_org_roles varchar(1024),
   m_ratelimit_rpm int,
   primary key (id)
);

create table o_mcp_profile_to_identity (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_profile bigint not null,
   fk_identity bigint not null,
   primary key (id)
);

create table o_mcp_profile_to_organisation (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_profile bigint not null,
   fk_organisation bigint not null,
   primary key (id)
);
alter table o_mcp_profile ENGINE = InnoDB;
alter table o_mcp_profile_to_identity ENGINE = InnoDB;
alter table o_mcp_profile_to_organisation ENGINE = InnoDB;

create unique index idx_mcp_profile_ident_idx on o_mcp_profile (m_identifier);
alter table o_mcp_profile_to_identity add constraint mcp_prof_to_id_prof_idx foreign key (fk_profile) references o_mcp_profile (id);
alter table o_mcp_profile_to_identity add constraint mcp_prof_to_id_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create unique index idx_mcp_prof_to_id_uniq_idx on o_mcp_profile_to_identity (fk_profile, fk_identity);
alter table o_mcp_profile_to_organisation add constraint mcp_prof_to_org_prof_idx foreign key (fk_profile) references o_mcp_profile (id);
alter table o_mcp_profile_to_organisation add constraint mcp_prof_to_org_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create unique index idx_mcp_prof_to_org_uniq_idx on o_mcp_profile_to_organisation (fk_profile, fk_organisation);

-- User course infos
alter table o_as_user_course_infos add column activity double default 0;
