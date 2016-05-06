create table o_goto_organizer (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_name varchar(128) default null,
   g_account_key varchar(128) default null,
   g_access_token varchar(128) not null,
   g_renew_date timestamp not null,
   g_organizer_key varchar(128) not null,
   g_username varchar(128) not null,
   g_firstname varchar(128) default null,
   g_lastname varchar(128) default null,
   g_email varchar(128) default null,
   fk_identity int8 default null,
   primary key (id)
);

alter table o_goto_organizer add constraint goto_organ_owner_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_goto_organ_owner_idx on o_goto_organizer(fk_identity);
create index idx_goto_organ_okey_idx on o_goto_organizer(g_organizer_key);
create index idx_goto_organ_uname_idx on o_goto_organizer(g_username);


create table o_goto_meeting (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_external_id varchar(128) default null,
   g_type varchar(16) not null,
   g_meeting_key varchar(128) not null,
   g_name varchar(255) default null,
   g_description varchar(2000) default null,
   g_start_date timestamp default null,
   g_end_date timestamp default null,
   fk_organizer_id int8 not null,
   fk_entry_id int8 default null,
   g_sub_ident varchar(64) default null,
   fk_group_id int8 default null,
   primary key (id)
);

alter table o_goto_meeting add constraint goto_meet_repoentry_idx foreign key (fk_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_goto_meet_repoentry_idx on o_goto_meeting(fk_entry_id);
alter table o_goto_meeting add constraint goto_meet_busgrp_idx foreign key (fk_group_id) references o_gp_business (group_id);
create index idx_goto_meet_busgrp_idx on o_goto_meeting(fk_group_id);
alter table o_goto_meeting add constraint goto_meet_organizer_idx foreign key (fk_organizer_id) references o_goto_organizer (id);
create index idx_goto_meet_organizer_idx on o_goto_meeting(fk_organizer_id);


create table o_goto_registrant (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   g_status varchar(16) default null,
   g_join_url varchar(1024) default null,
   g_confirm_url varchar(1024) default null,
   g_registrant_key varchar(64) default null,
   fk_meeting_id int8 not null,
   fk_identity_id int8 not null,
   primary key (id)
);

alter table o_goto_registrant add constraint goto_regis_meeting_idx foreign key (fk_meeting_id) references o_goto_meeting (id);
create index idx_goto_regis_meeting_idx on o_goto_registrant(fk_meeting_id);
alter table o_goto_registrant add constraint goto_regis_ident_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_goto_regis_ident_idx on o_goto_registrant(fk_identity_id);


create table o_vid_transcoding (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   vid_resolution int8 default null,
   vid_width int8 default null,
   vid_height int8 default null,
   vid_size int8 default null,
   vid_format varchar(128) default null,
   vid_status int8 default null,
   vid_transcoder varchar(128) default null,
   fk_resource_id int8 not null,
   primary key (id)
);
alter table o_vid_transcoding add constraint fk_resource_id_idx foreign key (fk_resource_id) references o_olatresource (resource_id);
create index idx_vid_trans_resource_idx on o_vid_transcoding(fk_resource_id);

create index vid_status_trans_idx on o_vid_transcoding(vid_status);
create index vid_transcoder_trans_idx on o_vid_transcoding(vid_transcoder);

