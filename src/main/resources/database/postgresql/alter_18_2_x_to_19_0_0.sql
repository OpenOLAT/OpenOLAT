-- Export
create table o_ex_export_metadata (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   e_archive_type varchar(32),
   e_title varchar(255),
   e_description varchar(4000),
   e_file_name varchar(255),
   e_file_path varchar(1024),
   e_only_administrators bool default false,
   e_expiration_date timestamp,
   fk_entry int8,
   e_sub_ident varchar(2048),
   fk_task int8,
   fk_creator int8,
   fk_metadata int8,
   primary key (id)
);

alter table o_ex_export_metadata add constraint export_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_export_to_entry_idx on o_ex_export_metadata(fk_entry);
alter table o_ex_export_metadata add constraint export_to_creator_idx foreign key (fk_creator) references o_bs_identity (id);
create index idx_export_to_creator_idx on o_ex_export_metadata(fk_creator);
alter table o_ex_export_metadata add constraint export_to_task_idx foreign key (fk_task) references o_ex_task (id);
create index idx_export_to_task_idx on o_ex_export_metadata(fk_task);
create index idx_export_sub_ident_idx on o_ex_export_metadata(e_sub_ident);
alter table o_ex_export_metadata add constraint export_to_vfsdata_idx foreign key (fk_metadata) references o_vfs_metadata(id);
create index idx_export_to_vfsdata_idx on o_ex_export_metadata(fk_metadata);

-- Content Editor
alter table o_ce_page_part add column p_storage_path varchar(255);

-- Identity
alter table o_bs_identity add column plannedinactivationdate timestamp;
alter table o_bs_identity add column planneddeletiondate timestamp;

-- VFS
alter table o_vfs_metadata add column f_deleted_date timestamp;
alter table o_vfs_metadata add column fk_deleted_by int8;

-- Media to Page Part (Content Editor)
create table o_media_to_page_part (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   pos int8 default null,
   fk_media int8 not null,
   fk_page_part int8 not null,
   primary key (id)
);

alter table o_media_to_page_part add constraint media_to_page_part_media_idx foreign key (fk_media) references o_media (id);
create index idx_media_to_page_part_media_idx on o_media_to_page_part (fk_media);
alter table o_media_to_page_part add constraint media_to_page_part_page_part_idx foreign key (fk_page_part) references o_ce_page_part (id);
create index idx_media_to_page_part_page_part_idx on o_media_to_page_part (fk_page_part);
