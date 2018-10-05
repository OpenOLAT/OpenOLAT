-- quality management
alter table o_qual_context add q_location varchar(1024);

create table o_qual_analysis_presentation (
   id bigserial,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   q_name varchar(256),
   q_analysis_segment varchar(100),
   q_search_params text,
   q_heatmap_grouping text,
   q_heatmap_insufficient_only boolean default false,
   fk_form_entry bigint not null,
   primary key (id)
);

ALTER TABLE public.o_qual_analysis_presentation ADD q_heatmap_insufficient_only bool NULL;

-- portfolio
alter table o_pf_page_part alter column p_layout_options type text;
