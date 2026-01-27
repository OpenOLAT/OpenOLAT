alter table o_cur_curriculum_element modify c_external_id varchar(128);

-- Video meta data
alter table o_vid_metadata add vid_master_replaced number default 0 not null;
