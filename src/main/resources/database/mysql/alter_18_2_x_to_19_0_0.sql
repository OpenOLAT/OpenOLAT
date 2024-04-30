-- Export
create table o_ex_export_metadata (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   e_archive_type varchar(32),
   e_title varchar(255),
   e_description varchar(4000),
   e_file_name varchar(255),
   e_file_path varchar(1024),
   e_only_administrators bool default false,
   e_expiration_date datetime,
   fk_entry bigint,
   e_sub_ident varchar(2048),
   fk_task bigint,
   fk_creator bigint,
   fk_metadata bigint,
   primary key (id)
);
alter table o_ex_export_metadata ENGINE = InnoDB;

alter table o_ex_export_metadata add constraint export_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_ex_export_metadata add constraint export_to_creator_idx foreign key (fk_creator) references o_bs_identity (id);
alter table o_ex_export_metadata add constraint export_to_task_idx foreign key (fk_task) references o_ex_task (id);
alter table o_ex_export_metadata add constraint export_to_vfsdata_idx foreign key (fk_metadata) references o_vfs_metadata(id);

-- Content Editor
alter table o_ce_page_part add column p_storage_path varchar(255);

-- Identity
alter table o_bs_identity add column plannedinactivationdate datetime;
alter table o_bs_identity add column planneddeletiondate datetime;

-- VFS
alter table o_vfs_metadata add column f_deleted_date datetime;
alter table o_vfs_metadata add column fk_deleted_by bigint;

-- Media to Page Part (Content Editor)
create table o_media_to_page_part (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   pos bigint default null,
   fk_media bigint not null,
   fk_page_part bigint not null,
   primary key (id)
);
alter table o_media_to_page_part ENGINE = InnoDB;

alter table o_media_to_page_part add constraint media_to_page_part_media_idx foreign key (fk_media) references o_media (id);
alter table o_media_to_page_part add constraint media_to_page_part_page_part_idx foreign key (fk_page_part) references o_ce_page_part (id);
