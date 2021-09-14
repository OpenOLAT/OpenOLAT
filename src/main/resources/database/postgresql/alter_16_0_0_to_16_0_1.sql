-- Immunity Proof
create table o_immunity_proof (
   id bigserial,
   creationdate timestamp not null,
   fk_user int8 not null,
   safedate timestamp not null,
   validated boolean not null,
   send_mail boolean not null,
   email_sent boolean not null default false,
   primary key (id)
);

alter table o_immunity_proof add constraint immunity_proof_to_user_idx foreign key (fk_user) references o_bs_identity(id);
create unique index idx_immunity_proof on o_immunity_proof (fk_user);