-- edu-sharing
create table o_es_usage (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   e_identifier varchar2(64) not null,
   e_resname varchar2(50) not null,
   e_resid number(20) not null,
   e_object_url varchar2(255) not null,
   e_version varchar2(64),
   e_mime_type varchar2(128),
   e_media_type varchar2(128),
   e_width varchar2(8),
   e_height varchar2(8),
   fk_identity number(20) not null,
   primary key (id)
);

create index idx_es_usage_ident_idx on o_es_usage (e_identifier);
create index idx_es_usage_ores_idx on o_eva_form_survey (e_resid, e_resname);
