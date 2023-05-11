alter table o_bs_authentication add column externalid varchar(255);
alter table o_bs_authentication add constraint unique_pro_iss_externalid unique (provider, issuer, externalid);