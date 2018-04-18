drop index idx_response_identifier_idx on o_qti_assessment_response;
alter table o_qti_assessment_response modify q_responseidentifier varchar(256);
create index idx_response_identifier_idx on o_qti_assessment_response (q_responseidentifier(255));

alter table o_bs_authentication add column lastmodified datetime;
update o_bs_authentication set lastmodified=creationdate;
alter table o_bs_authentication modify lastmodified datetime not null;
