-- Reminder 
alter table o_rem_reminder add r_email_copy varchar(32);
alter table o_rem_reminder add r_email_custom_copy varchar(1024);

-- Video Meta Data
alter table o_vid_metadata add vid_download_enabled number default 0 not null;