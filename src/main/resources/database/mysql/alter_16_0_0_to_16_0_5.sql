-- VFS
create table o_vfs_statistics (
   id bigint not null auto_increment,
   creationdate datetime not null,
   f_files_amount bigint default 0,
   f_files_size bigint default 0,
   f_trash_amount bigint default 0,
   f_trash_size bigint default 0,
   f_revisions_amount bigint default 0,
   f_revisions_size bigint default 0,
   f_thumbnails_amount bigint default 0,
   f_thumbnails_size bigint default 0,
   primary key (id)
);