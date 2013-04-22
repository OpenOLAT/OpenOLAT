-- mail preferences
alter table o_user add (receiverealmail varchar(16 char));

-- mail system
create table o_mail (
  mail_id number(20) not null,
  meta_mail_id varchar(64 char),
  creationdate date,
  lastmodified date,
  resname varchar(50 char),
  resid number(20),
  ressubpath varchar(2048 char),
  businesspath varchar(2048 char),
  subject varchar(512 char),
  body clob,
  fk_from_id number(20),
  primary key (mail_id)
);

create table o_mail_to_recipient (
  pos number(20) default 0,
  fk_mail_id number(20),
  fk_recipient_id number(20)
);

create table o_mail_recipient (
  recipient_id number(20) NOT NULL,
  recipientvisible number,
  deleted number,
  mailread number,
  mailmarked number,
  email varchar(255 char),
  recipientgroup varchar(255 char),
  creationdate date,
  fk_recipient_id number(20),
  primary key (recipient_id)
);

create table o_mail_attachment (
  attachment_id number(20) NOT NULL,
  creationdate date,
  datas blob,
  datas_size number(20),
  datas_name varchar(255 char),
  mimetype varchar(255 char),
  fk_att_mail_id number(20),
  primary key (attachment_id)
);

create table o_ac_offer (
  offer_id number(20) NOT NULL,
  creationdate date,
  lastmodified date,
  is_valid number default 1,
  validfrom date,
  validto date,
  version number(20) not null,
  resourceid number(20),
  resourcetypename varchar(255 char),
  resourcedisplayname varchar(255 char),
  token varchar(255 char),
  price_amount number(20,2),
  price_currency_code VARCHAR(3 char),
  offer_desc VARCHAR(2000 char),
  fk_resource_id number(20),
  primary key (offer_id)
);

create table o_ac_method (
  method_id number(20) NOT NULL,
  access_method varchar(32 char),
  version number(20) not null,
  creationdate date,
  lastmodified date,
  is_valid number default 1,
  is_enabled number default 1,
  validfrom date,
  validto date,
  primary key (method_id)
);

create table o_ac_offer_access (
  offer_method_id number(20) NOT NULL,
  version number(20) not null,
  creationdate date,
  is_valid number default 1,
  validfrom date,
  validto date,
  fk_offer_id number(20),
  fk_method_id number(20),
  primary key (offer_method_id)
);

create table o_ac_order (
  order_id number(20) NOT NULL,
  version number(20) not null,
  creationdate date,
  lastmodified date,
  is_valid number default 1,
  total_lines_amount number(20,2),
  total_lines_currency_code VARCHAR(3 char),
  total_amount number(20,2),
  total_currency_code VARCHAR(3 char),
  discount_amount number(20,2),
  discount_currency_code VARCHAR(3 char),
  order_status VARCHAR(32 char) default 'NEW',
  fk_delivery_id number(20),
  primary key (order_id)
);

create table o_ac_order_part (
  order_part_id number(20) NOT NULL,
  version number(20) not null,
  pos number(20),
  creationdate date,
  total_lines_amount number(20,2),
  total_lines_currency_code VARCHAR(3 char),
  total_amount number(20,2),
  total_currency_code VARCHAR(3 char),
  fk_order_id number(20),
  primary key (order_part_id)
);

create table o_ac_order_line (
  order_item_id number(20) NOT NULL,
  version number(20) not null,
  pos number(20),
  creationdate date,
  unit_price_amount number(20,2),
  unit_price_currency_code VARCHAR(3 char),
  total_amount number(20,2),
  total_currency_code VARCHAR(3 char),
  fk_order_part_id number(20),
  fk_offer_id number(20),
  primary key (order_item_id)
); 

create table o_ac_transaction (
  transaction_id number(20) NOT NULL,
  version number(20) not null,
  creationdate date,
  trx_status VARCHAR(32 char) default 'NEW',
  amount_amount number(20,2),
  amount_currency_code VARCHAR(3 char),
  fk_order_part_id number(20),
  fk_order_id number(20),
  fk_method_id number(20),
  primary key (transaction_id)
);

