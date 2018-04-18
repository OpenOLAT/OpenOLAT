alter table o_qti_assessment_response modify q_responseidentifier varchar2(255 char);

alter table o_bs_authentication add lastmodified date;
update o_bs_authentication set lastmodified=creationdate;
alter table o_bs_authentication modify lastmodified date not null;
