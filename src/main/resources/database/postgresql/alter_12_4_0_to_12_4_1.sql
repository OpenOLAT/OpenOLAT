alter table o_qti_assessment_response alter column q_responseidentifier type character varying(255);

alter table o_bs_authentication add column lastmodified timestamp;
update o_bs_authentication set lastmodified=creationdate;
alter table o_bs_authentication alter column lastmodified set not null;


create table o_bs_authentication_history (
   id bigserial not null,
   creationdate timestamp,
   provider varchar(8),
   authusername varchar(255),
   credential varchar(255),
   salt varchar(255) default null,
   hashalgorithm varchar(16) default null,
   fk_identity int8 not null,
   primary key (id)
);

alter table o_bs_authentication_history add constraint auth_hist_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);
create index idx_auth_hist_to_ident_idx on o_bs_authentication_history (fk_identity);
