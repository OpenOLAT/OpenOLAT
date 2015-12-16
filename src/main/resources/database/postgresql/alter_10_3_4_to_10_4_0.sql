alter table o_message alter column creator_id DROP NOT NULL;
alter table o_message add column pseudonym varchar(255);
alter table o_message add column guest bool not null default false;


alter table o_repositoryEntry add column location varchar(255) default null;


create table o_cal_use_config (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_calendar_id varchar(128) not null,
   c_calendar_type varchar(16) not null,
   c_token varchar(36),
   c_cssclass varchar(36),
   c_visible bool default true,
   c_aggregated_feed bool default true,
   fk_identity int8 not null,
   primary key (id),
   unique (c_calendar_id, c_calendar_type, fk_identity)
);

alter table o_cal_use_config add constraint cal_u_conf_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_cal_u_conf_to_ident_idx on o_cal_use_config (fk_identity);
create index idx_cal_u_conf_cal_id_idx on o_cal_use_config (c_calendar_id);
create index idx_cal_u_conf_cal_type_idx on o_cal_use_config (c_calendar_type);


create table o_cal_import (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_calendar_id varchar(128) not null,
   c_calendar_type varchar(16) not null,
   c_displayname varchar(256),
   c_lastupdate timestamp not null,
   c_url varchar(1024),
   fk_identity int8,
   primary key (id)
);

alter table o_cal_import add constraint cal_imp_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_cal_imp_to_ident_idx on o_cal_import (fk_identity);
create index idx_cal_imp_cal_id_idx on o_cal_import (c_calendar_id);
create index idx_cal_imp_cal_type_idx on o_cal_import (c_calendar_type);


create table o_cal_import_to (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_to_calendar_id varchar(128) not null,
   c_to_calendar_type varchar(16) not null,
   c_lastupdate timestamp not null,
   c_url varchar(1024),
   primary key (id)
);

create index idx_cal_imp_to_cal_id_idx on o_cal_import_to (c_to_calendar_id);
create index idx_cal_imp_to_cal_type_idx on o_cal_import_to (c_to_calendar_type);