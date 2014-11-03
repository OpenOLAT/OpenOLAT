create table o_cer_template (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   c_name varchar2(256 char) not null,
   c_path varchar2(1024 char) not null,
   c_public number default 0 not null,
   primary key (id)
);

create table o_cer_certificate (
   id number(20) not null,
   creationdate date not null,
   lastmodified date not null,
   c_uuid varchar2(36 char) not null,
   c_name varchar2(256 char) not null,
   c_path varchar2(1024 char) not null,
   c_last number default 1 not null,
   c_archived_resource_id number(20) not null,
   fk_olatresource number(20),
   fk_identity number(20) not null,
   primary key (id)
);

alter table o_cer_certificate add constraint cer_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index cer_identity_idx on o_cer_certificate (fk_identity);
alter table o_cer_certificate add constraint cer_to_resource_idx foreign key (fk_olatresource) references o_olatresource (resource_id);
create index cer_resource_idx on o_cer_certificate (fk_olatresource);
create index cer_archived_resource_idx on o_cer_certificate (c_archived_resource_id);
create index cer_uuid_idx on o_cer_certificate (c_uuid);


alter table o_gp_business add allowtoleave number default 1 not null;


drop view o_qp_item_shared_v;
drop view o_qp_item_pool_v;
drop view o_qp_item_author_v;
drop view o_qp_item_v;

drop view o_gp_member_v;