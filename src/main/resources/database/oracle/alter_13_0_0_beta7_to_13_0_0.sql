-- portfolio
alter table o_pf_page_part add p_layout_options_large clob;
update o_pf_page_part set p_layout_options_large = p_layout_options;
alter table o_pf_page_part rename column p_layout_options to p_layout_options_old;
alter table o_pf_page_part rename column p_layout_options_large to p_layout_options;
alter table o_pf_page_part modify p_layout_options_old invisible;
