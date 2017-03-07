create table o_vid_metadata (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
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



alter table o_user add column u_smstelmobile varchar(255);

create table o_sms_message_log (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   s_message_uuid varchar(256) not null,
   s_server_response varchar(256),
   s_service_id varchar(32) not null,
   fk_identity bigint not null,
   primary key (id)
);
alter table o_sms_message_log ENGINE = InnoDB;

alter table o_sms_message_log add constraint sms_log_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);


alter table o_qti_assessmentitem_session modify q_itemidentifier varchar(255);
alter table o_qti_assessmentitem_session modify q_sectionidentifier varchar(255);
alter table o_qti_assessmentitem_session modify q_testpartidentifier varchar(255);
alter table o_qti_assessmenttest_session modify q_subident varchar(255);


alter table o_as_entry add column a_user_visibility bit default 1;

