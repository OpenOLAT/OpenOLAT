-- Document editor
drop table o_wopi_access;
create table o_de_access (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   o_editor_type varchar(64) not null,
   o_expires_at timestamp not null,
   o_mode varchar(64) not null,
   o_version_controlled bool not null,
   fk_metadata bigint not null,
   fk_identity bigint not null,
   primary key (id)
);

create table o_de_user_info (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   o_info varchar(2048) not null,
   fk_identity bigint not null,
   primary key (id)
);

create unique index idx_de_userinfo_ident_idx on o_de_user_info(fk_identity);


-- Assessment
alter table o_as_entry add column a_current_run_start timestamp;


-- Disadvantage compensation
alter table o_qti_assessmenttest_session add column q_compensation_extra_time int8;

create table o_as_compensation (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   a_subident varchar(512),
   a_subident_name varchar(512),
   a_extra_time int8 not null,
   a_approved_by varchar(2000),
   a_approval timestamp,
   a_status varchar(32),
   fk_identity bigint not null,
   fk_creator bigint not null,
   fk_entry bigint not null,
   primary key (id)
);

alter table o_as_compensation add constraint compensation_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_compensation_ident_idx on o_as_compensation(fk_identity);
alter table o_as_compensation add constraint compensation_crea_idx foreign key (fk_creator) references o_bs_identity (id);
create index idx_compensation_crea_idx on o_as_compensation(fk_creator);
alter table o_as_compensation add constraint compensation_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_compensation_entry_idx on o_as_compensation(fk_entry);


create table o_as_compensation_log (
   id bigserial,
   creationdate timestamp not null,
   a_action varchar(32) not null,
   a_val_before text,
   a_val_after text,
   a_subident varchar(512),
   fk_entry_id bigint not null,
   fk_identity_id bigint not null,
   fk_compensation_id bigint not null,
   fk_author_id bigint,
   primary key (id)
);

create index comp_log_entry_idx on o_as_compensation_log (fk_entry_id);
create index comp_log_ident_idx on o_as_compensation_log (fk_identity_id);





