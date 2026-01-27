alter table o_cur_curriculum_element alter column c_external_id type varchar(128);

-- Video meta data
alter table o_vid_metadata add column vid_master_replaced boolean not null default false;
