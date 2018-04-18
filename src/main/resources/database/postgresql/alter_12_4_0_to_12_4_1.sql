alter table o_qti_assessment_response alter column q_responseidentifier type character varying(255);

alter table o_bs_authentication add column lastmodified timestamp;
update o_bs_authentication set lastmodified=creationdate;
alter table o_bs_authentication alter column lastmodified set not null;
