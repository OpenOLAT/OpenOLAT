drop index idx_response_identifier_idx on o_qti_assessment_response;
alter table o_qti_assessment_response modify q_responseidentifier varchar(256);
create index idx_response_identifier_idx on o_qti_assessment_response (q_responseidentifier(255));

alter table o_bs_authentication add column lastmodified datetime;
update o_bs_authentication set lastmodified=creationdate;
alter table o_bs_authentication modify lastmodified datetime not null;

create table o_bs_authentication_history (
   id bigint not null auto_increment,
   creationdate datetime,
   provider varchar(8),
   authusername varchar(255),
   credential varchar(255),
   salt varchar(255) default null,
   hashalgorithm varchar(16) default null,
   fk_identity bigint not null,
   primary key (id)
);
alter table o_bs_authentication_history ENGINE = InnoDB;

alter table o_bs_authentication_history add constraint auth_hist_to_ident_idx foreign key (fk_identity) references o_bs_identity (id);


create table o_csp_log (
   id bigint not null auto_increment,
   creationdate datetime,
   l_blocked_uri varchar(1024),
   l_disposition varchar(32),
   l_document_uri varchar(1024),
   l_effective_directive mediumtext,
   l_original_policy mediumtext,
   l_referrer varchar(1024),
   l_script_sample mediumtext,
   l_status_code varchar(1024),
   l_violated_directive varchar(1024),
   l_source_file varchar(1024),
   l_line_number bigint,
   l_column_number bigint,
   fk_identity bigint,
   primary key (id)
);
alter table o_csp_log ENGINE = InnoDB;

create index idx_csp_log_to_ident_idx on o_csp_log (fk_identity);
