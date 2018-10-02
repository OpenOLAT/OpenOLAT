-- quality management
alter table o_qual_context add q_location varchar2(1024);

create table o_qual_analysis_presentation (
   id number(20) generated always as identity,
   creationdate date not null,
   lastmodified date not null,
   q_name varchar2(256),
   q_analysis_segment varchar2(100),
   q_search_params CLOB,
   q_heatmap_grouping CLOB,
   q_heatmap_insufficient_only number default 0,
   fk_form_entry number(20) not null,
   primary key (id)
);
