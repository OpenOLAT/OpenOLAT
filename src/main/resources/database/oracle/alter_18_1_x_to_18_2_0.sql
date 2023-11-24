
-- To-dos
alter table o_todo_task add t_origin_sub_title varchar(500);

-- Media
create table o_media_version_metadata (
   id number(20) generated always as identity,
   creationdate date not null,
   p_url varchar(1024) default null,
   p_width number(20) default null,
   p_height number(20) default null,
   p_length varchar(32) default null,
   p_format varchar(32) default null,
   primary key (id)
);

alter table o_media_version add column fk_version_metadata number(20);

alter table o_media_version add constraint media_version_version_metadata_idx foreign key (fk_version_metadata) references o_media_version_metadata(id);
create index idx_media_version_version_metadata_idx on o_media_version (fk_version_metadata);

