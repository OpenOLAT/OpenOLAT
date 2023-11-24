
-- To-dos
alter table o_todo_task add column t_origin_sub_title varchar(500);

-- Media
create table o_media_version_metadata (
   id bigint not null auto_increment,
   creationdate datetime not null,
   p_url varchar(1024) default null,
   p_width bigint default null,
   p_height bigint default null,
   p_length varchar(32) default null,
   p_format varchar(32) default null,
   primary key (id)
);
alter table o_media_version_metadata ENGINE = InnoDB;

alter table o_media_version add column fk_version_metadata bigint;

alter table o_media_version add constraint media_version_version_metadata_idx foreign key (fk_version_metadata) references o_media_version_metadata(id);
