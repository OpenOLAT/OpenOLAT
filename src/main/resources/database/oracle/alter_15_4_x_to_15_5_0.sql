-- Portfolio
alter table o_pf_page_body add p_usage number(20) default 1;
alter table o_pf_page_body add p_synthetic_status varchar(32);
