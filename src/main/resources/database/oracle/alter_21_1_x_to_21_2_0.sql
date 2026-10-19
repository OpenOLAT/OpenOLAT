-- MCP server
create table o_mcp_profile (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   m_identifier varchar2(64 char) not null,
   m_name varchar2(255 char) not null,
   m_description clob,
   m_enabled number default 0 not null,
   m_type varchar2(16 char) not null,
   m_tools clob,
   m_org_roles varchar2(1024 char),
   m_ratelimit_rpm number(20),
   primary key (id)
);

create table o_mcp_profile_to_identity (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate timestamp not null,
   fk_profile number(20) not null,
   fk_identity number(20) not null,
   primary key (id)
);

create table o_mcp_profile_to_organisation (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate timestamp not null,
   fk_profile number(20) not null,
   fk_organisation number(20) not null,
   primary key (id)
);

create unique index idx_mcp_profile_ident_idx on o_mcp_profile (m_identifier);
alter table o_mcp_profile_to_identity add constraint mcp_prof_to_id_prof_idx foreign key (fk_profile) references o_mcp_profile (id);
create index idx_mcp_prof_to_id_prof_idx on o_mcp_profile_to_identity (fk_profile);
alter table o_mcp_profile_to_identity add constraint mcp_prof_to_id_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_mcp_prof_to_id_ident_idx on o_mcp_profile_to_identity (fk_identity);
create unique index idx_mcp_prof_to_id_uniq_idx on o_mcp_profile_to_identity (fk_profile, fk_identity);
alter table o_mcp_profile_to_organisation add constraint mcp_prof_to_org_prof_idx foreign key (fk_profile) references o_mcp_profile (id);
create index idx_mcp_prof_to_org_prof_idx on o_mcp_profile_to_organisation (fk_profile);
alter table o_mcp_profile_to_organisation add constraint mcp_prof_to_org_org_idx foreign key (fk_organisation) references o_org_organisation (id);
create index idx_mcp_prof_to_org_org_idx on o_mcp_profile_to_organisation (fk_organisation);
create unique index idx_mcp_prof_to_org_uniq_idx on o_mcp_profile_to_organisation (fk_profile, fk_organisation);
