-- edu-sharing
create table o_es_usage (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   e_identifier varchar(64) not null,
   e_resname varchar(50) not null,
   e_resid bigint not null,
   e_sub_path varchar(256),
   e_object_url varchar(255) not null,
   e_version varchar(64),
   e_mime_type varchar(128),
   e_media_type varchar(128),
   e_width varchar(8),
   e_height varchar(8),
   fk_identity bigint not null,
   primary key (id)
);

create index idx_es_usage_ident_idx on o_es_usage (e_identifier);
create index idx_es_usage_ores_idx on o_es_usage (e_resid, e_resname);

-- goto meeting
alter table o_goto_organizer add column g_refresh_token varchar(128);
alter table o_goto_organizer add column g_renew_refresh_date timestamp;
