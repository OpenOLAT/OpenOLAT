-- Absence notices
create table o_lecture_absence_category (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_title varchar(255),
   l_descr CLOB,
   primary key (id)
);


create table o_lecture_absence_notice (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_type varchar(32),
   l_absence_reason CLOB,
   l_absence_authorized number default null,
   l_start_date timestamp,
   l_end_date timestamp,
   l_target varchar(32) default 'allentries' not null,
   l_attachments_dir varchar(255),
   fk_identity number(20) not null,
   fk_notifier number(20),
   fk_authorizer number(20),
   fk_absence_category number(20),
   primary key (id)
);

alter table o_lecture_absence_notice add constraint notice_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_notice_identity_idx on o_lecture_absence_notice (fk_identity);
alter table o_lecture_absence_notice add constraint notice_notif_identity_idx foreign key (fk_notifier) references o_bs_identity (id);
create index idx_notice_notif_identity_idx on o_lecture_absence_notice (fk_notifier);
alter table o_lecture_absence_notice add constraint notice_auth_identity_idx foreign key (fk_authorizer) references o_bs_identity (id);
create index idx_notice_auth_identity_idx on o_lecture_absence_notice (fk_authorizer);
alter table o_lecture_absence_notice add constraint notice_category_idx foreign key (fk_absence_category) references o_lecture_absence_category (id);
create index idx_notice_category_idx on o_lecture_absence_notice (fk_absence_category);


create table o_lecture_notice_to_block (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   fk_lecture_block number(20) not null,
   fk_absence_notice number(20) not null,
   primary key (id)
);

alter table o_lecture_notice_to_block add constraint notice_to_block_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_notice_to_block_idx on o_lecture_notice_to_block (fk_lecture_block);
alter table o_lecture_notice_to_block add constraint notice_to_notice_idx foreign key (fk_absence_notice) references o_lecture_absence_notice (id);
create index idx_notice_to_notice_idx on o_lecture_notice_to_block (fk_absence_notice);

create table o_lecture_notice_to_entry (
   id number(20) generated always as identity,
   creationdate timestamp not null,
   fk_entry number(20) not null,
   fk_absence_notice number(20) not null,
   primary key (id)
);

alter table o_lecture_notice_to_entry add constraint notice_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_notice_to_entry_idx on o_lecture_notice_to_entry (fk_entry);
alter table o_lecture_notice_to_entry add constraint rel_notice_e_to_notice_idx foreign key (fk_absence_notice) references o_lecture_absence_notice (id);
create index idx_rel_notice_e_to_notice_idx on o_lecture_notice_to_entry (fk_absence_notice);


alter table o_lecture_block_roll_call add fk_absence_category number(20) default null;
alter table o_lecture_block_roll_call add constraint absence_category_idx foreign key (fk_absence_category) references o_lecture_absence_category (id);
create index idx_absence_category_idx on o_lecture_block_roll_call (fk_absence_category);

alter table o_lecture_block_roll_call add l_absence_notice_lectures varchar(128);
alter table o_lecture_block_roll_call add fk_absence_notice number(20) default null;
alter table o_lecture_block_roll_call add constraint rollcall_to_notice_idx foreign key (fk_absence_notice) references o_lecture_absence_notice (id);
create index idx_rollcall_to_notice_idx on o_lecture_block_roll_call (fk_absence_notice);
