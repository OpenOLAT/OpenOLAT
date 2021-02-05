-- Portfolio
alter table o_pf_page_body add column p_usage bigint default 1;
alter table o_pf_page_body add column p_synthetic_status varchar(32);
