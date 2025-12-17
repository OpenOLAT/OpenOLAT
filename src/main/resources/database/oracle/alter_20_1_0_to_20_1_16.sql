create table o_noti_mail (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   lastmodified date not null,
   n_last_mail date,
   n_next_mail date,
   fk_identity number(20) not null,
   primary key (id)
);

alter table o_noti_mail add constraint noti_mail_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_noti_mail_to_identity_idx on o_noti_mail (fk_identity);