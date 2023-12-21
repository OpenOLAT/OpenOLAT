
-- To-dos
alter table o_todo_task add column t_origin_sub_title varchar(500);

-- Media
create table o_media_version_metadata (
   id bigserial,
   creationdate timestamp not null,
   p_url varchar(1024) default null,
   p_width int8 default null,
   p_height int8 default null,
   p_length varchar(32) default null,
   p_format varchar(32) default null,
   primary key (id)
);

alter table o_media_version add column fk_version_metadata int8;

alter table o_media_version add constraint media_version_version_metadata_idx foreign key (fk_version_metadata) references o_media_version_metadata(id);
create index idx_media_version_version_metadata_idx on o_media_version (fk_version_metadata);


-- Weighted score
alter table o_as_entry add column a_weighted_score decimal default null;
alter table o_as_entry add column a_score_scale decimal default null;
alter table o_as_entry add column a_weighted_max_score decimal default null;

alter table o_as_eff_statement add column weighted_score decimal default null;


-- Quality management
create table o_qual_generator_override (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_identifier varchar(512) not null,
   q_start timestamp,
   q_generator_provider_key int8,
   fk_generator int8 not null,
   fk_data_collection int8,
   primary key (id)
);

alter table o_qual_generator_override add constraint qual_override_to_gen_idx foreign key (fk_generator) references o_qual_generator (id);
create index idx_override_to_gen_idx on o_qual_generator_override(fk_generator);
alter table o_qual_generator_override add constraint qual_override_to_dc_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
create index idx_override_to_dc_idx on o_qual_generator_override(fk_data_collection);
create index idx_override_ident_idx on o_qual_generator_override(q_identifier);
