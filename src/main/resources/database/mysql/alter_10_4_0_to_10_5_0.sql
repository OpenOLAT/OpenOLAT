create table o_goto_organizer (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_name varchar(128) default null,
   g_account_key varchar(128) default null,
   g_access_token varchar(128) not null,
   g_renew_date datetime not null,
   g_organizer_key varchar(128) not null,
   g_username varchar(128) not null,
   g_firstname varchar(128) default null,
   g_lastname varchar(128) default null,
   g_email varchar(128) default null,
   fk_identity bigint default null,
   primary key (id)
);
alter table o_goto_organizer ENGINE = InnoDB;

alter table o_goto_organizer add constraint goto_organ_owner_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_goto_organ_okey_idx on o_goto_organizer(g_organizer_key);
create index idx_goto_organ_uname_idx on o_goto_organizer(g_username);


create table o_goto_meeting (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_external_id varchar(128) default null, 
   g_type varchar(16) not null,
   g_meeting_key varchar(128) not null,
   g_name varchar(255) default null,
   g_description varchar(2000) default null,
   g_start_date datetime default null,
   g_end_date datetime default null,
   fk_organizer_id bigint not null,
   fk_entry_id bigint default null,
   g_sub_ident varchar(64) default null,
   fk_group_id bigint default null,
   primary key (id)
);
alter table o_goto_meeting ENGINE = InnoDB;

alter table o_goto_meeting add constraint goto_meet_repoentry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
alter table o_goto_meeting add constraint goto_meet_busgrp_idx foreign key (fk_group_id) references o_gp_business (group_id);
alter table o_goto_meeting add constraint goto_meet_organizer_idx foreign key (fk_organizer_id) references o_goto_organizer (id);


create table o_goto_registrant (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   g_status varchar(16) default null,
   g_join_url varchar(1024) default null,
   g_confirm_url varchar(1024) default null,
   g_registrant_key varchar(64) default null,
   fk_meeting_id bigint not null,
   fk_identity_id bigint not null,
   primary key (id)
);
alter table o_goto_registrant ENGINE = InnoDB;

alter table o_goto_registrant add constraint goto_regis_meeting_idx foreign key (fk_meeting_id) references o_goto_meeting (id);
alter table o_goto_registrant add constraint goto_regis_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);


create table o_vid_transcoding (
   id bigint not null,
   creationdate datetime not null,
   lastmodified datetime not null,
   vid_resolution bigint default null,
   vid_width bigint default null,
   vid_height bigint default null,
   vid_size bigint default null,
   vid_format varchar(128) default null,
   vid_status bigint default null,
   vid_transcoder varchar(128) default null,
   fk_resource_id bigint not null,
   primary key (id)
);
alter table o_vid_transcoding ENGINE = InnoDB;

alter table o_vid_transcoding add constraint fk_resource_id_idx foreign key (fk_resource_id) references o_olatresource (resource_id);
create index vid_status_trans_idx on o_vid_transcoding(vid_status);
create index vid_transcoder_trans_idx on o_vid_transcoding(vid_transcoder);




