-- VFS
create table o_vfs_statistics (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   f_files_amount number(20) default 0,
   f_files_size number(20) default 0,
   f_trash_amount number(20) default 0,
   f_trash_size number(20) default 0,
   f_revisions_amount number(20) default 0,
   f_revisions_size number(20) default 0,
   f_thumbnails_amount number(20) default 0,
   f_thumbnails_size number(20) default 0,
   primary key (id)
);