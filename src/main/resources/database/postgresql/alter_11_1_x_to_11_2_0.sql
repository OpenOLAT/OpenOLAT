create table o_pf_binder_user_infos (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_initiallaunchdate timestamp,
   p_recentlaunchdate timestamp,
   p_visit int4,
   fk_identity int8,
   fk_binder int8,
   unique(fk_identity, fk_binder),
   primary key (id)
);

alter table o_pf_binder_user_infos add constraint binder_user_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_binder_user_to_ident_idx on o_pf_binder_user_infos (fk_identity);
alter table o_pf_binder_user_infos add constraint binder_user_binder_idx foreign key (fk_binder) references o_pf_binder (id);
create index idx_binder_user_binder_idx on o_pf_binder_user_infos (fk_binder);


alter table o_repositoryentry add column deletiondate timestamp default null;
alter table o_repositoryentry add column fk_deleted_by int8 default null;
alter table o_repositoryentry add constraint re_deleted_to_identity_idx foreign key (fk_deleted_by) references o_bs_identity (id);
create index idx_re_deleted_to_identity_idx on o_repositoryentry (fk_deleted_by);


alter table o_pf_assignment add column fk_form_entry_id int8 default null;
alter table o_pf_assignment add constraint pf_assign_form_idx foreign key (fk_form_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_pf_assign_form_idx on o_pf_assignment (fk_form_entry_id);

alter table o_pf_assignment add column p_only_auto_eva bool default true;
alter table o_pf_assignment add column p_reviewer_see_auto_eva bool default false;
alter table o_pf_assignment add column p_anon_extern_eva bool default true;

alter table o_pf_page add column p_editable bool default true;

alter table o_pf_page_part add column fk_form_entry_id int8 default null;
alter table o_pf_page_part add constraint pf_part_form_idx foreign key (fk_form_entry_id) references o_repositoryentry (repositoryentry_id);
create index idx_pf_part_form_idx on o_pf_page_part (fk_form_entry_id);


create table o_eva_form_session (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   e_status varchar(16),
   e_submission_date timestamp,
   e_first_submission_date timestamp,
   fk_identity int8 not null,
   fk_page_body int8,
   fk_form_entry int8 not null,
   primary key (id)
);

alter table o_eva_form_session add constraint eva_session_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_eva_session_to_ident_idx on o_eva_form_session (fk_identity);
alter table o_eva_form_session add constraint eva_session_to_body_idx foreign key (fk_page_body) references o_pf_page_body (id);
create index idx_eva_session_to_body_idx on o_eva_form_session (fk_page_body);
alter table o_eva_form_session add constraint eva_session_to_form_idx foreign key (fk_form_entry) references o_repositoryentry (repositoryentry_id);
create index idx_eva_session_to_form_idx on o_eva_form_session (fk_form_entry);

create table o_eva_form_response (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   e_responseidentifier varchar(64) not null,
   e_responsedatatype varchar(16) not null,
   e_numericalresponse decimal default null,
   e_stringuifiedresponse text,
   fk_session int8 not null,
   primary key (id)
);

alter table o_eva_form_response add constraint eva_resp_to_sess_idx foreign key (fk_session) references o_eva_form_session (id);
create index idx_eva_resp_to_sess_idx on o_eva_form_response (fk_session);


