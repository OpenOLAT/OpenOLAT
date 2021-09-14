-- Immunity Proof
create table o_immunity_proof (
	id number(20) generated  always as identity,
	creationdate date not null,
	fk_user number(20) not null,
	safedate date not null,
	validated number default 0 not null,
	send_mail default 1 not null,
	email_sent default 0 not null,
	primary key (id)
);

alter table o_immunity_proof add constraint proof_to_user_idx foreign key (fk_user) references o_bs_user (id);
create index idx_proof_to_user_idx on o_immunity_proof (fk_user);