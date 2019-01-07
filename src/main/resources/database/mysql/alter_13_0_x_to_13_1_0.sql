-- edu-sharing
create table o_es_usage (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   e_identifier varchar(64) not null,
   e_resname varchar(50) not null,
   e_resid bigint not null,
   e_object_url varchar(255) not null,
   e_version varchar(64),
   e_mime_type varchar(128),
   e_media_type varchar(128),
   e_width varchar(8),
   e_height varchar(8),
   fk_identity bigint not null,
   primary key (id)
);

alter table o_es_usage ENGINE = InnoDB;

create index idx_es_usage_ident_idx on o_es_usage (e_identifier);
create index idx_es_usage_ores_idx on o_eva_form_survey (e_resid, e_resname);
