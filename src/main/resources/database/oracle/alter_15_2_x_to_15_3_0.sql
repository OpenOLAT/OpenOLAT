-- Document editor
drop table o_wopi_access;
create table o_de_access (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   o_editor_type varchar(64) not null,
   o_token varchar(64) not null,
   o_expires_at timestamp not null,
   o_mode varchar(64) not null,
   o_version_controlled number default 0 not null,
   fk_metadata number(20) not null,
   fk_identity number(20) not null,
   primary key (id)
);
create table o_de_user_info (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   o_info varchar(2048) not null,
   fk_identity number(20) not null,
   primary key (id)
);

create unique index idx_de_token_idx on o_de_access(o_token);
create unique index idx_de_userinfo_ident_idx on o_de_user_info(fk_identity);