create table o_ac_reservation (
   reservation_id number(20) NOT NULL,
   creationdate date,
   lastmodified date,
   version number(20) not null,
   expirationdate date,
   reservationtype varchar(32),
   fk_identity number(20) not null,
   fk_resource number(20) not null,
   primary key (reservation_id)
);

create table o_ac_paypal_transaction (
   transaction_id number(20) not null,
   version number(20) not null,
   creationdate date,
   ref_no varchar(255 char),
   order_id number(20) not null,
   order_part_id number(20) not null,
   method_id number(20) not null,
   success_uuid varchar(32 char) not null,
   cancel_uuid varchar(32 char) not null,
   amount_amount DECIMAL,
   amount_currency_code VARCHAR(3 char),
   pay_response_date date,
   pay_key varchar(255 char),
   ack varchar(255 char),
   build varchar(255 char),
   coorelation_id varchar(255 char),
   payment_exec_status varchar(255 char),
   ipn_transaction_id varchar(255 char),
   ipn_transaction_status varchar(255 char),
   ipn_sender_transaction_id varchar(255 char),
   ipn_sender_transaction_status varchar(255 char),
   ipn_sender_email varchar(255 char),
   ipn_verify_sign varchar(255 char),
   ipn_pending_reason varchar(255 char),
   trx_status VARCHAR(32 char) default 'NEW' not null,
   trx_amount NUMBER (21,20),
   trx_currency_code VARCHAR(3 char),
   primary key (transaction_id)
);

-- access control
alter table o_repositoryentry add (fk_tutorgroup number(20));
alter table o_repositoryentry add (fk_participantgroup number(20));
alter table o_repositoryentry add (membersonly number default 0);
create index repo_members_only_idx on o_repositoryentry (membersonly);
alter table o_repositoryentry add constraint repo_tutor_sec_group_ctx foreign key (fk_tutorgroup) references o_bs_secgroup (id);
alter table o_repositoryentry add constraint repo_parti_sec_group_ctx foreign key (fk_participantgroup) references o_bs_secgroup (id);


alter table o_mail_to_recipient add constraint FKF86663165A4FA5DE foreign key (fk_mail_id) references o_mail (mail_id);
alter table o_mail_recipient add constraint FKF86663165A4FA5DG foreign key (fk_recipient_id) references o_bs_identity (id);
alter table o_mail add constraint FKF86663165A4FA5DC foreign key (fk_from_id) references o_mail_recipient (recipient_id);
alter table o_mail_to_recipient add constraint FKF86663165A4FA5DD foreign key (fk_recipient_id) references o_mail_recipient (recipient_id);
alter table o_mail_attachment add constraint FKF86663165A4FA5DF foreign key (fk_att_mail_id) references o_mail (mail_id);

create index ac_offer_to_resource_idx on o_ac_offer (fk_resource_id);
alter table o_ac_offer_access add constraint off_to_meth_meth_ctx foreign key (fk_method_id) references o_ac_method (method_id);
alter table o_ac_offer_access add constraint off_to_meth_off_ctx foreign key (fk_offer_id) references o_ac_offer (offer_id);
create index ac_order_to_delivery_idx on o_ac_order (fk_delivery_id);
alter table o_ac_order_part add constraint ord_part_ord_ctx foreign key (fk_order_id) references o_ac_order (order_id);
alter table o_ac_order_line add constraint ord_item_ord_part_ctx foreign key (fk_order_part_id) references o_ac_order_part (order_part_id);
alter table o_ac_order_line add constraint ord_item_offer_ctx foreign key (fk_offer_id) references o_ac_offer (offer_id);
alter table o_ac_transaction add constraint trans_ord_ctx foreign key (fk_order_id) references o_ac_order (order_id);
alter table o_ac_transaction add constraint trans_ord_part_ctx foreign key (fk_order_part_id) references o_ac_order_part (order_part_id);
alter table o_ac_transaction add constraint trans_method_ctx foreign key (fk_method_id) references o_ac_method (method_id);