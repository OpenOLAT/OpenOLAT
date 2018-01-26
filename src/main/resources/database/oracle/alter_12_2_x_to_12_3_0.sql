-- QPool
alter table o_qp_item add column q_topic varchar2(1024);
alter table o_qp_item add column q_creator varchar2(1024);
alter table o_qp_item add column q_status_last_modified date;

create table o_qp_item_audit_log (
  id number(20) generated always as identity,
  creationdate date not null,
  q_action varchar2(64 char),
  q_val_before CLOB,
  q_val_after CLOB,
  q_message CLOB,
  fk_author_id number(20),
  fk_item_id number(20),
  primary key (id)
);

create index idx_tax_level_path_key_idx on o_tax_taxonomy_level (t_m_path_keys);
create index idx_item_audit_item_idx on o_qp_item_audit_log (fk_item_id);
create index userrating_rating_res_idx on o_userrating (resid, resname, creator_id, rating);


alter table o_as_entry add a_current_run_completion decimal;
alter table o_as_entry add a_current_run_status varchar2(16 char);


alter table o_qti_assessmenttest_session add q_num_questions number(20);
alter table o_qti_assessmenttest_session add q_num_answered_questions number(20);
alter table o_qti_assessmenttest_session add q_extra_time number(20);

-- dialog elements
create table o_dialog_element (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  d_filename varchar2(2048 char),
  d_filesize number(20),
  d_subident varchar2(64 char) not null,
  fk_author number(20),
  fk_entry number(20) not null,
  fk_forum number(20) not null,
  primary key (id)
);

alter table o_dialog_element add constraint dial_el_author_idx foreign key (fk_author) references o_bs_identity (id);
create index idx_dial_el_author_idx on o_dialog_element (fk_author);
alter table o_dialog_element add constraint dial_el_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_dial_el_entry_idx on o_dialog_element (fk_entry);
alter table o_dialog_element add constraint dial_el_forum_idx foreign key (fk_forum) references o_forum (forum_id);
create index idx_dial_el_forum_idx on o_dialog_element (fk_forum);
create index idx_dial_el_subident_idx on o_dialog_element (d_subident);


-- portfolio
create table o_pf_page_user_infos (
  id number(20) generated always as identity,
  creationdate date not null,
  lastmodified date not null,
  p_mark number default 0,
  p_status varchar2(16 char) default 'incoming' not null,
  p_recentlaunchdate date not null,
  fk_identity_id number(20) not null,
  fk_page_id number(20) not null,
  primary key (id)
);

alter table o_pf_page_user_infos add constraint user_pfpage_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_user_pfpage_idx on o_pf_page_user_infos (fk_identity_id);
alter table o_pf_page_user_infos add constraint page_pfpage_idx foreign key (fk_page_id) references o_pf_page (id);
create index idx_page_pfpage_idx on o_pf_page_user_infos (fk_page_id);


