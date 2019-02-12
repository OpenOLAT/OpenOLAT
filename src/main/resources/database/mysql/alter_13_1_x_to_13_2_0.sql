-- user to user relations
create table o_bs_relation_role (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_role varchar(128) not null,
   g_external_id varchar(128),
   g_external_ref varchar(128),
   g_managed_flags varchar(256),
   primary key (id)
);

alter table o_bs_relation_role ENGINE = InnoDB;

create table o_bs_relation_right (
   id bigint not null auto_increment,
   creationdate datetime not null,
   g_right varchar(128) not null,
   primary key (id)
);

alter table o_bs_relation_right ENGINE = InnoDB;

create index idx_right_idx on o_bs_relation_right (g_right);

create table o_bs_relation_role_to_right (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_role_id bigint,
   fk_right_id bigint not null,
   primary key (id)
);

alter table o_bs_relation_role_to_right ENGINE = InnoDB;

alter table o_bs_relation_role_to_right add constraint role_to_right_role_idx foreign key (fk_role_id) references o_bs_relation_role (id);
alter table o_bs_relation_role_to_right add constraint role_to_right_right_idx foreign key (fk_right_id) references o_bs_relation_right (id);

create table o_bs_identity_to_identity (
   id bigint not null auto_increment,
   creationdate datetime not null,
   g_external_id varchar(128),
   g_managed_flags varchar(256),
   fk_source_id bigint not null,
   fk_target_id bigint not null,
   fk_role_id bigint not null,
   primary key (id)
);

alter table o_bs_identity_to_identity add constraint id_to_id_source_idx foreign key (fk_source_id) references o_bs_identity (id);
alter table o_bs_identity_to_identity add constraint id_to_id_target_idx foreign key (fk_target_id) references o_bs_identity (id);
alter table o_bs_identity_to_identity add constraint id_to_role_idx foreign key (fk_role_id) references o_bs_relation_role (id);


-- quality management
alter table o_qual_analysis_presentation add q_temporal_grouping varchar(50);
alter table o_qual_analysis_presentation add q_trend_difference varchar(50);
alter table o_qual_analysis_presentation add q_rubric_id varchar(50);


-- video
alter table o_vid_metadata add vid_url varchar(512);

