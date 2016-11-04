create table o_forum_pseudonym (
   id bigserial,
   creationdate timestamp not null,
   p_pseudonym varchar(255) not null,
   p_credential varchar(255) not null,
   p_salt varchar(255) not null,
   p_hashalgorithm varchar(16) not null,
   primary key (id)
);

create index forum_pseudonym_idx on o_forum_pseudonym (p_pseudonym);
create index forum_msg_pseudonym_idx on o_message (pseudonym);