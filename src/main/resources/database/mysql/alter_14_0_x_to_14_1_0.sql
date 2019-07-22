-- Absence notices
create table o_lecture_absence_category (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   l_title varchar(255),
   l_descr mediumtext,
   primary key (id)
);

alter table o_lecture_block_roll_call add column fk_absence_category bigint default null;
alter table o_lecture_block_roll_call add constraint absence_category_idx foreign key (fk_absence_category) references o_lecture_absence_category (id);
