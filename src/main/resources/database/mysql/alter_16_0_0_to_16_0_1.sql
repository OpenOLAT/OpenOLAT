-- Immunity Proof
create table o_immunity_proof (
   id bigint not null auto_increment,
   creationdate datetime not null,
   fk_user int8 not null,
   safedate datetime not null,
   validated bool not null,
   send_mail bool not null,
   email_sent bool not null default false,
   primary key (id)
);

alter table o_immunity_proof ENGINE = InnoDB;

alter table o_immunity_proof add constraint immunity_proof_to_user_idx foreign key (fk_user) references o_bs_identity(id);
create unique index idx_immunity_proof on o_immunity_proof (fk_user);