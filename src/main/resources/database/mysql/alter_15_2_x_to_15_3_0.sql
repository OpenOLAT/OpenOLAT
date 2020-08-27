-- Document editor
drop table o_wopi_access;
create table o_de_access (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   o_editor_type varchar(64) not null,
   o_token varchar(64) not null,
   o_expires_at datetime not null,
   o_mode varchar(64) not null,
   o_version_controlled bool not null,
   fk_metadata bigint not null,
   fk_identity bigint not null,
   primary key (id)
);
create table o_de_user_info (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   o_info varchar(2048) not null,
   fk_identity bigint not null,
   primary key (id)
);

alter table o_de_access ENGINE = InnoDB;
alter table o_de_user_info ENGINE = InnoDB;

create unique index idx_de_token_idx on o_de_access(o_token);
create unique index idx_de_userinfo_ident_idx on o_de_user_info(fk_identity);
