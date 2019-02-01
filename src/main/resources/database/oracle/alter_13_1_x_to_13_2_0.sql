-- user to user relations
create table o_bs_relation_role (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_role varchar(128) not null,
   g_external_id varchar(128),
   g_external_ref varchar(128),
   g_managed_flags varchar(256),
   primary key (id)
);

create table o_bs_relation_right (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   g_right varchar(128) not null,
   primary key (id)
);
create index idx_right_idx on o_bs_relation_right (g_right);

create table o_bs_relation_role_to_right (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   fk_role_id number(20),
   fk_right_id number(20) not null,
   primary key (id)
);

alter table o_bs_relation_role_to_right add constraint role_to_right_role_idx foreign key (fk_role_id) references o_bs_relation_role (id);
create index idx_role_to_right_role_idx on o_bs_relation_role_to_right (fk_role_id);
alter table o_bs_relation_role_to_right add constraint role_to_right_right_idx foreign key (fk_right_id) references o_bs_relation_right (id);
create index idx_role_to_right_right_idx on o_bs_relation_role_to_right (fk_right_id);

create table o_bs_identity_to_identity (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   g_external_id varchar(128),
   g_managed_flags varchar(256),
   fk_source_id number(20) not null,
   fk_target_id number(20) not null,
   fk_role_id number(20) not null,
   primary key (id)
);

alter table o_bs_identity_to_identity add constraint id_to_id_source_idx foreign key (fk_source_id) references o_bs_identity (id);
create index idx_id_to_id_source_idx on o_bs_identity_to_identity (fk_source_id);
alter table o_bs_identity_to_identity add constraint id_to_id_target_idx foreign key (fk_target_id) references o_bs_identity (id);
create index idx_id_to_id_target_idx on o_bs_identity_to_identity (fk_target_id);
alter table o_bs_identity_to_identity add constraint id_to_role_idx foreign key (fk_role_id) references o_bs_relation_role (id);
create index idx_id_to_id_role_idx on o_bs_identity_to_identity (fk_role_id);


-- quality management
alter table o_qual_analysis_presentation add q_temporal_grouping varchar(50);
alter table o_qual_analysis_presentation add q_trend_difference varchar(50);
alter table o_qual_analysis_presentation add q_rubric_id varchar(50);
