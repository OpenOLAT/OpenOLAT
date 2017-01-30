create table o_vid_metadata (
  id bigint not null auto_increment,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  vid_width bigint default null,
  vid_height bigint default null,
  vid_size bigint default null,
  vid_format varchar(32) default null,
  vid_length varchar(32) default null,
  fk_resource_id bigint not null,
  primary key (id)
);

alter table o_vid_metadata ENGINE = InnoDB;

alter table o_vid_metadata add constraint vid_meta_rsrc_idx foreign key (fk_resource_id) references o_olatresource (resource_id);
