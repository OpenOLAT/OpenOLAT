-- quality management
alter table o_qual_context add q_location varchar(1024);

create table o_qual_analysis_presentation (
   id bigint not null auto_increment,
   creationdate datetime not null,
   lastmodified datetime not null,
   q_name varchar(256),
   q_analysis_segment varchar(100),
   q_search_params text,
   q_heatmap_grouping text,
   q_heatmap_insufficient_only number boolean default false,
   fk_form_entry bigint not null,
   primary key (id)
);
alter table o_qual_analysis_presentation ENGINE = InnoDB;

-- portfolio
alter table o_pf_page_part modify p_layout_options mediumtext;

