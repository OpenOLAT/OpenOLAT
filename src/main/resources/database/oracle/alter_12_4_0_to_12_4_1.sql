alter table o_qti_assessment_response modify q_responseidentifier varchar2(255 char);

alter table o_bs_authentication add lastmodified date;
update o_bs_authentication set lastmodified=creationdate;
alter table o_bs_authentication modify lastmodified date not null;


create table o_bs_authentication_history (
   id number(20) generated always as identity,
   creationdate date,
   provider varchar(8),
   authusername varchar(255),
   credential varchar(255),
   salt varchar(255) default null,
   hashalgorithm varchar(16) default null,
   fk_identity number(20) not null,
   primary key (id)
);

alter table o_bs_authentication_history add constraint auth_hist_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_auth_hist_to_ident_idx on o_bs_authentication_history (fk_identity);
