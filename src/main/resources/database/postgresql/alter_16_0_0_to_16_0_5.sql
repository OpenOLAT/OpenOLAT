-- VFS
create table o_vfs_statistics (
   id bigserial,
   creationdate timestamp not null,
   f_files_amount int8 default 0,
   f_files_size int8 default 0,
   f_trash_amount int8 default 0,
   f_trash_size int8 default 0,
   f_revisions_amount int8 default 0,
   f_revisions_size int8 default 0,
   f_thumbnails_amount int8 default 0,
   f_thumbnails_size int8 default 0,
   primary key (id)
);