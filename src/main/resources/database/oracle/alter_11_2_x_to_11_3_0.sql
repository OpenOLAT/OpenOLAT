create table o_vid_metadata (
  id number(20) GENERATED ALWAYS AS IDENTITY,
  creationdate date not null,
  lastmodified date not null,
  vid_width number(20) default null,
  vid_height number(20) default null,
  vid_size number(20) default null,
  vid_format varchar2(32 char) default null,
  vid_length varchar2(32 char) default null,
  fk_resource_id number(20) not null,
  primary key (id)
);

alter table o_vid_metadata add constraint vid_meta_rsrc_idx foreign key (fk_resource_id) references o_olatresource (resource_id);
create index idx_vid_meta_rsrc_idx on o_vid_metadata(fk_resource_id);