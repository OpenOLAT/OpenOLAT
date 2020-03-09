-- grading
alter table o_grad_time_record add g_metadata_time number(20) default 0 not null;

-- CATALOG
UPDATE o_catentry SET short_title = name;