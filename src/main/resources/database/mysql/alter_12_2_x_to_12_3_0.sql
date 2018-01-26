-- QPool
alter table o_qp_item add column q_topic varchar(1024);
alter table o_qp_item add column q_creator varchar(1024);
alter table o_qp_item add column q_status_last_modified datetime;

create table o_qp_item_audit_log (
  id bigint not null auto_increment,
  creationdate datetime not null,
  q_action varchar(64),
  q_val_before mediumtext,
  q_val_after mediumtext,
  q_message mediumtext,
  fk_author_id bigint,
  fk_item_id bigint,
  primary key (id)
);

create index idx_tax_level_path_key_idx on o_tax_taxonomy_level (t_m_path_keys);
create index idx_item_audit_item_idx on o_qp_item_audit_log (fk_item_id);
create index rtn_rating_res_idx on o_userrating (resid, resname, creator_id, rating);



alter table o_as_entry add column a_current_run_completion float(65,30);
alter table o_as_entry add column a_current_run_status varchar(16);



alter table o_qti_assessmenttest_session add column q_num_questions bigint;
alter table o_qti_assessmenttest_session add column q_num_answered_questions bigint;
alter table o_qti_assessmenttest_session add column q_extra_time bigint;


-- dialog elements
create table o_dialog_element (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  d_filename varchar(2048),
  d_filesize bigint,
  d_subident varchar(64) not null,
  fk_author bigint,
  fk_entry bigint not null,
  fk_forum bigint not null,
  primary key (id)
);

alter table o_dialog_element add constraint dial_el_author_idx foreign key (fk_author) references o_bs_identity (id);
alter table o_dialog_element add constraint dial_el_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_dialog_element add constraint dial_el_forum_idx foreign key (fk_forum) references o_forum (forum_id);
create index idx_dial_el_subident_idx on o_dialog_element (d_subident);

-- portfolio
create table o_pf_page_user_infos (
  id bigint not null auto_increment,
  creationdate datetime not null,
  lastmodified datetime not null,
  p_mark bit default 0,
  p_status varchar(16) not null default 'incoming',
  p_recentlaunchdate datetime not null,
  fk_identity_id bigint not null,
  fk_page_id bigint not null,
  primary key (id)
);

alter table o_pf_page_user_infos add constraint user_pfpage_idx foreign key (fk_identity_id) references o_bs_identity (id);
alter table o_pf_page_user_infos add constraint page_pfpage_idx foreign key (fk_page_id) references o_pf_page (id);



