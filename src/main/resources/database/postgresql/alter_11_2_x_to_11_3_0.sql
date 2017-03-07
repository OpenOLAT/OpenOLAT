create table o_vid_metadata (
  id bigserial not null,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  vid_width int8 default null,
  vid_height int8 default null,
  vid_size int8 default null,
  vid_format varchar(32) default null,
  vid_length varchar(32) default null,
  fk_resource_id int8 not null,
  primary key (id)
);

alter table o_vid_metadata add constraint vid_meta_rsrc_idx foreign key (fk_resource_id) references o_olatresource (resource_id);
create index idx_vid_meta_rsrc_idx on o_vid_metadata(fk_resource_id);




alter table o_user add column u_smstelmobile varchar(255);

create table o_sms_message_log (
   id bigserial not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   s_message_uuid varchar(256) not null,
   s_server_response varchar(256),
   s_service_id varchar(32) not null,
   fk_identity int8 not null,
   primary key (id)
);

alter table o_sms_message_log add constraint sms_log_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_sms_log_to_identity_idx on o_sms_message_log(fk_identity);



alter table o_qti_assessmentitem_session alter column q_itemidentifier type varchar(255);
alter table o_qti_assessmentitem_session alter column q_sectionidentifier type varchar(255);
alter table o_qti_assessmentitem_session alter column q_testpartidentifier type varchar(255);
alter table o_qti_assessmenttest_session alter column q_subident type varchar(255);


alter table o_as_entry add column a_user_visibility bool default true;


