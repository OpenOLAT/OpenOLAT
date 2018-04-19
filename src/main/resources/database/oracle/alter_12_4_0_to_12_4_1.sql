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


create table o_csp_log (
   id number(20) generated always as identity,
   creationdate date,
   l_blocked_uri varchar(1024),
   l_disposition varchar(32),
   l_document_uri varchar(1024),
   l_effective_directive CLOB,
   l_original_policy CLOB,
   l_referrer varchar(1024),
   l_script_sample CLOB,
   l_status_code varchar(1024),
   l_violated_directive varchar(1024),
   l_source_file varchar(1024),
   l_line_number number(20),
   l_column_number number(20),
   fk_identity number(20),
   primary key (id)
);

create index idx_csp_log_to_ident_idx on o_csp_log (fk_identity);
