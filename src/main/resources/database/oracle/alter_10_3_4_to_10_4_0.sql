alter table o_message modify (creator_id null);
alter table o_message add pseudonym varchar2(255 char);
alter table o_message add guest number default 0 not null;


alter table o_repositoryEntry add location varchar2(255 char) default null;


create table o_cal_use_config (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   c_calendar_id varchar2(128 char) not null,
   c_calendar_type varchar2(16 char) not null,
   c_token varchar2(36 char),
   c_cssclass varchar2(36 char),
   c_visible number default 1 not null,
   c_aggregated_feed number default 1 not null,
   fk_identity number(20) not null,
   primary key (id),
   unique (c_calendar_id, c_calendar_type, fk_identity)
);

alter table o_cal_use_config add constraint cal_u_conf_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_cal_u_conf_to_ident_idx on o_cal_use_config (fk_identity);
create index idx_cal_u_conf_cal_id_idx on o_cal_use_config (c_calendar_id);
create index idx_cal_u_conf_cal_type_idx on o_cal_use_config (c_calendar_type);


create table o_cal_import (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   c_calendar_id varchar2(128 char) not null,
   c_calendar_type varchar2(16 char) not null,
   c_displayname varchar2(256 char),
   c_lastupdate date not null,
   c_url varchar2(1024 char),
   fk_identity number(20),
   primary key (id)
);

alter table o_cal_import add constraint cal_imp_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_cal_imp_to_ident_idx on o_cal_import (fk_identity);
create index idx_cal_imp_cal_id_idx on o_cal_import (c_calendar_id);
create index idx_cal_imp_cal_type_idx on o_cal_import (c_calendar_type);


create table o_cal_import_to (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   c_to_calendar_id varchar2(128 char) not null,
   c_to_calendar_type varchar2(16 char) not null,
   c_lastupdate date not null,
   c_url varchar2(1024 char),
   primary key (id)
);

create index idx_cal_imp_to_cal_id_idx on o_cal_import_to (c_to_calendar_id);
create index idx_cal_imp_to_cal_type_idx on o_cal_import_to (c_to_calendar_type);