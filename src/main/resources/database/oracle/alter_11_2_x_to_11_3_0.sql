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


alter table o_user add u_smstelmobile varchar2(255 char);

create table o_sms_message_log (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   s_message_uuid varchar2(256 char) not null,
   s_server_response varchar2(256 char),
   s_service_id varchar2(32 char) not null,
   fk_identity number(20) not null,
   primary key (id)
);

alter table o_sms_message_log add constraint sms_log_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_sms_log_to_identity_idx on o_sms_message_log(fk_identity);



alter table o_qti_assessmentitem_session modify q_itemidentifier varchar2(255 char);
alter table o_qti_assessmentitem_session modify q_sectionidentifier varchar2(255 char);
alter table o_qti_assessmentitem_session modify q_testpartidentifier varchar2(255 char);
alter table o_qti_assessmenttest_session modify q_subident varchar2(255 char);


alter table o_as_entry add a_user_visibility number default 1;
