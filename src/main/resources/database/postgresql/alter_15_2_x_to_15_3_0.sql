-- Document editor
drop table o_wopi_access;
create table o_de_access (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   o_editor_type varchar(64) not null,
   o_expires_at timestamp not null,
   o_mode varchar(64) not null,
   o_version_controlled bool not null,
   fk_metadata bigint not null,
   fk_identity bigint not null,
   primary key (id)
);

create table o_de_user_info (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   o_info varchar(2048) not null,
   fk_identity bigint not null,
   primary key (id)
);

create unique index idx_de_userinfo_ident_idx on o_de_user_info(fk_identity);


-- Assessment
alter table o_as_entry add column a_current_run_start timestamp;

