-- Reminder 
alter table o_rem_reminder add r_email_copy varchar(32);
alter table o_rem_reminder add r_email_custom_copy varchar(1024);

-- Video Meta Data
alter table o_vid_metadata add vid_download_enabled number default 0 not null;

-- Course styles
create table o_course_color_category (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  c_identifier varchar2(128) not null,
  c_type varchar2(16) not null,
  c_sort_order number(20) not null,
  c_enabled number not null,
  c_css_class varchar2(128),
  primary key (id)
);

create unique index idx_course_colcat_ident on o_course_color_category (c_identifier);
