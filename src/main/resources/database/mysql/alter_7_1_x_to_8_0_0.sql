-- mail preferences
alter table o_user add column receiverealmail varchar(16);

-- mail system
create table if not exists o_mail (
  mail_id bigint NOT NULL,
  meta_mail_id varchar(64),
  creationdate datetime,
	lastmodified datetime,
	resname varchar(50),
  resid bigint,
  ressubpath varchar(2048),
  businesspath varchar(2048),
  subject varchar(512),
  body longtext,
  fk_from_id bigint,
  primary key (mail_id)
);

-- mail recipient
create table if not exists o_mail_to_recipient (
  pos mediumint NOT NULL default 0,
  fk_mail_id bigint,
  fk_recipient_id bigint
);

create table if not exists o_mail_recipient (
  recipient_id bigint NOT NULL,
  recipientvisible bit,
  deleted bit,
  mailread bit,
  mailmarked bit,
  email varchar(255),
  recipientgroup varchar(255),
  creationdate datetime,
  fk_recipient_id bigint,
  primary key (recipient_id)
);

-- mail attachments
create table o_mail_attachment (
	attachment_id bigint NOT NULL,
  creationdate datetime,
	datas mediumblob,
	datas_size bigint,
	datas_name varchar(255),
	mimetype varchar(255),
  fk_att_mail_id bigint,
	primary key (attachment_id)
);

-- access control
create table  if not exists o_ac_offer (
	offer_id bigint NOT NULL,
  creationdate datetime,
	lastmodified datetime,
	is_valid bit default 1,
	validfrom datetime,
	validto datetime,
  version mediumint unsigned not null,
  resourceid bigint,
  resourcetypename varchar(255),
  resourcedisplayname varchar(255),
  token varchar(255),
  price_amount DECIMAL(12,4),
	price_currency_code VARCHAR(3),
	offer_desc VARCHAR(2000),
  fk_resource_id bigint,
	primary key (offer_id)
);

create table if not exists o_ac_method (
	method_id bigint NOT NULL,
	access_method varchar(32),
  version mediumint unsigned not null,
  creationdate datetime,
	lastmodified datetime,
	is_valid bit default 1,
	is_enabled bit default 1,
	validfrom datetime,
	validto datetime,
	primary key (method_id)
);

create table if not exists o_ac_offer_access (
	offer_method_id bigint NOT NULL,
  version mediumint unsigned not null,
  creationdate datetime,
	is_valid bit default 1,
	validfrom datetime,
	validto datetime,
  fk_offer_id bigint,
  fk_method_id bigint,
	primary key (offer_method_id)
);

-- access cart
create table if not exists o_ac_order (
	order_id bigint NOT NULL,
  version mediumint unsigned not null,
  creationdate datetime,
	lastmodified datetime,
	is_valid bit default 1,
	total_lines_amount DECIMAL(12,4),
	total_lines_currency_code VARCHAR(3),
	total_amount DECIMAL(12,4),
	total_currency_code VARCHAR(3),
	discount_amount DECIMAL(12,4),
	discount_currency_code VARCHAR(3),
	order_status VARCHAR(32) default 'NEW',
  fk_delivery_id bigint,
	primary key (order_id)
);

create table if not exists o_ac_order_part (
	order_part_id bigint NOT NULL,
  version mediumint unsigned not null,
  pos mediumint unsigned,
  creationdate datetime,
  total_lines_amount DECIMAL(12,4),
  total_lines_currency_code VARCHAR(3),
  total_amount DECIMAL(12,4),
  total_currency_code VARCHAR(3),
  fk_order_id bigint,
	primary key (order_part_id)
);

create table if not exists o_ac_order_line (
	order_item_id bigint NOT NULL,
  version mediumint unsigned not null,
  pos mediumint unsigned,
  creationdate datetime,
	unit_price_amount DECIMAL(12,4),
	unit_price_currency_code VARCHAR(3),
	total_amount DECIMAL(12,4),
	total_currency_code VARCHAR(3),
  fk_order_part_id bigint,
  fk_offer_id bigint,
	primary key (order_item_id)
);

create table if not exists o_ac_transaction (
	transaction_id bigint NOT NULL,
  version mediumint unsigned not null,
  creationdate datetime,
  trx_status VARCHAR(32) default 'NEW',
  amount_amount DECIMAL(12,4),
  amount_currency_code VARCHAR(3),
  fk_order_part_id bigint,
  fk_order_id bigint,
  fk_method_id bigint,
	primary key (transaction_id)
);

-- access control
alter table o_repositoryentry add column fk_tutorgroup bigint;
alter table o_repositoryentry add column fk_participantgroup bigint;
alter table o_repositoryentry add column membersonly bit default 0;
create index repo_members_only_idx on o_repositoryentry (membersonly);
alter table o_repositoryentry add constraint repo_tutor_sec_group_ctx foreign key (fk_tutorgroup) references o_bs_secgroup (id);
alter table o_repositoryentry add constraint repo_parti_sec_group_ctx foreign key (fk_participantgroup) references o_bs_secgroup (id);

alter table o_mail ENGINE = InnoDB;
alter table o_mail_to_recipient ENGINE = InnoDB;
alter table o_mail_recipient ENGINE = InnoDB;
alter table o_mail_attachment ENGINE = InnoDB;
alter table o_ac_offer ENGINE = InnoDB;
alter table o_ac_method ENGINE = InnoDB;
alter table o_ac_offer_access ENGINE = InnoDB;
alter table o_ac_order ENGINE = InnoDB;
alter table o_ac_order_part ENGINE = InnoDB;
alter table o_ac_order_line ENGINE = InnoDB;
alter table o_ac_transaction ENGINE = InnoDB;

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