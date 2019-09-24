-- Absence notices
create table o_lecture_absence_category (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_title varchar(255),
   l_descr text,
   primary key (id)
);

create table o_lecture_absence_notice (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   l_type varchar(32),
   l_absence_reason text,
   l_absence_authorized bool default null,
   l_start_date timestamp not null,
   l_end_date timestamp not null,
   l_target varchar(32) default 'allentries' not null,
   l_attachments_dir varchar(255),
   fk_identity int8 not null,
   fk_notifier int8,
   fk_authorizer int8,
   fk_absence_category int8,
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
   id bigserial,
   creationdate timestamp not null,
   fk_lecture_block int8 not null,
   fk_absence_notice int8 not null,
   primary key (id)
);

alter table o_lecture_notice_to_block add constraint notice_to_block_idx foreign key (fk_lecture_block) references o_lecture_block (id);
create index idx_notice_to_block_idx on o_lecture_notice_to_block (fk_lecture_block);
alter table o_lecture_notice_to_block add constraint notice_to_notice_idx foreign key (fk_absence_notice) references o_lecture_absence_notice (id);
create index idx_notice_to_notice_idx on o_lecture_notice_to_block (fk_absence_notice);

create table o_lecture_notice_to_entry (
   id bigserial,
   creationdate timestamp not null,
   fk_entry int8 not null,
   fk_absence_notice int8 not null,
   primary key (id)
);

alter table o_lecture_notice_to_entry add constraint notice_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
create index idx_notice_to_entry_idx on o_lecture_notice_to_entry (fk_entry);
alter table o_lecture_notice_to_entry add constraint rel_notice_e_to_notice_idx foreign key (fk_absence_notice) references o_lecture_absence_notice (id);
create index idx_rel_notice_e_to_notice_idx on o_lecture_notice_to_entry (fk_absence_notice);


alter table o_lecture_block_roll_call add column fk_absence_category bigint default null;
alter table o_lecture_block_roll_call add constraint absence_category_idx foreign key (fk_absence_category) references o_lecture_absence_category (id);
create index idx_absence_category_idx on o_lecture_block_roll_call (fk_absence_category);

alter table o_lecture_block_roll_call add column l_absence_notice_lectures varchar(128);
alter table o_lecture_block_roll_call add column fk_absence_notice bigint default null;
alter table o_lecture_block_roll_call add constraint rollcall_to_notice_idx foreign key (fk_absence_notice) references o_lecture_absence_notice (id);
create index idx_rollcall_to_notice_idx on o_lecture_block_roll_call (fk_absence_notice);

alter table o_lecture_block_audit_log add column fk_absence_notice int8 default null;

-- curriculum
update o_cur_curriculum set c_status='active' where c_status is null;

-- portfolio
alter table o_pf_media alter column p_business_path drop not null;

-- paypal checkout
create table o_ac_checkout_transaction (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   p_success_uuid varchar(64) not null,
   p_cancel_uuid varchar(64) not null,
   p_order_nr varchar(64) not null,
   p_order_id int8 not null,
   p_order_part_id int8 not null,
   p_method_id int8 not null,
   p_amount_currency_code varchar(3) not null,
   p_amount_amount decimal not null,
   p_status varchar(32) not null,
   p_paypal_order_id varchar(64),
   p_paypal_order_status varchar(64),
   p_paypal_order_status_reason text,
   p_paypal_authorization_id varchar(64),
   p_paypal_capture_id varchar(64),
   p_capture_currency_code varchar(3),
   p_capture_amount decimal,
   p_paypal_invoice_id varchar(64),
   primary key (id)
);




