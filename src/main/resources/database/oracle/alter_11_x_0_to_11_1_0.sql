create table o_forum_pseudonym (
   id number(20) GENERATED ALWAYS AS IDENTITY,
   creationdate date not null,
   p_pseudonym varchar2(255 char) not null,
   p_credential varchar2(255 char) not null,
   p_salt varchar2(255 char) not null,
   p_hashalgorithm varchar2(16 char) not null,
   primary key (id)
);

create index forum_pseudonym_idx on o_forum_pseudonym (p_pseudonym);
create index forum_msg_pseudonym_idx on o_message (pseudonym);
