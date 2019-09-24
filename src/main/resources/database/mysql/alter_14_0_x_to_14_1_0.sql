-- Absence notices
create table o_lecture_absence_category (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   l_title varchar(255),
   l_descr mediumtext,
   primary key (id)
);

alter table o_lecture_absence_category ENGINE = InnoDB;

create table o_lecture_absence_notice (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   l_type varchar(32),
   l_absence_reason mediumtext,
   l_absence_authorized bit default null,
   l_start_date datetime not null,
   l_end_date datetime not null,
   l_target varchar(32) default 'allentries' not null,
   l_attachments_dir varchar(255),
   fk_identity bigint not null,
   fk_notifier bigint,
   fk_authorizer bigint,
   fk_absence_category bigint,
   primary key (id)
);

alter table o_lecture_absence_notice ENGINE = InnoDB;

alter table o_lecture_absence_notice add constraint notice_identity_idx foreign key (fk_identity) references o_bs_identity (id);
alter table o_lecture_absence_notice add constraint notice_notif_identity_idx foreign key (fk_notifier) references o_bs_identity (id);
alter table o_lecture_absence_notice add constraint notice_auth_identity_idx foreign key (fk_authorizer) references o_bs_identity (id);
alter table o_lecture_absence_notice add constraint notice_category_idx foreign key (fk_absence_category) references o_lecture_absence_category (id);


create table o_lecture_notice_to_block (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_lecture_block bigint not null,
   fk_absence_notice bigint not null,
   primary key (id)
);
alter table o_lecture_notice_to_block ENGINE = InnoDB;

alter table o_lecture_notice_to_block add constraint notice_to_block_idx foreign key (fk_lecture_block) references o_lecture_block (id);
alter table o_lecture_notice_to_block add constraint notice_to_notice_idx foreign key (fk_absence_notice) references o_lecture_absence_notice (id);


create table o_lecture_notice_to_entry (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_entry bigint not null,
   fk_absence_notice bigint not null,
   primary key (id)
);
alter table o_lecture_notice_to_entry ENGINE = InnoDB;

alter table o_lecture_notice_to_entry add constraint notice_to_entry_idx foreign key (fk_entry) references o_repositoryentry (repositoryentry_id);
alter table o_lecture_notice_to_entry add constraint rel_notice_e_to_notice_idx foreign key (fk_absence_notice) references o_lecture_absence_notice (id);


alter table o_lecture_block_roll_call add column fk_absence_category bigint default null;
alter table o_lecture_block_roll_call add constraint absence_category_idx foreign key (fk_absence_category) references o_lecture_absence_category (id);

alter table o_lecture_block_roll_call add column l_absence_notice_lectures varchar(128);
alter table o_lecture_block_roll_call add column fk_absence_notice bigint default null;
alter table o_lecture_block_roll_call add constraint rollcall_to_notice_idx foreign key (fk_absence_notice) references o_lecture_absence_notice (id);

alter table o_lecture_block_audit_log add column fk_absence_notice bigint default null;

-- curriculum
update o_cur_curriculum set c_status='active' where c_status is null;

-- portfolio
alter table o_pf_media modify p_business_path varchar(255) default null;

-- paypal checkout
create table o_ac_checkout_transaction (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   p_success_uuid varchar(64) not null,
   p_cancel_uuid varchar(64) not null,
   p_order_nr varchar(64) not null,
   p_order_id bigint not null,
   p_order_part_id bigint not null,
   p_method_id bigint not null,
   p_amount_currency_code varchar(3) not null,
   p_amount_amount decimal(12,4) not null,
   p_status varchar(32) not null,
   p_paypal_order_id varchar(64),
   p_paypal_order_status varchar(64),
   p_paypal_order_status_reason text,
   p_paypal_authorization_id varchar(64),
   p_paypal_capture_id varchar(64),
   p_capture_currency_code varchar(3),
   p_capture_amount decimal(12,4),
   p_paypal_invoice_id varchar(64),
   primary key (id)
);
