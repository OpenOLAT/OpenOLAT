-- Absence notices
create table o_lecture_absence_category (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_title varchar(255),
   l_descr text,
   primary key (id)
);

alter table o_lecture_block_roll_call add column fk_absence_category bigint default null;
alter table o_lecture_block_roll_call add constraint absence_category_idx foreign key (fk_absence_category) references o_lecture_absence_category (id);
create index idx_absence_category_idx on o_lecture_block_roll_call (fk_absence_category);

