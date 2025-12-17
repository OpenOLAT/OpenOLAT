create table o_noti_mail (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   n_last_mail timestamp,
   n_next_mail timestamp,
   fk_identity int8 not null,
   primary key (id)
);

alter table o_noti_mail add constraint noti_mail_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_noti_mail_to_identity_idx on o_noti_mail (fk_identity);