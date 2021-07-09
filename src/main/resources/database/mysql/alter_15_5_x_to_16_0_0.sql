-- Reminder 
alter table o_rem_reminder add column r_email_copy varchar(32);
alter table o_rem_reminder add column r_email_custom_copy varchar(1024);

-- Video Meta Data
alter table o_vid_metadata add column vid_download_enabled boolean not null default false;

-- Course styles
create table o_course_color_category (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   c_identifier varchar(128) not null,
   c_type varchar(16) not null,
   c_sort_order int not null,
   c_enabled bool not null default true,
   c_css_class varchar(128),
   primary key (id)
);
alter table o_course_color_category ENGINE = InnoDB;

create unique index idx_course_colcat_ident on o_course_color_category (c_identifier);
