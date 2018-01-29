-- QPool
alter table o_qp_item add column q_topic varchar(1024);
alter table o_qp_item add column q_creator varchar(1024);
alter table o_qp_item add column q_status_last_modified timestamp;

create table o_qp_item_audit_log (
  id bigserial,
  creationdate timestamp not null,
  q_action varchar(64),
  q_val_before text,
  q_val_after text,
  q_message text,
  fk_author_id int8,
  fk_item_id int8,
  primary key (id)
);

create index idx_tax_level_path_key_idx on o_tax_taxonomy_level (t_m_path_keys);
create index idx_item_audit_item_idx on o_qp_item_audit_log (fk_item_id);
create index userrating_rating_res_idx on o_userrating (resid, resname, creator_id, rating);


alter table o_as_entry add column a_current_run_completion float(24);
alter table o_as_entry add column a_current_run_status varchar(16);


alter table o_qti_assessmenttest_session add column q_num_questions int8;
alter table o_qti_assessmenttest_session add column q_num_answered_questions int8;
alter table o_qti_assessmenttest_session add column q_extra_time int8;

-- dialog elements
create table o_dialog_element (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  d_filename varchar(2048),
  d_filesize int8,
  d_subident varchar(64) not null,
  fk_author int8,
  fk_entry int8 not null,
  fk_forum int8 not null,
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
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  p_mark bool default false,
  p_status varchar(16) not null default 'incoming',
  p_recentlaunchdate timestamp not null,
  fk_identity_id int8 not null,
  fk_page_id int8 not null,
  primary key (id)
);

alter table o_pf_page_user_infos add constraint user_pfpage_idx foreign key (fk_identity_id) references o_bs_identity (id);
create index idx_user_pfpage_idx on o_pf_page_user_infos (fk_identity_id);
alter table o_pf_page_user_infos add constraint page_pfpage_idx foreign key (fk_page_id) references o_pf_page (id);
create index idx_page_pfpage_idx on o_pf_page_user_infos (fk_page_id);


-- index o_user
create index xx_idx_email_low_text on o_user(lower(u_email) text_pattern_ops);
create index xx_idx_institutionalemail_low_text on o_user(lower(u_institutionalemail) text_pattern_ops);
create index xx_idx_username_low_text on o_bs_identity(lower(name) text_pattern_ops);








