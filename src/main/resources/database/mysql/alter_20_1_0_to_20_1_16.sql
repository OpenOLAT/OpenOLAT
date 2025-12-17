create table o_noti_mail (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   n_last_mail datetime,
   n_next_mail datetime,
   fk_identity bigint not null,
   primary key (id)
);
alter table o_noti_mail ENGINE = InnoDB;

alter table o_noti_mail add constraint noti_mail_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);