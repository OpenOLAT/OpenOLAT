create table o_cer_template (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   c_name varchar(256) not null,
   c_path varchar(1024) not null,
   c_public boolean not null,
   c_format varchar(16),
   c_orientation varchar(16),
   primary key (id)
);
alter table o_cer_template ENGINE = InnoDB;

create table o_cer_certificate (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   c_uuid varchar(36) not null,
   c_name varchar(256) not null,
   c_path varchar(1024) not null,
   c_last boolean not null default 1,
   c_course_title varchar(255),
   c_archived_resource_id bigint not null,
   fk_olatresource bigint,
   fk_identity bigint not null,
   primary key (id)
);
alter table o_cer_certificate ENGINE = InnoDB;

alter table o_cer_certificate add constraint cer_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_cer_certificate add constraint cer_to_resource_idx foreign key (fk_olatresource) references o_olatresource (resource_id);
create index cer_archived_resource_idx on o_cer_certificate (c_archived_resource_id);
create index cer_uuid_idx on o_cer_certificate (c_uuid);


alter table o_gp_business add column allowtoleave boolean default 1;


drop view o_qp_item_shared_v;
drop view o_qp_item_pool_v;
drop view o_qp_item_author_v;
drop view o_qp_item_v;

drop view o_gp_member_v;